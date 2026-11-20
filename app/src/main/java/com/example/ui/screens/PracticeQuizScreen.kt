package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.QuizFocusMode
import com.example.model.QuizQuestion
import com.example.model.Subject
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.PracticeQuizViewModel
import com.example.viewmodel.QuizUiState

@Composable
fun PracticeQuizScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    quizViewModel: PracticeQuizViewModel = viewModel()
) {
    val selectedClass by viewModel.selectedClass.collectAsStateWithLifecycle()
    val allMaterials by viewModel.materials.collectAsStateWithLifecycle()

    val uiState by quizViewModel.uiState.collectAsStateWithLifecycle()
    val activeSubject by quizViewModel.activeSubject.collectAsStateWithLifecycle()
    val selectedChapter by quizViewModel.selectedChapter.collectAsStateWithLifecycle()
    val focusMode by quizViewModel.focusMode.collectAsStateWithLifecycle()
    val questionCount by quizViewModel.questionCount.collectAsStateWithLifecycle()
    val customTopic by quizViewModel.customTopic.collectAsStateWithLifecycle()

    val currentIndex by quizViewModel.currentIndex.collectAsStateWithLifecycle()
    val selectedAnswers by quizViewModel.selectedAnswers.collectAsStateWithLifecycle()
    val isSubmitted by quizViewModel.isSubmitted.collectAsStateWithLifecycle()

    var showCustomizer by remember { mutableStateOf(false) }

    val availableChapters = remember(allMaterials, selectedClass, activeSubject) {
        allMaterials
            .filter { it.academicClass == selectedClass && it.subject == activeSubject }
            .sortedBy { it.chapterNumber }
            .map { it.chapterName }
            .distinct()
    }

    LaunchedEffect(selectedClass, allMaterials) {
        if (allMaterials.isNotEmpty()) {
            quizViewModel.ensureInitialQuizLoaded(selectedClass, allMaterials)
        }
    }

    val accentColor = if (activeSubject == Subject.PHYSICS) VioletSecondary else TealAccent

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            Surface(color = NavyDark, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(NavyDark, NavyPrimary)))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Practice Quiz",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PureWhite
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gemini MCQs grounded in ${selectedClass.displayName} syllabus notes",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyanAccent,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        FilledTonalButton(
                            onClick = { quizViewModel.generateQuiz(selectedClass, allMaterials) },
                            enabled = uiState !is QuizUiState.Loading && allMaterials.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("generate_quiz_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Generate new quiz",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Subject Selector + Filter Toggle
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = activeSubject == Subject.PHYSICS,
                            onClick = {
                                quizViewModel.selectSubject(
                                    Subject.PHYSICS,
                                    selectedClass,
                                    allMaterials
                                )
                            },
                            label = { Text("Physics") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletSecondary,
                                selectedLabelColor = PureWhite
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("quiz_subject_physics")
                        )
                        FilterChip(
                            selected = activeSubject == Subject.CHEMISTRY,
                            onClick = {
                                quizViewModel.selectSubject(
                                    Subject.CHEMISTRY,
                                    selectedClass,
                                    allMaterials
                                )
                            },
                            label = { Text("Chemistry") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealAccent,
                                selectedLabelColor = PureWhite
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("quiz_subject_chemistry")
                        )
                    }

                    OutlinedButton(
                        onClick = { showCustomizer = !showCustomizer },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("toggle_quiz_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize quiz",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showCustomizer) "Hide Options" else "Customize",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Chapter Strip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedChapter == null,
                            onClick = { quizViewModel.selectChapter(null) },
                            label = { Text("All Chapters") },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("quiz_chapter_all")
                        )
                    }
                    items(availableChapters) { chapter ->
                        FilterChip(
                            selected = selectedChapter == chapter,
                            onClick = { quizViewModel.selectChapter(chapter) },
                            label = {
                                Text(
                                    text = chapter,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }

        // Expandable Quiz Configurator
        if (showCustomizer) {
            item {
                QuizCustomizerCard(
                    focusMode = focusMode,
                    questionCount = questionCount,
                    customTopic = customTopic,
                    accentColor = accentColor,
                    isLoading = uiState is QuizUiState.Loading,
                    onFocusModeSelected = quizViewModel::setFocusMode,
                    onQuestionCountSelected = quizViewModel::setQuestionCount,
                    onCustomTopicChanged = quizViewModel::setCustomTopic,
                    onGenerateClicked = {
                        showCustomizer = false
                        quizViewModel.generateQuiz(selectedClass, allMaterials)
                    }
                )
            }
        }

        // Main State Body
        when (val state = uiState) {
            is QuizUiState.Idle -> {
                item {
                    QuizPromptReadyCard(
                        subjectName = activeSubject.displayName,
                        className = selectedClass.displayName,
                        chapterName = selectedChapter,
                        onGenerateClick = {
                            quizViewModel.generateQuiz(selectedClass, allMaterials)
                        }
                    )
                }
            }

            is QuizUiState.Loading -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("quiz_loading_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = accentColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Crafting ${questionCount} MCQs with Gemini...",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Reading ${selectedChapter ?: "all"} ${activeSubject.displayName} notes (${focusMode.label})",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                            )
                        }
                    }
                }
            }

            is QuizUiState.Error -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("quiz_error_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Quiz generation error",
                                tint = ErrorRed,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Could Not Generate Quiz",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    quizViewModel.generateQuiz(selectedClass, allMaterials)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("quiz_retry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry Generation", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            is QuizUiState.Ready -> {
                val questions = state.questions
                val totalQuestions = questions.size
                val currentQuestion = questions.getOrNull(currentIndex)

                if (isSubmitted) {
                    val score = questions.indices.count { idx ->
                        selectedAnswers[idx] == questions[idx].correctIndex
                    }
                    val scorePercentage = if (totalQuestions > 0) (score * 100) / totalQuestions else 0

                    item {
                        QuizResultsCard(
                            score = score,
                            totalQuestions = totalQuestions,
                            scorePercentage = scorePercentage,
                            subjectName = activeSubject.displayName,
                            onNewQuizClick = {
                                quizViewModel.generateQuiz(selectedClass, allMaterials)
                            }
                        )
                    }

                    item {
                        Text(
                            text = "Question Review & Explanations",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    items(questions.size) { index ->
                        QuizReviewQuestionCard(
                            index = index,
                            question = questions[index],
                            selectedOptionIndex = selectedAnswers[index]
                        )
                    }
                } else if (currentQuestion != null) {
                    // Question Stepper Pills
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(questions.size) { idx ->
                                val isCurrent = idx == currentIndex
                                val isAnswered = selectedAnswers.containsKey(idx)
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isCurrent -> accentColor
                                        isAnswered -> NavyPrimary.copy(alpha = 0.14f)
                                        else -> PureWhite
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            isCurrent -> accentColor
                                            isAnswered -> NavyPrimary
                                            else -> Slate200
                                        }
                                    ),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable { quizViewModel.jumpToQuestion(idx) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) PureWhite else Slate900
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Active Question Card
                    item {
                        ActiveQuizQuestionCard(
                            question = currentQuestion,
                            currentIndex = currentIndex,
                            totalQuestions = totalQuestions,
                            selectedOption = selectedAnswers[currentIndex],
                            answeredCount = selectedAnswers.size,
                            accentColor = accentColor,
                            onSelectOption = { optIndex ->
                                quizViewModel.selectAnswer(currentIndex, optIndex)
                            },
                            onPreviousClick = { quizViewModel.previousQuestion() },
                            onNextClick = { quizViewModel.nextQuestion() },
                            onSubmitClick = {
                                quizViewModel.submitQuiz { attempt ->
                                    viewModel.recordQuizAttempt(attempt)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizCustomizerCard(
    focusMode: QuizFocusMode,
    questionCount: Int,
    customTopic: String,
    accentColor: androidx.compose.ui.graphics.Color,
    isLoading: Boolean,
    onFocusModeSelected: (QuizFocusMode) -> Unit,
    onQuestionCountSelected: (Int) -> Unit,
    onCustomTopicChanged: (String) -> Unit,
    onGenerateClicked: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quiz Focus & Length",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(QuizFocusMode.entries) { mode ->
                    FilterChip(
                        selected = focusMode == mode,
                        onClick = { onFocusModeSelected(mode) },
                        label = { Text(mode.label) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Questions:",
                    style = MaterialTheme.typography.labelLarge.copy(color = Slate700)
                )
                listOf(3, 5, 8).forEach { count ->
                    FilterChip(
                        selected = questionCount == count,
                        onClick = { onQuestionCountSelected(count) },
                        label = { Text("$count") },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            OutlinedTextField(
                value = customTopic,
                onValueChange = onCustomTopicChanged,
                label = { Text("Specific concept or formula (optional)") },
                placeholder = { Text("e.g., Snell's law, Ohm's law, pH scale") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_custom_topic_input")
            )

            Button(
                onClick = onGenerateClicked,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("apply_quiz_config_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Quiz from Syllabus Notes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuizPromptReadyCard(
    subjectName: String,
    className: String,
    chapterName: String?,
    onGenerateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Ready to test $className $subjectName?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scope: ${chapterName ?: "All Chapters"}",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onGenerateClick,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Generate Questions with Gemini", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActiveQuizQuestionCard(
    question: QuizQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    answeredCount: Int,
    accentColor: androidx.compose.ui.graphics.Color,
    onSelectOption: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalQuestions.coerceAtLeast(1).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = Slate200
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of $totalQuestions",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate600
                    )
                )

                Surface(shape = RoundedCornerShape(8.dp), color = Slate100) {
                    Text(
                        text = question.chapterName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate700,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            question.options.forEachIndexed { optIndex, optionText ->
                val isSelected = selectedOption == optIndex
                val optionLetter = ('A'.code + optIndex).toChar()

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NavyPrimary.copy(alpha = 0.08f) else Slate50,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) NavyPrimary else Slate200
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectOption(optIndex) }
                        .testTag("quiz_option_$optIndex")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) NavyPrimary else Slate200,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$optionLetter",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PureWhite else Slate700
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isSelected) NavyPrimary else Slate900,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = onPreviousClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("quiz_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous question",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                } else {
                    Text(
                        text = "$answeredCount/$totalQuestions answered",
                        style = MaterialTheme.typography.labelMedium.copy(color = Slate600)
                    )
                }

                if (currentIndex < totalQuestions - 1) {
                    Button(
                        onClick = onNextClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("quiz_next_button")
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next question",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onSubmitClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("quiz_submit_button")
                    ) {
                        Text("Submit Quiz", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResultsCard(
    score: Int,
    totalQuestions: Int,
    scorePercentage: Int,
    subjectName: String,
    onNewQuizClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("quiz_results_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (scorePercentage >= 60) {
                    SuccessGreen.copy(alpha = 0.15f)
                } else {
                    GoldAccent.copy(alpha = 0.2f)
                },
                modifier = Modifier.size(84.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$scorePercentage%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (scorePercentage >= 60) SuccessGreen else Slate900
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when {
                    scorePercentage >= 80 -> "Outstanding Mastery!"
                    scorePercentage >= 60 -> "Solid Effort!"
                    else -> "Keep Revising!"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You scored $score out of $totalQuestions in $subjectName",
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNewQuizClick,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("retake_quiz_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Another Quiz", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuizReviewQuestionCard(
    index: Int,
    question: QuizQuestion,
    selectedOptionIndex: Int?
) {
    val isCorrect = selectedOptionIndex == question.correctIndex

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q${index + 1} • ${question.chapterName}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) SuccessGreen else ErrorRed
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = if (isCorrect) "Correct answer" else "Incorrect answer",
                    tint = if (isCorrect) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            question.options.forEachIndexed { optIndex, optText ->
                val isCorrectOpt = optIndex == question.correctIndex
                val isChosenOpt = optIndex == selectedOptionIndex

                val bg = when {
                    isCorrectOpt -> SuccessGreen.copy(alpha = 0.12f)
                    isChosenOpt && !isCorrectOpt -> ErrorRed.copy(alpha = 0.12f)
                    else -> Slate100
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = bg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A'.code + optIndex).toChar()}.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isCorrectOpt) SuccessGreen else Slate700
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = optText,
                            fontSize = 12.sp,
                            color = if (isCorrectOpt) SuccessGreen else Slate900
                        )
                    }
                }
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VioletLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Explanation: ${question.explanation}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = VioletSecondary,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
