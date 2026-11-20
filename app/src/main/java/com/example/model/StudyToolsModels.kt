package com.example.model

/**
 * Focus session record for Pomodoro study timer.
 */
data class FocusSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val subject: Subject = Subject.PHYSICS,
    val durationMinutes: Int = 25,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true,
    val sessionType: FocusSessionType = FocusSessionType.FOCUS,
    val chapterName: String = ""
)

enum class FocusSessionType(val label: String, val defaultMinutes: Int) {
    FOCUS("Focus Session", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

/**
 * Multiple-choice question for AI Practice Quiz.
 */
data class QuizQuestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val subject: Subject = Subject.PHYSICS,
    val chapterName: String = "",
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = "",
    val selectedIndex: Int? = null
)

/**
 * Quiz submission result for tracking learning progress.
 */
data class QuizAttempt(
    val id: String = java.util.UUID.randomUUID().toString(),
    val subject: Subject = Subject.PHYSICS,
    val chapterName: String = "",
    val totalQuestions: Int = 5,
    val correctAnswers: Int = 4,
    val scorePercentage: Int = 80,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Spaced Repetition Rating for SuperMemo SM-2 & Leitner Box algorithm.
 */
enum class SrsRating(val score: Int, val label: String, val shortDescription: String) {
    AGAIN(1, "Again", "Forgot • Reset to Box 1"),
    HARD(2, "Hard", "Difficult recall • Box unchanged"),
    GOOD(3, "Good", "Correct recall • Next Box"),
    EASY(4, "Easy", "Effortless • Jump +2 Boxes")
}

/**
 * Leitner Box category in Spaced Repetition System.
 */
enum class LeitnerBox(val boxNumber: Int, val title: String, val defaultIntervalDays: Int) {
    BOX_1(1, "Daily Review", 1),
    BOX_2(2, "3-Day Review", 3),
    BOX_3(3, "Weekly Review", 7),
    BOX_4(4, "Bi-Weekly Review", 14),
    BOX_5(5, "Mastered / Monthly", 30);

    companion object {
        fun fromNumber(box: Int): LeitnerBox {
            return entries.firstOrNull { it.boxNumber == box.coerceIn(1, 5) } ?: BOX_1
        }
    }
}

/**
 * Digital flashcard for memorizing formulas, definitions, and laws with spaced repetition tracking.
 */
data class Flashcard(
    val id: String = java.util.UUID.randomUUID().toString(),
    val subject: Subject = Subject.PHYSICS,
    val chapterName: String = "",
    val frontTitle: String = "",
    val frontSubtitle: String = "",
    val backContent: String = "",
    val keyPoints: List<String> = emptyList(),
    val isMastered: Boolean = false,
    val academicClass: AcademicClass = AcademicClass.CLASS_10,
    // Spaced-Repetition Algorithm fields (Leitner & SM-2)
    val boxLevel: Int = 1, // 1 to 5
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val nextReviewEpochMillis: Long = System.currentTimeMillis(),
    val lastReviewedEpochMillis: Long? = null,
    val isCustom: Boolean = false
) {
    val isDueForReview: Boolean
        get() = System.currentTimeMillis() >= nextReviewEpochMillis

    val leitnerBox: LeitnerBox
        get() = LeitnerBox.fromNumber(boxLevel)
}

/**
 * Daily study point for progress tracking charts over the last 30 days.
 */
data class DailyStudyRecord(
    val dayLabel: String, // e.g. "Oct 12"
    val dateEpochDays: Long,
    val minutesStudied: Int,
    val quizScorePercent: Int?
)

/**
 * Calendar-based scheduled study session with topic/chapter review and push reminder settings.
 */
data class ScheduledStudySession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val subject: Subject = Subject.PHYSICS,
    val academicClass: AcademicClass = AcademicClass.CLASS_10,
    val chapterName: String = "",
    val topicTitle: String = "",
    val scheduledEpochMillis: Long = System.currentTimeMillis() + 1000 * 60 * 60 * 2,
    val durationMinutes: Int = 45,
    val isCompleted: Boolean = false,
    val reminderMinutesBefore: Int = 10,
    val materialId: String? = null,
    val notes: String = ""
)

/**
 * Module progress record visualizing syllabus chapter completion and quiz performance
 * across Physics and Chemistry modules for the Recharts and native analytics dashboards.
 */
data class ModuleProgress(
    val id: String,
    val chapterNumber: Int,
    val chapterName: String,
    val subject: Subject,
    val academicClass: AcademicClass,
    val completionRatePercent: Int, // 0 - 100%
    val quizScoreAveragePercent: Int, // 0 - 100%
    val quizzesAttempted: Int,
    val studyMinutes: Int,
    val masteryLevel: String, // "Mastered", "Proficient", "In Progress", "Needs Review"
    val keyConcept: String,
    val totalTopicsCount: Int = 5,
    val completedTopicsCount: Int = 4
)
