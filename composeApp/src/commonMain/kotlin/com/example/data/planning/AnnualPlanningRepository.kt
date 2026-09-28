package com.example.data.planning

import com.example.compat.*
import kotlinx.coroutines.IO

import com.example.data.models.ClassSection

object AnnualPlanningRepository {

    fun resolveLevel(classSection: ClassSection?): Int {
        if (classSection == null) return 0

        // 0. The level chosen by the teacher and stored with the class always wins
        if (classSection.level in 1..6) {
            return classSection.level
        }

        // 1. Try parsing explicit level from class name or formatted name
        val parsedFromName = parseLevelFromClassName(classSection.name)
        if (parsedFromName in 1..6) {
            return parsedFromName
        }
        val parsedFromFormatted = parseLevelFromClassName(classSection.getFormattedName())
        if (parsedFromFormatted in 1..6) {
            return parsedFromFormatted
        }
        val parsedFromSection = parseLevelFromClassName(classSection.sectionName)
        if (parsedFromSection in 1..6) {
            return parsedFromSection
        }

        // 2. If name does not contain explicit level indicator, check stored level property
        if (classSection.level in 1..6) {
            return classSection.level
        }

        return 0
    }

    fun parseLevelFromClassName(className: String): Int {
        val name = className.trim()
        if (name.isBlank()) return 0
        
        // 1. Direct textual keywords
        if (name.contains("السادسة") || name.contains("سادسة") || name.contains("السادس") || name.contains("سادس") ||
            name.contains("6AP", ignoreCase = true) || name.contains("6 AP", ignoreCase = true) ||
            name.contains("6AF", ignoreCase = true) || name.contains("6 AF", ignoreCase = true) ||
            name.contains("6أساس") || name.contains("6 أساس") || name.contains("6اساس") ||
            name.contains("6ème", ignoreCase = true) || name.contains("6eme", ignoreCase = true)
        ) return 6

        if (name.contains("الخامسة") || name.contains("خامسة") || name.contains("الخامس") || name.contains("خامس") ||
            name.contains("5AP", ignoreCase = true) || name.contains("5 AP", ignoreCase = true) ||
            name.contains("5AF", ignoreCase = true) || name.contains("5 AF", ignoreCase = true) ||
            name.contains("5أساس") || name.contains("5 أساس") || name.contains("5اساس") ||
            name.contains("5ème", ignoreCase = true) || name.contains("5eme", ignoreCase = true)
        ) return 5

        if (name.contains("الرابعة") || name.contains("رابعة") || name.contains("الرابع") || name.contains("رابع") ||
            name.contains("4AP", ignoreCase = true) || name.contains("4 AP", ignoreCase = true) ||
            name.contains("4AF", ignoreCase = true) || name.contains("4 AF", ignoreCase = true) ||
            name.contains("4أساس") || name.contains("4 أساس") || name.contains("4اساس") ||
            name.contains("4ème", ignoreCase = true) || name.contains("4eme", ignoreCase = true)
        ) return 4

        if (name.contains("الثالثة") || name.contains("ثالثة") || name.contains("الثالث") || name.contains("ثالث") ||
            name.contains("3AP", ignoreCase = true) || name.contains("3 AP", ignoreCase = true) ||
            name.contains("3AF", ignoreCase = true) || name.contains("3 AF", ignoreCase = true) ||
            name.contains("3أساس") || name.contains("3 أساس") || name.contains("3اساس") ||
            name.contains("3ème", ignoreCase = true) || name.contains("3eme", ignoreCase = true)
        ) return 3

        if (name.contains("الثانية") || name.contains("ثانية") || name.contains("الثاني") || name.contains("ثاني") ||
            name.contains("2AP", ignoreCase = true) || name.contains("2 AP", ignoreCase = true) ||
            name.contains("2AF", ignoreCase = true) || name.contains("2 AF", ignoreCase = true) ||
            name.contains("2أساس") || name.contains("2 أساس") || name.contains("2اساس") ||
            name.contains("2ème", ignoreCase = true) || name.contains("2eme", ignoreCase = true)
        ) return 2

        if (name.contains("الأولى") || name.contains("الاولى") || name.contains("أولى") || name.contains("اولى") ||
            name.contains("الأول") || name.contains("الاول") || name.contains("أول") || name.contains("اول") ||
            name.contains("1AP", ignoreCase = true) || name.contains("1 AP", ignoreCase = true) ||
            name.contains("1AF", ignoreCase = true) || name.contains("1 AF", ignoreCase = true) ||
            name.contains("1أساس") || name.contains("1 أساس") || name.contains("1اساس") ||
            name.contains("1ère", ignoreCase = true) || name.contains("1ere", ignoreCase = true)
        ) return 1

        // 2. Contextual Prefix + Digit
        val patterns = listOf(
            6 to Regex("""(?:سنة|س|صف|مستوى|المستوى|قسم|القسم|السنة|أساسي|ابتدائي|fondamentale|af|ap|classe|niveau)\s*[:\-_/]?\s*6""", RegexOption.IGNORE_CASE),
            5 to Regex("""(?:سنة|س|صف|مستوى|المستوى|قسم|القسم|السنة|أساسي|ابتدائي|fondamentale|af|ap|classe|niveau)\s*[:\-_/]?\s*5""", RegexOption.IGNORE_CASE),
            4 to Regex("""(?:سنة|س|صف|مستوى|المستوى|قسم|القسم|السنة|أساسي|ابتدائي|fondamentale|af|ap|classe|niveau)\s*[:\-_/]?\s*4""", RegexOption.IGNORE_CASE),
            3 to Regex("""(?:سنة|س|صف|مستوى|المستوى|قسم|القسم|السنة|أساسي|ابتدائي|fondamentale|af|ap|classe|niveau)\s*[:\-_/]?\s*3""", RegexOption.IGNORE_CASE),
            2 to Regex("""(?:سنة|س|صف|مستوى|المستوى|قسم|القسم|السنة|أساسي|ابتدائي|fondamentale|af|ap|classe|niveau)\s*[:\-_/]?\s*2""", RegexOption.IGNORE_CASE),
            1 to Regex("""(?:سنة|س|صف|مستوى|المستوى|قسم|القسم|السنة|أساسي|ابتدائي|fondamentale|af|ap|classe|niveau)\s*[:\-_/]?\s*1""", RegexOption.IGNORE_CASE),
            6 to Regex("""\b6\s*(?:[a-zA-Z]|أ|ب|ج|د|هـ|/|-|\b)"""),
            5 to Regex("""\b5\s*(?:[a-zA-Z]|أ|ب|ج|د|هـ|/|-|\b)"""),
            4 to Regex("""\b4\s*(?:[a-zA-Z]|أ|ب|ج|د|هـ|/|-|\b)"""),
            3 to Regex("""\b3\s*(?:[a-zA-Z]|أ|ب|ج|د|هـ|/|-|\b)"""),
            2 to Regex("""\b2\s*(?:[a-zA-Z]|أ|ب|ج|د|هـ|/|-|\b)"""),
            1 to Regex("""\b1\s*(?:[a-zA-Z]|أ|ب|ج|د|هـ|/|-|\b)""")
        )

        for ((lvl, pat) in patterns) {
            if (pat.containsMatchIn(name)) {
                return lvl
            }
        }

        return 0
    }

    fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "السنة الأولى"
            2 -> "السنة الثانية"
            3 -> "السنة الثالثة"
            4 -> "السنة الرابعة"
            5 -> "السنة الخامسة"
            6 -> "السنة السادسة"
            else -> "مستوى غير محدد"
        }
    }

    fun getLessonsForLevel(level: Int): List<AnnualLessonItem> {
        return allLessons.filter { it.level == level }
    }

    fun getSubjectsForLevel(level: Int): List<String> {
        return allLessons
            .filter { it.level == level && it.isTeachingLesson() }
            .map { it.subject }
            .distinct()
    }

    fun getAllSubjectsForLevel(level: Int): List<String> {
        return allLessons
            .filter { it.level == level }
            .map { it.subject }
            .distinct()
    }

    fun getWeeksForLevel(level: Int): List<Int> {
        return (1..38).toList()
    }

    fun getActualTeachingWeeksList(level: Int): List<Int> {
        val weeks = mutableSetOf<Int>()
        allLessons.filter { it.level == level && it.isTeachingLesson() }.forEach { lesson ->
            for (w in lesson.week..lesson.weekEnd) {
                weeks.add(w)
            }
        }
        return weeks.sorted()
    }

    fun getActualTeachingWeeksCount(level: Int): Int {
        return getActualTeachingWeeksList(level).size
    }

    fun getWeekType(level: Int, week: Int): String {
        val weekLessons = allLessons.filter { it.level == level && week in it.week..it.weekEnd }
        if (weekLessons.isEmpty()) return "TEACHING"
        if (weekLessons.all { it.getCategoryType() == "HOLIDAY" }) return "HOLIDAY"
        if (weekLessons.all { it.getCategoryType() == "EXAM" }) return "EXAM"
        if (weekLessons.all { it.getCategoryType() == "INTEGRATION" }) return "INTEGRATION"
        if (weekLessons.all { it.getCategoryType() == "DIAGNOSTIC" }) return "DIAGNOSTIC"
        if (weekLessons.any { it.isTeachingLesson() }) return "TEACHING"
        return weekLessons.firstOrNull()?.getCategoryType() ?: "TEACHING"
    }

    fun getUnitsForSubject(level: Int, subject: String): List<PlanningUnit> {
        val lessons = allLessons.filter { it.level == level && it.subject == subject }
        return lessons.map { lesson ->
            PlanningUnit(
                id = lesson.id,
                subject = lesson.subject,
                domain = lesson.domain.ifBlank { lesson.title },
                weekStart = lesson.week,
                weekEnd = lesson.weekEnd,
                isFrench = lesson.isFrench,
                type = lesson.type,
                lesson = lesson
            )
        }.sortedBy { it.weekStart }
    }

    fun getTeachingUnitsForSubject(level: Int, subject: String): List<PlanningUnit> {
        return getUnitsForSubject(level, subject).filter { it.isTeachingUnit }
    }

    fun getSubjectsForWeek(level: Int, week: Int): List<String> {
        return allLessons
            .filter { it.level == level && week in it.week..it.weekEnd }
            .map { it.subject }
            .distinct()
    }

    fun getLessonsForWeekAndSubject(level: Int, week: Int, subject: String): List<AnnualLessonItem> {
        return allLessons.filter {
            it.level == level && subject == it.subject && week in it.week..it.weekEnd
        }
    }

    fun searchLessons(level: Int, query: String): List<AnnualLessonItem> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return allLessons.filter {
            it.level == level && (
                it.title.lowercase().contains(q) ||
                it.subject.lowercase().contains(q) ||
                it.domain.lowercase().contains(q) ||
                it.skills.lowercase().contains(q) ||
                it.competency.lowercase().contains(q)
            )
        }
    }

    // Comprehensive official primary education curriculum dataset for all 5 levels (38 weeks each)
    private val allLessons: List<AnnualLessonItem> = listOf(
        AnnualLessonItem(
            id = "l1_w1_diag",
            level = 1, week = 1, weekEnd = 1,
            subject = "التهيئة والتقويم التشخيصي",
            domain = "تشخيص المكتسبات والتهيئة",
            title = "استقبال التلاميذ والتهيئة النفسية والاجتماعية والتقويم التشخيصي لمكتسبات الروضة والتعليم الأولي",
            competency = "التهيئة النفسية للتعود على الفضاء المدرسي وبناء علاقة ثقة مع المعلم والزملاء.",
            skills = "التقييم الذاتي والتشخيصي وتحديد مكامن الدعم الأولي.",
            activities = "شبكات تقويم تشخيصي، بطاقات تعارف، تمارين على الألواح وقراءات استطلاعية."
        ),
        AnnualLessonItem(
            id = "spec_w12_l1",
            level = 1, week = 12, weekEnd = 12,
            subject = "تقويم وإدماج الفصل الأول",
            domain = "حصيلة التعلمات والتقويم المرحلي",
            title = "أسبوع التقويم والإدماج والمعالجة البيداغوجية لحصيلة تعلمات الفصل الأول (السنة 1)",
            competency = "تشخيص مكتسبات الفصل الأول ومعالجة الثغرات والتعثرات لدى المتعلمين قبل امتحانات الفصل الأول.",
            skills = "إدماج المكتسبات المعرفية والمهارية، والتقييم الذاتي ومعالجة الصعوبات.",
            activities = "وضعيات إدماجية تقويمية، تمارين علاجية مستهدفة، وشبكات رصد التعثرات."
        ),
        AnnualLessonItem(
            id = "spec_w13_l1",
            level = 1, week = 13, weekEnd = 13,
            subject = "اختبارات وعطلة الفصل الأول",
            domain = "التقويم الختامي والعطلة المدرسية",
            title = "امتحانات وتقويم الفصل الأول وعطلة منتصف السنة الدراسية",
            competency = "إجراء امتحانات الفصل الأول وحساب المعدلات وتوزيع كشوف الدرجات والاستفادة من العطلة المدرسية.",
            skills = "إجراء الاختبارات الرسمية في ظروف تربوية محكمة وتحرير النتائج.",
            activities = "أوراق امتحانات الفصل الأول، التصحيح الجماعي، حساب المعدلات وكشوف النقاط."
        ),
        AnnualLessonItem(
            id = "spec_w24_l1",
            level = 1, week = 24, weekEnd = 24,
            subject = "تقويم وإدماج الفصل الثاني",
            domain = "حصيلة التعلمات والتقويم المرحلي",
            title = "أسبوع التقويم والإدماج والمعالجة البيداغوجية لحصيلة تعلمات الفصل الثاني (السنة 1)",
            competency = "تشخيص مكتسبات الفصل الثاني ومعالجة الثغرات والتعثرات وتثبيت الكفايات الأساسية.",
            skills = "الربط بين تعلمات الفصلين الأول والثاني ومعالجة الفروق الفردية بين المتعلمين.",
            activities = "أنشطة إدماج مركبة، حصص دعم وتثبيت بيداغوجي، وشبكات الملاحظة المستمرة."
        ),
        AnnualLessonItem(
            id = "spec_w25_l1",
            level = 1, week = 25, weekEnd = 25,
            subject = "اختبارات وعطلة الفصل الثاني",
            domain = "التقويم الختامي والعطلة المدرسية",
            title = "امتحانات وتقويم الفصل الثاني وعطلة الفصل الثاني المدرسية",
            competency = "إجراء اختبارات الفصل الثاني ورصد الحصيلة الدورية وإعداد كشوف الدرجات.",
            skills = "التقويم الموضوعي للتحصيل الدراسي وضبط سجلات التقويم.",
            activities = "أوراق الاختبارات الموحدة، التصحيح والتدقيق، ورصد النتائج على كشوف الدرجات."
        ),
        AnnualLessonItem(
            id = "spec_w36_l1",
            level = 1, week = 36, weekEnd = 36,
            subject = "تقويم شامل وإدماج نهاية السنة",
            domain = "الحصيلة السنوية الشاملة والإدماج النهائي",
            title = "أسبوع التقويم الشامل والإدماج والمعالجة البيداغوجية الختامية لتعلمات السنة الدراسية (السنة 1)",
            competency = "إدماج الكفايات الختامية السنوية ومعالجة التعثرات المتبقية وتهيئة التلاميذ للامتحانات النهائية.",
            skills = "استحضار كافة الكفايات الممتدة والقدرة على حل الوضعيات الإدماجية الشاملة.",
            activities = "وضعيات تقويمية شاملة، حصص مراجعة مكثفة، وتدريبات على نماذج الاختبارات النهائية."
        ),
        AnnualLessonItem(
            id = "spec_w37_l1",
            level = 1, week = 37, weekEnd = 37,
            subject = "الامتحانات النهائية والتجاوز",
            domain = "التقويم الإشهادي والنهائي",
            title = "الامتحانات النهائية الموحدة وامتحان التجاوز إلى المستوى الموالي",
            competency = "إجراء الامتحانات الختامية وحساب المعدلات السنوية العامة وتحديد التجاوز والنجاح.",
            skills = "اجتياز الاختبارات الرسمية بدقة وثقة وفق المعايير البيداغوجية المعتمدة.",
            activities = "مواضيع الامتحانات النهائية، لجان التصحيح، ومحاضر المراقبة."
        ),
        AnnualLessonItem(
            id = "spec_w38_l1",
            level = 1, week = 38, weekEnd = 38,
            subject = "الفرز والمداولات واختتام السنة",
            domain = "اختتام السنة الدراسية والنتائج",
            title = "فرز النتائج والمداولات الرسمية وتوزيع الجوائز وكشوف الدرجات واختتام السنة",
            competency = "إعلان النتائج النهائية وتكريم المتفوقين وتسليم الكشوف والشهادات للأولياء والتلاميذ.",
            skills = "المداولات البيداغوجية، حصر نسب النجاح والتجاوز، وتوثيق السجلات المدرسية.",
            activities = "حفل الاختتام المدرسي، توزيع الجوائز والشهادات، وأرشفة السجلات الرسمية."
        ),
        AnnualLessonItem(
            id = "l2_w1_diag",
            level = 2, week = 1, weekEnd = 1,
            subject = "التهيئة والتقويم التشخيصي",
            domain = "تشخيص المكتسبات والتهيئة",
            title = "تقويم مكتسبات السنة الأولى في القراءة والحروف والعمليات الحسابية البسيطة والتربية الإسلامية",
            competency = "تشخيص مدى تحكم المتعلمين في مهارات القراءة والكتابة والجمع والطرح البسيط.",
            skills = "التقييم الذاتي والتشخيصي وتحديد مكامن الدعم الأولي.",
            activities = "شبكات تقويم تشخيصي، بطاقات تعارف، تمارين على الألواح وقراءات استطلاعية."
        ),
        AnnualLessonItem(
            id = "spec_w12_l2",
            level = 2, week = 12, weekEnd = 12,
            subject = "تقويم وإدماج الفصل الأول",
            domain = "حصيلة التعلمات والتقويم المرحلي",
            title = "أسبوع التقويم والإدماج والمعالجة البيداغوجية لحصيلة تعلمات الفصل الأول (السنة 2)",
            competency = "تشخيص مكتسبات الفصل الأول ومعالجة الثغرات والتعثرات لدى المتعلمين قبل امتحانات الفصل الأول.",
            skills = "إدماج المكتسبات المعرفية والمهارية، والتقييم الذاتي ومعالجة الصعوبات.",
            activities = "وضعيات إدماجية تقويمية، تمارين علاجية مستهدفة، وشبكات رصد التعثرات."
        ),
        AnnualLessonItem(
            id = "spec_w13_l2",
            level = 2, week = 13, weekEnd = 13,
            subject = "اختبارات وعطلة الفصل الأول",
            domain = "التقويم الختامي والعطلة المدرسية",
            title = "امتحانات وتقويم الفصل الأول وعطلة منتصف السنة الدراسية",
            competency = "إجراء امتحانات الفصل الأول وحساب المعدلات وتوزيع كشوف الدرجات والاستفادة من العطلة المدرسية.",
            skills = "إجراء الاختبارات الرسمية في ظروف تربوية محكمة وتحرير النتائج.",
            activities = "أوراق امتحانات الفصل الأول، التصحيح الجماعي، حساب المعدلات وكشوف النقاط."
        ),
        AnnualLessonItem(
            id = "spec_w24_l2",
            level = 2, week = 24, weekEnd = 24,
            subject = "تقويم وإدماج الفصل الثاني",
            domain = "حصيلة التعلمات والتقويم المرحلي",
            title = "أسبوع التقويم والإدماج والمعالجة البيداغوجية لحصيلة تعلمات الفصل الثاني (السنة 2)",
            competency = "تشخيص مكتسبات الفصل الثاني ومعالجة الثغرات والتعثرات وتثبيت الكفايات الأساسية.",
            skills = "الربط بين تعلمات الفصلين الأول والثاني ومعالجة الفروق الفردية بين المتعلمين.",
            activities = "أنشطة إدماج مركبة، حصص دعم وتثبيت بيداغوجي، وشبكات الملاحظة المستمرة."
        ),
        AnnualLessonItem(
            id = "spec_w25_l2",
            level = 2, week = 25, weekEnd = 25,
            subject = "اختبارات وعطلة الفصل الثاني",
            domain = "التقويم الختامي والعطلة المدرسية",
            title = "امتحانات وتقويم الفصل الثاني وعطلة الفصل الثاني المدرسية",
            competency = "إجراء اختبارات الفصل الثاني ورصد الحصيلة الدورية وإعداد كشوف الدرجات.",
            skills = "التقويم الموضوعي للتحصيل الدراسي وضبط سجلات التقويم.",
            activities = "أوراق الاختبارات الموحدة، التصحيح والتدقيق، ورصد النتائج على كشوف الدرجات."
        ),
        AnnualLessonItem(
            id = "spec_w36_l2",
            level = 2, week = 36, weekEnd = 36,
            subject = "تقويم شامل وإدماج نهاية السنة",
            domain = "الحصيلة السنوية الشاملة والإدماج النهائي",
            title = "أسبوع التقويم الشامل والإدماج والمعالجة البيداغوجية الختامية لتعلمات السنة الدراسية (السنة 2)",
            competency = "إدماج الكفايات الختامية السنوية ومعالجة التعثرات المتبقية وتهيئة التلاميذ للامتحانات النهائية.",
            skills = "استحضار كافة الكفايات الممتدة والقدرة على حل الوضعيات الإدماجية الشاملة.",
            activities = "وضعيات تقويمية شاملة، حصص مراجعة مكثفة، وتدريبات على نماذج الاختبارات النهائية."
        ),
        AnnualLessonItem(
            id = "spec_w37_l2",
            level = 2, week = 37, weekEnd = 37,
            subject = "الامتحانات النهائية والتجاوز",
            domain = "التقويم الإشهادي والنهائي",
            title = "الامتحانات النهائية الموحدة وامتحان التجاوز إلى المستوى الموالي",
            competency = "إجراء الامتحانات الختامية وحساب المعدلات السنوية العامة وتحديد التجاوز والنجاح.",
            skills = "اجتياز الاختبارات الرسمية بدقة وثقة وفق المعايير البيداغوجية المعتمدة.",
            activities = "مواضيع الامتحانات النهائية، لجان التصحيح، ومحاضر المراقبة."
        ),
        AnnualLessonItem(
            id = "spec_w38_l2",
            level = 2, week = 38, weekEnd = 38,
            subject = "الفرز والمداولات واختتام السنة",
            domain = "اختتام السنة الدراسية والنتائج",
            title = "فرز النتائج والمداولات الرسمية وتوزيع الجوائز وكشوف الدرجات واختتام السنة",
            competency = "إعلان النتائج النهائية وتكريم المتفوقين وتسليم الكشوف والشهادات للأولياء والتلاميذ.",
            skills = "المداولات البيداغوجية، حصر نسب النجاح والتجاوز، وتوثيق السجلات المدرسية.",
            activities = "حفل الاختتام المدرسي، توزيع الجوائز والشهادات، وأرشفة السجلات الرسمية."
        ),
        AnnualLessonItem(
            id = "l3_w1_diag",
            level = 3, week = 1, weekEnd = 1,
            subject = "التهيئة والتقويم التشخيصي",
            domain = "تشخيص المكتسبات والتهيئة",
            title = "تقويم مكتسبات السنة الثانية في اللغة والرياضيات والعلوم وتهيئة التلاميذ للمواد الجديدة (التاريخ، الجغرافيا، الفرنسية)",
            competency = "تشخيص الحصيلة التعلمية السابقة وتهيئة التلاميذ للمواد واللغات الجديدة.",
            skills = "التقييم الذاتي والتشخيصي وتحديد مكامن الدعم الأولي.",
            activities = "شبكات تقويم تشخيصي، بطاقات تعارف، تمارين على الألواح وقراءات استطلاعية."
        ),
        AnnualLessonItem(
            id = "spec_w12_l3",
            level = 3, week = 12, weekEnd = 12,
            subject = "تقويم وإدماج الفصل الأول",
            domain = "حصيلة التعلمات والتقويم المرحلي",
            title = "أسبوع التقويم والإدماج والمعالجة البيداغوجية لحصيلة تعلمات الفصل الأول (السنة 3)",
            competency = "تشخيص مكتسبات الفصل الأول ومعالجة الثغرات والتعثرات لدى المتعلمين قبل امتحانات الفصل الأول.",
            skills = "إدماج المكتسبات المعرفية والمهارية، والتقييم الذاتي ومعالجة الصعوبات.",
            activities = "وضعيات إدماجية تقويمية، تمارين علاجية مستهدفة، وشبكات رصد التعثرات."
        ),
        AnnualLessonItem(
            id = "spec_w13_l3",
            level = 3, week = 13, weekEnd = 13,
            subject = "اختبارات وعطلة الفصل الأول",
            domain = "التقويم الختامي والعطلة المدرسية",
            title = "امتحانات وتقويم الفصل الأول وعطلة منتصف السنة الدراسية",
            competency = "إجراء امتحانات الفصل الأول وحساب المعدلات وتوزيع كشوف الدرجات والاستفادة من العطلة المدرسية.",
            skills = "إجراء الاختبارات الرسمية في ظروف تربوية محكمة وتحرير النتائج.",
            activities = "أوراق امتحانات الفصل الأول، التصحيح الجماعي، حساب المعدلات وكشوف النقاط."
        ),
        AnnualLessonItem(
            id = "spec_w24_l3",
            level = 3, week = 24, weekEnd = 24,
            subject = "تقويم وإدماج الفصل الثاني",
            domain = "حصيلة التعلمات والتقويم المرحلي",
            title = "أسبوع التقويم والإدماج والمعالجة البيداغوجية لحصيلة تعلمات الفصل الثاني (السنة 3)",
            competency = "تشخيص مكتسبات الفصل الثاني ومعالجة الثغرات والتعثرات وتثبيت الكفايات الأساسية.",
            skills = "الربط بين تعلمات الفصلين الأول والثاني ومعالجة الفروق الفردية بين المتعلمين.",
            activities = "أنشطة إدماج مركبة، حصص دعم وتثبيت بيداغوجي، وشبكات الملاحظة المستمرة."
        ),
        AnnualLessonItem(
            id = "spec_w25_l3",
            level = 3, week = 25, weekEnd = 25,
            subject = "اختبارات وعطلة الفصل الثاني",
            domain = "التقويم الختامي والعطلة المدرسية",
            title = "امتحانات وتقويم الفصل الثاني وعطلة الفصل الثاني المدرسية",
            competency = "إجراء اختبارات الفصل الثاني ورصد الحصيلة الدورية وإعداد كشوف الدرجات.",
            skills = "التقويم الموضوعي للتحصيل الدراسي وضبط سجلات التقويم.",
            activities = "أوراق الاختبارات الموحدة، التصحيح والتدقيق، ورصد النتائج على كشوف الدرجات."
        ),
        AnnualLessonItem(
            id = "spec_w36_l3",
            level = 3, week = 36, weekEnd = 36,
            subject = "تقويم شامل وإدماج نهاية السنة",
            domain = "الحصيلة السنوية الشاملة والإدماج النهائي",
            title = "أسبوع التقويم الشامل والإدماج والمعالجة البيداغوجية الختامية لتعلمات السنة الدراسية (السنة 3)",
            competency = "إدماج الكفايات الختامية السنوية ومعالجة التعثرات المتبقية وتهيئة التلاميذ للامتحانات النهائية.",
            skills = "استحضار كافة الكفايات الممتدة والقدرة على حل الوضعيات الإدماجية الشاملة.",
            activities = "وضعيات تقويمية شاملة، حصص مراجعة مكثفة، وتدريبات على نماذج الاختبارات النهائية."
        ),
        AnnualLessonItem(
            id = "spec_w37_l3",
            level = 3, week = 37, weekEnd = 37,
            subject = "الامتحانات النهائية والتجاوز",
            domain = "التقويم الإشهادي والنهائي",
            title = "الامتحانات النهائية الموحدة وامتحان التجاوز إلى المستوى الموالي",
            competency = "إجراء الامتحانات الختامية وحساب المعدلات السنوية العامة وتحديد التجاوز والنجاح.",
            skills = "اجتياز الاختبارات الرسمية بدقة وثقة وفق المعايير البيداغوجية المعتمدة.",
            activities = "مواضيع الامتحانات النهائية، لجان التصحيح، ومحاضر المراقبة."
        ),
        AnnualLessonItem(
            id = "spec_w38_l3",
            level = 3, week = 38, weekEnd = 38,
            subject = "الفرز والمداولات واختتام السنة",
            domain = "اختتام السنة الدراسية والنتائج",
            title = "فرز النتائج والمداولات الرسمية وتوزيع الجوائز وكشوف الدرجات واختتام السنة",
            competency = "إعلان النتائج النهائية وتكريم المتفوقين وتسليم الكشوف والشهادات للأولياء والتلاميذ.",
            skills = "المداولات البيداغوجية، حصر نسب النجاح والتجاوز، وتوثيق السجلات المدرسية.",
            activities = "حفل الاختتام المدرسي، توزيع الجوائز والشهادات، وأرشفة السجلات الرسمية."
        ),
        AnnualLessonItem(
            id = "l4_w1_diag",
            level = 4, week = 1, weekEnd = 1,
            subject = "التهيئة والتقويم التشخيصي",
            domain = "تشخيص المكتسبات والتهيئة",
            title = "تقويم مكتسبات السنة الثالثة في القراءة والنحو والعمليات الحسابية واللغة الفرنسية والعلوم والتاريخ والجغرافيا",
            competency = "تشخيص مكتسبات التلميذ في القراءة والنحو والعمليات الحسابية والفرنسية والعلوم.",
            skills = "التقييم الذاتي والتشخيصي وتحديد مكامن الدعم الأولي.",
            activities = "شبكات تقويم تشخيصي، بطاقات تعارف، تمارين على الألواح وقراءات استطلاعية."
        ),
        AnnualLessonItem(
            id = "spec_w12_l4",
            level = 4, week = 12, weekEnd = 12,
            subject = "تقويم وإدماج الفصل الأول",
            domain = "حصيلة التعلمات والتقويم المرحلي",
            title = "أسبوع التقويم والإدماج والمعالجة البيداغوجية لحصيلة تعلمات الفصل الأول (السنة 4)",
            competency = "تشخيص مكتسبات الفصل الأول ومعالجة الثغرات والتعثرات لدى المتعلمين قبل امتحانات الفصل الأول.",
            skills = "إدماج المكتسبات المعرفية والمهارية، والتقييم الذاتي ومعالجة الصعوبات.",
            activities = "وضعيات إدماجية تقويمية، تمارين علاجية مستهدفة، وشبكات رصد التعثرات."
        ),
        AnnualLessonItem(
            id = "spec_w13_l4",
            level = 4, week = 13, weekEnd = 13,
            subject = "اختبارات وعطلة الفصل الأول",
            domain = "التقويم الختامي والعطلة المدرسية",
            title = "امتحانات وتقويم الفصل الأول وعطلة منتصف السنة الدراسية",
            competency = "إجراء امتحانات الفصل الأول وحساب المعدلات وتوزيع كشوف الدرجات والاستفادة من العطلة المدرسية.",
            skills = "إجراء الاختبارات الرسمية في ظروف تربوية محكمة وتحرير النتائج.",
            activities = "أوراق امتحانات الفصل الأول، التصحيح الجماعي، حساب المعدلات وكشوف النقاط."
        ),
        AnnualLessonItem(
            id = "spec_w24_l4",
            level = 4, week = 24, weekEnd = 24,
            subject = "تقويم وإدماج الفصل الثاني",
            domain = "حصيلة التعلمات والتقويم المرحلي",
            title = "أسبوع التقويم والإدماج والمعالجة البيداغوجية لحصيلة تعلمات الفصل الثاني (السنة 4)",
            competency = "تشخيص مكتسبات الفصل الثاني ومعالجة الثغرات والتعثرات وتثبيت الكفايات الأساسية.",
            skills = "الربط بين تعلمات الفصلين الأول والثاني ومعالجة الفروق الفردية بين المتعلمين.",
            activities = "أنشطة إدماج مركبة، حصص دعم وتثبيت بيداغوجي، وشبكات الملاحظة المستمرة."
        ),
        AnnualLessonItem(
            id = "spec_w25_l4",
            level = 4, week = 25, weekEnd = 25,
            subject = "اختبارات وعطلة الفصل الثاني",
            domain = "التقويم الختامي والعطلة المدرسية",
            title = "امتحانات وتقويم الفصل الثاني وعطلة الفصل الثاني المدرسية",
            competency = "إجراء اختبارات الفصل الثاني ورصد الحصيلة الدورية وإعداد كشوف الدرجات.",
            skills = "التقويم الموضوعي للتحصيل الدراسي وضبط سجلات التقويم.",
            activities = "أوراق الاختبارات الموحدة، التصحيح والتدقيق، ورصد النتائج على كشوف الدرجات."
        ),
        AnnualLessonItem(
            id = "spec_w36_l4",
            level = 4, week = 36, weekEnd = 36,
            subject = "تقويم شامل وإدماج نهاية السنة",
            domain = "الحصيلة السنوية الشاملة والإدماج النهائي",
            title = "أسبوع التقويم الشامل والإدماج والمعالجة البيداغوجية الختامية لتعلمات السنة الدراسية (السنة 4)",
            competency = "إدماج الكفايات الختامية السنوية ومعالجة التعثرات المتبقية وتهيئة التلاميذ للامتحانات النهائية.",
            skills = "استحضار كافة الكفايات الممتدة والقدرة على حل الوضعيات الإدماجية الشاملة.",
            activities = "وضعيات تقويمية شاملة، حصص مراجعة مكثفة، وتدريبات على نماذج الاختبارات النهائية."
        ),
        AnnualLessonItem(
            id = "spec_w37_l4",
            level = 4, week = 37, weekEnd = 37,
            subject = "الامتحانات النهائية والتجاوز",
            domain = "التقويم الإشهادي والنهائي",
            title = "الامتحانات النهائية الموحدة وامتحان التجاوز إلى المستوى الموالي",
            competency = "إجراء الامتحانات الختامية وحساب المعدلات السنوية العامة وتحديد التجاوز والنجاح.",
            skills = "اجتياز الاختبارات الرسمية بدقة وثقة وفق المعايير البيداغوجية المعتمدة.",
            activities = "مواضيع الامتحانات النهائية، لجان التصحيح، ومحاضر المراقبة."
        ),
        AnnualLessonItem(
            id = "spec_w38_l4",
            level = 4, week = 38, weekEnd = 38,
            subject = "الفرز والمداولات واختتام السنة",
            domain = "اختتام السنة الدراسية والنتائج",
            title = "فرز النتائج والمداولات الرسمية وتوزيع الجوائز وكشوف الدرجات واختتام السنة",
            competency = "إعلان النتائج النهائية وتكريم المتفوقين وتسليم الكشوف والشهادات للأولياء والتلاميذ.",
            skills = "المداولات البيداغوجية، حصر نسب النجاح والتجاوز، وتوثيق السجلات المدرسية.",
            activities = "حفل الاختتام المدرسي، توزيع الجوائز والشهادات، وأرشفة السجلات الرسمية."
        ),
        AnnualLessonItem(
            id = "l1_ar_u1",
            level = 1, week = 2, weekEnd = 5,
            subject = "اللغة العربية",
            domain = "الوحدة 1: مدرستي وبيئتي",
            title = "التواصل الشفهي، القراءة (حروف: أ، ب، م، د) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u1",
            level = 1, week = 2, weekEnd = 5,
            subject = "التربية الإسلامية",
            domain = "الوحدة 1: مدرستي وبيئتي",
            title = "سورة الفاتحة + الإيمان بالله تعالى + آداب الاستئذان",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u1",
            level = 1, week = 2, weekEnd = 5,
            subject = "الرياضيات",
            domain = "الوحدة 1: مدرستي وبيئتي",
            title = "الأعداد من 1 إلى 5 والمقارنة (أكبر/أصغر/يساوي)",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u1",
            level = 1, week = 2, weekEnd = 5,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 1: مدرستي وبيئتي",
            title = "حواسي الخمس وأهميتها في استكشاف العالم",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u1",
            level = 1, week = 2, weekEnd = 5,
            subject = "التربية المدنية",
            domain = "الوحدة 1: مدرستي وبيئتي",
            title = "إفشاء التحية والسلام وتنظيم الأدوات المدرسية",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_ar_u2",
            level = 1, week = 6, weekEnd = 9,
            subject = "اللغة العربية",
            domain = "الوحدة 2: أسرتي وبيتي",
            title = "التواصل الشفهي، القراءة (حروف: ر، س، ل، ت) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u2",
            level = 1, week = 6, weekEnd = 9,
            subject = "التربية الإسلامية",
            domain = "الوحدة 2: أسرتي وبيتي",
            title = "سورة الإخلاص والمعوذتان + بر الوالدين",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u2",
            level = 1, week = 6, weekEnd = 9,
            subject = "الرياضيات",
            domain = "الوحدة 2: أسرتي وبيتي",
            title = "الأعداد من 6 إلى 9 والعدد 0 والجمع البسيط",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u2",
            level = 1, week = 6, weekEnd = 9,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 2: أسرتي وبيتي",
            title = "جسمي يتحرك ومفاصل الجسم وصحة الأسنان",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u2",
            level = 1, week = 6, weekEnd = 9,
            subject = "التربية المدنية",
            domain = "الوحدة 2: أسرتي وبيتي",
            title = "احترام المواعيد المدرسية والانضباط في الساحة",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_ar_u3",
            level = 1, week = 10, weekEnd = 11,
            subject = "اللغة العربية",
            domain = "الوحدة 3: حيي ومحيطي",
            title = "التواصل الشفهي، القراءة (حروف: ن، ح، ج، خ) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u3",
            level = 1, week = 10, weekEnd = 11,
            subject = "التربية الإسلامية",
            domain = "الوحدة 3: حيي ومحيطي",
            title = "سورة الكوثر وسورة النصر + الصدق والأمانة",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u3",
            level = 1, week = 10, weekEnd = 11,
            subject = "الرياضيات",
            domain = "الوحدة 3: حيي ومحيطي",
            title = "العدد 10 وكتابة الأعداد وجدول المنازل البسيط",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u3",
            level = 1, week = 10, weekEnd = 11,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 3: حيي ومحيطي",
            title = "التنفس والنمو وتأثير الهواء النقي على الجسم",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u3",
            level = 1, week = 10, weekEnd = 11,
            subject = "التربية المدنية",
            domain = "الوحدة 3: حيي ومحيطي",
            title = "الاحترام المتبادل بين الزملاء ومساعدة المحتاج",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_ar_u4",
            level = 1, week = 14, weekEnd = 17,
            subject = "اللغة العربية",
            domain = "الوحدة 4: الصحة والتغذية",
            title = "التواصل الشفهي، القراءة (حروف: ع، غ، ف، ق) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u4",
            level = 1, week = 14, weekEnd = 17,
            subject = "التربية الإسلامية",
            domain = "الوحدة 4: الصحة والتغذية",
            title = "سورة الفلق وسورة قريش + آداب الطعام والشراب",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u4",
            level = 1, week = 14, weekEnd = 17,
            subject = "الرياضيات",
            domain = "الوحدة 4: الصحة والتغذية",
            title = "الأعداد حتى 19 والجمع والطرح البسيط دون احتفاظ",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u4",
            level = 1, week = 14, weekEnd = 17,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 4: الصحة والتغذية",
            title = "الحيوانات والنباتات في محيطي (التغذية والماء)",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u4",
            level = 1, week = 14, weekEnd = 17,
            subject = "التربية المدنية",
            domain = "الوحدة 4: الصحة والتغذية",
            title = "نظافة الجسم والثياب والمحيط المدرسي السليم",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_ar_u5",
            level = 1, week = 18, weekEnd = 21,
            subject = "اللغة العربية",
            domain = "الوحدة 5: الحيوانات والنباتات",
            title = "التواصل الشفهي، القراءة (حروف: ص، ض، ط، ظ) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u5",
            level = 1, week = 18, weekEnd = 21,
            subject = "التربية الإسلامية",
            domain = "الوحدة 5: الحيوانات والنباتات",
            title = "سورة الماعون + الرفق بالحيوان والمحافظة على النبات",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u5",
            level = 1, week = 18, weekEnd = 21,
            subject = "الرياضيات",
            domain = "الوحدة 5: الحيوانات والنباتات",
            title = "الأعداد حتى 49 والمستقيم والمستطيل والمربع",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u5",
            level = 1, week = 18, weekEnd = 21,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 5: الحيوانات والنباتات",
            title = "حالات الماء واستعمالاته والأجسام العائمة والغاطسة",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u5",
            level = 1, week = 18, weekEnd = 21,
            subject = "التربية المدنية",
            domain = "الوحدة 5: الحيوانات والنباتات",
            title = "علم الجمهورية والنشيد الوطني واحترام الرموز",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_ar_u6",
            level = 1, week = 22, weekEnd = 23,
            subject = "اللغة العربية",
            domain = "الوحدة 6: الألعاب والرياضة",
            title = "التواصل الشفهي، القراءة (حروف: ك، هـ، و، ي) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u6",
            level = 1, week = 22, weekEnd = 23,
            subject = "التربية الإسلامية",
            domain = "الوحدة 6: الألعاب والرياضة",
            title = "سورة العصر وسورة الكافرون + التواضع والمحبة",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u6",
            level = 1, week = 22, weekEnd = 23,
            subject = "الرياضيات",
            domain = "الوحدة 6: الألعاب والرياضة",
            title = "الأعداد حتى 69 والقطع النقدية البسيطة والجمع المكرر",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u6",
            level = 1, week = 22, weekEnd = 23,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 6: الألعاب والرياضة",
            title = "الأدوات البسيطة للقطع والربط وأدوات قياس الزمن",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u6",
            level = 1, week = 22, weekEnd = 23,
            subject = "التربية المدنية",
            domain = "الوحدة 6: الألعاب والرياضة",
            title = "قواعد عبور الشارع والمرور والحذر من أخطار الطريق",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_ar_u7",
            level = 1, week = 26, weekEnd = 29,
            subject = "اللغة العربية",
            domain = "الوحدة 7: الحرف والمهن والرحلات",
            title = "التواصل الشفهي، القراءة (نصوص قرائية وتثبيت التنوين والشدة والمدود) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u7",
            level = 1, week = 26, weekEnd = 29,
            subject = "التربية الإسلامية",
            domain = "الوحدة 7: الحرف والمهن والرحلات",
            title = "سورة المسد وسورة التين + إتقان العمل والأمانة",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u7",
            level = 1, week = 26, weekEnd = 29,
            subject = "الرياضيات",
            domain = "الوحدة 7: الحرف والمهن والرحلات",
            title = "الأعداد حتى 99 والمقارنة والترتيب التصاعدي والتنازلي",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u7",
            level = 1, week = 26, weekEnd = 29,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 7: الحرف والمهن والرحلات",
            title = "الأشياء الحارة والباردة والوقاية من أخطار الكهرباء",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u7",
            level = 1, week = 26, weekEnd = 29,
            subject = "التربية المدنية",
            domain = "الوحدة 7: الحرف والمهن والرحلات",
            title = "المؤسسات والمرافق العمومية والحدائق العامة",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_ar_u8",
            level = 1, week = 30, weekEnd = 33,
            subject = "اللغة العربية",
            domain = "الوحدة 8: الأعياد والمناسبات والتضامن",
            title = "التواصل الشفهي، القراءة (نصوص تعبيرية وقصصية وإملاء الحروف والكلمات) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u8",
            level = 1, week = 30, weekEnd = 33,
            subject = "التربية الإسلامية",
            domain = "الوحدة 8: الأعياد والمناسبات والتضامن",
            title = "سورة القدر وسورة الشرح + صلة الرحم والتضامن",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u8",
            level = 1, week = 30, weekEnd = 33,
            subject = "الرياضيات",
            domain = "الوحدة 8: الأعياد والمناسبات والتضامن",
            title = "الجمع والطرح العمودي وحل مسائل بسيطة من خطوة واحدة",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u8",
            level = 1, week = 30, weekEnd = 33,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 8: الأعياد والمناسبات والتضامن",
            title = "المحافظة على بيئة المدرسة والحي والتطبيقات التقنية",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u8",
            level = 1, week = 30, weekEnd = 33,
            subject = "التربية المدنية",
            domain = "الوحدة 8: الأعياد والمناسبات والتضامن",
            title = "التضامن وحب الوطن والخدمة العامة في المدرسة والحي",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_ar_u9",
            level = 1, week = 34, weekEnd = 35,
            subject = "اللغة العربية",
            domain = "المراجعة والتثبيت الشامل",
            title = "التواصل الشفهي، القراءة (مراجعة شاملة لجميع الحروف والمهارات الإقرائية والكتابية) والخط والتعبير",
            competency = "أن يقرأ ويكتب الحروف والكلمات المحددة ويوظفها شفهياً وكتابياً بطلاقة.",
            skills = "التمييز السمعي والبصري، التركيب، ونطق المقاطع الصوتية بدقة.",
            activities = "مشاهد مصورة، بطاقات الحروف والمقاطع، والألواح وكراس الخط."
        ),
        AnnualLessonItem(
            id = "l1_isl_u9",
            level = 1, week = 34, weekEnd = 35,
            subject = "التربية الإسلامية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة جميع السور القرآنية والآداب المقررة بالسنة الأولى",
            competency = "أن يستظهر السور الكريمة برواية ورش ويتمثل الآداب والأخلاق الإسلامية.",
            skills = "حفظ السور القرآنية، معرفة أحكام الإسلام الأساسية، والتطبيق السلوكي.",
            activities = "المصحف الشريف، الاستماع والترتيل، وتمثيل المواقف الأخلاقية."
        ),
        AnnualLessonItem(
            id = "l1_math_u9",
            level = 1, week = 34, weekEnd = 35,
            subject = "الرياضيات",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للأعداد حتى 99 والعمليات والأشكال الهندسية",
            competency = "أن يتحكم في قراءة وكتابة الأعداد وإجراء العمليات وتوظيف الأشكال الهندسية.",
            skills = "العد والحساب الذهني، الترتيب والمقارنة، واستعمال المسطرة.",
            activities = "صفائح وقطع العد، مجسمات، بطاقات الأرقام وكراس الأنشطة."
        ),
        AnnualLessonItem(
            id = "l1_sci_u9",
            level = 1, week = 34, weekEnd = 35,
            subject = "التربية العلمية والتكنولوجية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للمفاهيم العلمية والصحية والتكنولوجية",
            competency = "أن يكتشف الجسم ومحيطه الطبيعي ويطبق قواعد الصحة والأمان التكنولوجي.",
            skills = "الملاحظة العلمية، المقارنة والاستنتاج، وتطبيق السلوك الوقائي.",
            activities = "أدوات تجريبية بسيطة، صور علمية توضيحية، ومجسمات استكشافية."
        ),
        AnnualLessonItem(
            id = "l1_civ_u9",
            level = 1, week = 34, weekEnd = 35,
            subject = "التربية المدنية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للقواعد السلوكية والرموز الوطنية والمدنية",
            competency = "أن يلتزم بالقواعد السلوكية والقيم الوطنية ومبادئ المواطنة الصالحة.",
            skills = "احترام النظام والرموز الوطنية والتعاون الإيجابي مع الآخرين.",
            activities = "تمثيل أدوار سلوكية، لوحات إرشادية، وترديد النشيد الوطني."
        ),
        AnnualLessonItem(
            id = "l1_art_pe",
            level = 1, week = 2, weekEnd = 35,
            subject = "التربية الفنية والبدنية",
            domain = "التعبير الفني والنشاط البدني",
            title = "الألوان الأساسية والرسم التعبيري + الأناشيد + الألعاب الجماعية والتوازن الحركي",
            competency = "أن ينمي التلميذ التناسق الحركي والذوق الجمالي والتعبير الصوتي والتشكيلي.",
            skills = "التلوين، الإنشاد الإيقاعي، والجري والتوازن والحركات التوافقية.",
            activities = "أوراق رسم وألوان، أناشيد مسبورة، ألعاب جرب وتتابع وصافرة."
        ),
        AnnualLessonItem(
            id = "l2_ar_u1",
            level = 2, week = 2, weekEnd = 5,
            subject = "اللغة العربية",
            domain = "الوحدة 1: العائلة والأقارب",
            title = "القراءة والفهم، الظواهر اللغوية (نصوص قرائية، التاء المربوطة والمفتوحة، أسماء الإشارة (هذا، هذه)) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u1",
            level = 2, week = 2, weekEnd = 5,
            subject = "التربية الإسلامية",
            domain = "الوحدة 1: العائلة والأقارب",
            title = "سورة المسد + التوحيد والإيمان بالرسل + بر الوالدين وصلة الرحم",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u1",
            level = 2, week = 2, weekEnd = 5,
            subject = "الرياضيات",
            domain = "الوحدة 1: العائلة والأقارب",
            title = "الأعداد حتى 199، الجمع بالاحتفاظ، والمستطيل والمربع",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u1",
            level = 2, week = 2, weekEnd = 5,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 1: العائلة والأقارب",
            title = "مظاهر النمو عند الإنسان، نبض القلب والتنفس في الراحة والجهد",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u1",
            level = 2, week = 2, weekEnd = 5,
            subject = "التربية المدنية",
            domain = "الوحدة 1: العائلة والأقارب",
            title = "الحق في التعليم والصحة والاسم العائلي",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_ar_u2",
            level = 2, week = 6, weekEnd = 9,
            subject = "اللغة العربية",
            domain = "الوحدة 2: مدرستي وأصدقائي",
            title = "القراءة والفهم، الظواهر اللغوية (نصوص قرائية، اللام الشمسية واللام القمرية، ضمائر المتكلم والمخاطب) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u2",
            level = 2, week = 6, weekEnd = 9,
            subject = "التربية الإسلامية",
            domain = "الوحدة 2: مدرستي وأصدقائي",
            title = "سورة النصر وسورة القارعة + الوضوء شروطه وكيفيته + الصدق",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u2",
            level = 2, week = 6, weekEnd = 9,
            subject = "الرياضيات",
            domain = "الوحدة 2: مدرستي وأصدقائي",
            title = "الأعداد حتى 299، الطرح بالاستعارة، والمثلث القائم والرسم بالمسطرة",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u2",
            level = 2, week = 6, weekEnd = 9,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 2: مدرستي وأصدقائي",
            title = "حركة الجسم والمفاصل ووقاية الهيكل العظمي والأسنان",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u2",
            level = 2, week = 6, weekEnd = 9,
            subject = "التربية المدنية",
            domain = "الوحدة 2: مدرستي وأصدقائي",
            title = "احترام النظام الداخلي للمدرسة ونظافة القسم والساحة",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_ar_u3",
            level = 2, week = 10, weekEnd = 11,
            subject = "اللغة العربية",
            domain = "الوحدة 3: حيي وقريتي",
            title = "القراءة والفهم، الظواهر اللغوية (نصوص قرائية، حروف الجر (من، إلى، في، على)، الجملة الاسمية البسيطة) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u3",
            level = 2, week = 10, weekEnd = 11,
            subject = "التربية الإسلامية",
            domain = "الوحدة 3: حيي وقريتي",
            title = "سورة العاديات + الصلوات الخمس المفروضة وعدد ركعاتها + الأمانة",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u3",
            level = 2, week = 10, weekEnd = 11,
            subject = "الرياضيات",
            domain = "الوحدة 3: حيي وقريتي",
            title = "الأعداد حتى 499، مقارنة وترتيب الأعداد وجدول المنازل (و، ع، م)",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u3",
            level = 2, week = 10, weekEnd = 11,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 3: حيي وقريتي",
            title = "النبات الأخضر وحاجته للماء والضوء للنمو والإزهار",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u3",
            level = 2, week = 10, weekEnd = 11,
            subject = "التربية المدنية",
            domain = "الوحدة 3: حيي وقريتي",
            title = "المحافظة على المرافق العامة والحدائق والشارع",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_ar_u4",
            level = 2, week = 14, weekEnd = 17,
            subject = "اللغة العربية",
            domain = "الوحدة 4: الصحة والتغذية والرياضة",
            title = "القراءة والفهم، الظواهر اللغوية (نصوص قرائية، الفعل الماضي وتصريفه مع الضمائر، علامات الترقيم) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u4",
            level = 2, week = 14, weekEnd = 17,
            subject = "التربية الإسلامية",
            domain = "الوحدة 4: الصحة والتغذية والرياضة",
            title = "سورة الزلزلة + صلاة الجمعة وآداب المسجد + النظافة والطهارة",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u4",
            level = 2, week = 14, weekEnd = 17,
            subject = "الرياضيات",
            domain = "الوحدة 4: الصحة والتغذية والرياضة",
            title = "الأعداد حتى 699، مفهوم الضرب وبناء جداول الضرب لـ 2 و 3 و 4",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u4",
            level = 2, week = 14, weekEnd = 17,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 4: الصحة والتغذية والرياضة",
            title = "حالات المادة (الصلبة والسائلة) وتحولات الماء بفعل الحرارة",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u4",
            level = 2, week = 14, weekEnd = 17,
            subject = "التربية المدنية",
            domain = "الوحدة 4: الصحة والتغذية والرياضة",
            title = "آداب الحوار والتحدث بلباقة مع المعلم والزملاء",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_ar_u5",
            level = 2, week = 18, weekEnd = 21,
            subject = "اللغة العربية",
            domain = "الوحدة 5: البيئة والطبيعة والمياه",
            title = "القراءة والفهم، الظواهر اللغوية (نصوص قرائية، الفعل المضارع وتصريفه، حروف العطف (و، ف، ثم)) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u5",
            level = 2, week = 18, weekEnd = 21,
            subject = "التربية الإسلامية",
            domain = "الوحدة 5: البيئة والطبيعة والمياه",
            title = "سورة البينة + نشأة النبي ﷺ ورعايته للغنم + الرفق بالمخلوقات",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u5",
            level = 2, week = 18, weekEnd = 21,
            subject = "الرياضيات",
            domain = "الوحدة 5: البيئة والطبيعة والمياه",
            title = "الأعداد حتى 899، جداول الضرب لـ 5 و 10، وقياس الأطوال (المتر والسنتيمتر)",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u5",
            level = 2, week = 18, weekEnd = 21,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 5: البيئة والطبيعة والمياه",
            title = "الهواء غاز موجود في محيطنا وخصائصه وكيفية إثبات وجوده",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u5",
            level = 2, week = 18, weekEnd = 21,
            subject = "التربية المدنية",
            domain = "الوحدة 5: البيئة والطبيعة والمياه",
            title = "المحافظة على الماء الصالح للشرب وحماية الشجرة",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_ar_u6",
            level = 2, week = 22, weekEnd = 23,
            subject = "اللغة العربية",
            domain = "الوحدة 6: الحرف والمهن والإنتاج",
            title = "القراءة والفهم، الظواهر اللغوية (نصوص قرائية، الجملة الفعلية الفاعل، التنوين بأنواعه (ضم، فتح، كسر)) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u6",
            level = 2, week = 22, weekEnd = 23,
            subject = "التربية الإسلامية",
            domain = "الوحدة 6: الحرف والمهن والإنتاج",
            title = "سورة القدر + الصلاة على النبي ﷺ وفضلها + إتقان العمل والكسب الحلال",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u6",
            level = 2, week = 22, weekEnd = 23,
            subject = "الرياضيات",
            domain = "الوحدة 6: الحرف والمهن والإنتاج",
            title = "الأعداد حتى 999، الضرب في عدد برقم واحد بالاحتفاظ، والمكعب ومتوازي المستطيلات",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u6",
            level = 2, week = 22, weekEnd = 23,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 6: الحرف والمهن والإنتاج",
            title = "الدارة الكهربائية البسيطة (المصباح، البطارية، الأسلاك، القاطعة)",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u6",
            level = 2, week = 22, weekEnd = 23,
            subject = "التربية المدنية",
            domain = "الوحدة 6: الحرف والمهن والإنتاج",
            title = "واجب احترام المهن والعمال وحب العمل اليدوي",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_ar_u7",
            level = 2, week = 26, weekEnd = 29,
            subject = "اللغة العربية",
            domain = "الوحدة 7: الأسفار والرحلات والاستكشاف",
            title = "القراءة والفهم، الظواهر اللغوية (نصوص قرائية، الأسماء الموصولة (الذي، التي)، أسلوب الاستفهام والتعجب) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u7",
            level = 2, week = 26, weekEnd = 29,
            subject = "التربية الإسلامية",
            domain = "الوحدة 7: الأسفار والرحلات والاستكشاف",
            title = "سورة العلق (الآيات 1-8) + هجرة النبي ﷺ + التسامح والعفو",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u7",
            level = 2, week = 26, weekEnd = 29,
            subject = "الرياضيات",
            domain = "الوحدة 7: الأسفار والرحلات والاستكشاف",
            title = "قياس الكتل (الكيلوغرام والغرام)، وقراءة الساعة ذات العقارب والرقمية",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u7",
            level = 2, week = 26, weekEnd = 29,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 7: الأسفار والرحلات والاستكشاف",
            title = "حركة الشمس والظلال وتحديد أوقات النهار وتعاقب الفصول",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u7",
            level = 2, week = 26, weekEnd = 29,
            subject = "التربية المدنية",
            domain = "الوحدة 7: الأسفار والرحلات والاستكشاف",
            title = "رموز الجمهورية الإسلامية الموريتانية (العلم، الشعار، النشيد)",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_ar_u8",
            level = 2, week = 30, weekEnd = 33,
            subject = "اللغة العربية",
            domain = "الوحدة 8: الألعاب والأعياد والتضامن الوطني",
            title = "القراءة والفهم، الظواهر اللغوية (نصوص قرائية إثرائية، التعبير الكتابي والإنتاج، الإملاء غير المنظور) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u8",
            level = 2, week = 30, weekEnd = 33,
            subject = "التربية الإسلامية",
            domain = "الوحدة 8: الألعاب والأعياد والتضامن الوطني",
            title = "سورة التين وسورة الضحى + حقوق الجار والمحتاجين والتضامن",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u8",
            level = 2, week = 30, weekEnd = 33,
            subject = "الرياضيات",
            domain = "الوحدة 8: الألعاب والأعياد والتضامن الوطني",
            title = "حل وضعيات مشكلة ومسائل حسابية مركبة من خطوتين",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u8",
            level = 2, week = 30, weekEnd = 33,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 8: الألعاب والأعياد والتضامن الوطني",
            title = "الأجهزة والآلات البسيطة وقواعد الأمان المنزلية والمدرسية",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u8",
            level = 2, week = 30, weekEnd = 33,
            subject = "التربية المدنية",
            domain = "الوحدة 8: الألعاب والأعياد والتضامن الوطني",
            title = "التضامن الاجتماعي ومساعدة الأيتام والمسنين في الحي والوطن",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_ar_u9",
            level = 2, week = 34, weekEnd = 35,
            subject = "اللغة العربية",
            domain = "المراجعة والتثبيت الشامل",
            title = "القراءة والفهم، الظواهر اللغوية (مراجعة شاملة لجميع نصوص وقواعد وإملاء السنة الثانية) والإنتاج الكتابي",
            competency = "أن يقرأ نصوصاً مسترسلة بطلاقة ويستوعب معانيها ويوظف القواعد النحوية والإملائية بدقة.",
            skills = "الطلاقة القرائية، تصريف الأفعال، التحليل والتركيب الكتابي السليم.",
            activities = "نصوص كتاب القراءة، بطاقات إملائية، تمارين لغوية، وكراس التعبير."
        ),
        AnnualLessonItem(
            id = "l2_isl_u9",
            level = 2, week = 34, weekEnd = 35,
            subject = "التربية الإسلامية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة لجميع السور الكريمة والأحكام الفقهية والسيرة النبوية",
            competency = "أن يستظهر السور الكريمة ويتقن كيفية الصلاة والوضوء ويتمثل السيرة النبوية العطرة.",
            skills = "الحفظ والترتيل المتقن، أداء الصلوات المفروضة، والتمسك بالخلق الفاضل.",
            activities = "المصحف الشريف، التطبيق العملي للصلاة والوضوء، وسرد السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l2_math_u9",
            level = 2, week = 34, weekEnd = 35,
            subject = "الرياضيات",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للأعداد حتى 999 وجداول الضرب والهندسة والمسائل",
            competency = "أن يتقن الحساب حتى 999 وجداول الضرب وحساب الأطوال والكتل والمجسمات والمسائل.",
            skills = "الوضع العمودي للعمليات، حفظ جداول الضرب، استخدام أدوات القياس وحل المسائل.",
            activities = "صفائح المئات، أشرطة القياس، موازين، بطاقات الجداءات وكراس المسائل."
        ),
        AnnualLessonItem(
            id = "l2_sci_u9",
            level = 2, week = 34, weekEnd = 35,
            subject = "التربية العلمية والتكنولوجية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للمفاهيم العلمية والتكنولوجية المقررة",
            competency = "أن يستكشف جسم الإنسان وعالم النبات والمادة والكهرباء وتفسير الظواهر الطبيعية.",
            skills = "إجراء التجارب، تدوين الملاحظات، وتركيب الدارات الكهربائية البسيطة بأمان.",
            activities = "تجارب علمية، مجسمات ونباتات تجريبية، بطاريات ومصابيح."
        ),
        AnnualLessonItem(
            id = "l2_civ_u9",
            level = 2, week = 34, weekEnd = 35,
            subject = "التربية المدنية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للقيم المدنية والحقوق والواجبات الوطنية",
            competency = "أن يرسخ السلوك المدني القويم ويدرك حقوقه وواجباته ويحترم رموز ومؤسسات الوطن.",
            skills = "ممارسة السلوك الديمقراطي والتعاون والتعبير بأدب عن الرأي.",
            activities = "مشاهد توعوية، لوحات حقوق الطفل، بطاقات المواطنة والتضامن."
        ),
        AnnualLessonItem(
            id = "l2_art_pe",
            level = 2, week = 2, weekEnd = 35,
            subject = "التربية الفنية والبدنية",
            domain = "التربية الفنية والرياضية",
            title = "الأشكال الهندسية في الفن التشكيلي، الإيقاع والأناشيد المدرسية، وألعاب التتابع والجري",
            competency = "تنمية القدرات الحسية والإنشادية والتناسق الحركي والبدني السليم لدى التلميذ.",
            skills = "الرسم بالخامات، الأداء الإيقاعي للأناشيد، والجري والرمي والتوازن.",
            activities = "أقلام ألوان، كراس الرسم، أناشيد مدرسية، وأقماع وملاعب المدرسة."
        ),
        AnnualLessonItem(
            id = "l3_ar_u1",
            level = 3, week = 2, weekEnd = 5,
            subject = "اللغة العربية",
            domain = "الوحدة 1: القيم الإنسانية والحياة المدرسية",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (نصوص قراءة، أقسام الكلام (اسم، فعل، حرف)، التاء المربوطة والمفتوحة) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u1",
            level = 3, week = 2, weekEnd = 5,
            subject = "التربية الإسلامية",
            domain = "الوحدة 1: القيم الإنسانية والحياة المدرسية",
            title = "سورة التين وسورة الشرح + الإيمان بالله وملائكته وكتبه + أركان الصلاة",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u1",
            level = 3, week = 2, weekEnd = 5,
            subject = "الرياضيات",
            domain = "الوحدة 1: القيم الإنسانية والحياة المدرسية",
            title = "الأعداد حتى 9999، تقنيات الجمع والطرح العمودي، والمستقيمات المتعامدة والمتوازية",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u1",
            level = 3, week = 2, weekEnd = 5,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 1: القيم الإنسانية والحياة المدرسية",
            title = "التغذية عند الإنسان: المجموعات الغذائية والهضم والوقاية من سوء التغذية",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u1",
            level = 3, week = 2, weekEnd = 5,
            subject = "التاريخ والجغرافيا",
            domain = "الوحدة 1: القيم الإنسانية والحياة المدرسية",
            title = "التسلسل الزمني، الخط الزمني، ومعالم التاريخ المحلي",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u1",
            level = 3, week = 2, weekEnd = 5,
            subject = "التربية المدنية",
            domain = "الوحدة 1: القيم الإنسانية والحياة المدرسية",
            title = "حقوق الطفل الأساسية (الهوية، الصحة، الحماية)",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u1",
            level = 3, week = 2, weekEnd = 5,
            subject = "Français",
            domain = "الوحدة 1: القيم الإنسانية والحياة المدرسية",
            title = "Projet 1 : Mon école et mes nouveaux amis (Salutations, alphabet, voyelles a, i, o, u, consonnes m, p, t, d, graphisme et premiers mots).",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l3_ar_u2",
            level = 3, week = 6, weekEnd = 9,
            subject = "اللغة العربية",
            domain = "الوحدة 2: الأسرة والتعاون والتضامن",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (نصوص قراءة، الفعل الماضي والمضارع وتصريفهما، الهمزة في أول الكلمة (وصل وقطع)) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u2",
            level = 3, week = 6, weekEnd = 9,
            subject = "التربية الإسلامية",
            domain = "الوحدة 2: الأسرة والتعاون والتضامن",
            title = "سورة الضحى وسورة الليل + الصلاة ومبطلاتها وسننها + بر الوالدين",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u2",
            level = 3, week = 6, weekEnd = 9,
            subject = "الرياضيات",
            domain = "الوحدة 2: الأسرة والتعاون والتضامن",
            title = "الأعداد حتى 99999، الضرب في عدد برقم واحد وبرقمين، والمثلثات الخاصة ورسمها",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u2",
            level = 3, week = 6, weekEnd = 9,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 2: الأسرة والتعاون والتضامن",
            title = "التنفس عند الإنسان: حركة القفص الصدري، تبادل الغازات والمحافظة على الرئتين",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u2",
            level = 3, week = 6, weekEnd = 9,
            subject = "التاريخ والجغرافيا",
            domain = "الوحدة 2: الأسرة والتعاون والتضامن",
            title = "الآثار والمعالم التاريخية الوطنية في موريتانيا",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u2",
            level = 3, week = 6, weekEnd = 9,
            subject = "التربية المدنية",
            domain = "الوحدة 2: الأسرة والتعاون والتضامن",
            title = "الحوار وقبول الرأي الآخر ونبذ العنف المدرسي",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u2",
            level = 3, week = 6, weekEnd = 9,
            subject = "Français",
            domain = "الوحدة 2: الأسرة والتعاون والتضامن",
            title = "Projet 2 : Ma famille et ma maison (Vocabulaire de la famille, sons l, r, f, v, s, z, lecture de phrases simples, articles un/une/le/la).",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l3_ar_u3",
            level = 3, week = 10, weekEnd = 11,
            subject = "اللغة العربية",
            domain = "الوحدة 3: حيي وقريتي والبيئة المحيطة",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (نصوص قراءة، الجملة الاسمية (المبتدأ والخبر)، الشمسية والقمرية وتطبيقات) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u3",
            level = 3, week = 10, weekEnd = 11,
            subject = "التربية الإسلامية",
            domain = "الوحدة 3: حيي وقريتي والبيئة المحيطة",
            title = "سورة الشمس وسورة البلد + مكانة المسجد في الإسلام + الصدق والأمانة",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u3",
            level = 3, week = 10, weekEnd = 11,
            subject = "الرياضيات",
            domain = "الوحدة 3: حيي وقريتي والبيئة المحيطة",
            title = "القسمة الإقليدية البسيطة (مفهوم التوزيع المتساوي)، ورسم الزوايا بالمنقلة والكوس",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u3",
            level = 3, week = 10, weekEnd = 11,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 3: حيي وقريتي والبيئة المحيطة",
            title = "الدوران عند الإنسان: نبض القلب والأوعية الدموية وأهمية ممارسة الرياضة",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u3",
            level = 3, week = 10, weekEnd = 11,
            subject = "التاريخ والجغرافيا",
            domain = "الوحدة 3: حيي وقريتي والبيئة المحيطة",
            title = "الاتجاهات الأربعة وتحديد المواقع بالبوصلة",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u3",
            level = 3, week = 10, weekEnd = 11,
            subject = "التربية المدنية",
            domain = "الوحدة 3: حيي وقريتي والبيئة المحيطة",
            title = "المحافظة على البيئة المدرسية ومصادر المياه",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u3",
            level = 3, week = 10, weekEnd = 11,
            subject = "Français",
            domain = "الوحدة 3: حيي وقريتي والبيئة المحيطة",
            title = "Projet 3 : Mon quartier et mon village (Vocabulaire du quartier, sons b, n, c, g, j, ch, lecture de courts textes illustrés, la phrase simple).",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l3_ar_u4",
            level = 3, week = 14, weekEnd = 17,
            subject = "اللغة العربية",
            domain = "الوحدة 4: الصحة والرياضة والتغذية السليمة",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (نصوص قراءة، الجملة الفعلية (الفعل والفاعل والمفعول به)، الألف اللينة في الحروف) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u4",
            level = 3, week = 14, weekEnd = 17,
            subject = "التربية الإسلامية",
            domain = "الوحدة 4: الصحة والرياضة والتغذية السليمة",
            title = "سورة الفجر (الآيات 1-14) + صلاة الجماعة وفضلها + آداب الزيارة والعيادة",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u4",
            level = 3, week = 14, weekEnd = 17,
            subject = "الرياضيات",
            domain = "الوحدة 4: الصحة والرياضة والتغذية السليمة",
            title = "الكسور البسيطة (النصف، الربع، الثلث، الخمس) والمقارنة والتمثيل على شريط",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u4",
            level = 3, week = 14, weekEnd = 17,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 4: الصحة والرياضة والتغذية السليمة",
            title = "تحولات المادة بفعل الحرارة: الانصهار، التجمد، التبخر، والتكاثف ودورة الماء",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u4",
            level = 3, week = 14, weekEnd = 17,
            subject = "التاريخ والجغرافيا",
            domain = "الوحدة 4: الصحة والرياضة والتغذية السليمة",
            title = "تضاريس موريتانيا: الجبال والهضاب والسهول والكثبان الرملية",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u4",
            level = 3, week = 14, weekEnd = 17,
            subject = "التربية المدنية",
            domain = "الوحدة 4: الصحة والرياضة والتغذية السليمة",
            title = "مؤسسات الدولة الخدمية (المستشفى، البريد، البلدية)",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u4",
            level = 3, week = 14, weekEnd = 17,
            subject = "Français",
            domain = "الوحدة 4: الصحة والرياضة والتغذية السليمة",
            title = "Projet 4 : Les animaux et la nature (Sons on, ou, an, in, le pluriel des noms avec -s, verbes d'action usuels, petite production écrite).",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l3_ar_u5",
            level = 3, week = 18, weekEnd = 21,
            subject = "اللغة العربية",
            domain = "الوحدة 5: الحرف والصناعات التقليدية والفلاحة",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (نصوص قراءة، كان وأخواتها وتأثيرها على الجملة الاسمية، همزة ابن وابنة) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u5",
            level = 3, week = 18, weekEnd = 21,
            subject = "التربية الإسلامية",
            domain = "الوحدة 5: الحرف والصناعات التقليدية والفلاحة",
            title = "سورة الفجر (15-30) وسورة الغاشية + هجرة النبي ﷺ وبناء المسجد + إتقان الصنعة",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u5",
            level = 3, week = 18, weekEnd = 21,
            subject = "الرياضيات",
            domain = "الوحدة 5: الحرف والصناعات التقليدية والفلاحة",
            title = "حساب المحيط للأشكال المستوية (المربع، المستطيل، المثلث)، والتحويل بين وحدات الأطوال",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u5",
            level = 3, week = 18, weekEnd = 21,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 5: الحرف والصناعات التقليدية والفلاحة",
            title = "الميزان واستعماله لقياس الكتل (الكيلوغرام والغرام والأجزاء)",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u5",
            level = 3, week = 18, weekEnd = 21,
            subject = "التاريخ والجغرافيا",
            domain = "الوحدة 5: الحرف والصناعات التقليدية والفلاحة",
            title = "المناخ والفصول والغطاء النباتي في موريتانيا",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u5",
            level = 3, week = 18, weekEnd = 21,
            subject = "التربية المدنية",
            domain = "الوحدة 5: الحرف والصناعات التقليدية والفلاحة",
            title = "الرموز الوطنية الموريتانية والاعتزاز بالانتماء",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u5",
            level = 3, week = 18, weekEnd = 21,
            subject = "Français",
            domain = "الوحدة 5: الحرف والصناعات التقليدية والفلاحة",
            title = "Projet 5 : La fête, les métiers et les jeux (Sons oi, eu, révision des sons complexes, lecture fluide de récits courts, rédaction de 2 phrases autonomes).",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l3_ar_u6",
            level = 3, week = 22, weekEnd = 23,
            subject = "اللغة العربية",
            domain = "الوحدة 6: الأسفار والمغامرات والاكتشافات",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (نصوص قراءة، حروف الجر وحروف العطف ووظائفها التركيبية، الإملاء التطبيقي) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u6",
            level = 3, week = 22, weekEnd = 23,
            subject = "التربية الإسلامية",
            domain = "الوحدة 6: الأسفار والمغامرات والاكتشافات",
            title = "سورة الأعلى + صلاة العيدين والجمعة + التسامح وإصلاح ذات البين",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u6",
            level = 3, week = 22, weekEnd = 23,
            subject = "الرياضيات",
            domain = "الوحدة 6: الأسفار والمغامرات والاكتشافات",
            title = "قياس السعات (اللتر وأجزاؤه ومضاعفاته)، وجداول البيانات والمخططات البسيطة",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u6",
            level = 3, week = 22, weekEnd = 23,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 6: الأسفار والمغامرات والاكتشافات",
            title = "الدارة الكهربائية: المولد، المصباح، القاطعة، النواقل والعوازل الكهربائية",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u6",
            level = 3, week = 22, weekEnd = 23,
            subject = "التاريخ والجغرافيا",
            domain = "الوحدة 6: الأسفار والمغامرات والاكتشافات",
            title = "شبكة المواصلات والموانئ والمطارات الوطنية",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u6",
            level = 3, week = 22, weekEnd = 23,
            subject = "التربية المدنية",
            domain = "الوحدة 6: الأسفار والمغامرات والاكتشافات",
            title = "قواعد السلامة المرورية واحترام إشارات المرور",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u6",
            level = 3, week = 22, weekEnd = 23,
            subject = "Français",
            domain = "الوحدة 6: الأسفار والمغامرات والاكتشافات",
            title = "Projet 6 : Voyages et découvertes (Lecture de contes courts, enrichissement du vocabulaire thématique, consolidation de l'orthographe d'usage).",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l3_ar_u7",
            level = 3, week = 26, weekEnd = 29,
            subject = "اللغة العربية",
            domain = "الوحدة 7: العلوم والتكنولوجيا والاتصال",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (نصوص قراءة، الأسماء الموصولة وأسماء الإشارة وتصريف الأفعال مع كافة الضمائر) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u7",
            level = 3, week = 26, weekEnd = 29,
            subject = "التربية الإسلامية",
            domain = "الوحدة 7: العلوم والتكنولوجيا والاتصال",
            title = "سورة الطارق + أركان الإيمان بالقضاء والقدر + المحافظة على النعم",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u7",
            level = 3, week = 26, weekEnd = 29,
            subject = "الرياضيات",
            domain = "الوحدة 7: العلوم والتكنولوجيا والاتصال",
            title = "التناظر المحوري ورسم نظير شكل على شبكة تربيعية، والضرب في أعداد كبيرة",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u7",
            level = 3, week = 26, weekEnd = 29,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 7: العلوم والتكنولوجيا والاتصال",
            title = "الحركات والمفاصل والروافع البسيطة وقواعد السلامة البدنية والتكنولوجية",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u7",
            level = 3, week = 26, weekEnd = 29,
            subject = "التاريخ والجغرافيا",
            domain = "الوحدة 7: العلوم والتكنولوجيا والاتصال",
            title = "الموارد المائية والسدود والأنهار في موريتانيا",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u7",
            level = 3, week = 26, weekEnd = 29,
            subject = "التربية المدنية",
            domain = "الوحدة 7: العلوم والتكنولوجيا والاتصال",
            title = "المواطنة الفاعلة وحماية الممتلكات العامة",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u7",
            level = 3, week = 26, weekEnd = 29,
            subject = "Français",
            domain = "الوحدة 7: العلوم والتكنولوجيا والاتصال",
            title = "Projet 7 : La science et les technologies modernes (Textes documentaires simples, initiation à la grammaire de texte, production de 3 phrases descriptives).",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l3_ar_u8",
            level = 3, week = 30, weekEnd = 33,
            subject = "اللغة العربية",
            domain = "الوحدة 8: الثقافة والفنون والتراث الموريتاني",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (نصوص قراءة أدبية وتراثية، التعبير الكتابي المتكامل، وتطبيقات نحوية شاملة) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u8",
            level = 3, week = 30, weekEnd = 33,
            subject = "التربية الإسلامية",
            domain = "الوحدة 8: الثقافة والفنون والتراث الموريتاني",
            title = "سورة البروج + فضل تلاوة القرآن الكريم + حب الوطن والتضامن الاجتماعي",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u8",
            level = 3, week = 30, weekEnd = 33,
            subject = "الرياضيات",
            domain = "الوحدة 8: الثقافة والفنون والتراث الموريتاني",
            title = "حل المسائل الرياضية المركبة ذات الخطوات المتعددة واستراتيجيات التفكير",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u8",
            level = 3, week = 30, weekEnd = 33,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 8: الثقافة والفنون والتراث الموريتاني",
            title = "المحافظة على التوازن البيئي والتنوع الحيواني والنباتي في موريتانيا",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u8",
            level = 3, week = 30, weekEnd = 33,
            subject = "التاريخ والجغرافيا",
            domain = "الوحدة 8: الثقافة والفنون والتراث الموريتاني",
            title = "المدن التاريخية الموريتانية (شنقيط، وادان، تيشيت، ولاتة)",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u8",
            level = 3, week = 30, weekEnd = 33,
            subject = "التربية المدنية",
            domain = "الوحدة 8: الثقافة والفنون والتراث الموريتاني",
            title = "الدستور الموريتاني وحقوق المواطن وواجباته",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u8",
            level = 3, week = 30, weekEnd = 33,
            subject = "Français",
            domain = "الوحدة 8: الثقافة والفنون والتراث الموريتاني",
            title = "Projet 8 : Culture, contes et traditions mauritaniennes (Lecture expressive, jeux de rôles en français, et bilan des compétences orales et écrites).",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l3_ar_u9",
            level = 3, week = 34, weekEnd = 35,
            subject = "اللغة العربية",
            domain = "المراجعة والتثبيت الشامل",
            title = "القراءة والتحليل، القواعد النحوية والإملائية (مراجعة شاملة لجميع ظواهر النحو والصرف والإملاء والتعبير بالسنة الثالثة) والتعبير",
            competency = "أن يقرأ نصوصاً أدبية بطلاقة واستيعاب ويوظف القواعد النحوية والإملائية وينتج نصوصاً قصيرة.",
            skills = "التحليل النصي، الضبط بالشكل، التحرير الكتابي المنسجم.",
            activities = "كتاب القراءة، كراس النشاطات، بطاقات الإعراب، وجداول الإملاء."
        ),
        AnnualLessonItem(
            id = "l3_isl_u9",
            level = 3, week = 34, weekEnd = 35,
            subject = "التربية الإسلامية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة لجميع السور الكريمة والأحكام الفقهية والسيرة المقررة",
            competency = "أن يستظهر السور الكريمة برواية ورش، ويفهم أحكام الصلاة والعقيدة ويقتدي بالسيرة النبوية.",
            skills = "الترتيل المتقن، تمييز أركان وسنن ومبطلات الصلاة، والتمسك بالخلق القويم.",
            activities = "المصحف الشريف، التطبيق الفقهي، ولوحات السيرة النبوية."
        ),
        AnnualLessonItem(
            id = "l3_math_u9",
            level = 3, week = 34, weekEnd = 35,
            subject = "الرياضيات",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للحساب والعمليات الأربع والهندسة والمحيطات والمسائل",
            competency = "أن يتحكم في العمليات الحسابية للأعداد الكبيرة، الهندسة، المحيطات، القياس والمسائل.",
            skills = "الحساب الذهني والعمودي، الرسم الهندسي بالكوس والمنقلة، وتحليل المسائل.",
            activities = "جداول المراتب، أدوات الهندسة، أوراق قياس الكتل والسعات."
        ),
        AnnualLessonItem(
            id = "l3_sci_u9",
            level = 3, week = 34, weekEnd = 35,
            subject = "التربية العلمية والتكنولوجية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للمفاهيم العلمية والتجارب الفيزيائية والحيوية",
            competency = "أن يستوعب وظائف أجهزة الجسم، تحولات المادة، الميزان، والدارة الكهربائية.",
            skills = "الملاحظة والتجريب، استنتاج القوانين العلمية، وتطبيق قواعد الوقاية.",
            activities = "أدوات تجريبية، ميزان، دارات كهربائية، كراس الملاحظة العلمية."
        ),
        AnnualLessonItem(
            id = "l3_hg_u9",
            level = 3, week = 34, weekEnd = 35,
            subject = "التاريخ والجغرافيا",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للتاريخ والجغرافيا الموريتانية وأدوات القياس",
            competency = "أن يستخدم الخط الزمني والخريطة لفهم تاريخ وجغرافية موريتانيا وتضاريسها ومناخها.",
            skills = "قراءة الخرائط وتحديد الاتجاهات، رسم الخط الزمني، وتقدير التراث الوطني.",
            activities = "خرائط موريتانيا الطبيعية، بوصلة، صور معالم تاريخية."
        ),
        AnnualLessonItem(
            id = "l3_civ_u9",
            level = 3, week = 34, weekEnd = 35,
            subject = "التربية المدنية",
            domain = "المراجعة والتثبيت الشامل",
            title = "مراجعة شاملة للحقوق والواجبات والمواطنة والرموز الوطنية",
            competency = "أن يمارس حقوقه وواجباته ويلتزم بقيم الحوار والمواطنة الصالحة واحترام الرموز.",
            skills = "المشاركة المدنية الواعية، احترام التنوع الثقافي، والانضباط المؤسسي.",
            activities = "نصوص حقوقية مبسطة، مناظرات صفية، سندات مصورة."
        ),
        AnnualLessonItem(
            id = "l3_fr_u9",
            level = 3, week = 34, weekEnd = 35,
            subject = "Français",
            domain = "المراجعة والتثبيت الشامل",
            title = "Révision annuelle générale de français : alphabet, sons, vocabulaire usuel, lecture fluide et production de courts textes.",
            competency = "Développer la communication orale, la maîtrise phonétique des sons français, la lecture et l'écriture autonome de mots et phrases.",
            skills = "Écoute active, décodage graphie-phonie, lecture courante et production écrite guidée.",
            activities = "Supports imagés, étiquettes-mots, comptines, exercices du manuel et cahier d'activités.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u1",
            level = 4, week = 2, weekEnd = 5,
            subject = "اللغة العربية",
            domain = "الوحدة 1: القيم والأخلاق الإنسانية",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (نصوص قراءة، المبتدأ والخبر، كان وأخواتها، الفاعل ونائب الفاعل، الهمزة المتوسطة على الألف) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u1",
            level = 4, week = 2, weekEnd = 5,
            subject = "التربية الإسلامية",
            domain = "الوحدة 1: القيم والأخلاق الإنسانية",
            title = "سورة الغاشية وسورة الأعلى + عقيدة التوحيد وأركان الإيمان + الصدق والوفاء",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u1",
            level = 4, week = 2, weekEnd = 5,
            subject = "الرياضيات",
            domain = "الوحدة 1: القيم والأخلاق الإنسانية",
            title = "الأعداد حتى الملايين، الجمع والطرح والضرب العمودي للأعداد الكبيرة، والمستقيمات المتعامدة والمتوازية والزوايا",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u1",
            level = 4, week = 2, weekEnd = 5,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 1: القيم والأخلاق الإنسانية",
            title = "الهيكل العظمي عند الإنسان: المفاصل، العظام، الحركة والوقاية من الكسور والإصابات",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u1",
            level = 4, week = 2, weekEnd = 5,
            subject = "التاريخ",
            domain = "الوحدة 1: القيم والأخلاق الإنسانية",
            title = "عصور ما قبل التاريخ والعصور التاريخية القديمة في موريتانيا",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u1",
            level = 4, week = 2, weekEnd = 5,
            subject = "الجغرافيا",
            domain = "الوحدة 1: القيم والأخلاق الإنسانية",
            title = "موقع موريتانيا الجغرافي والفلكي وحدودها البرية والبحرية",
            competency = "موقع موريتانيا الجغرافي والفلكي وحدودها البرية والبحرية",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u1",
            level = 4, week = 2, weekEnd = 5,
            subject = "التربية المدنية",
            domain = "الوحدة 1: القيم والأخلاق الإنسانية",
            title = "الدستور الموريتاني: تعريفه، أهميته، ودوره في حماية الحقوق والحريات",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u1",
            level = 4, week = 2, weekEnd = 5,
            subject = "Français",
            domain = "الوحدة 1: القيم والأخلاق الإنسانية",
            title = "Projet 1 : Découvrir notre environnement et notre patrimoine (Textes narratifs et descriptifs, le nom et les déterminants, l'adjectif qualificatif, le présent de l'indicatif des verbes être, avoir et 1er groupe, rédaction d'un paragraphe court).",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u2",
            level = 4, week = 6, weekEnd = 9,
            subject = "اللغة العربية",
            domain = "الوحدة 2: الرياضة والتسلية والصحة البدنية",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (نصوص قراءة، إن وأخواتها، الفعل الصحيح والمعتل، المفعول به، الهمزة المتوسطة على الواو) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u2",
            level = 4, week = 6, weekEnd = 9,
            subject = "التربية الإسلامية",
            domain = "الوحدة 2: الرياضة والتسلية والصحة البدنية",
            title = "سورة الطارق وسورة البروج + أحكام الصيام وشروطه ومقاصده + حفظ اللسان",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u2",
            level = 4, week = 6, weekEnd = 9,
            subject = "الرياضيات",
            domain = "الوحدة 2: الرياضة والتسلية والصحة البدنية",
            title = "الأعداد العشرية (المفهوم، القراءة والكتابة والتمثيل)، والتحويل بين الكسور والأعداد العشرية",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u2",
            level = 4, week = 6, weekEnd = 9,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 2: الرياضة والتسلية والصحة البدنية",
            title = "العضلات والحركة عند الإنسان: التقلص والتمدد والتناسق العضلي وسلامة الرياضة",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u2",
            level = 4, week = 6, weekEnd = 9,
            subject = "التاريخ",
            domain = "الوحدة 2: الرياضة والتسلية والصحة البدنية",
            title = "تاريخ موريتانيا القديم والحضارات التي تعاقبت على المنطقة",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u2",
            level = 4, week = 6, weekEnd = 9,
            subject = "الجغرافيا",
            domain = "الوحدة 2: الرياضة والتسلية والصحة البدنية",
            title = "تضاريس موريتانيا: الهضاب الصخرية، الكثبان الرملية، والسهول الساحلية",
            competency = "تضاريس موريتانيا: الهضاب الصخرية، الكثبان الرملية، والسهول الساحلية",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u2",
            level = 4, week = 6, weekEnd = 9,
            subject = "التربية المدنية",
            domain = "الوحدة 2: الرياضة والتسلية والصحة البدنية",
            title = "السلطة التشريعية في موريتانيا: الجمعية الوطنية، انتخاب النواب وسن القوانين",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u2",
            level = 4, week = 6, weekEnd = 9,
            subject = "Français",
            domain = "الوحدة 2: الرياضة والتسلية والصحة البدنية",
            title = "Projet 2 : La santé, le sport et la nutrition (Texte explicatif, la phrase déclarative et interrogative, les verbes du 2ème groupe au présent, le féminin des noms et adjectifs, rédaction d'un conseil santé).",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u3",
            level = 4, week = 10, weekEnd = 11,
            subject = "اللغة العربية",
            domain = "الوحدة 3: الهوية والتاريخ والتراث الوطني",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (نصوص قراءة، المفعول المطلق والمفعول لأجله، تصريف الفعل المعتل، الهمزة المتوسطة على النبرة) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u3",
            level = 4, week = 10, weekEnd = 11,
            subject = "التربية الإسلامية",
            domain = "الوحدة 3: الهوية والتاريخ والتراث الوطني",
            title = "سورة الانشقاق وسورة المطففين + أحكام الزكاة ومصارفها الثمانية + الأمانة",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u3",
            level = 4, week = 10, weekEnd = 11,
            subject = "الرياضيات",
            domain = "الوحدة 3: الهوية والتاريخ والتراث الوطني",
            title = "الجمع والطرح للأعداد العشرية، والتحويل بين وحدات القياس (الأطوال والمساحات)",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u3",
            level = 4, week = 10, weekEnd = 11,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 3: الهوية والتاريخ والتراث الوطني",
            title = "التكاثر عند الحيوان: الحيوانات البيوضة والولودة وطرق التكاثر والرعاية الأبوية",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u3",
            level = 4, week = 10, weekEnd = 11,
            subject = "التاريخ",
            domain = "الوحدة 3: الهوية والتاريخ والتراث الوطني",
            title = "دخول الإسلام إلى موريتانيا ودور قوافل التجارة وحركة المرابطين",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u3",
            level = 4, week = 10, weekEnd = 11,
            subject = "الجغرافيا",
            domain = "الوحدة 3: الهوية والتاريخ والتراث الوطني",
            title = "المناخ في موريتانيا: الأقاليم المناخية ودرجات الحرارة ونظام الأمطار",
            competency = "المناخ في موريتانيا: الأقاليم المناخية ودرجات الحرارة ونظام الأمطار",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u3",
            level = 4, week = 10, weekEnd = 11,
            subject = "التربية المدنية",
            domain = "الوحدة 3: الهوية والتاريخ والتراث الوطني",
            title = "السلطة التنفيذية: رئيس الجمهورية والحكومة والوزارات والخدمات العامة",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u3",
            level = 4, week = 10, weekEnd = 11,
            subject = "Français",
            domain = "الوحدة 3: الهوية والتاريخ والتراث الوطني",
            title = "Projet 3 : La science, les métiers et les découvertes (Textes informatifs, les compléments du verbe COD/COI simples, les verbes usuels du 3ème groupe au présent, accords dans le groupe nominal).",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u4",
            level = 4, week = 14, weekEnd = 17,
            subject = "اللغة العربية",
            domain = "الوحدة 4: البيئة والتنمية والطبيعة",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (نصوص قراءة، الحال والتمييز، اسم الفاعل واسم المفعول، الهمزة المتطرفة على السطر والياء) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u4",
            level = 4, week = 14, weekEnd = 17,
            subject = "التربية الإسلامية",
            domain = "الوحدة 4: البيئة والتنمية والطبيعة",
            title = "سورة الانفطار وسورة التكوير + غزوة بدر الكبرى وأسبابها ونتائجها + الصبر والشجاعة",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u4",
            level = 4, week = 14, weekEnd = 17,
            subject = "الرياضيات",
            domain = "الوحدة 4: البيئة والتنمية والطبيعة",
            title = "الضرب في الأعداد العشرية، وجداول التناسبية ومعامل التناسب والنسب المئوية البسيطة",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u4",
            level = 4, week = 14, weekEnd = 17,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 4: البيئة والتنمية والطبيعة",
            title = "التكاثر عند النبات: التكاثر البذري واللا تزاوجي (الافتسال والترقيد والدرنات)",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u4",
            level = 4, week = 14, weekEnd = 17,
            subject = "التاريخ",
            domain = "الوحدة 4: البيئة والتنمية والطبيعة",
            title = "المدن التاريخية الموريتانية وإشعاعها العلمي (شنقيط، وادان، تيشيت، ولاتة)",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u4",
            level = 4, week = 14, weekEnd = 17,
            subject = "الجغرافيا",
            domain = "الوحدة 4: البيئة والتنمية والطبيعة",
            title = "الموارد المائية في موريتانيا: المياه الجوفية، السدود، والأنهار وحمايتها",
            competency = "الموارد المائية في موريتانيا: المياه الجوفية، السدود، والأنهار وحمايتها",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u4",
            level = 4, week = 14, weekEnd = 17,
            subject = "التربية المدنية",
            domain = "الوحدة 4: البيئة والتنمية والطبيعة",
            title = "السلطة القضائية: المحاكم واستقلال القضاء والعدالة بين المواطنين",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u4",
            level = 4, week = 14, weekEnd = 17,
            subject = "Français",
            domain = "الوحدة 4: البيئة والتنمية والطبيعة",
            title = "Projet 4 : Voyages, contes et légendes (Lecture suivie de contes, l'imparfait et le futur simple, les connecteurs temporels et logiques, rédaction de la fin d'une histoire).",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u5",
            level = 4, week = 18, weekEnd = 21,
            subject = "اللغة العربية",
            domain = "الوحدة 5: العلم والابتكار والتكنولوجيا",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (نصوص قراءة، المستثنى بـ إلا، المجرد والمزيد من الأفعال، التاء المربوطة والمفتوحة وحالاتها) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u5",
            level = 4, week = 18, weekEnd = 21,
            subject = "التربية الإسلامية",
            domain = "الوحدة 5: العلم والابتكار والتكنولوجيا",
            title = "سورة عبس وسورة النازعات + غزوة أحد والعبر المستخلصة منها + الشورى وطاعة القيادة",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u5",
            level = 4, week = 18, weekEnd = 21,
            subject = "الرياضيات",
            domain = "الوحدة 5: العلم والابتكار والتكنولوجيا",
            title = "القسمة الإقليدية على عدد برقمين مع الباقي، والتحقق من صحة القسمة (المقسوم والمقسوم عليه)",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u5",
            level = 4, week = 18, weekEnd = 21,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 5: العلم والابتكار والتكنولوجيا",
            title = "الطاقة الكهربائية: الدارة على التوالي والدارة على التوازي ومخاطر التيار الكهربائي",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u5",
            level = 4, week = 18, weekEnd = 21,
            subject = "التاريخ",
            domain = "الوحدة 5: العلم والابتكار والتكنولوجيا",
            title = "الإمارات الموريتانية ودورها السياسي والاجتماعي وتاريخ المقاومة",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u5",
            level = 4, week = 18, weekEnd = 21,
            subject = "الجغرافيا",
            domain = "الوحدة 5: العلم والابتكار والتكنولوجيا",
            title = "الأنشطة الاقتصادية في موريتانيا: التعدين (الحديد والذهب والنحاس) وثروات باطن الأرض",
            competency = "الأنشطة الاقتصادية في موريتانيا: التعدين (الحديد والذهب والنحاس) وثروات باطن الأرض",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u5",
            level = 4, week = 18, weekEnd = 21,
            subject = "التربية المدنية",
            domain = "الوحدة 5: العلم والابتكار والتكنولوجيا",
            title = "المؤسسات الدستورية المستقلة: المجلس الدستوري ومحكمة الحسابات",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u5",
            level = 4, week = 18, weekEnd = 21,
            subject = "Français",
            domain = "الوحدة 5: العلم والابتكار والتكنولوجيا",
            title = "Projet 5 : La protection de l'environnement et de la planète (Lecture de textes documentaires, l'expression de la cause et de la conséquence, le passé composé avec avoir et être, production d'un texte d'engagement écologique).",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u6",
            level = 4, week = 22, weekEnd = 23,
            subject = "اللغة العربية",
            domain = "الوحدة 6: الثقافة والفنون والصناعة التقليدية",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (نصوص قراءة، الأفعال الخمسة وإعرابها، الأسماء الخمسة، الألف اللينة في أواخر الأفعال والأسماء) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u6",
            level = 4, week = 22, weekEnd = 23,
            subject = "التربية الإسلامية",
            domain = "الوحدة 6: الثقافة والفنون والصناعة التقليدية",
            title = "سورة النبأ وسورة المرسلات + غزوة الخندق وصفح النبي ﷺ + الوفاء بالعهود",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u6",
            level = 4, week = 22, weekEnd = 23,
            subject = "الرياضيات",
            domain = "الوحدة 6: الثقافة والفنون والصناعة التقليدية",
            title = "حساب مساحة الأشكال المستوية: مساحة المربع والمستطيل والمثلث (م² وسنتيمتر مربع)",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u6",
            level = 4, week = 22, weekEnd = 23,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 6: الثقافة والفنون والصناعة التقليدية",
            title = "مصادر الطاقة المتجددة (الشمسية، الهوائية، المائية) والترشيد الطاقي في الحياة اليومية",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u6",
            level = 4, week = 22, weekEnd = 23,
            subject = "التاريخ",
            domain = "الوحدة 6: الثقافة والفنون والصناعة التقليدية",
            title = "المدارس العلمية والمحاظر الشنقيطية وأثرها في نشر العلوم في إفريقيا والعالم العربي",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u6",
            level = 4, week = 22, weekEnd = 23,
            subject = "الجغرافيا",
            domain = "الوحدة 6: الثقافة والفنون والصناعة التقليدية",
            title = "الصيد البحري في موريتانيا: الشواطئ والموانئ وأنواع الأسماك وحماية الثروة السمكية",
            competency = "الصيد البحري في موريتانيا: الشواطئ والموانئ وأنواع الأسماك وحماية الثروة السمكية",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u6",
            level = 4, week = 22, weekEnd = 23,
            subject = "التربية المدنية",
            domain = "الوحدة 6: الثقافة والفنون والصناعة التقليدية",
            title = "المجتمع المدني والجمعيات التطوعية ودورها في التنمية المجتمعية",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u6",
            level = 4, week = 22, weekEnd = 23,
            subject = "Français",
            domain = "الوحدة 6: الثقافة والفنون والصناعة التقليدية",
            title = "Projet 6 : Les arts, les traditions et l'artisanat mauritanien (Lecture expressive de récits culturels, la phrase complexe avec parce que/quand, révision des temps du passé, rédaction d'une description artisanale).",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u7",
            level = 4, week = 26, weekEnd = 29,
            subject = "اللغة العربية",
            domain = "الوحدة 7: الرحلات والاستكشاف والمبادرة والعمل",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (نصوص قراءة، التوكيد اللفظي والمعنوي، النعت الحقيقي والسببي، إعراب الجمل المركبة) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u7",
            level = 4, week = 26, weekEnd = 29,
            subject = "التربية الإسلامية",
            domain = "الوحدة 7: الرحلات والاستكشاف والمبادرة والعمل",
            title = "مراجعة سور جزء عم + أحكام صلاة الجنازة والكسوف والاستسقاء + التعاون على البر والتقوى",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u7",
            level = 4, week = 26, weekEnd = 29,
            subject = "الرياضيات",
            domain = "الوحدة 7: الرحلات والاستكشاف والمبادرة والعمل",
            title = "المجسمات الهندسية: المكعب، متوازي المستطيلات، الأسطوانة، والموشور القائم وخواصها",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u7",
            level = 4, week = 26, weekEnd = 29,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 7: الرحلات والاستكشاف والمبادرة والعمل",
            title = "التلوث البيئي: أنواعه (الهوائي، المائي، التربة) وأثره على الكائنات الحية والحلول الوقائية",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u7",
            level = 4, week = 26, weekEnd = 29,
            subject = "التاريخ",
            domain = "الوحدة 7: الرحلات والاستكشاف والمبادرة والعمل",
            title = "أعلام وعلماء موريتانيا ومؤلفاتهم في الفقه واللغة والتصوف والفلك",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u7",
            level = 4, week = 26, weekEnd = 29,
            subject = "الجغرافيا",
            domain = "الوحدة 7: الرحلات والاستكشاف والمبادرة والعمل",
            title = "الزراعة والتنمية الريفية في موريتانيا: زراعة النخيل والمحاصيل الفيضية والمروية",
            competency = "الزراعة والتنمية الريفية في موريتانيا: زراعة النخيل والمحاصيل الفيضية والمروية",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u7",
            level = 4, week = 26, weekEnd = 29,
            subject = "التربية المدنية",
            domain = "الوحدة 7: الرحلات والاستكشاف والمبادرة والعمل",
            title = "المواطنة الرقمية والاستخدام الآمن لوسائل الاتصال والإنترنت",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u7",
            level = 4, week = 26, weekEnd = 29,
            subject = "Français",
            domain = "الوحدة 7: الرحلات والاستكشاف والمبادرة والعمل",
            title = "Projet 7 : Découvertes scientifiques et innovations (Textes explicatifs, le conditionnel présent de politesse, la négation complexe ne...plus/ne...jamais, rédaction d'une notice d'utilisation).",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u8",
            level = 4, week = 30, weekEnd = 33,
            subject = "اللغة العربية",
            domain = "الوحدة 8: المراجعة والإعداد الشامل للامتحانات",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (مراجعة شاملة لجميع نصوص وقواعد النحو والصرف والإملاء والإنتاج الكتابي) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u8",
            level = 4, week = 30, weekEnd = 33,
            subject = "التربية الإسلامية",
            domain = "الوحدة 8: المراجعة والإعداد الشامل للامتحانات",
            title = "مراجعة شاملة لجميع السور الكريمة والأحكام الفقهية والسيرة النبوية المقررة",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u8",
            level = 4, week = 30, weekEnd = 33,
            subject = "الرياضيات",
            domain = "الوحدة 8: المراجعة والإعداد الشامل للامتحانات",
            title = "مراجعة شاملة للعمليات الحسابية للأعداد الطبيعية والعشرية والكسور والمساحات والمسائل",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u8",
            level = 4, week = 30, weekEnd = 33,
            subject = "التربية العلمية والتكنولوجية",
            domain = "الوحدة 8: المراجعة والإعداد الشامل للامتحانات",
            title = "مراجعة شاملة لمفاهيم وظائف الجسم والتكاثر والطاقة والكهرباء والبيئة",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u8",
            level = 4, week = 30, weekEnd = 33,
            subject = "التاريخ",
            domain = "الوحدة 8: المراجعة والإعداد الشامل للامتحانات",
            title = "مراجعة شاملة لأحداث التاريخ الموريتاني والعصور والإمارات والعلماء",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u8",
            level = 4, week = 30, weekEnd = 33,
            subject = "الجغرافيا",
            domain = "الوحدة 8: المراجعة والإعداد الشامل للامتحانات",
            title = "مراجعة شاملة لجغرافيا موريتانيا الطبيعية والاقتصادية والموارد الوطنية",
            competency = "مراجعة شاملة لجغرافيا موريتانيا الطبيعية والاقتصادية والموارد الوطنية",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u8",
            level = 4, week = 30, weekEnd = 33,
            subject = "التربية المدنية",
            domain = "الوحدة 8: المراجعة والإعداد الشامل للامتحانات",
            title = "مراجعة شاملة للدستور والسلطات والمؤسسات الوطنية والمواطنة الفاعلة",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u8",
            level = 4, week = 30, weekEnd = 33,
            subject = "Français",
            domain = "الوحدة 8: المراجعة والإعداد الشامل للامتحانات",
            title = "Projet 8 : Bilan annuel, révision générale et consolidation des acquis en français (Compréhension de l'écrit, grammaire, conjugaison, orthographe et production d'écrits variés).",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_ar_u9",
            level = 4, week = 34, weekEnd = 35,
            subject = "اللغة العربية",
            domain = "المراجعة والتثبيت النهائي",
            title = "القراءة والتحليل، القواعد النحوية والصرفية (مراجعة نهائية لجميع المعارف والمهارات اللغوية والإنشائية للسنة الرابعة) والإنشاء",
            competency = "أن يحلل نصوصاً متنوعة ويتقن قواعد النحو والصرف والإملاء وينتج نصوصاً سردية ووصفية متكاملة.",
            skills = "التحليل الأدبي، الإعراب التام، الإنشاء المنهجي السليم.",
            activities = "كتاب القراءة والنصوص، كراس القواعد، تطبيقات إعرابية، وكراس الإنشاء."
        ),
        AnnualLessonItem(
            id = "l4_isl_u9",
            level = 4, week = 34, weekEnd = 35,
            subject = "التربية الإسلامية",
            domain = "المراجعة والتثبيت النهائي",
            title = "مراجعة نهائية للقرآن الكريم والعبادات والمعاملات والسيرة النبوية",
            competency = "أن يستظهر السور المقررة ويفهم أركان الإيمان وأحكام الصيام والزكاة وغزوات السيرة النبوية.",
            skills = "الحفظ المتقن، فقه العبادات، واستخلاص العبر من الغزوات النبوية.",
            activities = "المصحف الشريف، أطلس الغزوات النبوية، وكراس التربية الإسلامية."
        ),
        AnnualLessonItem(
            id = "l4_math_u9",
            level = 4, week = 34, weekEnd = 35,
            subject = "الرياضيات",
            domain = "المراجعة والتثبيت النهائي",
            title = "مراجعة نهائية لجميع المفاهيم الرياضية والهندسية وحل المسائل الاختبارية",
            competency = "أن يتحكم في الأعداد العشرية، التناسبية، المساحات، المجسمات وحل المسائل المركبة.",
            skills = "الحساب الدقيق، التحويل بين الوحدات، الرسم الهندسي المتقن، وحل الوضعيات المشكلة.",
            activities = "أدوات الهندسة، جداول التناسبية، أوراق الحساب والمسائل."
        ),
        AnnualLessonItem(
            id = "l4_sci_u9",
            level = 4, week = 34, weekEnd = 35,
            subject = "التربية العلمية والتكنولوجية",
            domain = "المراجعة والتثبيت النهائي",
            title = "مراجعة نهائية للتجارب العلمية والتطبيقات التكنولوجية والصحية",
            competency = "أن يستوعب آليات الحركة والتكاثر والدارات الكهربائية والتلوث البيئي ومصادر الطاقة.",
            skills = "التفكير العلمي التجريبي، عزل المتغيرات، واستنتاج العلاقات السببية.",
            activities = "نماذج مجسمات الهيكل العظمي، عينات نباتية، دارات كهربائية."
        ),
        AnnualLessonItem(
            id = "l4_hist_u9",
            level = 4, week = 34, weekEnd = 35,
            subject = "التاريخ",
            domain = "المراجعة والتثبيت النهائي",
            title = "مراجعة نهائية للتاريخ الوطني والإشعاع الحضاري لموريتانيا",
            competency = "أن يتعرف على تاريخ موريتانيا القديم والإسلامي، عصر المرابطين والمدن التاريخية والإمارات.",
            skills = "التحليل التاريخي، استخدام الوثائق والخرائط التاريخية، والاعتزاز بالهوية.",
            activities = "خرائط تاريخية لموريتانيا، صور آثار ومدن قديمة، وثائق تاريخية."
        ),
        AnnualLessonItem(
            id = "l4_geog_u9",
            level = 4, week = 34, weekEnd = 35,
            subject = "الجغرافيا",
            domain = "المراجعة والتثبيت النهائي",
            title = "مراجعة نهائية لجغرافية موريتانيا وثرواتها الاقتصادية",
            competency = "مراجعة نهائية لجغرافية موريتانيا وثرواتها الاقتصادية",
            skills = "أن يحدد موقع موريتانيا وتضاريسها ومناخها ومواردها الاقتصادية (التعدين، الصيد، الزراعة).",
            activities = "قراءة وتفسير الخرائط الجغرافية والاقتصادية، وتحليل المعطيات البيئية.", isFrench = true
        ),
        AnnualLessonItem(
            id = "l4_civ_u9",
            level = 4, week = 34, weekEnd = 35,
            subject = "التربية المدنية",
            domain = "المراجعة والتثبيت النهائي",
            title = "مراجعة نهائية للمؤسسات الديمقراطية والواجبات الوطنية",
            competency = "أن يفهم الدستور والسلطات الثلاث واستقلال القضاء والمواطنة الفاعلة والمجتمع المدني.",
            skills = "الوعي الدستوري والحقوقي، احترام سيادة القانون والمؤسسات.",
            activities = "نصوص الدستور الموريتاني، مخططات هيكل الدولة، مقالات توعوية."
        ),
        AnnualLessonItem(
            id = "l4_fr_u9",
            level = 4, week = 34, weekEnd = 35,
            subject = "Français",
            domain = "المراجعة والتثبيت النهائي",
            title = "Révision finale intensive de français : lecture analytique, maîtrise linguistique et production écrite autonome.",
            competency = "Développer la compréhension et production écrite et orale en français, enrichir le vocabulaire, et consolider la grammaire et la conjugaison.",
            skills = "Compréhension de textes variés, maîtrise des accords, conjugaison aux temps de base, et rédaction autonome.",
            activities = "Manuels de français, fiches de lecture, exercices d'application et productions écrites.", isFrench = true
        )
    ) + Level5OfficialCurriculum.lessons
}
