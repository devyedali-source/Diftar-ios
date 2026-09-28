package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    val domainText = remember(lesson, context) {
        val lvlCode = com.example.data.planning.canonical.OfficialAnnualPlanningRepository.getLevelCodeFromInt(lesson.level)
        val rawDomain = lesson.domain.ifBlank { "المجال العام" }
        getDomainArabicLabel(context, lvlCode, rawDomain)
    }

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
                                text = "مذكرة تحضير الدرس",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = InkDarkBlue
                            )
                            val termNum = OfficialPlanningHierarchyAdapter.getTermForWeek(currentWeek)
                            Text(
                                text = "الفصل ${getFaslArabicName(termNum)} ← الأسبوع $currentWeek ← ${com.example.data.planning.canonical.OfficialAnnualPlanningRepository.mapMatiereToArabic(lesson.subject)} ← $domainText",
                                fontSize = 11.sp,
                                color = InkSoftBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "الدرس: ${lesson.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkDarkBlue,
                                maxLines = Int.MAX_VALUE,
                                overflow = TextOverflow.Clip,
                                softWrap = true
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
                            border = BorderStroke(1.dp, if (isTaught) Color(0xFF2E7D32) else PaperBorderBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isTaught) Color(0xFFE8F5E9) else Color.Transparent,
                                contentColor = if (isTaught) Color(0xFF1B5E20) else InkDarkBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isTaught) "✓ تم الإنجاز" else "تسجيل كمنجز",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = InkDarkBlue)
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
                            text = "جارٍ تحضير الورقة...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDarkBlue
                        )
                    }
                }
            } else {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // Scrollable White Preparation Sheet
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
                                                    title = "التاريخ الهجري:",
                                                    value = "................",
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = "الموافق الميلادي:",
                                                    value = "................",
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = "المستوى:",
                                                    value = proposalData.levelTitle.ifBlank { "${getLevelArabicName(lesson.level)} ابتدائي" },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = if (proposalData.sessionInfo.isNotBlank()) "رمز الحصة:" else "",
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
                                                    title = "المادة:",
                                                    value = proposalData.subject.ifBlank { lesson.subject },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = "المجال:",
                                                    value = proposalData.domain.ifBlank { domainText },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = "الموضوع:",
                                                    value = proposalData.topic.ifBlank { lesson.title },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                PaperVerticalLine()
                                                HeaderCell(
                                                    title = if (proposalData.durationText.isNotBlank()) "المدة:" else "",
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
                                                text = "الوسائل:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = InkDarkBlue
                                            )
                                            Text(
                                                text = proposalData.teachingAids,
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
                                                text = "المرجع:",
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
                                                        text = "الكفاية:",
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
                                                        text = "الوحدة:",
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
                                                    text = "الهدف المميز:",
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
                                        text = "سير الدرس",
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
                                            SignatureBox(title = "توقيع المعلم", modifier = Modifier.fillMaxWidth())
                                            if (stampTeacherSettings.show) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    OfficialStampVisual(
                                                        roleTitle = "المعلم",
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
                                            SignatureBox(title = "توقيع المدير", modifier = Modifier.fillMaxWidth())
                                            if (stampPrincipalSettings.show) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    OfficialStampVisual(
                                                        roleTitle = "المدير",
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
                                            SignatureBox(title = "توقيع المفتش", modifier = Modifier.fillMaxWidth())
                                        }
                                    }

                                    // تنبيه تربوي أسفل خانات التوقيعات الثلاث
                                    Text(
                                        text = "هذا التحضير لا يغني عن كتابة المعلم لتحضيره الخاص! هذا مثال تحضير مساعد يربط بين التحضير والتخطيط السنوي وأرقام صفحات الدرس من الكتاب",
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
                text = "لا توجد خطوات سير مفصلة لهذا الدرس حاليًا.",
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
                    text = "مراحل الدرس",
                    modifier = Modifier.weight(0.22f)
                )
                PaperVerticalLine()
                TableHeadText(
                    text = "نشاط المعلم",
                    modifier = Modifier.weight(0.35f)
                )
                PaperVerticalLine()
                TableHeadText(
                    text = "نشاط التلميذ",
                    modifier = Modifier.weight(0.33f)
                )
                PaperVerticalLine()
                TableHeadText(
                    text = "المدة",
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
