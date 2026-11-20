package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.viewmodel.MainViewModel

/**
 * Unified Practice & AI Hub combining:
 * 1. AI Practice Quiz (MCQs with Gemini)
 * 2. Digital Flashcards (3D flip cards with Gemini)
 * 3. Learning Progress Dashboard (Monthly native Canvas charts)
 */
@Composable
fun PracticeHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    initialTab: Int = 0
) {
    val currentTabFromVm by viewModel.practiceHubSelectedTab.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    androidx.compose.runtime.LaunchedEffect(currentTabFromVm) {
        selectedTab = currentTabFromVm
    }

    val tabs = listOf(
        Triple(0, "Practice Quiz", Icons.Default.Quiz),
        Triple(1, "Flashcards", Icons.Default.Style),
        Triple(2, "Progress", Icons.Default.TrendingUp)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("practice_hub_screen")
    ) {
        // Top Tab Row for switching between Practice, Flashcards, and Progress
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PureWhite,
            contentColor = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEach { (index, title, icon) ->
                val selected = selectedTab == index
                Tab(
                    selected = selected,
                    onClick = { 
                        selectedTab = index
                        viewModel.setPracticeHubTab(index)
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (selected) NavyPrimary else Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) NavyPrimary else Slate600
                            )
                        )
                    },
                    modifier = Modifier.testTag("practice_tab_$index")
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> PracticeQuizScreen(viewModel = viewModel)
                1 -> FlashcardsScreen(viewModel = viewModel)
                2 -> LearningProgressScreen(viewModel = viewModel)
            }
        }
    }
}
