package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FocusSession
import com.example.model.FocusSessionType
import com.example.model.Subject
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun PomodoroTimerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val studentProfile by viewModel.studentProfile.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()

    var selectedSessionType by remember { mutableStateOf(FocusSessionType.FOCUS) }
    var selectedSubject by remember { mutableStateOf(Subject.PHYSICS) }

    // Timer Duration in seconds
    var targetDurationSeconds by remember(selectedSessionType) {
        mutableIntStateOf(selectedSessionType.defaultMinutes * 60)
    }
    var remainingSeconds by remember(selectedSessionType) {
        mutableIntStateOf(selectedSessionType.defaultMinutes * 60)
    }
    var isRunning by remember { mutableStateOf(false) }

    // Countdown Effect
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            // Complete session
            val session = FocusSession(
                subject = selectedSubject,
                durationMinutes = targetDurationSeconds / 60,
                sessionType = selectedSessionType,
                isCompleted = true,
                chapterName = if (selectedSubject == Subject.PHYSICS) "Physics Focus Session" else "Chemistry Focus Session"
            )
            viewModel.recordFocusSession(session)
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (targetDurationSeconds > 0) remainingSeconds.toFloat() / targetDurationSeconds.toFloat() else 0f,
        label = "TimerProgress"
    )

    // Calculate today's stats
    val todayMinutes = remember(focusSessions) {
        viewModel.getTodayStudyMinutes()
    }
    val todayCompletedSessions = remember(focusSessions) {
        viewModel.getTodayCompletedSessionsCount()
    }

    val minutesPart = remainingSeconds / 60
    val secondsPart = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutesPart, secondsPart)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Surface(
                color = NavyDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(NavyDark, NavyPrimary)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pomodoro Study Timer",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PureWhite
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Track focused learning blocks & master formulas",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyanAccent,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        // Daily Streak Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${studentProfile.streakDays} Day Streak",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Study Time Summary Statistics Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today's Study Summary",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Study Time Today
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VioletSecondary.copy(alpha = 0.08f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_study_time_stat")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Focus Time Today",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (todayMinutes >= 60) "${todayMinutes / 60}h ${todayMinutes % 60}m" else "${todayMinutes}m",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = VioletSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Completed Sessions
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealAccent.copy(alpha = 0.08f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_sessions_count_stat")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Sessions Done",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$todayCompletedSessions Blocks",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = TealAccent
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Timer Preset Selector & Subject Selection
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Subject Selection Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = selectedSubject == Subject.PHYSICS,
                            onClick = { selectedSubject = Subject.PHYSICS },
                            label = { Text("Physics Focus") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletSecondary,
                                selectedLabelColor = PureWhite
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        FilterChip(
                            selected = selectedSubject == Subject.CHEMISTRY,
                            onClick = { selectedSubject = Subject.CHEMISTRY },
                            label = { Text("Chemistry Focus") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealAccent,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode Presets (25m Focus, 5m Short Break, 15m Long Break)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FocusSessionType.values().forEach { type ->
                            val isSelected = selectedSessionType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    isRunning = false
                                    selectedSessionType = type
                                    targetDurationSeconds = type.defaultMinutes * 60
                                    remainingSeconds = type.defaultMinutes * 60
                                },
                                label = { Text("${type.defaultMinutes}m ${type.label.split(" ").first()}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Circular Progress Canvas
                    val ringColor = if (selectedSubject == Subject.PHYSICS) VioletSecondary else TealAccent

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(230.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            // Track
                            drawCircle(
                                color = Color(0xFFE2E8F0),
                                style = Stroke(width = strokeWidth)
                            )
                            // Animated Progress
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate900
                                ),
                                modifier = Modifier.testTag("pomodoro_timer_display")
                            )
                            Text(
                                text = if (isRunning) "Focus in progress..." else "Ready to study",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isRunning) ringColor else Slate500,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset Button
                        IconButton(
                            onClick = {
                                isRunning = false
                                remainingSeconds = targetDurationSeconds
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Slate100)
                                .testTag("pomodoro_reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Timer",
                                tint = Slate700
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        // Play/Pause Main Button
                        Button(
                            onClick = { isRunning = !isRunning },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Slate800 else NavyPrimary
                            ),
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("pomodoro_start_pause_button"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause Timer" else "Start Timer",
                                tint = PureWhite,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        // Complete Now Button
                        IconButton(
                            onClick = {
                                isRunning = false
                                val elapsedMins = maxOf(1, (targetDurationSeconds - remainingSeconds) / 60)
                                val session = FocusSession(
                                    subject = selectedSubject,
                                    durationMinutes = elapsedMins,
                                    sessionType = selectedSessionType,
                                    isCompleted = true,
                                    chapterName = "${selectedSubject.displayName} Review Session"
                                )
                                viewModel.recordFocusSession(session)
                                remainingSeconds = targetDurationSeconds
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Slate100)
                                .testTag("pomodoro_complete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Complete Session",
                                tint = SuccessGreen
                            )
                        }
                    }
                }
            }
        }

        // Recent Completed Sessions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Slate700,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recent Study Sessions (${focusSessions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
            }
        }

        // Sessions List
        items(focusSessions.take(5)) { session ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val badgeColor = if (session.subject == Subject.PHYSICS) VioletSecondary else TealAccent
                        Surface(
                            shape = CircleShape,
                            color = badgeColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = session.chapterName.ifBlank { "${session.subject.displayName} Focus" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "${session.subject.displayName} • ${session.durationMinutes} Minutes",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = "+${session.durationMinutes}m",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
