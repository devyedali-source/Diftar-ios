package com.example.data.planning

import com.example.compat.*
import kotlinx.coroutines.IO

/**
 * Data structures representing the Official Curriculum Hierarchy:
 * Term (الفصل الدراسي) -> Subject (المادة) -> Domain (المجال) -> Competency (الكفاية) -> Week (الأسبوع) -> Lesson Title (المعارف / عناوين الدروس)
 */

data class OfficialTermItem(
    val termNumber: Int,
    val title: String,
    val subtitle: String,
    val weekRangeText: String,
    val teachingWeeksText: String,
    val lessonCount: Int,
    val pedagogicalStationCount: Int = 0,
    val specialStationsCount: Int = 0
)

data class OfficialSubjectItem(
    val subject: String,
    val emoji: String,
    val lessonCount: Int,
    val domainCount: Int,
    val pedagogicalStationCount: Int = 0
)

data class OfficialDomainItem(
    val domain: String,
    val subject: String,
    val lessonCount: Int,
    val competencyCount: Int,
    val pedagogicalStationCount: Int = 0
)

data class OfficialCompetencyItem(
    val competencyNumber: Int?,
    val competencyText: String,
    val displayTitle: String,
    val domain: String,
    val subject: String,
    val lessonCount: Int,
    val weekCount: Int,
    val pedagogicalStationCount: Int = 0
)

data class OfficialWeekItem(
    val week: Int,
    val weekEnd: Int,
    val weekDisplay: String,
    val lessonCount: Int,
    val isMultiWeek: Boolean,
    val pedagogicalStationCount: Int = 0,
    val itemType: LessonItemType = LessonItemType.REGULAR_LESSON
)

object OfficialPlanningHierarchyAdapter {

    /**
     * Identifies if an item is a regular instructional lesson (excluding stations, holidays and exams)
     */
    fun isTeachingLesson(item: AnnualLessonItem): Boolean {
        return LessonProposalRepository.classifyLessonItem(item) == LessonItemType.REGULAR_LESSON
    }

    /**
     * Identifies if an item is a pedagogical station (integration / evaluation / diagnostic / remediation)
     */
    fun isPedagogicalStation(item: AnnualLessonItem): Boolean {
        return LessonProposalRepository.classifyLessonItem(item) == LessonItemType.PEDAGOGICAL_STATION
    }

    /**
     * Identifies if an item is a holiday or official exam
     */
    fun isHolidayOrExam(item: AnnualLessonItem): Boolean {
        val type = LessonProposalRepository.classifyLessonItem(item)
        return type == LessonItemType.HOLIDAY || type == LessonItemType.EXAM
    }

    /**
     * Determines the Term (1, 2, or 3) for a given week
     */
    fun getTermForWeek(week: Int): Int {
        return when (week) {
            in 1..13 -> 1
            in 14..26 -> 2
            in 27..38 -> 3
            else -> 1
        }
    }

    /**
     * Cleans domain name from attached week numbers like (W02–W05)
     * and maps legacy unit descriptions to clean official domain names.
     */
    fun getCleanDomain(item: AnnualLessonItem): String {
        val rawDomain = item.domain.trim()
        val subject = item.subject.trim()
        val title = item.title.trim()

        // 1. Remove week ranges from domain like "(W02–W05)", "(W14)"
        val stripped = rawDomain.replace(Regex("\\s*\\([Ww\\d–\\-\\s]+\\)$"), "").trim()
        if (stripped.isNotBlank() && !stripped.startsWith("الوحدة")) {
            return stripped
        }

        // 2. Map legacy / general unit names for Levels 1-4 to standard official domains
        return when {
            subject.contains("رياضيات") || subject.contains("الرياضيات") -> {
                when {
                    title.contains("هندسة") || title.contains("مستقيم") || title.contains("أشكال") ||
                            title.contains("مجسم") || title.contains("مثلث") || title.contains("مربع") ||
                            title.contains("دائرة") || title.contains("زوايا") -> "الهندسة والفضاء"
                    title.contains("قياس") || title.contains("طول") || title.contains("كتل") ||
                            title.contains("سعة") || title.contains("نقود") || title.contains("زمن") ||
                            title.contains("ساعة") || title.contains("متر") || title.contains("لتر") ||
                            title.contains("كغ") -> "القياس والمقادير"
                    title.contains("مسألة") || title.contains("مسائل") || title.contains("تناسبية") ||
                            title.contains("بيانات") || title.contains("مخطط") -> "حل المسائل وتنظيم البيانات"
                    else -> "الأعداد والحساب والعمليات"
                }
            }
            subject.contains("العربية") || subject.contains("اللغة العربية") -> {
                when {
                    title.contains("قواعد") || title.contains("نحو") || title.contains("صرف") ||
                            title.contains("تحويل") || title.contains("أقسام الكلام") ||
                            title.contains("جملة") || title.contains("فعل") || title.contains("اسم") -> "القواعد النحوية والصرفية"
                    title.contains("إملاء") || title.contains("خط") || title.contains("رسم") ||
                            title.contains("تاء") || title.contains("همزة") || title.contains("تنوين") -> "الإملاء والخط"
                    title.contains("تعبير") || title.contains("إنتاج") || title.contains("كتابة") ||
                            title.contains("إنشاء") || title.contains("مشروع") -> "التعبير والإنتاج الكتابي"
                    else -> "القراءة والتواصل الشفهي"
                }
            }
            subject.contains("الإسلامية") || subject.contains("التربية الإسلامية") -> {
                when {
                    title.contains("سورة") || title.contains("قرآن") || title.contains("الفاتحة") ||
                            title.contains("الملك") || title.contains("القلم") || title.contains("الحاقة") ||
                            title.contains("المعارج") || title.contains("نوح") || title.contains("التين") ||
                            title.contains("الشرح") || title.contains("الضحى") || title.contains("الناس") -> "القرآن الكريم"
                    title.contains("حديث") || title.contains("سيرة") || title.contains("النبي") ||
                            title.contains("الرسول") || title.contains("هجرة") || title.contains("غزوة") -> "الحديث الشريف والسيرة النبوية"
                    title.contains("إيمان") || title.contains("عقيدة") || title.contains("الله") ||
                            title.contains("الموت") || title.contains("البعث") || title.contains("القيامة") ||
                            title.contains("الجنة") || title.contains("النار") || title.contains("التوحيد") -> "العقيدة الإسلامية"
                    else -> "العبادات والآداب الإسلامية"
                }
            }
            subject.contains("الفرنسية") || subject.contains("Français") || subject.contains("français") -> {
                when {
                    title.contains("Grammaire") || title.contains("grammaire") -> "Grammaire"
                    title.contains("Conjugaison") || title.contains("conjugaison") -> "Conjugaison"
                    title.contains("Orthographe") || title.contains("orthographe") -> "Orthographe"
                    title.contains("Vocabulaire") || title.contains("vocabulaire") -> "Vocabulaire"
                    title.contains("Production") || title.contains("écriture") -> "Production d'écrits"
                    else -> "Compréhension de l'écrit"
                }
            }
            subject.contains("العلوم") || subject.contains("العلمية") || subject.contains("التكنولوجية") -> "التربية العلمية والتكنولوجية"
            subject.contains("المدنية") -> "التربية المدنية"
            subject.contains("التاريخ") && subject.contains("الجغرافيا") -> "التاريخ والجغرافيا"
            subject.contains("التاريخ") -> "التاريخ"
            subject.contains("الجغرافيا") -> "الجغرافيا"
            subject.contains("البدنية") || subject.contains("الرياضية") -> "التربية البدنية والرياضية"
            subject.contains("الفنية") || subject.contains("النشيد") -> "التربية الفنية والنشيد"
            else -> subject.ifBlank { "المجال العام" }
        }
    }

    /**
     * Extracts or formats competency text and number
     */
    fun extractCompetencyNumber(competencyText: String): Int? {
        val match = Regex("(?:الكفاية|كفاية)\\s*(\\d+)").find(competencyText)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Formats a clean competency display title
     */
    fun formatCompetencyDisplay(competencyText: String, index: Int = 1): String {
        val num = extractCompetencyNumber(competencyText)
        val clean = competencyText.trim()
        return if (clean.isBlank()) {
            "الكفاية المقررة"
        } else if (num != null) {
            "الكفاية $num: $clean"
        } else {
            clean
        }
    }

    /**
     * Calculates the exact number of regular teaching lesson instances (where NUMBER OF LESSONS = NUMBER OF PREPARATIONS).
     * Multi-week topics are calculated as independent weekly lesson instances.
     */
    fun countLessonTitles(
        level: Int,
        term: Int? = null,
        subject: String? = null,
        domain: String? = null,
        competency: String? = null,
        week: Int? = null
    ): Int {
        val allLessons = AnnualPlanningRepository.getLessonsForLevel(level)
        var total = 0

        for (item in allLessons) {
            if (!isTeachingLesson(item)) continue
            if (subject != null && item.subject.trim() != subject.trim()) continue
            if (domain != null && getCleanDomain(item) != domain.trim()) continue
            if (competency != null && item.competency.trim() != competency.trim()) continue

            val wStart = item.week
            val wEnd = item.weekEnd.coerceAtLeast(wStart)

            for (w in wStart..wEnd) {
                if (term != null && getTermForWeek(w) != term) continue
                if (week != null && w != week) continue
                total++
            }
        }
        return total
    }

    /**
     * Calculates the exact number of pedagogical stations (integration, evaluation, remediation, diagnostics).
     */
    fun countPedagogicalStations(
        level: Int,
        term: Int? = null,
        subject: String? = null,
        domain: String? = null,
        competency: String? = null,
        week: Int? = null
    ): Int {
        val allLessons = AnnualPlanningRepository.getLessonsForLevel(level)
        var total = 0

        for (item in allLessons) {
            if (!isPedagogicalStation(item)) continue
            if (subject != null && item.subject.trim() != subject.trim()) continue
            if (domain != null && getCleanDomain(item) != domain.trim()) continue
            if (competency != null && item.competency.trim() != competency.trim()) continue

            val wStart = item.week
            val wEnd = item.weekEnd.coerceAtLeast(wStart)

            for (w in wStart..wEnd) {
                if (term != null && getTermForWeek(w) != term) continue
                if (week != null && w != week) continue
                total++
            }
        }
        return total
    }

    /**
     * Step 1: Get the 3 Terms with dynamically calculated lesson counts and pedagogical stations
     */
    fun getTerms(level: Int): List<OfficialTermItem> {
        val t1Count = countLessonTitles(level = level, term = 1)
        val t2Count = countLessonTitles(level = level, term = 2)
        val t3Count = countLessonTitles(level = level, term = 3)

        val t1Ped = countPedagogicalStations(level = level, term = 1)
        val t2Ped = countPedagogicalStations(level = level, term = 2)
        val t3Ped = countPedagogicalStations(level = level, term = 3)

        val t1Special = getSpecialStationsForTerm(level, 1).size
        val t2Special = getSpecialStationsForTerm(level, 2).size
        val t3Special = getSpecialStationsForTerm(level, 3).size

        return listOf(
            OfficialTermItem(
                termNumber = 1,
                title = "الفصل الدراسي الأول",
                subtitle = "الأسابيع 1 إلى 13",
                weekRangeText = "الأسابيع 1 – 13",
                teachingWeeksText = "11 أسبوع تدريس + محطة اختبارات + عطلة الفصل",
                lessonCount = t1Count,
                pedagogicalStationCount = t1Ped,
                specialStationsCount = t1Special
            ),
            OfficialTermItem(
                termNumber = 2,
                title = "الفصل الدراسي الثاني",
                subtitle = "الأسابيع 14 إلى 26",
                weekRangeText = "الأسابيع 14 – 26",
                teachingWeeksText = "11 أسبوع تدريس + محطة اختبارات + عطلة الفصل",
                lessonCount = t2Count,
                pedagogicalStationCount = t2Ped,
                specialStationsCount = t2Special
            ),
            OfficialTermItem(
                termNumber = 3,
                title = "الفصل الدراسي الثالث",
                subtitle = "الأسابيع 27 إلى 38",
                weekRangeText = "الأسابيع 27 – 38",
                teachingWeeksText = "11 أسبوع تدريس + محطة تقويم + امتحان التجاوز",
                lessonCount = t3Count,
                pedagogicalStationCount = t3Ped,
                specialStationsCount = t3Special
            )
        )
    }

    /**
     * Step 2: Get Subjects present in the selected Term with dynamic lesson counts
     */
    fun getSubjectsForTerm(level: Int, termNumber: Int): List<OfficialSubjectItem> {
        val termLessons = AnnualPlanningRepository.getLessonsForLevel(level).filter {
            (isTeachingLesson(it) || isPedagogicalStation(it)) && getTermForWeek(it.week) == termNumber
        }

        val subjectNames = termLessons.map { it.subject.trim() }.distinct().sortedBy { getSubjectOrder(it) }

        return subjectNames.map { subject ->
            val count = countLessonTitles(level = level, term = termNumber, subject = subject)
            val pedCount = countPedagogicalStations(level = level, term = termNumber, subject = subject)
            val domains = getDomainsForSubject(level = level, termNumber = termNumber, subject = subject).size
            OfficialSubjectItem(
                subject = subject,
                emoji = getSubjectEmoji(subject),
                lessonCount = count,
                domainCount = domains,
                pedagogicalStationCount = pedCount
            )
        }
    }

    /**
     * Step 3: Get Official Domains present in the selected Subject & Term
     */
    fun getDomainsForSubject(level: Int, termNumber: Int, subject: String): List<OfficialDomainItem> {
        val lessons = AnnualPlanningRepository.getLessonsForLevel(level).filter {
            (isTeachingLesson(it) || isPedagogicalStation(it)) && getTermForWeek(it.week) == termNumber && it.subject.trim() == subject.trim()
        }

        val domainNames = lessons.map { getCleanDomain(it) }.distinct()

        return domainNames.map { domain ->
            val count = countLessonTitles(level = level, term = termNumber, subject = subject, domain = domain)
            val pedCount = countPedagogicalStations(level = level, term = termNumber, subject = subject, domain = domain)
            val comps = getCompetenciesForDomain(level = level, termNumber = termNumber, subject = subject, domain = domain).size
            OfficialDomainItem(
                domain = domain,
                subject = subject,
                lessonCount = count,
                competencyCount = comps,
                pedagogicalStationCount = pedCount
            )
        }
    }

    /**
     * Step 4: Get Competencies present in the selected Domain, Subject & Term
     */
    fun getCompetenciesForDomain(
        level: Int,
        termNumber: Int,
        subject: String,
        domain: String
    ): List<OfficialCompetencyItem> {
        val lessons = AnnualPlanningRepository.getLessonsForLevel(level).filter {
            (isTeachingLesson(it) || isPedagogicalStation(it)) &&
                    getTermForWeek(it.week) == termNumber &&
                    it.subject.trim() == subject.trim() &&
                    getCleanDomain(it) == domain.trim()
        }

        val compTexts = lessons.map { it.competency.trim() }.distinct()

        return compTexts.mapIndexed { idx, compText ->
            val count = countLessonTitles(
                level = level,
                term = termNumber,
                subject = subject,
                domain = domain,
                competency = compText
            )
            val pedCount = countPedagogicalStations(
                level = level,
                term = termNumber,
                subject = subject,
                domain = domain,
                competency = compText
            )
            val weeks = getWeeksForCompetency(
                level = level,
                termNumber = termNumber,
                subject = subject,
                domain = domain,
                competencyText = compText
            ).size

            val compNum = extractCompetencyNumber(compText) ?: (if (compTexts.size > 1) idx + 1 else null)
            val displayTitle = if (compNum != null) "الكفاية $compNum" else "الكفاية العامة"

            OfficialCompetencyItem(
                competencyNumber = compNum,
                competencyText = compText.ifBlank { "الكفاية المقررة في المنهاج الرسمي" },
                displayTitle = displayTitle,
                domain = domain,
                subject = subject,
                lessonCount = count,
                weekCount = weeks,
                pedagogicalStationCount = pedCount
            )
        }
    }

    /**
     * Step 5: Get Weeks linked to the selected Competency
     */
    fun getWeeksForCompetency(
        level: Int,
        termNumber: Int,
        subject: String,
        domain: String,
        competencyText: String
    ): List<OfficialWeekItem> {
        val lessons = AnnualPlanningRepository.getLessonsForLevel(level).filter {
            (isTeachingLesson(it) || isPedagogicalStation(it)) &&
                    getTermForWeek(it.week) == termNumber &&
                    it.subject.trim() == subject.trim() &&
                    getCleanDomain(it) == domain.trim() &&
                    it.competency.trim() == competencyText.trim()
        }

        val weekRanges = lessons.map { Pair(it.week, it.weekEnd) }.distinct().sortedBy { it.first }

        return weekRanges.map { (wStart, wEnd) ->
            val matching = lessons.filter { it.week == wStart && it.weekEnd == wEnd }
            val regCount = matching.filter { isTeachingLesson(it) }.sumOf { (it.weekEnd - it.week + 1).coerceAtLeast(1) }
            val pedCount = matching.filter { isPedagogicalStation(it) }.sumOf { (it.weekEnd - it.week + 1).coerceAtLeast(1) }
            val isMulti = wEnd > wStart
            val display = if (isMulti) "الأسابيع $wStart – $wEnd" else "الأسبوع $wStart"
            val primaryType = matching.firstOrNull()?.let { LessonProposalRepository.classifyLessonItem(it) } ?: LessonItemType.REGULAR_LESSON

            OfficialWeekItem(
                week = wStart,
                weekEnd = wEnd,
                weekDisplay = display,
                lessonCount = regCount,
                isMultiWeek = isMulti,
                pedagogicalStationCount = pedCount,
                itemType = primaryType
            )
        }
    }

    /**
     * Step 6: Get Official Lesson Titles / Items for a specific Week
     */
    fun getLessonsForWeek(
        level: Int,
        termNumber: Int,
        subject: String,
        domain: String,
        competencyText: String,
        week: Int,
        weekEnd: Int = week
    ): List<AnnualLessonItem> {
        return AnnualPlanningRepository.getLessonsForLevel(level).filter {
            (isTeachingLesson(it) || isPedagogicalStation(it)) &&
                    getTermForWeek(it.week) == termNumber &&
                    it.subject.trim() == subject.trim() &&
                    getCleanDomain(it) == domain.trim() &&
                    it.competency.trim() == competencyText.trim() &&
                    it.week == week && it.weekEnd == weekEnd
        }
    }

    /**
     * Returns special stations (diagnostic, exams, vacations, integration stations) for a Term
     */
    fun getSpecialStationsForTerm(level: Int, termNumber: Int): List<AnnualLessonItem> {
        return AnnualPlanningRepository.getLessonsForLevel(level).filter {
            (isHolidayOrExam(it) || isPedagogicalStation(it)) && getTermForWeek(it.week) == termNumber
        }
    }

    private fun getSubjectOrder(subject: String): Int {
        return when {
            subject.contains("القرآن") || subject.contains("الإسلامية") -> 1
            subject.contains("العربية") || subject.contains("القراءة") -> 2
            subject.contains("الرياضيات") || subject.contains("الحساب") -> 3
            subject.contains("Français") || subject.contains("français") -> 4
            subject.contains("العلوم") || subject.contains("العلمية") || subject.contains("التكنولوجية") -> 5
            subject.contains("التاريخ") || subject.contains("الجغرافيا") -> 6
            subject.contains("المدنية") -> 7
            subject.contains("الفنية") || subject.contains("النشيد") -> 8
            subject.contains("البدنية") || subject.contains("الرياضية") -> 9
            else -> 10
        }
    }

    private fun getSubjectEmoji(subject: String): String {
        return when {
            subject.contains("القرآن") || subject.contains("الإسلامية") -> "🕌"
            subject.contains("العربية") || subject.contains("القراءة") -> "📖"
            subject.contains("الرياضيات") || subject.contains("الحساب") -> "📐"
            subject.contains("Français") || subject.contains("français") -> "🇫🇷"
            subject.contains("العلوم") || subject.contains("العلمية") -> "🔬"
            subject.contains("التاريخ") || subject.contains("الجغرافيا") -> "🗺️"
            subject.contains("المدنية") -> "⚖️"
            subject.contains("الفنية") || subject.contains("النشيد") -> "🎨"
            subject.contains("البدنية") || subject.contains("الرياضية") -> "🏃"
            else -> "📚"
        }
    }
}

