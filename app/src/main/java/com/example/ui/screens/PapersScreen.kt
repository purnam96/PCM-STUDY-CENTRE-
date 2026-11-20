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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicClass
import com.example.model.MaterialCategory
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MaterialCard
import com.example.ui.components.SearchBarField
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
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun PapersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val allMaterials by viewModel.materials.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = 10-Yr Papers, 1 = Sample Papers
    var selectedYearFilter by remember { mutableStateOf<Int?>(null) }
    var paperSearchQuery by remember { mutableStateOf("") }

    val targetCategory = if (activeTab == 0) MaterialCategory.PREVIOUS_YEAR_PAPERS else MaterialCategory.SAMPLE_PAPERS

    val papersList = remember(allMaterials, selectedClass, targetCategory, selectedYearFilter, paperSearchQuery) {
        allMaterials.filter { mat ->
            val matchClass = (mat.academicClass == selectedClass)
            val matchCategory = (mat.category == targetCategory)
            val matchYear = (selectedYearFilter == null || mat.examYear == selectedYearFilter)
            val matchSearch = if (paperSearchQuery.isBlank()) true else {
                mat.title.contains(paperSearchQuery, ignoreCase = true) ||
                        mat.description.contains(paperSearchQuery, ignoreCase = true) ||
                        mat.examYear?.toString()?.contains(paperSearchQuery) == true
            }
            matchClass && matchCategory && matchYear && matchSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Top Header
        Surface(
            color = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (activeTab == 0) "Previous 10 Years' Papers" else "Sample & Model Papers",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = PureWhite
                            )
                        )
                        Text(
                            text = "BOSEM ${selectedClass.displayName} • 2015 to 2024 Archive",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Class switcher
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite.copy(alpha = 0.15f)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            listOf(AcademicClass.CLASS_9, AcademicClass.CLASS_10).forEach { cls ->
                                val isSel = selectedClass == cls
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) PureWhite else androidx.compose.ui.graphics.Color.Transparent,
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .testTag("papers_class_${cls.name.lowercase()}")
                                ) {
                                    Text(
                                        text = cls.shortLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSel) NavyDark else PureWhite,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search inside papers
                SearchBarField(
                    query = paperSearchQuery,
                    onQueryChange = { paperSearchQuery = it },
                    placeholder = "Filter by year, subject, keywords...",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Tab Row: 10-Yr Papers vs Sample Papers
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = PureWhite,
            contentColor = NavyPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = VioletSecondary
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = if (activeTab == 0) VioletSecondary else Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "10-Year Papers (2015-2024)",
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                modifier = Modifier.testTag("tab_past_papers")
            )

            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = if (activeTab == 1) VioletSecondary else Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sample & Model Papers",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                modifier = Modifier.testTag("tab_sample_papers")
            )
        }

        // Year Filter Horizontal Scroll
        if (activeTab == 0) {
            Surface(
                color = PureWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedYearFilter == null,
                        onClick = { selectedYearFilter = null },
                        label = { Text("All Years (10)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = PureWhite
                        ),
                        modifier = Modifier.testTag("year_chip_all")
                    )

                    (2024 downTo 2015).forEach { yr ->
                        val isSelected = selectedYearFilter == yr
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedYearFilter = if (isSelected) null else yr },
                            label = { Text("$yr") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletSecondary,
                                selectedLabelColor = PureWhite
                            ),
                            modifier = Modifier.testTag("year_chip_$yr")
                        )
                    }
                }
            }
        }

        // Exam Blueprint Overview Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldAccent.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Official BOSEM Science Pattern",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "80 Theory Marks (Physics 40 + Chemistry 40) • 3 Hours • Complete step-by-step marking scheme",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                    )
                }
            }
        }

        // Papers List
        if (papersList.isEmpty()) {
            EmptyStateView(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "No question papers found",
                message = "Try clearing your year filter or search term to view all 10 years of archived question papers."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(papersList) { paper ->
                    MaterialCard(
                        material = paper,
                        isBookmarked = bookmarks.contains(paper.id),
                        isDownloaded = downloadedIds.contains(paper.id),
                        downloadProgress = downloadProgress[paper.id],
                        onView = { viewModel.openMaterial(paper) },
                        onToggleBookmark = { viewModel.toggleBookmark(paper.id) },
                        onDownload = { viewModel.downloadMaterial(paper) }
                    )
                }
            }
        }
    }
}
