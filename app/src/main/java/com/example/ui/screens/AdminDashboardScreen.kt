package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicClass
import com.example.model.Announcement
import com.example.model.MaterialCategory
import com.example.model.MaterialSection
import com.example.model.StudyMaterial
import com.example.model.Subject
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
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.MainViewModel
import java.util.UUID

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val materials by viewModel.materials.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    var activeAdminTab by remember { mutableIntStateOf(0) } // 0 = Materials, 1 = Announcements
    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }
    var materialToDelete by remember { mutableStateOf<StudyMaterial?>(null) }

    Scaffold(
        topBar = {
            Surface(color = NavyDark, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = PureWhite
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Admin & Educator Portal",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = GoldAccent
                                    ) {
                                        Text(
                                            text = "VERIFIED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NavyDark,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "PCM STUDY CENTRE • BOSEM Curriculum Management",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent, fontSize = 11.sp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.logoutAdmin()
                                onBack()
                            },
                            modifier = Modifier.testTag("admin_logout_button")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = PureWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "Total Materials",
                            value = "${materials.size}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "10-Yr Papers",
                            value = "${materials.count { it.category == MaterialCategory.PREVIOUS_YEAR_PAPERS }}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Notices",
                            value = "${announcements.size}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeAdminTab == 0) showAddMaterialDialog = true else showAddAnnouncementDialog = true
                },
                containerColor = GoldAccent,
                contentColor = NavyDark,
                modifier = Modifier.testTag("admin_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Entry")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate100)
        ) {
            TabRow(
                selectedTabIndex = activeAdminTab,
                containerColor = PureWhite,
                contentColor = NavyPrimary
            ) {
                Tab(
                    selected = activeAdminTab == 0,
                    onClick = { activeAdminTab = 0 },
                    text = { Text("Study Materials (${materials.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 1,
                    onClick = { activeAdminTab = 1 },
                    text = { Text("Announcements (${announcements.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (activeAdminTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(materials) { mat ->
                        AdminMaterialRow(
                            material = mat,
                            onToggleVerified = { viewModel.adminToggleVerified(mat.id) },
                            onDelete = { materialToDelete = mat },
                            onView = { viewModel.openMaterial(mat) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(announcements) { ann ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = ann.tag,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = VioletSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = ann.date,
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ann.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ann.message,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Material Dialog Form
    if (showAddMaterialDialog) {
        AddMaterialDialog(
            onDismiss = { showAddMaterialDialog = false },
            onAdd = { newMat ->
                viewModel.adminAddMaterial(newMat)
                showAddMaterialDialog = false
            }
        )
    }

    // Add Announcement Dialog
    if (showAddAnnouncementDialog) {
        AddAnnouncementDialog(
            onDismiss = { showAddAnnouncementDialog = false },
            onAdd = { newAnn ->
                viewModel.adminAddAnnouncement(newAnn)
                showAddAnnouncementDialog = false
            }
        )
    }

    // Delete Confirmation
    if (materialToDelete != null) {
        val mat = materialToDelete!!
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
            title = { Text("Unpublish & Delete Material?") },
            text = { Text("Are you sure you want to permanently delete '${mat.title}' from the curriculum repository?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.adminDeleteMaterial(mat.id)
                        materialToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed),
                    modifier = Modifier.testTag("admin_confirm_delete_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetricBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PureWhite.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 10.sp)
            )
        }
    }
}

@Composable
private fun AdminMaterialRow(
    material: StudyMaterial,
    onToggleVerified: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = "${material.academicClass.shortLabel} • ${material.subject.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (material.isVerified) SuccessGreen.copy(alpha = 0.12f) else Slate200
                    ) {
                        Text(
                            text = if (material.isVerified) "Verified" else "Draft",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (material.isVerified) SuccessGreen else Slate600,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${material.downloadCount} downloads",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = material.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = material.description,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onView,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Preview PDF", style = MaterialTheme.typography.labelSmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleVerified, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Toggle verified",
                            tint = if (material.isVerified) VioletSecondary else Slate400
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete document",
                            tint = ErrorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onAdd: (StudyMaterial) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(AcademicClass.CLASS_10) }
    var selectedSubj by remember { mutableStateOf(Subject.PHYSICS) }
    var selectedCat by remember { mutableStateOf(MaterialCategory.NOTES) }
    var chapterName by remember { mutableStateOf("") }
    var examYearText by remember { mutableStateOf("2025") }
    var isOfficial by remember { mutableStateOf(true) }
    var noteContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Study Material") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = chapterName,
                    onValueChange = { chapterName = it },
                    label = { Text("Chapter / Unit Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Brief Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { selectedClass = AcademicClass.CLASS_9 },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedClass == AcademicClass.CLASS_9) NavyPrimary else Slate100,
                            contentColor = if (selectedClass == AcademicClass.CLASS_9) PureWhite else Slate800
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("Class 9") }

                    FilledTonalButton(
                        onClick = { selectedClass = AcademicClass.CLASS_10 },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedClass == AcademicClass.CLASS_10) NavyPrimary else Slate100,
                            contentColor = if (selectedClass == AcademicClass.CLASS_10) PureWhite else Slate800
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("Class 10") }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { selectedSubj = Subject.PHYSICS },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedSubj == Subject.PHYSICS) NavyPrimary else Slate100,
                            contentColor = if (selectedSubj == Subject.PHYSICS) PureWhite else Slate800
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("Physics") }

                    FilledTonalButton(
                        onClick = { selectedSubj = Subject.CHEMISTRY },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedSubj == Subject.CHEMISTRY) NavyPrimary else Slate100,
                            contentColor = if (selectedSubj == Subject.CHEMISTRY) PureWhite else Slate800
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("Chemistry") }
                }

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Key Study Notes & Formulas") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isOfficial,
                        onCheckedChange = { isOfficial = it },
                        colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark as Official BOSEM Aligned", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val mat = StudyMaterial(
                            id = "custom-${UUID.randomUUID()}",
                            title = title,
                            description = description.ifBlank { "Study guide for $chapterName" },
                            academicClass = selectedClass,
                            subject = selectedSubj,
                            chapterName = chapterName.ifBlank { "General Science" },
                            chapterNumber = 1,
                            category = selectedCat,
                            academicYear = "2024-2025",
                            fileSize = "1.5 MB",
                            isOfficialBoard = isOfficial,
                            isVerified = true,
                            pageCount = 6,
                            sections = listOf(
                                MaterialSection(
                                    title = "Educator Notes: $title",
                                    content = noteContent.ifBlank { "Comprehensive verified learning resource from PCM Study Centre educators." }
                                )
                            )
                        )
                        onAdd(mat)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Publish to App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onAdd: (Announcement) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("BOSEM Alert") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post New Announcement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Category Tag (e.g. Exam Alert)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Announcement Message") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val ann = Announcement(
                            id = "ann-${UUID.randomUUID()}",
                            title = title,
                            message = message,
                            date = "Today",
                            tag = tag,
                            isUrgent = true
                        )
                        onAdd(ann)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Post Notice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
