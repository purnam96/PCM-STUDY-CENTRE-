package com.example.util

import com.example.model.AcademicClass
import com.example.model.DailyStudyRecord
import com.example.model.FocusSession
import com.example.model.ModuleProgress
import com.example.model.QuizAttempt
import com.example.model.Subject

/**
 * Engine calculating module completion rates, quiz performance trends,
 * and generating the Recharts-powered HTML dashboard for Physics and Chemistry.
 */
object ModuleProgressEngine {

    /**
     * Standard Class 10 BOSEM syllabus modules.
     */
    private val class10Modules = listOf(
        // Physics Modules
        ModuleProgress(
            id = "c10-phy-mod-1",
            chapterNumber = 1,
            chapterName = "Light – Reflection & Refraction",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 90,
            quizScoreAveragePercent = 88,
            quizzesAttempted = 6,
            studyMinutes = 185,
            masteryLevel = "Mastered",
            keyConcept = "Mirror formula: 1/f = 1/v + 1/u & Snell's Law",
            totalTopicsCount = 5,
            completedTopicsCount = 5
        ),
        ModuleProgress(
            id = "c10-phy-mod-2",
            chapterNumber = 2,
            chapterName = "Human Eye & Colourful World",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 80,
            quizScoreAveragePercent = 84,
            quizzesAttempted = 4,
            studyMinutes = 120,
            masteryLevel = "Proficient",
            keyConcept = "Prism dispersion, atmospheric refraction & Rayleigh scattering",
            totalTopicsCount = 4,
            completedTopicsCount = 3
        ),
        ModuleProgress(
            id = "c10-phy-mod-3",
            chapterNumber = 3,
            chapterName = "Electricity & Circuits",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 75,
            quizScoreAveragePercent = 78,
            quizzesAttempted = 8,
            studyMinutes = 240,
            masteryLevel = "In Progress",
            keyConcept = "Ohm's Law: V = IR, Joule heating H = I²Rt",
            totalTopicsCount = 6,
            completedTopicsCount = 4
        ),
        ModuleProgress(
            id = "c10-phy-mod-4",
            chapterNumber = 4,
            chapterName = "Magnetic Effects of Electric Current",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 65,
            quizScoreAveragePercent = 82,
            quizzesAttempted = 4,
            studyMinutes = 110,
            masteryLevel = "In Progress",
            keyConcept = "Right-hand thumb rule, Fleming's left-hand rule & solenoid",
            totalTopicsCount = 5,
            completedTopicsCount = 3
        ),
        ModuleProgress(
            id = "c10-phy-mod-5",
            chapterNumber = 5,
            chapterName = "Sources of Energy",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 85,
            quizScoreAveragePercent = 90,
            quizzesAttempted = 3,
            studyMinutes = 95,
            masteryLevel = "Mastered",
            keyConcept = "Solar cells, biomass, nuclear fission & geothermal",
            totalTopicsCount = 4,
            completedTopicsCount = 4
        ),

        // Chemistry Modules
        ModuleProgress(
            id = "c10-chm-mod-1",
            chapterNumber = 1,
            chapterName = "Chemical Reactions & Equations",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 95,
            quizScoreAveragePercent = 94,
            quizzesAttempted = 7,
            studyMinutes = 190,
            masteryLevel = "Mastered",
            keyConcept = "Balancing equations, redox reactions & rancidity",
            totalTopicsCount = 5,
            completedTopicsCount = 5
        ),
        ModuleProgress(
            id = "c10-chm-mod-2",
            chapterNumber = 2,
            chapterName = "Acids, Bases & Salts",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 88,
            quizScoreAveragePercent = 91,
            quizzesAttempted = 5,
            studyMinutes = 160,
            masteryLevel = "Mastered",
            keyConcept = "pH scale, chlor-alkali process, Plaster of Paris",
            totalTopicsCount = 6,
            completedTopicsCount = 5
        ),
        ModuleProgress(
            id = "c10-chm-mod-3",
            chapterNumber = 3,
            chapterName = "Metals & Non-Metals",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 78,
            quizScoreAveragePercent = 86,
            quizzesAttempted = 4,
            studyMinutes = 135,
            masteryLevel = "Proficient",
            keyConcept = "Reactivity series, ionic bonding & roasting/calcination",
            totalTopicsCount = 5,
            completedTopicsCount = 4
        ),
        ModuleProgress(
            id = "c10-chm-mod-4",
            chapterNumber = 4,
            chapterName = "Carbon & Its Compounds",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 70,
            quizScoreAveragePercent = 80,
            quizzesAttempted = 6,
            studyMinutes = 175,
            masteryLevel = "In Progress",
            keyConcept = "Covalent bonding, homologous series, functional groups",
            totalTopicsCount = 6,
            completedTopicsCount = 4
        ),
        ModuleProgress(
            id = "c10-chm-mod-5",
            chapterNumber = 5,
            chapterName = "Periodic Classification of Elements",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_10,
            completionRatePercent = 85,
            quizScoreAveragePercent = 89,
            quizzesAttempted = 4,
            studyMinutes = 115,
            masteryLevel = "Proficient",
            keyConcept = "Modern periodic law, periodic trends (valency, radius)",
            totalTopicsCount = 4,
            completedTopicsCount = 3
        )
    )

    /**
     * Standard Class 9 BOSEM syllabus modules.
     */
    private val class9Modules = listOf(
        // Physics Modules
        ModuleProgress(
            id = "c9-phy-mod-1",
            chapterNumber = 1,
            chapterName = "Motion",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 92,
            quizScoreAveragePercent = 90,
            quizzesAttempted = 5,
            studyMinutes = 160,
            masteryLevel = "Mastered",
            keyConcept = "Equations of motion: v = u + at, s = ut + ½at²",
            totalTopicsCount = 5,
            completedTopicsCount = 5
        ),
        ModuleProgress(
            id = "c9-phy-mod-2",
            chapterNumber = 2,
            chapterName = "Force & Laws of Motion",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 85,
            quizScoreAveragePercent = 86,
            quizzesAttempted = 4,
            studyMinutes = 140,
            masteryLevel = "Proficient",
            keyConcept = "Newton's 3 Laws & Conservation of Linear Momentum",
            totalTopicsCount = 4,
            completedTopicsCount = 3
        ),
        ModuleProgress(
            id = "c9-phy-mod-3",
            chapterNumber = 3,
            chapterName = "Gravitation",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 75,
            quizScoreAveragePercent = 80,
            quizzesAttempted = 4,
            studyMinutes = 130,
            masteryLevel = "In Progress",
            keyConcept = "Universal law: F = G(m1·m2)/d² & Archimedes' Principle",
            totalTopicsCount = 5,
            completedTopicsCount = 4
        ),
        ModuleProgress(
            id = "c9-phy-mod-4",
            chapterNumber = 4,
            chapterName = "Work & Energy",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 82,
            quizScoreAveragePercent = 88,
            quizzesAttempted = 3,
            studyMinutes = 110,
            masteryLevel = "Proficient",
            keyConcept = "Work W = F·s, Kinetic energy ½mv², Potential energy mgh",
            totalTopicsCount = 4,
            completedTopicsCount = 3
        ),
        ModuleProgress(
            id = "c9-phy-mod-5",
            chapterNumber = 5,
            chapterName = "Sound",
            subject = Subject.PHYSICS,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 70,
            quizScoreAveragePercent = 82,
            quizzesAttempted = 3,
            studyMinutes = 95,
            masteryLevel = "In Progress",
            keyConcept = "Wavelength, frequency, echo calculation & ultrasound",
            totalTopicsCount = 4,
            completedTopicsCount = 3
        ),

        // Chemistry Modules
        ModuleProgress(
            id = "c9-chm-mod-1",
            chapterNumber = 1,
            chapterName = "Matter in Our Surroundings",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 95,
            quizScoreAveragePercent = 92,
            quizzesAttempted = 4,
            studyMinutes = 130,
            masteryLevel = "Mastered",
            keyConcept = "States of matter, latent heat & factors affecting evaporation",
            totalTopicsCount = 4,
            completedTopicsCount = 4
        ),
        ModuleProgress(
            id = "c9-chm-mod-2",
            chapterNumber = 2,
            chapterName = "Is Matter Around Us Pure",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 85,
            quizScoreAveragePercent = 88,
            quizzesAttempted = 4,
            studyMinutes = 125,
            masteryLevel = "Proficient",
            keyConcept = "Solutions, colloids, suspensions & Tyndall effect",
            totalTopicsCount = 5,
            completedTopicsCount = 4
        ),
        ModuleProgress(
            id = "c9-chm-mod-3",
            chapterNumber = 3,
            chapterName = "Atoms & Molecules",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 78,
            quizScoreAveragePercent = 84,
            quizzesAttempted = 5,
            studyMinutes = 150,
            masteryLevel = "In Progress",
            keyConcept = "Mole concept, molecular mass & law of conservation of mass",
            totalTopicsCount = 5,
            completedTopicsCount = 4
        ),
        ModuleProgress(
            id = "c9-chm-mod-4",
            chapterNumber = 4,
            chapterName = "Structure of the Atom",
            subject = Subject.CHEMISTRY,
            academicClass = AcademicClass.CLASS_9,
            completionRatePercent = 80,
            quizScoreAveragePercent = 87,
            quizzesAttempted = 3,
            studyMinutes = 110,
            masteryLevel = "Proficient",
            keyConcept = "Bohr's atomic model, electronic configuration, valency & isotopes",
            totalTopicsCount = 4,
            completedTopicsCount = 3
        )
    )

    /**
     * Returns module progress records for the specified academic class,
     * dynamically updated with user focus sessions and quiz attempts.
     */
    fun getModuleProgress(
        academicClass: AcademicClass,
        focusSessions: List<FocusSession> = emptyList(),
        quizAttempts: List<QuizAttempt> = emptyList()
    ): List<ModuleProgress> {
        val baseList = if (academicClass == AcademicClass.CLASS_9) class9Modules else class10Modules

        return baseList.map { mod ->
            // Match sessions by subject and chapter name keyword
            val matchingSessions = focusSessions.filter {
                it.subject == mod.subject && (it.chapterName.contains(mod.chapterName, ignoreCase = true) || mod.chapterName.contains(it.chapterName, ignoreCase = true))
            }
            val extraMinutes = matchingSessions.sumOf { it.durationMinutes }

            val matchingQuizzes = quizAttempts.filter {
                it.subject == mod.subject && (it.chapterName.contains(mod.chapterName, ignoreCase = true) || mod.chapterName.contains(it.chapterName, ignoreCase = true))
            }
            val quizCount = mod.quizzesAttempted + matchingQuizzes.size
            val avgQuiz = if (matchingQuizzes.isNotEmpty()) {
                val combinedScores = matchingQuizzes.map { it.scorePercentage } + List(mod.quizzesAttempted) { mod.quizScoreAveragePercent }
                combinedScores.average().toInt()
            } else {
                mod.quizScoreAveragePercent
            }

            val totalStudyMins = mod.studyMinutes + extraMinutes
            val extraCompletionBonus = (matchingSessions.size * 3 + matchingQuizzes.size * 4).coerceAtMost(10)
            val finalCompletion = (mod.completionRatePercent + extraCompletionBonus).coerceIn(0, 100)

            val mastery = when {
                finalCompletion >= 90 && avgQuiz >= 88 -> "Mastered"
                finalCompletion >= 75 && avgQuiz >= 75 -> "Proficient"
                finalCompletion >= 50 -> "In Progress"
                else -> "Needs Review"
            }

            mod.copy(
                completionRatePercent = finalCompletion,
                quizScoreAveragePercent = avgQuiz,
                quizzesAttempted = quizCount,
                studyMinutes = totalStudyMins,
                masteryLevel = mastery
            )
        }
    }

    /**
     * Calculates summary metrics for Physics vs Chemistry modules.
     */
    fun calculateSummary(modules: List<ModuleProgress>): ProgressSummary {
        val physicsMods = modules.filter { it.subject == Subject.PHYSICS }
        val chemistryMods = modules.filter { it.subject == Subject.CHEMISTRY }

        val phyCompletion = if (physicsMods.isNotEmpty()) physicsMods.map { it.completionRatePercent }.average().toInt() else 0
        val chmCompletion = if (chemistryMods.isNotEmpty()) chemistryMods.map { it.completionRatePercent }.average().toInt() else 0

        val phyQuiz = if (physicsMods.isNotEmpty()) physicsMods.map { it.quizScoreAveragePercent }.average().toInt() else 0
        val chmQuiz = if (chemistryMods.isNotEmpty()) chemistryMods.map { it.quizScoreAveragePercent }.average().toInt() else 0

        val totalCompletion = if (modules.isNotEmpty()) modules.map { it.completionRatePercent }.average().toInt() else 0
        val totalQuiz = if (modules.isNotEmpty()) modules.map { it.quizScoreAveragePercent }.average().toInt() else 0
        val mastered = modules.count { it.masteryLevel == "Mastered" }

        return ProgressSummary(
            totalModulesCount = modules.size,
            physicsModulesCount = physicsMods.size,
            chemistryModulesCount = chemistryMods.size,
            physicsCompletionAvg = phyCompletion,
            chemistryCompletionAvg = chmCompletion,
            physicsQuizAvg = phyQuiz,
            chemistryQuizAvg = chmQuiz,
            overallCompletionAvg = totalCompletion,
            overallQuizAvg = totalQuiz,
            modulesMasteredCount = mastered
        )
    }

    /**
     * Generates a fully self-contained HTML/JS dashboard styled and patterned after Recharts,
     * featuring responsive SVG BarChart, LineChart/AreaChart, Tooltip, Legend, and Cartesian Grid.
     * Guaranteed to work offline with zero external network dependencies.
     */
    fun generateRechartsDashboardHtml(
        modules: List<ModuleProgress>,
        dailyRecords: List<DailyStudyRecord>,
        selectedSubject: Subject? = null
    ): String {
        val filtered = if (selectedSubject != null) modules.filter { it.subject == selectedSubject } else modules
        val summary = calculateSummary(modules)

        // Module data items
        val moduleJsonList = filtered.map { mod ->
            val shortName = mod.chapterName.split("–", "-", ":").first().trim().take(16)
            """{"name": "${mod.chapterNumber}. $shortName", "fullName": "${mod.chapterName}", "subject": "${mod.subject.displayName}", "completion": ${mod.completionRatePercent}, "quiz": ${mod.quizScoreAveragePercent}, "quizzes": ${mod.quizzesAttempted}, "minutes": ${mod.studyMinutes}, "mastery": "${mod.masteryLevel}", "concept": "${mod.keyConcept.replace("\"", "\\\"")}"}"""
        }.joinToString(",")

        // 30-day quiz trend data items
        val recentDays = dailyRecords.takeLast(14)
        val trendJsonList = recentDays.map { rec ->
            val score = rec.quizScorePercent ?: 75
            """{"day": "${rec.dayLabel}", "score": $score, "minutes": ${rec.minutesStudied}}"""
        }.joinToString(",")

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>BOSEM Recharts Learning Progress Dashboard</title>
    <style>
        :root {
            --navy-dark: #0A1628;
            --navy-surface: #132238;
            --navy-card: #1E293B;
            --physics-blue: #3B82F6;
            --physics-glow: rgba(59, 130, 246, 0.25);
            --chemistry-orange: #F97316;
            --chemistry-glow: rgba(249, 115, 22, 0.25);
            --cyan-accent: #06B6D4;
            --gold-accent: #F59E0B;
            --success-green: #10B981;
            --slate-300: #CBD5E1;
            --slate-400: #94A3B8;
            --slate-600: #475569;
            --pure-white: #FFFFFF;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            -webkit-tap-highlight-color: transparent;
        }

        body {
            background-color: var(--navy-dark);
            color: var(--pure-white);
            padding: 12px;
            overflow-x: hidden;
        }

        /* Top summary stat tiles */
        .recharts-summary-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 8px;
            margin-bottom: 14px;
        }

        .summary-card {
            background: var(--navy-card);
            border-radius: 12px;
            padding: 10px 8px;
            border: 1px solid rgba(255, 255, 255, 0.08);
            text-align: center;
        }

        .summary-card.physics { border-left: 3px solid var(--physics-blue); }
        .summary-card.chemistry { border-left: 3px solid var(--chemistry-orange); }
        .summary-card.total { border-left: 3px solid var(--cyan-accent); }
        .summary-card.quiz { border-left: 3px solid var(--gold-accent); }

        .summary-title {
            font-size: 10px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: var(--slate-400);
            margin-bottom: 4px;
        }

        .summary-val {
            font-size: 18px;
            font-weight: 800;
        }

        .summary-sub {
            font-size: 9px;
            color: var(--slate-400);
            margin-top: 2px;
        }

        /* Chart container */
        .recharts-panel {
            background: var(--navy-surface);
            border-radius: 16px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            padding: 14px;
            margin-bottom: 14px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        }

        .panel-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
        }

        .panel-title {
            font-size: 14px;
            font-weight: 700;
            color: var(--pure-white);
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .panel-subtitle {
            font-size: 10px;
            color: var(--slate-400);
            margin-top: 2px;
        }

        /* Chart Controls (Recharts View Selector) */
        .recharts-nav-tabs {
            display: flex;
            gap: 6px;
            background: rgba(0, 0, 0, 0.25);
            padding: 3px;
            border-radius: 8px;
        }

        .nav-btn {
            background: transparent;
            border: none;
            color: var(--slate-400);
            font-size: 10px;
            font-weight: 600;
            padding: 4px 8px;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .nav-btn.active {
            background: var(--physics-blue);
            color: var(--pure-white);
        }

        /* Responsive SVG container */
        .recharts-responsive-container {
            width: 100%;
            height: 230px;
            position: relative;
        }

        svg.recharts-surface {
            width: 100%;
            height: 100%;
            overflow: visible;
        }

        .recharts-cartesian-grid line {
            stroke: rgba(255, 255, 255, 0.08);
            stroke-dasharray: 3 3;
        }

        .recharts-cartesian-axis text {
            fill: var(--slate-400);
            font-size: 9px;
            font-weight: 500;
        }

        /* Bar elements */
        .recharts-bar-rect {
            cursor: pointer;
            transition: transform 0.2s, opacity 0.2s;
        }

        .recharts-bar-rect:hover, .recharts-bar-rect:active {
            opacity: 0.85;
            filter: drop-shadow(0 0 6px rgba(255, 255, 255, 0.4));
        }

        /* Legend */
        .recharts-legend {
            display: flex;
            justify-content: center;
            gap: 16px;
            margin-top: 10px;
            font-size: 11px;
            color: var(--slate-300);
        }

        .legend-item {
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .legend-indicator {
            width: 10px;
            height: 10px;
            border-radius: 2px;
        }

        /* Tooltip */
        .recharts-tooltip {
            position: absolute;
            background: rgba(15, 23, 42, 0.95);
            border: 1px solid rgba(255, 255, 255, 0.2);
            backdrop-filter: blur(8px);
            border-radius: 8px;
            padding: 8px 12px;
            font-size: 11px;
            color: var(--pure-white);
            pointer-events: none;
            display: none;
            z-index: 100;
            box-shadow: 0 4px 14px rgba(0, 0, 0, 0.5);
            max-width: 220px;
        }

        .tooltip-title {
            font-weight: 700;
            color: var(--cyan-accent);
            margin-bottom: 4px;
        }

        .tooltip-row {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            margin-top: 2px;
        }

        .tooltip-val { font-weight: 700; }

        /* Module progress detail list */
        .modules-list {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 8px;
        }

        .mod-item {
            background: rgba(30, 41, 59, 0.7);
            border-radius: 10px;
            padding: 10px;
            border: 1px solid rgba(255, 255, 255, 0.06);
            transition: transform 0.15s;
        }

        .mod-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 6px;
        }

        .mod-title {
            font-size: 11px;
            font-weight: 700;
            color: var(--pure-white);
            max-width: 80%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .mod-badge {
            font-size: 8px;
            font-weight: 700;
            padding: 2px 6px;
            border-radius: 4px;
        }

        .badge-mastered { background: rgba(16, 185, 129, 0.2); color: var(--success-green); }
        .badge-proficient { background: rgba(59, 130, 246, 0.2); color: var(--physics-blue); }
        .badge-progress { background: rgba(245, 158, 11, 0.2); color: var(--gold-accent); }

        .mod-bars {
            display: flex;
            flex-direction: column;
            gap: 4px;
            margin-top: 6px;
        }

        .bar-container {
            height: 5px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 3px;
            overflow: hidden;
            position: relative;
        }

        .bar-fill {
            height: 100%;
            border-radius: 3px;
        }

        .fill-physics { background: linear-gradient(90deg, #2563EB, #38BDF8); }
        .fill-chemistry { background: linear-gradient(90deg, #EA580C, #FBBF24); }

        .meta-row {
            display: flex;
            justify-content: space-between;
            font-size: 9px;
            color: var(--slate-400);
            margin-top: 3px;
        }
    </style>
</head>
<body>

    <!-- Top Key Metrics Row -->
    <div class="recharts-summary-grid">
        <div class="summary-card total">
            <div class="summary-title">Overall</div>
            <div class="summary-val" style="color: var(--cyan-accent);">${summary.overallCompletionAvg}%</div>
            <div class="summary-sub">Syllabus Done</div>
        </div>
        <div class="summary-card physics">
            <div class="summary-title">Physics</div>
            <div class="summary-val" style="color: var(--physics-blue);">${summary.physicsCompletionAvg}%</div>
            <div class="summary-sub">Quiz: ${summary.physicsQuizAvg}%</div>
        </div>
        <div class="summary-card chemistry">
            <div class="summary-title">Chemistry</div>
            <div class="summary-val" style="color: var(--chemistry-orange);">${summary.chemistryCompletionAvg}%</div>
            <div class="summary-sub">Quiz: ${summary.chemistryQuizAvg}%</div>
        </div>
        <div class="summary-card quiz">
            <div class="summary-title">Quiz Avg</div>
            <div class="summary-val" style="color: var(--gold-accent);">${summary.overallQuizAvg}%</div>
            <div class="summary-sub">Distinction</div>
        </div>
    </div>

    <!-- Main Recharts Visual Panel -->
    <div class="recharts-panel">
        <div class="panel-header">
            <div>
                <div class="panel-title">
                    <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:var(--cyan-accent);"></span>
                    Recharts Progress Engine
                </div>
                <div class="panel-subtitle" id="chart-sub-label">Completion rates & quiz accuracy by module</div>
            </div>

            <div class="recharts-nav-tabs">
                <button class="nav-btn active" id="btn-tab-bars" onclick="switchChartMode('bars')">Modules</button>
                <button class="nav-btn" id="btn-tab-trends" onclick="switchChartMode('trends')">Quiz Trends</button>
                <button class="nav-btn" id="btn-tab-both" onclick="switchChartMode('both')">Dual View</button>
            </div>
        </div>

        <!-- Responsive SVG Chart Surface -->
        <div class="recharts-responsive-container" id="recharts-container">
            <svg class="recharts-surface" id="recharts-svg"></svg>
            <div class="recharts-tooltip" id="recharts-tooltip"></div>
        </div>

        <!-- Dynamic Legend -->
        <div class="recharts-legend" id="recharts-legend">
            <div class="legend-item">
                <div class="legend-indicator" style="background: var(--physics-blue);"></div>
                <span>Physics Completion (%)</span>
            </div>
            <div class="legend-item">
                <div class="legend-indicator" style="background: var(--chemistry-orange);"></div>
                <span>Chemistry Completion (%)</span>
            </div>
            <div class="legend-item">
                <div class="legend-indicator" style="background: var(--gold-accent); border-radius: 50%;"></div>
                <span>Quiz Accuracy (%)</span>
            </div>
        </div>
    </div>

    <!-- Physics & Chemistry Module Grid with Completion Progress -->
    <div class="recharts-panel" style="padding: 12px;">
        <div class="panel-header" style="margin-bottom: 10px;">
            <div class="panel-title">
                Module Breakdown (${filtered.size} Modules)
            </div>
            <div style="font-size: 10px; color: var(--gold-accent); font-weight: 600;">
                BOSEM Syllabus Aligned
            </div>
        </div>

        <div class="modules-list" id="modules-list">
            <!-- Populated via JavaScript -->
        </div>
    </div>

    <script>
        const moduleData = [$moduleJsonList];
        const trendData = [$trendJsonList];
        let currentMode = 'bars'; // 'bars', 'trends', 'both'

        const container = document.getElementById('recharts-container');
        const svg = document.getElementById('recharts-svg');
        const tooltip = document.getElementById('recharts-tooltip');
        const subLabel = document.getElementById('chart-sub-label');

        function switchChartMode(mode) {
            currentMode = mode;
            document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
            if (mode === 'bars') document.getElementById('btn-tab-bars').classList.add('active');
            if (mode === 'trends') document.getElementById('btn-tab-trends').classList.add('active');
            if (mode === 'both') document.getElementById('btn-tab-both').classList.add('active');

            if (mode === 'bars') subLabel.innerText = "Module-by-module syllabus completion rates (%)";
            if (mode === 'trends') subLabel.innerText = "14-Day quiz score accuracy trends (%)";
            if (mode === 'both') subLabel.innerText = "Composite view: Completion vs Quiz Accuracy";

            renderChart();
        }

        function renderChart() {
            var w = container.clientWidth || 320;
            var h = container.clientHeight || 230;
            var padL = 36, padR = 16, padT = 20, padB = 40;
            var plotW = w - padL - padR;
            var plotH = h - padT - padB;

            var html = '';

            // Grid lines (0%, 25%, 50%, 75%, 100%)
            html += '<g class="recharts-cartesian-grid">';
            for (var i = 0; i <= 4; i++) {
                var gy = padT + (plotH * (4 - i) / 4);
                html += '<line x1="' + padL + '" y1="' + gy + '" x2="' + (w - padR) + '" y2="' + gy + '" />';
                html += '<text x="' + (padL - 6) + '" y="' + (gy + 3) + '" text-anchor="end" class="recharts-cartesian-axis">' + (i * 25) + '%</text>';
            }
            html += '</g>';

            if (currentMode === 'bars' || currentMode === 'both') {
                var count = moduleData.length;
                if (count > 0) {
                    var slotW = plotW / count;
                    var barW = Math.max(8, Math.min(22, slotW * 0.65));

                    moduleData.forEach(function(m, idx) {
                        var cx = padL + idx * slotW + (slotW / 2);
                        var isPhysics = m.subject === "Physics";
                        var color = isPhysics ? "var(--physics-blue)" : "var(--chemistry-orange)";

                        // Completion Bar
                        var barHeight = (m.completion / 100) * plotH;
                        var by = padT + (plotH - barHeight);

                        html += '<rect class="recharts-bar-rect" x="' + (cx - barW / 2) + '" y="' + by + '" width="' + barW + '" height="' + barHeight + '" rx="4" ry="4" fill="' + color + '" data-idx="' + idx + '" onmouseover="showModuleTooltip(event, ' + idx + ')" onmousemove="moveTooltip(event)" onmouseout="hideTooltip()" ontouchstart="showModuleTooltip(event, ' + idx + ')" />';

                        // Label on X Axis
                        var shortCode = m.name.split('.')[0] + (isPhysics ? 'P' : 'C');
                        html += '<text x="' + cx + '" y="' + (h - padB + 14) + '" text-anchor="middle" class="recharts-cartesian-axis" font-size="8">' + shortCode + '</text>';

                        // If 'both' mode, draw quiz accuracy dot
                        if (currentMode === 'both') {
                            var qy = padT + plotH - (m.quiz / 100) * plotH;
                            html += '<circle cx="' + cx + '" cy="' + qy + '" r="4" fill="var(--gold-accent)" stroke="#FFFFFF" stroke-width="1.5" />';
                        }
                    });

                    // In 'both' mode, connect the quiz dots with a smoothed curve
                    if (currentMode === 'both') {
                        var pathD = '';
                        moduleData.forEach(function(m, idx) {
                            var cx = padL + idx * slotW + (slotW / 2);
                            var qy = padT + plotH - (m.quiz / 100) * plotH;
                            if (idx === 0) pathD += 'M ' + cx + ' ' + qy;
                            else pathD += ' L ' + cx + ' ' + qy;
                        });
                        html += '<path d="' + pathD + '" fill="none" stroke="var(--gold-accent)" stroke-width="2" stroke-dasharray="3 3" />';
                    }
                }
            } else if (currentMode === 'trends') {
                // Line / Area Chart for 14-day quiz trends
                var count = trendData.length;
                if (count > 1) {
                    var step = plotW / (count - 1);
                    var lineD = '';
                    var areaD = 'M ' + padL + ' ' + (padT + plotH);

                    trendData.forEach(function(d, idx) {
                        var tx = padL + idx * step;
                        var ty = padT + plotH - (d.score / 100) * plotH;
                        if (idx === 0) {
                            lineD += 'M ' + tx + ' ' + ty;
                            areaD += ' L ' + tx + ' ' + ty;
                        } else {
                            lineD += ' L ' + tx + ' ' + ty;
                            areaD += ' L ' + tx + ' ' + ty;
                        }
                    });

                    areaD += ' L ' + (padL + (count - 1) * step) + ' ' + (padT + plotH) + ' Z';

                    // Area fill gradient
                    html += '<defs><linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="var(--cyan-accent)" stop-opacity="0.35"/><stop offset="100%" stop-color="var(--cyan-accent)" stop-opacity="0.0"/></linearGradient></defs>';
                    html += '<path d="' + areaD + '" fill="url(#areaGrad)" />';
                    html += '<path d="' + lineD + '" fill="none" stroke="var(--cyan-accent)" stroke-width="2.5" />';

                    // Dots & X-Labels
                    trendData.forEach(function(d, idx) {
                        var tx = padL + idx * step;
                        var ty = padT + plotH - (d.score / 100) * plotH;
                        html += '<circle cx="' + tx + '" cy="' + ty + '" r="4" fill="var(--pure-white)" stroke="var(--cyan-accent)" stroke-width="2" onmouseover="showTrendTooltip(event, ' + idx + ')" onmousemove="moveTooltip(event)" onmouseout="hideTooltip()" ontouchstart="showTrendTooltip(event, ' + idx + ')" />';

                        if (idx % 2 === 0) {
                            var dayPart = d.day.split(' ')[1] || d.day;
                            html += '<text x="' + tx + '" y="' + (h - padB + 14) + '" text-anchor="middle" class="recharts-cartesian-axis">' + dayPart + '</text>';
                        }
                    });

                    // BOSEM 80% Benchmark Reference Line
                    var benchmarkY = padT + plotH - (80 / 100) * plotH;
                    html += '<line x1="' + padL + '" y1="' + benchmarkY + '" x2="' + (w - padR) + '" y2="' + benchmarkY + '" stroke="var(--gold-accent)" stroke-width="1.5" stroke-dasharray="4 4" />';
                    html += '<text x="' + (w - padR) + '" y="' + (benchmarkY - 4) + '" text-anchor="end" fill="var(--gold-accent)" font-size="9" font-weight="700">80% Distinction</text>';
                }
            }

            svg.innerHTML = html;
        }

        function showModuleTooltip(evt, idx) {
            var m = moduleData[idx];
            if (!m) return;
            var badgeClass = m.mastery === 'Mastered' ? 'badge-mastered' : (m.mastery === 'Proficient' ? 'badge-proficient' : 'badge-progress');
            var subjColor = m.subject === 'Physics' ? 'var(--physics-blue)' : 'var(--chemistry-orange)';

            tooltip.innerHTML = '<div class="tooltip-title">' + m.fullName + '</div>' +
                '<div class="tooltip-row"><span>Subject:</span><span class="tooltip-val" style="color:' + subjColor + '">' + m.subject + '</span></div>' +
                '<div class="tooltip-row"><span>Completion:</span><span class="tooltip-val">' + m.completion + '%</span></div>' +
                '<div class="tooltip-row"><span>Avg Quiz Score:</span><span class="tooltip-val" style="color:var(--gold-accent)">' + m.quiz + '%</span></div>' +
                '<div class="tooltip-row"><span>Status:</span><span class="mod-badge ' + badgeClass + '">' + m.mastery + '</span></div>' +
                '<div style="font-size:9px; color:var(--slate-400); margin-top:6px; border-top:1px solid rgba(255,255,255,0.1); padding-top:4px;">' + m.concept + '</div>';
            tooltip.style.display = 'block';
            moveTooltip(evt);
        }

        function showTrendTooltip(evt, idx) {
            var d = trendData[idx];
            if (!d) return;
            tooltip.innerHTML = '<div class="tooltip-title">' + d.day + '</div>' +
                '<div class="tooltip-row"><span>Quiz Accuracy:</span><span class="tooltip-val" style="color:var(--cyan-accent)">' + d.score + '%</span></div>' +
                '<div class="tooltip-row"><span>Study Time:</span><span class="tooltip-val">' + d.minutes + ' mins</span></div>';
            tooltip.style.display = 'block';
            moveTooltip(evt);
        }

        function moveTooltip(evt) {
            var rect = container.getBoundingClientRect();
            var clientX = evt.clientX;
            var clientY = evt.clientY;

            if (evt.touches && evt.touches.length > 0) {
                clientX = evt.touches[0].clientX;
                clientY = evt.touches[0].clientY;
            }

            var x = clientX - rect.left + 10;
            var y = clientY - rect.top - 60;

            if (x + 200 > rect.width) x = rect.width - 210;
            if (x < 10) x = 10;
            if (y < 10) y = 10;

            tooltip.style.left = x + 'px';
            tooltip.style.top = y + 'px';
        }

        function hideTooltip() {
            tooltip.style.display = 'none';
        }

        function renderModulesList() {
            var listEl = document.getElementById('modules-list');
            var itemsHtml = '';
            moduleData.forEach(function(m) {
                var isPhysics = m.subject === "Physics";
                var fillClass = isPhysics ? "fill-physics" : "fill-chemistry";
                var subjColor = isPhysics ? "var(--physics-blue)" : "var(--chemistry-orange)";
                var badgeClass = m.mastery === 'Mastered' ? 'badge-mastered' : (m.mastery === 'Proficient' ? 'badge-proficient' : 'badge-progress');

                itemsHtml += '<div class="mod-item">' +
                    '<div class="mod-header">' +
                        '<span class="mod-title">' + m.name + '</span>' +
                        '<span class="mod-badge ' + badgeClass + '">' + m.mastery + '</span>' +
                    '</div>' +
                    '<div class="meta-row"><span>Completion</span><span style="font-weight:700; color:' + subjColor + '">' + m.completion + '%</span></div>' +
                    '<div class="bar-container"><div class="bar-fill ' + fillClass + '" style="width: ' + m.completion + '%"></div></div>' +
                    '<div class="meta-row" style="margin-top:6px;"><span>Quiz Avg</span><span style="font-weight:700; color:var(--gold-accent)">' + m.quiz + '%</span></div>' +
                    '<div class="bar-container"><div class="bar-fill" style="width: ' + m.quiz + '%; background: var(--gold-accent);"></div></div>' +
                '</div>';
            });
            listEl.innerHTML = itemsHtml;
        }

        window.addEventListener('resize', renderChart);
        renderChart();
        renderModulesList();
    </script>
</body>
</html>
        """.trimIndent()
    }

    data class ProgressSummary(
        val totalModulesCount: Int,
        val physicsModulesCount: Int,
        val chemistryModulesCount: Int,
        val physicsCompletionAvg: Int,
        val chemistryCompletionAvg: Int,
        val physicsQuizAvg: Int,
        val chemistryQuizAvg: Int,
        val overallCompletionAvg: Int,
        val overallQuizAvg: Int,
        val modulesMasteredCount: Int = 0
    ) {
        val overallCompletionRate: Int get() = overallCompletionAvg
        val overallQuizScore: Int get() = overallQuizAvg
        val physicsAverageCompletion: Int get() = physicsCompletionAvg
        val chemistryAverageCompletion: Int get() = chemistryCompletionAvg
    }
}
