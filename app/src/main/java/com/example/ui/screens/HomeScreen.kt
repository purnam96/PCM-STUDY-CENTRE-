package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicClass
import com.example.model.MaterialCategory
import com.example.model.StudyMaterial
import com.example.model.Subject
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExportDossierDialog
import com.example.ui.components.MaterialCard
import com.example.ui.components.SearchBarField
import com.example.ui.components.StudyFilterSection
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

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val materials by viewModel.filteredMaterials.collectAsState()
    val allMaterials by viewModel.materials.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recentlyViewedIds by viewModel.recentlyViewedIds.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()

    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    val isExporting by viewModel.isExportingDossier.collectAsState()
    val exportedFile by viewModel.exportedDossierFile.collectAsState()

    val classMaterials = remember(allMaterials, selectedClass, isOffline, downloadedIds) {
        allMaterials.filter { 
            it.academicClass == selectedClass && (!isOffline || downloadedIds.contains(it.id) || it.isDownloaded)
        }
    }
    val physicsCount = remember(classMaterials) { classMaterials.count { it.subject == Subject.PHYSICS } }
    val chemistryCount = remember(classMaterials) { classMaterials.count { it.subject == Subject.CHEMISTRY } }
    val totalClassCount = remember(classMaterials) { classMaterials.size }

    val recentMaterials = allMaterials.filter { 
        recentlyViewedIds.contains(it.id) && (!isOffline || downloadedIds.contains(it.id) || it.isDownloaded)
    }
    val featuredMaterials = (if (isOffline) materials else allMaterials).filter { 
        it.academicClass == selectedClass && (!isOffline || downloadedIds.contains(it.id) || it.isDownloaded)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Welcome Header & Quick Stats
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NavyDark, NavyPrimary)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back, ${studentProfile.name.split(" ").firstOrNull() ?: "Student"}!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = "Targeting ${selectedClass.displayName} BOSEM Board Exam",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Streak Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldAccent.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                            modifier = Modifier.testTag("study_streak_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Study Streak",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${studentProfile.streakDays} Days",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent Search Bar
                    SearchBarField(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        placeholder = "Search notes, past papers, formulas...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Progress Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${studentProfile.totalMaterialsStudied}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                )
                                Text(
                                    text = "Topics Studied",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(PureWhite.copy(alpha = 0.2f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${studentProfile.totalQuestionsPracticed}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                )
                                Text(
                                    text = "Qs Practiced",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(PureWhite.copy(alpha = 0.2f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "10 Years",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                )
                                Text(
                                    text = "Papers Solved",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Announcement Banner
        if (announcements.isNotEmpty()) {
            val latestAnn = announcements.first()
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VioletLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("announcement_banner")
                        .clickable { viewModel.openAnnouncements() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = VioletSecondary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "Alert",
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = latestAnn.tag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = VioletDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${latestAnn.date}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp)
                                )
                            }
                            Text(
                                text = latestAnn.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Read announcements",
                            tint = VioletSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Upcoming Scheduled Review Session Card
        val upcomingSession = scheduledSessions.firstOrNull { !it.isCompleted }
        if (upcomingSession != null) {
            item {
                val timeFormat = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US) }
                val timeStr = remember(upcomingSession.scheduledEpochMillis) {
                    timeFormat.format(java.util.Date(upcomingSession.scheduledEpochMillis))
                }
                val isPhysics = upcomingSession.subject == Subject.PHYSICS
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPhysics) PhysicsBlue.copy(alpha = 0.08f) else ChemistryOrange.copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPhysics) PhysicsBlue.copy(alpha = 0.3f) else ChemistryOrange.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("upcoming_study_session_banner")
                        .clickable { viewModel.navigateTo(AppDestination.PLANNER) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isPhysics) PhysicsBlue else ChemistryOrange,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Upcoming Study Session",
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "UPCOMING REVIEW • $timeStr",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isPhysics) PhysicsBlue else ChemistryOrange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldAccent.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "${upcomingSession.durationMinutes}m",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = upcomingSession.chapterName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Open Planner",
                            tint = if (isPhysics) PhysicsBlue else ChemistryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Class 9 vs Class 10 Switcher
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(AcademicClass.CLASS_9, AcademicClass.CLASS_10).forEach { cls ->
                    val isSelected = selectedClass == cls
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NavyPrimary else PureWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NavyPrimary else Slate200
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setClass(cls) }
                            .testTag("tab_class_${cls.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = cls.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else Slate700
                                )
                            )
                            if (cls == AcademicClass.CLASS_10) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) GoldAccent else Slate100
                                ) {
                                    Text(
                                        text = "HSLC",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) NavyDark else Slate600,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dedicated Physics and Chemistry Subject Cards
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Core Subjects • ${selectedClass.displayName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Physics Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setSubject(Subject.PHYSICS)
                                viewModel.navigateTo(AppDestination.CLASSES)
                            }
                            .testTag("subject_card_physics")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(PhysicsBlue, VioletSecondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Physics",
                                    tint = PureWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Physics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = if (selectedClass == AcademicClass.CLASS_10) "Optics, Electricity, Energy" else "Motion, Gravitation, Sound",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "5 Chapters",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PhysicsBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = PhysicsBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Chemistry Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setSubject(Subject.CHEMISTRY)
                                viewModel.navigateTo(AppDestination.CLASSES)
                            }
                            .testTag("subject_card_chemistry")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(ChemistryOrange, GoldAccent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = "Chemistry",
                                    tint = PureWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Chemistry",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = if (selectedClass == AcademicClass.CLASS_10) "Reactions, Acids, Carbon" else "Matter, Atoms, Compounds",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (selectedClass == AcademicClass.CLASS_10) "5 Chapters" else "4 Chapters",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ChemistryOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = ChemistryOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Access Resource Grid
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "High-Yield Learning Hub",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Past 10 Years Papers
                    QuickActionTile(
                        title = "10-Yr Papers",
                        subtitle = "BOSEM 2015-2024",
                        icon = Icons.Default.HistoryEdu,
                        color = NavyLight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setCategory(MaterialCategory.PREVIOUS_YEAR_PAPERS)
                            viewModel.navigateTo(AppDestination.PAPERS)
                        }
                    )

                    // Chapter Notes
                    QuickActionTile(
                        title = "Revision Notes",
                        subtitle = "Formulas & Tips",
                        icon = Icons.Default.Description,
                        color = VioletDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setCategory(MaterialCategory.NOTES)
                            viewModel.navigateTo(AppDestination.CLASSES)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sample Papers
                    QuickActionTile(
                        title = "Sample Papers",
                        subtitle = "80-Mark Mock",
                        icon = Icons.Default.MenuBook,
                        color = TealAccent,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setCategory(MaterialCategory.SAMPLE_PAPERS)
                            viewModel.navigateTo(AppDestination.PAPERS)
                        }
                    )

                    // Important Questions Bank
                    QuickActionTile(
                        title = "Important Qs",
                        subtitle = "MCQ & Numericals",
                        icon = Icons.Default.Quiz,
                        color = ChemistryOrange,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.openImportantQuestions()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smart Study Tools & AI Hub Row 1 (Pomodoro & AI Practice Quiz)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Focus Timer",
                        subtitle = "Pomodoro & Stats",
                        icon = Icons.Default.Schedule,
                        color = TealAccent,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateTo(AppDestination.TIMER)
                        }
                    )

                    QuickActionTile(
                        title = "Practice Quiz",
                        subtitle = "AI MCQ Engine",
                        icon = Icons.Default.AutoAwesome,
                        color = VioletDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateToPracticeHub(0)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smart Study Tools & AI Hub Row 2 (Flashcards & 30-Day Learning Progress)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Flashcards",
                        subtitle = "SRS Formula Decks",
                        icon = Icons.Default.Style,
                        color = PhysicsBlue,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateToPracticeHub(1)
                        }
                    )

                    QuickActionTile(
                        title = "Progress Trends",
                        subtitle = "30-Day Analytics",
                        icon = Icons.Default.TrendingUp,
                        color = CyanAccent,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateToPracticeHub(2)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smart Study Tools & AI Hub Row 3 (Study Planner Calendar & My Favorites)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Study Planner",
                        subtitle = "Calendar & Push Alerts",
                        icon = Icons.Default.CalendarMonth,
                        color = GoldAccent,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateTo(AppDestination.PLANNER)
                        }
                    )

                    QuickActionTile(
                        title = "My Favorites",
                        subtitle = "Flagged Notes & Qs",
                        icon = Icons.Default.Bookmark,
                        color = VioletSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateTo(AppDestination.FAVORITES)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Peer Discussion Forum Feature Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppDestination.FORUM) }
                        .testTag("home_peer_forum_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(PhysicsBlue, TealAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = "Discussion Forum",
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "STUDENT DOUBT FORUM",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SuccessGreen.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "PEER DOUBTS & SOLUTIONS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Ask Physics & Chemistry Doubts",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = "Discuss numericals, share derivations, and get step-by-step solutions",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate400,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Open Forum",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Formatted Study Dossier Export Banner
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.exportStudyDossier(context)
                            showExportDialog = true
                        }
                        .testTag("home_export_dossier_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GoldAccent, VioletSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Export Study Dossier",
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "STUDY PROGRESS DOSSIER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyanAccent.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "PRINTABLE A4 PDF",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CyanAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Export Revision & Progress Dossier",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = "Formatted PDF with 30-day analytics, formula cheat sheet & bookmarked notes",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate400,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Export Dossier",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Continue Studying / Recently Viewed
        if (recentMaterials.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continue Studying",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Recent",
                            style = MaterialTheme.typography.labelMedium.copy(color = Slate500)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentMaterials) { mat ->
                            CompactRecentCard(
                                material = mat,
                                onClick = { viewModel.openMaterial(mat) }
                            )
                        }
                    }
                }
            }
        }

        // Study Materials Filter Section (Class dropdown & Subject tab-based navigation)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Study Materials & Notes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Filter by Class & Subject to access revision materials",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            StudyFilterSection(
                selectedClass = selectedClass,
                onClassSelected = { cls -> viewModel.setClass(cls) },
                selectedSubject = selectedSubject,
                onSubjectSelected = { subj -> viewModel.setSubject(subj) },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                searchPlaceholder = "Search materials by title or chapter...",
                totalCount = totalClassCount,
                physicsCount = physicsCount,
                chemistryCount = chemistryCount,
                selectedCategory = selectedCategory,
                onCategorySelected = { cat -> viewModel.setCategory(cat) },
                showCategoryRow = true,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (materials.isEmpty()) {
            item {
                EmptyStateView(
                    icon = {
                        Icon(
                            imageVector = if (searchQuery.isNotBlank()) Icons.Outlined.Search else Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = if (searchQuery.isNotBlank()) "No Matching Materials" else "No Materials Found",
                    message = if (searchQuery.isNotBlank())
                        "No study documents matched \"$searchQuery\" in title or chapter. Try checking the spelling or clearing the search filter."
                    else
                        "No study documents match your active class, subject, or category filters.",
                    actionButton = if (searchQuery.isNotBlank()) {
                        {
                            androidx.compose.material3.FilledTonalButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.testTag("reset_search_button")
                            ) {
                                Text("Clear Search Filter")
                            }
                        }
                    } else null,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )
            }
        } else {
            items(materials) { mat ->
                MaterialCard(
                    material = mat,
                    isBookmarked = bookmarks.contains(mat.id),
                    isDownloaded = downloadedIds.contains(mat.id),
                    downloadProgress = downloadProgress[mat.id],
                    onView = { viewModel.openMaterial(mat) },
                    onToggleBookmark = { viewModel.toggleBookmark(mat.id) },
                    onDownload = { viewModel.downloadMaterial(mat) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (showExportDialog) {
        ExportDossierDialog(
            isExporting = isExporting,
            exportedFile = exportedFile,
            onDismiss = {
                showExportDialog = false
                viewModel.clearExportedDossierState()
            },
            onPreviewInApp = { file ->
                showExportDialog = false
                viewModel.openExportedDossierInReader(file)
            }
        )
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PureWhite,
        shadowElevation = 1.dp,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate500,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CompactRecentCard(
    material: StudyMaterial,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(220.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (material.subject == Subject.PHYSICS) PhysicsBlue.copy(alpha = 0.1f) else ChemistryOrange.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = material.subject.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (material.subject == Subject.PHYSICS) PhysicsBlue else ChemistryOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "${material.pageCount} pgs",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = material.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = material.category.shortName,
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("•", color = Slate400, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = material.fileSize,
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 11.sp)
                )
            }
        }
    }
}
