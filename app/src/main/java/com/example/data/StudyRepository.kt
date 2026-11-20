package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.StudyMaterialDao
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.remote.CloudStudyMaterial
import com.example.data.remote.toCloudModel
import com.example.data.remote.toEntity
import com.example.model.AcademicClass
import com.example.model.Announcement
import com.example.model.ImportantQuestion
import com.example.model.StudentProfile
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface SyncStatus {
    object Idle : SyncStatus
    object Syncing : SyncStatus
    data class Success(val syncedCount: Int, val timestamp: Long = System.currentTimeMillis()) : SyncStatus
    data class Error(val message: String) : SyncStatus
}

/**
 * Repository that uses [StudyMaterialDao] (Room) and [FirebaseFirestore] (Cloud)
 * to handle local SQLite caching, offline capabilities, and cloud data synchronization.
 */
class StudyRepository(
    val dao: StudyMaterialDao,
    val firestore: FirebaseFirestore? = null,
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
) {

    private var onlineStatusProvider: (() -> Boolean)? = null

    fun setOnlineStatusProvider(provider: () -> Boolean) {
        onlineStatusProvider = provider
    }

    fun isOnline(): Boolean {
        return onlineStatusProvider?.invoke() ?: true
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Reactive streams backed by Room Local Database (offline-first)
    val materials: StateFlow<List<StudyMaterial>> = dao.getAllMaterials()
        .map { list -> list.map { it.toDomainModel() } }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = CurriculumData.getInitialMaterials()
        )

    val bookmarks: StateFlow<Set<String>> = dao.getBookmarkedMaterials()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = setOf("c10-phy-ch1-notes", "bosem-hslc-paper-2024")
        )

    val downloadedIds: StateFlow<Set<String>> = dao.getDownloadedMaterials()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    private val _questions = MutableStateFlow<List<ImportantQuestion>>(CurriculumData.getImportantQuestions())
    val questions: StateFlow<List<ImportantQuestion>> = _questions.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(CurriculumData.announcements)
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val _studentProfile = MutableStateFlow(
        StudentProfile(
            name = "Thoiba Singh",
            selectedClass = AcademicClass.CLASS_10,
            schoolName = "Imphal Valley Academy",
            bosemRollNumber = "25HSLC-10492",
            email = "thoiba.singh@pcmstudy.org",
            streakDays = 5,
            totalMaterialsStudied = 14,
            totalQuestionsPracticed = 48
        )
    )
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _recentlyViewedIds = MutableStateFlow<List<String>>(listOf("c10-phy-ch1-notes", "c10-chm-ch1-notes"))
    val recentlyViewedIds: StateFlow<List<String>> = _recentlyViewedIds.asStateFlow()

    // Pomodoro Focus Sessions Tracking
    private val _focusSessions = MutableStateFlow<List<com.example.model.FocusSession>>(
        listOf(
            com.example.model.FocusSession(
                id = "fs-1",
                subject = Subject.PHYSICS,
                durationMinutes = 25,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
                chapterName = "Light - Reflection & Refraction"
            ),
            com.example.model.FocusSession(
                id = "fs-2",
                subject = Subject.CHEMISTRY,
                durationMinutes = 25,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 90,
                chapterName = "Chemical Reactions & Equations"
            ),
            com.example.model.FocusSession(
                id = "fs-3",
                subject = Subject.PHYSICS,
                durationMinutes = 25,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 20,
                chapterName = "Electricity Numerical Practice"
            )
        )
    )
    val focusSessions: StateFlow<List<com.example.model.FocusSession>> = _focusSessions.asStateFlow()

    // Practice Quiz Performance Tracking
    private val _quizAttempts = MutableStateFlow<List<com.example.model.QuizAttempt>>(
        listOf(
            com.example.model.QuizAttempt(
                id = "qa-1",
                subject = Subject.PHYSICS,
                chapterName = "Light & Optics",
                totalQuestions = 5,
                correctAnswers = 4,
                scorePercentage = 80,
                timestamp = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 6
            ),
            com.example.model.QuizAttempt(
                id = "qa-2",
                subject = Subject.CHEMISTRY,
                chapterName = "Acids, Bases & Salts",
                totalQuestions = 5,
                correctAnswers = 5,
                scorePercentage = 100,
                timestamp = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 4
            ),
            com.example.model.QuizAttempt(
                id = "qa-3",
                subject = Subject.PHYSICS,
                chapterName = "Electricity & Circuits",
                totalQuestions = 5,
                correctAnswers = 4,
                scorePercentage = 80,
                timestamp = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 2
            ),
            com.example.model.QuizAttempt(
                id = "qa-4",
                subject = Subject.CHEMISTRY,
                chapterName = "Metals & Non-Metals",
                totalQuestions = 5,
                correctAnswers = 5,
                scorePercentage = 100,
                timestamp = System.currentTimeMillis() - 1000L * 60 * 60 * 5
            )
        )
    )
    val quizAttempts: StateFlow<List<com.example.model.QuizAttempt>> = _quizAttempts.asStateFlow()

    // Digital Flashcard Mastery & Spaced Repetition (SRS)
    private val _flashcards = MutableStateFlow<List<com.example.model.Flashcard>>(
        com.example.util.SpacedRepetitionEngine.getInitialCurriculumFlashcards()
    )
    val flashcards: StateFlow<List<com.example.model.Flashcard>> = _flashcards.asStateFlow()

    private val _masteredFlashcardIds = MutableStateFlow<Set<String>>(setOf("fc-c10-p-1", "fc-c10-c-1"))
    val masteredFlashcardIds: StateFlow<Set<String>> = _masteredFlashcardIds.asStateFlow()

    // Calendar Study Planner Scheduled Sessions
    private val _scheduledSessions = MutableStateFlow<List<com.example.model.ScheduledStudySession>>(
        listOf(
            com.example.model.ScheduledStudySession(
                id = "plan-1",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Light - Reflection & Refraction",
                topicTitle = "Mirror & Lens Formula Derivations",
                scheduledEpochMillis = System.currentTimeMillis() + 1000 * 60 * 60 * 2,
                durationMinutes = 45,
                isCompleted = false,
                reminderMinutesBefore = 10,
                notes = "Solve BOSEM 2023 numericals on spherical mirrors",
                materialId = "c10-phy-ch1-notes"
            ),
            com.example.model.ScheduledStudySession(
                id = "plan-2",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Chemical Reactions & Equations",
                topicTitle = "Balancing Redox Reactions & Types",
                scheduledEpochMillis = System.currentTimeMillis() + 1000 * 60 * 60 * 5,
                durationMinutes = 30,
                isCompleted = false,
                reminderMinutesBefore = 10,
                notes = "Memorize precipitation and displacement reaction equations",
                materialId = "c10-chm-ch1-notes"
            ),
            com.example.model.ScheduledStudySession(
                id = "plan-3",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Electricity & Circuits",
                topicTitle = "Ohm's Law & Resistor Networks",
                scheduledEpochMillis = System.currentTimeMillis() + 1000 * 60 * 60 * 26,
                durationMinutes = 60,
                isCompleted = false,
                reminderMinutesBefore = 15,
                notes = "Review circuit diagrams and equivalent resistance formula",
                materialId = "c10-phy-ch3-notes"
            )
        )
    )
    val scheduledSessions: StateFlow<List<com.example.model.ScheduledStudySession>> = _scheduledSessions.asStateFlow()

    // Peer Discussion Forum State
    private val _forumQuestions = MutableStateFlow<List<com.example.model.ForumQuestion>>(ForumData.getInitialQuestions())
    val forumQuestions: StateFlow<List<com.example.model.ForumQuestion>> = _forumQuestions.asStateFlow()

    init {
        // Automatically seed local Room database if empty on launch
        scope.launch {
            seedInitialDataIfEmpty()
        }
    }

    /**
     * Seeds initial verified BOSEM curriculum materials into Room on first startup.
     */
    suspend fun seedInitialDataIfEmpty() = withContext(ioDispatcher) {
        val count = dao.getCount()
        if (count == 0) {
            val initial = CurriculumData.getInitialMaterials().map { mat ->
                mat.toEntity(
                    isBookmarked = (mat.id == "c10-phy-ch1-notes" || mat.id == "bosem-hslc-paper-2024")
                )
            }
            dao.insertMaterials(initial)
            Log.d(TAG, "Seeded ${initial.size} curriculum items into Room database.")
        }
    }

    /**
     * Synchronizes study materials between Firestore cloud collection and Room local database.
     * Preserves local device bookmarks and offline download states during merge.
     */
    suspend fun syncWithCloud(): Result<Int> = withContext(ioDispatcher) {
        if (!isOnline()) {
            val msg = "Offline: Network requests are prevented while disconnected."
            _syncStatus.value = SyncStatus.Error(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }
        val db = firestore ?: return@withContext Result.failure(
            IllegalStateException("FirebaseFirestore instance is not available or configured.")
        )
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (uid.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Sign in required for cloud sync."))
        }
        _syncStatus.value = SyncStatus.Syncing

        try {
            val snapshot = db.collection(COLLECTION_STUDY_MATERIALS)
                .whereEqualTo("visibility", "public")
                .get()
                .await()
            val cloudItems = snapshot.toObjects(CloudStudyMaterial::class.java)

            if (cloudItems.isNotEmpty()) {
                val entities = cloudItems.map { cloudMat ->
                    val local = dao.getMaterialById(cloudMat.id)
                    cloudMat.toEntity(
                        isBookmarked = local?.isBookmarked ?: false,
                        isDownloaded = local?.isDownloaded ?: false,
                        downloadedAt = local?.downloadedAt
                    )
                }
                dao.insertMaterials(entities)
                _syncStatus.value = SyncStatus.Success(entities.size)
                Log.d(TAG, "Synced ${entities.size} study materials from Firestore into Room.")
                Result.success(entities.size)
            } else {
                _syncStatus.value = SyncStatus.Success(0)
                Result.success(0)
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Failed to sync with cloud"
            _syncStatus.value = SyncStatus.Error(msg)
            Log.e(TAG, "Cloud sync error: $msg", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a study material to Firestore and saves it locally to Room.
     */
    suspend fun uploadMaterialToCloud(material: StudyMaterial): Result<Unit> = withContext(ioDispatcher) {
        // 1. Save locally to Room
        dao.insertMaterial(material.toEntity(syncedWithCloud = true))

        if (!isOnline()) {
            Log.d(TAG, "Offline mode: Material saved to local Room database. Cloud network upload suspended.")
            return@withContext Result.failure(IllegalStateException("Device is offline. Network requests are prevented."))
        }

        // 2. Upload to Firestore if configured and signed in
        val db = firestore
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (db != null && !uid.isNullOrBlank()) {
            try {
                db.collection(COLLECTION_STUDY_MATERIALS)
                    .document(material.id)
                    .set(material.toCloudModel().copy(uploaderId = uid, visibility = "public"))
                    .await()
                Log.d(TAG, "Uploaded material ${material.id} to Firestore.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading material to Firestore: ${e.message}", e)
                Result.failure(e)
            }
        } else {
            Result.success(Unit)
        }
    }

    /**
     * Deletes a material from both the local Room database and Cloud Firestore.
     */
    suspend fun deleteMaterial(materialId: String): Result<Unit> = withContext(ioDispatcher) {
        dao.deleteMaterialById(materialId)
        val db = firestore
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (db != null && !uid.isNullOrBlank()) {
            try {
                db.collection(COLLECTION_STUDY_MATERIALS)
                    .document(materialId)
                    .delete()
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting material from Firestore: ${e.message}", e)
                Result.failure(e)
            }
        } else {
            Result.success(Unit)
        }
    }

    fun syncDownloadsWithDisk(context: Context) {
        scope.launch(ioDispatcher) {
            val currentMaterials = dao.getAllMaterials()
            // Check downloaded files on disk
            materials.value.forEach { mat ->
                val onDisk = PdfManager.isDownloaded(context, mat)
                val local = dao.getMaterialById(mat.id)
                if (local != null && local.isDownloaded != onDisk) {
                    dao.updateDownloadStatus(mat.id, onDisk, if (onDisk) System.currentTimeMillis() else null)
                }
            }
        }
    }

    fun toggleBookmark(materialId: String) {
        scope.launch(ioDispatcher) {
            val isCurrentlyBookmarked = bookmarks.value.contains(materialId)
            dao.updateBookmarkStatus(materialId, !isCurrentlyBookmarked)
        }
    }

    fun recordView(materialId: String) {
        _recentlyViewedIds.update { list ->
            listOf(materialId) + list.filter { it != materialId }.take(7)
        }
        _studentProfile.update { current ->
            current.copy(totalMaterialsStudied = current.totalMaterialsStudied + 1)
        }
    }

    fun recordQuestionPracticed() {
        _studentProfile.update { current ->
            current.copy(totalQuestionsPracticed = current.totalQuestionsPracticed + 1)
        }
    }

    fun recordFocusSession(session: com.example.model.FocusSession) {
        _focusSessions.update { listOf(session) + it }
        _studentProfile.update { current ->
            current.copy(
                totalMaterialsStudied = current.totalMaterialsStudied + 1
            )
        }
    }

    fun recordQuizAttempt(attempt: com.example.model.QuizAttempt) {
        _quizAttempts.update { listOf(attempt) + it }
        _studentProfile.update { current ->
            current.copy(
                totalQuestionsPracticed = current.totalQuestionsPracticed + attempt.totalQuestions
            )
        }
    }

    fun toggleFlashcardMastered(cardId: String) {
        val willMaster = !_masteredFlashcardIds.value.contains(cardId)
        _masteredFlashcardIds.update { set ->
            if (willMaster) set + cardId else set - cardId
        }
        _flashcards.update { list ->
            list.map {
                if (it.id == cardId) {
                    it.copy(
                        isMastered = willMaster,
                        boxLevel = if (willMaster) 5 else 1
                    )
                } else it
            }
        }
    }

    /**
     * Applies the Spaced-Repetition Algorithm (Leitner 5-Box & SM-2) on user rating.
     */
    fun processSrsReview(cardId: String, rating: com.example.model.SrsRating): com.example.model.Flashcard? {
        var updatedCard: com.example.model.Flashcard? = null
        _flashcards.update { currentList ->
            currentList.map { card ->
                if (card.id == cardId) {
                    val processed = com.example.util.SpacedRepetitionEngine.processReview(card, rating)
                    updatedCard = processed
                    processed
                } else card
            }
        }
        updatedCard?.let { card ->
            if (card.isMastered) {
                _masteredFlashcardIds.update { it + cardId }
            } else {
                _masteredFlashcardIds.update { it - cardId }
            }
        }
        return updatedCard
    }

    fun addCustomFlashcard(card: com.example.model.Flashcard) {
        _flashcards.update { current -> listOf(card) + current }
    }

    fun resetFlashcardSrs(cardId: String) {
        _flashcards.update { current ->
            current.map {
                if (it.id == cardId) it.copy(
                    boxLevel = 1,
                    intervalDays = 1,
                    repetitions = 0,
                    easeFactor = 2.5f,
                    nextReviewEpochMillis = System.currentTimeMillis(),
                    isMastered = false
                ) else it
            }
        }
        _masteredFlashcardIds.update { it - cardId }
    }

    fun scheduleSession(session: com.example.model.ScheduledStudySession, context: Context? = null) {
        _scheduledSessions.update { current ->
            (current.filter { it.id != session.id } + session).sortedBy { it.scheduledEpochMillis }
        }
        if (context != null) {
            com.example.util.StudyNotificationHelper.scheduleSessionNotification(context, session)
        }
    }

    fun toggleSessionCompleted(sessionId: String) {
        _scheduledSessions.update { list ->
            list.map { if (it.id == sessionId) it.copy(isCompleted = !it.isCompleted) else it }
        }
    }

    fun deleteScheduledSession(sessionId: String, context: Context? = null) {
        _scheduledSessions.update { list -> list.filter { it.id != sessionId } }
        if (context != null) {
            com.example.util.StudyNotificationHelper.cancelSessionNotification(context, sessionId)
        }
    }

    fun triggerDemoNotification(session: com.example.model.ScheduledStudySession, context: Context) {
        com.example.util.StudyNotificationHelper.triggerImmediateNotification(context, session)
    }

    fun postForumQuestion(question: com.example.model.ForumQuestion) {
        _forumQuestions.update { current ->
            listOf(question) + current
        }
    }

    fun addForumReply(questionId: String, reply: com.example.model.ForumReply) {
        _forumQuestions.update { questions ->
            questions.map { q ->
                if (q.id == questionId) {
                    q.copy(replies = q.replies + reply)
                } else q
            }
        }
    }

    fun toggleQuestionUpvote(questionId: String) {
        _forumQuestions.update { questions ->
            questions.map { q ->
                if (q.id == questionId) {
                    val newUpvoted = !q.userUpvoted
                    val newCount = if (newUpvoted) q.upvotesCount + 1 else (q.upvotesCount - 1).coerceAtLeast(0)
                    q.copy(userUpvoted = newUpvoted, upvotesCount = newCount)
                } else q
            }
        }
    }

    fun toggleReplyUpvote(questionId: String, replyId: String) {
        _forumQuestions.update { questions ->
            questions.map { q ->
                if (q.id == questionId) {
                    val updatedReplies = q.replies.map { r ->
                        if (r.id == replyId) {
                            val newUpvoted = !r.userUpvoted
                            val newCount = if (newUpvoted) r.upvotesCount + 1 else (r.upvotesCount - 1).coerceAtLeast(0)
                            r.copy(userUpvoted = newUpvoted, upvotesCount = newCount)
                        } else r
                    }
                    q.copy(replies = updatedReplies)
                } else q
            }
        }
    }

    fun toggleMarkSolutionAccepted(questionId: String, replyId: String) {
        _forumQuestions.update { questions ->
            questions.map { q ->
                if (q.id == questionId) {
                    val targetReply = q.replies.find { it.id == replyId }
                    val willBeAccepted = !(targetReply?.isAcceptedSolution ?: false)
                    val updatedReplies = q.replies.map { r ->
                        if (r.id == replyId) r.copy(isAcceptedSolution = willBeAccepted)
                        else r.copy(isAcceptedSolution = false)
                    }
                    q.copy(replies = updatedReplies, isSolved = willBeAccepted)
                } else q
            }
        }
    }

    fun deleteForumQuestion(questionId: String) {
        _forumQuestions.update { questions -> questions.filter { it.id != questionId } }
    }

    fun getTodayStudyMinutes(): Int {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24L * 60 * 60 * 1000))
        return _focusSessions.value
            .filter { it.timestamp >= startOfDay && it.isCompleted }
            .sumOf { it.durationMinutes }
    }

    fun getTodayCompletedSessionsCount(): Int {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24L * 60 * 60 * 1000))
        return _focusSessions.value
            .count { it.timestamp >= startOfDay && it.isCompleted }
    }

    fun getLast30DaysProgress(): List<com.example.model.DailyStudyRecord> {
        val list = mutableListOf<com.example.model.DailyStudyRecord>()
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000

        for (i in 29 downTo 0) {
            val dayStart = now - (i * dayMillis)
            val dayEnd = dayStart + dayMillis
            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = dayStart }
            val month = calendar.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.SHORT, java.util.Locale.US) ?: "Day"
            val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val label = "$month $dayOfMonth"

            val minutes = _focusSessions.value
                .filter { it.timestamp in dayStart until dayEnd }
                .sumOf { it.durationMinutes }
                .let { if (it == 0 && (i % 3 == 0 || i % 4 == 0)) ((i * 7) % 45 + 15) else it }

            val quizScore = _quizAttempts.value
                .filter { it.timestamp in dayStart until dayEnd }
                .map { it.scorePercentage }
                .average()
                .let { if (it.isNaN()) { if (i % 5 == 0) (70 + (i * 3) % 25) else null } else it.toInt() }

            list.add(
                com.example.model.DailyStudyRecord(
                    dayLabel = label,
                    dateEpochDays = dayStart / dayMillis,
                    minutesStudied = minutes,
                    quizScorePercent = quizScore
                )
            )
        }
        return list
    }

    fun downloadMaterial(context: Context, material: StudyMaterial, downloadScope: CoroutineScope) {
        if (downloadedIds.value.contains(material.id)) return
        if (!isOnline()) {
            Log.w(TAG, "Offline mode active: Network download prevented while offline.")
            return
        }

        downloadScope.launch(ioDispatcher) {
            for (step in 1..10) {
                _downloadProgress.update { map -> map + (material.id to (step * 0.1f)) }
                delay(80)
            }

            PdfManager.generateOrGetPdfFile(context, material)

            _downloadProgress.update { map -> map - material.id }
            dao.updateDownloadStatus(material.id, true, System.currentTimeMillis())

            // Increment download count locally
            val local = dao.getMaterialById(material.id)
            if (local != null) {
                dao.updateMaterial(local.copy(downloadCount = local.downloadCount + 1))
            }
        }
    }

    fun deleteDownloadedMaterial(context: Context, material: StudyMaterial) {
        scope.launch(ioDispatcher) {
            PdfManager.deleteDownloadedFile(context, material)
            dao.updateDownloadStatus(material.id, false, null)
        }
    }

    fun clearAllDownloads(context: Context) {
        scope.launch(ioDispatcher) {
            PdfManager.deleteAllDownloadedFiles(context)
            dao.clearAllDownloads()
        }
    }

    fun updateProfile(name: String, academicClass: AcademicClass, school: String, rollNo: String) {
        _studentProfile.update {
            it.copy(
                name = name.ifBlank { "PCM Student" },
                selectedClass = academicClass,
                schoolName = school.ifBlank { "BOSEM Secondary School" },
                bosemRollNumber = rollNo.ifBlank { "BOSEM-STUDENT" }
            )
        }
    }

    fun updateSelectedClass(academicClass: AcademicClass) {
        _studentProfile.update { it.copy(selectedClass = academicClass) }
    }

    fun adminAddMaterial(material: StudyMaterial) {
        scope.launch(ioDispatcher) {
            uploadMaterialToCloud(material)
        }
    }

    fun adminDeleteMaterial(materialId: String) {
        scope.launch(ioDispatcher) {
            deleteMaterial(materialId)
        }
    }

    fun adminToggleVerified(materialId: String) {
        scope.launch(ioDispatcher) {
            val local = dao.getMaterialById(materialId)
            if (local != null) {
                val updated = local.copy(isVerified = !local.isVerified)
                dao.updateMaterial(updated)
                val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
                if (!uid.isNullOrBlank()) {
                    firestore?.collection(COLLECTION_STUDY_MATERIALS)
                        ?.document(materialId)
                        ?.update("verified", updated.isVerified)
                }
            }
        }
    }

    fun adminAddAnnouncement(announcement: Announcement) {
        _announcements.update { listOf(announcement) + it }
    }

    fun verifyAdminPin(pin: String): Boolean {
        return pin == "2026" || pin == "1234"
    }

    companion object {
        private const val TAG = "StudyRepository"
        private const val COLLECTION_STUDY_MATERIALS = "study_materials"

        @Volatile
        private var instance: StudyRepository? = null

        fun initialize(context: Context, firestore: FirebaseFirestore? = null): StudyRepository {
            return synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val fs = firestore ?: try {
                    FirebaseFirestore.getInstance(
                        context.applicationContext.getString(R.string.firestore_database_id)
                    )
                } catch (e: Exception) {
                    null
                }
                StudyRepository(db.studyMaterialDao(), fs).also { instance = it }
            }
        }

        fun getInstance(context: Context): StudyRepository {
            return instance ?: synchronized(this) {
                instance ?: initialize(context)
            }
        }

        fun getInstance(): StudyRepository {
            return instance ?: synchronized(this) {
                instance ?: error("StudyRepository must be initialized with Context. Call getInstance(context) first.")
            }
        }
    }
}
