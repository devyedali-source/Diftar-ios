package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.course.CourseAttemptStore
import com.example.data.course.CourseExam
import com.example.data.course.CourseProgressStore
import com.example.data.course.CourseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SimOkColor = Color(0xFF2E7D32)
private val SimBadColor = Color(0xFFC62828)
private val SimMutedColor = Color(0xFF6B6B6B)
private val SimWarnColor = Color(0xFFE65100)
private val SimMarkColor = Color(0xFF6750A4)
private val SimIdleColor = Color(0xFFBDBDBD)

private const val SIM_MSG =
    "واصل الدورة حتى تزيد مستوى نقاطك لتحظى بفرصة أكبر بين المتسابقين"

private val SIM_LETTERS = listOf("أ", "ب", "ج", "د", "هـ")

private fun simMinutesFromMeta(meta: String): Int {
    val digits = StringBuilder()
    for (ch in meta) {
        if (ch.isDigit()) {
            digits.append(ch)
        } else if (digits.isNotEmpty()) {
            break
        }
    }
    val value = digits.toString().toIntOrNull() ?: 120
    return if (value in 1..300) value else 120
}

private fun simFormatTime(totalSeconds: Int): String {
    val s = if (totalSeconds > 0) totalSeconds else 0
    val hours = s / 3600
    val minutes = (s % 3600) / 60
    val seconds = s % 60
    val mm = if (minutes < 10) "0$minutes" else "$minutes"
    val ss = if (seconds < 10) "0$seconds" else "$seconds"
    return if (hours > 0) "$hours:$mm:$ss" else "$mm:$ss"
}

@Composable
private fun SimBackLink(text: String, onClick: () -> Unit) {
    Text(
        text = "› $text",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = SimMutedColor,
        modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp)
    )
}

@Composable
fun CourseSimulatorScreen(subjectName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var openedId by remember(subjectName) { mutableStateOf<String?>(null) }

    val exams = CourseRepository.getSimulatorExams(context, subjectName)
    val opened = exams.firstOrNull { it.id == openedId }

    if (opened != null) {
        CourseSimulatorRunner(
            subjectName = subjectName,
            exam = opened,
            onBack = { openedId = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SimBackLink(text = "رجوع إلى الامتحانات", onClick = onBack)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "المحاكي", fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$subjectName · ${exams.size} امتحانًا على نمط اللجنة الوطنية",
            fontSize = 13.sp,
            color = SimMutedColor
        )
        Spacer(modifier = Modifier.height(14.dp))

        for (exam in exams) {
            val score = CourseProgressStore.getExamScore(context, exam.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { openedId = exam.id }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                        text = "محاكاة رقم ${exam.n}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (exam.meta.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = exam.meta, fontSize = 12.sp, color = SimMutedColor)
                    }
                    if (score != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "أفضل نقطة لك: $score/20",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (score >= 10) SimOkColor else SimWarnColor
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CourseSimulatorRunner(
    subjectName: String,
    exam: CourseExam,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val questions = exam.qs
    val total = questions.size
    val totalPoints = questions.sumOf { it.p ?: 0 }
    val durationSeconds = simMinutesFromMeta(exam.meta) * 60

    var attempt by remember(exam.id) { mutableStateOf(0) }
    var started by remember(exam.id, attempt) {
        mutableStateOf(CourseAttemptStore.hasActiveAttempt(context, exam.id))
    }
    var submitted by remember(exam.id, attempt) { mutableStateOf(false) }
    var showSummary by remember(exam.id, attempt) { mutableStateOf(false) }
    var remaining by remember(exam.id, attempt) {
        mutableStateOf(CourseAttemptStore.remainingSeconds(context, exam.id, durationSeconds))
    }
    val answers = remember(exam.id, attempt) {
        val restored = mutableStateMapOf<Int, Set<Int>>()
        restored.putAll(CourseAttemptStore.loadAnswers(context, exam.id))
        restored
    }
    val marked = remember(exam.id, attempt) {
        val restored = mutableStateMapOf<Int, Boolean>()
        for (questionIndex in CourseAttemptStore.loadMarked(context, exam.id)) {
            restored[questionIndex] = true
        }
        restored
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val hasText = exam.text != null && exam.text!!.isNotBlank()
    val hasSituation = exam.situationText != null && exam.situationText!!.isNotBlank()
    val headerCount = 1 + (if (hasText) 1 else 0) + (if (hasSituation) 1 else 0)

    var earnedPoints = 0
    for (i in questions.indices) {
        val q = questions[i]
        val given = answers[i] ?: emptySet()
        if (given.isNotEmpty() && given == q.ks.toSet()) {
            earnedPoints += (q.p ?: 0)
        }
    }
    val finalScore = if (totalPoints > 0) {
        ((earnedPoints * 20) + (totalPoints / 2)) / totalPoints
    } else {
        0
    }

    LaunchedEffect(exam.id, attempt, started, submitted) {
        if (started && !submitted) {
            while (true) {
                remaining = CourseAttemptStore.remainingSeconds(context, exam.id, durationSeconds)
                if (remaining <= 0) break
                delay(1000L)
            }
            if (!submitted) {
                showSummary = false
                submitted = true
            }
        }
    }

    LaunchedEffect(exam.id, attempt, submitted) {
        if (submitted) {
            CourseProgressStore.saveExamScore(context, exam.id, finalScore)
            CourseAttemptStore.clearAttempt(context, exam.id)
        }
    }

    if (!started) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SimBackLink(text = "رجوع إلى المحاكي", onClick = onBack)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "محاكاة رقم ${exam.n}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subjectName, fontSize = 13.sp, color = SimMutedColor)
            Spacer(modifier = Modifier.height(14.dp))

            if (exam.meta.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = exam.meta,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (exam.struct.isNotBlank()) {
                Text(text = exam.struct, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "تنبيهات قبل البدء",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = SimWarnColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• المؤقّت ينزل ولا يتوقّف بعد البدء.", fontSize = 13.sp)
                    Text(text = "• كل الأسئلة في صفحة واحدة، والتنقّل بينها حرّ.", fontSize = 13.sp)
                    Text(text = "• بعض الأسئلة تقبل أكثر من إجابة صحيحة.", fontSize = 13.sp)
                    Text(text = "• التسليم نهائي لا رجعة فيه.", fontSize = 13.sp)
                    Text(
                        text = "• عند انتهاء الوقت يُسلَّم الامتحان تلقائيًّا.",
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    CourseAttemptStore.startAttempt(context, exam.id)
                    remaining = durationSeconds
                    started = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "بدء المحاكاة ($total سؤالًا)", fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        return
    }

    if (submitted) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "نتيجة المحاكاة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "محاكاة رقم ${exam.n} · $subjectName",
                        fontSize = 13.sp,
                        color = SimMutedColor
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$finalScore / 20",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = if (finalScore >= 10) SimOkColor else SimBadColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "النقاط المحصّلة: $earnedPoints من $totalPoints",
                                fontSize = 14.sp,
                                color = SimMutedColor
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (finalScore >= 10) {
                                    "ناجح ✅"
                                } else {
                                    "لم تبلغ النجاح (النجاح بعشر)"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = SIM_MSG,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SimOkColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "التصحيح بالشروح",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            itemsIndexed(questions) { qIndex, question ->
                val given = answers[qIndex] ?: emptySet()
                val keys = question.ks.toSet()
                val isRight = given.isNotEmpty() && given == keys

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "السؤال ${qIndex + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SimMutedColor
                            )
                            Text(
                                text = if (isRight) {
                                    "صحيحة ✅ (+${question.p ?: 0})"
                                } else {
                                    "خاطئة ❌"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRight) SimOkColor else SimBadColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = question.q,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        for (oi in question.c.indices) {
                            val letter = if (oi < SIM_LETTERS.size) SIM_LETTERS[oi] else "${oi + 1}"
                            val isKey = keys.contains(oi)
                            val isGiven = given.contains(oi)
                            val mark = when {
                                isKey && isGiven -> "✅"
                                isKey -> "◀"
                                isGiven -> "❌"
                                else -> "•"
                            }
                            Text(
                                text = "$mark $letter. ${question.c[oi]}",
                                fontSize = 13.sp,
                                color = when {
                                    isKey -> SimOkColor
                                    isGiven -> SimBadColor
                                    else -> Color.Unspecified
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        if (given.size == 1) {
                            val only = given.first()
                            if (only < question.f.size && question.f[only].isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = question.f[only], fontSize = 13.sp)
                            }
                        }

                        if (question.e.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = question.e,
                                fontSize = 13.sp,
                                color = SimMutedColor
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { attempt++ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "إعادة المحاكاة", fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onBack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "رجوع إلى المحاكي", fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        return
    }

    val answeredCount = questions.indices.count { (answers[it] ?: emptySet()).isNotEmpty() }
    val markedCount = questions.indices.count { marked[it] == true }

    if (showSummary) {
        AlertDialog(
            onDismissRequest = { showSummary = false },
            title = {
                Text(text = "ملخّص المحاولة", fontWeight = FontWeight.Black)
            },
            text = {
                Column {
                    Text(text = "المُجاب عنها: $answeredCount من $total", fontSize = 14.sp)
                    Text(
                        text = "الفارغة: ${total - answeredCount}",
                        fontSize = 14.sp,
                        color = if (answeredCount < total) SimWarnColor else SimMutedColor
                    )
                    Text(text = "الموسومة بعلامة: $markedCount", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "التسليم نهائي لا رجعة فيه.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = SimBadColor
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSummary = false
                    submitted = true
                }) {
                    Text(text = "تسليم نهائي", color = SimBadColor, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSummary = false }) {
                    Text(text = "متابعة الامتحان")
                }
            }
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        item {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محاكاة ${exam.n} · $subjectName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SimMutedColor
                    )
                    Text(
                        text = "⏳ ${simFormatTime(remaining)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (remaining <= 300) SimBadColor else SimOkColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "المُجاب: $answeredCount / $total · الموسوم: $markedCount",
                    fontSize = 12.sp,
                    color = SimMutedColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                for (row in questions.indices.chunked(5)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (qi in row) {
                            val answered = (answers[qi] ?: emptySet()).isNotEmpty()
                            val isMarked = marked[qi] == true
                            val chipColor = when {
                                isMarked -> SimMarkColor
                                answered -> SimOkColor
                                else -> SimIdleColor
                            }
                            Box(
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(38.dp)
                                    .border(2.dp, chipColor, RoundedCornerShape(9.dp))
                                    .clickable {
                                        scope.launch {
                                            listState.animateScrollToItem(headerCount + qi)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${qi + 1}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chipColor
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showSummary = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "تسليم الامتحان", fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (hasText) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (!exam.textTitle.isNullOrBlank()) {
                            Text(
                                text = exam.textTitle ?: "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        Text(text = exam.text ?: "", fontSize = 14.sp)
                    }
                }
            }
        }

        if (hasSituation) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "الوضعية",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = exam.situationText ?: "", fontSize = 14.sp)
                    }
                }
            }
        }

        itemsIndexed(questions) { qIndex, question ->
            val given = answers[qIndex] ?: emptySet()
            val isMulti = question.m == "multi"
            val isMarked = marked[qIndex] == true

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "السؤال ${qIndex + 1} من $total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SimMutedColor
                        )
                        Text(
                            text = if (isMarked) "★ ضع علامة" else "☆ ضع علامة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMarked) SimMarkColor else SimMutedColor,
                            modifier = Modifier
                                .clickable {
                                    marked[qIndex] = !isMarked
                                    CourseAttemptStore.saveMarked(
                                        context,
                                        exam.id,
                                        marked.filterValues { it }.keys
                                    )
                                }
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = question.q,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isMulti) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "هذا السؤال يقبل أكثر من إجابة صحيحة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SimWarnColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    for (oi in question.c.indices) {
                        val isChosen = given.contains(oi)
                        val letter = if (oi < SIM_LETTERS.size) SIM_LETTERS[oi] else "${oi + 1}"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(
                                    1.dp,
                                    if (isChosen) SimMarkColor else SimIdleColor,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    val current = answers[qIndex] ?: emptySet()
                                    answers[qIndex] = if (isMulti) {
                                        if (current.contains(oi)) current - oi else current + oi
                                    } else {
                                        setOf(oi)
                                    }
                                    CourseAttemptStore.saveAnswers(
                                        context,
                                        exam.id,
                                        answers.toMap()
                                    )
                                }
                                .padding(11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isChosen) "◉ " else "○ ",
                                fontSize = 14.sp,
                                color = if (isChosen) SimMarkColor else SimIdleColor
                            )
                            Text(
                                text = "$letter. ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isChosen) SimMarkColor else SimMutedColor
                            )
                            Text(text = question.c[oi], fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        item {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showSummary = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "تسليم الامتحان", fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
