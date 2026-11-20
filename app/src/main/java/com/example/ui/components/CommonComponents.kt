package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AcademicClass
import com.example.model.MaterialCategory
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.example.ui.theme.ChemistryOrange
import com.example.ui.theme.ChemistryOrangeLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PhysicsBlue
import com.example.ui.theme.PhysicsBlueLight
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
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.AppDestination

@Composable
fun PCMHeader(
    studentName: String,
    selectedClass: AcademicClass,
    onClassToggle: () -> Unit,
    onNotificationsClick: () -> Unit,
    isOffline: Boolean = false,
    onToggleOffline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavyDark,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pcm_logo),
                        contentDescription = "PCM Study Centre Emblem",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PCM STUDY CENTRE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = PureWhite
                                )
                            )
                        }
                        Text(
                            text = "BOSEM Manipur • Physics & Chemistry",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // State-based Network Status Pill (Online / Offline)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isOffline) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isOffline) Color(0xFFF59E0B) else Color(0xFF10B981).copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .testTag("network_status_indicator")
                            .clickable { onToggleOffline() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.Wifi,
                                contentDescription = if (isOffline) "Offline Mode" else "Online Mode",
                                tint = if (isOffline) Color(0xFFFDE68A) else PureWhite,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOffline) "Offline" else "Online",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isOffline) Color(0xFFFDE68A) else PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Class Selector Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = VioletSecondary.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletSecondary.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .testTag("class_switch_pill")
                            .clickable { onClassToggle() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = selectedClass.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .testTag("notifications_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = GoldAccent) {
                                    Text("4", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Announcements & Exam Updates",
                                tint = PureWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * State-based UI indicator banner that animates into view whenever the app is offline.
 * Displays offline warning, cached documents count, and informs the user that only cached content is shown.
 */
@Composable
fun OfflineIndicatorBanner(
    isOffline: Boolean,
    cachedCount: Int,
    onViewCachedClick: () -> Unit,
    onToggleOfflineMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = Color(0xFF1E293B), // Dark Slate
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offline_indicator_banner")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF59E0B).copy(alpha = 0.5f)
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline indicator",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Offline Mode Active",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PureWhite
                                    ),
                                    modifier = Modifier.testTag("offline_status_text")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                                    modifier = Modifier.testTag("offline_cached_count_badge")
                                ) {
                                    Text(
                                        text = "$cachedCount Offline Available",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFDE68A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Showing cached offline materials only • Network requests paused",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Toggle/Simulation Button for quick user testing/override
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PureWhite.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clickable { onToggleOfflineMode() }
                                .testTag("offline_mode_toggle_button")
                        ) {
                            Text(
                                text = "Go Online",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        // View Cached Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF59E0B),
                            modifier = Modifier
                                .clickable { onViewCachedClick() }
                                .testTag("offline_view_cached_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFF78350F),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Cached",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF78350F),
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PCMBottomBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = PureWhite,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        val items = listOf(
            Triple(AppDestination.HOME, Icons.Outlined.Home, "Home"),
            Triple(AppDestination.CLASSES, Icons.Outlined.Class, "Classes"),
            Triple(AppDestination.PLANNER, Icons.Default.CalendarMonth, "Planner"),
            Triple(AppDestination.PRACTICE, Icons.Default.Psychology, "Practice"),
            Triple(AppDestination.DOWNLOADS, Icons.Outlined.Folder, "Downloads")
        )

        items.forEach { (destination, icon, label) ->
            val selected = currentDestination == destination
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = destination.iconDescription
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureWhite,
                    indicatorColor = NavyPrimary,
                    selectedTextColor = NavyPrimary,
                    unselectedIconColor = Slate500,
                    unselectedTextColor = Slate500
                ),
                modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialCard(
    material: StudyMaterial,
    isBookmarked: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    onView: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDownload: () -> Unit,
    onDeleteDownload: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("material_card_${material.id}")
            .clickable { onView() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Subject & Category Pills + Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Subject Badge
                    val (subjBg, subjFg) = if (material.subject == Subject.PHYSICS) {
                        PhysicsBlueLight to PhysicsBlue
                    } else {
                        ChemistryOrangeLight to ChemistryOrange
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjBg
                    ) {
                        Text(
                            text = material.subject.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = subjFg,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = material.category.shortName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate700,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Official BOSEM badge
                    if (material.isOfficialBoard) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = VioletLight
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Official BOSEM",
                                    tint = VioletSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "BOSEM",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = VioletSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Bookmark toggle
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_${material.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove Bookmark" else "Save Bookmark",
                        tint = if (isBookmarked) GoldDark else Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = material.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    lineHeight = 22.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = material.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate600,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Tags row
            if (material.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    material.tags.take(3).forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate500,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom action row: Metadata (pages, size) & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${material.pageCount} Pages",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                        )
                    }

                    Text("•", color = Slate400, fontSize = 11.sp)

                    Text(
                        text = material.fileSize,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate500,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )

                    if (isDownloaded) {
                        Text("•", color = Slate400, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = SuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Offline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Download / Status Button
                    if (downloadProgress != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(38.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                strokeWidth = 3.dp,
                                color = NavyPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else if (isDownloaded) {
                        if (onDeleteDownload != null) {
                            IconButton(
                                onClick = onDeleteDownload,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_download_${material.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove offline file",
                                    tint = Slate400
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = SuccessGreen.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.DownloadDone,
                                        contentDescription = "Saved Offline",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("download_button_${material.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download PDF",
                                tint = NavyPrimary
                            )
                        }
                    }

                    // View PDF Button
                    FilledTonalButton(
                        onClick = onView,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("view_pdf_${material.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Read",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search notes, chapters, papers, formulas...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
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
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = Slate500
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PureWhite,
            unfocusedContainerColor = PureWhite,
            focusedBorderColor = VioletSecondary,
            unfocusedBorderColor = Slate200
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("global_search_input")
    )
}

@Composable
fun EmptyStateView(
    icon: @Composable () -> Unit,
    title: String,
    message: String,
    actionButton: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Slate100,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Slate500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        )
        if (actionButton != null) {
            Spacer(modifier = Modifier.height(16.dp))
            actionButton()
        }
    }
}
