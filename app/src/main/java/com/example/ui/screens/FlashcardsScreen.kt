package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicClass
import com.example.model.Flashcard
import com.example.model.LeitnerBox
import com.example.model.SrsRating
import com.example.model.Subject
import com.example.ui.theme.ChemistryOrange
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
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
import com.example.ui.theme.VioletSecondary
import com.example.ui.theme.WarningAmber
import com.example.util.SpacedRepetitionEngine
import com.example.viewmodel.MainViewModel

enum class DeckFilter(val label: String) {
    ALL("All Cards"),
    DUE_TODAY("Due for Review"),
    LEARNING("Learning (Box 1-2)"),
    REVIEWING("Reviewing (Box 3-4)"),
    MASTERED("Mastered (Box 5)")
}

/**
 * Interactive Spaced-Repetition Flashcard Screen for Physics and Chemistry formulas & definitions.
 * Employs the Leitner 5-box system and SuperMemo SM-2 interval expansion algorithm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedClass by viewModel.selectedClass.collectAsState()
    val allFlashcards by viewModel.flashcards.collectAsState()
    val masteredIds by viewModel.masteredFlashcardIds.collectAsState()

    var activeSubject by remember { mutableStateOf(Subject.PHYSICS) }
    var selectedDeckFilter by remember { mutableStateOf(DeckFilter.ALL) }
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var showAddCardSheet by remember { mutableStateOf(false) }

    // Filter cards for active subject, academic class, and selected deck filter
    val filteredCards = remember(allFlashcards, activeSubject, selectedClass, selectedDeckFilter) {
        val subjectCards = allFlashcards.filter {
            it.subject == activeSubject && it.academicClass == selectedClass
        }
        val now = System.currentTimeMillis()
        when (selectedDeckFilter) {
            DeckFilter.ALL -> subjectCards
            DeckFilter.DUE_TODAY -> subjectCards.filter { it.nextReviewEpochMillis <= now }
            DeckFilter.LEARNING -> subjectCards.filter { it.boxLevel in 1..2 }
            DeckFilter.REVIEWING -> subjectCards.filter { it.boxLevel in 3..4 }
            DeckFilter.MASTERED -> subjectCards.filter { it.boxLevel >= 5 || it.isMastered }
        }
    }

    // Keep card index bounded
    val safeIndex = if (filteredCards.isEmpty()) 0 else currentCardIndex.coerceIn(0, filteredCards.size - 1)
    val currentCard = filteredCards.getOrNull(safeIndex)

    // Flip Animation (0 to 180 degrees)
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "Flashcard3DRotation"
    )

    // Deck overall statistics
    val deckStats = remember(allFlashcards, activeSubject, selectedClass) {
        val subjectDeck = allFlashcards.filter { it.subject == activeSubject && it.academicClass == selectedClass }
        SpacedRepetitionEngine.calculateDeckStats(subjectDeck)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
            .testTag("flashcards_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Gradient Hero Header
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
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Spaced Repetition Decks",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = PureWhite
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Leitner 5-Box & SM-2 algorithm for long-term formula memory",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyanAccent,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            // Add Custom Card Button
                            IconButton(
                                onClick = { showAddCardSheet = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite.copy(alpha = 0.15f))
                                    .testTag("add_custom_flashcard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Formula Card",
                                    tint = PureWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Spaced Repetition Analytics Summary Row
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MetricColumn(
                                    value = "${deckStats.dueCount}",
                                    label = "Due Today",
                                    color = if (deckStats.dueCount > 0) WarningAmber else CyanAccent
                                )
                                MetricDivider()
                                MetricColumn(
                                    value = "${deckStats.learningCount}",
                                    label = "Learning (B1-2)",
                                    color = CyanAccent
                                )
                                MetricDivider()
                                MetricColumn(
                                    value = "${deckStats.reviewCount}",
                                    label = "Reviewing (B3-4)",
                                    color = VioletSecondary
                                )
                                MetricDivider()
                                MetricColumn(
                                    value = "${deckStats.masteredCount}",
                                    label = "Mastered (B5)",
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subject Selector Chips (Physics vs Chemistry)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeSubject == Subject.PHYSICS,
                    onClick = {
                        activeSubject = Subject.PHYSICS
                        currentCardIndex = 0
                        isFlipped = false
                    },
                    label = { Text("Physics Formulas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhysicsBlue,
                        selectedLabelColor = PureWhite
                    ),
                    modifier = Modifier.testTag("srs_subject_physics_chip")
                )

                FilterChip(
                    selected = activeSubject == Subject.CHEMISTRY,
                    onClick = {
                        activeSubject = Subject.CHEMISTRY
                        currentCardIndex = 0
                        isFlipped = false
                    },
                    label = { Text("Chemistry Reactions") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChemistryOrange,
                        selectedLabelColor = PureWhite
                    ),
                    modifier = Modifier.testTag("srs_subject_chemistry_chip")
                )
            }
        }

        // Leitner Box & Due Filter Horizontal Scroll
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeckFilter.entries.forEach { filter ->
                    val isSelected = selectedDeckFilter == filter
                    val countBadge = when (filter) {
                        DeckFilter.ALL -> deckStats.totalCount
                        DeckFilter.DUE_TODAY -> deckStats.dueCount
                        DeckFilter.LEARNING -> deckStats.learningCount
                        DeckFilter.REVIEWING -> deckStats.reviewCount
                        DeckFilter.MASTERED -> deckStats.masteredCount
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NavyPrimary else PureWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NavyPrimary else Slate300
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedDeckFilter = filter
                                currentCardIndex = 0
                                isFlipped = false
                            }
                            .testTag("deck_filter_${filter.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else Slate800,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) GoldAccent else Slate200,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$countBadge",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) NavyDark else Slate700,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Progress & Card Position Indicator
        if (filteredCards.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Card ${safeIndex + 1} of ${filteredCards.size}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                        )

                        // 5 Leitner Box Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (b in 1..5) {
                                val currentBox = currentCard?.boxLevel ?: 1
                                val isActive = b <= currentBox
                                Box(
                                    modifier = Modifier
                                        .size(if (b == currentBox) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (b == currentBox) GoldAccent
                                            else if (isActive) TealAccent
                                            else Slate300
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (safeIndex + 1).toFloat() / filteredCards.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (activeSubject == Subject.PHYSICS) PhysicsBlue else ChemistryOrange,
                        trackColor = Slate200
                    )
                }
            }
        }

        // 3D Flip Flashcard Main Interactive View
        if (filteredCards.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreen.copy(alpha = 0.12f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Deck Complete!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "No cards matching '${selectedDeckFilter.label}' right now. Select 'All Cards' to practice any formula, or add a custom flashcard.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate600,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        OutlinedButton(
                            onClick = { selectedDeckFilter = DeckFilter.ALL },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("View All Cards")
                        }
                    }
                }
            }
        } else if (currentCard != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 14f * density
                            }
                            .clickable { isFlipped = !isFlipped }
                            .testTag("srs_flashcard_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped) Slate900 else PureWhite
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                    ) {
                        if (rotation <= 90f) {
                            // ================= FRONT SIDE (PROMPT & FORMULA QUESTION) =================
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top Badges: Leitner Box & Due Indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val subjectColor = if (currentCard.subject == Subject.PHYSICS) PhysicsBlue else ChemistryOrange
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = subjectColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "[Box ${currentCard.boxLevel}] ${currentCard.leitnerBox.title}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = subjectColor,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Review Due Pill
                                    val dueText = SpacedRepetitionEngine.formatNextReviewText(currentCard.nextReviewEpochMillis)
                                    val isDue = currentCard.isDueForReview
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDue) WarningAmber.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = dueText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isDue) WarningAmber else SuccessGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Center Content
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentCard.chapterName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate400,
                                            letterSpacing = 1.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentCard.frontTitle,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = Slate900,
                                            lineHeight = 28.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    if (currentCard.frontSubtitle.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = currentCard.frontSubtitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Slate600
                                            ),
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Bottom Hint
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flip,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tap card to flip & reveal formula / law",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate500,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        } else {
                            // ================= BACK SIDE (ANSWER, DERIVATION & SRS RATINGS) =================
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FORMULA & CONCEPT SOLUTION",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = GoldAccent,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Highlighted Formula Box
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = PureWhite.copy(alpha = 0.12f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = currentCard.backContent,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = CyanAccent,
                                                lineHeight = 22.sp
                                            ),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    if (currentCard.keyPoints.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        currentCard.keyPoints.take(3).forEach { point ->
                                            Text(
                                                text = "• $point",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFFE2E8F0),
                                                    fontSize = 11.sp
                                                ),
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Rate your recall below to apply Spaced Repetition",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Spaced Repetition Recall Buttons (Shown when card is flipped or always accessible)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (isFlipped) "RATE RECALL FOR NEXT REVIEW INTERVAL:" else "SPACED REPETITION FEEDBACK:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate700,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Again (Forgot)
                        SrsResponseButton(
                            rating = SrsRating.AGAIN,
                            intervalText = SpacedRepetitionEngine.previewIntervalText(currentCard, SrsRating.AGAIN),
                            containerColor = Color(0xFFEF4444), // Coral Red
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.processSrsReview(currentCard.id, SrsRating.AGAIN)
                                isFlipped = false
                                if (currentCardIndex < filteredCards.size - 1) {
                                    currentCardIndex += 1
                                } else {
                                    currentCardIndex = 0
                                }
                                Toast.makeText(context, "Marked for daily review (Box 1)", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 2. Hard
                        SrsResponseButton(
                            rating = SrsRating.HARD,
                            intervalText = SpacedRepetitionEngine.previewIntervalText(currentCard, SrsRating.HARD),
                            containerColor = Color(0xFFF59E0B), // Amber
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.processSrsReview(currentCard.id, SrsRating.HARD)
                                isFlipped = false
                                if (currentCardIndex < filteredCards.size - 1) {
                                    currentCardIndex += 1
                                } else {
                                    currentCardIndex = 0
                                }
                                Toast.makeText(context, "Review interval set to ${SpacedRepetitionEngine.previewIntervalText(currentCard, SrsRating.HARD)}", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 3. Good
                        SrsResponseButton(
                            rating = SrsRating.GOOD,
                            intervalText = SpacedRepetitionEngine.previewIntervalText(currentCard, SrsRating.GOOD),
                            containerColor = PhysicsBlue,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.processSrsReview(currentCard.id, SrsRating.GOOD)
                                isFlipped = false
                                if (currentCardIndex < filteredCards.size - 1) {
                                    currentCardIndex += 1
                                } else {
                                    currentCardIndex = 0
                                }
                                Toast.makeText(context, "Promoted to next Leitner Box! (${SpacedRepetitionEngine.previewIntervalText(currentCard, SrsRating.GOOD)})", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 4. Easy
                        SrsResponseButton(
                            rating = SrsRating.EASY,
                            intervalText = SpacedRepetitionEngine.previewIntervalText(currentCard, SrsRating.EASY),
                            containerColor = SuccessGreen,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.processSrsReview(currentCard.id, SrsRating.EASY)
                                isFlipped = false
                                if (currentCardIndex < filteredCards.size - 1) {
                                    currentCardIndex += 1
                                } else {
                                    currentCardIndex = 0
                                }
                                Toast.makeText(context, "Mastered! Next review in ${SpacedRepetitionEngine.previewIntervalText(currentCard, SrsRating.EASY)}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Navigation Controls (Previous, Flip, Next, Reset)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentCardIndex > 0) {
                                isFlipped = false
                                currentCardIndex -= 1
                            }
                        },
                        enabled = currentCardIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("srs_prev_button")
                    ) {
                        Text("Previous")
                    }

                    FilledTonalButton(
                        onClick = { isFlipped = !isFlipped },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("srs_flip_toggle_button")
                    ) {
                        Icon(imageVector = Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFlipped) "Show Question" else "Show Answer")
                    }

                    Button(
                        onClick = {
                            if (currentCardIndex < filteredCards.size - 1) {
                                isFlipped = false
                                currentCardIndex += 1
                            } else {
                                currentCardIndex = 0
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("srs_next_button")
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Add Custom Formula Card
    if (showAddCardSheet) {
        AddCustomFlashcardSheet(
            activeSubject = activeSubject,
            selectedClass = selectedClass,
            onDismiss = { showAddCardSheet = false },
            onSave = { subject, chapter, title, subtitle, answer, bullets ->
                viewModel.addCustomFlashcard(
                    subject = subject,
                    chapterName = chapter,
                    frontTitle = title,
                    frontSubtitle = subtitle,
                    backContent = answer,
                    keyPoints = bullets
                )
                showAddCardSheet = false
                Toast.makeText(context, "Added new formula card to SRS deck!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun SrsResponseButton(
    rating: SrsRating,
    intervalText: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = modifier
            .height(50.dp)
            .clickable { onClick() }
            .testTag("srs_btn_${rating.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = rating.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = PureWhite,
                    fontSize = 11.5.sp
                )
            )
            Text(
                text = intervalText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = PureWhite.copy(alpha = 0.85f),
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun MetricColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate400,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(PureWhite.copy(alpha = 0.2f))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomFlashcardSheet(
    activeSubject: Subject,
    selectedClass: AcademicClass,
    onDismiss: () -> Unit,
    onSave: (Subject, String, String, String, String, List<String>) -> Unit
) {
    var subject by remember { mutableStateOf(activeSubject) }
    var chapterName by remember { mutableStateOf("") }
    var frontTitle by remember { mutableStateOf("") }
    var frontSubtitle by remember { mutableStateOf("") }
    var backContent by remember { mutableStateOf("") }
    var bulletPoints by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        modifier = Modifier.testTag("add_custom_card_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Custom Formula Card",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = subject == Subject.PHYSICS,
                    onClick = { subject = Subject.PHYSICS },
                    label = { Text("Physics") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhysicsBlue,
                        selectedLabelColor = PureWhite
                    )
                )
                FilterChip(
                    selected = subject == Subject.CHEMISTRY,
                    onClick = { subject = Subject.CHEMISTRY },
                    label = { Text("Chemistry") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChemistryOrange,
                        selectedLabelColor = PureWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = chapterName,
                onValueChange = { chapterName = it },
                label = { Text("Chapter (e.g. Light, Electricity, Carbon)") },
                modifier = Modifier.fillMaxWidth().testTag("custom_card_chapter_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = frontTitle,
                onValueChange = { frontTitle = it },
                label = { Text("Formula / Law Name (Front Title)") },
                modifier = Modifier.fillMaxWidth().testTag("custom_card_title_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = frontSubtitle,
                onValueChange = { frontSubtitle = it },
                label = { Text("Prompt / Subtitle (e.g. Relation between V, I and R)") },
                modifier = Modifier.fillMaxWidth().testTag("custom_card_subtitle_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = backContent,
                onValueChange = { backContent = it },
                label = { Text("Formula / Definition (Answer on Back)") },
                modifier = Modifier.fillMaxWidth().testTag("custom_card_answer_input"),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bulletPoints,
                onValueChange = { bulletPoints = it },
                label = { Text("Key Units & Sign Rules (Comma separated)") },
                placeholder = { Text("V in Volts, I in Amperes, R in Ohms") },
                modifier = Modifier.fillMaxWidth().testTag("custom_card_bullets_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (frontTitle.isNotBlank() && backContent.isNotBlank()) {
                        val bullets = bulletPoints.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(subject, chapterName, frontTitle, frontSubtitle, backContent, bullets)
                    }
                },
                enabled = frontTitle.isNotBlank() && backContent.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_custom_flashcard_btn")
            ) {
                Text("Save to Spaced Repetition Deck", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
