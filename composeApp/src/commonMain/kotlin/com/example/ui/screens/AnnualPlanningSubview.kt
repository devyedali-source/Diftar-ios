package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.concours.ConcoursRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.ClassSection
import com.example.data.planning.AnnualLessonItem
import com.example.data.planning.AnnualPlanningProgress
import com.example.data.planning.AnnualPlanningProgressManager
import com.example.data.planning.AnnualPlanningRepository
import com.example.data.planning.canonical.CanonicalAnnualPlan
import com.example.data.planning.canonical.CanonicalLessonInstance
import com.example.data.planning.canonical.CanonicalLocalMarker
import com.example.data.planning.canonical.CanonicalSubject
import com.example.data.planning.canonical.CanonicalUnit
import com.example.data.planning.canonical.OfficialAnnualPlanningRepository
import com.example.ui.theme.BentoGrayOutline
import com.example.ui.theme.BentoLightLavender
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDesc

// -----------------------------------------------------------------------------
// MAIN ENTRY POINT: ANNUAL PLANNING SUBVIEW
// -----------------------------------------------------------------------------
@Composable
fun AnnualPlanningSubview(
    activeClass: ClassSection,
    viewModel: com.example.ui.TeacherViewModel? = null,
    onUpdateLevel: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val level = remember(activeClass.level, activeClass.id, activeClass.name) {
        AnnualPlanningRepository.resolveLevel(activeClass)
    }

    if (level !in 1..6) {
        UnassignedLevelCard(
            activeClass = activeClass,
            onSelectLevel = { lvl ->
                onUpdateLevel?.invoke(lvl)
                viewModel?.updateClassSectionLevel(activeClass.id, lvl)
            }
        )
        return
    }

    val levelCode = remember(level) { OfficialAnnualPlanningRepository.getLevelCodeFromInt(level) }
    val levelTitle = remember(levelCode) { OfficialAnnualPlanningRepository.getLevelTitle(levelCode) }
    val plan = remember(levelCode) { OfficialAnnualPlanningRepository.getLevelPlan(context, levelCode) }

    var progressState by remember(activeClass.id) {
        mutableStateOf(AnnualPlanningProgressManager.getProgress(context, activeClass.id))
    }

    fun refreshProgress() {
        progressState = AnnualPlanningProgressManager.getProgress(context, activeClass.id)
    }

    AnnualPlanningScreenContent(
        activeClass = activeClass,
        level = level,
        levelCode = levelCode,
        levelTitle = levelTitle,
        plan = plan,
        progressState = progressState,
        onRefreshProgress = { refreshProgress() },
        viewModel = viewModel
    )
}

// -----------------------------------------------------------------------------
// SCREEN CONTENT WITH TERMS ACCORDION & COLOR KEY
// -----------------------------------------------------------------------------
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DYNAMIC NAV LEVEL ENUM
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
private enum class NavLevel {
    TERMS, WEEKS, SUBJECTS, DOMAINS, LESSONS, PREPARATION
}

private data class RestoredPlanPlace(
    val levels: List<NavLevel>,
    val term: Int?,
    val week: Int?,
    val subject: CanonicalSubject?,
    val domain: String?,
    val lesson: CanonicalLessonInstance?
)

@Composable
private fun AnnualPlanningScreenContent(
    activeClass: ClassSection,
    level: Int,
    levelCode: String,
    levelTitle: String,
    plan: CanonicalAnnualPlan?,
    progressState: AnnualPlanningProgress,
    onRefreshProgress: () -> Unit,
    viewModel: com.example.ui.TeacherViewModel? = null
) {
    val context = LocalContext.current


    // Screen Security Enforcer (FLAG_SECURE) to prevent screenshotting, recording, and recent app previews (Bypassed temporarily until Sep 30, 2026)
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val calendar = java.util.Calendar.getInstance()
        val isBeforeOct2026 = calendar.get(java.util.Calendar.YEAR) < 2026 || 
            (calendar.get(java.util.Calendar.YEAR) == 2026 && calendar.get(java.util.Calendar.MONTH) < java.util.Calendar.OCTOBER)
        
        if (!isBeforeOct2026) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (!isBeforeOct2026) {
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    // School Start Date & Current Week
    val schoolStartDateStr = remember(activeClass.id, activeClass.academicYear, progressState) {
        AnnualPlanningProgressManager.getSchoolStartDateString(context, activeClass.id, activeClass.academicYear)
    }
    val calculatedCurrentWeek = remember(activeClass.id, activeClass.academicYear, schoolStartDateStr) {
        AnnualPlanningProgressManager.calculateCurrentWeekFromStartDate(context, activeClass.id, activeClass.academicYear)
    }

    // Drill-down State Machine History
    val lastPlaceKey = "plan_" + activeClass.id + "_"
    val restoredPlace = remember(activeClass.id) {
        val store = com.example.data.state.LastPlaceStore
        val levels = mutableListOf(NavLevel.TERMS)
        var rTerm: Int? = null
        var rWeek: Int? = null
        var rSubject: CanonicalSubject? = null
        var rDomain: String? = null
        var rLesson: CanonicalLessonInstance? = null
        try {
            val savedLevels = (store.getString(context, lastPlaceKey + "nav") ?: "")
                .split(",")
                .mapNotNull { name -> NavLevel.values().find { it.name == name } }
            val t = store.getInt(context, lastPlaceKey + "term")
            if (savedLevels.contains(NavLevel.WEEKS) && t != null && activeClass.isTermUnlocked(t)) {
                rTerm = t
                levels.add(NavLevel.WEEKS)
                val w = store.getInt(context, lastPlaceKey + "week")
                if (savedLevels.contains(NavLevel.SUBJECTS) && w != null && w in 1..38) {
                    rWeek = w
                    levels.add(NavLevel.SUBJECTS)
                    val sCode = store.getString(context, lastPlaceKey + "subject")
                    val s = if (sCode != null) OfficialAnnualPlanningRepository.getSubjects(context, levelCode).find { it.code == sCode } else null
                    if (savedLevels.contains(NavLevel.DOMAINS) && s != null) {
                        rSubject = s
                        levels.add(NavLevel.DOMAINS)
                        val d = store.getString(context, lastPlaceKey + "domain")
                        if (savedLevels.contains(NavLevel.LESSONS) && d != null) {
                            rDomain = d
                            levels.add(NavLevel.LESSONS)
                            val lId = store.getString(context, lastPlaceKey + "lesson")
                            val lPart = store.getInt(context, lastPlaceKey + "lesson_part") ?: 1
                            val l = if (lId != null) OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, w)
                                .find { it.lesson_unit_id == lId && it.partie == lPart } else null
                            if (savedLevels.contains(NavLevel.PREPARATION) && l != null) {
                                rLesson = l
                                levels.add(NavLevel.PREPARATION)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        RestoredPlanPlace(levels, rTerm, rWeek, rSubject, rDomain, rLesson)
    }
    val navHistory = remember { mutableStateListOf<NavLevel>(*restoredPlace.levels.toTypedArray()) }
    var selectedTerm by remember { mutableStateOf<Int?>(restoredPlace.term) }
    var selectedWeek by remember { mutableStateOf<Int?>(restoredPlace.week) }
    var selectedSubject by remember { mutableStateOf<CanonicalSubject?>(restoredPlace.subject) }
    var selectedDomain by remember { mutableStateOf<String?>(restoredPlace.domain) }
    var selectedLesson by remember { mutableStateOf<CanonicalLessonInstance?>(restoredPlace.lesson) }

    // Intercept system back button to back-navigate through the hierarchy
    BackHandler(enabled = navHistory.size > 1) {
        if (navHistory.size > 1) {
            navHistory.removeAt(navHistory.lastIndex)
        }
    }

    fun goBack() {
        if (navHistory.size > 1) {
            navHistory.removeAt(navHistory.lastIndex)
        }
    }

    LaunchedEffect(navHistory.toList(), selectedTerm, selectedWeek, selectedSubject?.code, selectedDomain, selectedLesson?.lesson_unit_id, selectedLesson?.partie) {
        val store = com.example.data.state.LastPlaceStore
        store.putString(context, lastPlaceKey + "nav", navHistory.joinToString(",") { it.name })
        store.putInt(context, lastPlaceKey + "term", selectedTerm)
        store.putInt(context, lastPlaceKey + "week", selectedWeek)
        store.putString(context, lastPlaceKey + "subject", selectedSubject?.code)
        store.putString(context, lastPlaceKey + "domain", selectedDomain)
        store.putString(context, lastPlaceKey + "lesson", selectedLesson?.lesson_unit_id)
        store.putInt(context, lastPlaceKey + "lesson_part", selectedLesson?.partie)
    }

    val currentLevel = navHistory.last()

    if (currentLevel == NavLevel.PREPARATION && selectedLesson != null && selectedWeek != null) {
        // Render the preparation sheet itself inside the same view space, with correct path and FLAG_SECURE
        val dummyItem = remember(selectedLesson, selectedWeek) {
            AnnualLessonItem(
                id = selectedLesson!!.lesson_unit_id,
                level = level,
                week = selectedWeek!!,
                weekEnd = selectedWeek!!,
                subject = selectedLesson!!.matiere,
                domain = selectedLesson!!.domaine,
                title = selectedLesson!!.titre_officiel,
                competency = selectedLesson!!.competence,
                isFrench = isMatiereFrench(selectedLesson!!.matiere)
            )
        }
        LessonProposalScreen(
            lesson = dummyItem,
            currentWeek = selectedWeek!!,
            classId = activeClass.id,
            onBack = { goBack() }
        )
        return
    }

    var showConcoursGuide by remember {
        mutableStateOf(
            level == 6 &&
            (activeClass.isTermUnlocked(2) || activeClass.isTermUnlocked(3)) &&
            com.example.data.state.LastPlaceStore.getBoolean(context, lastPlaceKey + "concours")
        )
    }
    LaunchedEffect(showConcoursGuide) {
        com.example.data.state.LastPlaceStore.putBoolean(context, lastPlaceKey + "concours", showConcoursGuide)
    }

    if (showConcoursGuide) {
        ConcoursGuideScreen(onBack = { showConcoursGuide = false })
        return
    }

    val isUnlocked = activeClass.isTermUnlocked(2) || activeClass.isTermUnlocked(3)
    var subjectsCount by remember { mutableStateOf(0) }
    var totalExercisesCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val (sCount, eCount) = withContext(Dispatchers.IO) {
            ConcoursRepository.getSubjects(context).size to ConcoursRepository.getTotalExercisesCount(context)
        }
        subjectsCount = sCount
        totalExercisesCount = eCount
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // STICKY HIERARCHICAL HEADER
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, BentoGrayOutline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Row: Back button, Section Title, and dynamic preparation counter badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (navHistory.size > 1) {
                            IconButton(
                                onClick = { goBack() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(BentoLightLavender, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "الرجوع",
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "التخطيط السنوي للمنهاج الرسمي",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = BentoPrimary
                        )
                    }

                    // Dynamic Count Badge (الأعداد تُحسب ديناميكيًا من المجموعات الفعلية)
                    val activeCount = remember(currentLevel, selectedTerm, selectedWeek, selectedSubject, selectedDomain, levelCode) {
                        when (currentLevel) {
                            NavLevel.TERMS -> OfficialAnnualPlanningRepository.getAllLessonInstances(context, levelCode).size
                            NavLevel.WEEKS -> OfficialAnnualPlanningRepository.getAllLessonInstances(context, levelCode).count { it.fasl == selectedTerm }
                            NavLevel.SUBJECTS -> OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, selectedWeek ?: 1).size
                            NavLevel.DOMAINS -> OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, selectedWeek ?: 1).count { it.matiere.equals(selectedSubject?.nom, ignoreCase = true) || it.matiere.equals(selectedSubject?.code, ignoreCase = true) }
                            NavLevel.LESSONS -> OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, selectedWeek ?: 1).count {
                                (it.matiere.equals(selectedSubject?.nom, ignoreCase = true) || it.matiere.equals(selectedSubject?.code, ignoreCase = true)) &&
                                (it.domaine.equals(selectedDomain, ignoreCase = true) || getDomainArabicLabel(context, levelCode, it.domaine).equals(selectedDomain, ignoreCase = true))
                            }
                            else -> 0
                        }
                    }

                    val countLabel = if (activeCount > 10) "تحضيراً" else "تحاضير"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "$activeCount $countLabel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Path Breadcrumbs Trail: الفصل ← الأسبوع ← المادة ← المجال
                val breadcrumbsTrail = remember(navHistory.size, selectedTerm, selectedWeek, selectedSubject, selectedDomain) {
                    val trail = mutableListOf<String>()
                    if (navHistory.size > 1 && selectedTerm != null) {
                        trail.add("الفصل ${getFaslArabicName(selectedTerm!!)}")
                    }
                    if (navHistory.size > 2 && selectedWeek != null) {
                        trail.add("الأسبوع $selectedWeek")
                    }
                    if (navHistory.size > 3 && selectedSubject != null) {
                        trail.add(OfficialAnnualPlanningRepository.mapMatiereToArabic(selectedSubject!!.nom))
                    }
                    if (navHistory.size > 4 && selectedDomain != null) {
                        trail.add(getDomainArabicLabel(context, levelCode, selectedDomain!!))
                    }
                    trail.joinToString(" ← ")
                }

                if (breadcrumbsTrail.isNotEmpty()) {
                    Text(
                        text = breadcrumbsTrail,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoPrimaryDesc,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // SCROLLABLE LIST OF CONTENT BASED ON navHistory.last()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentLevel) {
                NavLevel.TERMS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            TermsListScreen(
                                context = context,
                                activeClass = activeClass,
                                levelCode = levelCode,
                                onTermSelected = { termNum ->
                                    selectedTerm = termNum
                                    navHistory.add(NavLevel.WEEKS)
                                },
                                onLockedTermSelected = { termNum ->
                                    Toast.makeText(
                                        context,
                                        "لفتح هذا الفصل، اذهب إلى صفحة النتائج واضغط على زر اختيار الفصل، ومنها يمكنك تفعيل اشتراكك.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                footerContent = if (level == 6) {
                                    {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .widthIn(max = 500.dp)
                                                    .clickable {
                                                        if (isUnlocked) {
                                                            showConcoursGuide = true
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "لفتح هذه الصفحة، اذهب إلى صفحة النتائج واضغط على زر اختيار الفصل، ومنها يمكنك تفعيل اشتراكك.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                border = BorderStroke(1.5.dp, BentoPrimary.copy(alpha = 0.4f)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "دليل النجاح في مسابقة دخول سنة أولى إعدادية",
                                                            fontSize = 15.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = BentoPrimary
                                                        )
                                                        Text(
                                                            text = "$subjectsCount مواد • $totalExercisesCount تمرين",
                                                            fontSize = 12.5.sp,
                                                            color = BentoPrimaryDesc,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        com.example.data.concours.ConcoursRepository.lastError?.let { err ->
                                                            Text(
                                                                text = err,
                                                                fontSize = 10.sp,
                                                                color = Color(0xFFC62828)
                                                            )
                                                        }
                                                    }
                                                    if (!isUnlocked) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFFFFEBEE),
                                                            modifier = Modifier.size(34.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Lock,
                                                                    contentDescription = "مقفل",
                                                                    tint = Color(0xFFD32F2F),
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
                NavLevel.WEEKS -> {
                    WeeksListScreen(
                        context = context,
                        levelCode = levelCode,
                        selectedTerm = selectedTerm ?: 1,
                        calculatedCurrentWeek = calculatedCurrentWeek,
                        progressState = progressState,
                        onWeekSelected = { weekNum ->
                            selectedWeek = weekNum
                            navHistory.add(NavLevel.SUBJECTS)
                        },
                        onToggleWeekCompleted = { weekNum ->
                            AnnualPlanningProgressManager.toggleWeekCompletion(context, activeClass.id, weekNum)
                            onRefreshProgress()
                        }
                    )
                }
                NavLevel.SUBJECTS -> {
                    SubjectsListScreen(
                        context = context,
                        levelCode = levelCode,
                        selectedWeek = selectedWeek ?: 1,
                        plan = plan,
                        onBack = { goBack() },
                        onSubjectSelected = { subject ->
                            selectedSubject = subject
                            navHistory.add(NavLevel.DOMAINS)
                        }
                    )
                }
                NavLevel.DOMAINS -> {
                    DomainsListScreen(
                        context = context,
                        levelCode = levelCode,
                        selectedWeek = selectedWeek ?: 1,
                        selectedSubject = selectedSubject!!,
                        plan = plan,
                        onDomainSelected = { domain ->
                            selectedDomain = domain
                            navHistory.add(NavLevel.LESSONS)
                        }
                    )
                }
                NavLevel.LESSONS -> {
                    LessonsListScreen(
                        context = context,
                        level = level,
                        levelCode = levelCode,
                        selectedWeek = selectedWeek ?: 1,
                        selectedSubject = selectedSubject!!,
                        selectedDomain = selectedDomain ?: "",
                        plan = plan,
                        progressState = progressState,
                        onLessonSelected = { lesson ->
                            selectedLesson = lesson
                            navHistory.add(NavLevel.PREPARATION)
                        },
                        onToggleLessonTaught = { lessonId ->
                            AnnualPlanningProgressManager.toggleLessonTaught(context, activeClass.id, lessonId)
                            onRefreshProgress()
                        }
                    )
                }
                else -> {}
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DRILL DOWN SCREENS & OPTIMIZED SUB-COMPOSABLES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
private fun TermsListScreen(
    context: Context,
    activeClass: ClassSection,
    levelCode: String,
    onTermSelected: (Int) -> Unit,
    onLockedTermSelected: ((Int) -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null
) {
    val term1Weeks = remember(levelCode) { OfficialAnnualPlanningRepository.getWeeksForFasl(context, levelCode, 1) }
    val term2Weeks = remember(levelCode) { OfficialAnnualPlanningRepository.getWeeksForFasl(context, levelCode, 2) }
    val term3Weeks = remember(levelCode) { OfficialAnnualPlanningRepository.getWeeksForFasl(context, levelCode, 3) }

    val progressState = remember(activeClass.id) { AnnualPlanningProgressManager.getProgress(context, activeClass.id) }
    val completedWeeksCount = progressState.completedWeeks.size
    val totalWeeksCount = 38
    val progressFraction = (completedWeeksCount.toFloat() / totalWeeksCount.toFloat()).coerceIn(0f, 1f)
    val progressPercentage = (progressFraction * 100).toInt()
    
    val calculatedCurrentWeek = remember(activeClass.id, activeClass.academicYear) {
        AnnualPlanningProgressManager.calculateCurrentWeekFromStartDate(context, activeClass.id, activeClass.academicYear)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. PROGRESS HEADER CARD
        item {
            val levelTitle = OfficialAnnualPlanningRepository.getLevelTitle(levelCode)
            CurriculumHeaderCard(
                activeClass = activeClass,
                levelTitle = levelTitle,
                totalWeeks = totalWeeksCount,
                completedWeeks = completedWeeksCount,
                progressPercentage = progressPercentage,
                progressFraction = progressFraction,
                currentWeek = calculatedCurrentWeek
            )
        }

        // 2. COMPACT COLOR KEY
        item {
            CompactColorKeyBar()
        }

        // 3. THREE TERMS
        for (termNum in 1..3) {
            val isUnlocked = activeClass.isTermUnlocked(termNum)
            val weeksForTerm = when (termNum) {
                1 -> term1Weeks
                2 -> term2Weeks
                else -> term3Weeks
            }

            item(key = "term_$termNum") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isUnlocked) {
                                onTermSelected(termNum)
                            } else {
                                onLockedTermSelected?.invoke(termNum)
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.2.dp, BentoGrayOutline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isUnlocked) BentoPrimary else Color(0xFFFFEBEE),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isUnlocked) {
                                        Text(
                                            text = "$termNum",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "مقفل",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = when (termNum) {
                                        1 -> "الفصل الأول"
                                        2 -> "الفصل الثاني"
                                        else -> "الفصل الثالث"
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = BentoPrimary
                                )
                                Text(
                                    text = "الأسابيع ${weeksForTerm.firstOrNull() ?: 1} - ${weeksForTerm.lastOrNull() ?: 13} (${weeksForTerm.size} أسبوعاً)",
                                    fontSize = 11.5.sp,
                                    color = BentoPrimaryDesc,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val termPreps = OfficialAnnualPlanningRepository.getAllLessonInstances(context, levelCode).count { it.fasl == termNum }
                            val countLabel = if (termPreps > 10) "تحضيراً" else "تحاضير"

                            Text(
                                text = "$termPreps $countLabel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimaryDesc
                            )

                            if (!isUnlocked) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFEBEE)
                                ) {
                                    Text(
                                        text = "مقفل",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "فتح الفصل",
                                tint = BentoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        if (footerContent != null) {
            item(key = "terms_footer") {
                footerContent()
            }
        }
    }
}

@Composable
private fun WeeksListScreen(
    context: Context,
    levelCode: String,
    selectedTerm: Int,
    calculatedCurrentWeek: Int,
    progressState: AnnualPlanningProgress,
    onWeekSelected: (Int) -> Unit,
    onToggleWeekCompleted: (Int) -> Unit
) {
    val weeksForTerm = remember(levelCode, selectedTerm) {
        OfficialAnnualPlanningRepository.getWeeksForFasl(context, levelCode, selectedTerm)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(weeksForTerm, key = { it }) { weekNumber ->
            val isCurrentWeek = weekNumber == calculatedCurrentWeek
            val isCompleted = progressState.completedWeeks.contains(weekNumber)

            val marker = remember(levelCode, weekNumber) {
                OfficialAnnualPlanningRepository.getCalendarMarkerForWeek(context, levelCode, weekNumber)
            }

            val rawWeekType = remember(marker, levelCode, weekNumber) {
                marker?.type ?: OfficialAnnualPlanningRepository.getWeekType(context, levelCode, weekNumber)
            }
            val weekType = cleanLabelExams(rawWeekType)
            val rawOfficialLabel = remember(marker, rawWeekType) {
                marker?.label ?: OfficialAnnualPlanningRepository.getWeekTypeLabel(rawWeekType)
            }
            val officialLabel = cleanLabelExams(rawOfficialLabel)

            val weekPreps = remember(levelCode, weekNumber) {
                OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, weekNumber).size
            }

            val (cardBg, borderColor, badgeBg, badgeColor) = when (weekType) {
                "accueil" -> QuadColor(Color(0xFFE0F2F1).copy(alpha = 0.6f), Color(0xFF4DB6AC), Color(0xFFE0F2F1), Color(0xFF00695C))
                "integration" -> QuadColor(Color(0xFFFFF3E0).copy(alpha = 0.6f), Color(0xFFFFB300), Color(0xFFFFF3E0), Color(0xFFE65100))
                "examen" -> QuadColor(Color(0xFFFFEBEE).copy(alpha = 0.6f), Color(0xFFE57373), Color(0xFFFFEBEE), Color(0xFFC62828))
                "vacances" -> QuadColor(Color(0xFFECEFF1).copy(alpha = 0.5f), Color(0xFFB0BEC5), Color(0xFFECEFF1), Color(0xFF455A64))
                "exception_calendrier" -> QuadColor(Color(0xFFF3E5F5).copy(alpha = 0.6f), Color(0xFFBA68C8), Color(0xFFF3E5F5), Color(0xFF6A1B9A))
                else -> QuadColor(
                    if (isCompleted) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface,
                    if (isCompleted) Color(0xFF81C784) else BentoGrayOutline.copy(alpha = 0.5f),
                    Color(0xFFE8F5E9),
                    Color(0xFF2E7D32)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (weekPreps > 0 || weekType != "vacances") {
                            onWeekSelected(weekNumber)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(if (isCurrentWeek) 2.dp else 1.2.dp, if (isCurrentWeek) BentoPrimary else borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "الأسبوع $weekNumber",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = BentoPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = when (weekType) {
                                    "accueil" -> "🔵 $officialLabel"
                                    "integration" -> "🟠 $officialLabel"
                                    "examen" -> "🔴 $officialLabel"
                                    "vacances" -> "⚪ $officialLabel"
                                    "exception_calendrier" -> "🟣 $officialLabel"
                                    else -> if (isCompleted) "✓ أسبوع منجز" else "🟢 دروس وتدريس"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (weekPreps > 0) {
                            val label = if (weekPreps > 10) "تحضيراً" else "تحاضير"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "$weekPreps $label",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimaryDesc,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        } else if (weekType == "vacances") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "🌴 عطلة سعيدة",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (!(weekPreps == 0 && weekType == "vacances")) {
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = { onToggleWeekCompleted(weekNumber) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2E7D32),
                                    uncheckedColor = BentoPrimaryDesc
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectsListScreen(
    context: Context,
    levelCode: String,
    selectedWeek: Int,
    plan: CanonicalAnnualPlan?,
    onBack: () -> Unit,
    onSubjectSelected: (CanonicalSubject) -> Unit
) {
    val subjectsInPlan = remember(levelCode) {
        OfficialAnnualPlanningRepository.getSubjects(context, levelCode)
    }

    val activeSubjects = remember(levelCode, selectedWeek, subjectsInPlan) {
        val weekLessons = OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, selectedWeek)
        val activeSubjectNames = weekLessons.map { it.matiere }.toSet()
        subjectsInPlan.filter { it.nom in activeSubjectNames || it.code in activeSubjectNames }
    }

    var examSubjects by remember(levelCode, selectedWeek) { mutableStateOf<List<com.example.data.exams.ExamSubject>>(emptyList()) }

    LaunchedEffect(levelCode, selectedWeek) {
        val result = withContext(Dispatchers.IO) {
            com.example.data.exams.ExamsRepository.getSubjects(context, levelCode, selectedWeek)
        }
        examSubjects = result
    }

    if (activeSubjects.isEmpty()) {
        if (examSubjects.isNotEmpty()) {
            DisposableEffect(Unit) { onDispose { try { com.example.data.exams.ExamsRepository.release() } catch (e: Exception) { e.printStackTrace() } } }
            val examPlaceKey = "exam_subject_" + levelCode + "_" + selectedWeek
            var openedExamSubject by remember {
                mutableStateOf<String?>(com.example.data.state.LastPlaceStore.getString(context, examPlaceKey))
            }
            LaunchedEffect(openedExamSubject) {
                com.example.data.state.LastPlaceStore.putString(context, examPlaceKey, openedExamSubject)
            }
            if (openedExamSubject != null) {
                ExamProposalsScreen(
                    levelCode = levelCode,
                    semaine = selectedWeek,
                    subjectCode = openedExamSubject!!,
                    onBack = { openedExamSubject = null }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BentoLightLavender, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "الرجوع",
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "مقترحات امتحان الأسبوع $selectedWeek",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimary
                                )
                                val subjectsCount = examSubjects.size
                                val subjectsLabel = if (subjectsCount > 10) "مادة" else "مواد"
                                Text(
                                    text = "$subjectsCount $subjectsLabel",
                                    fontSize = 12.sp,
                                    color = BentoPrimaryDesc
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(examSubjects, key = { it.code.ifEmpty { it.name } }) { subject ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        openedExamSubject = subject.code
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                border = BorderStroke(1.2.dp, BentoGrayOutline.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = BentoLightLavender,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.MenuBook,
                                                    contentDescription = null,
                                                    tint = BentoPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = subject.name,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.5.sp,
                                                color = BentoPrimary
                                            )
                                            val label = if (subject.proposals.size > 10) "مقترحاً" else "مقترحات"
                                            Text(
                                                text = "${subject.proposals.size} $label",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = BentoPrimaryDesc
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "لا توجد مواد أو دروس مقررة في هذا الأسبوع المخصص لـ (الأنشطة والامتحانات أو العطل).",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = BentoPrimaryDesc,
                        modifier = Modifier.padding(24.dp)
                    )
                    com.example.data.exams.ExamsRepository.lastError?.let { err ->
                        Text(
                            text = err,
                            fontSize = 10.sp,
                            color = Color(0xFFC62828),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(activeSubjects, key = { it.nom }) { subject ->
                val prepCount = remember(levelCode, selectedWeek, subject) {
                    OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, selectedWeek).count {
                        it.matiere.equals(subject.nom, ignoreCase = true) || it.matiere.equals(subject.code, ignoreCase = true)
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubjectSelected(subject) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.2.dp, BentoGrayOutline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BentoLightLavender,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = OfficialAnnualPlanningRepository.mapMatiereToArabic(subject.nom),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.5.sp,
                                color = BentoPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val countLabel = if (prepCount > 10) "تحضيراً" else "تحاضير"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "$prepCount $countLabel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimaryDesc,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "عرض المجالات",
                                tint = BentoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DomainsListScreen(
    context: Context,
    levelCode: String,
    selectedWeek: Int,
    selectedSubject: CanonicalSubject,
    plan: CanonicalAnnualPlan?,
    onDomainSelected: (String) -> Unit
) {
    val activeDomains = remember(levelCode, selectedWeek, selectedSubject) {
        val weekLessons = OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, selectedWeek).filter {
            it.matiere.equals(selectedSubject.nom, ignoreCase = true) || it.matiere.equals(selectedSubject.code, ignoreCase = true)
        }
        weekLessons.map { it.domaine }.distinct().filter { it.isNotBlank() }
    }

    if (activeDomains.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "لا توجد مجالات فرعية مقررة لهذه المادة في هذا الأسبوع.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = BentoPrimaryDesc,
                modifier = Modifier.padding(24.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(activeDomains, key = { it }) { domainCode ->
                val domainArabicName = remember(domainCode) {
                    getDomainArabicLabel(context, levelCode, domainCode)
                }

                val prepCount = remember(levelCode, selectedWeek, selectedSubject, domainCode) {
                    OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, selectedWeek).count {
                        (it.matiere.equals(selectedSubject.nom, ignoreCase = true) || it.matiere.equals(selectedSubject.code, ignoreCase = true)) &&
                        it.domaine == domainCode
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDomainSelected(domainCode) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.2.dp, BentoGrayOutline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = domainArabicName,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = BentoPrimary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val countLabel = if (prepCount > 10) "تحضيراً" else "تحاضير"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "$prepCount $countLabel",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "عرض الدروس",
                                tint = BentoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonsListScreen(
    context: Context,
    level: Int,
    levelCode: String,
    selectedWeek: Int,
    selectedSubject: CanonicalSubject,
    selectedDomain: String,
    plan: CanonicalAnnualPlan?,
    progressState: AnnualPlanningProgress,
    onLessonSelected: (CanonicalLessonInstance) -> Unit,
    onToggleLessonTaught: (String) -> Unit
) {
    val lessons = remember(levelCode, selectedWeek, selectedSubject, selectedDomain) {
        OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, selectedWeek).filter {
            (it.matiere.equals(selectedSubject.nom, ignoreCase = true) || it.matiere.equals(selectedSubject.code, ignoreCase = true)) &&
            it.domaine == selectedDomain
        }
    }

    if (lessons.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "لا توجد دروس مقررة في هذا المجال الفرعي لهذا الأسبوع.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = BentoPrimaryDesc,
                modifier = Modifier.padding(24.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lessons, key = { it.lesson_unit_id.ifBlank { "${level}_w${it.semaine}_${it.partie}" } }) { lesson ->
                val lessonId = lesson.lesson_unit_id.ifBlank { "${level}_w${lesson.semaine}_${lesson.partie}" }
                val isTaught = progressState.taughtLessons.contains(lessonId)

                val displayTitle = remember(lesson) {
                    if (lesson.nombre_parties > 1) {
                        "${lesson.titre_officiel} — الجزء ${lesson.partie} من ${lesson.nombre_parties}"
                    } else {
                        lesson.titre_officiel
                    }
                }

                val cleanComp = remember(lesson.competence) {
                    val raw = lesson.competence
                    val cleaned = raw.replace(Regex("^K\\s*\\d+\\s*:?\\s*", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("^\\s*\\d+\\s*:?\\s*"), "")
                        .trim()
                    cleaned.replace(Regex("K\\s*(\\d+)", RegexOption.IGNORE_CASE)) {
                        "الكفاية " + it.groupValues[1]
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLessonSelected(lesson) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTaught) Color(0xFFE8F5E9).copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.2.dp, if (isTaught) Color(0xFF81C784) else BentoGrayOutline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val lessonType = remember(lesson.titre_officiel) {
                                val t = lesson.titre_officiel
                                val hasInteg = t.contains("إدماج")
                                val hasAss = t.contains("تقويم")
                                val hasRem = t.contains("علاج")
                                when {
                                    (hasInteg && hasAss) || (hasInteg && hasRem) || (hasAss && hasRem) -> "وحدة مركبة"
                                    hasInteg -> "إدماج بيداغوجي"
                                    hasAss -> "تقويم بيداغوجي"
                                    hasRem -> "علاج بيداغوجي"
                                    else -> ""
                                }
                            }
                            
                            val badgeColors = remember(lessonType) {
                                when (lessonType) {
                                    "وحدة مركبة" -> Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
                                    "إدماج بيداغوجي" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
                                    "تقويم بيداغوجي" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
                                    "علاج بيداغوجي" -> Pair(Color(0xFFE0F2F1), Color(0xFF00695C))
                                    else -> Pair(Color.Transparent, Color.Transparent)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = displayTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimary,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (lessonType.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = badgeColors.first,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    ) {
                                        Text(
                                            text = lessonType,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColors.second,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (cleanComp.isNotBlank()) {
                                Text(
                                    text = "🎯 الكفاية: $cleanComp",
                                    fontSize = 11.sp,
                                    color = Color(0xFF455A64),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { onToggleLessonTaught(lessonId) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTaught) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isTaught) "تم التدريس" else "لم يدرّس",
                                    tint = if (isTaught) Color(0xFF2E7D32) else BentoPrimaryDesc.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "فتح دفتر التحضير",
                                tint = BentoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// PRIVATE HELPERS & ADAPTERS FOR BREADCRUMBS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

private fun getFaslArabicName(term: Int): String {
    return when (term) {
        1 -> "الأول"
        2 -> "الثاني"
        3 -> "الثالث"
        else -> "$term"
    }
}

private fun isMatiereFrench(matiereName: String): Boolean {
    val upper = matiereName.uppercase()
    return upper.contains("FR") || upper.contains("FRENCH") || upper.contains("FRAN")
}

private fun cleanLabelExams(text: String): String {
    return text
        .replace("اختبارات الفصل", "امتحانات الفصل")
        .replace("اختبارات", "امتحانات")
        .replace("اختبار", "امتحان")
}

private fun getDomainArabicLabel(context: Context, levelCode: String, domainNameOrCode: String): String {
    val plan = OfficialAnnualPlanningRepository.getLevelPlan(context, levelCode) ?: return OfficialAnnualPlanningRepository.mapDomainCodeToArabic(domainNameOrCode)
    for (subject in plan.matieres) {
        for (domain in subject.domaines) {
            if (domain.code.equals(domainNameOrCode, ignoreCase = true) || 
                domain.nom.equals(domainNameOrCode, ignoreCase = true)) {
                return domain.nom
            }
        }
    }
    return OfficialAnnualPlanningRepository.mapDomainCodeToArabic(domainNameOrCode)
}

// -----------------------------------------------------------------------------
// COMPONENT 1: HEADER PROGRESS CARD
// -----------------------------------------------------------------------------
@Composable
private fun CurriculumHeaderCard(
    activeClass: ClassSection,
    levelTitle: String,
    totalWeeks: Int,
    completedWeeks: Int,
    progressPercentage: Int,
    progressFraction: Float,
    currentWeek: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BentoGrayOutline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "تقدم إنجاز البرنامج الدراسي 📈",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BentoPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (progressPercentage >= 100) Color(0xFFE8F5E9) else BentoLightLavender
                ) {
                    Text(
                        text = "$progressPercentage%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (progressPercentage >= 100) Color(0xFF1B5E20) else BentoPrimary
                    )
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF2E7D32),
                    trackColor = Color(0xFFEEEEEE)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الإنجاز: $completedWeeks من $totalWeeks أسبوعاً",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                    if (currentWeek in 1..38) {
                        Text(
                            text = "الأسبوع الدراسي الحالي: $currentWeek",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDesc
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 2: COMPACT COLOR KEY BAR (مفتاح الألوان الصغير)
// -----------------------------------------------------------------------------
@Composable
private fun CompactColorKeyBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BentoGrayOutline.copy(alpha = 0.4f))
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Text(
                    text = "دليل الألوان:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoPrimary
                )
            }
            item {
                ColorKeyPill(label = "دروس", dotColor = Color(0xFF2E7D32), bgColor = Color(0xFFE8F5E9))
            }
            item {
                ColorKeyPill(label = "إدماج / تقويم / علاج", dotColor = Color(0xFFE65100), bgColor = Color(0xFFFFF3E0))
            }
            item {
                ColorKeyPill(label = "امتحانات", dotColor = Color(0xFFC62828), bgColor = Color(0xFFFFEBEE))
            }
            item {
                ColorKeyPill(label = "عطلة", dotColor = Color(0xFF455A64), bgColor = Color(0xFFECEFF1))
            }
            item {
                ColorKeyPill(label = "استقبال / تشخيص", dotColor = Color(0xFF00695C), bgColor = Color(0xFFE0F2F1))
            }
        }
    }
}

@Composable
private fun ColorKeyPill(label: String, dotColor: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = dotColor
            )
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 3: TERM ACCORDION CARD (بطاقة الفصل الدراسي)
// -----------------------------------------------------------------------------
@Composable
private fun TermAccordionCard(
    termNumber: Int,
    weeksRange: String,
    isUnlocked: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) BentoLightLavender.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 1.dp),
        border = BorderStroke(1.2.dp, if (isExpanded) BentoPrimary else BentoGrayOutline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isUnlocked) BentoPrimary else Color(0xFFFFEBEE),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUnlocked) {
                            Text(
                                text = "$termNumber",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "مقفل",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = when (termNumber) {
                            1 -> "الفصل الأول"
                            2 -> "الفصل الثاني"
                            else -> "الفصل الثالث"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = BentoPrimary
                    )
                    Text(
                        text = weeksRange,
                        fontSize = 11.5.sp,
                        color = BentoPrimaryDesc,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = "مقفل",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "طي" else "توسيع",
                    tint = BentoPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 4: WEEKLY PLAN CARD (بطاقة الأسبوع المستقلة)
// -----------------------------------------------------------------------------
@Composable
private fun WeeklyPlanCard(
    week: Int,
    level: Int,
    levelCode: String,
    isCurrentWeek: Boolean,
    isCompleted: Boolean,
    progressState: AnnualPlanningProgress,
    onToggleWeekCompleted: () -> Unit,
    onToggleLessonTaught: (String) -> Unit
) {
    val context = LocalContext.current

    // Calendar Marker from reperes_calendrier
    val marker = remember(levelCode, week) {
        OfficialAnnualPlanningRepository.getCalendarMarkerForWeek(context, levelCode, week)
    }

    // Determine week type: lecon, integration, examen, vacances, accueil, exception_calendrier
    val weekType = remember(marker, levelCode, week) {
        marker?.type ?: OfficialAnnualPlanningRepository.getWeekType(context, levelCode, week)
    }
    val officialLabel = remember(marker, weekType) {
        marker?.label ?: OfficialAnnualPlanningRepository.getWeekTypeLabel(weekType)
    }

    // Lesson instances for this week
    val lessonInstances = remember(levelCode, week) {
        OfficialAnnualPlanningRepository.getLessonInstancesForWeek(context, levelCode, week)
    }

    // Units for this week (including integration stations)
    val unitsForWeek = remember(levelCode, week) {
        OfficialAnnualPlanningRepository.getUnitsForWeek(context, levelCode, week)
    }
    val integrationUnits = remember(unitsForWeek) {
        unitsForWeek.filter { it.third.type == "integration" }
    }

    // Subjects in plan
    val subjectsInPlan = remember(levelCode) {
        OfficialAnnualPlanningRepository.getSubjects(context, levelCode)
    }

    // Color definitions based on week type
    val (cardBg, borderColor, badgeBg, badgeColor) = when (weekType) {
        "accueil" -> QuadColor(Color(0xFFE0F2F1).copy(alpha = 0.6f), Color(0xFF4DB6AC), Color(0xFFE0F2F1), Color(0xFF00695C))
        "integration" -> QuadColor(Color(0xFFFFF3E0).copy(alpha = 0.6f), Color(0xFFFFB300), Color(0xFFFFF3E0), Color(0xFFE65100))
        "examen" -> QuadColor(Color(0xFFFFEBEE).copy(alpha = 0.6f), Color(0xFFE57373), Color(0xFFFFEBEE), Color(0xFFC62828))
        "vacances" -> QuadColor(Color(0xFFECEFF1).copy(alpha = 0.5f), Color(0xFFB0BEC5), Color(0xFFECEFF1), Color(0xFF455A64))
        "exception_calendrier" -> QuadColor(Color(0xFFF3E5F5).copy(alpha = 0.6f), Color(0xFFBA68C8), Color(0xFFF3E5F5), Color(0xFF6A1B9A))
        else -> QuadColor(
            if (isCompleted) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface,
            if (isCompleted) Color(0xFF81C784) else BentoGrayOutline.copy(alpha = 0.5f),
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(if (isCurrentWeek) 2.dp else 1.2.dp, if (isCurrentWeek) BentoPrimary else borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CARD HEADER: Week Number + Official Label + Current Week Badge + Complete Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "الأسبوع $week",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.5.sp,
                            color = BentoPrimary
                        )
                    }

                    // Official Label / Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = when (weekType) {
                                "accueil" -> "🔵 $officialLabel"
                                "integration" -> "🟠 $officialLabel"
                                "examen" -> "🔴 $officialLabel"
                                "vacances" -> "⚪ $officialLabel"
                                "exception_calendrier" -> "🟣 $officialLabel"
                                else -> if (isCompleted) "✓ أسبوع منجز" else "🟢 دروس وتدريس"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Week Completion Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleWeekCompleted() }
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { onToggleWeekCompleted() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2E7D32),
                            uncheckedColor = BentoPrimaryDesc
                        )
                    )
                    Text(
                        text = if (isCompleted) "منجز ✓" else "إنجاز",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFF2E7D32) else BentoPrimaryDesc
                    )
                }
            }

            // CARD BODY:
            // 1. If special milestone without regular lessons (Accueil, Examen, Vacances, or milestone without lesson instances):
            if ((weekType == "examen" || weekType == "vacances" || weekType == "accueil") && lessonInstances.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = officialLabel,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 2. Regular Teaching / Integration Week:
                // Group content: Subject -> Domain -> Lessons / Integration Stations
                val activeSubjectsInWeek = remember(lessonInstances, integrationUnits, subjectsInPlan, week, levelCode) {
                    val lessonSubjects = lessonInstances.map { it.matiere }.toSet()
                    val integrationSubjects = integrationUnits.map { it.first.nom }.toSet()
                    // Check for subjects with local exceptions in 5AP
                    val localSubjects = subjectsInPlan.filter {
                        OfficialAnnualPlanningRepository.getLocalMarkerForSubject(context, levelCode, it.nom, week) != null
                    }.map { it.nom }.toSet()

                    val allSubjectNames = (lessonSubjects + integrationSubjects + localSubjects)
                    subjectsInPlan.filter { it.nom in allSubjectNames || it.code in allSubjectNames }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeSubjectsInWeek.forEach { subject ->
                        SubjectSectionInWeek(
                            subject = subject,
                            week = week,
                            level = level,
                            levelCode = levelCode,
                            lessonInstances = lessonInstances.filter { it.matiere == subject.nom || it.matiere == subject.code },
                            integrationUnits = integrationUnits.filter { it.first.nom == subject.nom || it.first.code == subject.code },
                            progressState = progressState,
                            onToggleLessonTaught = onToggleLessonTaught
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 5: SUBJECT SECTION IN WEEK
// Hierarchy: المادة -> المجال -> عنوان الدرس / محطة الإدماج
// -----------------------------------------------------------------------------
@Composable
private fun SubjectSectionInWeek(
    subject: CanonicalSubject,
    week: Int,
    level: Int,
    levelCode: String,
    lessonInstances: List<CanonicalLessonInstance>,
    integrationUnits: List<Triple<CanonicalSubject, com.example.data.planning.canonical.CanonicalDomain, CanonicalUnit>>,
    progressState: AnnualPlanningProgress,
    onToggleLessonTaught: (String) -> Unit
) {
    val context = LocalContext.current

    // Check for Local Exception (reperes_locaux in 5AP)
    val localMarker: CanonicalLocalMarker? = remember(subject.nom, week, levelCode) {
        OfficialAnnualPlanningRepository.getLocalMarkerForSubject(context, levelCode, subject.nom, week)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BentoGrayOutline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Subject Title Header (Displayed ONCE per subject in the week)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = BentoLightLavender,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(
                    text = OfficialAnnualPlanningRepository.mapMatiereToArabic(subject.nom),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = BentoPrimary
                )
            }

            // If Subject has a Local Exception (e.g. 5AP Arabic W24/W25)
            if (localMarker != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (localMarker.type == "examen") Color(0xFFFFEBEE) else Color(0xFFECEFF1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📌 ${localMarker.label ?: "استثناء محلي رسمي"}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (localMarker.type == "examen") Color(0xFFC62828) else Color(0xFF455A64)
                        )
                    }
                }
            }

            // Domains under this subject
            val domainsInWeek = remember(lessonInstances, integrationUnits, subject) {
                val lessonDomains = lessonInstances.map { it.domaine }.toSet()
                val integrationDomains = integrationUnits.map { it.second.nom }.toSet()
                (lessonDomains + integrationDomains).filter { it.isNotBlank() }
            }

            if (domainsInWeek.isNotEmpty()) {
                domainsInWeek.forEach { domainName ->
                    val lessonsInDomain = lessonInstances.filter { it.domaine == domainName }
                    val integrationsInDomain = integrationUnits.filter { it.second.nom == domainName }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Domain Sub-Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "↳ المجال: $domainName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimaryDesc
                            )
                        }

                        // 1. Regular Lesson Instances (type = lecon)
                        lessonsInDomain.forEach { lesson ->
                            val lessonId = lesson.lesson_unit_id.ifBlank { "${level}_w${lesson.semaine}_${lesson.partie}" }
                            val isTaught = progressState.taughtLessons.contains(lessonId)

                            // Display Title:
                            // If nombre_parties > 1: "العنوان — الجزء X من Y"
                            // If nombre_parties = 1: "العنوان"
                            val displayTitle = remember(lesson) {
                                if (lesson.nombre_parties > 1) {
                                    "${lesson.titre_officiel} — الجزء ${lesson.partie} من ${lesson.nombre_parties}"
                                } else {
                                    lesson.titre_officiel
                                }
                            }

                            LessonRowInsideDomain(
                                displayTitle = displayTitle,
                                competence = lesson.competence,
                                lessonUnitId = lesson.lesson_unit_id,
                                isTaught = isTaught,
                                onToggleTaught = { onToggleLessonTaught(lessonId) }
                            )
                        }

                        // 2. Integration Stations (type = integration)
                        integrationsInDomain.forEach { (_, _, unit) ->
                            IntegrationStationBox(
                                title = unit.titre,
                                habiletes = unit.habiletes
                            )
                        }
                    }
                }
            } else if (localMarker == null) {
                // If there are direct lessons with blank domain
                lessonInstances.forEach { lesson ->
                    val lessonId = lesson.lesson_unit_id.ifBlank { "${level}_w${lesson.semaine}_${lesson.partie}" }
                    val isTaught = progressState.taughtLessons.contains(lessonId)
                    val displayTitle = if (lesson.nombre_parties > 1) {
                        "${lesson.titre_officiel} — الجزء ${lesson.partie} من ${lesson.nombre_parties}"
                    } else {
                        lesson.titre_officiel
                    }

                    LessonRowInsideDomain(
                        displayTitle = displayTitle,
                        competence = lesson.competence,
                        lessonUnitId = lesson.lesson_unit_id,
                        isTaught = isTaught,
                        onToggleTaught = { onToggleLessonTaught(lessonId) }
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 6: LESSON ROW INSIDE DOMAIN
// -----------------------------------------------------------------------------
@Composable
private fun LessonRowInsideDomain(
    displayTitle: String,
    competence: String,
    lessonUnitId: String,
    isTaught: Boolean,
    onToggleTaught: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isTaught) Color(0xFFE8F5E9).copy(alpha = 0.7f) else MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, if (isTaught) Color(0xFF81C784) else BentoGrayOutline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "• $displayTitle",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoPrimary
                )

                if (competence.isNotBlank()) {
                    Text(
                        text = "🎯 الكفاية: $competence",
                        fontSize = 10.5.sp,
                        color = Color(0xFF455A64),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Taught Checkbox
            IconButton(
                onClick = onToggleTaught,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isTaught) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isTaught) "تم التدريس" else "لم يدرّس",
                    tint = if (isTaught) Color(0xFF2E7D32) else BentoPrimaryDesc.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 7: INTEGRATION STATION BOX (إدماج / تقويم / علاج)
// -----------------------------------------------------------------------------
@Composable
private fun IntegrationStationBox(
    title: String,
    habiletes: List<String>
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFF3E0),
        border = BorderStroke(1.dp, Color(0xFFFFB300)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE65100)
                ) {
                    Text(
                        text = "إدماج / تقويم / علاج",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFBF360C)
                )
            }

            if (habiletes.isNotEmpty()) {
                Text(
                    text = "المهارات المستهدفة: ${habiletes.joinToString("، ")}",
                    fontSize = 10.sp,
                    color = Color(0xFF5D4037),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 9: UNASSIGNED LEVEL CARD
// -----------------------------------------------------------------------------
@Composable
private fun UnassignedLevelCard(
    activeClass: ClassSection,
    onSelectLevel: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = "المستوى الدراسي",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "تحديد مستوى القسم",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "لم يتم تحديد مستوى هذا القسم (${activeClass.getFormattedName()}) في السجل المحلي.\nيرجى اختيار المستوى التعليمي لعرض التخطيط السنوي:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (lvl in 1..6) {
                        Button(
                            onClick = { onSelectLevel(lvl) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lvl == 5) BentoPrimary else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (lvl == 5) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(
                                text = "السنة ${if (lvl == 6) "السادسة (6AP)" else "$lvl"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER DATA CLASS FOR QUAD COLORS
// -----------------------------------------------------------------------------
private data class QuadColor(
    val bg: Color,
    val border: Color,
    val badgeBg: Color,
    val badgeColor: Color
)
