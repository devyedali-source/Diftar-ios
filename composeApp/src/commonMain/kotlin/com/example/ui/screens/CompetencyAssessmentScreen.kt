package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import com.example.ui.utils.uiT
import com.example.ui.utils.uiTf

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ClassSection
import com.example.data.models.Student
import com.example.data.planning.LessonPreparationRepository
import com.example.ui.TeacherViewModel
import com.example.ui.theme.*

private val CmpGreen = Color(0xFF2E9E4F)
private val CmpRed = Color(0xFFD32F2F)
private val CmpLine = Color(0xFFB0B0C0)

/** صفحة تقييم الكفايات: دفتر متابعة القسم + جدول دعم المجموعات (قابلة للمشاركة PDF) */
@Composable
fun CompetencyAssessmentScreen(
    viewModel: TeacherViewModel,
    section: ClassSection,
    students: List<Student>,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current
    BackHandler { onClose() }

    val catalog = remember(section.level) {
        try { LessonPreparationRepository.getCompetencyCatalog(ctx, section.level) } catch (e: Exception) { emptyList() }
    }
    val all by viewModel.competencies.collectAsState()
    val sorted = remember(students) { students.sortedBy { it.id } }

    // بدون تلاميذ: يُسحب نموذج فارغ بـ 60 سطراً ليُكتب بالقلم بعد الطباعة
    val exportStudents = remember(students, section.id) {
        if (students.isEmpty()) (1..60).map { Student(id = -it.toLong(), name = "", classId = section.id) } else students
    }

    var discIdx by remember { mutableStateOf(0) }
    var chunkIdx by remember { mutableStateOf(0) }
    var tab by remember { mutableStateOf(0) } // 0 = دفتر المتابعة ، 1 = جدول الدعم
    var withCover by remember { mutableStateOf(false) }
    var withFrench by remember { mutableStateOf(false) }
    var coverJob by remember { mutableStateOf<((String?) -> Unit)?>(null) }
    // يسحب الدفتر، وإن فُعّل الغلاف تُفتح واجهة الغلاف أولاً ثم يُدرج في أول الملف
    val runExport: (String, () -> String) -> Unit = { fname, makeHtml ->
        val build: (String?) -> Unit = { cover ->
            var html = makeHtml()
            if (cover != null) html = com.example.ui.CoverDesign.insert(html, cover)
            com.example.ui.HtmlReportHelper.shareHtmlAsPdf(ctx, html, fname, isLandscape = true)
        }
        if (withCover) coverJob = build else build(null)
    }

    val classEntries = remember(all, section.id) { all.filter { it.classId == section.id } }
    val marks = remember(classEntries) {
        classEntries.filter { it.studentId > 0 }.associate { "${it.studentId}|${it.entryKey}" to it.value }
    }
    val notes = remember(classEntries) {
        classEntries.filter { it.studentId == 0L && it.note.isNotBlank() }.associate { it.entryKey to it.note }
    }

    coverJob?.let { job ->
        CoverDialog(
            viewModel = viewModel,
            section = section,
            bookTitle = "دفتر المتابعة",
            onDismiss = { coverJob = null },
            onResult = { c -> coverJob = null; job(c) }
        )
    }
    Surface(modifier = Modifier.fillMaxSize(), color = BentoBg) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
                Text(
                    uiTf("دفتر المتابعة — {0}", section.name),
                    fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // أزرار السحب في قائمة واحدة حتى لا تضيق الشاشة على الهاتف
            val disc0 = catalog.getOrNull(discIdx)
            var exportMenu by remember { mutableStateOf(false) }
            Box(Modifier.fillMaxWidth()) {
                Button(
                    onClick = { exportMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) { Text(uiT("سحب لوائح المتابعة (PDF) ▾"), maxLines = 1) }
                DropdownMenu(expanded = exportMenu, onDismissRequest = { exportMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(uiT("سحب لائحة المادة")) },
                        enabled = disc0 != null,
                        onClick = {
                            exportMenu = false
                            if (disc0 != null) {
                                runExport("دفتر_المتابعة_${disc0.label}.pdf") {
                                    com.example.ui.HtmlReportHelper.generateCompetencyHtml(
                                        ctx, section, exportStudents, disc0, marks, notes, viewModel, withFrench
                                    )
                                }
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(uiT("سحب لوائح كل المواد")) },
                        enabled = catalog.isNotEmpty(),
                        onClick = {
                            exportMenu = false
                            runExport("دفتر_المتابعة_جميع_المواد_${section.name}.pdf") {
                                com.example.ui.HtmlReportHelper.generateCompetencyAllHtml(
                                    ctx, section, exportStudents, catalog, marks, notes, viewModel, withFrench
                                )
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(uiT("سحب لوائح فارغة لكل المواد (60 سطراً)")) },
                        enabled = catalog.isNotEmpty(),
                        onClick = {
                            exportMenu = false
                            val blank = (1..60).map { Student(id = -it.toLong(), name = "", classId = section.id) }
                            runExport("دفتر_المتابعة_فارغ_جميع_المواد.pdf") {
                                com.example.ui.HtmlReportHelper.generateCompetencyAllHtml(
                                    ctx, section, blank, catalog, emptyMap(), emptyMap(), viewModel, withFrench
                                )
                            }
                        }
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().clickable { withCover = !withCover },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = withCover, onCheckedChange = { withCover = it })
                Text(uiT("إضافة غلاف (صفحة أولى) في أول الدفتر"), fontSize = 14.sp)
            }
            Row(
                Modifier.fillMaxWidth().clickable { withFrench = !withFrench },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = withFrench, onCheckedChange = { withFrench = it })
                Text(uiT("سحب الاسم بالفرنسية مع العربية (سطر ثانٍ تحت الاسم العربي)"), fontSize = 14.sp)
            }

            if (catalog.isEmpty()) {
                Text(uiT("لا توجد كفايات متاحة لهذا المستوى."), color = BentoPrimaryDesc, modifier = Modifier.padding(16.dp))
                return@Column
            }
            if (students.isEmpty()) {
                Text(
                    uiT("لا يوجد تلاميذ في القسم. يمكنك سحب نموذج فارغ بـ 60 سطراً (PDF) وتعبئته يدوياً، أو أضف التلاميذ ليظهروا في الجدول."),
                    color = BentoPrimaryDesc, fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            val disc = catalog[discIdx.coerceIn(0, catalog.lastIndex)]
            val chunks = disc.spheres.chunked(3)

            Text(uiT("المادة"), fontSize = 12.sp, color = BentoPrimaryDesc)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                catalog.forEachIndexed { i, d ->
                    FilterChip(selected = i == discIdx, onClick = { discIdx = i; chunkIdx = 0 }, label = { Text(d.label) })
                }
            }
            if (chunks.size > 1) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    chunks.forEachIndexed { i, c ->
                        FilterChip(
                            selected = i == chunkIdx.coerceIn(0, chunks.lastIndex),
                            onClick = { chunkIdx = i },
                            label = { Text(c.joinToString(" · ") { it.label }, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            }
            val chunk = chunks.getOrNull(chunkIdx.coerceIn(0, (chunks.size - 1).coerceAtLeast(0))).orEmpty()

            Spacer(Modifier.height(4.dp))
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(if (com.example.ui.utils.AppLocalization.lang == "fr") "Évaluation CB" else "تقييم الكفايات", maxLines = 1) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(uiT("جدول الدعم"), maxLines = 1) })
            }
            Spacer(Modifier.height(6.dp))

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (tab == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text(uiT("لائحة الكفايات"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            chunk.forEach { sp ->
                                sp.cbs.forEach { (k, t) ->
                                    Text(uiTf("{0} — الكفاية {1}: {2}", sp.label, k, t), fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(uiT("اضغط على الخانة: + (امتلك) ← − (لم يمتلك) ← فارغة"), fontSize = 11.sp, color = BentoPrimaryDesc)
                    Spacer(Modifier.height(4.dp))
                    Column(Modifier.horizontalScroll(rememberScrollState())) {
                        // رأس المجالات
                        Row {
                            CmpCell("الرقم", 36.dp, bold = true)
                            CmpCell("اسم التلميذ", 130.dp, bold = true)
                            chunk.forEach { sp -> CmpCell(sp.label, 132.dp, bold = true, bg = BentoLightLavender) }
                        }
                        Row {
                            CmpCell("", 36.dp)
                            CmpCell("", 130.dp)
                            chunk.forEach { _ -> for (k in 1..3) CmpCell("ك$k", 44.dp, bold = true) }
                        }
                        sorted.forEachIndexed { i, st ->
                            Row {
                                CmpCell("${i + 1}", 36.dp)
                                CmpCell(st.name, 130.dp, align = TextAlign.Start)
                                chunk.forEach { sp ->
                                    for (k in 1..3) {
                                        val key = "${disc.code}|${sp.code}|$k"
                                        val v = marks["${st.id}|$key"] ?: 0
                                        val has = sp.cbs.containsKey(k)
                                        Box(
                                            Modifier.width(44.dp).height(36.dp)
                                                .border(0.5.dp, CmpLine)
                                                .background(if (has) Color.White else Color(0xFFEDEDED))
                                                .clickable(enabled = has) {
                                                    val next = when (v) { 0 -> 1; 1 -> -1; else -> 0 }
                                                    viewModel.saveCompetencyEntry(section.id, st.id, key, next)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when {
                                                v > 0 -> Text("+", color = CmpGreen, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                                v < 0 -> Text("−", color = CmpRed, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                                !has -> Text("/", color = Color.Gray, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(uiT("المجموعات تُحسب تلقائياً من الخانات المعلّمة بـ (−) وتظهر بأرقام التلاميذ وأسمائهم، مفصولة إلى بنات وأولاد."), fontSize = 11.sp, color = BentoPrimaryDesc)
                    Spacer(Modifier.height(6.dp))
                    for (k in 1..3) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(uiTf("المجموعة التي لم تمتلك الكفاية {0}", k), fontWeight = FontWeight.Bold)
                                chunk.forEach { sp ->
                                    val members = sorted.mapIndexedNotNull { i, st ->
                                        if ((marks["${st.id}|${disc.code}|${sp.code}|$k"] ?: 0) < 0) Triple(i + 1, st.gender, st.name) else null
                                    }
                                    fun lineOf(label: String, list: List<Triple<Int, String, String>>) =
                                        if (list.isEmpty()) "" else "$label: " + list.joinToString(" - ") { "${it.first} ${it.third}" }
                                    val girlsLine = lineOf(uiT("بنات"), members.filter { it.second == "أنثى" })
                                    val boysLine = lineOf(uiT("أولاد"), members.filter { it.second == "ذكر" })
                                    val otherLine = lineOf(uiT("غير محدد"), members.filter { it.second != "أنثى" && it.second != "ذكر" })
                                    Text("${sp.label}:", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                                    if (members.isEmpty()) {
                                        Text("—", fontSize = 13.sp)
                                    } else {
                                        listOf(girlsLine, boysLine, otherLine).filter { it.isNotEmpty() }.forEach {
                                            Text(it, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                                        }
                                    }
                                }
                                val noteKey = "SUP|${disc.code}|$k"
                                var text by remember(noteKey, notes[noteKey]) { mutableStateOf(notes[noteKey].orEmpty()) }
                                OutlinedTextField(
                                    value = text,
                                    onValueChange = {
                                        text = it
                                        viewModel.saveCompetencyEntry(section.id, 0L, noteKey, 0, it)
                                    },
                                    label = { Text(uiT("الدعم المدرسي")) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                )
                                var showSuggestions by remember(noteKey) { mutableStateOf(false) }
                                TextButton(onClick = { showSuggestions = true }) {
                                    Text(uiT("💡 اقتراحات الدعم"))
                                }
                                if (showSuggestions) {
                                    val suggestions = remember(section.level, disc.code, k, chunkIdx) {
                                        try {
                                            LessonPreparationRepository.getRemediationSuggestions(
                                                ctx, section.level, disc.code, chunk.map { it.code }.toSet(), k
                                            )
                                        } catch (e: Exception) { emptyList() }
                                    }
                                    AlertDialog(
                                        onDismissRequest = { showSuggestions = false },
                                        title = { Text(uiTf("اقتراحات الدعم للكفاية {0}", k), fontWeight = FontWeight.Bold) },
                                        text = {
                                            Column {
                                                Text(
                                                    uiT("مقترح تربوي مساعد: أنشطة علاج من أوراق التحضير، ليست نصاً وزارياً حرفياً. اختر ما يناسب أو اكتب اقتراحك."),
                                                    fontSize = 11.sp, color = BentoPrimaryDesc
                                                )
                                                Spacer(Modifier.height(6.dp))
                                                if (suggestions.isEmpty()) {
                                                    Text(uiT("لا توجد اقتراحات لهذه الكفاية. اكتب اقتراحك في الخانة."), fontSize = 13.sp)
                                                } else {
                                                    LazyColumn(Modifier.heightIn(max = 380.dp)) {
                                                        items(suggestions.size) { idx ->
                                                            val sug = suggestions[idx]
                                                            Card(
                                                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable {
                                                                    val merged = if (text.isBlank()) sug else text + "\n" + sug
                                                                    text = merged
                                                                    viewModel.saveCompetencyEntry(section.id, 0L, noteKey, 0, merged)
                                                                    showSuggestions = false
                                                                },
                                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                                                            ) {
                                                                Text(sug, fontSize = 13.sp, modifier = Modifier.padding(8.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { showSuggestions = false }) { Text(uiT("إغلاق")) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CmpCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    bold: Boolean = false,
    bg: Color = Color.White,
    align: TextAlign = TextAlign.Center
) {
    Box(
        Modifier.width(width).height(36.dp).border(0.5.dp, CmpLine).background(bg).padding(horizontal = 3.dp),
        contentAlignment = if (align == TextAlign.Start) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text, fontSize = 11.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            textAlign = align, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
    }
}
