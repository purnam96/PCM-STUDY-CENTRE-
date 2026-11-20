package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StudyRepository
import com.example.model.AcademicClass
import com.example.model.StudyMaterial
import com.example.model.Subject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * UI State representing the state-managed subject-wise study material feed.
 */
sealed interface SubjectMaterialsUiState {
    object Loading : SubjectMaterialsUiState

    data class Success(
        val activeClass: AcademicClass,
        val selectedSubject: Subject?,
        val displayedMaterials: List<StudyMaterial>,
        val physicsMaterials: List<StudyMaterial>,
        val chemistryMaterials: List<StudyMaterial>,
        val totalCount: Int
    ) : SubjectMaterialsUiState

    data class Empty(
        val activeClass: AcademicClass,
        val subject: Subject?,
        val message: String = "No study materials available for the selected subject."
    ) : SubjectMaterialsUiState

    data class Error(val message: String) : SubjectMaterialsUiState
}

/**
 * Base ViewModel consuming the StudyRepository to expose reactive, state-managed
 * study materials organized by subject (Physics and Chemistry) and class.
 */
open class BaseStudyViewModel(
    application: Application,
    protected val studyRepository: StudyRepository = StudyRepository.getInstance(application)
) : AndroidViewModel(application) {

    // Base state flows from the StudyRepository
    val allMaterials: StateFlow<List<StudyMaterial>> = studyRepository.materials
    val bookmarks: StateFlow<Set<String>> = studyRepository.bookmarks
    val downloadedIds: StateFlow<Set<String>> = studyRepository.downloadedIds
    val downloadProgress: StateFlow<Map<String, Float>> = studyRepository.downloadProgress
    val studentProfile = studyRepository.studentProfile

    // Network & Offline Mode States
    protected val _isNetworkConnected = MutableStateFlow(true)
    val isNetworkConnected: StateFlow<Boolean> = _isNetworkConnected.asStateFlow()

    protected val _isSimulatedOffline = MutableStateFlow(false)
    val isSimulatedOffline: StateFlow<Boolean> = _isSimulatedOffline.asStateFlow()

    val isOffline: StateFlow<Boolean> = combine(
        _isNetworkConnected,
        _isSimulatedOffline
    ) { connected, simulated ->
        !connected || simulated
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Filter controls for subject-wise exploration
    protected val _selectedClass = MutableStateFlow(AcademicClass.CLASS_10)
    val selectedClass: StateFlow<AcademicClass> = _selectedClass.asStateFlow()

    protected val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

    /**
     * Dedicated state-managed stream for Physics materials for the selected class.
     * When offline, displays ONLY cached/downloaded materials.
     */
    val physicsMaterials: StateFlow<List<StudyMaterial>> = combine(
        allMaterials,
        selectedClass,
        downloadedIds,
        isOffline
    ) { materials, currentClass, cachedIds, offline ->
        materials.filter { mat ->
            val match = itMatch(mat, currentClass, Subject.PHYSICS)
            if (!match) return@filter false
            val isCached = cachedIds.contains(mat.id) || mat.isDownloaded
            !offline || isCached
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Dedicated state-managed stream for Chemistry materials for the selected class.
     * When offline, displays ONLY cached/downloaded materials.
     */
    val chemistryMaterials: StateFlow<List<StudyMaterial>> = combine(
        allMaterials,
        selectedClass,
        downloadedIds,
        isOffline
    ) { materials, currentClass, cachedIds, offline ->
        materials.filter { mat ->
            val match = itMatch(mat, currentClass, Subject.CHEMISTRY)
            if (!match) return@filter false
            val isCached = cachedIds.contains(mat.id) || mat.isDownloaded
            !offline || isCached
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun itMatch(mat: StudyMaterial, cls: AcademicClass, subj: Subject): Boolean {
        return mat.academicClass == cls && mat.subject == subj
    }

    fun setSimulatedOffline(offline: Boolean) {
        _isSimulatedOffline.value = offline
    }

    fun toggleOfflineMode() {
        _isSimulatedOffline.value = !_isSimulatedOffline.value
    }

    /**
     * Unified UI State combining class, selected subject filter, and subject-wise material lists.
     */
    val subjectMaterialsUiState: StateFlow<SubjectMaterialsUiState> = combine(
        selectedClass,
        selectedSubject,
        physicsMaterials,
        chemistryMaterials
    ) { currentClass, currentSubject, physicsList, chemistryList ->
        val displayed = when (currentSubject) {
            Subject.PHYSICS -> physicsList
            Subject.CHEMISTRY -> chemistryList
            null -> physicsList + chemistryList
        }

        if (displayed.isEmpty()) {
            SubjectMaterialsUiState.Empty(
                activeClass = currentClass,
                subject = currentSubject
            )
        } else {
            SubjectMaterialsUiState.Success(
                activeClass = currentClass,
                selectedSubject = currentSubject,
                displayedMaterials = displayed,
                physicsMaterials = physicsList,
                chemistryMaterials = chemistryList,
                totalCount = displayed.size
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubjectMaterialsUiState.Loading
    )

    /**
     * Updates the active academic class filter (Class 9 or Class 10).
     */
    open fun setClass(academicClass: AcademicClass) {
        _selectedClass.value = academicClass
        studyRepository.updateSelectedClass(academicClass)
    }

    /**
     * Selects or clears the active subject filter (Physics, Chemistry, or null for both).
     */
    open fun setSubject(subject: Subject?) {
        _selectedSubject.value = subject
    }

    /**
     * Toggles bookmark state for the specified material ID.
     */
    fun toggleBookmark(materialId: String) {
        studyRepository.toggleBookmark(materialId)
    }

    /**
     * Initiates a download for the given study material.
     */
    fun downloadMaterial(material: StudyMaterial) {
        studyRepository.downloadMaterial(getApplication(), material, viewModelScope)
    }

    /**
     * Deletes an offline downloaded material from device storage.
     */
    fun deleteDownloadedMaterial(material: StudyMaterial) {
        studyRepository.deleteDownloadedMaterial(getApplication(), material)
    }

    /**
     * Clears all offline downloaded materials and wipes cached PDF files to free storage space.
     */
    fun clearAllDownloads() {
        studyRepository.clearAllDownloads(getApplication())
    }

    /**
     * Returns materials for a specific chapter within the given subject and class.
     */
    fun getMaterialsForChapter(chapterName: String, subject: Subject): List<StudyMaterial> {
        val currentClass = _selectedClass.value
        return allMaterials.value.filter {
            it.academicClass == currentClass &&
                    it.subject == subject &&
                    it.chapterName.contains(chapterName, ignoreCase = true)
        }
    }
}
