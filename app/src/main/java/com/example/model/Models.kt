package com.example.model

enum class AcademicClass(val displayName: String, val shortLabel: String, val subtitle: String) {
    CLASS_9("Class 9", "IX", "BOSEM Secondary Stage - Foundation"),
    CLASS_10("Class 10", "X", "BOSEM HSLC Board Examination Stage")
}

enum class Subject(val displayName: String, val code: String, val iconLabel: String) {
    PHYSICS("Physics", "PHY", "atom"),
    CHEMISTRY("Chemistry", "CHM", "flask")
}

enum class MaterialCategory(val displayName: String, val shortName: String) {
    NOTES("Chapter Notes", "Notes"),
    PREVIOUS_YEAR_PAPERS("Previous 10 Years' Papers", "Past Papers"),
    SAMPLE_PAPERS("Sample & Model Papers", "Sample Papers"),
    IMPORTANT_QUESTIONS("Important Questions Bank", "Important Qs"),
    PRACTICE_TEST("Practice Tests", "Tests")
}

enum class QuestionType(val label: String, val badgeText: String) {
    MCQ("Multiple Choice Question", "MCQ (1 Mark)"),
    VERY_SHORT("Very Short Answer", "VSA (1 Mark)"),
    SHORT_ANSWER("Short Answer", "SA (2-3 Marks)"),
    LONG_ANSWER("Long Answer", "LA (5 Marks)"),
    NUMERICAL("Numerical Problem", "Numerical (3-5 Marks)"),
    CONCEPTUAL("Conceptual / Reasoning", "Reasoning (2-3 Marks)")
}

enum class QuestionDifficulty(val label: String) {
    BASIC("Basic"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

data class ImportantQuestion(
    val id: String,
    val chapterName: String,
    val academicClass: AcademicClass,
    val subject: Subject,
    val questionText: String,
    val type: QuestionType,
    val marks: Int,
    val yearAsked: String? = null,
    val answerExplanation: String,
    val mcqOptions: List<String> = emptyList(),
    val correctOptionIndex: Int = -1,
    val numericalGiven: String? = null,
    val numericalFormula: String? = null,
    val difficulty: QuestionDifficulty = QuestionDifficulty.INTERMEDIATE
)

/**
 * Core StudyMaterial data class annotated with Room annotations (@Entity, @PrimaryKey, @ColumnInfo)
 * to support local SQLite caching alongside Cloud Firestore integration.
 *
 * Includes fields for ID, title, description, class, subject, chapter, category,
 * year, fileUrl, verification status, and offline caching metadata.
 */
@androidx.room.Entity(tableName = "study_materials")
@androidx.room.TypeConverters(com.example.data.local.Converters::class)
@com.google.firebase.firestore.IgnoreExtraProperties
data class StudyMaterial(
    @androidx.room.PrimaryKey
    @com.google.firebase.firestore.DocumentId
    @androidx.room.ColumnInfo(name = "id")
    val id: String = "",

    @androidx.room.ColumnInfo(name = "title")
    val title: String = "",

    @androidx.room.ColumnInfo(name = "description")
    val description: String = "",

    @androidx.room.ColumnInfo(name = "academicClass")
    val academicClass: AcademicClass = AcademicClass.CLASS_10,

    @androidx.room.ColumnInfo(name = "subject")
    val subject: Subject = Subject.PHYSICS,

    @androidx.room.ColumnInfo(name = "chapterName")
    val chapterName: String = "",

    @androidx.room.ColumnInfo(name = "chapterNumber")
    val chapterNumber: Int = 1,

    @androidx.room.ColumnInfo(name = "category")
    val category: MaterialCategory = MaterialCategory.NOTES,

    @androidx.room.ColumnInfo(name = "examYear")
    val examYear: Int? = null, // e.g. 2024, 2023 for past papers

    @androidx.room.ColumnInfo(name = "academicYear")
    val academicYear: String = "2024-2025",

    @androidx.room.ColumnInfo(name = "year")
    val year: String = "2024-2025",

    @androidx.room.ColumnInfo(name = "fileUrl")
    val fileUrl: String = "",

    @androidx.room.ColumnInfo(name = "fileSize")
    val fileSize: String = "1.5 MB",

    @androidx.room.ColumnInfo(name = "fileSizeBytes")
    val fileSizeBytes: Long = 1024 * 512,

    @androidx.room.ColumnInfo(name = "isOfficialBoard")
    val isOfficialBoard: Boolean = true,

    @androidx.room.ColumnInfo(name = "isVerified")
    val isVerified: Boolean = true,

    @androidx.room.ColumnInfo(name = "verificationStatus")
    val verificationStatus: String = "VERIFIED",

    @androidx.room.ColumnInfo(name = "pageCount")
    val pageCount: Int = 8,

    @androidx.room.ColumnInfo(name = "downloadCount")
    val downloadCount: Int = 0,

    @androidx.room.ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),

    @androidx.room.ColumnInfo(name = "keyTakeaways")
    val keyTakeaways: List<String> = emptyList(),

    @androidx.room.ColumnInfo(name = "contentSummary")
    val contentSummary: String = "",

    @androidx.room.ColumnInfo(name = "sections")
    val sections: List<MaterialSection> = emptyList(),

    @androidx.room.ColumnInfo(name = "isBookmarked")
    val isBookmarked: Boolean = false,

    @androidx.room.ColumnInfo(name = "isDownloaded")
    val isDownloaded: Boolean = false,

    @androidx.room.ColumnInfo(name = "downloadedAt")
    val downloadedAt: Long? = null,

    @androidx.room.ColumnInfo(name = "syncedWithCloud")
    val syncedWithCloud: Boolean = false,

    @androidx.room.ColumnInfo(name = "lastModified")
    val lastModified: Long = System.currentTimeMillis()
) {
    // Aliases and accessors supporting different naming conventions
    @get:androidx.room.Ignore
    val chapter: String get() = chapterName

    @get:androidx.room.Ignore
    val className: String get() = academicClass.displayName

    @get:androidx.room.Ignore
    val `class`: String get() = academicClass.name

    @get:androidx.room.Ignore
    val subjectName: String get() = subject.displayName

    @get:androidx.room.Ignore
    val categoryName: String get() = category.displayName
}

data class MaterialSection(
    val title: String,
    val content: String,
    val formulaOrEquation: String? = null,
    val highlights: List<String> = emptyList()
)

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val tag: String,
    val isUrgent: Boolean = false,
    val targetClass: AcademicClass? = null
)

data class StudentProfile(
    val name: String = "PCM Student",
    val selectedClass: AcademicClass = AcademicClass.CLASS_10,
    val schoolName: String = "Imphal Higher Secondary / BOSEM School",
    val bosemRollNumber: String = "BOSEM-HSLC-2025",
    val email: String = "student@pcmstudycentre.org",
    val streakDays: Int = 7,
    val totalMaterialsStudied: Int = 18,
    val totalQuestionsPracticed: Int = 64,
    val isGuest: Boolean = false,
    val notificationsEnabled: Boolean = true
)

data class DownloadRecord(
    val materialId: String,
    val localFilePath: String,
    val downloadedAt: Long,
    val fileSizeBytes: Long,
    val isComplete: Boolean = true
)
