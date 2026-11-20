package com.example.data

import com.example.model.AcademicClass
import com.example.model.Announcement
import com.example.model.ImportantQuestion
import com.example.model.MaterialCategory
import com.example.model.MaterialSection
import com.example.model.QuestionDifficulty
import com.example.model.QuestionType
import com.example.model.StudyMaterial
import com.example.model.Subject

object CurriculumData {

    val announcements = listOf(
        Announcement(
            id = "ann-1",
            title = "BOSEM HSLC Exam 2025: Science Paper Blueprint Released",
            message = "The Board of Secondary Education Manipur (BOSEM) has officially announced the HSLC 2025 question paper design. Science theory consists of 80 marks (Physics 40 + Chemistry 40) with 20 marks school-based practical assessment.",
            date = "Oct 24, 2026",
            tag = "BOSEM Official",
            isUrgent = true,
            targetClass = AcademicClass.CLASS_10
        ),
        Announcement(
            id = "ann-2",
            title = "Class 9 Annual Examination Model Papers Now Available",
            message = "PCM Study Centre has uploaded the verified model question papers and chapter-wise marking distributions for Class 9 Physics and Chemistry covering the full revised syllabus.",
            date = "Oct 20, 2026",
            tag = "Study Update",
            isUrgent = false,
            targetClass = AcademicClass.CLASS_9
        ),
        Announcement(
            id = "ann-3",
            title = "Previous 10 Years' HSLC Science Papers Archive Complete",
            message = "Complete question papers from 2015 through 2024 with question-by-question answer keys and marking scheme annotations are available for instant offline reading.",
            date = "Oct 15, 2026",
            tag = "Archive",
            isUrgent = false,
            targetClass = AcademicClass.CLASS_10
        ),
        Announcement(
            id = "ann-4",
            title = "Special Revision: Balancing Chemical Equations & Numerical Shortcuts",
            message = "Access formula sheets and step-by-step solving guides for Light and Electricity numerical problems in the Important Questions section.",
            date = "Oct 10, 2026",
            tag = "Revision Guide",
            isUrgent = false,
            targetClass = null
        )
    )

    fun getInitialMaterials(): List<StudyMaterial> {
        val list = mutableListOf<StudyMaterial>()

        // ---------------- CLASS 10 PHYSICS ----------------
        list.add(
            StudyMaterial(
                id = "c10-phy-ch1-notes",
                title = "Light: Reflection & Refraction - Master Revision Notes",
                description = "Comprehensive revision notes with mirror formulas, lens formulas, sign conventions, ray diagrams and refractive index tables aligned with BOSEM HSLC syllabus.",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.PHYSICS,
                chapterName = "Light – Reflection and Refraction",
                chapterNumber = 1,
                category = MaterialCategory.NOTES,
                academicYear = "2024-2025",
                fileSize = "1.8 MB",
                fileSizeBytes = 1887436,
                isOfficialBoard = true,
                isVerified = true,
                pageCount = 12,
                downloadCount = 428,
                tags = listOf("Ray Diagrams", "Mirror Formula", "Snell's Law", "Lens Power"),
                keyTakeaways = listOf(
                    "Mirror formula: 1/f = 1/v + 1/u with New Cartesian Sign Convention.",
                    "Magnification m = -v/u = h'/h for mirrors; m = v/u = h'/h for lenses.",
                    "Snell's Law: n = sin(i)/sin(r) = c/v.",
                    "Power of a lens P = 1/f (in metres), SI unit is Dioptre (D)."
                ),
                contentSummary = "Clear conceptual breakdowns of spherical mirrors (concave and convex), image formation rules, refraction through glass slabs, total internal reflection, lens formula derivations, and power of combination of lenses.",
                sections = listOf(
                    MaterialSection(
                        title = "1. Laws of Reflection & Spherical Mirrors",
                        content = "The angle of incidence is equal to the angle of reflection. The incident ray, the normal to the mirror at the point of incidence and the reflected ray, all lie in the same plane. Concave mirrors form real and inverted images except when the object is placed between pole and focus (virtual, erect and magnified). Convex mirrors always form virtual, erect and diminished images.",
                        formulaOrEquation = "1/f = 1/v + 1/u,  m = -v/u = h'/h",
                        highlights = listOf("Object distance (u) is always negative", "Focal length of concave mirror is negative; convex is positive")
                    ),
                    MaterialSection(
                        title = "2. Refraction & Snell's Law",
                        content = "When light travels from a rarer medium to a denser medium, it bends towards the normal. When it travels from denser to rarer, it bends away from the normal. Refractive index of medium 2 with respect to medium 1 is the ratio of speed of light in medium 1 to speed of light in medium 2.",
                        formulaOrEquation = "n21 = sin(i) / sin(r) = v1 / v2",
                        highlights = listOf("Absolute refractive index n = c/v", "Higher refractive index means optically denser medium")
                    ),
                    MaterialSection(
                        title = "3. Lenses & Lens Formula",
                        content = "Convex lens is converging and forms real images (virtual when between F1 and O). Concave lens is diverging and always forms virtual, erect and diminished images.",
                        formulaOrEquation = "1/f = 1/v - 1/u,  m = v/u = h'/h,  P = 1/f(m)",
                        highlights = listOf("1 Dioptre is the power of a lens of focal length 1 metre", "Convex lens power is positive; concave lens power is negative")
                    )
                )
            )
        )

        list.add(
            StudyMaterial(
                id = "c10-phy-ch3-notes",
                title = "Electricity - Concepts, Circuit Laws & Numericals Guide",
                description = "Complete theory and numerical practice guide covering Ohm's Law, factors affecting resistance, series and parallel circuits, Joule's heating effect, and commercial units of energy.",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.PHYSICS,
                chapterName = "Electricity",
                chapterNumber = 3,
                category = MaterialCategory.NOTES,
                academicYear = "2024-2025",
                fileSize = "2.1 MB",
                fileSizeBytes = 2202009,
                isOfficialBoard = true,
                isVerified = true,
                pageCount = 14,
                downloadCount = 512,
                tags = listOf("Ohm's Law", "Resistivity", "Series & Parallel", "Joule's Law"),
                keyTakeaways = listOf(
                    "Current I = Q/t; Potential Difference V = W/Q.",
                    "Ohm's Law: V = IR at constant temperature.",
                    "Resistance R = rho * (l / A); Resistivity depends only on material and temperature.",
                    "Series: Rs = R1 + R2 + R3; Parallel: 1/Rp = 1/R1 + 1/R2 + 1/R3.",
                    "Heat H = I^2 * R * t; 1 kWh = 3.6 x 10^6 Joules."
                ),
                contentSummary = "Structured breakdown of electric circuits, ammeter (low resistance, series), voltmeter (high resistance, parallel), drift of electrons, equivalent resistance calculations, fuse wire working principle, and electric rating calculations.",
                sections = listOf(
                    MaterialSection(
                        title = "1. Current, Potential & Ohm's Law",
                        content = "Electric current is the rate of flow of electric charges. One Ampere is the flow of one Coulomb of charge per second. Potential difference is work done in moving a unit positive charge between two points.",
                        formulaOrEquation = "I = Q/t,  V = W/Q,  V = I * R",
                        highlights = listOf("SI unit of charge is Coulomb (C)", "SI unit of potential difference is Volt (V)")
                    ),
                    MaterialSection(
                        title = "2. Resistor Combinations",
                        content = "In a series circuit, current remains the same through all resistors while potential difference divides. In parallel, potential difference remains the same across each resistor while total current is the sum of currents.",
                        formulaOrEquation = "Rs = R1 + R2 + ...,  1/Rp = 1/R1 + 1/R2 + ...",
                        highlights = listOf("Parallel combination minimizes total resistance", "Domestic wiring is always in parallel")
                    ),
                    MaterialSection(
                        title = "3. Electric Power & Heating",
                        content = "Joule's law states that heat produced in a resistor is directly proportional to square of current, resistance and time. Electric power is the rate at which electric energy is consumed.",
                        formulaOrEquation = "H = I^2 * R * t,  P = V * I = I^2 * R = V^2 / R",
                        highlights = listOf("1 kilowatt-hour (kWh) = 1 unit = 3.6 x 10^6 J", "Fuse wire has low melting point and high resistance")
                    )
                )
            )
        )

        // ---------------- CLASS 10 CHEMISTRY ----------------
        list.add(
            StudyMaterial(
                id = "c10-chm-ch1-notes",
                title = "Chemical Reactions & Equations - Balanced Equations & Types",
                description = "Master guide for BOSEM HSLC covering chemical equation balancing, combination, decomposition, displacement, double displacement, redox reactions, corrosion and rancidity.",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.CHEMISTRY,
                chapterName = "Chemical Reactions and Equations",
                chapterNumber = 1,
                category = MaterialCategory.NOTES,
                academicYear = "2024-2025",
                fileSize = "1.6 MB",
                fileSizeBytes = 1677721,
                isOfficialBoard = true,
                isVerified = true,
                pageCount = 10,
                downloadCount = 389,
                tags = listOf("Balancing", "Redox", "Exothermic", "Endothermic", "Corrosion"),
                keyTakeaways = listOf(
                    "Law of conservation of mass: total mass of reactants = total mass of products.",
                    "Types of reactions: Combination, Decomposition (Thermal, Electrolytic, Photolytic), Displacement, Double Displacement.",
                    "Redox: Oxidation is gain of oxygen/loss of hydrogen; Reduction is loss of oxygen/gain of hydrogen.",
                    "Rusting of iron requires both oxygen and moisture: 2Fe + 3/2 O2 + xH2O -> Fe2O3.xH2O."
                ),
                contentSummary = "Detailed reactions with physical states (s, l, g, aq), color change indicators (copper sulphate + iron nail, lead nitrate + potassium iodide), respiration as exothermic reaction, and prevention of rancidity.",
                sections = listOf(
                    MaterialSection(
                        title = "1. Writing & Balancing Chemical Equations",
                        content = "A chemical equation must be balanced to satisfy the law of conservation of mass. Number of atoms of each element must remain the same before and after a reaction.",
                        formulaOrEquation = "3Fe(s) + 4H2O(g) -> Fe3O4(s) + 4H2(g)",
                        highlights = listOf("Always balance using coefficients, never alter chemical subscripts", "Include state symbols (s, l, g, aq)")
                    ),
                    MaterialSection(
                        title = "2. Types of Chemical Reactions",
                        content = "Combination: two or more reactants form one product (CaO + H2O -> Ca(OH)2 + Heat). Decomposition: single reactant breaks down into multiple products (2FeSO4 -> Fe2O3 + SO2 + SO3). Displacement: more reactive element displaces less reactive element (Fe + CuSO4 -> FeSO4 + Cu).",
                        formulaOrEquation = "2Pb(NO3)2(s) --Heat-> 2PbO(s) + 4NO2(g) + O2(g)",
                        highlights = listOf("NO2 is a reddish-brown gas", "Photolytic decomposition of AgCl is used in black & white photography")
                    ),
                    MaterialSection(
                        title = "3. Redox, Corrosion & Rancidity",
                        content = "In CuO + H2 -> Cu + H2O, CuO is reduced to Cu (oxidizing agent) and H2 is oxidized to H2O (reducing agent). Corrosion damages metals (rust on iron, black coating on silver, green coating on copper). Rancidity is oxidation of fats and oils prevented by antioxidants or nitrogen flushing.",
                        formulaOrEquation = "CuO + H2 --Heat-> Cu + H2O",
                        highlights = listOf("Oxidation: Addition of O or removal of H", "Rancidity spoils taste and smell of food")
                    )
                )
            )
        )

        list.add(
            StudyMaterial(
                id = "c10-chm-ch2-notes",
                title = "Acids, Bases and Salts - pH Scale & Important Salts",
                description = "Complete syllabus notes for Acids and Bases, Olfactory indicators, Universal Indicator & pH scale, chlor-alkali process, bleaching powder, baking soda, washing soda and Plaster of Paris.",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.CHEMISTRY,
                chapterName = "Acids, Bases and Salts",
                chapterNumber = 2,
                category = MaterialCategory.NOTES,
                academicYear = "2024-2025",
                fileSize = "1.9 MB",
                fileSizeBytes = 1992294,
                isOfficialBoard = true,
                isVerified = true,
                pageCount = 11,
                downloadCount = 445,
                tags = listOf("pH Scale", "Baking Soda", "Plaster of Paris", "Neutralization"),
                keyTakeaways = listOf(
                    "Acids release H+(aq) / H3O+ ions; Bases release OH-(aq) ions.",
                    "pH < 7 is acidic; pH = 7 is neutral; pH > 7 is basic.",
                    "Bleaching powder: Ca(OH)2 + Cl2 -> CaOCl2 + H2O.",
                    "Baking soda is NaHCO3; Heating gives Na2CO3 + H2O + CO2.",
                    "Plaster of Paris CaSO4.1/2H2O turns to Gypsum CaSO4.2H2O on adding water."
                ),
                contentSummary = "Full preparation reactions, properties and uses of sodium hydroxide, bleaching powder, sodium hydrogen carbonate, sodium carbonate decahydrate, water of crystallization, and tooth decay prevention.",
                sections = listOf(
                    MaterialSection(
                        title = "1. Properties of Acids and Bases",
                        content = "Acids turn blue litmus red; bases turn red litmus blue. Acids react with metals to produce hydrogen gas (Zn + H2SO4 -> ZnSO4 + H2). Acids react with metal carbonates to produce carbon dioxide gas which turns lime water milky.",
                        formulaOrEquation = "Acid + Base -> Salt + Water + Heat",
                        highlights = listOf("Never add water to concentrated acid; always add acid to water slowly with constant stirring")
                    ),
                    MaterialSection(
                        title = "2. The pH Scale & Importance",
                        content = "pH stands for 'potenz' (power of hydrogen). Scale ranges from 0 to 14. Human body works within pH range 7.0 to 7.8. Acid rain has pH < 5.6. Tooth decay begins when mouth pH falls below 5.5.",
                        formulaOrEquation = "pH = -log10[H+]",
                        highlights = listOf("Antacids like Magnesium hydroxide (Milk of magnesia) neutralize excess stomach acid", "Nettle sting contains methanoic acid neutralized by dock plant leaf")
                    ),
                    MaterialSection(
                        title = "3. Important Chemical Salts",
                        content = "Bleaching powder (CaOCl2) used as disinfectant and bleaching agent. Baking soda (NaHCO3) used in baking and fire extinguishers. Washing soda (Na2CO3.10H2O) removes permanent hardness of water. Plaster of Paris (CaSO4.1/2H2O) sets fractured bones.",
                        formulaOrEquation = "CaSO4.1/2H2O + 1 1/2 H2O -> CaSO4.2H2O (Gypsum)",
                        highlights = listOf("Copper sulphate crystals contain 5 molecules of water of crystallization (CuSO4.5H2O)")
                    )
                )
            )
        )

        // ---------------- PREVIOUS 10 YEARS' QUESTION PAPERS (2015 - 2024) ----------------
        val pastYears = listOf(
            Triple(2024, "BOSEM HSLC Examination 2024 - Science (Physics & Chemistry)", "3.2 MB"),
            Triple(2023, "BOSEM HSLC Examination 2023 - Science (Physics & Chemistry)", "2.9 MB"),
            Triple(2022, "BOSEM HSLC Examination 2022 - Science (Physics & Chemistry)", "2.7 MB"),
            Triple(2021, "BOSEM HSLC Special Assessment 2021 - Science", "2.4 MB"),
            Triple(2020, "BOSEM HSLC Examination 2020 - Science (Physics & Chemistry)", "3.1 MB"),
            Triple(2019, "BOSEM HSLC Examination 2019 - Science (Physics & Chemistry)", "2.8 MB"),
            Triple(2018, "BOSEM HSLC Examination 2018 - Science (Physics & Chemistry)", "3.0 MB"),
            Triple(2017, "BOSEM HSLC Examination 2017 - Science (Physics & Chemistry)", "2.6 MB"),
            Triple(2016, "BOSEM HSLC Examination 2016 - Science (Physics & Chemistry)", "2.5 MB"),
            Triple(2015, "BOSEM HSLC Examination 2015 - Science (Physics & Chemistry)", "2.7 MB")
        )

        pastYears.forEach { (year, title, size) ->
            list.add(
                StudyMaterial(
                    id = "bosem-hslc-paper-$year",
                    title = title,
                    description = "Official BOSEM HSLC Science theory paper for $year. Covers Section A (Physics - 40 Marks) and Section B (Chemistry - 40 Marks) with verified answers, mark allocations and examiner guidelines.",
                    academicClass = AcademicClass.CLASS_10,
                    subject = Subject.PHYSICS,
                    chapterName = "HSLC Board Exam $year",
                    chapterNumber = 0,
                    category = MaterialCategory.PREVIOUS_YEAR_PAPERS,
                    examYear = year,
                    academicYear = "$year Examination",
                    fileSize = size,
                    fileSizeBytes = 2800000,
                    isOfficialBoard = true,
                    isVerified = true,
                    pageCount = 10,
                    downloadCount = 650 + (2025 - year) * 45,
                    tags = listOf("BOSEM HSLC", "Official $year", "Physics & Chemistry", "Marking Scheme"),
                    keyTakeaways = listOf(
                        "Time Allowed: 3 Hours | Maximum Marks: 80 (Physics 40 + Chemistry 40)",
                        "Section A: Physics contains MCQs, 2-mark short reasoning, 3-mark ray diagrams & numericals, and 5-mark long answers.",
                        "Section B: Chemistry contains balancing reactions, salt preparations, metallurgy and carbon reasoning questions.",
                        "All questions are compulsory with internal choices in 3 and 5 mark questions."
                    ),
                    contentSummary = "Authentic BOSEM HSLC Science Paper of $year. Includes exact question formulations, diagram-based questions, numericals with full stepwise solution keys and marking distribution.",
                    sections = listOf(
                        MaterialSection(
                            title = "Section A: Physics ($year - 40 Marks)",
                            content = "Q1-Q5: Multiple Choice Questions (1 mark each)\nQ6-Q9: Very Short Answer type (1 mark each)\nQ10-Q13: Short Answer type I (2 marks each - Circuit diagrams, sign conventions)\nQ14-Q16: Short Answer type II (3 marks each - Lens/mirror numericals, Fleming's rules)\nQ17-Q18: Long Answer type (5 marks each - Total internal reflection, electric generator/motor)",
                            formulaOrEquation = "Theory: 80 Marks | Practical: 20 Marks | Pass: 33%"
                        ),
                        MaterialSection(
                            title = "Section B: Chemistry ($year - 40 Marks)",
                            content = "Q19-Q23: Multiple Choice Questions (1 mark each)\nQ24-Q27: Very Short Answer (1 mark each)\nQ28-Q31: Short Answer type I (2 marks each - Balanced equations, acid-base indicators)\nQ32-Q34: Short Answer type II (3 marks each - Extraction of metals, chlor-alkali process)\nQ35-Q36: Long Answer type (5 marks each - Homologous series, cleansing action of soap, esterification)",
                            formulaOrEquation = "Verified Answer Keys & Marking Scheme Included"
                        )
                    )
                )
            )
        }

        // ---------------- CLASS 9 PREVIOUS PAPERS & NOTES ----------------
        list.add(
            StudyMaterial(
                id = "c9-phy-ch1-notes",
                title = "Motion - Equations of Motion & Graphical Analysis",
                description = "Complete Class 9 Physics notes on distance, displacement, velocity-time graphs, derivation of equations of motion by graphical method and uniform circular motion.",
                academicClass = AcademicClass.CLASS_9,
                subject = Subject.PHYSICS,
                chapterName = "Motion",
                chapterNumber = 1,
                category = MaterialCategory.NOTES,
                academicYear = "2024-2025",
                fileSize = "1.5 MB",
                fileSizeBytes = 1572864,
                isOfficialBoard = true,
                isVerified = true,
                pageCount = 8,
                downloadCount = 310,
                tags = listOf("Distance vs Displacement", "v = u + at", "Velocity-Time Graph"),
                keyTakeaways = listOf(
                    "Distance is scalar (actual path length); Displacement is vector (shortest path).",
                    "Speed = Distance / Time; Velocity = Displacement / Time.",
                    "Equations of motion: (1) v = u + at, (2) s = ut + 1/2 at^2, (3) v^2 = u^2 + 2as.",
                    "Slope of v-t graph represents acceleration; area under v-t graph gives displacement."
                ),
                contentSummary = "Conceptual fundamentals, uniform vs non-uniform motion, vector and scalar distinctions, numerical solving methodology, and circular motion formulas."
            )
        )

        list.add(
            StudyMaterial(
                id = "c9-chm-ch1-notes",
                title = "Matter in Our Surroundings - States & Phase Transitions",
                description = "Complete Class 9 Chemistry revision notes on characteristics of particles of matter, diffusion, Kelvin to Celsius conversions, latent heat and evaporation factors.",
                academicClass = AcademicClass.CLASS_9,
                subject = Subject.CHEMISTRY,
                chapterName = "Matter in Our Surroundings",
                chapterNumber = 1,
                category = MaterialCategory.NOTES,
                academicYear = "2024-2025",
                fileSize = "1.4 MB",
                fileSizeBytes = 1468006,
                isOfficialBoard = true,
                isVerified = true,
                pageCount = 7,
                downloadCount = 280,
                tags = listOf("States of Matter", "Latent Heat", "Evaporation", "Kelvin Scale"),
                keyTakeaways = listOf(
                    "Matter is particulate and particles have spaces and kinetic energy.",
                    "Solid -> Liquid (Melting); Liquid -> Gas (Vaporization); Solid <-> Gas (Sublimation).",
                    "Temperature in Kelvin = Temperature in Celsius + 273.15.",
                    "Evaporation is a surface phenomenon that causes cooling."
                ),
                contentSummary = "Detailed explanations of kinetic molecular theory, why steam causes more severe burns than boiling water, dry ice (solid CO2), and cotton clothes in summer."
            )
        )

        // ---------------- SAMPLE PAPERS & MODEL TESTS ----------------
        list.add(
            StudyMaterial(
                id = "c10-sample-paper-1",
                title = "BOSEM HSLC Model Sample Paper 1 (Full Syllabus)",
                description = "Strictly designed according to latest BOSEM HSLC pattern. Includes Section A Physics (40 Marks) + Section B Chemistry (40 Marks) with complete step-by-step marking scheme.",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.PHYSICS,
                chapterName = "HSLC Science Model Paper",
                chapterNumber = 0,
                category = MaterialCategory.SAMPLE_PAPERS,
                academicYear = "2024-2025",
                fileSize = "2.4 MB",
                fileSizeBytes = 2516582,
                isOfficialBoard = true,
                isVerified = true,
                pageCount = 12,
                downloadCount = 720,
                tags = listOf("Full Mock Test", "Marking Scheme", "3 Hours 80 Marks"),
                keyTakeaways = listOf(
                    "Standard 80-mark balanced HSLC blueprint.",
                    "Covers high-weightage topics: Light (10m), Electricity (12m), Chemical Reactions (8m), Carbon (10m).",
                    "Stepwise answer keys provided with teacher guidance."
                ),
                contentSummary = "Comprehensive mock test with blueprint, questions across all cognitive levels (Knowledge, Understanding, Application, Skill)."
            )
        )

        list.add(
            StudyMaterial(
                id = "c10-sample-paper-2",
                title = "BOSEM HSLC Model Sample Paper 2 (Expected Trends)",
                description = "Teacher-curated practice examination with high-yield numericals in optics & electricity, and key organic chemistry reaction mechanisms for Class 10.",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.CHEMISTRY,
                chapterName = "HSLC Science Model Paper",
                chapterNumber = 0,
                category = MaterialCategory.SAMPLE_PAPERS,
                academicYear = "2024-2025",
                fileSize = "2.2 MB",
                fileSizeBytes = 2306867,
                isOfficialBoard = false,
                isVerified = true,
                pageCount = 10,
                downloadCount = 540,
                tags = listOf("Teacher Created", "Expected Numericals", "High-Yield"),
                keyTakeaways = listOf(
                    "Features tricky numericals on lens combinations and equivalent resistance.",
                    "Includes balanced equations for industrial salt processes.",
                    "Full solutions and scoring rubric included."
                )
            )
        )

        list.add(
            StudyMaterial(
                id = "c9-sample-paper-1",
                title = "Class 9 Annual Examination Model Paper - Science",
                description = "Annual exam preparation paper for Class 9 Physics and Chemistry covering Motion, Laws of Motion, Gravitation, Matter, and Atomic Structure.",
                academicClass = AcademicClass.CLASS_9,
                subject = Subject.PHYSICS,
                chapterName = "Class 9 Annual Model Paper",
                chapterNumber = 0,
                category = MaterialCategory.SAMPLE_PAPERS,
                academicYear = "2024-2025",
                fileSize = "1.9 MB",
                fileSizeBytes = 1992294,
                isOfficialBoard = true,
                isVerified = true,
                pageCount = 9,
                downloadCount = 315,
                tags = listOf("Class 9 Annual", "Physics & Chemistry", "Solved Paper")
            )
        )

        return list
    }

    fun getImportantQuestions(): List<ImportantQuestion> {
        return listOf(
            ImportantQuestion(
                id = "iq-1",
                chapterName = "Light – Reflection and Refraction",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.PHYSICS,
                questionText = "An object 4 cm in height is placed at a distance of 25 cm in front of a concave mirror of focal length 15 cm. At what distance from the mirror should a screen be placed to obtain a sharp image? Find the nature and height of the image.",
                type = QuestionType.NUMERICAL,
                marks = 3,
                yearAsked = "BOSEM HSLC 2023, 2019",
                difficulty = QuestionDifficulty.INTERMEDIATE,
                numericalGiven = "Height of object h = +4 cm, Object distance u = -25 cm, Focal length f = -15 cm",
                numericalFormula = "1/f = 1/v + 1/u  and  m = h'/h = -v/u",
                answerExplanation = "Using mirror formula:\n1/v = 1/f - 1/u\n1/v = 1/(-15) - 1/(-25) = -1/15 + 1/25 = (-5 + 3)/75 = -2/75\nv = -75/2 = -37.5 cm.\n\nThus, the screen should be placed at 37.5 cm in front of the concave mirror.\n\nHeight of image:\nm = -v/u = -(-37.5) / (-25) = -1.5\nh' = m * h = -1.5 * 4 = -6 cm.\n\nConclusion: The image is real, inverted, magnified and formed at 37.5 cm in front of the mirror."
            ),
            ImportantQuestion(
                id = "iq-2",
                chapterName = "Light – Reflection and Refraction",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.PHYSICS,
                questionText = "Which of the following lens will produce a power of +2.5 D?",
                type = QuestionType.MCQ,
                marks = 1,
                yearAsked = "BOSEM HSLC 2024",
                difficulty = QuestionDifficulty.BASIC,
                mcqOptions = listOf(
                    "Convex lens of focal length 40 cm",
                    "Concave lens of focal length 40 cm",
                    "Convex lens of focal length 25 cm",
                    "Concave lens of focal length 25 cm"
                ),
                correctOptionIndex = 0,
                answerExplanation = "Power P = 1 / f(in metres). Since P is positive (+2.5 D), it must be a convex lens. Focal length f = 1 / P = 1 / 2.5 = 0.4 m = 40 cm."
            ),
            ImportantQuestion(
                id = "iq-3",
                chapterName = "Electricity",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.PHYSICS,
                questionText = "State Ohm's Law. Draw a labeled circuit diagram to verify it. What does the slope of a V-I graph represent?",
                type = QuestionType.LONG_ANSWER,
                marks = 5,
                yearAsked = "BOSEM HSLC 2022, 2017",
                difficulty = QuestionDifficulty.ADVANCED,
                answerExplanation = "Ohm's Law: The potential difference (V) across the ends of a given metallic conductor in an electric circuit is directly proportional to the current (I) flowing through it, provided its temperature remains constant (V = IR).\n\nCircuit Diagram components:\n1. Battery or power source\n2. Key (switch)\n3. Ammeter in series\n4. Unknown resistor R\n5. Voltmeter in parallel across R\n6. Rheostat (variable resistor) to adjust current.\n\nSlope of V-I graph:\nSlope = delta V / delta I = Resistance (R) of the conductor. If the graph is plotted as I vs V, then slope = 1/R (Conductance)."
            ),
            ImportantQuestion(
                id = "iq-4",
                chapterName = "Electricity",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.PHYSICS,
                questionText = "Why are electric bulbs filled with chemically inactive gases like nitrogen and argon?",
                type = QuestionType.CONCEPTUAL,
                marks = 2,
                yearAsked = "BOSEM HSLC 2020",
                difficulty = QuestionDifficulty.BASIC,
                answerExplanation = "Electric bulbs contain a thin tungsten filament that glows white-hot (around 2500°C). If air (oxygen) were present, tungsten would rapidly oxidize and burn out within seconds. Filling the bulb with unreactive nitrogen or argon gas prevents oxidation of the filament and significantly prolongs the lifespan of the bulb."
            ),
            ImportantQuestion(
                id = "iq-5",
                chapterName = "Chemical Reactions and Equations",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.CHEMISTRY,
                questionText = "Why is respiration considered an exothermic reaction? Write the balanced chemical equation representing cellular respiration.",
                type = QuestionType.SHORT_ANSWER,
                marks = 2,
                yearAsked = "BOSEM HSLC 2024, 2018",
                difficulty = QuestionDifficulty.BASIC,
                answerExplanation = "During digestion, food containing carbohydrates is broken down into glucose. This glucose combines with oxygen in the cells of our body and releases energy required to maintain life processes. Since energy is released in the form of heat/ATP, respiration is an exothermic reaction.\n\nBalanced Equation:\nC6H12O6(aq) + 6O2(aq) -> 6CO2(aq) + 6H2O(l) + Energy"
            ),
            ImportantQuestion(
                id = "iq-6",
                chapterName = "Chemical Reactions and Equations",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.CHEMISTRY,
                questionText = "What happens when lead nitrate powder is heated in a boiling tube? Give the balanced equation and state the color of the gas evolved.",
                type = QuestionType.SHORT_ANSWER,
                marks = 3,
                yearAsked = "BOSEM HSLC 2023, 2016",
                difficulty = QuestionDifficulty.INTERMEDIATE,
                answerExplanation = "When white lead nitrate powder is heated, it undergoes thermal decomposition to form yellow lead monoxide residue, brown nitrogen dioxide gas, and colorless oxygen gas.\n\nBalanced Equation:\n2Pb(NO3)2(s) --Heat-> 2PbO(s) [Yellow] + 4NO2(g) [Brown fumes] + O2(g) [Colorless]\n\nThe brown fumes evolved are of Nitrogen Dioxide (NO2)."
            ),
            ImportantQuestion(
                id = "iq-7",
                chapterName = "Acids, Bases and Salts",
                academicClass = AcademicClass.CLASS_10,
                subject = Subject.CHEMISTRY,
                questionText = "A student tests a white powder with water and finds it produces carbon dioxide with vinegar, and turns litmus blue. The compound is:",
                type = QuestionType.MCQ,
                marks = 1,
                yearAsked = "BOSEM HSLC 2021",
                difficulty = QuestionDifficulty.BASIC,
                mcqOptions = listOf(
                    "Baking Soda (Sodium hydrogen carbonate)",
                    "Bleaching Powder",
                    "Plaster of Paris",
                    "Common Salt"
                ),
                correctOptionIndex = 0,
                answerExplanation = "Sodium hydrogen carbonate (NaHCO3) is a mild non-corrosive basic salt (turns litmus blue). It reacts with acids like acetic acid (vinegar) with brisk effervescence releasing CO2 gas."
            ),
            ImportantQuestion(
                id = "iq-8",
                chapterName = "Motion",
                academicClass = AcademicClass.CLASS_9,
                subject = Subject.PHYSICS,
                questionText = "A bus accelerates uniformly from 18 km/h to 36 km/h in 5 seconds. Calculate: (i) the acceleration, (ii) the distance covered by the bus in that time.",
                type = QuestionType.NUMERICAL,
                marks = 3,
                yearAsked = "Class 9 Annual Exam 2023",
                difficulty = QuestionDifficulty.INTERMEDIATE,
                numericalGiven = "Initial velocity u = 18 km/h = 18 * (5/18) = 5 m/s, Final velocity v = 36 km/h = 36 * (5/18) = 10 m/s, Time t = 5 s",
                numericalFormula = "a = (v - u)/t  and  s = ut + 1/2 at^2",
                answerExplanation = "(i) Acceleration a = (v - u)/t = (10 - 5)/5 = 5/5 = 1 m/s^2.\n\n(ii) Distance covered s = ut + 1/2 at^2 = (5 * 5) + 0.5 * 1 * (5^2) = 25 + 12.5 = 37.5 m.\n\nConclusion: The acceleration is 1 m/s^2 and the distance covered is 37.5 metres."
            )
        )
    }
}
