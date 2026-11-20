package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.model.AcademicClass
import com.example.model.Announcement
import com.example.model.ImportantQuestion
import com.example.model.MaterialCategory
import com.example.model.QuestionType
import com.example.model.StudyMaterial
import com.example.model.Subject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val label: String, val iconDescription: String) {
    HOME("Home", "Dashboard and recommended resources"),
    CLASSES("Classes", "Class 9 and 10 subject libraries"),
    PLANNER("Study Planner", "Calendar study planner and session reminders"),
    FORUM("Forum", "Peer discussion forum for Physics & Chemistry"),
    FAVORITES("Favorites", "Flagged study materials and notes"),
    TIMER("Focus Timer", "Pomodoro study timer and daily statistics"),
    PRACTICE("Practice & AI", "AI Practice Quizzes, Flashcards & Progress"),
    PAPERS("Papers", "10-Year past papers and sample tests"),
    DOWNLOADS("Downloads", "Offline downloaded study materials"),
    PROFILE("Profile", "Student account and settings")
}

/**
 * Main application ViewModel extending [BaseStudyViewModel] to inherit
 * subject-wise state management while managing screen destinations, search filters,
 * and admin dashboard capabilities.
 */
class MainViewModel(application: Application) : BaseStudyViewModel(application) {

    val materials: StateFlow<List<StudyMaterial>> = allMaterials
    val questions: StateFlow<List<ImportantQuestion>> = studyRepository.questions
    val announcements: StateFlow<List<Announcement>> = studyRepository.announcements
    val recentlyViewedIds: StateFlow<List<String>> = studyRepository.recentlyViewedIds

    // Study Tools: Pomodoro, Quizzes, Flashcards, Planner, Forum & Favorites
    val focusSessions: StateFlow<List<com.example.model.FocusSession>> = studyRepository.focusSessions
    val quizAttempts: StateFlow<List<com.example.model.QuizAttempt>> = studyRepository.quizAttempts
    val flashcards: StateFlow<List<com.example.model.Flashcard>> = studyRepository.flashcards
    val masteredFlashcardIds: StateFlow<Set<String>> = studyRepository.masteredFlashcardIds
    val scheduledSessions: StateFlow<List<com.example.model.ScheduledStudySession>> = studyRepository.scheduledSessions
    val forumQuestions: StateFlow<List<com.example.model.ForumQuestion>> = studyRepository.forumQuestions

    val favoriteMaterials: StateFlow<List<StudyMaterial>> = combine(
        allMaterials,
        bookmarks
    ) { mats, bms ->
        mats.filter { bms.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moduleProgress: StateFlow<List<com.example.model.ModuleProgress>> = combine(
        selectedClass,
        focusSessions,
        quizAttempts
    ) { currentClass, sessions, quizzes ->
        com.example.util.ModuleProgressEngine.getModuleProgress(
            academicClass = currentClass,
            focusSessions = sessions,
            quizAttempts = quizzes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation and screen states
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _selectedCategory = MutableStateFlow<MaterialCategory?>(null)
    val selectedCategory: StateFlow<MaterialCategory?> = _selectedCategory.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _practiceHubSelectedTab = MutableStateFlow(0)
    val practiceHubSelectedTab: StateFlow<Int> = _practiceHubSelectedTab.asStateFlow()

    fun setPracticeHubTab(tabIndex: Int) {
        _practiceHubSelectedTab.value = tabIndex
    }

    fun navigateToPracticeHub(tabIndex: Int = 0) {
        _practiceHubSelectedTab.value = tabIndex
        navigateTo(AppDestination.PRACTICE)
    }

    // Sub-screens & modals
    private val _activeReadingMaterial = MutableStateFlow<StudyMaterial?>(null)
    val activeReadingMaterial: StateFlow<StudyMaterial?> = _activeReadingMaterial.asStateFlow()

    private val _showImportantQuestions = MutableStateFlow(false)
    val showImportantQuestions: StateFlow<Boolean> = _showImportantQuestions.asStateFlow()

    private val _showAnnouncements = MutableStateFlow(false)
    val showAnnouncements: StateFlow<Boolean> = _showAnnouncements.asStateFlow()

    private val _showAdminDashboard = MutableStateFlow(false)
    val showAdminDashboard: StateFlow<Boolean> = _showAdminDashboard.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _selectedQuestionType = MutableStateFlow<QuestionType?>(null)
    val selectedQuestionType: StateFlow<QuestionType?> = _selectedQuestionType.asStateFlow()

    init {
        try {
            val connectivityObserver = com.example.util.NetworkConnectivityObserver(application)
            viewModelScope.launch {
                connectivityObserver.isOnline.collect { online ->
                    _isNetworkConnected.value = online
                }
            }
        } catch (e: Exception) {
            _isNetworkConnected.value = true
        }

        studyRepository.setOnlineStatusProvider {
            !isOffline.value
        }

        studyRepository.syncDownloadsWithDisk(application)
        viewModelScope.launch {
            studentProfile.collect { profile ->
                _selectedClass.value = profile.selectedClass
            }
        }
    }

    // Filtered materials: WHEN OFFLINE, DISPLAYS ONLY CACHED / DOWNLOADED MATERIALS
    val filteredMaterials: StateFlow<List<StudyMaterial>> = combine(
        allMaterials,
        selectedClass,
        selectedSubject,
        combine(selectedCategory, selectedYear, searchQuery) { cat, yr, query -> Triple(cat, yr, query) },
        combine(downloadedIds, isOffline) { cachedIds, offline -> Pair(cachedIds, offline) }
    ) { mats, cls, subj, (cat, yr, query), (cachedIds, offline) ->
        mats.filter { mat ->
            val isCached = cachedIds.contains(mat.id) || mat.isDownloaded
            // When offline, display ONLY cached materials
            if (offline && !isCached) {
                return@filter false
            }

            val matchClass = (mat.academicClass == cls)
            val matchSubj = (subj == null || mat.subject == subj)
            val matchCat = (cat == null || mat.category == cat)
            val matchYear = (yr == null || mat.examYear == yr)
            val matchQuery = if (query.isBlank()) true else {
                mat.title.contains(query, ignoreCase = true) ||
                        mat.chapterName.contains(query, ignoreCase = true) ||
                        mat.description.contains(query, ignoreCase = true) ||
                        mat.tags.any { it.contains(query, ignoreCase = true) }
            }
            matchClass && matchSubj && matchCat && matchYear && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered questions
    val filteredQuestions: StateFlow<List<ImportantQuestion>> = combine(
        questions,
        selectedClass,
        selectedSubject,
        selectedQuestionType,
        searchQuery
    ) { allQs, cls, subj, type, query ->
        allQs.filter { q ->
            val matchClass = (q.academicClass == cls)
            val matchSubj = (subj == null || q.subject == subj)
            val matchType = (type == null || q.type == type)
            val matchQuery = if (query.isBlank()) true else {
                q.questionText.contains(query, ignoreCase = true) ||
                        q.chapterName.contains(query, ignoreCase = true)
            }
            matchClass && matchSubj && matchType && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
        _activeReadingMaterial.value = null
        _showImportantQuestions.value = false
        _showAnnouncements.value = false
        _showAdminDashboard.value = false
    }

    fun setCategory(category: MaterialCategory?) {
        _selectedCategory.value = category
    }

    fun setYear(year: Int?) {
        _selectedYear.value = year
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setQuestionType(type: QuestionType?) {
        _selectedQuestionType.value = type
    }

    fun openMaterial(material: StudyMaterial) {
        _activeReadingMaterial.value = material
        studyRepository.recordView(material.id)
    }

    fun closeMaterial() {
        _activeReadingMaterial.value = null
    }

    fun openImportantQuestions(subject: Subject? = null) {
        if (subject != null) _selectedSubject.value = subject
        _showImportantQuestions.value = true
    }

    fun closeImportantQuestions() {
        _showImportantQuestions.value = false
    }

    fun openAnnouncements() {
        _showAnnouncements.value = true
    }

    fun closeAnnouncements() {
        _showAnnouncements.value = false
    }

    fun openAdminDashboard() {
        _showAdminDashboard.value = true
    }

    fun closeAdminDashboard() {
        _showAdminDashboard.value = false
    }

    fun authenticateAdmin(pin: String): Boolean {
        val success = studyRepository.verifyAdminPin(pin)
        _isAdminAuthenticated.value = success
        return success
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun recordQuestionPracticed() {
        studyRepository.recordQuestionPracticed()
    }

    fun updateProfile(name: String, academicClass: AcademicClass, school: String, rollNo: String) {
        studyRepository.updateProfile(name, academicClass, school, rollNo)
    }

    fun adminAddMaterial(material: StudyMaterial) {
        studyRepository.adminAddMaterial(material)
    }

    fun adminDeleteMaterial(materialId: String) {
        studyRepository.adminDeleteMaterial(materialId)
    }

    fun adminToggleVerified(materialId: String) {
        studyRepository.adminToggleVerified(materialId)
    }

    fun adminAddAnnouncement(announcement: Announcement) {
        studyRepository.adminAddAnnouncement(announcement)
    }

    fun recordFocusSession(session: com.example.model.FocusSession) {
        studyRepository.recordFocusSession(session)
    }

    fun recordQuizAttempt(attempt: com.example.model.QuizAttempt) {
        studyRepository.recordQuizAttempt(attempt)
    }

    fun toggleFlashcardMastered(cardId: String) {
        studyRepository.toggleFlashcardMastered(cardId)
    }

    fun processSrsReview(cardId: String, rating: com.example.model.SrsRating) {
        studyRepository.processSrsReview(cardId, rating)
    }

    fun addCustomFlashcard(
        subject: Subject,
        chapterName: String,
        frontTitle: String,
        frontSubtitle: String,
        backContent: String,
        keyPoints: List<String>
    ) {
        val card = com.example.model.Flashcard(
            subject = subject,
            academicClass = selectedClass.value,
            chapterName = chapterName.ifBlank { "Personal Formula Notes" },
            frontTitle = frontTitle.trim(),
            frontSubtitle = frontSubtitle.trim(),
            backContent = backContent.trim(),
            keyPoints = keyPoints.filter { it.isNotBlank() },
            isCustom = true
        )
        studyRepository.addCustomFlashcard(card)
    }

    fun resetFlashcardSrs(cardId: String) {
        studyRepository.resetFlashcardSrs(cardId)
    }

    fun getTodayStudyMinutes(): Int = studyRepository.getTodayStudyMinutes()

    fun getTodayCompletedSessionsCount(): Int = studyRepository.getTodayCompletedSessionsCount()

    fun getLast30DaysProgress(): List<com.example.model.DailyStudyRecord> = studyRepository.getLast30DaysProgress()

    fun getModuleProgressForCurrentClass(): List<com.example.model.ModuleProgress> {
        return com.example.util.ModuleProgressEngine.getModuleProgress(
            academicClass = selectedClass.value,
            focusSessions = focusSessions.value,
            quizAttempts = quizAttempts.value
        )
    }

    fun scheduleSession(session: com.example.model.ScheduledStudySession, context: android.content.Context? = null) {
        val targetContext = context ?: getApplication<Application>()
        studyRepository.scheduleSession(session, targetContext)
    }

    fun toggleSessionCompleted(sessionId: String) {
        studyRepository.toggleSessionCompleted(sessionId)
    }

    fun deleteScheduledSession(sessionId: String, context: android.content.Context? = null) {
        val targetContext = context ?: getApplication<Application>()
        studyRepository.deleteScheduledSession(sessionId, targetContext)
    }

    fun triggerDemoNotification(session: com.example.model.ScheduledStudySession, context: android.content.Context? = null) {
        val targetContext = context ?: getApplication<Application>()
        studyRepository.triggerDemoNotification(session, targetContext)
    }

    fun postForumQuestion(
        title: String,
        content: String,
        subject: Subject,
        academicClass: AcademicClass,
        chapterName: String,
        tags: List<String>
    ) {
        val profile = studentProfile.value
        val question = com.example.model.ForumQuestion(
            title = title.trim(),
            content = content.trim(),
            subject = subject,
            academicClass = academicClass,
            chapterName = chapterName.ifBlank { "General Syllabus Discussion" },
            authorName = profile.name.ifBlank { "PCM Student" },
            authorSchool = profile.schoolName.ifBlank { "BOSEM Secondary School" },
            authorRollNo = profile.bosemRollNumber.ifBlank { "BOSEM-STUDENT" },
            tags = tags.filter { it.isNotBlank() }
        )
        studyRepository.postForumQuestion(question)
    }

    fun addForumReply(questionId: String, content: String) {
        val profile = studentProfile.value
        val reply = com.example.model.ForumReply(
            questionId = questionId,
            authorName = profile.name.ifBlank { "Peer Student" },
            authorSchool = profile.schoolName.ifBlank { "BOSEM Secondary School" },
            authorBadge = "Student Peer",
            content = content.trim()
        )
        studyRepository.addForumReply(questionId, reply)
    }

    fun toggleQuestionUpvote(questionId: String) {
        studyRepository.toggleQuestionUpvote(questionId)
    }

    fun toggleReplyUpvote(questionId: String, replyId: String) {
        studyRepository.toggleReplyUpvote(questionId, replyId)
    }

    fun toggleMarkSolutionAccepted(questionId: String, replyId: String) {
        studyRepository.toggleMarkSolutionAccepted(questionId, replyId)
    }

    fun deleteForumQuestion(questionId: String) {
        studyRepository.deleteForumQuestion(questionId)
    }

    // Study Dossier PDF Export State
    private val _isExportingDossier = MutableStateFlow(false)
    val isExportingDossier: StateFlow<Boolean> = _isExportingDossier.asStateFlow()

    private val _exportedDossierFile = MutableStateFlow<java.io.File?>(null)
    val exportedDossierFile: StateFlow<java.io.File?> = _exportedDossierFile.asStateFlow()

    fun exportStudyDossier(context: android.content.Context? = null, onComplete: ((java.io.File) -> Unit)? = null) {
        val targetContext = context ?: getApplication<Application>()
        viewModelScope.launch {
            _isExportingDossier.value = true
            try {
                val file = com.example.util.StudyDossierExporter.generateDossierPdf(
                    context = targetContext,
                    profile = studentProfile.value,
                    dailyRecords = getLast30DaysProgress(),
                    focusSessions = focusSessions.value,
                    quizAttempts = quizAttempts.value,
                    bookmarkedMaterials = favoriteMaterials.value,
                    importantQuestions = questions.value
                )
                _exportedDossierFile.value = file
                onComplete?.invoke(file)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error exporting study dossier: ${e.message}", e)
            } finally {
                _isExportingDossier.value = false
            }
        }
    }

    fun openExportedDossierInReader(file: java.io.File) {
        val material = com.example.util.StudyDossierExporter.createMaterialFromDossierFile(file, studentProfile.value)
        openMaterial(material)
    }

    fun downloadAllClassMaterials(targetClass: AcademicClass? = null) {
        val cls = targetClass ?: selectedClass.value
        val unDownloaded = allMaterials.value.filter {
            it.academicClass == cls && !downloadedIds.value.contains(it.id)
        }
        viewModelScope.launch {
            unDownloaded.forEach { mat ->
                studyRepository.downloadMaterial(getApplication(), mat, viewModelScope)
            }
        }
    }

    fun clearExportedDossierState() {
        _exportedDossierFile.value = null
    }
}
