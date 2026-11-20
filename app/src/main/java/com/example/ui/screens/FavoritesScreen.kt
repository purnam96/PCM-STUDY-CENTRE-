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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicClass
import com.example.model.Subject
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExportDossierDialog
import com.example.ui.components.MaterialCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealAccent
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.AppDestination
import com.example.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteMaterials by viewModel.favoriteMaterials.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    val context = LocalContext.current
    val isExportingDossier by viewModel.isExportingDossier.collectAsState()
    val exportedDossierFile by viewModel.exportedDossierFile.collectAsState()
    var showDossierDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf<Subject?>(null) }
    var selectedClassFilter by remember { mutableStateOf<AcademicClass?>(null) }

    val filteredFavorites = remember(
        favoriteMaterials,
        searchQuery,
        selectedSubjectFilter,
        selectedClassFilter
    ) {
        favoriteMaterials.filter { mat ->
            val matchSubject = selectedSubjectFilter == null || mat.subject == selectedSubjectFilter
            val matchClass = selectedClassFilter == null || mat.academicClass == selectedClassFilter
            val matchQuery = searchQuery.isBlank() ||
                    mat.title.contains(searchQuery, ignoreCase = true) ||
                    mat.chapterName.contains(searchQuery, ignoreCase = true) ||
                    mat.description.contains(searchQuery, ignoreCase = true)
            matchSubject && matchClass && matchQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Header Banner
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
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "My Favorites",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PureWhite
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Flagged BOSEM study notes, formulas & question papers",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GoldAccent,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        // Count Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${favoriteMaterials.size} Flagged",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prominent Export PDF Dossier Action Bar
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PureWhite.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDossierDialog = true
                                viewModel.exportStudyDossier(context)
                            }
                            .testTag("export_favorites_dossier_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = "Print Dossier",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Export Offline Revision Dossier",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Compiles progress summary, flagged notes & worked numericals to A4 PDF",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate400,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldAccent
                            ) {
                                Text(
                                    text = "Export PDF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NavyDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("favorites_search_input"),
            placeholder = {
                Text(
                    text = "Search in favorites...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Slate500
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
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
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedSubjectFilter == null && selectedClassFilter == null,
                onClick = {
                    selectedSubjectFilter = null
                    selectedClassFilter = null
                },
                label = { Text("All (${favoriteMaterials.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = PureWhite
                )
            )

            FilterChip(
                selected = selectedSubjectFilter == Subject.PHYSICS,
                onClick = {
                    selectedSubjectFilter = if (selectedSubjectFilter == Subject.PHYSICS) null else Subject.PHYSICS
                },
                label = { Text("Physics") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VioletSecondary,
                    selectedLabelColor = PureWhite
                )
            )

            FilterChip(
                selected = selectedSubjectFilter == Subject.CHEMISTRY,
                onClick = {
                    selectedSubjectFilter = if (selectedSubjectFilter == Subject.CHEMISTRY) null else Subject.CHEMISTRY
                },
                label = { Text("Chemistry") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealAccent,
                    selectedLabelColor = PureWhite
                )
            )

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
        }

        // List Content or Empty State
        if (favoriteMaterials.isEmpty()) {
            EmptyStateView(
                icon = {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(44.dp)
                    )
                },
                title = "No Favorite Materials Flagged",
                message = "Tap the bookmark icon on any chapter notes, question paper, or formula sheet to keep it handy in your personal favorites list.",
                actionButton = {
                    Button(
                        onClick = { viewModel.navigateTo(AppDestination.CLASSES) },
                        modifier = Modifier.testTag("browse_notes_for_favorites"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Explore Syllabus Notes")
                    }
                }
            )
        } else if (filteredFavorites.isEmpty()) {
            EmptyStateView(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = "No Matching Favorites",
                message = "No flagged study materials match your search or filter criteria.",
                actionButton = {
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedSubjectFilter = null
                            selectedClassFilter = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Reset Search")
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFavorites, key = { it.id }) { mat ->
                    MaterialCard(
                        material = mat,
                        isBookmarked = true,
                        isDownloaded = downloadedIds.contains(mat.id) || mat.isDownloaded,
                        downloadProgress = downloadProgress[mat.id],
                        onView = { viewModel.openMaterial(mat) },
                        onToggleBookmark = { viewModel.toggleBookmark(mat.id) },
                        onDownload = { viewModel.downloadMaterial(mat) }
                    )
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
