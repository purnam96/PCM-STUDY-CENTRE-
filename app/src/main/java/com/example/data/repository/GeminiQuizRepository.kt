package com.example.data.repository

import android.util.Log
import com.example.model.AcademicClass
import com.example.model.MaterialCategory
import com.example.model.QuizQuestion
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiQuizRepository"
private const val QUIZ_MODEL_NAME = "gemini-3.8-flash"
private const val GENERATION_TIMEOUT_MS = 60_000L

enum class QuizFocusMode(val label: String, val promptInstruction: String) {
    BALANCED(
        label = "Balanced",
        promptInstruction = "Include a balanced mix of conceptual understanding, definitions, laws, and applied numerical or chemical reaction questions."
    ),
    FORMULAS_AND_NUMERICALS(
        label = "Formulas & Numericals",
        promptInstruction = "Emphasize formulas, equations, SI units, balanced chemical equations, and step-by-step numerical calculations."
    ),
    CONCEPTUAL(
        label = "Conceptual",
        promptInstruction = "Emphasize scientific reasoning, underlying laws, everyday applications, and experimental observations."
    )
}

interface QuizGeneratorRepository {
    suspend fun generateQuizFromSyllabus(
        subject: Subject,
        academicClass: AcademicClass,
        materials: List<StudyMaterial>,
        chapterFilter: String? = null,
        focusMode: QuizFocusMode = QuizFocusMode.BALANCED,
        customTopic: String = "",
        questionCount: Int = 5
    ): Result<List<QuizQuestion>>
}

/**
 * Repository that generates structured multiple-choice Practice Quizzes directly from BOSEM
 * syllabus notes using Firebase AI Logic (Gemini).
 */
class GeminiQuizRepository : QuizGeneratorRepository {

    private val quizSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "chapterName" to Schema.string(
                            description = "Exact syllabus chapter name that this question tests"
                        ),
                        "questionText" to Schema.string(
                            description = "Clear multiple-choice question stem grounded in the provided syllabus notes"
                        ),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Exactly four distinct answer options without letter prefixes like A. or B."
                        ),
                        "correctIndex" to Schema.integer(
                            description = "0-based index (0, 1, 2, or 3) pointing to the single correct option"
                        ),
                        "explanation" to Schema.string(
                            description = "Concise explanation referencing the syllabus law, formula, reaction, or concept"
                        )
                    )
                ),
                description = "Array of multiple-choice questions generated strictly from the syllabus notes"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = QUIZ_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quizSchema
                temperature = 0.4f
            }
        )
    }

    override suspend fun generateQuizFromSyllabus(
        subject: Subject,
        academicClass: AcademicClass,
        materials: List<StudyMaterial>,
        chapterFilter: String?,
        focusMode: QuizFocusMode,
        customTopic: String,
        questionCount: Int
    ): Result<List<QuizQuestion>> {
        val targetMaterials = selectRelevantMaterials(
            subject = subject,
            academicClass = academicClass,
            materials = materials,
            chapterFilter = chapterFilter
        )

        if (targetMaterials.isEmpty()) {
            val scope = chapterFilter ?: "${academicClass.displayName} ${subject.displayName}"
            return Result.failure(
                IllegalStateException("No syllabus notes found for $scope.")
            )
        }

        val prompt = buildSyllabusPrompt(
            subject = subject,
            academicClass = academicClass,
            materials = targetMaterials,
            chapterFilter = chapterFilter,
            focusMode = focusMode,
            customTopic = customTopic,
            questionCount = questionCount
        )

        return try {
            val response = withTimeout(GENERATION_TIMEOUT_MS) {
                model.generateContent(prompt)
            }
            val rawJson = response.text
                ?: throw IllegalStateException("Gemini returned an empty response.")

            val fallbackChapter = chapterFilter
                ?: targetMaterials.firstOrNull()?.chapterName
                ?: subject.displayName

            val questions = parseQuizResponseJson(
                jsonString = rawJson,
                subject = subject,
                fallbackChapter = fallbackChapter
            )
            Result.success(questions)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Practice quiz generation timed out after ${GENERATION_TIMEOUT_MS}ms", e)
            Result.failure(
                IllegalStateException(
                    "Quiz generation timed out after 60 seconds. Check your connection and try again.",
                    e
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate practice quiz via Firebase AI Logic", e)
            Result.failure(e)
        }
    }

    companion object {
        internal fun selectRelevantMaterials(
            subject: Subject,
            academicClass: AcademicClass,
            materials: List<StudyMaterial>,
            chapterFilter: String?
        ): List<StudyMaterial> {
            val subjectClassMaterials = materials.filter {
                it.subject == subject && it.academicClass == academicClass
            }
            val filteredByChapter = if (chapterFilter.isNullOrBlank()) {
                subjectClassMaterials
            } else {
                subjectClassMaterials.filter {
                    it.chapterName.equals(chapterFilter, ignoreCase = true)
                }
            }
            val notesFirst = filteredByChapter.sortedBy { mat ->
                if (mat.category == MaterialCategory.NOTES) 0 else 1
            }
            return notesFirst
        }

        fun buildSyllabusPrompt(
            subject: Subject,
            academicClass: AcademicClass,
            materials: List<StudyMaterial>,
            chapterFilter: String? = null,
            focusMode: QuizFocusMode = QuizFocusMode.BALANCED,
            customTopic: String = "",
            questionCount: Int = 5
        ): String {
            val boundedCount = questionCount.coerceIn(1, 10)
            val contextBuilder = StringBuilder()
            val maxContextChars = 12_000

            materials.forEach { mat ->
                if (contextBuilder.length >= maxContextChars) return@forEach

                contextBuilder.appendLine("--- Syllabus Note ---")
                contextBuilder.appendLine("Chapter ${mat.chapterNumber}: ${mat.chapterName}")
                contextBuilder.appendLine("Title: ${mat.title}")
                if (mat.description.isNotBlank()) {
                    contextBuilder.appendLine("Overview: ${mat.description}")
                }
                if (mat.contentSummary.isNotBlank()) {
                    contextBuilder.appendLine("Summary: ${mat.contentSummary}")
                }
                if (mat.keyTakeaways.isNotEmpty()) {
                    contextBuilder.appendLine("Key Takeaways:")
                    mat.keyTakeaways.forEach { takeaway ->
                        contextBuilder.appendLine("  - $takeaway")
                    }
                }
                if (mat.sections.isNotEmpty()) {
                    contextBuilder.appendLine("Sections:")
                    mat.sections.forEach { section ->
                        contextBuilder.appendLine("  * ${section.title}: ${section.content}")
                        if (!section.formulaOrEquation.isNullOrBlank()) {
                            contextBuilder.appendLine("    Formula/Equation: ${section.formulaOrEquation}")
                        }
                        if (section.highlights.isNotEmpty()) {
                            contextBuilder.appendLine("    Highlights: ${section.highlights.joinToString("; ")}")
                        }
                    }
                }
                contextBuilder.appendLine()
            }

            val chapterScope = if (chapterFilter.isNullOrBlank()) {
                "All chapters present in the syllabus notes below"
            } else {
                "Specifically chapter \"$chapterFilter\""
            }

            val topicDirective = if (customTopic.isNotBlank()) {
                "\n- Student Focus Request: Prioritize questions related to \"${customTopic.trim()}\" within the provided notes."
            } else {
                ""
            }

            return """
                You are an experienced secondary school ${subject.displayName} teacher preparing a practice quiz for ${academicClass.displayName} (${academicClass.subtitle}) students.
                
                Generate exactly $boundedCount high-quality multiple-choice questions strictly grounded in the syllabus notes provided below.
                
                Requirements:
                - Subject: ${subject.displayName} (${academicClass.displayName})
                - Chapter Scope: $chapterScope
                - Focus Style (${focusMode.label}): ${focusMode.promptInstruction}$topicDirective
                - Each question must have exactly 4 plausible, distinct options (no "A.", "B.", "1)", etc. prefixes).
                - Set correctIndex to an integer 0, 1, 2, or 3 matching the single correct option.
                - Vary correctIndex across questions rather than always putting the answer at index 0.
                - Provide a clear, educational explanation citing the exact law, formula, chemical equation, or concept from the notes.
                
                SYLLABUS NOTES:
                ${contextBuilder.toString().take(maxContextChars)}
            """.trimIndent()
        }

        fun parseQuizResponseJson(
            jsonString: String,
            subject: Subject,
            fallbackChapter: String
        ): List<QuizQuestion> {
            val root = JSONObject(jsonString)
            val questionsArray = root.getJSONArray("questions")
            val parsedQuestions = mutableListOf<QuizQuestion>()

            for (i in 0 until questionsArray.length()) {
                val item = questionsArray.getJSONObject(i)
                val chapterName = item.optString("chapterName", fallbackChapter)
                    .trim()
                    .ifBlank { fallbackChapter }
                val questionText = item.getString("questionText").trim()
                val optionsArray = item.getJSONArray("options")
                val options = (0 until optionsArray.length())
                    .map { idx -> optionsArray.getString(idx).trim() }
                    .filter { it.isNotEmpty() }

                val correctIndex = item.getInt("correctIndex")
                val explanation = item.optString("explanation", "").trim()

                if (questionText.isBlank()) {
                    throw IllegalStateException("Generated question at index $i had blank questionText.")
                }
                if (options.size != 4) {
                    throw IllegalStateException(
                        "Generated question \"$questionText\" had ${options.size} options instead of 4."
                    )
                }
                if (correctIndex !in 0..3) {
                    throw IllegalStateException(
                        "Generated question \"$questionText\" had out-of-range correctIndex: $correctIndex."
                    )
                }

                parsedQuestions.add(
                    QuizQuestion(
                        id = "ai-${subject.code.lowercase()}-$i-${System.currentTimeMillis()}",
                        subject = subject,
                        chapterName = chapterName,
                        questionText = questionText,
                        options = options,
                        correctIndex = correctIndex,
                        explanation = explanation.ifBlank {
                            "Review $chapterName in your ${subject.displayName} syllabus notes."
                        }
                    )
                )
            }

            if (parsedQuestions.isEmpty()) {
                throw IllegalStateException("Gemini returned zero valid quiz questions.")
            }

            return parsedQuestions
        }
    }
}
