package com.example.data.planning.canonical

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStreamReader

object OfficialAnnualPlanningRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter by lazy {
        moshi.adapter(CanonicalAnnualPlan::class.java)
    }

    private val cache = mutableMapOf<String, CanonicalAnnualPlan>()

    fun getLevelPlan(context: Context, levelCode: String): CanonicalAnnualPlan? {
        val normalizedCode = normalizeLevelCode(levelCode)
        if (cache.containsKey(normalizedCode)) {
            return cache[normalizedCode]
        }

        val gzFileName = "planning/plan_annuel_${normalizedCode}_canonical.json.gz"
        val rawFileName = "planning/plan_annuel_${normalizedCode}_canonical.json"
        val plan: CanonicalAnnualPlan? = try {
            try {
                context.assets.open(gzFileName).use { base ->
                    java.util.zip.GZIPInputStream(base).use { gz ->
                        InputStreamReader(gz, Charsets.UTF_8).use { reader ->
                            adapter.fromJson(reader.readText())
                        }
                    }
                }
            } catch (e: Exception) {
                context.assets.open(rawFileName).use { inputStream ->
                    InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                        adapter.fromJson(reader.readText())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (plan != null) {
            cache[normalizedCode] = plan
        }
        return plan
    }

    fun parseDirect(jsonContent: String): CanonicalAnnualPlan? {
        return adapter.fromJson(jsonContent)
    }

    fun normalizeLevelCode(level: String): String {
        val clean = level.trim().uppercase()
        return when {
            clean.startsWith("1") || clean.contains("أول") || clean.contains("1AP") -> "1AP"
            clean.startsWith("2") || clean.contains("ثان") || clean.contains("2AP") -> "2AP"
            clean.startsWith("3") || clean.contains("ثال") || clean.contains("3AP") -> "3AP"
            clean.startsWith("4") || clean.contains("رابع") || clean.contains("4AP") -> "4AP"
            clean.startsWith("5") || clean.contains("خامس") || clean.contains("5AP") -> "5AP"
            clean.startsWith("6") || clean.contains("سادس") || clean.contains("6AP") -> "6AP"
            else -> "1AP"
        }
    }

    fun getLevelTitle(levelCode: String): String {
        return when (normalizeLevelCode(levelCode)) {
            "1AP" -> "السنة الأولى"
            "2AP" -> "السنة الثانية"
            "3AP" -> "السنة الثالثة"
            "4AP" -> "السنة الرابعة"
            "5AP" -> "السنة الخامسة"
            "6AP" -> "السنة السادسة"
            else -> "السنة الأولى"
        }
    }

    fun getFasls(context: Context, levelCode: String): List<CanonicalFaslInfo> {
        return getLevelPlan(context, levelCode)?.fasls ?: emptyList()
    }

    fun getCalendarMarkers(context: Context, levelCode: String): List<CanonicalCalendarMarker> {
        return getLevelPlan(context, levelCode)?.reperes_calendrier ?: emptyList()
    }

    fun getCalendarMarkerForWeek(context: Context, levelCode: String, week: Int): CanonicalCalendarMarker? {
        return getCalendarMarkers(context, levelCode).find { it.semaine == week }
    }

    fun getSubjects(context: Context, levelCode: String): List<CanonicalSubject> {
        return getLevelPlan(context, levelCode)?.matieres ?: emptyList()
    }

    fun getDomains(context: Context, levelCode: String, subjectCode: String): List<CanonicalDomain> {
        val subject = getSubjects(context, levelCode).find { it.code == subjectCode || it.canonical_code == subjectCode || it.source_code == subjectCode }
        return subject?.domaines ?: emptyList()
    }

    fun getCompetences(context: Context, levelCode: String, domainCode: String): List<CanonicalCompetence> {
        val allDomains = getSubjects(context, levelCode).flatMap { it.domaines }
        val domain = allDomains.find { it.code == domainCode || it.canonical_domain_code == domainCode || it.source_domain_code == domainCode }
        return domain?.competences ?: emptyList()
    }

    fun getUnitsForWeek(context: Context, levelCode: String, week: Int): List<Triple<CanonicalSubject, CanonicalDomain, CanonicalUnit>> {
        val plan = getLevelPlan(context, levelCode) ?: return emptyList()
        val result = mutableListOf<Triple<CanonicalSubject, CanonicalDomain, CanonicalUnit>>()
        plan.matieres.forEach { subject ->
            subject.domaines.forEach { domain ->
                domain.competences.forEach { comp ->
                    comp.unites.filter { week in it.semaines }.forEach { unit ->
                        result.add(Triple(subject, domain, unit))
                    }
                }
            }
        }
        return result
    }

    fun getLessonInstancesForWeek(context: Context, levelCode: String, week: Int): List<CanonicalLessonInstance> {
        val plan = getLevelPlan(context, levelCode) ?: return emptyList()
        val list = plan.lesson_instances.filter { it.semaine == week }

        // Find all integration/assessment/remediation units in this week
        val synthesizedInteg = mutableListOf<CanonicalLessonInstance>()
        val seenKeys = mutableSetOf<String>()
        plan.matieres.forEach { subject ->
            subject.domaines.forEach { domain ->
                domain.competences.forEach { comp ->
                    comp.unites.forEach { unit ->
                        if (week in unit.semaines) {
                            val isIntegrationUnit = unit.type == "integration" ||
                                    unit.titre.contains("إدماج") ||
                                    unit.titre.contains("تقويم") ||
                                    unit.titre.contains("علاج")
                            if (isIntegrationUnit) {
                                val key = "${subject.code}_${domain.code}_${unit.titre}"
                                if (!seenKeys.contains(key)) {
                                    seenKeys.add(key)
                                    synthesizedInteg.add(
                                        CanonicalLessonInstance(
                                            niveau = levelCode,
                                            fasl = when (week) {
                                                in 1..13 -> 1
                                                in 14..26 -> 2
                                                else -> 3
                                            },
                                            matiere = subject.nom,
                                            domaine = domain.nom,
                                            competence = comp.enonce ?: "",
                                            semaine = week,
                                            titre_officiel = unit.titre,
                                            lesson_unit_id = "${subject.code}_${domain.code}_w${week}_synth",
                                            partie = 1,
                                            nombre_parties = 1,
                                            est_suite = false
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // If we have both regular lessons and synthesized integration units, combine them!
        if (list.isNotEmpty()) {
            val combined = mutableListOf<CanonicalLessonInstance>()
            combined.addAll(list)
            // Only add synthesized integration units whose titles aren't already covered in list
            // to avoid duplicates if any exists
            val existingTitles = list.map { it.titre_officiel.trim() }.toSet()
            synthesizedInteg.forEach { synth ->
                if (!existingTitles.contains(synth.titre_officiel.trim())) {
                    combined.add(synth)
                }
            }
            return combined
        }

        // If list is empty, return ALL units for this week (original template logic fallback)
        val synthesizedAll = mutableListOf<CanonicalLessonInstance>()
        val seenAllKeys = mutableSetOf<String>()
        plan.matieres.forEach { subject ->
            subject.domaines.forEach { domain ->
                domain.competences.forEach { comp ->
                    comp.unites.forEach { unit ->
                        if (week in unit.semaines) {
                            val key = "${subject.code}_${domain.code}_${unit.titre}"
                            if (!seenAllKeys.contains(key)) {
                                seenAllKeys.add(key)
                                synthesizedAll.add(
                                    CanonicalLessonInstance(
                                        niveau = levelCode,
                                        fasl = when (week) {
                                            in 1..13 -> 1
                                            in 14..26 -> 2
                                            else -> 3
                                        },
                                        matiere = subject.nom,
                                        domaine = domain.nom,
                                        competence = comp.enonce ?: "",
                                        semaine = week,
                                        titre_officiel = unit.titre,
                                        lesson_unit_id = "${subject.code}_${domain.code}_w${week}_synth",
                                        partie = 1,
                                        nombre_parties = 1,
                                        est_suite = false
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return synthesizedAll
    }

    fun getAllLessonInstances(context: Context, levelCode: String): List<CanonicalLessonInstance> {
        val plan = getLevelPlan(context, levelCode) ?: return emptyList()
        val allList = mutableListOf<CanonicalLessonInstance>()
        for (w in 1..38) {
            allList.addAll(getLessonInstancesForWeek(context, levelCode, w))
        }
        return allList
    }

    fun isIntegrationWeek(context: Context, levelCode: String, week: Int): Boolean {
        val plan = getLevelPlan(context, levelCode) ?: return false
        plan.matieres.forEach { subject ->
            subject.domaines.forEach { domain ->
                domain.competences.forEach { comp ->
                    comp.unites.forEach { unit ->
                        if (week in unit.semaines && unit.type == "integration") {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun mapMatiereToArabic(matiere: String): String {
        val clean = matiere.trim().uppercase()
        return when (clean) {
            "TIS", "التربية الإسلامية" -> "التربية الإسلامية"
            "AR", "ARA", "اللغة العربية", "العربية" -> "اللغة العربية"
            "MATH", "الرياضيات" -> "الرياضيات"
            "SCI", "التربية العلمية والتكنولوجية", "التربية العلمية", "النشاط العلمي", "العلوم" -> "العلوم الطبيعية"
            "CIV", "التربية المدنية" -> "التربية المدنية"
            "SOC", "التاريخ والجغرافيا", "الاجتماعيات" -> "التاريخ والجغرافيا"
            "ART", "التربية الفنية والبدنية", "التربية الفنية" -> "التربية الفنية"
            "EPS", "التربية البدنية" -> "التربية البدنية"
            "FRN", "FR", "FRANCAIS", "FRANÇAIS", "اللغة الفرنسية", "الفرنسية" -> "اللغة الفرنسية"
            else -> {
                val mTrim = matiere.trim()
                if (mTrim == "الاجتماعيات") "التاريخ والجغرافيا"
                else if (mTrim == "النشاط العلمي" || mTrim == "العلوم" || mTrim == "التربية العلمية") "العلوم الطبيعية"
                else matiere
            }
        }
    }

    fun mapDomainCodeToArabic(domainCode: String): String {
        val clean = domainCode.replace(Regex("^(TIS|MATH|SCI|AR|SOC|CIV|ART|FRN|FR)-", RegexOption.IGNORE_CASE), "")
            .replace("_", " ")
            .trim()
        return when (clean.uppercase()) {
            "CORAN" -> "القرآن الكريم"
            "HADITH" -> "الحديث الشريف"
            "AQIDA" -> "العقيدة الإسلامية"
            "FIQH" -> "العبادات / الفقه"
            "SIRA" -> "السيرة النبوية"
            "AKHLAQ" -> "الأخلاق والآداب الإسلامية"
            "SANTE" -> "الغذاء والصحة"
            "ENG_MEC" -> "الآليات والأجهزة"
            "MATTER" -> "المادة وتحولاتها"
            "CALC" -> "الحساب"
            "MEAS" -> "القياس"
            "GEOM" -> "الهندسة والفضاء"
            "ORAL" -> "التعبير الشفهي"
            "READ" -> "القراءة"
            "WRITE" -> "الكتابة والإنتاج الكتابي"
            "GRAM" -> "القواعد النحوية"
            "CONJ" -> "التصريف والصرف"
            "ORTH" -> "الإملاء والرسم الإملائي"
            "VOC" -> "المعجم والمفردات"
            "CIV" -> "التربية المدنية"
            "SOC" -> "التاريخ والجغرافيا"
            else -> domainCode
        }
    }

    fun getLevelNumber(levelCode: String): Int {
        return when (normalizeLevelCode(levelCode)) {
            "1AP" -> 1
            "2AP" -> 2
            "3AP" -> 3
            "4AP" -> 4
            "5AP" -> 5
            "6AP" -> 6
            else -> 1
        }
    }

    fun getLevelCodeFromInt(level: Int): String {
        return when (level) {
            1 -> "1AP"
            2 -> "2AP"
            3 -> "3AP"
            4 -> "4AP"
            5 -> "5AP"
            6 -> "6AP"
            else -> "1AP"
        }
    }

    fun getWeekType(context: Context, levelCode: String, week: Int): String {
        val marker = getCalendarMarkerForWeek(context, levelCode, week)
        if (marker != null) return marker.type
        if (isIntegrationWeek(context, levelCode, week)) return "integration"
        val instances = getLessonInstancesForWeek(context, levelCode, week)
        return if (instances.isNotEmpty()) "lecon" else "lecon"
    }

    fun getWeekTypeLabel(type: String): String {
        return when (type) {
            "accueil" -> "استقبال وتهيئة"
            "lecon" -> "دروس وتدريس"
            "integration" -> "إدماج وتقويم ومعالجة"
            "examen" -> "اختبارات وامتحانات"
            "vacances" -> "عطلة مدرسية"
            "exception_calendrier" -> "محطة تقويمية خاصة"
            else -> "أسبوع دراسي"
        }
    }

    fun getTeachingWeeks(context: Context, levelCode: String): List<Int> {
        val plan = getLevelPlan(context, levelCode) ?: return (1..38).toList()
        val nonTeachingWeeks = plan.reperes_calendrier
            .filter { it.type in listOf("vacances", "examen", "accueil") }
            .map { it.semaine }
            .toSet()
        return (1..plan.niveau.semaines_total).filter { it !in nonTeachingWeeks }
    }

    fun getWeeksForFasl(context: Context, levelCode: String, faslNumber: Int): List<Int> {
        return when (faslNumber) {
            1 -> (1..13).toList()
            2 -> (14..26).toList()
            3 -> (27..38).toList()
            else -> emptyList()
        }
    }

    fun getLocalMarkerForSubject(context: Context, levelCode: String, subjectName: String, week: Int): CanonicalLocalMarker? {
        val plan = getLevelPlan(context, levelCode) ?: return null
        val subject = plan.matieres.find { it.nom == subjectName || it.code == subjectName } ?: return null
        return subject.reperes_locaux.find { it.semaine == week }
    }

    fun searchLessons(context: Context, levelCode: String, query: String): List<CanonicalLessonInstance> {
        if (query.isBlank()) return emptyList()
        val plan = getLevelPlan(context, levelCode) ?: return emptyList()
        val cleanQuery = query.trim().lowercase()
        return plan.lesson_instances.filter {
            it.titre_officiel.lowercase().contains(cleanQuery) ||
            it.matiere.lowercase().contains(cleanQuery) ||
            it.domaine.lowercase().contains(cleanQuery) ||
            it.competence.lowercase().contains(cleanQuery) ||
            it.lesson_unit_id.lowercase().contains(cleanQuery)
        }
    }

    fun findUnitForLesson(context: Context, levelCode: String, lesson: CanonicalLessonInstance): CanonicalUnit? {
        val plan = getLevelPlan(context, levelCode) ?: return null
        for (subject in plan.matieres) {
            for (domain in subject.domaines) {
                for (comp in domain.competences) {
                    for (unit in comp.unites) {
                        if (lesson.semaine in unit.semaines && 
                            (unit.titre.contains(lesson.titre_officiel) || lesson.titre_officiel.contains(unit.titre) || unit.titre == lesson.titre_officiel)) {
                            return unit
                        }
                    }
                }
            }
        }
        return null
    }
}
