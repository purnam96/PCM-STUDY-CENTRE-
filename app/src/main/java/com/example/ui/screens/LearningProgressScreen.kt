package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalContext
import com.example.model.ModuleProgress
import com.example.ui.components.ExportDossierDialog
import com.example.ui.components.RechartsDashboardView
import com.example.util.ModuleProgressEngine
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyStudyRecord
import com.example.model.Subject
import com.example.ui.theme.ChemistryOrange
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PhysicsBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent
import com.example.ui.theme.VioletDark
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.AppDestination
import com.example.viewmodel.MainViewModel

/**
 * Native Learning Progress Dashboard displaying trends in quiz performance
 * and study time over the last month with interactive Jetpack Compose Canvas charts.
 */
@Composable
fun LearningProgressScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dailyRecords = remember { viewModel.getLast30DaysProgress() }
    val quizAttempts by viewModel.quizAttempts.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    val context = LocalContext.current
    val isExportingDossier by viewModel.isExportingDossier.collectAsState()
    val exportedDossierFile by viewModel.exportedDossierFile.collectAsState()
    var showDossierDialog by remember { mutableStateOf(false) }

    var selectedMetricTab by remember { mutableIntStateOf(0) } // 0: Study Time, 1: Quiz Performance
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDashboardEngine by remember { mutableIntStateOf(0) } // 0: Recharts Dashboard, 1: Native Canvas, 2: Module Mastery
    var selectedSubjectFilter by remember { mutableStateOf<Subject?>(null) } // null: All, Subject.PHYSICS, Subject.CHEMISTRY

    val moduleProgressList by viewModel.moduleProgress.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    val filteredModules = remember(moduleProgressList, selectedSubjectFilter) {
        if (selectedSubjectFilter != null) {
            moduleProgressList.filter { it.subject == selectedSubjectFilter }
        } else {
            moduleProgressList
        }
    }

    val progressSummary = remember(moduleProgressList) {
        ModuleProgressEngine.calculateSummary(moduleProgressList)
    }

    val totalMinutesMonth = remember(dailyRecords) { dailyRecords.sumOf { it.minutesStudied } }
    val totalHoursMonth = String.format(java.util.Locale.US, "%.1f", totalMinutesMonth / 60f)
    val avgDailyMinutes = remember(dailyRecords) {
        if (dailyRecords.isNotEmpty()) totalMinutesMonth / dailyRecords.size else 0
    }

    val quizScores = remember(dailyRecords) { dailyRecords.mapNotNull { it.quizScorePercent } }
    val avgQuizScore = remember(quizScores) {
        if (quizScores.isNotEmpty()) quizScores.average().toInt() else 85
    }

    val physicsAttempts = remember(quizAttempts) { quizAttempts.filter { it.subject == Subject.PHYSICS } }
    val chemistryAttempts = remember(quizAttempts) { quizAttempts.filter { it.subject == Subject.CHEMISTRY } }
    val physicsAvg = remember(physicsAttempts) {
        if (physicsAttempts.isNotEmpty()) physicsAttempts.map { it.scorePercentage }.average().toInt() else 82
    }
    val chemistryAvg = remember(chemistryAttempts) {
        if (chemistryAttempts.isNotEmpty()) chemistryAttempts.map { it.scorePercentage }.average().toInt() else 92
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
            .testTag("learning_progress_dashboard"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Gradient Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NavyDark, NavyPrimary)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LEARNING ANALYTICS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Monthly Progress Trends",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = "30-Day performance analysis for BOSEM Physics & Chemistry",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate400
                                )
                            )
                        }

                        // Streak Indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldAccent.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("metric_card_streak")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "${studentProfile.streakDays} Days",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Streak",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GoldAccent,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Key Summary Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryPillCard(
                            title = "Study Time",
                            value = "${totalHoursMonth}h",
                            subtext = "$avgDailyMinutes min/day avg",
                            icon = Icons.Default.Schedule,
                            accentColor = CyanAccent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("metric_card_study_hours")
                        )
                        SummaryPillCard(
                            title = "Avg Accuracy",
                            value = "$avgQuizScore%",
                            subtext = "Distinction Zone",
                            icon = Icons.Default.Quiz,
                            accentColor = GoldAccent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("metric_card_quiz_accuracy")
                        )
                        SummaryPillCard(
                            title = "Sessions",
                            value = "${focusSessions.size}",
                            subtext = "Pomodoro Blocks",
                            icon = Icons.Default.CheckCircle,
                            accentColor = TealAccent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("metric_card_sessions")
                        )
                    }
                }
            }
        }

        // Export Study Summary & Bookmarked Notes PDF Dossier
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        showDossierDialog = true
                        viewModel.exportStudyDossier(context)
                    }
                    .testTag("export_progress_summary_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NavyDark,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Export Dossier",
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Study Progress & Notes Dossier",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Formatted 3-page A4 PDF for printing or offline review with formula cheat-sheet",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate500,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyPrimary
                    ) {
                        Text(
                            text = "Export PDF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Dashboard Engine Tab Navigation (Recharts Engine vs Daily Trends vs Module Mastery)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedDashboardEngine,
                    containerColor = PureWhite,
                    contentColor = NavyPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedDashboardEngine]),
                            color = NavyPrimary,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.testTag("dashboard_engine_tab_row")
                ) {
                    Tab(
                        selected = selectedDashboardEngine == 0,
                        onClick = { selectedDashboardEngine = 0 },
                        text = {
                            Text(
                                text = "Recharts Dashboard",
                                fontWeight = if (selectedDashboardEngine == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        selectedContentColor = NavyPrimary,
                        unselectedContentColor = Slate500,
                        modifier = Modifier.testTag("tab_engine_recharts")
                    )
                    Tab(
                        selected = selectedDashboardEngine == 1,
                        onClick = { selectedDashboardEngine = 1 },
                        text = {
                            Text(
                                text = "Daily Trends",
                                fontWeight = if (selectedDashboardEngine == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        selectedContentColor = NavyPrimary,
                        unselectedContentColor = Slate500,
                        modifier = Modifier.testTag("tab_engine_daily")
                    )
                    Tab(
                        selected = selectedDashboardEngine == 2,
                        onClick = { selectedDashboardEngine = 2 },
                        text = {
                            Text(
                                text = "Module Mastery",
                                fontWeight = if (selectedDashboardEngine == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        selectedContentColor = NavyPrimary,
                        unselectedContentColor = Slate500,
                        modifier = Modifier.testTag("tab_engine_modules")
                    )
                }
            }
        }

        if (selectedDashboardEngine == 0) {
            // Recharts Dashboard Mode: Subject Filter Chips
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSubjectFilter == null,
                        onClick = { selectedSubjectFilter = null },
                        label = {
                            Text(
                                text = "All Modules (${moduleProgressList.size})",
                                fontWeight = if (selectedSubjectFilter == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = PureWhite,
                            selectedLeadingIconColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_all_modules")
                    )
                    FilterChip(
                        selected = selectedSubjectFilter == Subject.PHYSICS,
                        onClick = { selectedSubjectFilter = Subject.PHYSICS },
                        label = {
                            Text(
                                text = "Physics (${moduleProgressList.count { it.subject == Subject.PHYSICS }})",
                                fontWeight = if (selectedSubjectFilter == Subject.PHYSICS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhysicsBlue,
                            selectedLabelColor = PureWhite,
                            selectedLeadingIconColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_physics_modules")
                    )
                    FilterChip(
                        selected = selectedSubjectFilter == Subject.CHEMISTRY,
                        onClick = { selectedSubjectFilter = Subject.CHEMISTRY },
                        label = {
                            Text(
                                text = "Chemistry (${moduleProgressList.count { it.subject == Subject.CHEMISTRY }})",
                                fontWeight = if (selectedSubjectFilter == Subject.CHEMISTRY) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChemistryOrange,
                            selectedLabelColor = PureWhite,
                            selectedLeadingIconColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_chemistry_modules")
                    )
                }
            }

            // Embedded Recharts Engine Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                RechartsDashboardView(
                    modules = filteredModules,
                    dailyRecords = dailyRecords,
                    selectedSubject = selectedSubjectFilter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(490.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("recharts_dashboard_container")
                )
            }

            // Engine Information Banner
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NavyDark,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QueryStats,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recharts Visualization Active",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = "Visualizing completion rates & quiz performance with responsive SVG BarChart, LineChart, and 80% BOSEM distinction reference benchmark.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate600,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Module Breakdown Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Module Breakdown & Action Plan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Showing ${filteredModules.size} chapters for ${selectedClass.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${progressSummary.modulesMasteredCount}/${moduleProgressList.size} Mastered",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Module Action Cards
            filteredModules.forEach { module ->
                item(key = "recharts_module_${module.id}") {
                    ModuleProgressItemCard(
                        module = module,
                        onTakeQuiz = { viewModel.navigateToPracticeHub(0) },
                        onReviewFlashcards = { viewModel.navigateToPracticeHub(1) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        } else if (selectedDashboardEngine == 1) {
            // View Mode Filter Tabs (Study Time vs Quiz Performance)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMetricTab == 0,
                        onClick = {
                            selectedMetricTab = 0
                            selectedDayIndex = null
                        },
                        label = {
                            Text(
                                text = "Daily Study Time (Minutes)",
                                fontWeight = if (selectedMetricTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = PureWhite,
                            selectedLeadingIconColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_tab_study_time")
                    )

                    FilterChip(
                        selected = selectedMetricTab == 1,
                        onClick = {
                            selectedMetricTab = 1
                            selectedDayIndex = null
                        },
                        label = {
                            Text(
                                text = "Quiz Accuracy Trend (%)",
                                fontWeight = if (selectedMetricTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = PureWhite,
                            selectedLeadingIconColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_tab_quiz_performance")
                    )
                }
            }

            // Selected Day Details Banner (Interactive Tooltip from Chart Tap)
            item {
                AnimatedVisibility(
                    visible = selectedDayIndex != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    selectedDayIndex?.let { index ->
                        val record = dailyRecords.getOrNull(index)
                        if (record != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PureWhite,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, VioletSecondary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .testTag("selected_day_tooltip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = VioletLight,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = VioletDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = record.dayLabel,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate900
                                                )
                                            )
                                            Text(
                                                text = "Study Log for this day",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${record.minutesStudied} min",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = NavyPrimary
                                                )
                                            )
                                            Text(
                                                text = "Study Duration",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                                            )
                                        }

                                        record.quizScorePercent?.let { score ->
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "$score%",
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (score >= 80) TealAccent else ChemistryOrange
                                                    )
                                                )
                                                Text(
                                                    text = "Quiz Score",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Primary Visual Chart Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (selectedMetricTab == 0) "Daily Focus Time (Last 30 Days)" else "Quiz Performance Trend (Last 30 Days)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Text(
                                    text = "Tap any bar or point to inspect individual day records",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate500,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate100
                            ) {
                                Text(
                                    text = "30 Days",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate700,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedMetricTab == 0) {
                            // Native Compose Canvas Bar Chart for Study Time
                            StudyTimeBarChart(
                                records = dailyRecords,
                                selectedIndex = selectedDayIndex,
                                onSelectDay = { selectedDayIndex = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .testTag("study_time_chart")
                            )
                        } else {
                            // Native Compose Canvas Line Chart for Quiz Accuracy Trend
                            QuizPerformanceLineChart(
                                records = dailyRecords,
                                selectedIndex = selectedDayIndex,
                                onSelectDay = { selectedDayIndex = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .testTag("quiz_performance_chart")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Legend & Benchmark Guide
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedMetricTab == 0) PhysicsBlue else TealAccent)
                                )
                                Text(
                                    text = if (selectedMetricTab == 0) "Study Sessions" else "Quiz Score %",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate600,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp, 2.dp)
                                        .background(GoldAccent)
                                )
                                Text(
                                    text = if (selectedMetricTab == 0) "Daily Target (45m)" else "BOSEM Benchmark (80%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate600,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Module Mastery Dedicated View
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSubjectFilter == null,
                        onClick = { selectedSubjectFilter = null },
                        label = {
                            Text(
                                text = "All Modules (${moduleProgressList.size})",
                                fontWeight = if (selectedSubjectFilter == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_all_modules_tab2")
                    )
                    FilterChip(
                        selected = selectedSubjectFilter == Subject.PHYSICS,
                        onClick = { selectedSubjectFilter = Subject.PHYSICS },
                        label = {
                            Text(
                                text = "Physics (${moduleProgressList.count { it.subject == Subject.PHYSICS }})",
                                fontWeight = if (selectedSubjectFilter == Subject.PHYSICS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PhysicsBlue,
                            selectedLabelColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_physics_modules_tab2")
                    )
                    FilterChip(
                        selected = selectedSubjectFilter == Subject.CHEMISTRY,
                        onClick = { selectedSubjectFilter = Subject.CHEMISTRY },
                        label = {
                            Text(
                                text = "Chemistry (${moduleProgressList.count { it.subject == Subject.CHEMISTRY }})",
                                fontWeight = if (selectedSubjectFilter == Subject.CHEMISTRY) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChemistryOrange,
                            selectedLabelColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_chemistry_modules_tab2")
                    )
                }
            }

            filteredModules.forEach { module ->
                item(key = "mastery_module_${module.id}") {
                    ModuleProgressItemCard(
                        module = module,
                        onTakeQuiz = { viewModel.navigateToPracticeHub(0) },
                        onReviewFlashcards = { viewModel.navigateToPracticeHub(1) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Subject Breakdown & Accuracy Cards (Physics vs Chemistry)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Subject-Wise Performance",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Calculated across practice tests and solved questions",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Physics Card
                    SubjectProgressCard(
                        subjectName = "Physics",
                        scorePercent = physicsAvg,
                        sessionsCount = focusSessions.count { it.subject == Subject.PHYSICS },
                        color = PhysicsBlue,
                        badge = "8 Chapters Covered",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_mastery_physics")
                    )

                    // Chemistry Card
                    SubjectProgressCard(
                        subjectName = "Chemistry",
                        scorePercent = chemistryAvg,
                        sessionsCount = focusSessions.count { it.subject == Subject.CHEMISTRY },
                        color = ChemistryOrange,
                        badge = "9 Chapters Covered",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_mastery_chemistry")
                    )
                }
            }
        }

        // AI Performance Insight & Exam Readiness Advice
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VioletLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("ai_study_recommendation_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = VioletSecondary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Recommendation",
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Study Recommendation",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "Based on your recent 30-day performance",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VioletDark,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "You maintain a high 92% accuracy in Chemistry reactions and organic compounds. For Physics, practice numerical derivations in 'Electricity & Ohm's Law' to elevate your composite BOSEM score above 90%.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate800,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.navigateTo(AppDestination.PRACTICE)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_start_quiz_recommendation")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take AI Quiz", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.navigateTo(AppDestination.TIMER)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_start_pomodoro_recommendation")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Focus Timer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDossierDialog) {
        ExportDossierDialog(
            isExporting = isExportingDossier,
            exportedFile = exportedDossierFile,
            onDismiss = {
                showDossierDialog = false
                viewModel.clearExportedDossierState()
            },
            onPreviewInApp = { file ->
                viewModel.openExportedDossierInReader(file)
            }
        )
    }
}

/**
 * Metric summary pill card for learning statistics.
 */
@Composable
private fun SummaryPillCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PureWhite.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate300,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accentColor,
                    fontSize = 9.sp
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Native Canvas Bar Chart showing daily study minutes for the last 30 days.
 */
@Composable
private fun StudyTimeBarChart(
    records: List<DailyStudyRecord>,
    selectedIndex: Int?,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxMinutes = remember(records) {
        (records.maxOfOrNull { it.minutesStudied } ?: 60).coerceAtLeast(60)
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records) {
                    detectTapGestures { offset ->
                        val count = records.size
                        if (count > 0) {
                            val barSlotWidth = size.width / count
                            val clickedIndex = (offset.x / barSlotWidth).toInt().coerceIn(0, count - 1)
                            onSelectDay(clickedIndex)
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomPadding = 24.dp.toPx()
            val chartHeight = canvasHeight - bottomPadding
            val barCount = records.size

            if (barCount == 0) return@Canvas

            val slotWidth = canvasWidth / barCount
            val barWidth = (slotWidth * 0.65f).coerceAtLeast(4.dp.toPx())

            // 1. Draw horizontal guide lines & Y-axis labels (30m, 60m target)
            val guideMinutes = listOf(30, 45, 60)
            guideMinutes.forEach { mins ->
                val ratio = (mins.toFloat() / maxMinutes).coerceIn(0f, 1f)
                val y = chartHeight - (ratio * chartHeight)
                val isTarget = (mins == 45)

                drawLine(
                    color = if (isTarget) GoldAccent.copy(alpha = 0.8f) else Slate200,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = if (isTarget) 1.5.dp.toPx() else 1.dp.toPx(),
                    pathEffect = if (isTarget) PathEffect.dashPathEffect(floatArrayOf(10f, 10f)) else null
                )
            }

            // 2. Draw each daily study bar
            records.forEachIndexed { i, record ->
                val x = (i * slotWidth) + ((slotWidth - barWidth) / 2f)
                val heightRatio = (record.minutesStudied.toFloat() / maxMinutes).coerceIn(0f, 1f)
                val barHeight = (heightRatio * chartHeight).coerceAtLeast(3.dp.toPx())
                val y = chartHeight - barHeight

                val isSelected = (selectedIndex == i)
                val barBrush = when {
                    isSelected -> Brush.verticalGradient(
                        colors = listOf(GoldAccent, Color(0xFFD97706)),
                        startY = y,
                        endY = chartHeight
                    )
                    record.minutesStudied >= 45 -> Brush.verticalGradient(
                        colors = listOf(CyanAccent, PhysicsBlue),
                        startY = y,
                        endY = chartHeight
                    )
                    record.minutesStudied > 0 -> Brush.verticalGradient(
                        colors = listOf(PhysicsBlue.copy(alpha = 0.7f), NavyLight),
                        startY = y,
                        endY = chartHeight
                    )
                    else -> Brush.verticalGradient(
                        colors = listOf(Slate300, Slate200),
                        startY = y,
                        endY = chartHeight
                    )
                }

                // Draw rounded bar
                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Draw indicator highlight ring for selected bar
                if (isSelected) {
                    drawRoundRect(
                        color = NavyDark,
                        topLeft = Offset(x - 1.5.dp.toPx(), y - 2.dp.toPx()),
                        size = Size(barWidth + 3.dp.toPx(), barHeight + 2.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx()),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )
                }
            }

            // 3. Draw baseline
            drawLine(
                color = Slate300,
                start = Offset(0f, chartHeight),
                end = Offset(canvasWidth, chartHeight),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // X-Axis Date Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val first = records.firstOrNull()?.dayLabel ?: "Day 1"
            val mid = records.getOrNull(records.size / 2)?.dayLabel ?: "Day 15"
            val last = records.lastOrNull()?.dayLabel ?: "Today"

            Text(text = first, style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp))
            Text(text = mid, style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp))
            Text(text = last, style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }
    }
}

/**
 * Native Canvas Line Chart showing quiz performance (%) across the month.
 */
@Composable
private fun QuizPerformanceLineChart(
    records: List<DailyStudyRecord>,
    selectedIndex: Int?,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records) {
                    detectTapGestures { offset ->
                        val count = records.size
                        if (count > 0) {
                            val slotWidth = size.width / count
                            val clickedIndex = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                            onSelectDay(clickedIndex)
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomPadding = 24.dp.toPx()
            val chartHeight = canvasHeight - bottomPadding
            val count = records.size

            if (count < 2) return@Canvas

            val stepX = canvasWidth / (count - 1).coerceAtLeast(1)

            // Benchmark 80% line
            val y80 = chartHeight - (0.80f * chartHeight)
            drawLine(
                color = GoldAccent.copy(alpha = 0.7f),
                start = Offset(0f, y80),
                end = Offset(canvasWidth, y80),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // 50% line
            val y50 = chartHeight - (0.50f * chartHeight)
            drawLine(
                color = Slate200,
                start = Offset(0f, y50),
                end = Offset(canvasWidth, y50),
                strokeWidth = 1.dp.toPx()
            )

            // Build line & gradient fill path
            val linePath = Path()
            val fillPath = Path()

            val points = mutableListOf<Offset>()
            records.forEachIndexed { i, record ->
                val score = record.quizScorePercent ?: 75 // default baseline
                val normScore = (score.toFloat() / 100f).coerceIn(0f, 1f)
                val x = i * stepX
                val y = chartHeight - (normScore * chartHeight)
                points.add(Offset(x, y))

                if (i == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, chartHeight)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            // Complete fill polygon
            fillPath.lineTo(points.last().x, chartHeight)
            fillPath.close()

            // Draw area gradient fill under line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(TealAccent.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = chartHeight
                )
            )

            // Draw line curve
            drawPath(
                path = linePath,
                color = TealAccent,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Draw individual data dots
            points.forEachIndexed { i, pt ->
                val hasActualQuiz = (records[i].quizScorePercent != null)
                val isSelected = (selectedIndex == i)

                if (hasActualQuiz || isSelected) {
                    drawCircle(
                        color = PureWhite,
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = if (isSelected) GoldAccent else TealAccent,
                        radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                        center = pt
                    )
                }
            }

            // Draw chart baseline
            drawLine(
                color = Slate300,
                start = Offset(0f, chartHeight),
                end = Offset(canvasWidth, chartHeight),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // X-Axis Date Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val first = records.firstOrNull()?.dayLabel ?: "Day 1"
            val mid = records.getOrNull(records.size / 2)?.dayLabel ?: "Day 15"
            val last = records.lastOrNull()?.dayLabel ?: "Today"

            Text(text = first, style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp))
            Text(text = mid, style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp))
            Text(text = last, style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }
    }
}

/**
 * Subject performance progress card with accuracy bar and stats.
 */
@Composable
private fun SubjectProgressCard(
    subjectName: String,
    scorePercent: Int,
    sessionsCount: Int,
    color: Color,
    badge: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$scorePercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = color,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { scorePercent / 100f },
                color = color,
                trackColor = Slate200,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = badge,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate600,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            )

            Text(
                text = "$sessionsCount Pomodoro focus blocks logged",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate400,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Interactive card displaying module completion rate, quiz accuracy average,
 * mastery badge, and quick launch buttons for practice.
 */
@Composable
private fun ModuleProgressItemCard(
    module: ModuleProgress,
    onTakeQuiz: () -> Unit,
    onReviewFlashcards: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPhysics = module.subject == Subject.PHYSICS
    val accentColor = if (isPhysics) PhysicsBlue else ChemistryOrange
    val masteryColor = when (module.masteryLevel) {
        "Mastered" -> SuccessGreen
        "Proficient" -> PhysicsBlue
        else -> GoldAccent
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("module_progress_card_${module.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${module.chapterNumber}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = module.chapterName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${module.subject.displayName} • ${module.academicClass.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = masteryColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, masteryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = module.masteryLevel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = masteryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Completion Rate Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Module Completion Rate",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                )
                Text(
                    text = "${module.completionRatePercent}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { module.completionRatePercent / 100f },
                color = accentColor,
                trackColor = Slate200,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quiz Performance Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quiz Accuracy Average",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                )
                Text(
                    text = "${module.quizScoreAveragePercent}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { module.quizScoreAveragePercent / 100f },
                color = GoldAccent,
                trackColor = Slate200,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Key concept snippet
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Slate100,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = module.keyConcept,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate700,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick practice action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.FilledTonalButton(
                    onClick = onTakeQuiz,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_module_quiz_${module.id}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Practice Quiz", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onReviewFlashcards,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_module_flashcards_${module.id}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flashcards", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

