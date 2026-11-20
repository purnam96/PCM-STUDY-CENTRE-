package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.auth.authStateFlow
import com.example.auth.signInWithGoogleInteractive
import com.example.auth.signOutGoogle
import com.example.data.PdfManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.model.AcademicClass
import com.example.model.StudyMaterial
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MaterialCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
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
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.AppDestination
import com.example.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val studentProfile by viewModel.studentProfile.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val allMaterials by viewModel.materials.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()

    val bookmarkedMaterials = remember(allMaterials, bookmarks) {
        allMaterials.filter { bookmarks.contains(it.id) }
    }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminErrorText by remember { mutableStateOf<String?>(null) }
    var notificationsEnabled by remember { mutableStateOf(studentProfile.notificationsEnabled) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Profile ID Card Banner
        item {
            Surface(
                color = NavyDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = VioletSecondary,
                                modifier = Modifier
                                    .size(54.dp)
                                    .border(2.dp, GoldAccent, CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = studentProfile.name.take(2).uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = studentProfile.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                )
                                Text(
                                    text = "${studentProfile.selectedClass.displayName} • ${studentProfile.bosemRollNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent)
                                )
                                Text(
                                    text = studentProfile.schoolName,
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 11.sp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = PureWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.08f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${studentProfile.streakDays} Days",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Study Streak",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.08f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${studentProfile.totalMaterialsStudied}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Topics Read",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.08f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${studentProfile.totalQuestionsPracticed}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Questions Done",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cloud Discussion Account (Google Sign-In / Sign-Out)
        item {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val auth = remember { runCatching { FirebaseAuth.getInstance() }.getOrNull() }
            val currentUser by if (auth != null) {
                remember(auth) { auth.authStateFlow() }
                    .collectAsStateWithLifecycle(initialValue = auth.currentUser)
            } else {
                remember { mutableStateOf(null) }
            }
            var isSigningIn by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentUser != null) "Discussion Account Connected" else "Peer Discussion Account",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = if (currentUser != null) {
                                currentUser?.displayName?.ifBlank { null } ?: "Signed in with Google"
                            } else {
                                "Sign in with Google to join real-time Q&A"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (auth != null) {
                        if (currentUser != null) {
                            TextButton(
                                onClick = {
                                    scope.launch { signOutGoogle(context, auth) }
                                },
                                modifier = Modifier.testTag("profile_google_sign_out")
                            ) {
                                Text("Sign Out", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            FilledTonalButton(
                                onClick = {
                                    scope.launch {
                                        isSigningIn = true
                                        signInWithGoogleInteractive(context, auth)
                                        isSigningIn = false
                                    }
                                },
                                enabled = !isSigningIn,
                                modifier = Modifier.testTag("profile_google_sign_in")
                            ) {
                                Text(if (isSigningIn) "..." else "Sign in with Google")
                            }
                        }
                    }
                }
            }
        }

        // Notification Preferences Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = VioletSecondary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = VioletSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Exam & Syllabus Alerts",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "BOSEM timetable, admit card & model paper releases",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                            )
                        }
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = NavyPrimary
                        ),
                        modifier = Modifier.testTag("notification_toggle_switch")
                    )
                }
            }
        }

        // Offline Downloads & Storage Quick Access
        item {
            val context = LocalContext.current
            val totalOfflineBytes = remember(downloadedIds, allMaterials) {
                PdfManager.getTotalDownloadedBytes(context, allMaterials)
            }
            val formattedOfflineStorage = remember(totalOfflineBytes) {
                PdfManager.formatFileSize(totalOfflineBytes)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PureWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TealAccent.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Offline Storage",
                                    tint = TealAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Offline Storage & Cache",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "${downloadedIds.size} documents saved • $formattedOfflineStorage used",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { viewModel.navigateTo(AppDestination.DOWNLOADS) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("profile_manage_downloads_button")
                    ) {
                        Text(
                            text = "Manage",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Saved Bookmarks Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Saved Bookmarks (${bookmarkedMaterials.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                }
            }
        }

        // Bookmarks List
        if (bookmarkedMaterials.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "No saved bookmarks yet. Tap the bookmark icon on any paper or notes card to save it for quick access here.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(bookmarkedMaterials) { mat ->
                MaterialCard(
                    material = mat,
                    isBookmarked = true,
                    isDownloaded = downloadedIds.contains(mat.id),
                    downloadProgress = null,
                    onView = { viewModel.openMaterial(mat) },
                    onToggleBookmark = { viewModel.toggleBookmark(mat.id) },
                    onDownload = { viewModel.downloadMaterial(mat) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Teacher & Admin Portal Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Teacher & Administrator Portal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = "Upload notes, manage past papers & post announcements",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showAdminLoginDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_admin_portal_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Access Admin Dashboard (PIN: 2026)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NavyDark,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }
            }
        }

        // About & Curriculum Accreditation
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_pcm_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PCM STUDY CENTRE",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "Empowering Minds & Academic Excellence",
                                style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Curriculum aligned with Board of Secondary Education, Manipur (BOSEM). Dedicated to providing top-quality Physics and Chemistry learning materials, 10-year question archives, and exam preparation guides for students across Manipur.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version 1.0.0 • Offline Storage Enabled",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(studentProfile.name) }
        var editSchool by remember { mutableStateOf(studentProfile.schoolName) }
        var editRoll by remember { mutableStateOf(studentProfile.bosemRollNumber) }
        var editClass by remember { mutableStateOf(studentProfile.selectedClass) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Student Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_student_name")
                    )

                    OutlinedTextField(
                        value = editSchool,
                        onValueChange = { editSchool = it },
                        label = { Text("School / Institution") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editRoll,
                        onValueChange = { editRoll = it },
                        label = { Text("BOSEM Roll Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(AcademicClass.CLASS_9, AcademicClass.CLASS_10).forEach { cls ->
                            val isSel = editClass == cls
                            FilledTonalButton(
                                onClick = { editClass = cls },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSel) NavyPrimary else Slate100,
                                    contentColor = if (isSel) PureWhite else Slate800
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(cls.displayName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editName, editClass, editSchool, editRoll)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Admin Passcode Dialog
    if (showAdminLoginDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminLoginDialog = false
                adminPinInput = ""
                adminErrorText = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Administrator Authentication")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter your PCM STUDY CENTRE teacher/admin PIN to access content uploads and archive management.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            adminErrorText = null
                        },
                        label = { Text("Admin PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = adminErrorText != null,
                        supportingText = {
                            if (adminErrorText != null) {
                                Text(adminErrorText!!, color = ErrorRed)
                            } else {
                                Text("Default educator passcode: 2026", color = Slate500)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val authenticated = viewModel.authenticateAdmin(adminPinInput)
                        if (authenticated) {
                            showAdminLoginDialog = false
                            adminPinInput = ""
                            adminErrorText = null
                            viewModel.openAdminDashboard()
                        } else {
                            adminErrorText = "Invalid Passcode. Please check educator credentials."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.testTag("admin_pin_submit_button")
                ) {
                    Text("Enter Dashboard")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAdminLoginDialog = false
                    adminPinInput = ""
                    adminErrorText = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
