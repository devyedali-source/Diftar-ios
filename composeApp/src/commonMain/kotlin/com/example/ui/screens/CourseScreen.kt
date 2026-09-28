package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.course.CourseLesson
import com.example.data.course.CourseProgressStore
import com.example.data.course.CourseRepository
import com.example.data.course.CourseSubscriptionStore
import com.example.data.course.CourseTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CourseOkColor = Color(0xFF2E7D32)
private val CourseBadColor = Color(0xFFC62828)
private val CourseMutedColor = Color(0xFF6B6B6B)
private val CourseWarnColor = Color(0xFFE65100)
private val CoursePaperInk = Color(0xFF1A3A8F)
private val CoursePaperLine = Color(0xFF2B4C7E)
private val CoursePaperDesk = Color(0xFFF1F5F9)

private const val COURSE_MSG =
    "واصل الدورة حتى تزيد مستوى نقاطك لتحظى بفرصة أكبر بين المتسابقين"

private val COURSE_OPTION_LETTERS = listOf("أ", "ب", "ج", "د", "هـ")

data class CourseSpecialization(
    val id: String,
    val title: String,
    val trackKey: String
)

val COURSE_SPECIALIZATIONS = listOf(
    CourseSpecialization("MAINAR", "معلم رئيسي عربي", "ar"),
    CourseSpecialization("TEACHAR", "معلم عربي", "ar"),
    CourseSpecialization("MAINDU", "معلم رئيسي مزدوج", "du"),
    CourseSpecialization("TEACHDU", "معلم مزدوج", "du")
)

private fun courseScoreOutOf20(correct: Int, total: Int): Int {
    if (total <= 0) return 0
    return ((correct * 20) + (total / 2)) / total
}

@Composable
private fun CourseBackLink(text: String, onClick: () -> Unit) {
    Text(
        text = "› $text",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = CourseMutedColor,
        modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp)
    )
}

@Composable
fun CourseScreen() {
    val context = LocalContext.current

    // Screen Security Enforcer (FLAG_SECURE) to prevent screenshotting, recording, and recent app previews (Bypassed temporarily until Sep 30, 2026)
    androidx.compose.runtime.DisposableEffect(Unit) {
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

    var loading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var selectedSpec by remember { mutableStateOf<CourseSpecialization?>(null) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var selectedLessonId by remember { mutableStateOf<String?>(null) }
    var inTest by remember { mutableStateOf(false) }
    var examsSubject by remember { mutableStateOf<String?>(null) }
    var writingsSubject by remember { mutableStateOf<String?>(null) }
    var placeRestored by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val ok = withContext(Dispatchers.IO) {
            CourseRepository.load(context) != null &&
                CourseRepository.getTracks(context).isNotEmpty()
        }
        loadFailed = !ok
        if (ok) {
            try {
                val store = com.example.data.state.LastPlaceStore
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val savedSpecId = store.getString(context, "course_spec")
                val sp = COURSE_SPECIALIZATIONS.find { it.id == savedSpecId }
                if (sp != null && CourseSubscriptionStore.isUnlocked(context, sp.id, uid)) {
                    selectedSpec = sp
                    val subj = store.getString(context, "course_subject")
                    if (subj != null && CourseRepository.getSubjectNames(context, sp.trackKey).contains(subj)) {
                        selectedSubject = subj
                        if (store.getBoolean(context, "course_in_exams")) {
                            examsSubject = subj
                        } else if (store.getBoolean(context, "course_in_writings")) {
                            writingsSubject = subj
                        } else {
                            val lId = store.getString(context, "course_lesson")
                            if (lId != null && CourseRepository.getLesson(context, subj, lId) != null) {
                                selectedLessonId = lId
                                inTest = store.getBoolean(context, "course_in_test")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        placeRestored = true
        loading = false
    }

    LaunchedEffect(placeRestored, selectedSpec?.id, selectedSubject, selectedLessonId, inTest, examsSubject, writingsSubject) {
        if (!placeRestored) return@LaunchedEffect
        val store = com.example.data.state.LastPlaceStore
        store.putString(context, "course_spec", selectedSpec?.id)
        store.putString(context, "course_subject", selectedSubject)
        store.putString(context, "course_lesson", selectedLessonId)
        store.putBoolean(context, "course_in_test", inTest)
        store.putBoolean(context, "course_in_exams", examsSubject != null)
        store.putBoolean(context, "course_in_writings", writingsSubject != null)
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            try {
                CourseRepository.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (loading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "جارٍ تحضير الدورة...", fontSize = 14.sp)
        }
        return
    }

    if (loadFailed) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "تعذّر تحميل محتوى الدورة",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CourseBadColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            SelectionContainer {
                Text(
                    text = CourseRepository.lastError ?: "بدون تفاصيل",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val spec = selectedSpec
    val subjectName = selectedSubject
    val lessonId = selectedLessonId

    if (spec == null) {
        CourseSpecializationList(onSelect = {
            selectedSpec = it
            selectedSubject = null
            selectedLessonId = null
            inTest = false
        })
        return
    }

    if (subjectName == null) {
        CourseSubjectList(
            spec = spec,
            onBack = { selectedSpec = null },
            onSelect = {
                selectedSubject = it
                selectedLessonId = null
                inTest = false
            }
        )
        return
    }

    if (writingsSubject != null) {
        CourseWritingsScreen(
            subjectName = subjectName,
            onBack = { writingsSubject = null }
        )
        return
    }

    if (examsSubject != null) {
        CourseSubjectExamsScreen(
            subjectName = subjectName,
            onBack = { examsSubject = null }
        )
        return
    }

    if (lessonId == null) {
        CourseLessonList(
            spec = spec,
            subjectName = subjectName,
            onBack = { selectedSubject = null },
            onSelect = {
                selectedLessonId = it
                inTest = false
            },
            onOpenExams = { examsSubject = subjectName },
            onOpenWritings = { writingsSubject = subjectName }
        )
        return
    }

    val lessons = CourseRepository.getLessons(context, subjectName)
    val currentIndex = lessons.indexOfFirst { it.id == lessonId }
    val lesson = if (currentIndex >= 0) lessons[currentIndex] else null
    val hasNext = currentIndex >= 0 && currentIndex < lessons.size - 1

    if (lesson == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            CourseBackLink(text = "رجوع إلى الدروس", onClick = { selectedLessonId = null })
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "تعذّر العثور على الدرس",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = CourseBadColor
            )
        }
        return
    }

    if (inTest) {
        CourseLessonTest(
            subjectName = subjectName,
            lesson = lesson,
            hasNextLesson = hasNext,
            onExitToLesson = { inTest = false },
            onBackToLessons = {
                inTest = false
                selectedLessonId = null
            },
            onGoToNextLesson = {
                if (hasNext) {
                    selectedLessonId = lessons[currentIndex + 1].id
                    inTest = false
                }
            }
        )
        return
    }

    CourseLessonDetail(
        subjectName = subjectName,
        lesson = lesson,
        onBack = { selectedLessonId = null },
        onStartTest = { inTest = true }
    )
}

@Composable
private fun CourseSpecializationList(onSelect: (CourseSpecialization) -> Unit) {
    val context = LocalContext.current
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val currentUid = currentUser?.uid ?: ""
    val currentEmail = currentUser?.email ?: ""
    var lockedSpec by remember { mutableStateOf<CourseSpecialization?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "اختر التخصّص", fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "دورة تحضير مسابقات المعلمين",
            fontSize = 13.sp,
            color = CourseMutedColor
        )
        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            border = BorderStroke(1.dp, CourseOkColor.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "📢 عن الدورة",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = CourseOkColor
                )
                Text(
                    text = "محتوى هذه الدورة مأخوذ من البرنامج الرسمي لمسابقات المعلمين، وهو نفسه المحتوى الذي يُمتحن فيه التلاميذ المعلمون.",
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp
                )
                Text(
                    text = "وتدرّبك الدورة على المحاكي: امتحانات على نفس نمط الامتحانات الرقمية المعتمدة في منصّة اللجنة الوطنية للمسابقات.",
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        val lockedNow = lockedSpec
        if (lockedNow != null) {
            CourseSubscriptionDialog(
                spec = lockedNow,
                email = currentEmail,
                onUnlocked = { lockedSpec = null },
                onDismiss = { lockedSpec = null }
            )
        }

        for (spec in COURSE_SPECIALIZATIONS) {
            val unlocked = CourseSubscriptionStore.isUnlocked(context, spec.id, currentUid)
            val daysLeft = CourseSubscriptionStore.remainingDays(context, spec.id)
            val expiryText = CourseSubscriptionStore.formatExpiryDate(context, spec.id)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable {
                        if (unlocked) {
                            lockedSpec = null
                            onSelect(spec)
                        } else {
                            lockedSpec = spec
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = if (unlocked) spec.title else "🔒 ${spec.title}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (unlocked) {
                            val expiringSoon = CourseSubscriptionStore.isExpiringSoon(
                                context,
                                spec.id,
                                currentUid
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "مفتوح · تبقّى $daysLeft يومًا · ينتهي في $expiryText",
                                fontSize = 12.sp,
                                color = if (expiringSoon) CourseWarnColor else CourseOkColor
                            )
                            if (expiringSoon) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "اشتراكك يقارب الانتهاء، جدّده قبل انقطاع المحتوى.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CourseWarnColor
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "يحتاج تفعيلًا",
                                fontSize = 12.sp,
                                color = CourseWarnColor
                            )
                        }
                    }
                    Text(text = "‹", fontSize = 18.sp, color = CourseMutedColor)
                }
            }
        }
    }
}

@Composable
private fun CourseSubjectList(
    spec: CourseSpecialization,
    onBack: () -> Unit,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    val subjectNames = CourseRepository.getSubjectNames(context, spec.trackKey)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CourseBackLink(text = "رجوع إلى التخصّصات", onClick = onBack)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = spec.title, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "اختر المادة", fontSize = 13.sp, color = CourseMutedColor)
        Spacer(modifier = Modifier.height(14.dp))

        for (name in subjectNames) {
            val subject = CourseRepository.getSubject(context, name)
            val lessonsCount = subject?.lessons?.size ?: 0
            val examsCount = (subject?.sub?.size ?: 0) + (subject?.sim?.size ?: 0)
            val writingsCount = subject?.wr?.size ?: 0
            val passed = CourseProgressStore.countPassedLessons(context, name)
            val percent = CourseProgressStore.subjectProgressPercent(context, name)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { onSelect(name) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$lessonsCount درسًا · $examsCount امتحانًا" +
                            (if (writingsCount > 0) " · $writingsCount موضوع إنتاج" else ""),
                        fontSize = 13.sp,
                        color = CourseMutedColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "المنجَز: $passed من $lessonsCount ($percent٪)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (passed > 0) CourseOkColor else CourseMutedColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseLessonList(
    spec: CourseSpecialization,
    subjectName: String,
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
    onOpenExams: () -> Unit,
    onOpenWritings: () -> Unit
) {
    val context = LocalContext.current
    val lessons = CourseRepository.getLessons(context, subjectName)
    val grouped = lessons.groupBy { it.dom }
    val passed = CourseProgressStore.countPassedLessons(context, subjectName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CourseBackLink(text = "رجوع إلى المواد", onClick = onBack)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = subjectName, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${spec.title} · ${lessons.size} درسًا · المنجَز $passed",
            fontSize = 13.sp,
            color = CourseMutedColor
        )
        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .clickable { onOpenExams() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (CourseProgressStore.areSubjectExamsUnlocked(context, subjectName)) {
                        "امتحانات المادة"
                    } else {
                        "🔒 امتحانات المادة"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = CourseOkColor
                )
                Text(text = "‹", fontSize = 18.sp, color = CourseMutedColor)
            }
        }

        if (CourseRepository.getWritings(context, subjectName).isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { onOpenWritings() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الإنتاج الكتابي",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = CourseOkColor
                    )
                    Text(text = "‹", fontSize = 18.sp, color = CourseMutedColor)
                }
            }
        }

        for ((domain, domainLessons) in grouped) {
            Text(
                text = "$domain (${domainLessons.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = CourseOkColor,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )

            for (lesson in domainLessons) {
                val score = CourseProgressStore.getLessonScore(context, lesson.id)
                val isPassed = CourseProgressStore.isLessonPassed(context, lesson.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelect(lesson.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text(
                                text = (if (isPassed) "✅ " else "") + "${lesson.no}. ${lesson.title}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (score != null) {
                                    "${lesson.qs.size} سؤالًا · آخر نقطة: $score/20"
                                } else {
                                    "${lesson.qs.size} سؤالًا"
                                },
                                fontSize = 12.sp,
                                color = if (isPassed) CourseOkColor else CourseMutedColor
                            )
                        }
                        Text(text = "‹", fontSize = 18.sp, color = CourseMutedColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseSectionTitle(text: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp)) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = CoursePaperInk
        )
        HorizontalDivider(color = CoursePaperLine.copy(alpha = 0.5f), thickness = 0.8.dp)
    }
}

@Composable
private fun CourseBulletList(items: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        for (item in items) {
            Text(
                text = "• $item",
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun CourseLessonDetail(
    subjectName: String,
    lesson: CourseLesson,
    onBack: () -> Unit,
    onStartTest: () -> Unit
) {
    val context = LocalContext.current
    val score = CourseProgressStore.getLessonScore(context, lesson.id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoursePaperDesk)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CourseBackLink(text = "رجوع إلى الدروس", onClick = onBack)
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(4.dp)),
            color = Color.White,
            contentColor = CoursePaperInk,
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text(
                    text = "${lesson.no}. ${lesson.title}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = CoursePaperInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${lesson.dom} · $subjectName",
                    fontSize = 13.sp,
                    color = CourseMutedColor
                )
                if (lesson.ref.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "المرجع: ${lesson.ref}", fontSize = 12.sp, color = CourseMutedColor)
                }
                if (score != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "أفضل نقطة لك في هذا الدرس: $score/20",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (score >= 10) CourseOkColor else CourseWarnColor
                    )
                }

                if (lesson.st.isNotBlank()) {
                    CourseSectionTitle("وضعية الدرس")
                    Text(text = lesson.st, fontSize = 14.sp)
                }

                if (lesson.sum.isNotBlank()) {
                    CourseSectionTitle("الملخّص")
                    Text(text = lesson.sum, fontSize = 14.sp, lineHeight = 22.sp)
                }

                if (lesson.obj.isNotEmpty()) {
                    CourseSectionTitle("الأهداف")
                    CourseBulletList(lesson.obj)
                }

                if (lesson.met.isNotEmpty()) {
                    CourseSectionTitle("طريقة المعالجة")
                    CourseBulletList(lesson.met)
                }

                if (lesson.err.isNotEmpty()) {
                    CourseSectionTitle("أخطاء شائعة")
                    CourseBulletList(lesson.err)
                }

                if (lesson.ex.isNotEmpty()) {
                    CourseSectionTitle("أمثلة محلولة")
                    for (example in lesson.ex) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)) {
                            Text(
                                text = example.prompt,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الجواب: ${example.answer}",
                                fontSize = 14.sp,
                                color = CourseOkColor
                            )
                            if (example.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = example.explanation,
                                    fontSize = 13.sp,
                                    color = CourseMutedColor
                                )
                            }
                        }
                    }
                }

                if (lesson.foc.isNotBlank()) {
                    CourseSectionTitle("ما يكثر السؤال عنه")
                    Text(text = lesson.foc, fontSize = 14.sp, lineHeight = 22.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onStartTest() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "اختبر نفسك (${lesson.qs.size} سؤالًا)", fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CourseLessonTest(
    subjectName: String,
    lesson: CourseLesson,
    hasNextLesson: Boolean,
    onExitToLesson: () -> Unit,
    onBackToLessons: () -> Unit,
    onGoToNextLesson: () -> Unit
) {
    val context = LocalContext.current
    val total = lesson.qs.size

    val testPosId = "lesson_" + lesson.id
    val restoredPos = remember(lesson.id) {
        com.example.data.course.CourseAttemptStore.loadTestPosition(context, testPosId)
            ?.takeIf { it.first in 0 until total && it.second in 0..it.first }
    }

    var attempt by remember(lesson.id) { mutableStateOf(0) }
    var index by remember(lesson.id, attempt) { mutableStateOf(if (attempt == 0) (restoredPos?.first ?: 0) else 0) }
    var correctCount by remember(lesson.id, attempt) { mutableStateOf(if (attempt == 0) (restoredPos?.second ?: 0) else 0) }
    var selected by remember(lesson.id, attempt) { mutableStateOf<Int?>(null) }
    var finished by remember(lesson.id, attempt) { mutableStateOf(false) }

    LaunchedEffect(lesson.id, attempt, index) {
        if (!finished) {
            com.example.data.course.CourseAttemptStore.saveTestPosition(context, testPosId, index, correctCount)
        }
    }
    LaunchedEffect(lesson.id, attempt, finished) {
        if (finished) {
            com.example.data.course.CourseAttemptStore.clearTestPosition(context, testPosId)
        }
    }

    val finalScore = courseScoreOutOf20(correctCount, total)

    LaunchedEffect(lesson.id, attempt, finished) {
        if (finished) {
            CourseProgressStore.saveLessonScore(context, lesson.id, finalScore)
        }
    }

    if (finished) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "نتيجة اختبار الدرس", fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${lesson.no}. ${lesson.title} · $subjectName",
                fontSize = 13.sp,
                color = CourseMutedColor
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$finalScore / 20",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            finalScore >= 10 -> CourseOkColor
                            finalScore >= 7 -> CourseWarnColor
                            else -> CourseBadColor
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "إجابات صحيحة: $correctCount من $total",
                        fontSize = 14.sp,
                        color = CourseMutedColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when {
                            finalScore >= 10 -> "نجحتَ في اختبار هذا الدرس ✅"
                            finalScore >= 7 -> "قريب من النجاح، والنجاح بعشر"
                            else -> "لم تبلغ الحدّ المطلوب"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = COURSE_MSG,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CourseOkColor
            )
            Spacer(modifier = Modifier.height(18.dp))

            if (finalScore >= 7) {
                if (hasNextLesson) {
                    Button(
                        onClick = { onGoToNextLesson() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "الدرس الموالي", fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = { onExitToLesson() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "إعادة قراءة الدرس", fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { attempt++ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "إعادة الامتحان", fontSize = 15.sp)
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ننصحك بإعادة قراءة الدرس قراءةً متأنّية قبل إعادة الاختبار، " +
                            "فالمراجعة الهادئة أنفع من تكرار المحاولة.",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onExitToLesson() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "إعادة قراءة الدرس", fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { attempt++ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "إعادة الامتحان", fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            CourseBackLink(text = "رجوع إلى قائمة الدروس", onClick = onBackToLessons)
            Spacer(modifier = Modifier.height(24.dp))
        }
        return
    }

    val question = lesson.qs[index]
    val chosen = selected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CourseBackLink(text = "الخروج من الاختبار", onClick = onExitToLesson)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "السؤال ${index + 1} من $total",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CourseMutedColor
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = question.q,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        for (optionIndex in question.c.indices) {
            val isCorrectOption = optionIndex == question.k
            val isChosen = chosen == optionIndex
            val borderColor = when {
                chosen == null -> Color(0xFFBDBDBD)
                isCorrectOption -> CourseOkColor
                isChosen -> CourseBadColor
                else -> Color(0xFFBDBDBD)
            }
            val letter = if (optionIndex < COURSE_OPTION_LETTERS.size) {
                COURSE_OPTION_LETTERS[optionIndex]
            } else {
                "${optionIndex + 1}"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable(enabled = chosen == null) {
                        if (chosen == null) {
                            selected = optionIndex
                            if (isCorrectOption) correctCount++
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$letter. ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = borderColor
                )
                Text(
                    text = question.c[optionIndex],
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        if (chosen != null) {
            Spacer(modifier = Modifier.height(12.dp))
            val isRight = chosen == question.k
            Text(
                text = if (isRight) "إجابة صحيحة ✅" else "إجابة خاطئة ❌",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = if (isRight) CourseOkColor else CourseBadColor
            )
            if (chosen < question.f.size && question.f[chosen].isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = question.f[chosen], fontSize = 14.sp)
            }
            if (!isRight) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "الصواب: ${question.c[question.k]}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CourseOkColor
                )
            }
            if (question.e.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = question.e,
                        fontSize = 13.sp,
                        color = CourseMutedColor,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (index < total - 1) {
                        index++
                        selected = null
                    } else {
                        finished = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (index < total - 1) "السؤال الموالي" else "عرض النتيجة",
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CourseCheckRow(label: String, actual: Int, expected: Int) {
    val ok = actual == expected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(
            text = if (ok) "$actual / $expected ✅" else "$actual / $expected ❌",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (ok) CourseOkColor else CourseBadColor
        )
    }
}

@Composable
fun CourseVerificationScreen() {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf<CourseRepository.CourseStats?>(null) }
    var tracks by remember { mutableStateOf<List<CourseTrack>>(emptyList()) }

    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) {
            val s = CourseRepository.getStats(context)
            val t = CourseRepository.getTracks(context)
            Pair(s, t)
        }
        stats = loaded.first
        tracks = loaded.second
        loading = false
    }

    if (loading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "جارٍ قراءة محتوى الدورة...", fontSize = 14.sp)
        }
        return
    }

    val s = stats
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "تحقّق مؤقّت من محتوى الدورة",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (s == null || s.lessons == 0) {
            Text(
                text = "فشلت قراءة الملف ❌",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CourseBadColor
            )
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    CourseCheckRow("المواد", s.subjects, 4)
                    CourseCheckRow("الدروس", s.lessons, 181)
                    CourseCheckRow("أسئلة الدروس", s.lessonQuestions, 5430)
                    CourseCheckRow("الامتحانات", s.exams, 80)
                    CourseCheckRow("أسئلة الامتحانات", s.examQuestions, 1800)
                    CourseCheckRow("مواضيع الإنتاج الكتابي", s.writings, 20)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "المسارات", fontSize = 16.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            for (track in tracks) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${track.name}  (${track.k})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = track.subs.joinToString("، "), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseSubscriptionDialog(
    spec: CourseSpecialization,
    email: String,
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val expiryPreview = CourseSubscriptionStore.previewExpiryDateFromNow(
        CourseSubscriptionStore.SUBSCRIPTION_DAYS
    )

    val currentUidForRequest =
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    val newRequestId = remember(spec.id) {
        "${currentUidForRequest}_${spec.id}_${System.currentTimeMillis()}"
    }
    val savedRequestId = remember(spec.id) {
        CourseSubscriptionStore.getLastRequestId(context, spec.id)
    }
    val requestId = newRequestId
    var watchedRequestId by remember(spec.id) {
        mutableStateOf(if (savedRequestId.isNotBlank()) savedRequestId else newRequestId)
    }

    androidx.compose.runtime.DisposableEffect(watchedRequestId) {
        var registration: com.google.firebase.firestore.ListenerRegistration? = null
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            registration = db.collection("course_activation_requests")
                .document(watchedRequestId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val status = snapshot.getString("status") ?: ""
                    val expiresAt = snapshot.getLong("expiresAt") ?: 0L
                    if (status == "completed" && expiresAt > System.currentTimeMillis()) {
                        val alreadyUnlocked = CourseSubscriptionStore.isUnlocked(
                            context,
                            spec.id,
                            currentUidForRequest
                        )
                        CourseSubscriptionStore.saveActivationUntil(
                            context,
                            spec.id,
                            currentUidForRequest,
                            expiresAt
                        )
                        if (!alreadyUnlocked) {
                            try {
                                val database = com.example.data.db.TeacherDatabase.getDatabase(
                                    context,
                                    currentUidForRequest
                                )
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    try {
                                        database.teacherDao().insertLocalNotification(
                                            com.example.data.models.LocalNotification(
                                                title = "تم تفعيل الدورة ✅",
                                                message = "تم تفعيل تخصّص: ${spec.title} في دورة تحضير مسابقات المعلمين",
                                                createdAt = System.currentTimeMillis(),
                                                isRead = false
                                            )
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        onUnlocked()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                registration?.remove()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    val activationLink = "https://diftar-almoaalim.web.app/course-activate/$requestId"

    val messageText = "السلام عليكم،\n" +
        "أنا المعلم صاحب البريد: ${if (email.isBlank()) "غير متوفّر" else email}\n" +
        "أريد تفعيل دورة تحضير مسابقات المعلمين\n\n" +
        "التخصّص المطلوب: ${spec.title}\n" +
        "المدّة: ${CourseSubscriptionStore.SUBSCRIPTION_DAYS} يومًا\n" +
        "السعر: ${CourseSubscriptionStore.SUBSCRIPTION_PRICE}\n" +
        "------------------------------------\n" +
        "🔑 رقم طلب التفعيل:\n" +
        "$requestId\n" +
        "------------------------------------\n" +
        "رابط طلب التفعيل:\n" +
        activationLink

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "تفعيل الدورة", fontWeight = FontWeight.Black, fontSize = 17.sp)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "🔒 ${spec.title}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = CourseWarnColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "المدّة", fontSize = 14.sp, color = CourseMutedColor)
                    Text(
                        text = "${CourseSubscriptionStore.SUBSCRIPTION_DAYS} يومًا",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "السعر", fontSize = 14.sp, color = CourseMutedColor)
                    Text(
                        text = CourseSubscriptionStore.SUBSCRIPTION_PRICE,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CourseOkColor
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "ينتهي في", fontSize = 14.sp, color = CourseMutedColor)
                    Text(
                        text = expiryPreview,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "بريدك: ${if (email.isBlank()) "غير متوفّر" else email}",
                    fontSize = 12.sp,
                    color = CourseMutedColor
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "كل تخصّص يُفعَّل وحده، وتفعيل هذا التخصّص لا يفتح التخصّصات الأخرى.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CourseWarnColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "بالضغط على الزرّ يُفتح الواتساب برسالة جاهزة إلى مدير الأقسام.",
                    fontSize = 12.sp,
                    color = CourseMutedColor
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val requestDoc = mapOf(
                            "requestId" to requestId,
                            "userId" to currentUidForRequest,
                            "email" to email,
                            "specId" to spec.id,
                            "specTitle" to spec.title,
                            "days" to CourseSubscriptionStore.SUBSCRIPTION_DAYS,
                            "status" to "pending",
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("course_activation_requests")
                            .document(requestId)
                            .set(requestDoc)
                        CourseSubscriptionStore.saveLastRequestId(context, spec.id, requestId)
                        watchedRequestId = requestId
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        val encodedMessage = android.net.Uri.encode(messageText)
                        val url = "https://api.whatsapp.com/send?phone=22237786585&text=$encodedMessage"
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                        )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            context,
                            "لم نتمكن من فتح الواتساب، يرجى التأكد من توفر التطبيق على جهازك.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                    onDismiss()
                }
            ) {
                Text(text = "إرسال طلب التفعيل", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "إغلاق")
            }
        }
    )
}
