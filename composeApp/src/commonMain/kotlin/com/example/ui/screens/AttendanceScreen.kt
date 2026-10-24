package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import com.example.ui.utils.uiT
import com.example.ui.utils.uiTf

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.models.AttendanceRecord
import com.example.data.models.ClassSection
import com.example.data.models.Student
import com.example.ui.TeacherViewModel
import com.example.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate

private val AttGreen = Color(0xFF2E9E4F)
private val AttRed = Color(0xFFD32F2F)

private val ATT_DAYS = listOf(
    "MON" to "الاثنين", "TUE" to "الثلاثاء", "WED" to "الأربعاء", "THU" to "الخميس", "FRI" to "الجمعة"
)

private fun dayKey(d: LocalDate): String = when (d.dayOfWeek) {
    DayOfWeek.MONDAY -> "MON"
    DayOfWeek.TUESDAY -> "TUE"
    DayOfWeek.WEDNESDAY -> "WED"
    DayOfWeek.THURSDAY -> "THU"
    DayOfWeek.FRIDAY -> "FRI"
    DayOfWeek.SATURDAY -> "SAT"
    else -> "SUN"
}

private fun dayNameAr(d: LocalDate): String = when (d.dayOfWeek) {
    DayOfWeek.MONDAY -> "الاثنين"
    DayOfWeek.TUESDAY -> "الثلاثاء"
    DayOfWeek.WEDNESDAY -> "الأربعاء"
    DayOfWeek.THURSDAY -> "الخميس"
    DayOfWeek.FRIDAY -> "الجمعة"
    DayOfWeek.SATURDAY -> "السبت"
    else -> "الأحد"
}

private val MONTH_NAMES = mapOf(
    1 to "يناير", 2 to "فبراير", 3 to "مارس", 4 to "أبريل", 5 to "ماي", 6 to "يونيو",
    7 to "يوليو", 8 to "أغسطس", 9 to "سبتمبر", 10 to "أكتوبر", 11 to "نوفمبر", 12 to "ديسمبر"
)

private fun parseDate(s: String): LocalDate? = try { LocalDate.parse(s) } catch (e: Exception) { null }

/** زر التبديل: أخضر مفتوح = حاضر، وعند تحريكه لليمين يصبح أحمر = غائب */
@Composable
private fun PresenceToggle(absent: Boolean, onChange: (Boolean) -> Unit) {
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (absent) AttRed else AttGreen)
                .clickable { onChange(!absent) }
                .padding(3.dp),
            contentAlignment = if (absent) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(Color.White))
        }
    }
}

@Composable
fun AttendanceScreen(
    viewModel: TeacherViewModel,
    section: ClassSection,
    students: List<Student>,
    onClose: () -> Unit
) {
    val allAttendance by viewModel.attendance.collectAsState()
    val teachingDays by viewModel.teachingDays.collectAsState()
    val classRecords = remember(allAttendance, section.id) { allAttendance.filter { it.classId == section.id } }
    val dates = remember(classRecords) { classRecords.map { it.date }.distinct().sortedDescending() }

    // null = القائمة ، غير ذلك = تاريخ السجل قيد التحرير
    var editingDate by remember { mutableStateOf<String?>(null) }
    var monthView by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showDaysDialog by remember { mutableStateOf(false) }
    var dateToDelete by remember { mutableStateOf<String?>(null) }
    val closedMonthsAll by viewModel.closedAttendanceMonths.collectAsState()
    var withCover by remember { mutableStateOf(false) }
    var withFrench by remember { mutableStateOf(false) }
    var coverJob by remember { mutableStateOf<((String?) -> Unit)?>(null) }

    BackHandler {
        when {
            editingDate != null -> editingDate = null
            monthView != null -> monthView = null
            else -> onClose()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = BentoBg) {
        when {
            editingDate != null -> AttendanceEditor(
                viewModel = viewModel,
                section = section,
                students = students,
                initialDate = editingDate!!,
                existing = classRecords,
                teachingDays = teachingDays,
                onDone = { editingDate = null }
            )
            monthView != null -> AttendanceMonthPage(
                viewModel = viewModel,
                section = section,
                students = students,
                records = classRecords,
                teachingDays = teachingDays,
                year = monthView!!.first,
                month = monthView!!.second,
                onBack = { monthView = null }
            )
            else -> Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
                    Text(uiTf("دفتر الغياب — {0}", section.name), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { editingDate = LocalDate.now().toString() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) { Text(uiT("＋ إنشاء غياب جديد")) }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { showDaysDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(uiT("أيام التدريس"))
                }
                Spacer(Modifier.height(12.dp))

                Text(uiT("صفحات الأشهر"), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                val today = LocalDate.now()
                val startYear = if (today.monthValue >= 7) today.year else today.year - 1
                val months = listOf(10, 11, 12, 1, 2, 3, 4, 5, 6).map { m ->
                    m to (if (m >= 7) startYear else startYear + 1)
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    months.forEach { (m, y) ->
                        AssistChip(onClick = { monthView = y to m }, label = { Text(MONTH_NAMES[m] ?: "") })
                    }
                }
                Spacer(Modifier.height(8.dp))
                val allCtx = LocalContext.current
                val runExport: (List<Student>, List<com.example.data.models.AttendanceRecord>, String) -> Unit = { st, rc, fname ->
                    val build: (String?) -> Unit = { cover ->
                        // كل أشهر السنة في ملف واحد، حتى لو لم يُسجَّل أي نداء (لتُملأ يدوياً)
                        val parts = months.map { (m, y) ->
                            val first = LocalDate.of(y, m, 1)
                            val monthDays = (1..first.lengthOfMonth()).map { first.withDayOfMonth(it) }
                                .filter { dayKey(it) in teachingDays }
                            com.example.ui.HtmlReportHelper.generateAttendanceMonthHtml(
                                allCtx, section, st, rc, monthDays, "${MONTH_NAMES[m]} $y", viewModel,
                                viewModel.attendanceMonthKey(section.id, y, m) in closedMonthsAll, withFrench
                            )
                        }
                        var html = com.example.ui.HtmlReportHelper.combineHtmlPages(parts)
                        if (cover != null) html = com.example.ui.CoverDesign.insert(html, cover)
                        com.example.ui.HtmlReportHelper.shareHtmlAsPdf(allCtx, html, fname, isLandscape = true)
                    }
                    if (withCover) coverJob = build else build(null)
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
                OutlinedButton(
                    onClick = {
                        runExport(students, classRecords, "سجل_النداء_كل_الأشهر_${section.name.replace(" ", "_")}.pdf")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(uiT("📥 سحب جميع الأشهر (PDF)")) }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        val blank = (1..60).map { Student(id = -it.toLong(), name = "", classId = section.id) }
                        runExport(blank, emptyList(), "سجل_النداء_فارغ_${section.name.replace(" ", "_")}.pdf")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(uiT("📥 سحب دفتر فارغ (60 سطراً)")) }
                Spacer(Modifier.height(14.dp))

                Text(uiTf("سجلات الغياب ({0})", dates.size), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                if (dates.isEmpty()) {
                    Text(uiT("لا يوجد أي سجل غياب بعد."), color = BentoPrimaryDesc)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dates) { d ->
                        val recs = classRecords.filter { it.date == d }
                        val m = recs.count { it.morningAbsent }
                        val a = recs.count { it.afternoonAbsent }
                        val ld = parseDate(d)
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { editingDate = d },
                            colors = CardDefaults.cardColors(containerColor = BentoLightLavender)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(d, fontWeight = FontWeight.Bold)
                                    Text(
                                        (if (ld != null) dayNameAr(ld) + " — " else "") + "غياب الصباح: $m  |  غياب المساء: $a",
                                        fontSize = 12.sp, color = BentoPrimaryDesc
                                    )
                                }
                                IconButton(onClick = { dateToDelete = d }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AttRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    coverJob?.let { job ->
        CoverDialog(
            viewModel = viewModel,
            section = section,
            bookTitle = "سجل النداء",
            onDismiss = { coverJob = null },
            onResult = { c -> coverJob = null; job(c) }
        )
    }
    if (showDaysDialog) {
        var selected by remember { mutableStateOf(teachingDays) }
        AlertDialog(
            onDismissRequest = { showDaysDialog = false },
            title = { Text(uiT("الأيام التي تدرّسها")) },
            text = {
                Column {
                    Text(uiT("السبت والأحد مستثنيان دائماً. الأيام غير المحددة لا تظهر في سجلات الغياب."), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    ATT_DAYS.forEach { (k, name) ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                selected = if (k in selected) selected - k else selected + k
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = k in selected, onCheckedChange = {
                                selected = if (k in selected) selected - k else selected + k
                            })
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveTeachingDays(selected)
                    showDaysDialog = false
                }) { Text(uiT("حفظ")) }
            },
            dismissButton = { TextButton(onClick = { showDaysDialog = false }) { Text(uiT("إلغاء")) } }
        )
    }

    dateToDelete?.let { d ->
        AlertDialog(
            onDismissRequest = { dateToDelete = null },
            title = { Text(uiT("حذف سجل الغياب")) },
            text = { Text(uiTf("هل تريد حذف سجل غياب يوم {0}؟", d)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAttendanceDay(section.id, d)
                    dateToDelete = null
                }) { Text(uiT("حذف"), color = AttRed) }
            },
            dismissButton = { TextButton(onClick = { dateToDelete = null }) { Text(uiT("إلغاء")) } }
        )
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun AttendanceEditor(
    viewModel: TeacherViewModel,
    section: ClassSection,
    students: List<Student>,
    initialDate: String,
    existing: List<AttendanceRecord>,
    teachingDays: Set<String>,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(initialDate) }
    // studentId -> (صباحاً غائب, مساءً غائب)
    val state = remember { mutableStateMapOf<Long, Pair<Boolean, Boolean>>() }
    var summary by remember { mutableStateOf<List<Pair<Student, Pair<Boolean, Boolean>>>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(date) {
        state.clear()
        students.forEach { s ->
            val r = existing.firstOrNull { it.studentId == s.id && it.date == date }
            state[s.id] = (r?.morningAbsent ?: false) to (r?.afternoonAbsent ?: false)
        }
        error = null
    }

    val ld = parseDate(date)

    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        val d0 = ld ?: LocalDate.now()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = d0.toEpochDay() * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        date = LocalDate.ofEpochDay(ms / 86_400_000L).toString()
                    }
                    showPicker = false
                }) { Text(uiT("موافق")) }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text(uiT("إلغاء")) } }
        ) { DatePicker(state = pickerState) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
            Text(uiT("سجل غياب"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                showPicker = true
            },
            colors = CardDefaults.cardColors(containerColor = BentoLightLavender)
        ) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uiT("التاريخ (اضغط لتغييره)"), fontSize = 12.sp, color = BentoPrimaryDesc)
                Text(date, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (ld != null) Text(dayNameAr(ld), fontSize = 13.sp)
            }
        }
        error?.let { Text(it, color = AttRed, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text(uiT("التلميذ"), Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(uiT("صباحاً"), Modifier.width(60.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Text(uiT("مساءً"), Modifier.width(60.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Text(uiT("أخضر = حاضر ، حرّك الزر لليمين ليصبح أحمر = غائب"), fontSize = 11.sp, color = BentoPrimaryDesc)
        Spacer(Modifier.height(4.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(students, key = { it.id }) { s ->
                val p = state[s.id] ?: (false to false)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White).padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.name, Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                    Box(Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                        PresenceToggle(p.first) { state[s.id] = it to p.second }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                        PresenceToggle(p.second) { state[s.id] = p.first to it }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val d = parseDate(date)
                val key = d?.let { dayKey(it) }
                when {
                    d == null -> error = "التاريخ غير صالح."
                    key == "SAT" || key == "SUN" -> error = "السبت والأحد لا يدخلان في سجل الغياب."
                    key !in teachingDays -> error = "هذا اليوم (${dayNameAr(d)}) ليس من أيام التدريس التي حددتها."
                    else -> {
                        viewModel.saveAttendanceDay(section.id, date, state.toMap())
                        summary = students.filter { (state[it.id]?.first ?: false) || (state[it.id]?.second ?: false) }
                            .map { it to (state[it.id] ?: (false to false)) }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
        ) { Text(uiT("حفظ السجل")) }
    }

    summary?.let { list ->
        AlertDialog(
            onDismissRequest = { summary = null; onDone() },
            title = { Text(uiTf("معلومات الغياب — {0}", date)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(uiTf("عدد التلاميذ: {0}", students.size))
                    Text("غياب الصباح: ${list.count { it.second.first }}")
                    Text("غياب المساء: ${list.count { it.second.second }}")
                    Spacer(Modifier.height(8.dp))
                    if (list.isEmpty()) {
                        Text(uiT("لا غياب اليوم 🎉"), color = AttGreen, fontWeight = FontWeight.Bold)
                    } else {
                        list.forEach { (s, p) ->
                            val where = when {
                                p.first && p.second -> "صباحاً ومساءً"
                                p.first -> "صباحاً"
                                else -> "مساءً"
                            }
                            Text("• ${s.name} — $where", fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { summary = null; onDone() }) { Text(uiT("حسناً")) } }
        )
    }
}

@Composable
private fun AttendanceMonthPage(
    viewModel: TeacherViewModel,
    section: ClassSection,
    students: List<Student>,
    records: List<AttendanceRecord>,
    teachingDays: Set<String>,
    year: Int,
    month: Int,
    onBack: () -> Unit
) {
    var monthFrench by remember { mutableStateOf(false) }
    val closedMonths by viewModel.closedAttendanceMonths.collectAsState()
    val monthClosed = viewModel.attendanceMonthKey(section.id, year, month) in closedMonths
    val first = LocalDate.of(year, month, 1)
    val days = remember(year, month, teachingDays) {
        (1..first.lengthOfMonth()).map { first.withDayOfMonth(it) }.filter { dayKey(it) in teachingDays }
    }
    val byKey = remember(records) { records.associateBy { it.studentId to it.date } }
    val cell = 30.dp
    val rowH = 34.dp
    val nameW = 130.dp

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
            Text(uiTf("غياب شهر {0} {1}", MONTH_NAMES[month], year), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            val ctx = LocalContext.current
            Button(
                onClick = {
                    val label = "${MONTH_NAMES[month]} $year"
                    val html = com.example.ui.HtmlReportHelper.generateAttendanceMonthHtml(
                        ctx, section, students, records, days, label, viewModel, monthClosed, monthFrench
                    )
                    com.example.ui.HtmlReportHelper.shareHtmlAsPdf(ctx, html, "سجل_النداء_${MONTH_NAMES[month]}_$year.pdf", isLandscape = true)
                },
                enabled = days.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) { Text(uiT("مشاركة PDF")) }
        }
        if (days.isEmpty()) {
            Text(uiT("لا توجد أيام تدريس في هذا الشهر. غيّر «أيام التدريس»."), color = BentoPrimaryDesc)
            return@Column
        }
        OutlinedButton(
            onClick = { viewModel.setAttendanceMonthClosed(section.id, year, month, !monthClosed) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(uiT(if (monthClosed) "✔ الشهر منتهٍ — اضغط لإعادة فتحه" else "تسجيل انتهاء الشهر (لتظهر الإحصائيات)"))
        }
        Row(
            Modifier.fillMaxWidth().clickable { monthFrench = !monthFrench },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = monthFrench, onCheckedChange = { monthFrench = it })
            Text(uiT("سحب الاسم بالفرنسية مع العربية (سطر ثانٍ تحت الاسم العربي)"), fontSize = 13.sp)
        }
        Text(uiT("ص = الحصة الصباحية ، م = الحصة المسائية ، ✗ = غائب"), fontSize = 11.sp, color = BentoPrimaryDesc)
        Spacer(Modifier.height(6.dp))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row {
                // عمود الأسماء
                Column {
                    Box(Modifier.width(nameW).height(rowH * 2).border(0.5.dp, Color.Gray).background(BentoSoftPurpleContainer), contentAlignment = Alignment.Center) {
                        Text(uiT("التلميذ"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    students.forEach { s ->
                        Box(Modifier.width(nameW).height(rowH).border(0.5.dp, Color.Gray).padding(horizontal = 4.dp), contentAlignment = Alignment.CenterStart) {
                            Text(s.name, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    days.forEach { d ->
                        val ds = d.toString()
                        Column {
                            Box(Modifier.width(cell * 2).height(rowH).border(0.5.dp, Color.Gray).background(BentoSoftPurpleContainer), contentAlignment = Alignment.Center) {
                                Text("${d.dayOfMonth}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row {
                                listOf("ص", "م").forEach { t ->
                                    Box(Modifier.width(cell).height(rowH).border(0.5.dp, Color.Gray).background(BentoSoftPurpleContainer), contentAlignment = Alignment.Center) {
                                        Text(t, fontSize = 11.sp)
                                    }
                                }
                            }
                            students.forEach { s ->
                                val r = byKey[s.id to ds]
                                Row {
                                    listOf(r?.morningAbsent, r?.afternoonAbsent).forEach { abs ->
                                        Box(Modifier.width(cell).height(rowH).border(0.5.dp, Color.Gray), contentAlignment = Alignment.Center) {
                                            if (abs == true) Text("✗", color = AttRed, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // إحصائيات الشهر: الحضور المتوقع / غياب الشهر / الحضور الفعلي (حسب الأيام المسجلة)
                    // المتوقع = الأيام التي أُنشئ لها سجل فقط × 2
                    val recordedDates = records.map { it.date }.toSet()
                    val daysCounted = days.filter { it.toString() in recordedDates }
                    val expected = daysCounted.size * 2
                    val statW = 76.dp
                    val stats = listOf<Pair<String, (Student) -> Int>>(
                        "الحضور المتوقع" to { _ -> expected },
                        "غياب الشهر" to { st ->
                            daysCounted.sumOf { d ->
                                val r = byKey[st.id to d.toString()]
                                (if (r?.morningAbsent == true) 1 else 0) + (if (r?.afternoonAbsent == true) 1 else 0)
                            }
                        },
                        "الحضور الفعلي" to { st ->
                            expected - daysCounted.sumOf { d ->
                                val r = byKey[st.id to d.toString()]
                                (if (r?.morningAbsent == true) 1 else 0) + (if (r?.afternoonAbsent == true) 1 else 0)
                            }
                        }
                    )
                    stats.forEach { (label, calc) ->
                        Column {
                            Box(Modifier.width(statW).height(rowH * 2).border(0.5.dp, Color.Gray).background(BentoLightPink), contentAlignment = Alignment.Center) {
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                            students.forEach { st ->
                                Box(Modifier.width(statW).height(rowH).border(0.5.dp, Color.Gray), contentAlignment = Alignment.Center) {
                                    if (monthClosed) {
                                        val n = calc(st)
                                        val pct = if (expected > 0) Math.round(n * 100f / expected) else 0
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Text("$n", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Box(Modifier.width(1.dp).height(16.dp).background(Color.Gray))
                                            Text("$pct%", fontSize = 10.sp, color = BentoPrimaryDesc, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
