package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.GeminiQuizRepository
import com.example.data.repository.QuizFocusMode
import com.example.data.repository.QuizGeneratorRepository
import com.example.model.AcademicClass
import com.example.model.QuizAttempt
import com.example.model.QuizQuestion
import com.example.model.StudyMaterial
import com.example.model.Subject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface QuizUiState {
    data object Idle : QuizUiState
    data object Loading : QuizUiState
    data class Ready(val questions: List<QuizQuestion>) : QuizUiState
    data class Error(val message: String) : QuizUiState
}

/**
 * ViewModel managing AI Practice Quiz configuration, Firebase AI Logic generation state,
 * interactive answer tracking, and score calculation.
 */
class PracticeQuizViewModel(
    private val quizRepository: QuizGeneratorRepository = GeminiQuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Idle)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _activeSubject = MutableStateFlow(Subject.PHYSICS)
    val activeSubject: StateFlow<Subject> = _activeSubject.asStateFlow()

    private val _selectedChapter = MutableStateFlow<String?>(null)
    val selectedChapter: StateFlow<String?> = _selectedChapter.asStateFlow()

    private val _focusMode = MutableStateFlow(QuizFocusMode.BALANCED)
    val focusMode: StateFlow<QuizFocusMode> = _focusMode.asStateFlow()

    private val _questionCount = MutableStateFlow(5)
    val questionCount: StateFlow<Int> = _questionCount.asStateFlow()

    private val _customTopic = MutableStateFlow("")
    val customTopic: StateFlow<String> = _customTopic.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers.asStateFlow()

    private val _isSubmitted = MutableStateFlow(false)
    val isSubmitted: StateFlow<Boolean> = _isSubmitted.asStateFlow()

    private val _attemptRecorded = MutableStateFlow(false)
    val attemptRecorded: StateFlow<Boolean> = _attemptRecorded.asStateFlow()

    private val _lastAttempt = MutableStateFlow<QuizAttempt?>(null)
    val lastAttempt: StateFlow<QuizAttempt?> = _lastAttempt.asStateFlow()

    private var generationJob: Job? = null
    private var lastLoadedClass: AcademicClass? = null

    fun ensureInitialQuizLoaded(academicClass: AcademicClass, materials: List<StudyMaterial>) {
        if (materials.isEmpty()) return
        if (_uiState.value is QuizUiState.Idle || lastLoadedClass != academicClass) {
            if (lastLoadedClass != academicClass) {
                _selectedChapter.value = null
            }
            generateQuiz(academicClass, materials)
        }
    }

    fun selectSubject(
        subject: Subject,
        academicClass: AcademicClass,
        materials: List<StudyMaterial>
    ) {
        if (_activeSubject.value == subject && _uiState.value !is QuizUiState.Idle) return
        _activeSubject.value = subject
        _selectedChapter.value = null
        if (materials.isNotEmpty()) {
            generateQuiz(academicClass, materials)
        }
    }

    fun selectChapter(chapterName: String?) {
        _selectedChapter.value = chapterName
    }

    fun setFocusMode(mode: QuizFocusMode) {
        _focusMode.value = mode
    }

    fun setQuestionCount(count: Int) {
        _questionCount.value = count.coerceIn(1, 10)
    }

    fun setCustomTopic(topic: String) {
        _customTopic.value = topic
    }

    fun generateQuiz(academicClass: AcademicClass, materials: List<StudyMaterial>) {
        generationJob?.cancel()
        lastLoadedClass = academicClass
        _currentIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _isSubmitted.value = false
        _attemptRecorded.value = false
        _lastAttempt.value = null
        _uiState.value = QuizUiState.Loading

        val subject = _activeSubject.value
        val chapterFilter = _selectedChapter.value
        val mode = _focusMode.value
        val count = _questionCount.value
        val topic = _customTopic.value

        generationJob = viewModelScope.launch {
            try {
                val result = quizRepository.generateQuizFromSyllabus(
                    subject = subject,
                    academicClass = academicClass,
                    materials = materials,
                    chapterFilter = chapterFilter,
                    focusMode = mode,
                    customTopic = topic,
                    questionCount = count
                )
                result.fold(
                    onSuccess = { questions ->
                        _uiState.value = QuizUiState.Ready(questions)
                    },
                    onFailure = { error ->
                        val msg = error.localizedMessage?.takeIf { it.isNotBlank() }
                            ?: "Unable to generate quiz from syllabus notes. Please try again."
                        _uiState.value = QuizUiState.Error(msg)
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(
                    e.localizedMessage ?: "Failed to generate practice quiz."
                )
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        if (_isSubmitted.value) return
        _selectedAnswers.value = _selectedAnswers.value + (questionIndex to optionIndex)
    }

    fun nextQuestion() {
        val questions = (_uiState.value as? QuizUiState.Ready)?.questions ?: return
        if (_currentIndex.value < questions.lastIndex) {
            _currentIndex.value += 1
        }
    }

    fun previousQuestion() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
        }
    }

    fun jumpToQuestion(index: Int) {
        val questions = (_uiState.value as? QuizUiState.Ready)?.questions ?: return
        if (index in questions.indices) {
            _currentIndex.value = index
        }
    }

    fun submitQuiz(onAttemptRecorded: (QuizAttempt) -> Unit) {
        val questions = (_uiState.value as? QuizUiState.Ready)?.questions ?: return
        if (questions.isEmpty()) return

        val answers = _selectedAnswers.value
        val correctCount = questions.indices.count { index ->
            answers[index] == questions[index].correctIndex
        }
        val percentage = (correctCount * 100) / questions.size
        val chapterSummary = _selectedChapter.value
            ?: questions.map { it.chapterName }.distinct().take(2).joinToString(" & ")
                .ifBlank { _activeSubject.value.displayName }

        val attempt = QuizAttempt(
            subject = _activeSubject.value,
            chapterName = chapterSummary,
            totalQuestions = questions.size,
            correctAnswers = correctCount,
            scorePercentage = percentage
        )

        _isSubmitted.value = true
        _lastAttempt.value = attempt
        if (!_attemptRecorded.value) {
            _attemptRecorded.value = true
            onAttemptRecorded(attempt)
        }
    }
}
