package com.example.data.planning

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import com.example.data.planning.canonical.CanonicalLessonInstance
import com.example.data.planning.canonical.OfficialAnnualPlanningRepository
import com.example.data.planning.canonical.PreparationFileRoot
import com.example.data.planning.canonical.PreparationItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

object LessonPreparationRepository {

    data class PrepKey(
        val level: Int,
        val unitId: String,
        val week: Int,
        val part: Int
    )

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val rootAdapter by lazy {
        moshi.adapter(PreparationFileRoot::class.java)
    }

    private val prepCache = mutableMapOf<PrepKey, LessonProposalData>()
    private val rawPrepItemsCache = mutableMapOf<String, PreparationItem>()
    private val prepsByLevelAndWeek = mutableMapOf<Pair<Int, Int>, MutableList<Pair<PreparationItem, LessonProposalData>>>()
    private val levelInstancesCache = mutableMapOf<String, List<CanonicalLessonInstance>>()
    private val loadedLevels = mutableSetOf<String>()
    private var isInitialized = false

    private fun openAssetAsText(context: Context, path: String): String? {
        return try {
            val isGz = path.endsWith(".gz", ignoreCase = true)
            val baseStream = context.assets.open(path)
            val stream = if (isGz) GZIPInputStream(baseStream) else baseStream
            stream.use { s ->
                InputStreamReader(s, Charsets.UTF_8).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Loads the official new preparation JSON file for a given level (1AP to 5AP) from assets.
     */
    private fun loadPreparationsForLevel(context: Context, levelCode: String) {
        val normalizedCode = OfficialAnnualPlanningRepository.normalizeLevelCode(levelCode)
        if (loadedLevels.contains(normalizedCode)) return

        val possibleFileNames = listOf(
            "preparations/${normalizedCode}_PREPARATIONS_RUNTIME_LIGHT.json.gz",
            "preparations/${normalizedCode}_PREPARATIONS_RUNTIME_LIGHT.json",
            "planning/${normalizedCode}_PREPARATIONS_FINAL_CORRECTED_READY_AI_STUDIO.json",
            "planning/preparations_${normalizedCode}.json",
            "planning/${normalizedCode}_PREPARATIONS.json"
        )

        var prepList: List<PreparationItem> = emptyList()
        for (fileName in possibleFileNames) {
            try {
                val content = openAssetAsText(context, fileName)
                if (content != null) {
                    val root = rootAdapter.fromJson(content)
                    if (root != null && root.preparations.isNotEmpty()) {
                        prepList = root.preparations
                        break
                    }
                }
            } catch (e: Exception) {
                // Try next file name variant
            }
        }

        val levelInt = when (normalizedCode) {
            "1AP" -> 1
            "2AP" -> 2
            "3AP" -> 3
            "4AP" -> 4
            "5AP" -> 5
            "6AP" -> 6
            else -> 1
        }

        for (prep in prepList) {
            val proposal = convertPreparationToProposal(prep)
            val off = prep.official

            rawPrepItemsCache[prep.preparation_id] = prep

            val key = PrepKey(
                level = levelInt,
                unitId = off.lesson_unit_id,
                week = off.semaine,
                part = off.partie
            )
            prepCache[key] = proposal

            val weekKey = Pair(levelInt, off.semaine)
            val listForWeek = prepsByLevelAndWeek.getOrPut(weekKey) { mutableListOf() }
            listForWeek.add(Pair(prep, proposal))

            // Fallback alias registration for 1AP (ART-TIC vs TIC-GEN)
            if (levelInt == 1 && off.lesson_unit_id.startsWith("1AP|TIC-GEN|")) {
                val alias1 = off.lesson_unit_id.replace("1AP|TIC-GEN|", "1AP|ART-TIC|")
                val alias2 = off.lesson_unit_id.replace("1AP|TIC-GEN|", "1AP|ART-TIC-GEN|")
                prepCache.putIfAbsent(PrepKey(levelInt, alias1, off.semaine, off.partie), proposal)
                prepCache.putIfAbsent(PrepKey(levelInt, alias2, off.semaine, off.partie), proposal)
            }
        }

        loadedLevels.add(normalizedCode)
    }

    /**
     * Initializes the repository by importing the new preparations JSON files for all levels (1AP-5AP)
     * and pre-populating the lesson preparation cache.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                for (level in 1..6) {
                    val levelCode = OfficialAnnualPlanningRepository.getLevelCodeFromInt(level)
                    loadPreparationsForLevel(context, levelCode)

                    val instancesForLevel = mutableListOf<CanonicalLessonInstance>()
                    for (week in 1..38) {
                        instancesForLevel.addAll(OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, week))
                    }
                    levelInstancesCache[levelCode] = instancesForLevel
                }
                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Converts a new official PreparationItem model directly into the LessonProposalData
     * required by the preparation sheet UI.
     */
    fun convertPreparationToProposal(prep: PreparationItem): LessonProposalData {
        val off = prep.official
        val fiche = prep.fiche
        val isFrench = isMatiereFrench(off.matiere)

        val levelTitle = OfficialAnnualPlanningRepository.getLevelTitle(off.niveau)
        val domain = off.domaine_label ?: off.domaine
        val sectionModule = off.competence_enonce ?: off.competence ?: ""
        val subject = if (off.matiere.trim().uppercase() == "TIC") {
            "التربية الفنية"
        } else {
            off.matiere_label ?: mapMatiereToSubject(off.matiere)
        }

        val finalTopic = if (off.partie > 1 || off.est_suite) {
            if (isFrench) "${off.titre_officiel} (Suite)"
            else "${off.titre_officiel} (تابع)"
        } else {
            off.titre_officiel
        }

        val sessionText = if (isFrench) {
            "Séance ${off.partie}/${off.nombre_parties}"
        } else {
            "الجزء ${off.partie} من ${off.nombre_parties}"
        }

        val durationText = if (isFrench) "${fiche.duree_minutes} min" else "${fiche.duree_minutes} دقيقة"

        val referenceText = if (fiche.moyens.isNotEmpty()) {
            fiche.moyens.joinToString("، ")
        } else {
            if (isFrench) "Manuel officiel" else "الكتاب المدرسي الرسمي"
        }

        val teachingAidsList = mutableListOf<String>()
        if (fiche.moyens.isNotEmpty()) {
            teachingAidsList.add(fiche.moyens.joinToString("، "))
        }
        if (fiche.vocabulaire_cle.isNotEmpty()) {
            val vocabLabel = if (isFrench) "المفردات الأساسية: " else "المفردات الأساسية: "
            teachingAidsList.add(vocabLabel + fiche.vocabulaire_cle.joinToString("، "))
        }
        val teachingAidsText = if (teachingAidsList.isNotEmpty()) {
            teachingAidsList.joinToString(" | ")
        } else {
            if (isFrench) "Tableau, Manuel" else "السبورة، الكتاب المدرسي"
        }

        val specificObjective = fiche.objectif_specifique

        val procedureSteps = prep.deroulement.etapes.map { etape ->
            val teacherActivities = if (etape.activites_enseignant.size > 1) {
                etape.activites_enseignant.joinToString("\n• ", prefix = "• ")
            } else {
                etape.activites_enseignant.joinToString("\n")
            }

            val studentActivities = if (etape.activites_eleve.size > 1) {
                etape.activites_eleve.joinToString("\n• ", prefix = "• ")
            } else {
                etape.activites_eleve.joinToString("\n")
            }

            val stepDuration = if (etape.duree_minutes > 0) {
                if (isFrench) "${etape.duree_minutes} min" else "${etape.duree_minutes} د"
            } else ""

            LessonProcedureStep(
                phaseName = etape.libelle,
                teacherActivity = teacherActivities,
                studentActivity = studentActivities,
                durationText = stepDuration
            )
        }

        val currentWeekFocus = prep.continuity?.current_week_focus ?: ""

        return LessonProposalData(
            levelTitle = levelTitle,
            domain = domain,
            sectionModule = sectionModule,
            subject = subject,
            topic = finalTopic,
            currentWeekSubPhase = currentWeekFocus,
            sessionInfo = sessionText,
            durationText = durationText,
            referenceText = referenceText,
            teachingAids = teachingAidsText,
            specificObjective = specificObjective,
            procedureSteps = procedureSteps
        )
    }

    /**
     * Determines whether a given subject code represents French.
     */
    private fun isMatiereFrench(matiere: String): Boolean {
        val m = matiere.trim().uppercase()
        return m == "FRN" || m == "FR" || m == "FRANÇAIS" || m == "FRANCAIS"
    }

    /**
     * Fuzzy matches subjects between the canonical database and localized representations.
     */
    private fun subjectsMatch(itemSubject: String, mappedSubject: String): Boolean {
        val s1 = itemSubject.trim().lowercase()
        val s2 = mappedSubject.trim().lowercase()
        if (s1 == s2) return true
        if (s1.contains(s2) || s2.contains(s1)) return true
        if ((s1.contains("علم") || s1.contains("علوم")) && (s2.contains("علم") || s2.contains("علوم") || s2.contains("sci"))) return true
        if ((s1.contains("فني") || s1.contains("بدن") || s1.contains("رسم") || s1.contains("يدوي")) &&
            (s2.contains("فني") || s2.contains("بدن") || s2.contains("رسم") || s2.contains("art") || s2.contains("eps"))) return true
        if ((s1.contains("تاريخ") || s1.contains("جغراف")) && (s2.contains("soc") || s2.contains("تاريخ") || s2.contains("جغراف"))) return true
        if ((s1.contains("إسلام") || s1.contains("اسلام") || s1.contains("قرآن") || s1.contains("حديث") || s1.contains("سيرة") || s1.contains("تهذيب") || s1.contains("عبادات")) &&
            (s2.contains("إسلام") || s2.contains("اسلام") || s2.contains("tis"))) return true
        if ((s1.contains("مدني") || s1.contains("مدنية")) && (s2.contains("مدني") || s2.contains("civ"))) return true
        if ((s1.contains("عرب") || s1.contains("لغة")) && (s2.contains("عرب") || s2.contains("ar"))) return true
        return false
    }

    /**
     * Maps canonical subject codes to standard localized subject names.
     */
    fun mapMatiereToSubject(matiere: String): String {
        return when (matiere.trim().uppercase()) {
            "TIS" -> "التربية الإسلامية"
            "AR", "ARA" -> "اللغة العربية"
            "MATH" -> "الرياضيات"
            "SCI" -> "التربية العلمية والتكنولوجية"
            "CIV" -> "التربية المدنية"
            "SOC" -> "التاريخ والجغرافيا"
            "ART" -> "التربية الفنية"
            "EPS" -> "التربية البدنية"
            "TIC" -> "التربية الفنية"
            "FRN", "FR" -> "Français"
            else -> matiere
        }
    }

    /**
     * Computes the Levenshtein distance between two strings for fuzzy matching.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Normalizes 1AP lesson_unit_id by removing the "ART-" prefix when applicable,
     * allowing e.g. 1AP|ART-TIC-GEN|K1|U01 or 1AP|ART-TIC|K1|U01 to match 1AP|TIC-GEN|K1|U01.
     */
    private fun normalizeUnitIdFor1AP(unitId: String): String {
        return unitId
            .replace("1AP|ART-TIC-GEN|", "1AP|TIC-GEN|")
            .replace("1AP|ART-TIC|", "1AP|TIC-GEN|")
    }

    /**
     * Retrieves a lesson preparation directly from the cache using a stable composite key.
     */
    fun getPreparation(level: Int, unitId: String, week: Int, part: Int = 1): LessonProposalData? {
        val key = PrepKey(level, unitId, week, part)
        prepCache[key]?.let { return it }

        // Fallback for 1AP alias normalization (e.g. ART-TIC vs TIC-GEN)
        if (level == 1) {
            val normId = normalizeUnitIdFor1AP(unitId)
            if (normId != unitId) {
                prepCache[PrepKey(level, normId, week, part)]?.let { return it }
            }
        }
        return null
    }

    /**
     * Retrieves the raw PreparationItem if needed for advanced pedagogical inspection.
     */
    fun getRawPreparation(preparationId: String): PreparationItem? {
        return rawPrepItemsCache[preparationId]
    }

    /**
     * Resolves and retrieves the lesson preparation for a given lesson and selected week,
     * matching against the 5 official new preparation JSON files with multi-tier resolution.
     */
    fun getPreparationForLesson(context: Context, lesson: AnnualLessonItem, selectedWeek: Int): LessonProposalData {
        initialize(context)

        val level = lesson.level
        val week = selectedWeek
        val lessonId = lesson.id.trim()
        val lessonTitle = lesson.title.trim()

        // 1. Direct composite key lookup by lesson.id (unit_id) and week
        prepCache[PrepKey(level, lessonId, week, 1)]?.let { return it }
        prepCache[PrepKey(level, lessonId, week, 2)]?.let { return it }
        prepCache[PrepKey(level, lessonId, week, 3)]?.let { return it }
        prepCache[PrepKey(level, lessonId, week, 4)]?.let { return it }

        // Fallback for 1AP normalized unit_id
        if (level == 1) {
            val normId = normalizeUnitIdFor1AP(lessonId)
            if (normId != lessonId) {
                prepCache[PrepKey(level, normId, week, 1)]?.let { return it }
                prepCache[PrepKey(level, normId, week, 2)]?.let { return it }
                prepCache[PrepKey(level, normId, week, 3)]?.let { return it }
                prepCache[PrepKey(level, normId, week, 4)]?.let { return it }
            }
        }

        // 2. Direct lookup in preps for this level and week
        val weekPreps = prepsByLevelAndWeek[Pair(level, week)]
        if (!weekPreps.isNullOrEmpty()) {
            // Match by unit_id (direct first, then normalized for 1AP)
            val byUnit = weekPreps.find { (prep, _) ->
                prep.official.lesson_unit_id.equals(lessonId, ignoreCase = true) ||
                lessonId.startsWith(prep.official.lesson_unit_id) ||
                prep.official.lesson_unit_id.startsWith(lessonId)
            } ?: if (level == 1) {
                val normLessonId = normalizeUnitIdFor1AP(lessonId)
                weekPreps.find { (prep, _) ->
                    val normPrepId = normalizeUnitIdFor1AP(prep.official.lesson_unit_id)
                    normPrepId.equals(normLessonId, ignoreCase = true) ||
                    normLessonId.startsWith(normPrepId) ||
                    normPrepId.startsWith(normLessonId)
                }
            } else null
            if (byUnit != null) return byUnit.second

            // Match by exact subject and exact title
            val bySubjAndTitle = weekPreps.find { (prep, _) ->
                subjectsMatch(lesson.subject, mapMatiereToSubject(prep.official.matiere)) &&
                prep.official.titre_officiel.trim() == lessonTitle
            }
            if (bySubjAndTitle != null) return bySubjAndTitle.second

            // Match by title containing or fuzzy matching
            val byTitleMatch = weekPreps.find { (prep, _) ->
                subjectsMatch(lesson.subject, mapMatiereToSubject(prep.official.matiere)) &&
                (prep.official.titre_officiel.contains(lessonTitle) ||
                 lessonTitle.contains(prep.official.titre_officiel) ||
                 levenshteinDistance(prep.official.titre_officiel, lessonTitle) < 6)
            }
            if (byTitleMatch != null) return byTitleMatch.second

            // Match by title alone across the week if unique
            val byTitleOnly = weekPreps.find { (prep, _) ->
                prep.official.titre_officiel.trim() == lessonTitle ||
                prep.official.titre_officiel.contains(lessonTitle) ||
                lessonTitle.contains(prep.official.titre_officiel)
            }
            if (byTitleOnly != null) return byTitleOnly.second

            // Match by subject within the week
            val bySubject = weekPreps.find { (prep, _) ->
                subjectsMatch(lesson.subject, mapMatiereToSubject(prep.official.matiere))
            }
            if (bySubject != null) return bySubject.second
        }

        // 3. Fallback: Check canonical instances
        val levelCode = OfficialAnnualPlanningRepository.getLevelCodeFromInt(lesson.level)
        val instances = levelInstancesCache[levelCode]
        if (instances != null) {
            val matchedInstance = instances.find { inst ->
                inst.semaine == selectedWeek &&
                subjectsMatch(lesson.subject, mapMatiereToSubject(inst.matiere)) &&
                (inst.titre_officiel.contains(lesson.title) || lesson.title.contains(inst.titre_officiel) ||
                 levenshteinDistance(inst.titre_officiel, lesson.title) < 6)
            } ?: instances.find { inst ->
                inst.semaine == selectedWeek &&
                subjectsMatch(lesson.subject, mapMatiereToSubject(inst.matiere))
            }

            if (matchedInstance != null) {
                prepCache[PrepKey(lesson.level, matchedInstance.lesson_unit_id, selectedWeek, matchedInstance.partie)]?.let {
                    return it
                }
            }
        }

        // 4. Safe fallback to default dynamic template generator if completely unmatched
        return LessonProposalRepository.getProposalForLesson(lesson, selectedWeek)
    }
}

