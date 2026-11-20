package com.example.data

import com.example.model.AcademicClass
import com.example.model.ForumQuestion
import com.example.model.ForumReply
import com.example.model.Subject

/**
 * Seed discussions and peer solutions for BOSEM Physics & Chemistry forum.
 */
object ForumData {

    fun getInitialQuestions(): List<ForumQuestion> {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        return listOf(
            ForumQuestion(
                id = "fq-1",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Light - Reflection and Refraction",
                title = "Why is a concave mirror used as a reflector in vehicle headlights?",
                content = "I was solving BOSEM 2022 Paper question on spherical mirrors. The question asks why concave mirrors are preferred over convex mirrors as vehicle headlight reflectors. Can someone explain the ray optics behind this?",
                authorName = "Linthoingambi Devi",
                authorSchool = "Johnstone Higher Secondary School, Imphal",
                authorRollNo = "25HSLC-10241",
                timestamp = now - (3 * hour),
                upvotesCount = 14,
                isSolved = true,
                tags = listOf("optics", "concave-mirror", "headlights", "bosem-hslc"),
                replies = listOf(
                    ForumReply(
                        id = "fr-1-1",
                        questionId = "fq-1",
                        authorName = "Thoiba Singh",
                        authorSchool = "Imphal Valley Academy",
                        authorBadge = "Top Contributor",
                        content = "When a powerful light source (the bulb) is positioned precisely at the principal focus (F) of a concave mirror, the reflected light rays emerge parallel to the principal axis. This generates a powerful, concentrated beam of light that travels a long distance along the road without dispersing.",
                        timestamp = now - (2 * hour),
                        upvotesCount = 11,
                        isAcceptedSolution = true
                    ),
                    ForumReply(
                        id = "fr-1-2",
                        questionId = "fq-1",
                        authorName = "Sanatombi Sharma",
                        authorSchool = "Model Higher Secondary School",
                        authorBadge = "Peer Tutor",
                        content = "Also remember that convex mirrors produce diverged rays and virtual diminished images (which is why convex mirrors are used for rear-view mirrors, NOT headlights!).",
                        timestamp = now - (1 * hour),
                        upvotesCount = 5,
                        isAcceptedSolution = false
                    )
                )
            ),
            ForumQuestion(
                id = "fq-2",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Electricity & Circuits",
                title = "How to find equivalent resistance of 2Ω, 3Ω, and 6Ω resistors in parallel?",
                content = "Need help with this 3-mark numerical from BOSEM past paper: 'Three resistors of resistances 2Ω, 3Ω, and 6Ω are connected in parallel to a 6V battery. Calculate (a) equivalent resistance, (b) total current drawn.'",
                authorName = "Roshan Meitei",
                authorSchool = "Don Bosco High School, Chingmeirong",
                authorRollNo = "25HSLC-10892",
                timestamp = now - (8 * hour),
                upvotesCount = 9,
                isSolved = true,
                tags = listOf("electricity", "resistors", "numerical", "ohms-law"),
                replies = listOf(
                    ForumReply(
                        id = "fr-2-1",
                        questionId = "fq-2",
                        authorName = "Yaiphaba Luwang",
                        authorSchool = "Kendriya Vidyalaya Lamphelpat",
                        authorBadge = "Peer",
                        content = "Here is the step-by-step working:\n\n1/Rp = 1/R1 + 1/R2 + 1/R3\n1/Rp = 1/2 + 1/3 + 1/6\nLCM of 2, 3, 6 is 6.\n1/Rp = (3 + 2 + 1) / 6 = 6 / 6 = 1 Ω⁻¹\nTherefore, Rp = 1 Ω.\n\nFor total current (I):\nI = V / Rp = 6V / 1Ω = 6 Amperes.",
                        timestamp = now - (6 * hour),
                        upvotesCount = 8,
                        isAcceptedSolution = true
                    )
                )
            ),
            ForumQuestion(
                id = "fq-3",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Chemical Reactions & Equations",
                title = "How to properly balance the reaction of Iron with Steam: Fe + H2O -> Fe3O4 + H2?",
                content = "In Chapter 1 of Class 10 Chemistry, balancing the reaction of iron with steam is tricky. Can someone break down the hit-and-trial coefficient steps as required in BOSEM board exam?",
                authorName = "Bembem Chanu",
                authorSchool = "Little Flower School, Sangaiprou",
                authorRollNo = "25HSLC-10512",
                timestamp = now - (12 * hour),
                upvotesCount = 18,
                isSolved = true,
                tags = listOf("balancing-equations", "iron-steam", "redox", "reactions"),
                replies = listOf(
                    ForumReply(
                        id = "fr-3-1",
                        questionId = "fq-3",
                        authorName = "Chinglen Mangang",
                        authorSchool = "RIMS Secondary Wing",
                        authorBadge = "Chemistry Peer",
                        content = "Step 1: Balance Oxygen first since Fe3O4 has 4 oxygens.\nFe + 4H2O → Fe3O4 + H2\n\nStep 2: Balance Hydrogen next (4 * 2 = 8 H on left, so 4H2 on right).\nFe + 4H2O → Fe3O4 + 4H2\n\nStep 3: Balance Iron (Fe3 on right, so put 3Fe on left).\n3Fe(s) + 4H2O(g) → Fe3O4(s) + 4H2(g)\n\nBoth sides now have: Fe=3, H=8, O=4. Completely balanced!",
                        timestamp = now - (10 * hour),
                        upvotesCount = 15,
                        isAcceptedSolution = true
                    )
                )
            ),
            ForumQuestion(
                id = "fq-4",
                subject = Subject.CHEMISTRY,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Acids, Bases & Salts",
                title = "Why do brass and copper vessels get a green coating, and why does lemon juice clean them?",
                content = "Our teacher mentioned this corrosion question is frequent in the 2-mark section. What is the exact chemical composition of the green coating on copper and why does acid restore the shine?",
                authorName = "Tomba Singh",
                authorSchool = "Tamphasana Girls Higher Secondary",
                authorRollNo = "25HSLC-10762",
                timestamp = now - (1 * day),
                upvotesCount = 12,
                isSolved = true,
                tags = listOf("corrosion", "copper", "acids-bases", "cleaning"),
                replies = listOf(
                    ForumReply(
                        id = "fr-4-1",
                        questionId = "fq-4",
                        authorName = "Linthoingambi Devi",
                        authorSchool = "Johnstone Higher Secondary School",
                        authorBadge = "Top Contributor",
                        content = "When copper is exposed to moist air containing CO2 and O2, it forms basic copper carbonate [CuCO3 · Cu(OH)2], which is green in color.\n\nLemon juice contains citric acid. When applied, the acid reacts with the basic copper carbonate to form a soluble salt and water, neutralizing the alkaline layer and revealing the clean, reddish-brown metallic copper surface.",
                        timestamp = now - (20 * hour),
                        upvotesCount = 10,
                        isAcceptedSolution = true
                    )
                )
            ),
            ForumQuestion(
                id = "fq-5",
                subject = Subject.PHYSICS,
                academicClass = AcademicClass.CLASS_10,
                chapterName = "Magnetic Effects of Electric Current",
                title = "Difference between Fleming's Left-Hand Rule and Right-Hand Rule?",
                content = "I keep mixing up when to use Fleming's Left-Hand Rule versus Right-Hand Rule in exam questions. What is the clearest memory trick for BOSEM tests?",
                authorName = "Naoba Meitei",
                authorSchool = "Kakching Higher Secondary School",
                authorRollNo = "25HSLC-10331",
                timestamp = now - (2 * day),
                upvotesCount = 15,
                isSolved = false,
                tags = listOf("magnetism", "flemings-rule", "motor", "generator"),
                replies = listOf(
                    ForumReply(
                        id = "fr-5-1",
                        questionId = "fq-5",
                        authorName = "Thoiba Singh",
                        authorSchool = "Imphal Valley Academy",
                        authorBadge = "Top Contributor",
                        content = "Remember the simple mnemonic:\n\n1. Left-Hand Rule = Electric MOTOR (M-O-T-O-R). Used to find the direction of FORCE/MOTION acting on a current-carrying conductor in a magnetic field.\n\n2. Right-Hand Rule = Electric GENERATOR (G-E-N-E-R-A-T-O-R). Used to find the direction of INDUCED CURRENT produced when a conductor moves in a magnetic field.",
                        timestamp = now - (1 * day),
                        upvotesCount = 12,
                        isAcceptedSolution = false
                    )
                )
            )
        )
    }
}
