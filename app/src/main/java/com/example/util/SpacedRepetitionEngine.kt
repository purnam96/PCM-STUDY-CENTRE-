package com.example.util

import com.example.model.AcademicClass
import com.example.model.Flashcard
import com.example.model.LeitnerBox
import com.example.model.SrsRating
import com.example.model.Subject
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Spaced Repetition System (SRS) Engine implementing the Leitner 5-box system
 * and SuperMemo SM-2 interval expansion algorithm for physics and chemistry formulas.
 */
object SpacedRepetitionEngine {

    private const val ONE_DAY_MILLIS = 24L * 60 * 60 * 1000

    /**
     * Applies the spaced repetition algorithm to compute the updated card state
     * after a student reviews and grades their recall.
     */
    fun processReview(card: Flashcard, rating: SrsRating, now: Long = System.currentTimeMillis()): Flashcard {
        var box = card.boxLevel
        var repetitions = card.repetitions
        var intervalDays = card.intervalDays
        var easeFactor = card.easeFactor

        when (rating) {
            SrsRating.AGAIN -> {
                // Card forgotten: reset repetitions and demote to Box 1 for immediate review
                repetitions = 0
                box = 1
                intervalDays = 1
                easeFactor = (easeFactor - 0.2f).coerceAtLeast(1.3f)
            }
            SrsRating.HARD -> {
                // Recall was difficult: repetitions increase, box stays unchanged, gentle interval boost
                repetitions += 1
                intervalDays = when (repetitions) {
                    1 -> 1
                    2 -> 2
                    else -> (intervalDays * 1.2f).toInt().coerceAtLeast(intervalDays + 1)
                }
                easeFactor = (easeFactor - 0.15f).coerceAtLeast(1.3f)
            }
            SrsRating.GOOD -> {
                // Successful recall: promote to next Leitner Box, interval scales with ease factor
                repetitions += 1
                box = (box + 1).coerceAtMost(5)
                intervalDays = when (repetitions) {
                    1 -> 1
                    2 -> 3
                    3 -> 7
                    else -> (intervalDays * easeFactor).toInt().coerceAtLeast(intervalDays + 2)
                }
            }
            SrsRating.EASY -> {
                // Effortless recall: boost +2 Leitner Boxes, expand ease factor
                repetitions += 1
                box = (box + 2).coerceAtMost(5)
                intervalDays = when (repetitions) {
                    1 -> 3
                    2 -> 7
                    3 -> 14
                    else -> (intervalDays * easeFactor * 1.35f).toInt().coerceAtLeast(intervalDays + 4)
                }
                easeFactor = (easeFactor + 0.15f).coerceAtMost(3.0f)
            }
        }

        // Clamp interval to reasonable bounds (max 90 days for school term)
        intervalDays = intervalDays.coerceIn(1, 90)
        val nextReviewEpoch = now + (intervalDays * ONE_DAY_MILLIS)
        val isMastered = box >= 5

        return card.copy(
            boxLevel = box,
            repetitions = repetitions,
            intervalDays = intervalDays,
            easeFactor = easeFactor,
            nextReviewEpochMillis = nextReviewEpoch,
            lastReviewedEpochMillis = now,
            isMastered = isMastered
        )
    }

    /**
     * Human-friendly review status description (e.g. "Due today", "Review in 3 days").
     */
    fun formatNextReviewText(nextReviewEpochMillis: Long, now: Long = System.currentTimeMillis()): String {
        val diffMillis = nextReviewEpochMillis - now
        if (diffMillis <= 0) {
            return "Due for review today"
        }
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis).toInt()

        return when {
            diffDays >= 2 -> "Review in $diffDays days"
            diffDays == 1 -> "Review tomorrow"
            diffHours >= 1 -> "Review in $diffHours hrs"
            else -> "Review soon"
        }
    }

    /**
     * Preview the interval that each rating button will assign.
     */
    fun previewIntervalText(card: Flashcard, rating: SrsRating): String {
        val dummy = processReview(card, rating)
        val days = dummy.intervalDays
        return if (days == 1) "1 day" else "$days days"
    }

    /**
     * Summary stats for a deck of flashcards.
     */
    data class DeckStats(
        val totalCount: Int,
        val dueCount: Int,
        val learningCount: Int,  // Box 1-2
        val reviewCount: Int,    // Box 3-4
        val masteredCount: Int,  // Box 5
        val averageEaseFactor: Float
    )

    fun calculateDeckStats(cards: List<Flashcard>, now: Long = System.currentTimeMillis()): DeckStats {
        if (cards.isEmpty()) {
            return DeckStats(0, 0, 0, 0, 0, 2.5f)
        }
        val due = cards.count { it.nextReviewEpochMillis <= now }
        val learning = cards.count { it.boxLevel in 1..2 }
        val review = cards.count { it.boxLevel in 3..4 }
        val mastered = cards.count { it.boxLevel >= 5 || it.isMastered }
        val avgEase = cards.map { it.easeFactor }.average().toFloat()

        return DeckStats(
            totalCount = cards.size,
            dueCount = due,
            learningCount = learning,
            reviewCount = review,
            masteredCount = mastered,
            averageEaseFactor = avgEase
        )
    }

    /**
     * Returns a curated initial set of high-yield BOSEM Class 9 & 10 Physics & Chemistry formulas.
     */
    fun getInitialCurriculumFlashcards(): List<Flashcard> {
        val list = mutableListOf<Flashcard>()
        val now = System.currentTimeMillis()

        // ---------------- CLASS 10 PHYSICS ----------------
        list.add(
            Flashcard(
                id = "fc-c10-p-1",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Light - Reflection & Refraction",
                frontTitle = "Mirror Formula",
                frontSubtitle = "Spherical Mirrors (Concave & Convex)",
                backContent = "1 / f = 1 / v + 1 / u",
                keyPoints = listOf(
                    "f = focal length (+ for convex, - for concave)",
                    "v = image distance from pole",
                    "u = object distance (always negative in Cartesian convention)",
                    "Magnification: m = -v / u = h' / h"
                ),
                boxLevel = 1,
                intervalDays = 1,
                nextReviewEpochMillis = now - 1000 // due today
            )
        )

        list.add(
            Flashcard(
                id = "fc-c10-p-2",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Light - Reflection & Refraction",
                frontTitle = "Lens Formula & Power",
                frontSubtitle = "Refraction through thin spherical lenses",
                backContent = "1 / f = 1 / v - 1 / u\nLens Power: P = 1 / f (in metres)",
                keyPoints = listOf(
                    "Convex lens focal length f is positive (+)",
                    "Concave lens focal length f is negative (-)",
                    "Unit of power is Dioptre (1 D = 1 m⁻¹)",
                    "Magnification: m = +v / u = h' / h"
                ),
                boxLevel = 2,
                intervalDays = 3,
                nextReviewEpochMillis = now + (2L * ONE_DAY_MILLIS)
            )
        )

        list.add(
            Flashcard(
                id = "fc-c10-p-3",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Electricity",
                frontTitle = "Ohm's Law & Resistance",
                frontSubtitle = "Potential Difference, Current & Resistivity",
                backContent = "V = I · R\nResistance: R = ρ · (l / A)",
                keyPoints = listOf(
                    "V = Potential difference in Volts (V = W / Q)",
                    "I = Electric current in Amperes (I = Q / t)",
                    "ρ = Resistivity in Ω·m (depends only on material & temp)",
                    "Series: Rs = R1 + R2 + R3",
                    "Parallel: 1/Rp = 1/R1 + 1/R2 + 1/R3"
                ),
                boxLevel = 3,
                intervalDays = 7,
                nextReviewEpochMillis = now + (4L * ONE_DAY_MILLIS)
            )
        )

        list.add(
            Flashcard(
                id = "fc-c10-p-4",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Electricity",
                frontTitle = "Joule's Law of Heating & Electric Power",
                frontSubtitle = "Heat generated in resistors and commercial energy unit",
                backContent = "H = I² · R · t\nPower: P = V · I = I² · R = V² / R",
                keyPoints = listOf(
                    "Heat produced is proportional to square of current",
                    "Commercial unit of energy: 1 kWh = 3.6 × 10⁶ Joules",
                    "Fuse wire operates on heating effect (low melting point)"
                ),
                boxLevel = 1,
                intervalDays = 1,
                nextReviewEpochMillis = now - 5000 // due today
            )
        )

        list.add(
            Flashcard(
                id = "fc-c10-p-5",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Magnetic Effects of Electric Current",
                frontTitle = "Fleming's Left & Right Hand Rules",
                frontSubtitle = "Motor Effect vs Electromagnetic Induction (Generator)",
                backContent = "Left Hand Rule (Motor): Father (Force/Thumb), Mother (Magnetic Field/Forefinger), Child (Current/Middle).\nRight Hand Rule (Generator/Induction): Thumb = Motion, Forefinger = Field, Middle = Induced Current.",
                keyPoints = listOf(
                    "Left hand rule determines force on current in magnetic field",
                    "Right hand rule determines direction of induced current",
                    "All three fingers must be held mutually perpendicular"
                ),
                boxLevel = 2,
                intervalDays = 3,
                nextReviewEpochMillis = now + (1L * ONE_DAY_MILLIS)
            )
        )

        // ---------------- CLASS 10 CHEMISTRY ----------------
        list.add(
            Flashcard(
                id = "fc-c10-c-1",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Chemical Reactions & Equations",
                frontTitle = "Redox Reactions & Chemical Types",
                frontSubtitle = "Oxidation, Reduction, Oxidizing & Reducing Agents",
                backContent = "Oxidation: Gain of oxygen / loss of electrons (OIL).\nReduction: Loss of oxygen / gain of electrons (RIG).",
                keyPoints = listOf(
                    "Oxidizing agent gives oxygen or removes electrons (gets reduced)",
                    "Reducing agent removes oxygen or gives electrons (gets oxidized)",
                    "Example: CuO + H₂ → Cu + H₂O (CuO is reduced, H₂ is oxidized)",
                    "Precipitation: Double displacement forming insoluble salt"
                ),
                boxLevel = 1,
                intervalDays = 1,
                nextReviewEpochMillis = now - 2000 // due today
            )
        )

        list.add(
            Flashcard(
                id = "fc-c10-c-2",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Acids, Bases and Salts",
                frontTitle = "Important Salts & Chemical Formulas",
                frontSubtitle = "Bleaching Powder, Plaster of Paris, Baking Soda & Washing Soda",
                backContent = "• Bleaching Powder: CaOCl₂\n• Plaster of Paris: CaSO₄ · ½H₂O\n• Baking Soda: NaHCO₃\n• Washing Soda: Na₂CO₃ · 10H₂O\n• Gypsum: CaSO₄ · 2H₂O",
                keyPoints = listOf(
                    "Bleaching powder: Ca(OH)₂ + Cl₂ → CaOCl₂ + H₂O",
                    "POP preparation: Gypsum heated at 373 K yields POP",
                    "Baking powder is baking soda + mild edible tartaric acid"
                ),
                boxLevel = 2,
                intervalDays = 3,
                nextReviewEpochMillis = now + (2L * ONE_DAY_MILLIS)
            )
        )

        list.add(
            Flashcard(
                id = "fc-c10-c-3",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Metals and Non-Metals",
                frontTitle = "Reactivity Series of Metals & Ore Extraction",
                frontSubtitle = "Displacement series and calcination vs roasting",
                backContent = "K > Na > Ca > Mg > Al > Zn > Fe > Pb > [H] > Cu > Hg > Ag > Au",
                keyPoints = listOf(
                    "Top metals (K, Na, Ca, Mg, Al): Extracted by electrolytic reduction",
                    "Middle metals (Zn, Fe, Pb): Roasting (sulphides + O₂) or Calcination (carbonates - O₂)",
                    "Thermite reaction: Fe₂O₃ + 2Al → 2Fe + Al₂O₃ + Heat (used to join railway tracks)"
                ),
                boxLevel = 3,
                intervalDays = 7,
                nextReviewEpochMillis = now + (5L * ONE_DAY_MILLIS)
            )
        )

        list.add(
            Flashcard(
                id = "fc-c10-c-4",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Carbon and its Compounds",
                frontTitle = "Carbon Functional Groups & Esterification",
                frontSubtitle = "Homologous Series, Saponification & Cleansing Action",
                backContent = "• Alcohol: -OH\n• Aldehyde: -CHO\n• Ketone: >C=O\n• Carboxylic Acid: -COOH\nEsterification: Acid + Alcohol ⎯Conc.H₂SO₄⎯→ Ester (Fruity smell) + H₂O",
                keyPoints = listOf(
                    "Saponification: Ester + NaOH → Soap (sodium carboxylate) + Glycerol",
                    "Soap molecule has hydrophilic ionic head and hydrophobic hydrocarbon tail",
                    "Unsaturated hydrocarbons decolorize bromine water (Addition reaction)"
                ),
                boxLevel = 1,
                intervalDays = 1,
                nextReviewEpochMillis = now - 8000 // due today
            )
        )

        // ---------------- CLASS 9 PHYSICS ----------------
        list.add(
            Flashcard(
                id = "fc-c9-p-1",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_9,
                chapterName = "Motion",
                frontTitle = "Three Equations of Uniform Motion",
                frontSubtitle = "Relations between u, v, a, t, and s",
                backContent = "1) v = u + a·t\n2) s = u·t + ½a·t²\n3) v² = u² + 2a·s",
                keyPoints = listOf(
                    "u = initial velocity, v = final velocity",
                    "a = uniform acceleration, t = time, s = distance/displacement",
                    "For body starting from rest: u = 0; coming to stop: v = 0",
                    "Slope of distance-time graph = Speed; Slope of v-t graph = Acceleration"
                ),
                boxLevel = 2,
                intervalDays = 3,
                nextReviewEpochMillis = now + (1L * ONE_DAY_MILLIS)
            )
        )

        list.add(
            Flashcard(
                id = "fc-c9-p-2",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_9,
                chapterName = "Force and Laws of Motion",
                frontTitle = "Newton's Second Law & Momentum",
                frontSubtitle = "Force formula and law of conservation of momentum",
                backContent = "Force: F = m · a = (p₂ - p₁) / t\nMomentum: p = m · v",
                keyPoints = listOf(
                    "SI unit of Force is Newton (1 N = 1 kg·m/s²)",
                    "Law of conservation: Total momentum before collision = Total momentum after collision",
                    "m₁u₁ + m₂u₂ = m₁v₁ + m₂v₂ (in isolated system)",
                    "Third law: Action and reaction are equal and opposite, acting on different bodies"
                ),
                boxLevel = 1,
                intervalDays = 1,
                nextReviewEpochMillis = now - 3000
            )
        )

        list.add(
            Flashcard(
                id = "fc-c9-p-3",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_9,
                chapterName = "Gravitation",
                frontTitle = "Universal Law of Gravitation & Weight",
                frontSubtitle = "Gravitational constant G vs acceleration g",
                backContent = "F = G · (m₁ · m₂) / d²\nWeight: W = m · g",
                keyPoints = listOf(
                    "Universal constant G = 6.67 × 10⁻¹¹ N·m²/kg² (constant everywhere)",
                    "Acceleration due to gravity on Earth g ≈ 9.8 m/s²",
                    "g on Moon is 1/6th of g on Earth (W_moon = 1/6 W_earth)",
                    "Mass is constant, weight varies with location"
                ),
                boxLevel = 3,
                intervalDays = 7,
                nextReviewEpochMillis = now + (6L * ONE_DAY_MILLIS)
            )
        )

        // ---------------- CLASS 9 CHEMISTRY ----------------
        list.add(
            Flashcard(
                id = "fc-c9-c-1",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_9,
                chapterName = "Matter in Our Surroundings",
                frontTitle = "Latent Heat & Factors Affecting Evaporation",
                frontSubtitle = "Latent heat of fusion vs vaporization, cooling effect",
                backContent = "Evaporation causes cooling.\nFactors increasing evaporation: Surface area ↑, Temperature ↑, Wind speed ↑, Humidity ↓.",
                keyPoints = listOf(
                    "Latent heat of fusion: Heat to convert 1 kg solid to liquid at melting point",
                    "Latent heat of vaporization: Heat to convert 1 kg liquid to gas at boiling point",
                    "Steam causes more severe burns than boiling water due to latent heat",
                    "Sublimation: Solid converts directly into gas (Camphor, Ammonium Chloride)"
                ),
                boxLevel = 1,
                intervalDays = 1,
                nextReviewEpochMillis = now - 4000
            )
        )

        list.add(
            Flashcard(
                id = "fc-c9-c-2",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_9,
                chapterName = "Atoms and Molecules",
                frontTitle = "Mole Concept & Chemical Formula Rules",
                frontSubtitle = "Avogadro's number and criss-cross valency method",
                backContent = "1 Mole = 6.022 × 10²³ particles (NA)\nNumber of moles n = Given mass (m) / Molar mass (M)",
                keyPoints = listOf(
                    "Avogadro's constant NA = 6.022 × 10²³ entities",
                    "Law of conservation of mass: Mass can neither be created nor destroyed",
                    "Law of constant proportions: A chemical compound always contains elements in fixed ratio by mass",
                    "Aluminum Oxide formula: Al (valency 3) and O (valency 2) → Al₂O₃"
                ),
                boxLevel = 2,
                intervalDays = 3,
                nextReviewEpochMillis = now + (2L * ONE_DAY_MILLIS)
            )
        )

        return list
    }
}
