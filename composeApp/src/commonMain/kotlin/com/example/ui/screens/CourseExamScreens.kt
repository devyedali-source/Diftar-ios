package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.course.CourseExam
import com.example.data.course.CourseProgressStore
import com.example.data.course.CourseRepository

private val ExamOkColor = Color(0xFF2E7D32)
private val ExamBadColor = Color(0xFFC62828)
private val ExamMutedColor = Color(0xFF6B6B6B)
private val ExamWarnColor = Color(0xFFE65100)

private const val EXAM_MSG =
    "واصل الدورة حتى تزيد مستوى نقاطك لتحظى بفرصة أكبر بين المتسابقين"

private val EXAM_LETTERS = listOf("أ", "ب", "ج", "د", "هـ")

private fun examScoreOutOf20(correct: Int, total: Int): Int {
    if (total <= 0) return 0
    return ((correct * 20) + (total / 2)) / total
}

@Composable
private fun ExamBackLink(text: String, onClick: () -> Unit) {
    Text(
        text = "› $text",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = ExamMutedColor,
        modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp)
    )
}

@Composable
fun CourseSubjectExamsScreen(subjectName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val placeKeyOpen = "course_exam_open_" + subjectName
    val placeKeySim = "course_exam_sim_" + subjectName
    val examsUnlockedAtStart = remember(subjectName) { CourseProgressStore.areSubjectExamsUnlocked(context, subjectName) }
    var openedExamId by remember(subjectName) {
        mutableStateOf<String?>(
            if (examsUnlockedAtStart) com.example.data.state.LastPlaceStore.getString(context, placeKeyOpen) else null
        )
    }
    var inSimulator by remember(subjectName) {
        mutableStateOf(examsUnlockedAtStart && com.example.data.state.LastPlaceStore.getBoolean(context, placeKeySim))
    }
    LaunchedEffect(openedExamId) {
        com.example.data.state.LastPlaceStore.putString(context, placeKeyOpen, openedExamId)
    }
    LaunchedEffect(inSimulator) {
        com.example.data.state.LastPlaceStore.putBoolean(context, placeKeySim, inSimulator)
    }
    var showLockMessage by remember(subjectName) { mutableStateOf(false) }

    val exams = CourseRepository.getSubjectExams(context, subjectName)
    val unlocked = CourseProgressStore.areSubjectExamsUnlocked(context, subjectName)
    val remaining = CourseProgressStore.lessonsNeededToUnlockExams(context, subjectName)
    val openedExam = exams.firstOrNull { it.id == openedExamId }

    if (openedExam != null) {
        CourseExamRunner(
            subjectName = subjectName,
            exam = openedExam,
            onBack = { openedExamId = null }
        )
        return
    }

    if (inSimulator) {
        CourseSimulatorScreen(
            subjectName = subjectName,
            onBack = { inSimulator = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ExamBackLink(text = "رجوع إلى الدروس", onClick = onBack)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "امتحانات المادة", fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$subjectName · ${exams.size} امتحانًا",
            fontSize = 13.sp,
            color = ExamMutedColor
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (showLockMessage) {
            val totalLessons = CourseRepository.getLessons(context, subjectName).size
            val passedLessons = CourseProgressStore.countPassedLessons(context, subjectName)
            val neededLessons = passedLessons + remaining

            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔒 لم تبلغ المستوى المطلوب بعد",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = ExamWarnColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "أنجزتَ $passedLessons درسًا من $totalLessons في هذه المادة.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تُفتح امتحانات المادة والمحاكي بعد إنجاز $neededLessons درسًا.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تبقّى لك $remaining درسًا.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = ExamWarnColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "والدرس يُحسب منجَزًا إذا نلتَ في اختباره عشرة من عشرين فما فوق.",
                        fontSize = 12.sp,
                        color = ExamMutedColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showLockMessage = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "حسنًا", fontSize = 14.sp)
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clickable {
                    if (unlocked) {
                        inSimulator = true
                    } else {
                        showLockMessage = true
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text(
                    text = if (unlocked) {
                        "🎯 المحاكي (نمط اللجنة الوطنية)"
                    } else {
                        "🔒 المحاكي (نمط اللجنة الوطنية)"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = ExamWarnColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "خمسة خيارات · مؤقّت نازل · تسليم نهائي · تصحيح بالشروح",
                    fontSize = 12.sp,
                    color = ExamMutedColor
                )
            }
        }

        for (exam in exams) {
            val score = CourseProgressStore.getExamScore(context, exam.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable {
                        if (unlocked) {
                            openedExamId = exam.id
                        } else {
                            showLockMessage = true
                        }
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                        text = if (unlocked) {
                            "امتحان رقم ${exam.n}"
                        } else {
                            "🔒 امتحان رقم ${exam.n}"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (exam.meta.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = exam.meta, fontSize = 12.sp, color = ExamMutedColor)
                    }
                    if (score != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "أفضل نقطة لك: $score/20",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (score >= 10) ExamOkColor else ExamWarnColor
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CourseExamRunner(
    subjectName: String,
    exam: CourseExam,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val total = exam.qs.size

    val testPosId = "exam_" + exam.id
    val restoredPos = remember(exam.id) {
        com.example.data.course.CourseAttemptStore.loadTestPosition(context, testPosId)
            ?.takeIf { it.first in 0 until total && it.second in 0..it.first }
    }

    var attempt by remember(exam.id) { mutableStateOf(0) }
    var started by remember(exam.id, attempt) { mutableStateOf(attempt == 0 && restoredPos != null) }
    var index by remember(exam.id, attempt) { mutableStateOf(if (attempt == 0) (restoredPos?.first ?: 0) else 0) }
    var correctCount by remember(exam.id, attempt) { mutableStateOf(if (attempt == 0) (restoredPos?.second ?: 0) else 0) }
    var selected by remember(exam.id, attempt) { mutableStateOf<Int?>(null) }
    var finished by remember(exam.id, attempt) { mutableStateOf(false) }

    LaunchedEffect(exam.id, attempt, index, started) {
        if (started && !finished) {
            com.example.data.course.CourseAttemptStore.saveTestPosition(context, testPosId, index, correctCount)
        }
    }
    LaunchedEffect(exam.id, attempt, finished) {
        if (finished) {
            com.example.data.course.CourseAttemptStore.clearTestPosition(context, testPosId)
        }
    }

    val finalScore = examScoreOutOf20(correctCount, total)

    LaunchedEffect(exam.id, attempt, finished) {
        if (finished) {
            CourseProgressStore.saveExamScore(context, exam.id, finalScore)
        }
    }

    if (!started) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ExamBackLink(text = "رجوع إلى الامتحانات", onClick = onBack)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "امتحان رقم ${exam.n}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subjectName, fontSize = 13.sp, color = ExamMutedColor)
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

            if (exam.groups.isNotEmpty()) {
                Text(
                    text = "توزيع الأسئلة",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = ExamOkColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        for (group in exam.groups) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = group.t, fontSize = 13.sp)
                                Text(
                                    text = "${group.n}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { started = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "بدء الامتحان ($total سؤالًا)", fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        return
    }

    if (finished) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "نتيجة الامتحان", fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "امتحان رقم ${exam.n} · $subjectName",
                fontSize = 13.sp,
                color = ExamMutedColor
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
                        color = if (finalScore >= 10) ExamOkColor else ExamBadColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "إجابات صحيحة: $correctCount من $total",
                        fontSize = 14.sp,
                        color = ExamMutedColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (finalScore >= 10) "ناجح ✅" else "لم تبلغ النجاح (النجاح بعشر)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = EXAM_MSG,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ExamOkColor
            )
            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { attempt++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "إعادة الامتحان", fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "رجوع إلى الامتحانات", fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        return
    }

    val question = exam.qs[index]
    val chosen = selected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ExamBackLink(text = "الخروج من الامتحان", onClick = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "السؤال ${index + 1} من $total",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = ExamMutedColor
        )
        if (!question.g.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = question.g ?: "", fontSize = 12.sp, color = ExamMutedColor)
        }
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
                isCorrectOption -> ExamOkColor
                isChosen -> ExamBadColor
                else -> Color(0xFFBDBDBD)
            }
            val letter = if (optionIndex < EXAM_LETTERS.size) {
                EXAM_LETTERS[optionIndex]
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
                Text(text = question.c[optionIndex], fontSize = 14.sp)
            }
        }

        if (chosen != null) {
            Spacer(modifier = Modifier.height(12.dp))
            val isRight = chosen == question.k
            Text(
                text = if (isRight) "إجابة صحيحة ✅" else "إجابة خاطئة ❌",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = if (isRight) ExamOkColor else ExamBadColor
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
                    color = ExamOkColor
                )
            }
            if (question.e.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = question.e,
                        fontSize = 13.sp,
                        color = ExamMutedColor,
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
