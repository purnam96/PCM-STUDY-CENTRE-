package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AcademicClass
import com.example.ui.components.OfflineIndicatorBanner
import com.example.ui.components.PCMBottomBar
import com.example.ui.components.PCMHeader
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AnnouncementsScreen
import com.example.ui.screens.ClassesScreen
import com.example.ui.screens.DiscussionForumRoute
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImportantQuestionsScreen
import com.example.ui.screens.PapersScreen
import com.example.ui.screens.PdfReaderScreen
import com.example.ui.screens.PomodoroTimerScreen
import com.example.ui.screens.PracticeHubScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StudyPlannerScreen
import com.example.ui.theme.PCMStudyTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.AppDestination
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            PCMStudyTheme {
                val viewModel: MainViewModel = viewModel()
                PCMStudyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PCMStudyApp(viewModel: MainViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val activeReadingMaterial by viewModel.activeReadingMaterial.collectAsState()
    val showImportantQuestions by viewModel.showImportantQuestions.collectAsState()
    val showAnnouncements by viewModel.showAnnouncements.collectAsState()
    val showAdminDashboard by viewModel.showAdminDashboard.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()

    val isSubScreenOpen = activeReadingMaterial != null ||
            showImportantQuestions ||
            showAnnouncements ||
            showAdminDashboard

    Scaffold(
        topBar = {
            if (!isSubScreenOpen) {
                Column {
                    PCMHeader(
                        studentName = studentProfile.name,
                        selectedClass = selectedClass,
                        onClassToggle = {
                            val nextClass = if (selectedClass == AcademicClass.CLASS_10) AcademicClass.CLASS_9 else AcademicClass.CLASS_10
                            viewModel.setClass(nextClass)
                        },
                        onNotificationsClick = {
                            viewModel.openAnnouncements()
                        },
                        isOffline = isOffline,
                        onToggleOffline = {
                            viewModel.toggleOfflineMode()
                        }
                    )
                    OfflineIndicatorBanner(
                        isOffline = isOffline,
                        cachedCount = downloadedIds.size,
                        onViewCachedClick = {
                            viewModel.navigateTo(AppDestination.DOWNLOADS)
                        },
                        onToggleOfflineMode = {
                            viewModel.toggleOfflineMode()
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (!isSubScreenOpen) {
                PCMBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        viewModel.navigateTo(destination)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                activeReadingMaterial != null -> {
                    PdfReaderScreen(
                        material = activeReadingMaterial!!,
                        viewModel = viewModel,
                        onBack = { viewModel.closeMaterial() }
                    )
                }
                showImportantQuestions -> {
                    ImportantQuestionsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeImportantQuestions() }
                    )
                }
                showAnnouncements -> {
                    AnnouncementsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeAnnouncements() }
                    )
                }
                showAdminDashboard -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeAdminDashboard() }
                    )
                }
                else -> {
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "MainNavigationTransition"
                    ) { destination ->
                        when (destination) {
                            AppDestination.HOME -> HomeScreen(viewModel = viewModel)
                            AppDestination.CLASSES -> ClassesScreen(viewModel = viewModel)
                            AppDestination.PLANNER -> StudyPlannerScreen(viewModel = viewModel)
                            AppDestination.FAVORITES -> FavoritesScreen(viewModel = viewModel)
                            AppDestination.TIMER -> PomodoroTimerScreen(viewModel = viewModel)
                            AppDestination.PRACTICE -> PracticeHubScreen(viewModel = viewModel)
                            AppDestination.PAPERS -> PapersScreen(viewModel = viewModel)
                            AppDestination.DOWNLOADS -> DownloadsScreen(viewModel = viewModel)
                            AppDestination.PROFILE -> ProfileScreen(viewModel = viewModel)
                            AppDestination.FORUM -> DiscussionForumRoute(mainViewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
