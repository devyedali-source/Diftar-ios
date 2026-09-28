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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.course.CourseRepository
import com.example.data.course.CourseWriting

private val WrOkColor = Color(0xFF2E7D32)
private val WrMutedColor = Color(0xFF6B6B6B)
private val WrWarnColor = Color(0xFFE65100)
private val WrIdleColor = Color(0xFFBDBDBD)

private const val WR_MSG =
    "واصل الدورة حتى تزيد مستوى نقاطك لتحظى بفرصة أكبر بين المتسابقين"

private fun wrCriterionText(row: List<Any>): String {
    if (row.isEmpty()) return ""
    return row[0].toString()
}

private fun wrCriterionPoints(row: List<Any>): Int {
    if (row.size < 2) return 0
    val value = row[1]
    return if (value is Number) value.toInt() else 0
}

@Composable
private fun WrBackLink(text: String, onClick: () -> Unit) {
    Text(
        text = "› $text",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = WrMutedColor,
        modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp)
    )
}

@Composable
fun CourseWritingsScreen(subjectName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var openedId by remember(subjectName) { mutableStateOf<String?>(null) }

    val writings = CourseRepository.getWritings(context, subjectName)
    val opened = writings.firstOrNull { it.id == openedId }

    if (opened != null) {
        CourseWritingDetail(
            subjectName = subjectName,
            writing = opened,
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
        WrBackLink(text = "رجوع إلى الدروس", onClick = onBack)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "الإنتاج الكتابي", fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$subjectName · ${writings.size} موضوعًا",
            fontSize = 13.sp,
            color = WrMutedColor
        )
        Spacer(modifier = Modifier.height(14.dp))

        for (writing in writings) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { openedId = writing.id }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                        text = "${writing.n}. ${writing.t}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${writing.g} · ${writing.m} د · ${writing.p} نقاط · ${writing.mw} كلمة على الأقل",
                        fontSize = 12.sp,
                        color = WrMutedColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CourseWritingDetail(
    subjectName: String,
    writing: CourseWriting,
    onBack: () -> Unit
) {
    var showModel by remember(writing.id) { mutableStateOf(false) }
    val checked = remember(writing.id) { mutableStateMapOf<Int, Boolean>() }

    var earned = 0
    for (i in writing.cr.indices) {
        if (checked[i] == true) earned += wrCriterionPoints(writing.cr[i])
    }
    val totalPoints = writing.cr.sumOf { wrCriterionPoints(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        WrBackLink(text = "رجوع إلى المواضيع", onClick = onBack)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${writing.n}. ${writing.t}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${writing.g} · $subjectName",
            fontSize = 13.sp,
            color = WrMutedColor
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "المدّة: ${writing.m} د", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = "النقاط: ${writing.p}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = "≥ ${writing.mw} كلمة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "نصّ الموضوع",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = WrOkColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(text = writing.q, fontSize = 15.sp, modifier = Modifier.padding(14.dp))
        }

        if (writing.cr.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "معايير التصحيح",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = WrOkColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "اكتب موضوعك على ورقة، ثم ضع علامتك بنفسك على كل معيار تحقّق فيه.",
                fontSize = 12.sp,
                color = WrMutedColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            for (i in writing.cr.indices) {
                val isChecked = checked[i] == true
                val points = wrCriterionPoints(writing.cr[i])
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            1.dp,
                            if (isChecked) WrOkColor else WrIdleColor,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { checked[i] = !isChecked }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isChecked) "☑ " else "☐ ",
                            fontSize = 16.sp,
                            color = if (isChecked) WrOkColor else WrIdleColor
                        )
                        Text(text = wrCriterionText(writing.cr[i]), fontSize = 14.sp)
                    }
                    Text(
                        text = "$points",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isChecked) WrOkColor else WrMutedColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تقديرك الذاتي: $earned من $totalPoints",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (earned * 2 >= totalPoints) WrOkColor else WrWarnColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = WR_MSG,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WrOkColor
                    )
                }
            }
        }

        if (writing.md.isNotBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
            if (!showModel) {
                OutlinedButton(
                    onClick = { showModel = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "عرض النموذج المقترح", fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "لا تفتحه إلا بعد أن تكتب موضوعك بنفسك.",
                    fontSize = 12.sp,
                    color = WrWarnColor
                )
            } else {
                Text(
                    text = "النموذج المقترح",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = WrOkColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(text = writing.md, fontSize = 14.sp, modifier = Modifier.padding(14.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showModel = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "إخفاء النموذج", fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
