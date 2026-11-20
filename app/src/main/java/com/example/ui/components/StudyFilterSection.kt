package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicClass
import com.example.model.MaterialCategory
import com.example.model.Subject
import com.example.ui.theme.ChemistryOrange
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
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
import com.example.ui.theme.TealAccent
import com.example.ui.theme.VioletSecondary

/**
 * High-performance, Material 3 study material filter component supporting both
 * tab-based navigation and dropdown menu selection for subject (Physics, Chemistry, All)
 * and academic class level (Class 9, Class 10).
 */
@Composable
fun StudyFilterSection(
    selectedClass: AcademicClass,
    onClassSelected: (AcademicClass) -> Unit,
    selectedSubject: Subject?,
    onSubjectSelected: (Subject?) -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    searchPlaceholder: String = "Search materials by title or chapter...",
    totalCount: Int = 0,
    physicsCount: Int = 0,
    chemistryCount: Int = 0,
    selectedCategory: MaterialCategory? = null,
    onCategorySelected: ((MaterialCategory?) -> Unit)? = null,
    showCategoryRow: Boolean = false
) {
    var isClassDropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        color = PureWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_filter_section")
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
            // Real-Time Search Bar at Top of Study Materials
            if (onSearchQueryChange != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    SearchBarField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        placeholder = searchPlaceholder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_material_search_bar")
                    )

                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtering title or chapter: \"$searchQuery\"",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = VioletSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate100,
                                modifier = Modifier
                                    .testTag("clear_study_search")
                                    .clickable { onSearchQueryChange("") }
                            ) {
                                Text(
                                    text = "Clear filter",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate600,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Header Row: Class Level Selector with Dropdown and Quick Segmented Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Class Dropdown Menu Picker
                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier
                            .testTag("filter_class_dropdown_trigger")
                            .clickable { isClassDropdownExpanded = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Select Class",
                                tint = NavyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${selectedClass.displayName} (BOSEM)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Open dropdown",
                                tint = Slate600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isClassDropdownExpanded,
                        onDismissRequest = { isClassDropdownExpanded = false },
                        modifier = Modifier
                            .background(PureWhite)
                            .testTag("filter_class_dropdown_menu")
                    ) {
                        AcademicClass.values().forEach { cls ->
                            val isSelected = cls == selectedClass
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (cls == AcademicClass.CLASS_10) {
                                                "Class 10 (HSLC BOSEM Board)"
                                            } else {
                                                "Class 9 (BOSEM Foundation)"
                                            },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) NavyPrimary else Slate900
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = NavyPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onClassSelected(cls)
                                    isClassDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("filter_class_option_${cls.name.lowercase()}")
                            )
                        }
                    }
                }

                // Quick Segmented Class Switcher (Class 9 | Class 10)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AcademicClass.values().forEach { cls ->
                        val isSelected = cls == selectedClass
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) NavyPrimary else Slate100,
                            animationSpec = tween(durationMillis = 200),
                            label = "ClassChipBg"
                        )
                        val chipText by animateColorAsState(
                            targetValue = if (isSelected) PureWhite else Slate700,
                            animationSpec = tween(durationMillis = 200),
                            label = "ClassChipText"
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = chipBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NavyPrimary else Slate200
                            ),
                            modifier = Modifier
                                .testTag("filter_class_tab_${cls.name.lowercase()}")
                                .clickable { onClassSelected(cls) }
                        ) {
                            Text(
                                text = cls.shortLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = chipText
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab-Based Navigation for Subjects: All (Combined) | Physics | Chemistry
            val subjectTabs = listOf(
                Triple("All Subjects", null as Subject?, totalCount),
                Triple("Physics", Subject.PHYSICS, physicsCount),
                Triple("Chemistry", Subject.CHEMISTRY, chemistryCount)
            )

            val activeTabIndex = when (selectedSubject) {
                null -> 0
                Subject.PHYSICS -> 1
                Subject.CHEMISTRY -> 2
            }

            TabRow(
                selectedTabIndex = activeTabIndex,
                containerColor = PureWhite,
                contentColor = NavyPrimary,
                indicator = { tabPositions ->
                    val indicatorColor = when (selectedSubject) {
                        null -> NavyPrimary
                        Subject.PHYSICS -> PhysicsBlue
                        Subject.CHEMISTRY -> ChemistryOrange
                    }
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTabIndex]),
                        color = indicatorColor
                    )
                },
                modifier = Modifier.testTag("filter_subject_tab_row")
            ) {
                // Tab 0: All Subjects
                Tab(
                    selected = selectedSubject == null,
                    onClick = { onSubjectSelected(null) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "All Subjects",
                                tint = if (selectedSubject == null) NavyPrimary else Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All",
                                fontWeight = if (selectedSubject == null) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSubject == null) NavyPrimary else Slate700,
                                fontSize = 13.sp
                            )
                            if (totalCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                CountBadge(count = totalCount, isSelected = selectedSubject == null, activeColor = NavyPrimary)
                            }
                        }
                    },
                    modifier = Modifier.testTag("filter_subject_tab_all")
                )

                // Tab 1: Physics
                Tab(
                    selected = selectedSubject == Subject.PHYSICS,
                    onClick = { onSubjectSelected(Subject.PHYSICS) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Physics",
                                tint = if (selectedSubject == Subject.PHYSICS) PhysicsBlue else Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Physics",
                                fontWeight = if (selectedSubject == Subject.PHYSICS) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSubject == Subject.PHYSICS) PhysicsBlue else Slate700,
                                fontSize = 13.sp
                            )
                            if (physicsCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                CountBadge(count = physicsCount, isSelected = selectedSubject == Subject.PHYSICS, activeColor = PhysicsBlue)
                            }
                        }
                    },
                    modifier = Modifier.testTag("filter_subject_tab_physics")
                )

                // Tab 2: Chemistry
                Tab(
                    selected = selectedSubject == Subject.CHEMISTRY,
                    onClick = { onSubjectSelected(Subject.CHEMISTRY) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Chemistry",
                                tint = if (selectedSubject == Subject.CHEMISTRY) ChemistryOrange else Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Chemistry",
                                fontWeight = if (selectedSubject == Subject.CHEMISTRY) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSubject == Subject.CHEMISTRY) ChemistryOrange else Slate700,
                                fontSize = 13.sp
                            )
                            if (chemistryCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                CountBadge(count = chemistryCount, isSelected = selectedSubject == Subject.CHEMISTRY, activeColor = ChemistryOrange)
                            }
                        }
                    },
                    modifier = Modifier.testTag("filter_subject_tab_chemistry")
                )
            }

            // Optional Category Chips (Notes, Past Papers, Sample Papers, Important Questions)
            if (showCategoryRow && onCategorySelected != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text("All Types", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyDark,
                            selectedLabelColor = PureWhite
                        ),
                        modifier = Modifier.testTag("filter_category_all")
                    )

                    MaterialCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelected(cat) },
                            label = { Text(cat.shortName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealAccent,
                                selectedLabelColor = PureWhite
                            ),
                            modifier = Modifier.testTag("filter_category_${cat.name.lowercase()}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountBadge(
    count: Int,
    isSelected: Boolean,
    activeColor: Color
) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) activeColor.copy(alpha = 0.15f) else Slate100,
        modifier = Modifier.size(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) activeColor else Slate600
                )
            )
        }
    }
}
