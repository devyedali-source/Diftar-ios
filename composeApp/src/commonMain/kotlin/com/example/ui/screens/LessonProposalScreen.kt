package com.example.ui.screens

import org.jetbrains.compose.resources.decodeToImageBitmap
import com.example.compat.*
import kotlinx.coroutines.IO
import com.example.ui.utils.uiT
import com.example.ui.utils.uiTf

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TeacherDatabase
import com.example.ui.components.OfficialStampVisual
import com.example.data.planning.AnnualLessonItem
import com.example.data.planning.AnnualPlanningProgressManager
import com.example.data.planning.AssessmentProposal
import com.example.data.planning.IntegrationProposal
import com.example.data.planning.LessonItemType
import com.example.data.planning.LessonProcedureStep
import com.example.data.planning.LessonProposalData
import com.example.data.planning.LessonPreparationRepository
import com.example.data.planning.LessonProposalRepository
import com.example.data.planning.OfficialPlanningHierarchyAdapter
import com.example.data.planning.PedagogicalStationData
import com.example.data.planning.RemediationProposal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Classic Handwritten Blue Ink Palette (دفتر التحضير الورقي التقليدي — مذكرة المعلم)
private val InkDarkBlue = Color(0xFF1A3A8F)       // حبر أزرق داكن تقليدي للكتابة الأساسية
private val InkMediumBlue = Color(0xFF1A3A8F)     // حبر أزرق متوسط للنصوص والشروحات
private val InkSoftBlue = Color(0xFF1A3A8F)       // حبر أزرق هادئ للعناوين الفرعية
private val PaperBorderBlue = Color(0xFF2B4C7E)   // خطوط الجداول والإطارات الرفيعة
private val PaperPureWhite = Color(0xFFFFFFFF)    // خلفية ورقة التحضير البيضاء
private val PaperCream = Color(0xFFFAFAFA)        // ترويسة الجدول الهادئة

@Composable
fun LessonProposalScreen(
    lesson: AnnualLessonItem,
    currentWeek: Int = lesson.week,
    classId: Long = 0L,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Screen Security Enforcer (FLAG_SECURE) to prevent screenshotting, recording, and recent app previews (Bypassed temporarily until Sep 30, 2026)
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = com.example.data.security.TrustedClock.now() }
        val isBeforeOct2026 = calendar.get(java.util.Calendar.YEAR) < 2026 || 
            (calendar.get(java.util.Calendar.YEAR) == 2026 && calendar.get(java.util.Calendar.MONTH) < java.util.Calendar.OCTOBER)
        
        // مدير الأقسام وحده مسموح له بلقطات الشاشة وتسجيلها؛ باقي المستخدمين يبقون محميين
        val secureEnabled = !isBeforeOct2026 && !com.example.data.security.ScreenSecurity.isCaptureAllowedForCurrentUser()
        if (secureEnabled) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (secureEnabled) {
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    val itemType = remember(lesson.id, currentWeek) {
        LessonProposalRepository.classifyLessonItem(lesson, currentWeek)
    }

    if (itemType == LessonItemType.PEDAGOGICAL_STATION) {
        PedagogicalStationScreenContent(
            lesson = lesson,
            currentWeek = currentWeek,
            classId = classId,
            onBack = onBack
        )
        return
    }

    val proposalDataState = produceState<LessonProposalData?>(initialValue = null, lesson.id, currentWeek) {
        value = withContext(Dispatchers.IO) {
            LessonPreparationRepository.getPreparationForLesson(context, lesson, currentWeek)
        }
    }

    var isTaught by remember(lesson.id, classId) {
        val initialTaught = if (classId != 0L) {
            AnnualPlanningProgressManager.getProgress(context, classId).taughtLessons.contains(lesson.id)
        } else false
        mutableStateOf(initialTaught)
    }

    var showBoard by remember(lesson.id) { mutableStateOf(false) }
    var showBook by remember(lesson.id) { mutableStateOf(false) }

    val domainText = remember(lesson, context) {
        val lvlCode = com.example.data.planning.canonical.OfficialAnnualPlanningRepository.getLevelCodeFromInt(lesson.level)
        val rawDomain = lesson.domain.ifBlank { "المجال العام" }
        getDomainArabicLabel(context, lvlCode, rawDomain)
    }

    val fr = lesson.isFrench

    val unitNum = remember(lesson.id) {
        getUnitInfo(lesson)
    }

    val compNum = remember(lesson.id, lesson.competency) {
        val parts = lesson.id.split("|")
        val fromId = if (parts.size >= 3) {
            Regex("^K(\\d+)$", RegexOption.IGNORE_CASE).find(parts[2].trim())?.groupValues?.get(1)?.toIntOrNull()
        } else null
        
        fromId ?: Regex("K\\s*(\\d+)", RegexOption.IGNORE_CASE).find(lesson.competency)?.groupValues?.get(1)?.toIntOrNull()
    }

    val compText = remember(lesson) {
        val raw = lesson.competency.ifBlank {
            "اكتساب وتطبيق المعارف والمهارات الأساسية المقررة في المنهاج الوطني الرسمي."
        }
        val cleaned = raw.replace(Regex("^K\\s*\\d+\\s*:?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^\\s*\\d+\\s*:?\\s*"), "")
            .trim()
        cleaned.replace(Regex("K\\s*(\\d+)", RegexOption.IGNORE_CASE)) {
            if (lesson.isFrench) "Compétence ${it.groupValues[1]}" else "الكفاية ${it.groupValues[1]}"
        }
    }

    val stampTeacherSettings = remember {
        val prefs = context.getSharedPreferences("teacher_settings_prefs", android.content.Context.MODE_PRIVATE)
        val showInReports = prefs.getBoolean("stamp_teacher_show_in_reports", false)
        val name = prefs.getString("stamp_teacher_name", "") ?: ""
        val shape = prefs.getString("stamp_teacher_shape", "rectangle") ?: "rectangle"
        val role = prefs.getString("stamp_teacher_role", "المعلم") ?: "المعلم"
        val financialId = prefs.getString("stamp_teacher_financial_id", "") ?: ""
        object {
            val show = showInReports && name.isNotBlank()
            val name = name
            val shape = shape
            val role = role
            val financialId = financialId
        }
    }

    val stampPrincipalSettings = remember {
        val prefs = context.getSharedPreferences("teacher_settings_prefs", android.content.Context.MODE_PRIVATE)
        val showInReports = prefs.getBoolean("stamp_principal_show_in_reports", false)
        val name = prefs.getString("stamp_principal_name", "") ?: ""
        val shape = prefs.getString("stamp_principal_shape", "circle") ?: "circle"
        val role = prefs.getString("stamp_principal_role", "المدير") ?: "المدير"
        val financialId = prefs.getString("stamp_principal_financial_id", "") ?: ""
        object {
            val show = showInReports && name.isNotBlank()
            val name = name
            val shape = shape
            val role = role
            val financialId = financialId
        }
    }

    val classSchoolNameState = produceState(initialValue = "", classId) {
        if (classId != 0L) {
            value = withContext(Dispatchers.IO) {
                try {
                    TeacherDatabase.getDatabase(context).teacherDao().getClassSectionById(classId)?.schoolName ?: ""
                } catch (e: Exception) {
                    ""
                }
            }
        }
    }

    DisableSelection {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
        ) {
            // Slim Top Bar (رأس علوي مقتضب وواضح)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PaperPureWhite,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF8FAFC), CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "العودة",
                                tint = InkDarkBlue
                            )
                        }

                        Column {
                            Text(
                                text = uiT("مذكرة تحضير الدرس"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = InkDarkBlue
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                    if (proposalDataState.value?.board != null) {
                        Button(
                            onClick = { showBoard = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111), contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (fr) "Tableau" else "السبورة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    if (proposalDataState.value?.bookPages != null) {
                        Button(
                            onClick = { showBook = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = InkDarkBlue, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (fr) "Manuel" else "صفحات الكتاب",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    if (classId == 0L) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = InkDarkBlue)
                        }
                    }
                    }
                }
            }

            val proposalData = proposalDataState.value

            if (proposalData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = InkDarkBlue,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = uiT("جارٍ تحضير الورقة..."),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDarkBlue
                        )
                    }
                }
            } else {
                CompositionLocalProvider(LocalLayoutDirection provides (if (fr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
                    // Scrollable White Preparation Sheet (قابلة للتقريب بإصبعين)
                    ZoomableSheet {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(4.dp)),
                                color = PaperPureWhite,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 0. تاريخا اليوم المسجّلان محلياً أعلى الورقة (خارج الجدول)
                                    val prepCtx = androidx.compose.ui.platform.LocalContext.current
                                    val (prepHijri, prepGreg) = remember(fr) { com.example.ui.utils.LocalDates.today(prepCtx, fr) }
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(text = prepHijri, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkDarkBlue, modifier = Modifier.fillMaxWidth())
                                        Text(text = prepGreg, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkDarkBlue, modifier = Modifier.fillMaxWidth())
                                    }

                                    // 1. ترويسة من صفّين، كل صف أربع خانات
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, PaperBorderBlue.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                                            .background(PaperPureWhite)
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // الصف 1
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(IntrinsicSize.Min),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                HeaderCell(
                                                    title = (if (fr) "Date (hégire) :" else "التاريخ الهجري:"),
                                                    value = "................",
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = (if (fr) "Date :" else "الموافق الميلادي:"),
                                                    value = "................",
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = (if (fr) "Niveau :" else "المستوى:"),
                                                    value = if (fr) "${getLevelFrenchName(lesson.level)} année" else proposalData.levelTitle.ifBlank { "${getLevelArabicName(lesson.level)} ابتدائي" },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = if (proposalData.sessionInfo.isNotBlank()) (if (fr) "Séance :" else "رمز الحصة:") else "",
                                                    value = proposalData.sessionInfo,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            HorizontalDivider(color = PaperBorderBlue.copy(alpha = 0.5f), thickness = 0.8.dp)

                                            // الصف 2
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(IntrinsicSize.Min),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                HeaderCell(
                                                    title = (if (fr) "Matière :" else "المادة:"),
                                                    value = proposalData.subject.ifBlank { lesson.subject },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = (if (fr) "Domaine :" else "المجال:"),
                                                    value = proposalData.domain.ifBlank { domainText },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = (if (fr) "Thème :" else "الموضوع:"),
                                                    value = proposalData.topic.ifBlank { lesson.title },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = if (proposalData.durationText.isNotBlank()) (if (fr) "Durée :" else "المدة:") else "",
                                                    value = proposalData.durationText,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }

                                    // 2. سطر: الوسائل
                                    if (proposalData.teachingAids.isNotBlank()) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = (if (fr) "Moyens :" else "الوسائل:"),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = InkDarkBlue
                                            )
                                            Text(
                                                text = if (fr) proposalData.teachingAids.replace("المفردات الأساسية: ", "Vocabulaire : ").replace("،", ",") else proposalData.teachingAids,
                                                fontSize = 12.sp,
                                                color = InkMediumBlue,
                                                lineHeight = 18.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // 3. سطر: المرجع (أخفِ السطر كاملًا إذا كان فارغًا)
                                    if (proposalData.referenceText.isNotBlank()) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = (if (fr) "Référence :" else "المرجع:"),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = InkDarkBlue
                                            )
                                            Text(
                                                text = proposalData.referenceText,
                                                fontSize = 12.sp,
                                                color = InkMediumBlue,
                                                lineHeight = 18.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // سطر: الكفاية والوحدة (يمينًا الكفاية ويسارًا الوحدة)
                                    if (compNum != null || unitNum != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (compNum != null) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = (if (fr) "Compétence :" else "الكفاية:"),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = InkDarkBlue
                                                    )
                                                    Text(
                                                        text = "$compNum",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = InkDarkBlue
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(1.dp))
                                            }

                                            if (unitNum != null) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = (if (fr) "Unité :" else "الوحدة:"),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = InkDarkBlue
                                                    )
                                                    Text(
                                                        text = "$unitNum",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = InkDarkBlue
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(1.dp))
                                            }
                                        }
                                    }

                                    // 4. إطار الهدف المميز
                                    if (proposalData.specificObjective.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, PaperBorderBlue.copy(alpha = 0.6f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = (if (fr) "Objectif spécifique :" else "الهدف المميز:"),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = InkDarkBlue
                                                )
                                                Text(
                                                    text = proposalData.specificObjective,
                                                    fontSize = 12.sp,
                                                    color = InkDarkBlue,
                                                    lineHeight = 18.sp,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // 5. عنوان في وسط الشاشة: "سير الدرس"
                                    Text(
                                        text = if (fr) "Déroulement de la leçon" else "سير الدرس",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = InkDarkBlue,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )

                                    // 6. جدول بأربعة أعمدة
                                    LessonProcedureTable(
                                        steps = proposalData.procedureSteps,
                                        isFrench = lesson.isFrench
                                    )

                                    // 7. صف من ثلاث خانات متساوية العرض للتوقيع اليدوي
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // توقيع المعلم
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            SignatureBox(title = if (fr) "Signature de l'enseignant" else "توقيع المعلم", modifier = Modifier.fillMaxWidth())
                                            if (stampTeacherSettings.show) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    OfficialStampVisual(
                                                        roleTitle = if (fr) "Enseignant" else "المعلم",
                                                        personName = stampTeacherSettings.name,
                                                        schoolName = classSchoolNameState.value,
                                                        shape = stampTeacherSettings.shape,
                                                        scaleSize = 105.dp,
                                                        financialId = stampTeacherSettings.financialId
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        // توقيع المدير
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            SignatureBox(title = if (fr) "Signature du directeur" else "توقيع المدير", modifier = Modifier.fillMaxWidth())
                                            if (stampPrincipalSettings.show) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    OfficialStampVisual(
                                                        roleTitle = if (fr) "Directeur" else "المدير",
                                                        personName = stampPrincipalSettings.name,
                                                        schoolName = classSchoolNameState.value,
                                                        shape = "circle",
                                                        scaleSize = 105.dp,
                                                        financialId = stampPrincipalSettings.financialId
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        // توقيع المفتش
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            SignatureBox(title = if (fr) "Signature de l'inspecteur" else "توقيع المفتش", modifier = Modifier.fillMaxWidth())
                                        }
                                    }

                                    // تنبيه تربوي أسفل خانات التوقيعات الثلاث
                                    Text(
                                        text = if (fr) "Cette fiche ne remplace pas la préparation personnelle de l'enseignant. C'est un modèle d'aide qui relie la préparation à la planification annuelle et aux pages du manuel." else "هذا التحضير لا يغني عن كتابة المعلم لتحضيره الخاص! هذا مثال تحضير مساعد يربط بين التحضير والتخطيط السنوي وأرقام صفحات الدرس من الكتاب",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD32F2F),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                    }
                }
            }
        }

        if (showBook) {
            proposalDataState.value?.bookPages?.let { bp ->
                BookPagesViewer(bp = bp, isFrench = fr, onClose = { showBook = false })
            }
        }
        if (showBoard) {
            proposalDataState.value?.let { pd ->
                LessonBlackboardScreen(
                    data = pd,
                    isFrench = fr,
                    subjectFallback = lesson.subject,
                    domainFallback = domainText,
                    topicFallback = lesson.title,
                    onClose = { showBoard = false }
                )
            }
        }
        }
    }
}

/**
 * دالة مساعدة لتسمية الفصول بالعربية
 */
private fun getFaslArabicName(term: Int): String {
    return when (term) {
        1 -> "الأول"
        2 -> "الثاني"
        3 -> "الثالث"
        else -> "$term"
    }
}

/**
 * دالة مساعدة للحصول على الاسم العربي الرسمي للمجال من ملفات الـ JSON مع ترجمة احتياطية للرموز التقنية
 */
private fun getDomainArabicLabel(context: android.content.Context, levelCode: String, domainNameOrCode: String): String {
    val plan = com.example.data.planning.canonical.OfficialAnnualPlanningRepository.getLevelPlan(context, levelCode) ?: return com.example.data.planning.canonical.OfficialAnnualPlanningRepository.mapDomainCodeToArabic(domainNameOrCode)
    for (subject in plan.matieres) {
        for (domain in subject.domaines) {
            if (domain.code.equals(domainNameOrCode, ignoreCase = true) || 
                domain.nom.equals(domainNameOrCode, ignoreCase = true)) {
                return domain.nom
            }
        }
    }
    return com.example.data.planning.canonical.OfficialAnnualPlanningRepository.mapDomainCodeToArabic(domainNameOrCode)
}

/**
 * دالة مساعدة لتسمية المستوى بالعربية دون تكرار
 */
private fun getLevelArabicName(level: Int): String {
    return when (level) {
        1 -> "الأول"
        2 -> "الثاني"
        3 -> "الثالث"
        4 -> "الرابع"
        5 -> "الخامس"
        6 -> "السادس"
        else -> "$level"
    }
}

/**
 * دالة مساعدة لتسمية المستوى بالفرنسية
 */
private fun getLevelFrenchName(level: Int): String {
    return when (level) {
        1 -> "1ère"
        2 -> "2ème"
        3 -> "3ème"
        4 -> "4ème"
        5 -> "5ème"
        6 -> "6ème"
        else -> "${level}ème"
    }
}

/**
 * دالة استخراج رقم الوحدة من lesson.id إن كانت المقاطع 4 والمقطع الرابع يطابق U متبوعًا بأرقام
 */
private fun getUnitInfo(lesson: AnnualLessonItem): Int? {
    val parts = lesson.id.split("|")
    if (parts.size == 4) {
        val uMatch = Regex("^U(\\d+)$", RegexOption.IGNORE_CASE).find(parts[3].trim())
        if (uMatch != null) {
            return uMatch.groupValues[1].toIntOrNull()
        }
    }
    return null
}

/**
 * جدول سير الدرس: مراحل الدرس | نشاط المعلم | نشاط التلميذ | المدة
 */
@Composable
private fun LessonProcedureTable(
    steps: List<LessonProcedureStep>,
    isFrench: Boolean
) {
    if (steps.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaperBorderBlue.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                .background(PaperPureWhite)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFrench) "Aucune étape détaillée pour cette leçon." else "لا توجد خطوات سير مفصلة لهذا الدرس حاليًا.",
                fontSize = 12.sp,
                color = InkSoftBlue
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PaperBorderBlue, RoundedCornerShape(2.dp))
            .background(PaperPureWhite)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ترويسة أعمدة الجدول: مراحل الدرس | نشاط المعلم | نشاط التلميذ | المدة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperCream)
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeadText(
                    text = if (isFrench) "Étapes" else "مراحل الدرس",
                    modifier = Modifier.weight(0.22f)
                )
                PaperVerticalLine()
                TableHeadText(
                    text = if (isFrench) "Activité du maître" else "نشاط المعلم",
                    modifier = Modifier.weight(0.35f)
                )
                PaperVerticalLine()
                TableHeadText(
                    text = if (isFrench) "Activité de l'élève" else "نشاط التلميذ",
                    modifier = Modifier.weight(0.33f)
                )
                PaperVerticalLine()
                TableHeadText(
                    text = if (isFrench) "Durée" else "المدة",
                    modifier = Modifier.weight(0.10f)
                )
            }

            HorizontalDivider(color = PaperBorderBlue, thickness = 1.dp)

            // صفوف المراحل
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Top
                ) {
                    // العمود 1: مرحلة الدرس
                    Box(
                        modifier = Modifier
                            .weight(0.22f)
                            .fillMaxHeight()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.phaseName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDarkBlue,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }

                    PaperVerticalLine()

                    // العمود 2: نشاط المعلم
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight()
                            .padding(4.dp)
                    ) {
                        Text(
                            text = step.teacherActivity,
                            fontSize = 11.sp,
                            color = InkMediumBlue,
                            lineHeight = 15.sp,
                            textAlign = if (isFrench) TextAlign.Left else TextAlign.Right
                        )
                    }

                    PaperVerticalLine()

                    // العمود 3: نشاط التلميذ
                    Box(
                        modifier = Modifier
                            .weight(0.33f)
                            .fillMaxHeight()
                            .padding(4.dp)
                    ) {
                        Text(
                            text = step.studentActivity,
                            fontSize = 11.sp,
                            color = InkMediumBlue,
                            lineHeight = 15.sp,
                            textAlign = if (isFrench) TextAlign.Left else TextAlign.Right
                        )
                    }

                    PaperVerticalLine()

                    // العمود 4: المدة
                    Box(
                        modifier = Modifier
                            .weight(0.10f)
                            .fillMaxHeight()
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.durationText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDarkBlue,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (index < steps.size - 1) {
                    HorizontalDivider(color = PaperBorderBlue.copy(alpha = 0.4f), thickness = 0.8.dp)
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.TopStart
    ) {
        if (title.isNotBlank() || value.isNotBlank()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDarkBlue
                    )
                }
                if (value.isNotBlank()) {
                    Text(
                        text = value,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = InkMediumBlue,
                        softWrap = true,
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

@Composable
private fun SignatureBox(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = InkDarkBlue,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(26.dp))
        HorizontalDivider(
            color = PaperBorderBlue.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

@Composable
private fun TableHeadText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = InkDarkBlue,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 2.dp)
    )
}

@Composable
private fun PaperVerticalLine() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(PaperBorderBlue.copy(alpha = 0.5f))
    )
}

// -----------------------------------------------------------------------------
// PEDAGOGICAL STATION SCREEN (الإدماج والتقويم والعلاج)
// -----------------------------------------------------------------------------
@Composable
private fun PedagogicalStationScreenContent(
    lesson: AnnualLessonItem,
    currentWeek: Int,
    classId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val stationData = remember(lesson.id, currentWeek) {
        LessonProposalRepository.getPedagogicalStationData(lesson, currentWeek)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val isFrench = stationData.isFrench

    var isTaught by remember(lesson.id, classId) {
        val initialTaught = if (classId != 0L) {
            AnnualPlanningProgressManager.getProgress(context, classId).taughtLessons.contains(lesson.id)
        } else false
        mutableStateOf(initialTaught)
    }

    DisableSelection {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // Slim Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "العودة",
                                tint = Color(0xFF0F172A)
                            )
                        }

                        Column {
                            Text(
                                text = if (isFrench) "Station Pédagogique (Intégration & Évaluation)" else "محطة الإدماج والتقويم والعلاج",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${com.example.data.planning.canonical.OfficialAnnualPlanningRepository.mapMatiereToArabic(stationData.subject)} • ${if (isFrench) "Semaine $currentWeek" else "الأسبوع $currentWeek"}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (classId != 0L) {
                        OutlinedButton(
                            onClick = {
                                val newState = AnnualPlanningProgressManager.toggleLessonTaught(context, classId, lesson.id)
                                isTaught = newState
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isTaught) Color(0xFF10B981) else Color(0xFFCBD5E1)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isTaught) Color(0xFFECFDF5) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isTaught) "تم إنجاز المحطة ✓" else "تحديد كمنجزة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTaught) Color(0xFF059669) else Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            // Header Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0D233A)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1A3B5C)
                        ) {
                            Text(
                                text = stationData.levelTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE2E8F0),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B)
                        ) {
                            Text(
                                text = if (isFrench) "Semaine $currentWeek • Trimestre ${stationData.term}" else "الأسبوع $currentWeek • الفصل ${stationData.term}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = stationData.stationTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Text(
                        text = "${com.example.data.planning.canonical.OfficialAnnualPlanningRepository.mapMatiereToArabic(stationData.subject)} — ${stationData.domain}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // 3 Clean Tabs (الإدماج • التقويم • العلاج)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0F172A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF0D233A),
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = if (isFrench) "1. Intégration" else "1. مقترحات الإدماج",
                            fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = if (isFrench) "2. Évaluation" else "2. مقترحات التقويم",
                            fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = if (isFrench) "3. Remédiation" else "3. مقترحات العلاج",
                            fontWeight = if (selectedTab == 2) FontWeight.Black else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Contents
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // 1. INTEGRATION PROPOSALS
                        items(stationData.integrationProposals.size) { index ->
                            val proposal = stationData.integrationProposals[index]
                            IntegrationProposalCard(proposal = proposal, isFrench = isFrench)
                        }
                    }
                    1 -> {
                        // 2. ASSESSMENT PROPOSALS
                        items(stationData.assessmentProposals.size) { index ->
                            val proposal = stationData.assessmentProposals[index]
                            AssessmentProposalCard(proposal = proposal, isFrench = isFrench)
                        }
                    }
                    2 -> {
                        // 3. REMEDIATION PROPOSALS
                        items(stationData.remediationProposals.size) { index ->
                            val proposal = stationData.remediationProposals[index]
                            RemediationProposalCard(proposal = proposal, isFrench = isFrench)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegrationProposalCard(
    proposal: IntegrationProposal,
    isFrench: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = "🧩",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                Text(
                    text = proposal.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
            }

            // Situation Context
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (isFrench) "Contexte de la situation d'intégration :" else "سياق الوضعية الإدماجية:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = proposal.situationContext,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 17.sp
                    )
                }
            }

            // Tasks
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isFrench) "Tâches et consignes à réaliser :" else "المهام والأنشطة المطلوب إنجازها:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                proposal.tasks.forEach { task ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = task,
                            fontSize = 11.5.sp,
                            color = Color(0xFF334155),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Targeted Skills
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFrench) "Compétences ciblées :" else "الكفايات والمهارات المدمجة:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = proposal.targetedSkills,
                        fontSize = 11.sp,
                        color = Color(0xFF047857),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AssessmentProposalCard(
    proposal: AssessmentProposal,
    isFrench: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "📝",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = proposal.title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = proposal.questionType,
                        fontSize = 11.sp,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Assessment Items
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isFrench) "Éléments et questions d'évaluation :" else "بنود التقويم والأسئلة المقترحة:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                proposal.assessmentItems.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item,
                            fontSize = 11.5.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Success Criteria
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFrench) "Critères de réussite :" else "معايير النجاح والتحقق:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                    Text(
                        text = proposal.successCriteria,
                        fontSize = 11.sp,
                        color = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RemediationProposalCard(
    proposal: RemediationProposal,
    isFrench: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Expected Difficulty (الصعوبة المشخصة)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, Color(0xFFFECACA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "⚠️", fontSize = 13.sp)
                    Column {
                        Text(
                            text = if (isFrench) "Difficulté diagnostiquée :" else "الصعوبة المشخصة أو المتوقعة:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = proposal.expectedDifficulty,
                            fontSize = 11.5.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Remediation Activity (النشاط العلاجي)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "💡", fontSize = 13.sp)
                    Column {
                        Text(
                            text = if (isFrench) "Activité de remédiation ciblée :" else "النشاط العلاجي المقترح:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = proposal.remediationActivity,
                            fontSize = 11.5.sp,
                            color = Color(0xFF14532D),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Pedagogical Support (الوسيط والدعم)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFrench) "Support & médiation :" else "الدعم والوسيط البيداغوجي:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = proposal.pedagogicalSupport,
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


/** عنوان قسم صفحات الكتاب (يظهر بعد ورقة التحضير). */
@Composable
private fun BookPagesHeader(bp: com.example.data.planning.LessonBookPages, isFrench: Boolean) {
    val pagesText = bp.pages.joinToString(if (isFrench) ", " else "، ")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (isFrench) "Pages du manuel liées à cette préparation" else "صفحات الكتاب المرتبطة بهذا التحضير",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E3A8A),
            textAlign = TextAlign.Center
        )
        Text(
            text = bp.title + (if (isFrench) " — p. " else " — ص ") + pagesText,
            fontSize = 11.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        if (bp.aux) {
            Text(
                text = if (isFrench) "Pages suggérées (مقترح تربوي مساعد) : à vérifier par l'enseignant." else "صفحات مقترحة للاستئناس (مقترح تربوي مساعد) وليست مرجعًا رسميًا ملزمًا.",
                fontSize = 11.sp,
                color = Color(0xFFD32F2F),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

/** صورة صفحة من الكتاب: تُقرأ من حزمة الكتب التي تنزّلها Google Play بعد التثبيت. */
@Composable
private fun BookPageImage(book: String, page: Int) {
    val context = LocalContext.current
    val bitmapState = produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, book, page) {
        var tries = 0
        while (value == null && tries < 200) {
            value = withContext(Dispatchers.IO) {
                try {
                    com.example.data.BookPack.open(context, book, page)?.use { input ->
                        input.readBytes().decodeToImageBitmap()
                    }
                } catch (e: Exception) {
                    null
                }
            }
            if (value == null) {
                // الحزمة لم تصل بعد: اطلب التنزيل وأعد المحاولة بعد قليل
                com.example.data.BookPack.requestDownload(context)
                kotlinx.coroutines.delay(3000)
                tries++
            }
        }
    }
    val bitmap = bitmapState.value
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(4.dp)),
        color = Color.White,
        shape = RoundedCornerShape(4.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "ص $page",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp, color = Color(0xFF1E3A8A))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiT("جاري تحميل الكتب… تحتاج إنترنت مرة واحدة فقط"),
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


/**
 * تقريب الشاشة: قرص بإصبعين للتكبير والتصغير (من 1× إلى 3×)، سحب بإصبع واحد للتنقل جانبيًا
 * عند التكبير، والنقر مرتين للتكبير أو للعودة إلى الحجم العادي. التمرير الطولي يبقى كما هو.
 * يكبّر ورقة التحضير وصفحات الكتب معًا.
 */
@Composable
internal fun ZoomableSheet(content: @Composable () -> Unit) {
    val contentDirection = LocalLayoutDirection.current
    val baseDensity = LocalDensity.current
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var widthPx by remember { mutableStateOf(0f) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .onSizeChanged { widthPx = it.width.toFloat() }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { tap ->
                        val target = if (scale > 1.05f) 1f else 2f
                        val ratio = target / scale
                        val maxOff = size.width * (target - 1f)
                        offsetX = ((offsetX + tap.x) * ratio - tap.x).coerceIn(0f, maxOff)
                        scale = target
                    })
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.size >= 2) {
                                val zoom = event.calculateZoom()
                                if (zoom != 1f) {
                                    val c = event.calculateCentroid()
                                    val newScale = (scale * zoom).coerceIn(1f, 3f)
                                    val ratio = newScale / scale
                                    val maxOff = size.width * (newScale - 1f)
                                    offsetX = ((offsetX + c.x) * ratio - c.x).coerceIn(0f, maxOff)
                                    scale = newScale
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = scale > 1.05f,
                    state = rememberDraggableState { delta ->
                        val maxOff = widthPx * (scale - 1f)
                        offsetX = (offsetX - delta).coerceIn(0f, maxOff)
                    }
                )
        ) {
            val viewWidth = maxWidth
            val viewHeight = maxHeight
            val s = scale
            CompositionLocalProvider(
                LocalDensity provides Density(baseDensity.density * s, baseDensity.fontScale)
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize(Alignment.TopStart, unbounded = true)
                        .requiredSize(viewWidth, viewHeight / s)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0f)
                            translationX = -offsetX
                        }
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                        content()
                    }
                }
            }
        }
    }
}


/** عرض صفحات الكتاب المرتبطة بالتحضير في شاشة مستقلة، مع التقريب بإصبعين. */
@Composable
private fun BookPagesViewer(
    bp: com.example.data.planning.LessonBookPages,
    isFrench: Boolean,
    onClose: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onClose)
    CompositionLocalProvider(LocalLayoutDirection provides (if (isFrench) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), color = PaperPureWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF8FAFC), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = InkDarkBlue)
                    }
                    Text(
                        text = if (isFrench) "Pages du manuel" else "صفحات الكتاب",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = InkDarkBlue
                    )
                }
            }
            ZoomableSheet {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item(key = "book_pages_header") {
                        BookPagesHeader(bp = bp, isFrench = isFrench)
                    }
                    items(bp.pages, key = { "book_${bp.book}_$it" }) { pageNumber ->
                        BookPageImage(book = bp.book, page = pageNumber)
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}
