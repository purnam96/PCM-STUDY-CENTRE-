package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ImportantQuestion
import com.example.model.QuestionType
import com.example.model.Subject
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchBarField
import com.example.ui.theme.ChemistryOrange
import com.example.ui.theme.ChemistryOrangeLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
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
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun ImportantQuestionsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val selectedClass by viewModel.selectedClass.collectAsState()
    val questions by viewModel.filteredQuestions.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedQuestionType by viewModel.selectedQuestionType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val revealedAnswers = remember { mutableStateMapOf<String, Boolean>() }
    val userSelectedOptions = remember { mutableStateMapOf<String, Int>() }
    val practicedQuestions = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // App Bar
        Surface(
            color = NavyDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("questions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Important Questions Bank",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        )
                        Text(
                            text = "${selectedClass.displayName} • BOSEM High-Yield Questions",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldAccent,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SearchBarField(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    placeholder = "Search question text, formulas, topics...",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Subject Filter Row
        Surface(
            color = PureWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSubject == null,
                    onClick = { viewModel.setSubject(null) },
                    label = { Text("Both Subjects") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = PureWhite
                    )
                )

                FilterChip(
                    selected = selectedSubject == Subject.PHYSICS,
                    onClick = { viewModel.setSubject(if (selectedSubject == Subject.PHYSICS) null else Subject.PHYSICS) },
                    label = { Text("Physics") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhysicsBlue,
                        selectedLabelColor = PureWhite
                    )
                )

                FilterChip(
                    selected = selectedSubject == Subject.CHEMISTRY,
                    onClick = { viewModel.setSubject(if (selectedSubject == Subject.CHEMISTRY) null else Subject.CHEMISTRY) },
                    label = { Text("Chemistry") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChemistryOrange,
                        selectedLabelColor = PureWhite
                    )
                )
            }
        }

        // Question Type Filter Scroll Row
        Surface(
            color = PureWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedQuestionType == null,
                    onClick = { viewModel.setQuestionType(null) },
                    label = { Text("All Types (${questions.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VioletSecondary,
                        selectedLabelColor = PureWhite
                    )
                )

                QuestionType.values().forEach { type ->
                    val isSel = selectedQuestionType == type
                    FilterChip(
                        selected = isSel,
                        onClick = { viewModel.setQuestionType(if (isSel) null else type) },
                        label = { Text(type.badgeText) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletSecondary,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }
        }

        // List of Questions
        if (questions.isEmpty()) {
            EmptyStateView(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = "No questions found",
                message = "Try clearing search filters or changing the question type."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(questions) { question ->
                    val isAnswerRevealed = revealedAnswers[question.id] ?: false
                    val userOption = userSelectedOptions[question.id]
                    val isPracticed = practicedQuestions[question.id] ?: false

                    QuestionCard(
                        question = question,
                        isAnswerRevealed = isAnswerRevealed,
                        userSelectedOption = userOption,
                        isPracticed = isPracticed,
                        onToggleAnswer = {
                            revealedAnswers[question.id] = !isAnswerRevealed
                        },
                        onSelectOption = { optIndex ->
                            userSelectedOptions[question.id] = optIndex
                            revealedAnswers[question.id] = true
                            if (!isPracticed) {
                                practicedQuestions[question.id] = true
                                viewModel.recordQuestionPracticed()
                            }
                        },
                        onMarkPracticed = {
                            practicedQuestions[question.id] = true
                            viewModel.recordQuestionPracticed()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: ImportantQuestion,
    isAnswerRevealed: Boolean,
    userSelectedOption: Int?,
    isPracticed: Boolean,
    onToggleAnswer: () -> Unit,
    onSelectOption: (Int) -> Unit,
    onMarkPracticed: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (subjBg, subjFg) = if (question.subject == Subject.PHYSICS) {
                        PhysicsBlueLight to PhysicsBlue
                    } else {
                        ChemistryOrangeLight to ChemistryOrange
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjBg
                    ) {
                        Text(
                            text = question.subject.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(color = subjFg, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = question.type.badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate700, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (question.yearAsked != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = question.yearAsked,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chapter name
            Text(
                text = question.chapterName,
                style = MaterialTheme.typography.labelMedium.copy(color = Slate500, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Question Text
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    lineHeight = 22.sp
                )
            )

            // Numerical Given & Formula Callout
            if (question.numericalGiven != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Given: ${question.numericalGiven}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700, fontWeight = FontWeight.Medium)
                        )
                        if (question.numericalFormula != null) {
                            Text(
                                text = "Formula: ${question.numericalFormula}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TealAccent, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // MCQ Options (Interactive)
            if (question.type == QuestionType.MCQ && question.mcqOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.mcqOptions.forEachIndexed { idx, opt ->
                        val isSelected = userSelectedOption == idx
                        val isCorrect = idx == question.correctOptionIndex
                        val showCorrectness = userSelectedOption != null

                        val borderColor = when {
                            showCorrectness && isCorrect -> SuccessGreen
                            showCorrectness && isSelected && !isCorrect -> ErrorRed
                            isSelected -> VioletSecondary
                            else -> Slate200
                        }

                        val bgColor = when {
                            showCorrectness && isCorrect -> SuccessGreen.copy(alpha = 0.1f)
                            showCorrectness && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.1f)
                            isSelected -> VioletSecondary.copy(alpha = 0.08f)
                            else -> PureWhite
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bgColor,
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectOption(idx) }
                                .testTag("mcq_opt_${question.id}_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) VioletSecondary else Slate200,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = ('A' + idx).toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) PureWhite else Slate700,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate800,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (showCorrectness) {
                                    if (isCorrect) {
                                        Icon(Icons.Default.Check, contentDescription = "Correct", tint = SuccessGreen)
                                    } else if (isSelected) {
                                        Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Answer Explanation Box (Collapsible)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VioletSecondary.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { onToggleAnswer() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAnswerRevealed) "Hide Solution" else "View Solution & Explanation",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = VioletSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isAnswerRevealed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = VioletSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (!isPracticed) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate100,
                        modifier = Modifier.clickable { onMarkPracticed() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mark Done",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Practiced",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isAnswerRevealed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Official BOSEM Marking & Explanation:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = question.answerExplanation,
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
}
