package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StudyMaterial
import com.example.ui.components.PDFViewer
import com.example.ui.components.rememberPDFViewerState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    material: StudyMaterial,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val bookmarks by viewModel.bookmarks.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()
    val isBookmarked = bookmarks.contains(material.id)
    val isDownloaded = downloadedIds.contains(material.id)

    var viewMode by remember { mutableIntStateOf(0) } // 0 = PDF Rendered Pages, 1 = Syllabus Notes View
    val pdfViewerState = rememberPDFViewerState()

    val isNightMode = pdfViewerState.isNightMode
    val bgColor = if (isNightMode) Slate900 else Slate100
    val textColor = if (isNightMode) Slate100 else Slate900

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Reader App Bar
        Surface(
            color = NavyDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("pdf_reader_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PureWhite
                            )
                        }

                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = material.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${material.academicClass.displayName} • ${material.subject.displayName} • ${material.category.displayName}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Night mode toggle
                        IconButton(
                            onClick = { pdfViewerState.toggleNightMode() },
                            modifier = Modifier.testTag("reader_night_mode_toggle")
                        ) {
                            Icon(
                                imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle night mode",
                                tint = if (isNightMode) GoldAccent else PureWhite
                            )
                        }

                        // Bookmark button
                        IconButton(
                            onClick = { viewModel.toggleBookmark(material.id) },
                            modifier = Modifier.testTag("reader_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark document",
                                tint = if (isBookmarked) GoldAccent else PureWhite
                            )
                        }

                        // Download button
                        if (isDownloaded) {
                            Surface(
                                shape = CircleShape,
                                color = SuccessGreen.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(34.dp)
                                    .padding(2.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Saved Offline",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.downloadMaterial(material) },
                                modifier = Modifier.testTag("reader_download_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download document",
                                    tint = PureWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // View Mode Switcher: PDF Document vs Syllabus Notes
        TabRow(
            selectedTabIndex = viewMode,
            containerColor = NavyPrimary,
            contentColor = PureWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[viewMode]),
                    color = CyanAccent
                )
            }
        ) {
            Tab(
                selected = viewMode == 0,
                onClick = { viewMode = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ViewCarousel,
                            contentDescription = null,
                            tint = if (viewMode == 0) CyanAccent else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PDF Document",
                            fontWeight = if (viewMode == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                modifier = Modifier.testTag("tab_pdf_view")
            )

            Tab(
                selected = viewMode == 1,
                onClick = { viewMode = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TextSnippet,
                            contentDescription = null,
                            tint = if (viewMode == 1) CyanAccent else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Study Notes & Formulas",
                            fontWeight = if (viewMode == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                modifier = Modifier.testTag("tab_notes_view")
            )
        }

        // Main Reading Content
        if (viewMode == 0) {
            // High-performance PDFViewer Component with multi-page navigation, zoom, and thumbnails
            PDFViewer(
                material = material,
                state = pdfViewerState,
                showTopBar = false,
                onBookmarkClick = { viewModel.toggleBookmark(material.id) },
                isBookmarked = isBookmarked,
                onDownloadClick = { viewModel.downloadMaterial(material) },
                isDownloaded = isDownloaded,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            // Study Notes & Key Formulas Structured Mode
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Summary Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Overview & Exam Objectives",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = material.contentSummary.ifBlank { material.description },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Slate700,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }
                }

                // Key Takeaways Box
                if (material.keyTakeaways.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Exam Pointers & Must-Know Rules",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                material.keyTakeaways.forEach { takeaway ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("✔", color = GoldAccent, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = takeaway,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Slate800,
                                                lineHeight = 20.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sections
                itemsIndexed(material.sections) { index, section ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = section.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Slate700,
                                    lineHeight = 22.sp
                                )
                            )

                            if (section.formulaOrEquation != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TealAccent.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Core Equation / Formula:",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TealAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = section.formulaOrEquation,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = TealAccent,
                                                fontWeight = FontWeight.Black
                                            )
                                        )
                                    }
                                }
                            }

                            if (section.highlights.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                section.highlights.forEach { hl ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("•", color = VioletSecondary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = hl,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
