package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.ChemistryOrange
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
import com.example.viewmodel.MainViewModel

import com.example.ui.components.StudyFilterSection

data class ChapterInfo(
    val number: Int,
    val name: String,
    val subTopics: String,
    val keyFormulas: String,
    val difficulty: String = "High Weightage"
)

@Composable
fun ClassesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val allMaterials by viewModel.materials.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()

    var activeSubjectTab by remember(selectedSubject) { mutableStateOf(selectedSubject ?: Subject.PHYSICS) }
    var expandedChapterNumber by remember { mutableStateOf<Int?>(1) }

    val classMaterials = remember(allMaterials, selectedClass) {
        allMaterials.filter { it.academicClass == selectedClass }
    }
    val physicsCount = remember(classMaterials) { classMaterials.count { it.subject == Subject.PHYSICS } }
    val chemistryCount = remember(classMaterials) { classMaterials.count { it.subject == Subject.CHEMISTRY } }
    val totalCount = remember(classMaterials) { classMaterials.size }

    val effectiveSubject = selectedSubject ?: activeSubjectTab
    val chapters = remember(selectedClass, effectiveSubject) {
        getChaptersFor(selectedClass, effectiveSubject)
    }

    val filteredChapters = remember(chapters, searchQuery, allMaterials, selectedClass, effectiveSubject) {
        if (searchQuery.isBlank()) chapters else {
            chapters.filter { ch ->
                ch.name.contains(searchQuery, ignoreCase = true) ||
                        ch.subTopics.contains(searchQuery, ignoreCase = true) ||
                        ch.keyFormulas.contains(searchQuery, ignoreCase = true) ||
                        allMaterials.any { mat ->
                            mat.academicClass == selectedClass &&
                                    mat.subject == effectiveSubject &&
                                    mat.chapterName.contains(ch.name, ignoreCase = true) &&
                                    (mat.title.contains(searchQuery, ignoreCase = true) ||
                                            mat.chapterName.contains(searchQuery, ignoreCase = true) ||
                                            mat.description.contains(searchQuery, ignoreCase = true) ||
                                            mat.tags.any { it.contains(searchQuery, ignoreCase = true) })
                        }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Class & Subject Filtering Component with Real-Time Search Bar
        StudyFilterSection(
            selectedClass = selectedClass,
            onClassSelected = { cls -> viewModel.setClass(cls) },
            selectedSubject = selectedSubject,
            onSubjectSelected = { subj ->
                viewModel.setSubject(subj)
                if (subj != null) {
                    activeSubjectTab = subj
                }
            },
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            searchPlaceholder = "Search materials by title or chapter...",
            totalCount = totalCount,
            physicsCount = physicsCount,
            chemistryCount = chemistryCount
        )

        // Syllabus Content Header
        Surface(
            color = Slate100,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${selectedClass.displayName} • ${effectiveSubject.displayName} Syllabus",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = if (searchQuery.isNotBlank()) "Showing results matching \"$searchQuery\"" else "Board of Secondary Education, Manipur Aligned",
                        style = MaterialTheme.typography.bodySmall.copy(color = if (searchQuery.isNotBlank()) VioletSecondary else Slate500)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (effectiveSubject == Subject.PHYSICS) PhysicsBlue.copy(alpha = 0.12f) else ChemistryOrange.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${filteredChapters.size} Chapters",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (effectiveSubject == Subject.PHYSICS) PhysicsBlue else ChemistryOrange,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (filteredChapters.isEmpty()) {
            EmptyStateView(
                icon = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Quiz,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = "No Chapters or Materials Found",
                message = "No chapters or materials matched \"$searchQuery\". Try checking the spelling or clearing your search.",
                actionButton = {
                    androidx.compose.material3.FilledTonalButton(
                        onClick = { viewModel.setSearchQuery("") },
                        modifier = Modifier.testTag("reset_classes_search_button")
                    ) {
                        Text("Clear Search")
                    }
                },
                modifier = Modifier.padding(24.dp)
            )
        } else {
            // Chapters List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredChapters) { chapter ->
                    val isExpanded = if (searchQuery.isNotBlank()) true else (expandedChapterNumber == chapter.number)
                    val matchingMaterials = allMaterials.filter {
                        it.academicClass == selectedClass &&
                                it.subject == effectiveSubject &&
                                it.chapterName.contains(chapter.name, ignoreCase = true) &&
                                (searchQuery.isBlank() ||
                                        it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.chapterName.contains(searchQuery, ignoreCase = true) ||
                                        it.description.contains(searchQuery, ignoreCase = true) ||
                                        it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) })
                    }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_card_${chapter.number}")
                        .clickable {
                            expandedChapterNumber = if (isExpanded) null else chapter.number
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (effectiveSubject == Subject.PHYSICS) PhysicsBlue else ChemistryOrange,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${chapter.number}",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Text(
                                    text = chapter.subTopics,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                                    maxLines = 1
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Expanded Section: Details & Related Materials
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate100,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Functions,
                                            contentDescription = null,
                                            tint = TealAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Key Formulas / Concepts: ${chapter.keyFormulas}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate700,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = VioletSecondary.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.openImportantQuestions(effectiveSubject)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Quiz,
                                            contentDescription = null,
                                            tint = VioletSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Question Bank",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = VioletSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TealAccent.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val firstMat = matchingMaterials.firstOrNull()
                                            if (firstMat != null) {
                                                viewModel.openMaterial(firstMat)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = TealAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Revision Notes",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TealAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            if (matchingMaterials.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Available Chapter Documents:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate500,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                matchingMaterials.forEach { mat ->
                                    MaterialCard(
                                        material = mat,
                                        isBookmarked = bookmarks.contains(mat.id),
                                        isDownloaded = downloadedIds.contains(mat.id),
                                        downloadProgress = downloadProgress[mat.id],
                                        onView = { viewModel.openMaterial(mat) },
                                        onToggleBookmark = { viewModel.toggleBookmark(mat.id) },
                                        onDownload = { viewModel.downloadMaterial(mat) },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                }
            }
        }
    }
}
}
}

private fun getChaptersFor(academicClass: AcademicClass, subject: Subject): List<ChapterInfo> {
    return if (academicClass == AcademicClass.CLASS_10) {
        if (subject == Subject.PHYSICS) {
            listOf(
                ChapterInfo(1, "Light – Reflection and Refraction", "Mirror formula, Snell's law, lens formula, power of lens", "1/f = 1/v + 1/u, P = 1/f(m)"),
                ChapterInfo(2, "The Human Eye and the Colorful World", "Defects of vision, prism refraction, dispersion, rainbow", "Myopia & Hypermetropia corrections"),
                ChapterInfo(3, "Electricity", "Ohm's law, resistance, series & parallel circuits, heating effect", "V = IR, H = I²Rt, P = VI"),
                ChapterInfo(4, "Magnetic Effects of Electric Current", "Magnetic field lines, solenoid, Fleming's rules, motor & induction", "Right-hand thumb rule, F = BIL"),
                ChapterInfo(5, "Sources of Energy", "Conventional & renewable energy, solar, wind, biomass & nuclear", "Thermal vs Hydro plants")
            )
        } else {
            listOf(
                ChapterInfo(1, "Chemical Reactions and Equations", "Balancing chemical equations, combination, decomposition, redox", "3Fe + 4H2O -> Fe3O4 + 4H2"),
                ChapterInfo(2, "Acids, Bases and Salts", "pH scale, neutralization, chlor-alkali, bleaching powder, POP", "pH = -log[H+], CaOCl2, NaHCO3"),
                ChapterInfo(3, "Metals and Non-Metals", "Reactivity series, ionic bonding, metallurgy, roasting & calcination", "ZnCO3 --Heat-> ZnO + CO2"),
                ChapterInfo(4, "Carbon and Its Compounds", "Covalent bonds, homologous series, ethanol & ethanoic acid, soaps", "CH3COOH + C2H5OH -> CH3COOC2H5"),
                ChapterInfo(5, "Periodic Classification of Elements", "Mendeleev's periodic table, Modern periodic table & trends", "Valency & Atomic radius trends")
            )
        }
    } else {
        if (subject == Subject.PHYSICS) {
            listOf(
                ChapterInfo(1, "Motion", "Distance, displacement, velocity-time graph, equations of motion", "v = u + at, s = ut + ½at²"),
                ChapterInfo(2, "Force and Laws of Motion", "Inertia, Newton's 1st, 2nd, 3rd laws, momentum conservation", "F = ma, m1u1 + m2u2 = m1v1 + m2v2"),
                ChapterInfo(3, "Gravitation", "Universal gravitation, g on Earth, mass vs weight, Archimedes' law", "F = G(m1m2)/r², g = 9.8 m/s²"),
                ChapterInfo(4, "Work and Energy", "Work done, kinetic energy, potential energy, conservation of energy", "W = Fs, Ek = ½mv², Ep = mgh"),
                ChapterInfo(5, "Sound", "Longitudinal waves, pitch, loudness, echo, SONAR, ultrasound", "v = f * lambda, SONAR 2d = vt")
            )
        } else {
            listOf(
                ChapterInfo(1, "Matter in Our Surroundings", "States of matter, latent heat, evaporation, Kelvin scale", "K = °C + 273.15, Sublimation"),
                ChapterInfo(2, "Is Matter Around Us Pure?", "Mixtures, solutions, colloids, chromatography, distillation", "Mass percentage = (solute/solution)*100"),
                ChapterInfo(3, "Atoms and Molecules", "Dalton's atomic theory, molecular mass, mole concept", "1 mole = 6.022 x 10²³ particles"),
                ChapterInfo(4, "Structure of the Atom", "Thomson, Rutherford, Bohr models, isotopes and isobars", "Electrons, protons, neutrons, 2n² rule")
            )
        }
    }
}
