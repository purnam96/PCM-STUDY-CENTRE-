package com.example.data

import com.example.model.AcademicClass
import com.example.model.Flashcard
import com.example.model.StudyMaterial
import com.example.model.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service that generates Digital Flashcards from BOSEM syllabus notes.
 * Practice Quizzes are generated via Firebase AI Logic in [com.example.data.repository.GeminiQuizRepository].
 */
object GeminiStudyService {

    /**
     * Generates digital flashcards with front prompt and back detailed concept/formula.
     */
    suspend fun generateFlashcards(
        subject: Subject,
        academicClass: AcademicClass,
        materials: List<StudyMaterial>
    ): List<Flashcard> = withContext(Dispatchers.Default) {
        val flashcards = mutableListOf<Flashcard>()

        if (subject == Subject.PHYSICS) {
            flashcards.add(
                Flashcard(
                    id = "fc-p-1",
                    subject = Subject.PHYSICS,
                    chapterName = "Light - Reflection & Refraction",
                    frontTitle = "Mirror Formula",
                    frontSubtitle = "Spherical Mirrors (Concave & Convex)",
                    backContent = "1 / f = 1 / v + 1 / u",
                    keyPoints = listOf(
                        "f = focal length",
                        "v = image distance from pole",
                        "u = object distance (always negative by Cartesian convention)"
                    )
                )
            )
            flashcards.add(
                Flashcard(
                    id = "fc-p-2",
                    subject = Subject.PHYSICS,
                    chapterName = "Light - Reflection & Refraction",
                    frontTitle = "Lens Formula & Magnification",
                    frontSubtitle = "Refraction through thin lenses",
                    backContent = "1 / f = 1 / v - 1 / u\nMagnification m = v / u = h' / h",
                    keyPoints = listOf(
                        "Convex lens focal length f is positive (+)",
                        "Concave lens focal length f is negative (-)",
                        "Power of Lens P = 1 / f (in meters), measured in Dioptre (D)"
                    )
                )
            )
            flashcards.add(
                Flashcard(
                    id = "fc-p-3",
                    subject = Subject.PHYSICS,
                    chapterName = "Electricity",
                    frontTitle = "Ohm's Law",
                    frontSubtitle = "Potential Difference & Current",
                    backContent = "V = I * R\nResistance R = ρ * (l / A)",
                    keyPoints = listOf(
                        "V = Potential difference in Volts",
                        "I = Current in Amperes",
                        "ρ = Resistivity (property of material, unit: Ω·m)",
                        "Series: R_total = R1 + R2 + R3",
                        "Parallel: 1/R_total = 1/R1 + 1/R2 + 1/R3"
                    )
                )
            )
            flashcards.add(
                Flashcard(
                    id = "fc-p-4",
                    subject = Subject.PHYSICS,
                    chapterName = "Magnetic Effects of Electric Current",
                    frontTitle = "Right Hand Thumb Rule",
                    frontSubtitle = "Direction of Magnetic Field around straight wire",
                    backContent = "Imagine holding a straight current-carrying conductor in your right hand with the thumb pointing in the direction of current; your curled fingers show the direction of magnetic field lines.",
                    keyPoints = listOf(
                        "Thumb = Direction of Electric Current",
                        "Encircling fingers = Direction of Concentric Magnetic Field Lines",
                        "Field strength increases closer to wire"
                    )
                )
            )
        } else {
            // CHEMISTRY
            flashcards.add(
                Flashcard(
                    id = "fc-c-1",
                    subject = Subject.CHEMISTRY,
                    chapterName = "Chemical Reactions",
                    frontTitle = "Oxidation vs Reduction (Redox)",
                    frontSubtitle = "Electron & Oxygen transfer definition",
                    backContent = "Oxidation: Gain of oxygen or loss of electrons (OIL)\nReduction: Loss of oxygen or gain of electrons (RIG)",
                    keyPoints = listOf(
                        "Oxidizing agent gives oxygen or removes electrons",
                        "Reducing agent removes oxygen or gives electrons",
                        "Redox reactions occur simultaneously"
                    )
                )
            )
            flashcards.add(
                Flashcard(
                    id = "fc-c-2",
                    subject = Subject.CHEMISTRY,
                    chapterName = "Acids, Bases and Salts",
                    frontTitle = "Bleaching Powder & Plaster of Paris",
                    frontSubtitle = "Important industrial chemical salts",
                    backContent = "Bleaching Powder: CaOCl2 (formed by Ca(OH)2 + Cl2)\nPlaster of Paris: CaSO4 · (1/2)H2O (Calcium sulphate hemihydrate)",
                    keyPoints = listOf(
                        "Gypsum = CaSO4 · 2H2O",
                        "Heat Gypsum to 373 K to yield Plaster of Paris",
                        "Baking Soda = NaHCO3, Washing Soda = Na2CO3 · 10H2O"
                    )
                )
            )
            flashcards.add(
                Flashcard(
                    id = "fc-c-3",
                    subject = Subject.CHEMISTRY,
                    chapterName = "Metals and Non-Metals",
                    frontTitle = "Reactivity Series of Metals",
                    frontSubtitle = "Mnemonic from highest to lowest reactivity",
                    backContent = "K > Na > Ca > Mg > Al > Zn > Fe > Pb > [H] > Cu > Hg > Ag > Au",
                    keyPoints = listOf(
                        "Potassium (K) and Sodium (Na) are most reactive; stored under kerosene",
                        "Gold (Au) and Platinum (Pt) are least reactive (native state)",
                        "More reactive metals displace less reactive metals from salt solutions"
                    )
                )
            )
            flashcards.add(
                Flashcard(
                    id = "fc-c-4",
                    subject = Subject.CHEMISTRY,
                    chapterName = "Carbon and its Compounds",
                    frontTitle = "Functional Groups in Carbon Chemistry",
                    frontSubtitle = "Homologous series identifier",
                    backContent = "Alcohol: -OH\nAldehyde: -CHO\nKetone: >C=O\nCarboxylic Acid: -COOH\nEster: -COO-",
                    keyPoints = listOf(
                        "Saturated: Alkane (single bond C-C)",
                        "Unsaturated: Alkene (C=C), Alkyne (C≡C)",
                        "Ethanol C2H5OH, Ethanoic Acid CH3COOH"
                    )
                )
            )
        }

        // Add dynamic flashcards from study materials key takeaways
        materials.filter { it.subject == subject }.take(4).forEachIndexed { index, mat ->
            if (mat.keyTakeaways.isNotEmpty()) {
                flashcards.add(
                    Flashcard(
                        id = "fc-dyn-${mat.id}-$index",
                        subject = mat.subject,
                        chapterName = mat.chapterName,
                        frontTitle = mat.chapterName,
                        frontSubtitle = mat.title,
                        backContent = mat.contentSummary.ifBlank { mat.description },
                        keyPoints = mat.keyTakeaways.take(3)
                    )
                )
            }
        }

        flashcards
    }
}
