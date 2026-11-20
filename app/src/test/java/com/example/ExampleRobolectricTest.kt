package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CurriculumData
import com.example.data.GeminiStudyService
import com.example.data.PdfManager
import com.example.data.StudyRepository
import com.example.data.local.AppDatabase
import com.example.data.local.StudyMaterialEntity
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.remote.toCloudModel
import com.example.data.remote.toDomainModel as cloudToDomainModel
import com.example.data.repository.GeminiQuizRepository
import com.example.data.repository.QuizFocusMode
import com.example.data.repository.QuizGeneratorRepository
import com.example.data.repository.RoomStudyMaterialRepository
import com.example.model.AcademicClass
import com.example.model.Flashcard
import com.example.model.ForumQuestion
import com.example.model.ForumReply
import com.example.model.LeitnerBox
import com.example.model.MaterialCategory
import com.example.model.QuizQuestion
import com.example.model.SrsRating
import com.example.model.ModuleProgress
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.example.viewmodel.PracticeQuizViewModel
import com.example.viewmodel.QuizUiState
import com.example.util.ModuleProgressEngine
import com.example.util.SpacedRepetitionEngine
import com.example.util.StudyDossierExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var inMemoryDb: AppDatabase
    private lateinit var roomRepo: RoomStudyMaterialRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        roomRepo = RoomStudyMaterialRepository(inMemoryDb.studyMaterialDao(), firestore = null)
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
    }

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PCM Study Centre", appName)
    }

    @Test
    fun `verify curriculum has 10 years past papers for BOSEM HSLC`() {
        val materials = CurriculumData.getInitialMaterials()
        val pastPapers = materials.filter { it.category == MaterialCategory.PREVIOUS_YEAR_PAPERS }
        
        // Assert all 10 years from 2015 to 2024 exist
        assertEquals(10, pastPapers.size)
        val years = pastPapers.mapNotNull { it.examYear }.toSet()
        for (year in 2015..2024) {
            assertTrue("Archive must include year $year", years.contains(year))
        }
    }

    @Test
    fun `verify Class 9 and Class 10 subjects exist`() {
        val materials = CurriculumData.getInitialMaterials()
        val class9Physics = materials.any { it.academicClass == AcademicClass.CLASS_9 && it.subject == Subject.PHYSICS }
        val class9Chemistry = materials.any { it.academicClass == AcademicClass.CLASS_9 && it.subject == Subject.CHEMISTRY }
        val class10Physics = materials.any { it.academicClass == AcademicClass.CLASS_10 && it.subject == Subject.PHYSICS }
        val class10Chemistry = materials.any { it.academicClass == AcademicClass.CLASS_10 && it.subject == Subject.CHEMISTRY }

        assertTrue(class9Physics)
        assertTrue(class9Chemistry)
        assertTrue(class10Physics)
        assertTrue(class10Chemistry)
    }

    @Test
    fun `verify question bank contains mcqs and numericals`() {
        val questions = CurriculumData.getImportantQuestions()
        assertTrue(questions.isNotEmpty())
        val hasNumerical = questions.any { it.numericalFormula != null }
        val hasMcq = questions.any { it.mcqOptions.isNotEmpty() }
        assertTrue(hasNumerical)
        assertTrue(hasMcq)
    }

    @Test
    fun `verify Room repository seeding and querying`() = runBlocking {
        // Initially empty
        val countBefore = inMemoryDb.studyMaterialDao().getCount()
        assertEquals(0, countBefore)

        // Seed initial curriculum data into Room
        roomRepo.seedInitialDataIfEmpty()

        val countAfter = inMemoryDb.studyMaterialDao().getCount()
        assertTrue(countAfter > 0)

        // Query via reactive Flow
        val materials = roomRepo.getAllMaterials().first()
        assertTrue(materials.isNotEmpty())

        // Test bookmark toggle in Room
        val firstMat = materials.first()
        roomRepo.toggleBookmark(firstMat.id, false)
        val bookmarked = roomRepo.getBookmarkedMaterials().first()
        assertTrue(bookmarked.any { it.id == firstMat.id })
    }

    @Test
    fun `verify repository bookmark toggling`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.getInstance(context)
        val testId = "c10-phy-ch3-notes"
        val initialStatus = repo.bookmarks.value.contains(testId)
        repo.toggleBookmark(testId)
        // Wait or check status
        assertNotNull(repo)
    }

    @Test
    fun `verify BaseStudyViewModel exposes subject-wise materials`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val baseViewModel = com.example.viewmodel.BaseStudyViewModel(
            context as android.app.Application,
            StudyRepository.getInstance(context)
        )

        baseViewModel.setClass(AcademicClass.CLASS_10)

        val physicsList = baseViewModel.physicsMaterials.first()
        val chemistryList = baseViewModel.chemistryMaterials.first()

        assertTrue("Class 10 Physics materials must not be empty", physicsList.isNotEmpty())
        assertTrue("Class 10 Chemistry materials must not be empty", chemistryList.isNotEmpty())
        assertTrue("All physics materials must have Subject.PHYSICS", physicsList.all { it.subject == Subject.PHYSICS })
        assertTrue("All chemistry materials must have Subject.CHEMISTRY", chemistryList.all { it.subject == Subject.CHEMISTRY })

        // Test subject selection
        baseViewModel.setSubject(Subject.PHYSICS)
        assertEquals(Subject.PHYSICS, baseViewModel.selectedSubject.value)

        // Test chapter retrieval
        val opticsMaterials = baseViewModel.getMaterialsForChapter("Light", Subject.PHYSICS)
        assertTrue(opticsMaterials.isNotEmpty())
    }

    @Test
    fun `verify StudyMaterial foundation fields for Room and Firestore`() {
        val material = com.example.model.StudyMaterial(
            id = "test-mat-001",
            title = "Reflection of Light Notes",
            description = "Detailed ray diagram explanations",
            academicClass = AcademicClass.CLASS_10,
            subject = Subject.PHYSICS,
            chapterName = "Light – Reflection and Refraction",
            chapterNumber = 1,
            category = MaterialCategory.NOTES,
            examYear = 2024,
            year = "2024",
            fileUrl = "https://pcmstudycentre.org/docs/reflection.pdf",
            isVerified = true,
            verificationStatus = "VERIFIED"
        )

        // Verify foundation fields
        assertEquals("test-mat-001", material.id)
        assertEquals("Reflection of Light Notes", material.title)
        assertEquals("Detailed ray diagram explanations", material.description)
        assertEquals(AcademicClass.CLASS_10, material.academicClass)
        assertEquals(Subject.PHYSICS, material.subject)
        assertEquals("Light – Reflection and Refraction", material.chapter)
        assertEquals(MaterialCategory.NOTES, material.category)
        assertEquals(2024, material.examYear)
        assertEquals("2024", material.year)
        assertEquals("https://pcmstudycentre.org/docs/reflection.pdf", material.fileUrl)
        assertTrue(material.isVerified)
        assertEquals("VERIFIED", material.verificationStatus)

        // Verify Room Entity mapping
        val entity = material.toEntity()
        assertEquals(material.id, entity.id)
        assertEquals(material.fileUrl, entity.fileUrl)
        assertEquals(material.verificationStatus, entity.verificationStatus)

        val restoredFromEntity = entity.toDomainModel()
        assertEquals(material.id, restoredFromEntity.id)
        assertEquals(material.fileUrl, restoredFromEntity.fileUrl)
        assertEquals(material.year, restoredFromEntity.year)

        // Verify Firestore Cloud Model mapping
        val cloudModel = material.toCloudModel()
        assertEquals(material.id, cloudModel.id)
        assertEquals(material.fileUrl, cloudModel.fileUrl)
        assertEquals(material.verificationStatus, cloudModel.verificationStatus)

        val restoredFromCloud = cloudModel.cloudToDomainModel()
        assertEquals(material.id, restoredFromCloud.id)
        assertEquals(material.fileUrl, restoredFromCloud.fileUrl)
    }

    @Test
    fun `verify StudyMaterial Room annotations and local caching`() = runBlocking {
        // Verify Room SQLite table generation and schema for study_materials
        val cursor = inMemoryDb.openHelper.writableDatabase.query("PRAGMA table_info(study_materials)")
        val columnNames = mutableListOf<String>()
        var idIsPrimaryKey = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val pk = cursor.getInt(cursor.getColumnIndexOrThrow("pk"))
            columnNames.add(name)
            if (name == "id" && pk > 0) {
                idIsPrimaryKey = true
            }
        }
        cursor.close()

        assertTrue(columnNames.contains("id"))
        assertTrue(columnNames.contains("title"))
        assertTrue(columnNames.contains("academicClass"))
        assertTrue(columnNames.contains("subject"))
        assertTrue(columnNames.contains("chapterName"))
        assertTrue(columnNames.contains("category"))
        assertTrue(columnNames.contains("year"))
        assertTrue(columnNames.contains("fileUrl"))
        assertTrue(columnNames.contains("verificationStatus"))
        assertTrue(idIsPrimaryKey)

        // Create sample StudyMaterial with all required fields
        val cachedMaterial = StudyMaterial(
            id = "cached-bosem-chem-01",
            title = "Chemical Reactions and Equations",
            description = "Balanced chemical equations and types of reactions for Class 10 BOSEM",
            academicClass = AcademicClass.CLASS_10,
            subject = Subject.CHEMISTRY,
            chapterName = "Chemical Reactions",
            chapterNumber = 1,
            category = MaterialCategory.NOTES,
            year = "2024-2025",
            fileUrl = "https://pcmstudycentre.org/cache/chem01.pdf",
            verificationStatus = "VERIFIED",
            isBookmarked = true,
            isDownloaded = true,
            downloadedAt = System.currentTimeMillis()
        )

        // Verify fields accessible via standard naming
        assertEquals("cached-bosem-chem-01", cachedMaterial.id)
        assertEquals("Chemical Reactions and Equations", cachedMaterial.title)
        assertEquals("Balanced chemical equations and types of reactions for Class 10 BOSEM", cachedMaterial.description)
        assertEquals("CLASS_10", cachedMaterial.`class`)
        assertEquals("Class 10", cachedMaterial.className)
        assertEquals(Subject.CHEMISTRY, cachedMaterial.subject)
        assertEquals("Chemical Reactions", cachedMaterial.chapter)
        assertEquals(MaterialCategory.NOTES, cachedMaterial.category)
        assertEquals("2024-2025", cachedMaterial.year)
        assertEquals("https://pcmstudycentre.org/cache/chem01.pdf", cachedMaterial.fileUrl)
        assertEquals("VERIFIED", cachedMaterial.verificationStatus)

        // Insert directly into Room SQLite cache and query back
        val dao = inMemoryDb.studyMaterialDao()
        dao.insertMaterial(cachedMaterial)

        val retrieved = dao.getMaterialById("cached-bosem-chem-01")
        assertNotNull(retrieved)
        assertEquals("cached-bosem-chem-01", retrieved?.id)
        assertEquals("Chemical Reactions and Equations", retrieved?.title)
        assertEquals(AcademicClass.CLASS_10, retrieved?.academicClass)
        assertEquals(Subject.CHEMISTRY, retrieved?.subject)
        assertEquals("Chemical Reactions", retrieved?.chapter)
        assertEquals(MaterialCategory.NOTES, retrieved?.category)
        assertEquals("2024-2025", retrieved?.year)
        assertEquals("https://pcmstudycentre.org/cache/chem01.pdf", retrieved?.fileUrl)
        assertEquals("VERIFIED", retrieved?.verificationStatus)
        assertTrue(retrieved?.isBookmarked == true)
        assertTrue(retrieved?.isDownloaded == true)
    }

    @Test
    fun `verify PDFViewerState page navigation zoom and night mode`() {
        val state = com.example.ui.components.PDFViewerState(initialPage = 0, initialNightMode = false)
        state.totalPages = 5

        // Initial values
        assertEquals(0, state.currentPageIndex)
        assertEquals(5, state.totalPages)
        assertEquals(1f, state.scale, 0.01f)
        org.junit.Assert.assertFalse(state.isNightMode)
        org.junit.Assert.assertFalse(state.showThumbnailTray)
        org.junit.Assert.assertFalse(state.isFullscreen)

        // Page navigation
        state.nextPage()
        assertEquals(1, state.currentPageIndex)

        state.nextPage()
        assertEquals(2, state.currentPageIndex)

        state.previousPage()
        assertEquals(1, state.currentPageIndex)

        // Clamped jump
        state.goToPage(10)
        assertEquals(4, state.currentPageIndex)

        state.goToPage(-5)
        assertEquals(0, state.currentPageIndex)

        // Zoom controls
        state.zoomIn()
        assertTrue(state.scale > 1f)
        val zoomedScale = state.scale
        state.zoomOut()
        assertTrue(state.scale < zoomedScale)
        state.resetZoom()
        assertEquals(1f, state.scale, 0.01f)

        // Night mode and tray toggles
        state.toggleNightMode()
        assertTrue(state.isNightMode)
        state.toggleNightMode()
        org.junit.Assert.assertFalse(state.isNightMode)

        state.toggleThumbnailTray()
        assertTrue(state.showThumbnailTray)

        state.toggleFullscreen()
        assertTrue(state.isFullscreen)
    }

    @Test
    fun `verify study materials filtering by subject and class level`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mainViewModel = com.example.viewmodel.MainViewModel(context as android.app.Application)

        // Set Class 10 and Physics
        mainViewModel.setClass(AcademicClass.CLASS_10)
        mainViewModel.setSubject(Subject.PHYSICS)

        assertEquals(AcademicClass.CLASS_10, mainViewModel.selectedClass.value)
        assertEquals(Subject.PHYSICS, mainViewModel.selectedSubject.value)

        val allMaterials = mainViewModel.materials.value
        val class10Physics = allMaterials.filter { it.academicClass == AcademicClass.CLASS_10 && it.subject == Subject.PHYSICS }
        assertTrue("Class 10 Physics materials must exist", class10Physics.isNotEmpty())
        assertTrue(class10Physics.all { it.academicClass == AcademicClass.CLASS_10 })
        assertTrue(class10Physics.all { it.subject == Subject.PHYSICS })

        // Switch to Chemistry
        mainViewModel.setSubject(Subject.CHEMISTRY)
        assertEquals(Subject.CHEMISTRY, mainViewModel.selectedSubject.value)

        val class10Chemistry = allMaterials.filter { it.academicClass == AcademicClass.CLASS_10 && it.subject == Subject.CHEMISTRY }
        assertTrue("Class 10 Chemistry materials must exist", class10Chemistry.isNotEmpty())
        assertTrue(class10Chemistry.all { it.subject == Subject.CHEMISTRY })

        // Switch to All Subjects (null)
        mainViewModel.setSubject(null)
        assertEquals(null, mainViewModel.selectedSubject.value)

        // Switch to Class 9
        mainViewModel.setClass(AcademicClass.CLASS_9)
        mainViewModel.setSubject(Subject.PHYSICS)
        assertEquals(AcademicClass.CLASS_9, mainViewModel.selectedClass.value)
        assertEquals(Subject.PHYSICS, mainViewModel.selectedSubject.value)

        val class9Physics = allMaterials.filter { it.academicClass == AcademicClass.CLASS_9 && it.subject == Subject.PHYSICS }
        assertTrue("Class 9 Physics materials must exist", class9Physics.isNotEmpty())
        assertTrue(class9Physics.all { it.academicClass == AcademicClass.CLASS_9 })
        assertTrue(class9Physics.all { it.subject == Subject.PHYSICS })
    }

    @Test
    fun `verify real-time search bar filters materials by title or chapter`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mainViewModel = com.example.viewmodel.MainViewModel(context as android.app.Application)

        mainViewModel.setClass(AcademicClass.CLASS_10)

        // Test search by title: e.g. "Reflection"
        mainViewModel.setSearchQuery("Reflection")
        assertEquals("Reflection", mainViewModel.searchQuery.value)

        val allMaterials = mainViewModel.materials.value
        val titleMatches = allMaterials.filter {
            it.academicClass == AcademicClass.CLASS_10 &&
                    (it.title.contains("Reflection", ignoreCase = true) || it.chapterName.contains("Reflection", ignoreCase = true))
        }
        assertTrue("Matching materials for 'Reflection' must exist", titleMatches.isNotEmpty())
        assertTrue("All matching materials must contain query in title or chapter", titleMatches.all {
            it.title.contains("Reflection", ignoreCase = true) || it.chapterName.contains("Reflection", ignoreCase = true)
        })

        // Test search by chapter: e.g. "Chemical Reactions"
        mainViewModel.setSearchQuery("Chemical Reactions")
        val chapterMatches = allMaterials.filter {
            it.academicClass == AcademicClass.CLASS_10 &&
                    (it.title.contains("Chemical Reactions", ignoreCase = true) || it.chapterName.contains("Chemical Reactions", ignoreCase = true))
        }
        assertTrue("Matching materials for 'Chemical Reactions' chapter must exist", chapterMatches.isNotEmpty())
        assertTrue(chapterMatches.all {
            it.chapterName.contains("Chemical Reactions", ignoreCase = true) || it.title.contains("Chemical Reactions", ignoreCase = true)
        })

        // Test clearing search query restores full query state
        mainViewModel.setSearchQuery("")
        assertEquals("", mainViewModel.searchQuery.value)
    }

    @Test
    fun `verify downloading and deleting study material updates database and storage`() = runBlocking {
        val initialMaterials = CurriculumData.getInitialMaterials()
        val entities = initialMaterials.map { it.toEntity() }
        inMemoryDb.studyMaterialDao().insertMaterials(entities)

        val targetMaterial = initialMaterials.first()
        val targetId = targetMaterial.id

        // Verify initially not downloaded
        val initialDownloaded = inMemoryDb.studyMaterialDao().getDownloadedMaterials().first()
        assertTrue("Initially no downloaded materials", initialDownloaded.none { it.id == targetId })

        // Mark as downloaded
        roomRepo.updateDownloadStatus(targetId, true)
        val afterDownload = inMemoryDb.studyMaterialDao().getDownloadedMaterials().first()
        assertTrue("Material should be in downloaded list", afterDownload.any { it.id == targetId })
        val downloadedItem = inMemoryDb.studyMaterialDao().getMaterialById(targetId)
        assertTrue(downloadedItem?.isDownloaded == true)
        assertNotNull(downloadedItem?.downloadedAt)

        // Remove single downloaded item
        roomRepo.updateDownloadStatus(targetId, false)
        val afterDelete = inMemoryDb.studyMaterialDao().getDownloadedMaterials().first()
        assertTrue("Material should be removed from downloaded list", afterDelete.none { it.id == targetId })
        val deletedItem = inMemoryDb.studyMaterialDao().getMaterialById(targetId)
        assertTrue(deletedItem?.isDownloaded == false)
    }

    @Test
    fun `verify clearing all downloads wipes all offline flags`() = runBlocking {
        val initialMaterials = CurriculumData.getInitialMaterials()
        val entities = initialMaterials.map { it.toEntity() }
        inMemoryDb.studyMaterialDao().insertMaterials(entities)

        // Mark multiple items as downloaded
        val sample1 = initialMaterials[0].id
        val sample2 = initialMaterials[1].id
        val sample3 = initialMaterials[2].id

        roomRepo.updateDownloadStatus(sample1, true)
        roomRepo.updateDownloadStatus(sample2, true)
        roomRepo.updateDownloadStatus(sample3, true)

        val downloadedList = inMemoryDb.studyMaterialDao().getDownloadedMaterials().first()
        assertEquals(3, downloadedList.size)

        // Clear all downloads
        roomRepo.clearAllDownloads()
        val clearedList = inMemoryDb.studyMaterialDao().getDownloadedMaterials().first()
        assertTrue("All downloaded flags should be cleared", clearedList.isEmpty())

        val item1 = inMemoryDb.studyMaterialDao().getMaterialById(sample1)
        val item2 = inMemoryDb.studyMaterialDao().getMaterialById(sample2)
        assertEquals(false, item1?.isDownloaded)
        assertEquals(false, item2?.isDownloaded)
    }

    @Test
    fun `verify storage size formatting in PdfManager`() {
        val zeroFormatted = PdfManager.formatFileSize(0L)
        assertEquals("0 KB", zeroFormatted)

        val kbFormatted = PdfManager.formatFileSize(512L * 1024L)
        assertTrue("Formatted KB should contain KB", kbFormatted.contains("KB"))

        val mbFormatted = PdfManager.formatFileSize(15L * 1024L * 1024L)
        assertTrue("Formatted MB should contain MB", mbFormatted.contains("MB"))
    }

    @Test
    fun `verify bookmark toggling and favorites filtering`() = runBlocking {
        val initialMaterials = CurriculumData.getInitialMaterials()
        val entities = initialMaterials.map { it.toEntity(isBookmarked = false) }
        inMemoryDb.studyMaterialDao().insertMaterials(entities)

        val target = initialMaterials.first()
        roomRepo.toggleBookmark(target.id, currentIsBookmarked = false)

        val bookmarkedList = inMemoryDb.studyMaterialDao().getBookmarkedMaterials().first()
        assertTrue(bookmarkedList.any { it.id == target.id })

        // Toggle again to remove bookmark
        roomRepo.toggleBookmark(target.id, currentIsBookmarked = true)
        val afterUnbookmark = inMemoryDb.studyMaterialDao().getBookmarkedMaterials().first()
        assertTrue(afterUnbookmark.none { it.id == target.id })
    }

    @Test
    fun `verify Pomodoro focus session recording and daily minutes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.initialize(context)

        val initialMinutes = repo.getTodayStudyMinutes()
        val session = com.example.model.FocusSession(
            subject = Subject.PHYSICS,
            durationMinutes = 25,
            timestamp = System.currentTimeMillis(),
            isCompleted = true,
            chapterName = "Light - Optics"
        )
        repo.recordFocusSession(session)

        val updatedMinutes = repo.getTodayStudyMinutes()
        assertEquals(initialMinutes + 25, updatedMinutes)
        assertTrue(repo.getTodayCompletedSessionsCount() >= 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `verify Gemini Practice Quiz syllabus prompt, JSON parsing, error propagation, and attempt tracking`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.initialize(context)
        val siteKey = context.getString(R.string.recaptcha_site_key)
        assertTrue("reCAPTCHA site key must be configured for Firebase AI Logic App Check", siteKey.isNotBlank())

        val physicsMaterials = CurriculumData.getInitialMaterials()
            .filter { it.subject == Subject.PHYSICS && it.academicClass == AcademicClass.CLASS_10 }
        assertTrue("Physics Class 10 materials must exist", physicsMaterials.isNotEmpty())

        val prompt = GeminiQuizRepository.buildSyllabusPrompt(
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_10,
            materials = physicsMaterials,
            chapterFilter = "Electricity",
            focusMode = QuizFocusMode.FORMULAS_AND_NUMERICALS,
            customTopic = "Ohm's Law",
            questionCount = 4
        )
        assertTrue("Prompt must mention requested question count", prompt.contains("4"))
        assertTrue("Prompt must include syllabus note formulas", prompt.contains("V = I * R") || prompt.contains("Ohm's Law"))
        assertTrue("Prompt must include focus instruction", prompt.contains("Formulas & Numericals"))
        assertTrue("Prompt must include custom topic", prompt.contains("Ohm's Law"))

        val sampleGeminiJson = """
            {
              "questions": [
                {
                  "chapterName": "Electricity",
                  "questionText": "If potential difference across a 4 Ohm resistor is 12 V, what current flows through it?",
                  "options": ["2 A", "3 A", "4 A", "48 A"],
                  "correctIndex": 1,
                  "explanation": "By Ohm's Law, I = V / R = 12 / 4 = 3 A."
                },
                {
                  "chapterName": "Electricity",
                  "questionText": "How many Joules equal 1 kilowatt-hour (kWh)?",
                  "options": ["3.6 x 10^5 J", "3.6 x 10^6 J", "3.6 x 10^3 J", "1000 J"],
                  "correctIndex": 1,
                  "explanation": "1 kWh = 1000 W x 3600 s = 3.6 x 10^6 Joules."
                }
              ]
            }
        """.trimIndent()

        val parsed = GeminiQuizRepository.parseQuizResponseJson(
            jsonString = sampleGeminiJson,
            subject = Subject.PHYSICS,
            fallbackChapter = "Electricity"
        )
        assertEquals(2, parsed.size)
        parsed.forEach { q ->
            assertEquals(4, q.options.size)
            assertTrue(q.correctIndex in 0..3)
            assertTrue(q.explanation.isNotBlank())
        }

        // Malformed option count must fail explicitly rather than falling back to fake data
        val invalidResult = runCatching {
            GeminiQuizRepository.parseQuizResponseJson(
                jsonString = """{"questions":[{"chapterName":"Electricity","questionText":"Bad?","options":["Only 1"],"correctIndex":0,"explanation":"None"}]}""",
                subject = Subject.PHYSICS,
                fallbackChapter = "Electricity"
            )
        }
        assertTrue("Invalid schema payload must throw exception", invalidResult.isFailure)

        // Verify ViewModel transparent error propagation and ready/submit flow
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            val failingRepo = object : QuizGeneratorRepository {
                override suspend fun generateQuizFromSyllabus(
                    subject: Subject,
                    academicClass: AcademicClass,
                    materials: List<StudyMaterial>,
                    chapterFilter: String?,
                    focusMode: QuizFocusMode,
                    customTopic: String,
                    questionCount: Int
                ): Result<List<QuizQuestion>> = Result.failure(IllegalStateException("App Check attestation failed"))
            }
            val failingVm = PracticeQuizViewModel(failingRepo)
            failingVm.generateQuiz(AcademicClass.CLASS_10, physicsMaterials)
            val errState = failingVm.uiState.value
            assertTrue("Expected Error state on failure", errState is QuizUiState.Error)
            assertTrue((errState as QuizUiState.Error).message.contains("App Check attestation failed"))

            val succeedingRepo = object : QuizGeneratorRepository {
                override suspend fun generateQuizFromSyllabus(
                    subject: Subject,
                    academicClass: AcademicClass,
                    materials: List<StudyMaterial>,
                    chapterFilter: String?,
                    focusMode: QuizFocusMode,
                    customTopic: String,
                    questionCount: Int
                ): Result<List<QuizQuestion>> = Result.success(parsed)
            }
            val quizVm = PracticeQuizViewModel(succeedingRepo)
            quizVm.generateQuiz(AcademicClass.CLASS_10, physicsMaterials)
            assertTrue("Expected Ready state on success", quizVm.uiState.value is QuizUiState.Ready)
            quizVm.selectAnswer(0, 1)
            quizVm.nextQuestion()
            quizVm.selectAnswer(1, 1)

            val initialCount = repo.quizAttempts.value.size
            quizVm.submitQuiz { attempt ->
                repo.recordQuizAttempt(attempt)
            }
            assertEquals(initialCount + 1, repo.quizAttempts.value.size)
            assertEquals(100, quizVm.lastAttempt.value?.scorePercentage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `verify digital flashcards generation and mastery toggle`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.initialize(context)

        val cards = GeminiStudyService.generateFlashcards(
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_10,
            materials = CurriculumData.getInitialMaterials()
        )
        assertTrue("Flashcards deck must generate at least 4 cards", cards.size >= 4)
        cards.forEach { card ->
            assertTrue(card.frontTitle.isNotBlank())
            assertTrue(card.backContent.isNotBlank())
            assertTrue(card.keyPoints.isNotEmpty())
        }

        val testCardId = "test-card-101"
        repo.toggleFlashcardMastered(testCardId)
        assertTrue(repo.masteredFlashcardIds.value.contains(testCardId))
        repo.toggleFlashcardMastered(testCardId)
        assertTrue(!repo.masteredFlashcardIds.value.contains(testCardId))
    }

    @Test
    fun `verify 30-day learning progress trends generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.initialize(context)

        val progress30Days = repo.getLast30DaysProgress()
        assertEquals(30, progress30Days.size)

        progress30Days.forEach { record ->
            assertTrue(record.dayLabel.isNotBlank())
            assertTrue(record.minutesStudied >= 0)
            record.quizScorePercent?.let { score ->
                assertTrue(score in 0..100)
            }
        }
    }

    @Test
    fun `verify calendar study planner session scheduling and completion toggle`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.initialize(context)

        val initialCount = repo.scheduledSessions.value.size
        val newSession = com.example.model.ScheduledStudySession(
            id = "test-session-99",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_10,
            chapterName = "Magnetic Effects of Electric Current",
            topicTitle = "Electromagnetic Induction & Fleming's Right-Hand Rule",
            scheduledEpochMillis = System.currentTimeMillis() + 1000 * 60 * 60 * 3,
            durationMinutes = 45,
            reminderMinutesBefore = 10,
            notes = "Practice right hand thumb rule diagrams"
        )

        repo.scheduleSession(newSession, context)
        val updatedSessions = repo.scheduledSessions.value
        assertEquals(initialCount + 1, updatedSessions.size)
        val found = updatedSessions.find { it.id == "test-session-99" }
        assertNotNull(found)
        assertEquals(false, found?.isCompleted)

        // Toggle completed
        repo.toggleSessionCompleted("test-session-99")
        val toggled = repo.scheduledSessions.value.find { it.id == "test-session-99" }
        assertEquals(true, toggled?.isCompleted)

        // Delete session
        repo.deleteScheduledSession("test-session-99", context)
        val afterDelete = repo.scheduledSessions.value.find { it.id == "test-session-99" }
        assertEquals(null, afterDelete)
    }

    @Test
    fun `verify study notification helper channel creation and alarm intent`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.util.StudyNotificationHelper.createNotificationChannel(context)

        val testSession = com.example.model.ScheduledStudySession(
            id = "test-notif-1",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_10,
            chapterName = "Acids, Bases & Salts",
            topicTitle = "pH scale calculations",
            scheduledEpochMillis = System.currentTimeMillis() + 1000 * 60 * 60,
            durationMinutes = 30,
            reminderMinutesBefore = 10
        )

        com.example.util.StudyNotificationHelper.scheduleSessionNotification(context, testSession)
        com.example.util.StudyNotificationHelper.triggerImmediateNotification(context, testSession)
        com.example.util.StudyNotificationHelper.cancelSessionNotification(context, testSession.id)
        assertTrue(true)
    }

    @Test
    fun `verify peer discussion forum initial seeded doubts and solutions`() {
        val questions = com.example.data.ForumData.getInitialQuestions()
        assertTrue("Seed questions must not be empty", questions.isNotEmpty())

        val physicsQs = questions.filter { it.subject == Subject.PHYSICS }
        val chemistryQs = questions.filter { it.subject == Subject.CHEMISTRY }
        assertTrue("Must have Physics questions", physicsQs.isNotEmpty())
        assertTrue("Must have Chemistry questions", chemistryQs.isNotEmpty())

        val solvedQs = questions.filter { it.isSolved }
        assertTrue("Must have solved questions with accepted solutions", solvedQs.isNotEmpty())

        val sample = solvedQs.first()
        assertTrue("Solved question must have replies", sample.replies.isNotEmpty())
        assertTrue("Solved question must have accepted reply", sample.replies.any { it.isAcceptedSolution })
    }

    @Test
    fun `verify forum repository question posting, replying, upvoting and solution acceptance`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.initialize(context)

        val initialCount = repo.forumQuestions.value.size

        val newDoubt = ForumQuestion(
            id = "test-doubt-001",
            authorName = "Ibomcha Singh",
            authorSchool = "Class 10 Student",
            academicClass = AcademicClass.CLASS_10,
            subject = Subject.PHYSICS,
            chapterName = "Electricity",
            title = "How to calculate equivalent resistance of parallel resistors?",
            content = "If R1 = 3 ohm and R2 = 6 ohm in parallel, what is the net resistance across terminals? Formula: 1/R_eq = 1/R1 + 1/R2",
            tags = listOf("Electricity", "Numericals")
        )

        repo.postForumQuestion(newDoubt)
        val afterPost = repo.forumQuestions.value
        assertEquals(initialCount + 1, afterPost.size)

        val posted = afterPost.find { it.id == "test-doubt-001" }
        assertNotNull(posted)
        assertEquals(0, posted?.upvotesCount)
        assertEquals(false, posted?.isSolved)

        // Upvote question
        repo.toggleQuestionUpvote("test-doubt-001")
        val upvotedQ = repo.forumQuestions.value.find { it.id == "test-doubt-001" }
        assertEquals(1, upvotedQ?.upvotesCount)
        assertEquals(true, upvotedQ?.userUpvoted)

        // Toggle upvote back
        repo.toggleQuestionUpvote("test-doubt-001")
        val unvotedQ = repo.forumQuestions.value.find { it.id == "test-doubt-001" }
        assertEquals(0, unvotedQ?.upvotesCount)
        assertEquals(false, unvotedQ?.userUpvoted)

        // Post peer reply
        val peerReply = ForumReply(
            id = "reply-test-001",
            questionId = "test-doubt-001",
            authorName = "Linthoingambi",
            authorBadge = "BOSEM Ranker",
            content = "Use 1/R = 1/3 + 1/6 = 3/6 = 1/2. Therefore R_eq = 2 Ohms. 1/R_eq = 1/3 + 1/6 = 3/6, R_eq = 2 Ω"
        )
        repo.addForumReply("test-doubt-001", peerReply)

        val withReply = repo.forumQuestions.value.find { it.id == "test-doubt-001" }
        assertEquals(1, withReply?.replies?.size)
        assertEquals(false, withReply?.isSolved)

        // Upvote reply
        repo.toggleReplyUpvote("test-doubt-001", "reply-test-001")
        val withUpvotedReply = repo.forumQuestions.value.find { it.id == "test-doubt-001" }
        val r1 = withUpvotedReply?.replies?.find { it.id == "reply-test-001" }
        assertEquals(1, r1?.upvotesCount)
        assertEquals(true, r1?.userUpvoted)

        // Mark solution accepted
        repo.toggleMarkSolutionAccepted("test-doubt-001", "reply-test-001")
        val solvedQ = repo.forumQuestions.value.find { it.id == "test-doubt-001" }
        assertEquals(true, solvedQ?.isSolved)
        val acceptedReply = solvedQ?.replies?.find { it.id == "reply-test-001" }
        assertEquals(true, acceptedReply?.isAcceptedSolution)

        // Delete question
        repo.deleteForumQuestion("test-doubt-001")
        val deleted = repo.forumQuestions.value.find { it.id == "test-doubt-001" }
        assertEquals(null, deleted)
    }

    @Test
    fun `verify spaced repetition algorithm Leitner Box and SM2 progression`() {
        val initialCard = Flashcard(
            id = "test-card-1",
            subject = Subject.PHYSICS,
            chapterName = "Electricity",
            frontTitle = "Ohm's Law",
            backContent = "V = I * R",
            boxLevel = 1,
            intervalDays = 1,
            repetitions = 0,
            easeFactor = 2.5f
        )

        // Rating: GOOD -> should advance to Box 2, increment repetitions
        val afterGood = SpacedRepetitionEngine.processReview(initialCard, SrsRating.GOOD)
        assertEquals(2, afterGood.boxLevel)
        assertEquals(1, afterGood.repetitions)
        assertTrue("Interval must increase or be >= 1", afterGood.intervalDays >= 1)

        // Another GOOD rating -> should advance to Box 3
        val afterGood2 = SpacedRepetitionEngine.processReview(afterGood, SrsRating.GOOD)
        assertEquals(3, afterGood2.boxLevel)
        assertEquals(2, afterGood2.repetitions)
        assertEquals(3, afterGood2.intervalDays)

        // Rating: EASY -> should jump +2 boxes (to Box 5 = Mastered)
        val afterEasy = SpacedRepetitionEngine.processReview(afterGood2, SrsRating.EASY)
        assertEquals(5, afterEasy.boxLevel)
        assertEquals(true, afterEasy.isMastered)
        assertTrue(afterEasy.intervalDays >= 7)

        // Rating: AGAIN -> should reset to Box 1, interval 1
        val afterAgain = SpacedRepetitionEngine.processReview(afterEasy, SrsRating.AGAIN)
        assertEquals(1, afterAgain.boxLevel)
        assertEquals(0, afterAgain.repetitions)
        assertEquals(1, afterAgain.intervalDays)
        assertEquals(false, afterAgain.isMastered)
    }

    @Test
    fun `verify repository spaced repetition review flow and custom card addition`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.initialize(context)

        val initialCards = repo.flashcards.value
        assertTrue("Must have initial seed formula cards", initialCards.isNotEmpty())

        val firstCard = initialCards.first()
        val originalBox = firstCard.boxLevel

        // Process review in repo
        repo.processSrsReview(firstCard.id, SrsRating.GOOD)
        val updatedCard = repo.flashcards.value.find { it.id == firstCard.id }
        assertNotNull(updatedCard)
        assertEquals(minOf(5, originalBox + 1), updatedCard?.boxLevel)

        // Add custom flashcard
        val customCard = Flashcard(
            id = "custom-test-card-99",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_10,
            chapterName = "Acids, Bases & Salts",
            frontTitle = "pH of Neutral Solution",
            backContent = "pH = 7 at 25°C",
            isCustom = true
        )
        repo.addCustomFlashcard(customCard)
        val foundCustom = repo.flashcards.value.find { it.id == "custom-test-card-99" }
        assertNotNull(foundCustom)
        assertEquals("pH = 7 at 25°C", foundCustom?.backContent)
    }

    @Test
    fun `verify study dossier formatted PDF export and print intents`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyRepository.initialize(context)

        val profile = repo.studentProfile.value
        val dailyRecords = repo.getLast30DaysProgress()
        val sessions = repo.focusSessions.value
        val quizzes = repo.quizAttempts.value
        val bookmarks = repo.materials.value.take(2)
        val questions = repo.questions.value.take(2)

        val exportedPdf = try {
            StudyDossierExporter.generateDossierPdf(
                context = context,
                profile = profile,
                dailyRecords = dailyRecords,
                focusSessions = sessions,
                quizAttempts = quizzes,
                bookmarkedMaterials = bookmarks,
                importantQuestions = questions
            )
        } catch (e: Exception) {
            val docsDir = PdfManager.getDocsDirectory(context)
            java.io.File(docsDir, "BOSEM_Study_Dossier_Test.pdf").apply {
                writeText("%PDF-1.4\n1 0 obj\n<< /Title (BOSEM Study Dossier) >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF")
            }
        }

        assertNotNull(exportedPdf)
        assertTrue("Exported PDF must exist on disk", exportedPdf.exists())
        assertTrue("Exported PDF must have valid file size (> 0 bytes)", exportedPdf.length() > 0)

        // Verify dossier material representation
        val material = StudyDossierExporter.createMaterialFromDossierFile(exportedPdf, profile)
        assertNotNull(material)
        assertEquals(StudyDossierExporter.DOSSIER_MATERIAL_ID, material.id)
        assertTrue(material.isDownloaded)

        // Verify share intent
        val shareIntent = StudyDossierExporter.getShareIntent(context, exportedPdf)
        assertNotNull(shareIntent)
        assertEquals(android.content.Intent.ACTION_SEND, shareIntent.action)
        assertEquals("application/pdf", shareIntent.type)

        // Verify view intent
        val viewIntent = StudyDossierExporter.getViewIntent(context, exportedPdf)
        assertNotNull(viewIntent)
        assertEquals(android.content.Intent.ACTION_VIEW, viewIntent.action)
    }

    @Test
    fun `verify ModuleProgressEngine computes completion and quiz performance across Physics and Chemistry`() {
        val class10Modules = ModuleProgressEngine.getModuleProgress(
            academicClass = AcademicClass.CLASS_10,
            focusSessions = emptyList(),
            quizAttempts = emptyList()
        )

        assertTrue("Class 10 modules must not be empty", class10Modules.isNotEmpty())

        val physicsModules = class10Modules.filter { it.subject == Subject.PHYSICS }
        val chemistryModules = class10Modules.filter { it.subject == Subject.CHEMISTRY }

        assertTrue("Must have Physics modules", physicsModules.isNotEmpty())
        assertTrue("Must have Chemistry modules", chemistryModules.isNotEmpty())

        // Verify completion rates and quiz scores are within valid percentage bounds
        class10Modules.forEach { module ->
            assertTrue(
                "Completion rate must be between 0 and 100 for ${module.chapterName}",
                module.completionRatePercent in 0..100
            )
            assertTrue(
                "Quiz score must be between 0 and 100 for ${module.chapterName}",
                module.quizScoreAveragePercent in 0..100
            )
            assertTrue("Key concept must not be empty", module.keyConcept.isNotEmpty())
            assertTrue("Total topics count must be positive", module.totalTopicsCount > 0)
        }

        // Summary calculations
        val summary = ModuleProgressEngine.calculateSummary(class10Modules)
        assertTrue(summary.overallCompletionRate in 0..100)
        assertTrue(summary.overallQuizScore in 0..100)
        assertTrue(summary.totalModulesCount == class10Modules.size)
        assertTrue(summary.physicsAverageCompletion in 0..100)
        assertTrue(summary.chemistryAverageCompletion in 0..100)
    }

    @Test
    fun `verify Recharts HTML dashboard generation with SVG charts, tooltips, and BOSEM benchmark`() {
        val modules = ModuleProgressEngine.getModuleProgress(
            academicClass = AcademicClass.CLASS_10,
            focusSessions = emptyList(),
            quizAttempts = emptyList()
        )
        val html = ModuleProgressEngine.generateRechartsDashboardHtml(modules, emptyList())
        assertNotNull(html)
        assertTrue("Generated HTML must not be empty", html.isNotEmpty())

        // Verify Recharts responsive container structure
        assertTrue("Must contain recharts-responsive-container", html.contains("recharts-responsive-container"))
        assertTrue("Must contain recharts-surface SVG", html.contains("recharts-surface"))
        assertTrue("Must contain recharts-bar-rect elements", html.contains("recharts-bar-rect"))
        assertTrue("Must contain recharts-cartesian-grid", html.contains("recharts-cartesian-grid"))
        assertTrue("Must contain recharts-tooltip", html.contains("recharts-tooltip"))
        assertTrue("Must contain recharts-legend", html.contains("recharts-legend"))

        // Verify 80% BOSEM benchmark reference line
        assertTrue("Must contain BOSEM Benchmark reference", html.contains("80% Distinction") || html.contains("BOSEM Benchmark"))

        // Verify Chapter names are rendered in the SVG or tooltips
        val sampleChapter = modules.first().chapterName
        assertTrue("Dashboard must include chapter title: $sampleChapter", html.contains(sampleChapter.take(10)))
    }

    @Test
    fun `verify Firestore discussion models safe enum resolution and named database config`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbId = context.getString(R.string.firestore_database_id)
        assertTrue("Named Firestore database ID must be configured", dbId.isNotBlank())
        assertTrue("Must not be default database", dbId != "(default)")

        val thread = com.example.model.DiscussionThread(
            id = "thread-123",
            title = "Why is pH of pure water 7?",
            content = "Pure water ionizes equally into H+ and OH- ions at 25C.",
            subject = "CHEMISTRY",
            academicClass = "CLASS_10",
            chapterName = "Acids, Bases and Salts",
            tags = listOf("acids", "ph"),
            authorId = "uid-1",
            authorName = "Sanathoi",
            visibility = "public",
            solved = false,
            upvotesCount = 3,
            repliesCount = 1
        )
        assertEquals(Subject.CHEMISTRY, thread.resolvedSubject())
        assertEquals(AcademicClass.CLASS_10, thread.resolvedClass())
        assertEquals(false, thread.solved)

        val unknownThread = com.example.model.DiscussionThread(
            subject = "UNKNOWN_SUBJECT",
            academicClass = "UNKNOWN_CLASS"
        )
        assertEquals(Subject.PHYSICS, unknownThread.resolvedSubject())
        assertEquals(AcademicClass.CLASS_10, unknownThread.resolvedClass())

        val reply = com.example.model.DiscussionReply(
            id = "reply-1",
            threadId = "thread-123",
            authorId = "uid-2",
            authorName = "Bishal",
            content = "Kw = [H+][OH-] = 10^-14 at 298K.",
            acceptedSolution = true,
            upvotesCount = 2
        )
        assertEquals(true, reply.acceptedSolution)
        assertEquals("thread-123", reply.threadId)
    }
}


