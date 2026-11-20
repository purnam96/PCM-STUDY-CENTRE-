package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.FileDownloadOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.ui.components.ExportDossierDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfManager
import com.example.model.AcademicClass
import com.example.model.MaterialCategory
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.example.ui.components.EmptyStateView
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
fun DownloadsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allMaterials by viewModel.materials.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()

    var materialToDelete by remember { mutableStateOf<StudyMaterial?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    val isExporting by viewModel.isExportingDossier.collectAsState()
    val exportedFile by viewModel.exportedDossierFile.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val unDownloadedClassCount = remember(allMaterials, downloadedIds, selectedClass) {
        allMaterials.count { it.academicClass == selectedClass && !downloadedIds.contains(it.id) }
    }

    // Filters & Search
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf<Subject?>(null) }
    var selectedClassFilter by remember { mutableStateOf<AcademicClass?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<MaterialCategory?>(null) }

    // List of downloaded materials
    val allDownloadedMaterials = remember(allMaterials, downloadedIds) {
        allMaterials.filter { downloadedIds.contains(it.id) || it.isDownloaded }
    }

    // Filtered downloaded materials
    val filteredDownloadedMaterials = remember(
        allDownloadedMaterials,
        searchQuery,
        selectedSubjectFilter,
        selectedClassFilter,
        selectedCategoryFilter
    ) {
        allDownloadedMaterials.filter { mat ->
            val matchSubject = selectedSubjectFilter == null || mat.subject == selectedSubjectFilter
            val matchClass = selectedClassFilter == null || mat.academicClass == selectedClassFilter
            val matchCategory = selectedCategoryFilter == null || mat.category == selectedCategoryFilter
            val matchQuery = searchQuery.isBlank() ||
                    mat.title.contains(searchQuery, ignoreCase = true) ||
                    mat.chapterName.contains(searchQuery, ignoreCase = true) ||
                    mat.description.contains(searchQuery, ignoreCase = true) ||
                    (mat.examYear?.toString()?.contains(searchQuery) == true)
            matchSubject && matchClass && matchCategory && matchQuery
        }
    }

    // Storage calculation
    val totalBytes = remember(downloadedIds, allDownloadedMaterials) {
        PdfManager.getTotalDownloadedBytes(context, allDownloadedMaterials)
    }

    val formattedStorage = remember(totalBytes) {
        PdfManager.formatFileSize(totalBytes)
    }

    val physicsDownloadsCount = remember(allDownloadedMaterials) {
        allDownloadedMaterials.count { it.subject == Subject.PHYSICS }
    }
    val chemistryDownloadsCount = remember(allDownloadedMaterials) {
        allDownloadedMaterials.count { it.subject == Subject.CHEMISTRY }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Top Header
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Offline Storage & Downloads",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = PureWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cached BOSEM materials • Read anytime without internet",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyanAccent,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // Total Count Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite.copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${allDownloadedMaterials.size} Files",
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

        // Storage Management Overview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(VioletSecondary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Storage",
                                tint = VioletSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formattedStorage,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Slate900
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "used on device",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                )
                            }
                            Text(
                                text = "${allDownloadedMaterials.size} study documents offline ($physicsDownloadsCount Physics, $chemistryDownloadsCount Chemistry)",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Clear All / Free Storage Button
                    if (allDownloadedMaterials.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showClearAllDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ErrorRed
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("clear_all_downloads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Free Up Space",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Free Space",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Storage meter visual gauge (normalized to e.g. 50 MB cache quota)
                val quotaBytes = 50L * 1024L * 1024L
                val storageFraction = (totalBytes.toFloat() / quotaBytes.toFloat()).coerceIn(0.02f, 1f)

                LinearProgressIndicator(
                    progress = { if (totalBytes <= 0) 0f else storageFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = VioletSecondary,
                    trackColor = Slate200,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Local Cache Status: ${if (totalBytes > 0) "Active" else "Clean"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                    Text(
                        text = "50 MB Storage Quota",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (unDownloadedClassCount > 0) {
                        Button(
                            onClick = { viewModel.downloadAllClassMaterials() },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("download_all_class_materials_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save All ${selectedClass.displayName} ($unDownloadedClassCount)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.exportStudyDossier(context)
                            showExportDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_dossier_from_downloads_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export Dossier (PDF)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Search Bar for Cached Files
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("downloads_search_input"),
            placeholder = {
                Text(
                    text = "Search offline papers, notes, chapters...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search offline",
                    tint = Slate500
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Slate500
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite,
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = Slate200
            )
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // All Chip
            FilterChip(
                selected = selectedSubjectFilter == null && selectedClassFilter == null && selectedCategoryFilter == null,
                onClick = {
                    selectedSubjectFilter = null
                    selectedClassFilter = null
                    selectedCategoryFilter = null
                },
                label = { Text("All (${allDownloadedMaterials.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = PureWhite
                )
            )

            // Subject: Physics
            FilterChip(
                selected = selectedSubjectFilter == Subject.PHYSICS,
                onClick = {
                    selectedSubjectFilter = if (selectedSubjectFilter == Subject.PHYSICS) null else Subject.PHYSICS
                },
                label = { Text("Physics ($physicsDownloadsCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VioletSecondary,
                    selectedLabelColor = PureWhite
                )
            )

            // Subject: Chemistry
            FilterChip(
                selected = selectedSubjectFilter == Subject.CHEMISTRY,
                onClick = {
                    selectedSubjectFilter = if (selectedSubjectFilter == Subject.CHEMISTRY) null else Subject.CHEMISTRY
                },
                label = { Text("Chemistry ($chemistryDownloadsCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealAccent,
                    selectedLabelColor = PureWhite
                )
            )

            // Class 10
            FilterChip(
                selected = selectedClassFilter == AcademicClass.CLASS_10,
                onClick = {
                    selectedClassFilter = if (selectedClassFilter == AcademicClass.CLASS_10) null else AcademicClass.CLASS_10
                },
                label = { Text("Class 10") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = PureWhite
                )
            )

            // Class 9
            FilterChip(
                selected = selectedClassFilter == AcademicClass.CLASS_9,
                onClick = {
                    selectedClassFilter = if (selectedClassFilter == AcademicClass.CLASS_9) null else AcademicClass.CLASS_9
                },
                label = { Text("Class 9") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = PureWhite
                )
            )

            // Category: 10-Year Papers
            FilterChip(
                selected = selectedCategoryFilter == MaterialCategory.PREVIOUS_YEAR_PAPERS,
                onClick = {
                    selectedCategoryFilter = if (selectedCategoryFilter == MaterialCategory.PREVIOUS_YEAR_PAPERS) null else MaterialCategory.PREVIOUS_YEAR_PAPERS
                },
                label = { Text("Question Papers") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldAccent,
                    selectedLabelColor = NavyDark
                )
            )
        }

        // Main List Content or Empty State
        if (allDownloadedMaterials.isEmpty()) {
            EmptyStateView(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.FileDownloadOff,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(44.dp)
                    )
                },
                title = "No Offline Documents Cached",
                message = "Download revision notes, formula sheets, or past 10 years BOSEM HSLC papers to read and study offline anytime.",
                actionButton = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.downloadAllClassMaterials() },
                            modifier = Modifier.testTag("download_all_empty_state_action"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download All ${selectedClass.displayName} Syllabus Materials")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.navigateTo(AppDestination.PAPERS) },
                                modifier = Modifier.testTag("browse_papers_for_download")
                            ) {
                                Text("Browse Papers")
                            }
                            FilledTonalButton(
                                onClick = { viewModel.navigateTo(AppDestination.CLASSES) },
                                modifier = Modifier.testTag("browse_classes_for_download")
                            ) {
                                Text("Explore Classes")
                            }
                        }
                    }
                }
            )
        } else if (filteredDownloadedMaterials.isEmpty()) {
            EmptyStateView(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = "No Results Found",
                message = "No cached documents match your current search query or filter selection.",
                actionButton = {
                    TextButton(onClick = {
                        searchQuery = ""
                        selectedSubjectFilter = null
                        selectedClassFilter = null
                        selectedCategoryFilter = null
                    }) {
                        Text("Reset All Filters")
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDownloadedMaterials, key = { it.id }) { mat ->
                    val fileSizeText = PdfManager.getFileSizeForMaterial(context, mat)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_item_${mat.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Badge Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Subject Chip
                                    val subjectColor = if (mat.subject == Subject.PHYSICS) VioletSecondary else TealAccent
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = subjectColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = mat.subject.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = subjectColor,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    // Class Chip
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Slate100
                                    ) {
                                        Text(
                                            text = mat.academicClass.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Slate700,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    // Category Chip
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Slate100
                                    ) {
                                        Text(
                                            text = mat.category.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Slate600
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                // Storage size badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Slate100
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = ErrorRed,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = fileSizeText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Slate700,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Title & Chapter
                            Text(
                                text = mat.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (mat.chapterName.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Chapter: ${mat.chapterName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate600
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bottom Action Row: Offline Status + Read Offline + Delete Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Saved locally",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Saved Offline",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Read Offline Button
                                    FilledTonalButton(
                                        onClick = { viewModel.openMaterial(mat) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("read_offline_${mat.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Read Offline",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Read Offline",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    // Remove / Free space button
                                    IconButton(
                                        onClick = { materialToDelete = mat },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .testTag("delete_download_button")
                                            .testTag("delete_download_${mat.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove file to free space",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(20.dp)
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

    // Single-file Delete Confirmation Dialog
    if (materialToDelete != null) {
        val mat = materialToDelete!!
        val fileSizeStr = PdfManager.getFileSizeForMaterial(context, mat)

        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Remove Offline Document?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${mat.title}' from local storage? This will free $fileSizeStr of disk space. You can re-download it anytime.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDownloadedMaterial(mat)
                        materialToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed),
                    modifier = Modifier.testTag("confirm_delete_download_button")
                ) {
                    Text(
                        text = "Delete & Free Space",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All / Free All Storage Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Clear All Offline Downloads?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                )
            },
            text = {
                Text(
                    text = "This will remove all ${allDownloadedMaterials.size} offline study documents and free up $formattedStorage of storage space on your device. You can re-download any material later when connected to the internet.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDownloads()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text(
                        text = "Clear All & Free Space",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
