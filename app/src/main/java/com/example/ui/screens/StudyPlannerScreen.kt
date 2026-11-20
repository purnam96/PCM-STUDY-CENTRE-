package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AcademicClass
import com.example.model.ScheduledStudySession
import com.example.model.Subject
import com.example.ui.components.EmptyStateView
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Calendar-based Study Planner allowing students to schedule specific topics or chapters
 * for review, organize daily revision goals, and receive push notifications for upcoming sessions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlannerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheduledSessions by viewModel.scheduledSessions.collectAsState()
    val allMaterials by viewModel.materials.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    var showScheduleSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Calendar selection state: Default to today
    val todayCalendar = remember { Calendar.getInstance() }
    var selectedCalendar by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        )
    }

    // Filter by subject on selected day (null: All, Physics, Chemistry)
    var subjectFilter by remember { mutableStateOf<Subject?>(null) }

    // Check notification permission state (Android 13+ / API 33+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Study notification alerts enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications disabled. Reminders will not alert you.", Toast.LENGTH_LONG).show()
        }
    }

    // Filter sessions for the currently selected calendar date
    val selectedDayStart = selectedCalendar.timeInMillis
    val selectedDayEnd = selectedDayStart + (24L * 60 * 60 * 1000)

    val daySessions = remember(scheduledSessions, selectedDayStart, subjectFilter) {
        scheduledSessions.filter { session ->
            val matchDate = session.scheduledEpochMillis in selectedDayStart until selectedDayEnd
            val matchSubject = subjectFilter == null || session.subject == subjectFilter
            matchDate && matchSubject
        }.sortedBy { it.scheduledEpochMillis }
    }

    val totalDayMinutes = remember(daySessions) { daySessions.sumOf { it.durationMinutes } }
    val completedCount = remember(daySessions) { daySessions.count { it.isCompleted } }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_planner_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showScheduleSheet = true },
                containerColor = GoldAccent,
                contentColor = NavyDark,
                modifier = Modifier
                    .testTag("fab_schedule_session")
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Schedule New Topic"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate100)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Gradient Header Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(NavyDark, NavyPrimary)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 18.dp)
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
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "STUDY PLANNER",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = GoldAccent,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Targeted Review Calendar",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                )
                                Text(
                                    text = "Plan BOSEM Physics & Chemistry topics and receive push alerts",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                                )
                            }

                            // Quick Add Button in Header
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PureWhite.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .testTag("btn_header_add_session")
                                    .clickable { showScheduleSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Plan",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Notification Status Banner / Request Alert
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (hasNotificationPermission) PureWhite.copy(alpha = 0.08f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (hasNotificationPermission) PureWhite.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notification_permission_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (hasNotificationPermission) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                        contentDescription = "Notification Status",
                                        tint = if (hasNotificationPermission) CyanAccent else Color(0xFFFDE68A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (hasNotificationPermission) "Push Notifications Active" else "Enable Push Notifications",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = PureWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = if (hasNotificationPermission) "Timely reminders will sound before review sessions" else "Allow notification permission to receive study reminders",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (hasNotificationPermission) Slate400 else Color(0xFFFDE68A),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Button(
                                        onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .testTag("btn_request_notification_permission")
                                            .height(38.dp)
                                    ) {
                                        Text(
                                            text = "Enable",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NavyDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                } else {
                                    // Test Push Alert Button
                                    OutlinedButton(
                                        onClick = {
                                            val demoSession = daySessions.firstOrNull() ?: ScheduledStudySession(
                                                subject = Subject.PHYSICS,
                                                chapterName = "Light - Reflection & Refraction",
                                                topicTitle = "Mirror Formula Quick Review",
                                                durationMinutes = 45
                                            )
                                            viewModel.triggerDemoNotification(demoSession, context)
                                            Toast.makeText(context, "Test study notification sent!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                                        modifier = Modifier
                                            .testTag("btn_test_notification")
                                            .height(38.dp)
                                    ) {
                                        Text(
                                            text = "Test Alert",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Calendar Date Selector Strip
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Month Navigator Row
                        val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = monthYearFormat.format(selectedCalendar.time),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate100,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            val prev = (selectedCalendar.clone() as Calendar).apply {
                                                add(Calendar.DAY_OF_YEAR, -1)
                                            }
                                            selectedCalendar = prev
                                        }
                                        .testTag("btn_prev_day")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronLeft,
                                            contentDescription = "Previous Day",
                                            tint = Slate700,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate100,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            val next = (selectedCalendar.clone() as Calendar).apply {
                                                add(Calendar.DAY_OF_YEAR, 1)
                                            }
                                            selectedCalendar = next
                                        }
                                        .testTag("btn_next_day")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Next Day",
                                            tint = Slate700,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NavyLight,
                                    modifier = Modifier
                                        .clickable {
                                            selectedCalendar = Calendar.getInstance().apply {
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                        }
                                        .testTag("btn_today")
                                ) {
                                    Text(
                                        text = "Today",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Horizontal Scrollable Calendar Day Chips (Showing 14 days around selected date)
                        val daysList = remember(selectedCalendar) {
                            val list = mutableListOf<Calendar>()
                            val base = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.DAY_OF_YEAR, -3) // 3 days before
                            }
                            for (i in 0..13) {
                                list.add((base.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) })
                            }
                            list
                        }

                        val dayOfWeekFormat = remember { SimpleDateFormat("EEE", Locale.US) }
                        val dayNumFormat = remember { SimpleDateFormat("dd", Locale.US) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            daysList.forEach { cal ->
                                val isSelected = (cal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                                        cal.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR))
                                val isToday = (cal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                                        cal.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR))

                                val calStart = (cal.clone() as Calendar).apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                val calEnd = calStart + (24L * 60 * 60 * 1000)
                                val hasSession = scheduledSessions.any { it.scheduledEpochMillis in calStart until calEnd }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isSelected -> NavyPrimary
                                        isToday -> GoldAccent.copy(alpha = 0.2f)
                                        else -> Slate100
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when {
                                            isSelected -> NavyPrimary
                                            isToday -> GoldAccent
                                            else -> Color.Transparent
                                        }
                                    ),
                                    modifier = Modifier
                                        .width(52.dp)
                                        .clickable {
                                            selectedCalendar = (cal.clone() as Calendar).apply {
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                        }
                                        .testTag("date_chip_${cal.get(Calendar.DAY_OF_MONTH)}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = dayOfWeekFormat.format(cal.time),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) CyanAccent else Slate500,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = dayNumFormat.format(cal.time),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = if (isSelected) PureWhite else Slate800,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Scheduled session dot indicator
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        hasSession && isSelected -> GoldAccent
                                                        hasSession -> TealAccent
                                                        else -> Color.Transparent
                                                    }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Summary & Subject Filter Chips
            item {
                Spacer(modifier = Modifier.height(16.dp))
                val fullDateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.US) }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = fullDateFormat.format(selectedCalendar.time),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "$totalDayMinutes min review planned • $completedCount/${daySessions.size} completed",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }

                        // Subject Filter Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = subjectFilter == null,
                                onClick = { subjectFilter = null },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("filter_planner_all")
                            )

                            FilterChip(
                                selected = subjectFilter == Subject.PHYSICS,
                                onClick = { subjectFilter = if (subjectFilter == Subject.PHYSICS) null else Subject.PHYSICS },
                                label = { Text("Physics") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PhysicsBlue,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("filter_planner_physics")
                            )

                            FilterChip(
                                selected = subjectFilter == Subject.CHEMISTRY,
                                onClick = { subjectFilter = if (subjectFilter == Subject.CHEMISTRY) null else Subject.CHEMISTRY },
                                label = { Text("Chemistry") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChemistryOrange,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("filter_planner_chemistry")
                            )
                        }
                    }
                }
            }

            // Scheduled Sessions List for the selected day
            if (daySessions.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("planner_empty_state")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Slate100,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Study Sessions Scheduled",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Schedule specific chapters or topics to stay on track for your BOSEM Board Exams.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showScheduleSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .testTag("btn_empty_schedule_topic")
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Schedule Topic for Today", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(daySessions, key = { it.id }) { session ->
                    Spacer(modifier = Modifier.height(10.dp))
                    ScheduledSessionCard(
                        session = session,
                        onToggleCompleted = { viewModel.toggleSessionCompleted(session.id) },
                        onDelete = { viewModel.deleteScheduledSession(session.id, context) },
                        onStartReview = {
                            val targetMat = allMaterials.firstOrNull { it.id == session.materialId }
                                ?: allMaterials.firstOrNull { it.subject == session.subject && it.chapterName.contains(session.chapterName, ignoreCase = true) }
                            if (targetMat != null) {
                                viewModel.openMaterial(targetMat)
                            } else {
                                viewModel.navigateTo(AppDestination.CLASSES)
                            }
                        },
                        onStartTimer = {
                            viewModel.navigateTo(AppDestination.TIMER)
                        },
                        onTestAlert = {
                            viewModel.triggerDemoNotification(session, context)
                            Toast.makeText(context, "Push alert delivered for ${session.chapterName}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet: Schedule New Review Session
    if (showScheduleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showScheduleSheet = false },
            sheetState = sheetState,
            containerColor = PureWhite,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            ScheduleSessionSheetContent(
                selectedClass = selectedClass,
                availableMaterials = allMaterials,
                defaultEpochMillis = selectedCalendar.timeInMillis,
                onSchedule = { newSession ->
                    viewModel.scheduleSession(newSession, context)
                    showScheduleSheet = false
                    Toast.makeText(
                        context,
                        "Scheduled ${newSession.chapterName} with reminder ${newSession.reminderMinutesBefore}m before",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onCancel = { showScheduleSheet = false }
            )
        }
    }
}

/**
 * Card displaying an individual scheduled study session with interactive controls.
 */
@Composable
private fun ScheduledSessionCard(
    session: ScheduledStudySession,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onStartReview: () -> Unit,
    onStartTimer: () -> Unit,
    onTestAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val formattedTime = remember(session.scheduledEpochMillis) {
        timeFormat.format(java.util.Date(session.scheduledEpochMillis))
    }

    val subjectColor = if (session.subject == Subject.PHYSICS) PhysicsBlue else ChemistryOrange

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (session.isCompleted) androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)) else null,
        modifier = modifier.testTag("session_card_${session.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = session.isCompleted,
                        onCheckedChange = { onToggleCompleted() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SuccessGreen,
                            uncheckedColor = Slate400
                        ),
                        modifier = Modifier
                            .testTag("checkbox_${session.id}")
                            .size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = subjectColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = session.subject.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = subjectColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Slate100
                            ) {
                                Text(
                                    text = session.academicClass.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate600,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = session.chapterName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (session.isCompleted) Slate500 else Slate900,
                                textDecoration = if (session.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (session.topicTitle.isNotBlank()) {
                            Text(
                                text = session.topicTitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (session.isCompleted) Slate400 else Slate700,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Delete IconButton with accessible touch target
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .testTag("btn_delete_session_${session.id}")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Scheduled Session",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (session.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Goal: ${session.notes}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate600,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time, Duration & Reminder Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = NavyLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NavyLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${session.durationMinutes}m",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${session.reminderMinutesBefore}m before",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Action Buttons Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Test Push Alert Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate100,
                        modifier = Modifier
                            .clickable { onTestAlert() }
                            .testTag("btn_alert_session_${session.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Test Notification",
                                tint = VioletSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Alert",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = VioletDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Start Review Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyPrimary,
                        modifier = Modifier
                            .clickable { onStartReview() }
                            .testTag("btn_review_session_${session.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Read",
                                tint = PureWhite,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Review",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom Sheet content for configuring and scheduling a new study review session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSessionSheetContent(
    selectedClass: AcademicClass,
    availableMaterials: List<com.example.model.StudyMaterial>,
    defaultEpochMillis: Long,
    onSchedule: (ScheduledStudySession) -> Unit,
    onCancel: () -> Unit
) {
    var subject by remember { mutableStateOf(Subject.PHYSICS) }
    var targetClass by remember { mutableStateOf(selectedClass) }

    val chaptersForSubject = remember(availableMaterials, subject, targetClass) {
        availableMaterials
            .filter { it.subject == subject && it.academicClass == targetClass }
            .map { it.chapterName }
            .distinct()
            .ifEmpty {
                if (subject == Subject.PHYSICS) {
                    listOf("Light - Reflection and Refraction", "Electricity & Circuits", "Magnetic Effects of Electric Current")
                } else {
                    listOf("Chemical Reactions and Equations", "Acids, Bases and Salts", "Metals and Non-Metals")
                }
            }
    }

    var selectedChapter by remember { mutableStateOf(chaptersForSubject.firstOrNull() ?: "") }
    var topicTitle by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var reminderMinutesBefore by remember { mutableIntStateOf(10) }

    // Hour and Minute selector (Default 04:30 PM)
    var selectedHour by remember { mutableIntStateOf(16) }
    var selectedMinute by remember { mutableIntStateOf(30) }

    // Update chapter when subject changes
    LaunchedEffect(subject, targetClass) {
        val newChapters = availableMaterials
            .filter { it.subject == subject && it.academicClass == targetClass }
            .map { it.chapterName }
            .distinct()
        if (newChapters.isNotEmpty()) {
            selectedChapter = newChapters.first()
        }
    }

    var chapterDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("schedule_session_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Schedule Study Session",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Choose chapter and configure notification reminder",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }

            IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Close",
                    tint = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Switcher
        Text(text = "Subject", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Subject.values().forEach { subj ->
                val isSelected = (subject == subj)
                val color = if (subj == Subject.PHYSICS) PhysicsBlue else ChemistryOrange
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) color else Slate100,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { subject = subj }
                        .testTag("sheet_subject_${subj.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subj.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (isSelected) PureWhite else Slate700,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chapter Dropdown Selector
        Text(text = "Chapter / Syllabus Unit", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700))
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = chapterDropdownExpanded,
            onExpandedChange = { chapterDropdownExpanded = !chapterDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedChapter,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Slate300
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("dropdown_chapter_input")
            )

            ExposedDropdownMenu(
                expanded = chapterDropdownExpanded,
                onDismissRequest = { chapterDropdownExpanded = false }
            ) {
                chaptersForSubject.forEach { ch ->
                    DropdownMenuItem(
                        text = { Text(ch, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            selectedChapter = ch
                            chapterDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Topic / Key Focus Input
        Text(text = "Key Topic or Problem Set", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = topicTitle,
            onValueChange = { topicTitle = it },
            placeholder = { Text("e.g., Snell's Law numerical derivations, Balancing Equations") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = Slate300
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_topic_title")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Duration & Notification Timing Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Duration", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(25, 45, 60).forEach { mins ->
                        val isSelected = durationMinutes == mins
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NavyPrimary else Slate100,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { durationMinutes = mins }
                        ) {
                            Text(
                                text = "${mins}m",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) PureWhite else Slate700,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Remind Me", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5, 10, 15).forEach { mins ->
                        val isSelected = reminderMinutesBefore == mins
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GoldAccent else Slate100,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { reminderMinutesBefore = mins }
                        ) {
                            Text(
                                text = "${mins}m before",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) NavyDark else Slate700,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Time of Day Presets (Morning, Afternoon, Evening, Night)
        Text(text = "Scheduled Time", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(
                Triple("09:00 AM", 9, 0),
                Triple("02:00 PM", 14, 0),
                Triple("04:30 PM", 16, 30),
                Triple("07:00 PM", 19, 0)
            )

            presets.forEach { (label, h, m) ->
                val isSelected = (selectedHour == h && selectedMinute == m)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) TealAccent else Slate100,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedHour = h
                            selectedMinute = m
                        }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) PureWhite else Slate700,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Action: Schedule Button
        Button(
            onClick = {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = defaultEpochMillis
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If scheduled time for today has already passed, schedule for tomorrow at same time
                if (cal.timeInMillis <= System.currentTimeMillis()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val matchedMaterial = availableMaterials.firstOrNull {
                    it.subject == subject && it.chapterName.contains(selectedChapter, ignoreCase = true)
                }

                val newSession = ScheduledStudySession(
                    subject = subject,
                    academicClass = targetClass,
                    chapterName = selectedChapter.ifBlank { "General Syllabus Review" },
                    topicTitle = topicTitle.ifBlank { "Core Concept & Problem Review" },
                    scheduledEpochMillis = cal.timeInMillis,
                    durationMinutes = durationMinutes,
                    reminderMinutesBefore = reminderMinutesBefore,
                    notes = notes,
                    materialId = matchedMaterial?.id
                )
                onSchedule(newSession)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_confirm_schedule_session")
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Schedule Review & Set Reminder",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
