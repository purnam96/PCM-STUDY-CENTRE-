package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfManager
import com.example.model.StudyMaterial
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VioletSecondary
import kotlinx.coroutines.launch
import java.io.File

/**
 * State holder for the PDFViewer component.
 * Controls active page index, zoom level, pan offset, night mode, and thumbnail navigation.
 */
@Stable
class PDFViewerState(
    initialPage: Int = 0,
    initialNightMode: Boolean = false
) {
    var currentPageIndex by mutableIntStateOf(initialPage)
    var totalPages by mutableIntStateOf(1)
    var scale by mutableFloatStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    var isNightMode by mutableStateOf(initialNightMode)
    var showThumbnailTray by mutableStateOf(false)
    var isFullscreen by mutableStateOf(false)
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    fun nextPage() {
        if (currentPageIndex < totalPages - 1) {
            currentPageIndex++
            resetZoom()
        }
    }

    fun previousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--
            resetZoom()
        }
    }

    fun goToPage(page: Int) {
        val clamped = page.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        if (clamped != currentPageIndex) {
            currentPageIndex = clamped
            resetZoom()
        }
    }

    fun zoomIn() {
        scale = (scale + 0.35f).coerceAtMost(4f)
    }

    fun zoomOut() {
        scale = (scale - 0.35f).coerceAtLeast(1f)
        if (scale == 1f) {
            offset = Offset.Zero
        }
    }

    fun resetZoom() {
        scale = 1f
        offset = Offset.Zero
    }

    fun toggleNightMode() {
        isNightMode = !isNightMode
    }

    fun toggleThumbnailTray() {
        showThumbnailTray = !showThumbnailTray
    }

    fun toggleFullscreen() {
        isFullscreen = !isFullscreen
    }
}

@Composable
fun rememberPDFViewerState(
    initialPage: Int = 0,
    initialNightMode: Boolean = false
): PDFViewerState {
    return remember {
        PDFViewerState(
            initialPage = initialPage,
            initialNightMode = initialNightMode
        )
    }
}

/**
 * High-performance, production-ready PDFViewer component designed for the PCM Study Centre app.
 * Displays study documents, previous year question papers, and sample notes with pinch-to-zoom,
 * page-by-page scrolling, thumbnail navigation drawer, page scrub slider, and night mode.
 */
@Composable
fun PDFViewer(
    material: StudyMaterial,
    modifier: Modifier = Modifier,
    state: PDFViewerState = rememberPDFViewerState(),
    onPageChanged: (pageIndex: Int, totalPages: Int) -> Unit = { _, _ -> },
    onBookmarkClick: (() -> Unit)? = null,
    isBookmarked: Boolean = false,
    onDownloadClick: (() -> Unit)? = null,
    isDownloaded: Boolean = false,
    onShareClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    showTopBar: Boolean = true
) {
    val context = LocalContext.current
    var pdfFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(material.id) {
        state.isLoading = true
        state.errorMessage = null
        try {
            val file = PdfManager.generateOrGetPdfFile(context, material)
            pdfFile = file
            val count = PdfManager.getPageCount(file)
            state.totalPages = if (count > 0) count else material.pageCount
            state.isLoading = false
        } catch (e: Exception) {
            state.errorMessage = "Failed to load document: ${e.message}"
            state.isLoading = false
        }
    }

    PDFViewer(
        pdfFile = pdfFile,
        title = material.title,
        subtitle = "${material.academicClass.displayName} • ${material.subject.displayName} • ${material.category.displayName}",
        modifier = modifier,
        state = state,
        onPageChanged = onPageChanged,
        onBookmarkClick = onBookmarkClick,
        isBookmarked = isBookmarked,
        onDownloadClick = onDownloadClick,
        isDownloaded = isDownloaded,
        onShareClick = onShareClick,
        onBack = onBack,
        showTopBar = showTopBar,
        isOfficialVerified = material.isOfficialBoard
    )
}

/**
 * File-based overload of PDFViewer component.
 */
@Composable
fun PDFViewer(
    pdfFile: File?,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    state: PDFViewerState = rememberPDFViewerState(),
    onPageChanged: (pageIndex: Int, totalPages: Int) -> Unit = { _, _ -> },
    onBookmarkClick: (() -> Unit)? = null,
    isBookmarked: Boolean = false,
    onDownloadClick: (() -> Unit)? = null,
    isDownloaded: Boolean = false,
    onShareClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    showTopBar: Boolean = true,
    isOfficialVerified: Boolean = true
) {
    val scope = rememberCoroutineScope()
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRenderingPage by remember { mutableStateOf(false) }

    // In-memory cache for page thumbnails to make jumping instant
    val thumbnailCache = remember { mutableStateMapOf<Int, Bitmap>() }
    val thumbnailListState = rememberLazyListState()

    // High contrast reading matrix for Night Mode
    val nightModeColorFilter = remember {
        ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    -0.85f, 0f, 0f, 0f, 235f,
                    0f, -0.85f, 0f, 0f, 235f,
                    0f, 0f, -0.85f, 0f, 235f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
    }

    // Function to load the active high-res page
    fun loadActivePage(index: Int) {
        val file = pdfFile ?: return
        isRenderingPage = true
        state.errorMessage = null
        scope.launch {
            try {
                val bmp = PdfManager.renderPdfPageToBitmap(file, index, targetWidth = 1200)
                if (bmp != null) {
                    currentBitmap = bmp
                    state.currentPageIndex = index
                    onPageChanged(index, state.totalPages)
                } else {
                    state.errorMessage = "Unable to render page ${index + 1}"
                }
            } catch (e: Exception) {
                state.errorMessage = e.message ?: "Page rendering error"
            } finally {
                isRenderingPage = false
            }
        }
    }

    // Load page whenever file or page index changes
    LaunchedEffect(pdfFile, state.currentPageIndex) {
        if (pdfFile != null && pdfFile.exists()) {
            val count = PdfManager.getPageCount(pdfFile)
            if (count > 0 && state.totalPages != count) {
                state.totalPages = count
            }
            loadActivePage(state.currentPageIndex)
        }
    }

    // Load thumbnails lazily when thumbnail tray is open
    LaunchedEffect(state.showThumbnailTray, pdfFile) {
        if (state.showThumbnailTray && pdfFile != null) {
            for (p in 0 until state.totalPages) {
                if (!thumbnailCache.containsKey(p)) {
                    val thumb = PdfManager.renderPdfPageToBitmap(pdfFile, p, targetWidth = 240)
                    if (thumb != null) {
                        thumbnailCache[p] = thumb
                    }
                }
            }
        }
    }

    // Gestures for zooming and panning
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        state.scale = (state.scale * zoomChange).coerceIn(1f, 4f)
        if (state.scale > 1f) {
            state.offset += offsetChange
        } else {
            state.offset = Offset.Zero
        }
    }

    val backgroundColor = if (state.isNightMode) Slate900 else Slate200
    val contentTextColor = if (state.isNightMode) Slate100 else Slate900

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("pdf_viewer_container")
    ) {
        // Optional Top Control Bar
        if (showTopBar && !state.isFullscreen) {
            Surface(
                color = NavyDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("pdf_viewer_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = PureWhite
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(start = if (onBack != null) 2.dp else 8.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (subtitle.isNotBlank()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldAccent,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Thumbnail tray toggle
                        IconButton(
                            onClick = { state.toggleThumbnailTray() },
                            modifier = Modifier.testTag("pdf_viewer_thumbnails_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Page thumbnails",
                                tint = if (state.showThumbnailTray) CyanAccent else PureWhite
                            )
                        }

                        // Night mode toggle
                        IconButton(
                            onClick = { state.toggleNightMode() },
                            modifier = Modifier.testTag("pdf_viewer_night_mode_button")
                        ) {
                            Icon(
                                imageVector = if (state.isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Night Mode Toggle",
                                tint = if (state.isNightMode) GoldAccent else PureWhite
                            )
                        }

                        // Bookmark button
                        if (onBookmarkClick != null) {
                            IconButton(
                                onClick = onBookmarkClick,
                                modifier = Modifier.testTag("pdf_viewer_bookmark_button")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) GoldAccent else PureWhite
                                )
                            }
                        }

                        // Download button
                        if (onDownloadClick != null) {
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
                                            contentDescription = "Downloaded",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                IconButton(
                                    onClick = onDownloadClick,
                                    modifier = Modifier.testTag("pdf_viewer_download_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download PDF",
                                        tint = PureWhite
                                    )
                                }
                            }
                        }

                        // Share button
                        if (onShareClick != null) {
                            IconButton(
                                onClick = onShareClick,
                                modifier = Modifier.testTag("pdf_viewer_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = PureWhite
                                )
                            }
                        }

                        // Fullscreen toggle
                        IconButton(
                            onClick = { state.toggleFullscreen() },
                            modifier = Modifier.testTag("pdf_viewer_fullscreen_button")
                        ) {
                            Icon(
                                imageVector = if (state.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = PureWhite
                            )
                        }
                    }
                }
            }
        }

        // Main Document Viewport
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(backgroundColor)
                .transformable(state = transformState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (state.scale > 1.2f) {
                                state.resetZoom()
                            } else {
                                state.scale = 2.2f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading || isRenderingPage -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            color = VioletSecondary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (state.isLoading) "Opening study document..." else "Rendering page ${state.currentPageIndex + 1} of ${state.totalPages}...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentTextColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Vector rendering with BOSEM curriculum fidelity",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                state.errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = state.errorMessage ?: "Failed to render document",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentTextColor,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FilledTonalButton(
                            onClick = { loadActivePage(state.currentPageIndex) },
                            modifier = Modifier.testTag("pdf_viewer_retry_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Rendering")
                        }
                    }
                }

                currentBitmap != null -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = if (state.isNightMode) 1.dp else 6.dp,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .graphicsLayer(
                                scaleX = state.scale,
                                scaleY = state.scale,
                                translationX = state.offset.x,
                                translationY = state.offset.y
                            )
                    ) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Document Page ${state.currentPageIndex + 1}",
                            colorFilter = if (state.isNightMode) nightModeColorFilter else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(currentBitmap!!.width.toFloat() / currentBitmap!!.height.toFloat())
                                .testTag("pdf_rendered_page_image")
                        )
                    }
                }
            }

            // Top-right Quick Zoom Floating Controls
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NavyDark.copy(alpha = 0.85f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { state.zoomOut() },
                        modifier = Modifier.size(32.dp).testTag("pdf_viewer_zoom_out")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "${(state.scale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier
                            .clickable { state.resetZoom() }
                            .padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = { state.zoomIn() },
                        modifier = Modifier.size(32.dp).testTag("pdf_viewer_zoom_in")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom In",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (state.scale > 1f) {
                        IconButton(
                            onClick = { state.resetZoom() },
                            modifier = Modifier.size(32.dp).testTag("pdf_viewer_reset_zoom")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitScreen,
                                contentDescription = "Fit to screen",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Exit fullscreen floating button if in fullscreen mode
            if (state.isFullscreen) {
                Surface(
                    shape = CircleShape,
                    color = NavyDark.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clickable { state.toggleFullscreen() }
                ) {
                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Fullscreen",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Expandable Page Thumbnail Strip Tray
        AnimatedVisibility(
            visible = state.showThumbnailTray,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                color = Slate900,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_thumbnail_tray")
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page Thumbnails (${state.totalPages} Pages)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(
                            onClick = { state.showThumbnailTray = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Thumbnails",
                                tint = Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        state = thumbnailListState,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        items(state.totalPages) { pIndex ->
                            val isSelected = state.currentPageIndex == pIndex
                            val thumbBitmap = thumbnailCache[pIndex]

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        state.goToPage(pIndex)
                                    }
                                    .testTag("pdf_thumb_page_$pIndex")
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) CyanAccent else Slate700
                                    ),
                                    color = Slate800,
                                    modifier = Modifier
                                        .width(56.dp)
                                        .height(78.dp)
                                ) {
                                    if (thumbBitmap != null) {
                                        Image(
                                            bitmap = thumbBitmap.asImageBitmap(),
                                            contentDescription = "Page ${pIndex + 1}",
                                            colorFilter = if (state.isNightMode) nightModeColorFilter else null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = "${pIndex + 1}",
                                                color = Slate400,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "P. ${pIndex + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) CyanAccent else Slate400,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar & Scrubber
        Surface(
            color = if (state.isNightMode) Slate900 else PureWhite,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Page Button
                    IconButton(
                        onClick = { state.previousPage() },
                        enabled = state.currentPageIndex > 0,
                        modifier = Modifier.testTag("pdf_viewer_prev_page")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Page",
                            tint = if (state.currentPageIndex > 0) {
                                if (state.isNightMode) PureWhite else NavyPrimary
                            } else Slate400
                        )
                    }

                    // Page Indicator Text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Page ${state.currentPageIndex + 1} of ${state.totalPages}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (state.isNightMode) PureWhite else Slate900
                            ),
                            modifier = Modifier.testTag("pdf_viewer_page_indicator")
                        )
                        if (isOfficialVerified) {
                            Text(
                                text = "BOSEM Verified Curriculum",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SuccessGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Next Page Button
                    IconButton(
                        onClick = { state.nextPage() },
                        enabled = state.currentPageIndex < state.totalPages - 1,
                        modifier = Modifier.testTag("pdf_viewer_next_page")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Page",
                            tint = if (state.currentPageIndex < state.totalPages - 1) {
                                if (state.isNightMode) PureWhite else NavyPrimary
                            } else Slate400
                        )
                    }
                }

                // Slider Scrubber for Smooth Multi-Page Jump
                if (state.totalPages > 1) {
                    Slider(
                        value = state.currentPageIndex.toFloat(),
                        onValueChange = { newIdx ->
                            state.goToPage(newIdx.toInt())
                        },
                        valueRange = 0f..(state.totalPages - 1).toFloat(),
                        steps = (state.totalPages - 2).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = if (state.isNightMode) CyanAccent else NavyPrimary,
                            activeTrackColor = VioletSecondary,
                            inactiveTrackColor = if (state.isNightMode) Slate700 else Slate200
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .testTag("pdf_viewer_page_slider")
                    )
                }
            }
        }
    }
}
