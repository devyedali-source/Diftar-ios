package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.models.ClassSection
import com.example.data.planning.AnnualPlanningRepository
import com.example.data.planning.DayOfWeekAr
import com.example.data.planning.OfficialTimetableRepository
import com.example.data.planning.OfficialTimetableSlot
import com.example.data.planning.TimetablePeriodType
import com.example.ui.TeacherViewModel
import com.example.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun ClassTimetableSubview(
    activeClass: ClassSection,
    viewModel: TeacherViewModel
) {
    val context = LocalContext.current
    val level = activeClass.level

    // Day Detection & Tab State
    val today = LocalDate.now()
    val currentDayOfWeek = today.dayOfWeek
    val isWeekend = currentDayOfWeek == DayOfWeek.SATURDAY || currentDayOfWeek == DayOfWeek.SUNDAY

    val initialDay = remember(currentDayOfWeek) {
        when (currentDayOfWeek) {
            DayOfWeek.MONDAY -> DayOfWeekAr.LUNDI
            DayOfWeek.TUESDAY -> DayOfWeekAr.MARDI
            DayOfWeek.WEDNESDAY -> DayOfWeekAr.MERCREDI
            DayOfWeek.THURSDAY -> DayOfWeekAr.JEUDI
            DayOfWeek.FRIDAY -> DayOfWeekAr.VENDREDI
            else -> DayOfWeekAr.LUNDI // Default to Monday during weekend
        }
    }

    var selectedDay by remember { mutableStateOf(initialDay) }

    // local memory persistence for regional Hijri adjustment
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var hijriOffset by remember { mutableStateOf(prefs.getLong("hijri_offset_days_v2", 0L)) }

    // Calculations of local Date Offline
    val todayG = LocalDate.now()
    val adjustedG = remember(todayG, hijriOffset) { todayG.plusDays(hijriOffset) }

    // Hijri date conversion
    val adjustedHijri = remember(adjustedG) {
        try {
            HijrahDate.from(adjustedG)
        } catch (e: Exception) {
            HijrahDate.now()
        }
    }

    val hijriDay = adjustedHijri.get(ChronoField.DAY_OF_MONTH)
    val hijriMonth = adjustedHijri.get(ChronoField.MONTH_OF_YEAR)
    val hijriYear = adjustedHijri.get(ChronoField.YEAR)

    val hijriMonthNames = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )
    val hijriMonthName = hijriMonthNames.getOrNull(hijriMonth - 1) ?: "$hijriMonth"
    val hijriArabicStr = "$hijriDay $hijriMonthName $hijriYear هـ"

    val gregorianArabicStr = remember(todayG) {
        val gDay = todayG.dayOfMonth
        val gMonth = todayG.monthValue
        val gYear = todayG.year
        val gMonthNames = listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        val gMonthName = gMonthNames.getOrNull(gMonth - 1) ?: "$gMonth"

        val gDayOfWeekName = when (todayG.dayOfWeek) {
            DayOfWeek.MONDAY -> "الإثنين"
            DayOfWeek.TUESDAY -> "الثلاثاء"
            DayOfWeek.WEDNESDAY -> "الأربعاء"
            DayOfWeek.THURSDAY -> "الخميس"
            DayOfWeek.FRIDAY -> "الجمعة"
            DayOfWeek.SATURDAY -> "السبت"
            DayOfWeek.SUNDAY -> "الأحد"
        }
        "$gDayOfWeekName $gDay $gMonthName $gYear مـ"
    }

    var showAdjustDialog by remember { mutableStateOf(false) }

    // Retrieve active level timetable slots
    val daySlots = remember(level, selectedDay) {
        OfficialTimetableRepository.getSlotsForDay(level, selectedDay)
    }

    val periodBeforeRecess = remember(daySlots) {
        daySlots.filter { it.periodType == TimetablePeriodType.MORNING_BEFORE_RECESS }
    }

    val periodAfterRecess = remember(daySlots) {
        daySlots.filter { it.periodType == TimetablePeriodType.MORNING_AFTER_RECESS }
    }

    val periodAfternoon = remember(daySlots) {
        daySlots.filter { it.periodType == TimetablePeriodType.AFTERNOON }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. HEADER CARD (Gregorian & Hijri Dates with Mauritanian adjustment)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, BentoGrayOutline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "الجدول الزمني للقسم",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = BentoPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoLightLavender
                    ) {
                        Text(
                            text = "رسمي",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoPrimary
                        )
                    }
                }

                HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.3f), thickness = 0.8.dp)

                // Date Displays (Gregorian & Hijri with Manual adjustments)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gregorian offline Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = BentoPrimaryDesc,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = gregorianArabicStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDesc
                        )
                    }

                    // Hijri offline Date with adjustment
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BentoLightLavender.copy(alpha = 0.5f))
                            .clickable { showAdjustDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = hijriArabicStr,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل التاريخ الهجري",
                            tint = BentoPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BentoPrimaryDesc.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "تحقق من توافق التاريخ الهجري مع موريتانيا",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryDesc.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 2. DAY SELECTOR CHIPS BAR (الإثنين ← الجمعة)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, BentoGrayOutline.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val days = listOf(
                    DayOfWeekAr.LUNDI,
                    DayOfWeekAr.MARDI,
                    DayOfWeekAr.MERCREDI,
                    DayOfWeekAr.JEUDI,
                    DayOfWeekAr.VENDREDI
                )

                days.forEach { day ->
                    val isSelected = selectedDay == day
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) BentoPrimary else Color.Transparent
                            )
                            .clickable { selectedDay = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.arabicName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.White else BentoPrimary
                        )
                    }
                }
            }
        }

        // Weekend Notice
        if (isWeekend) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = BorderStroke(0.8.dp, Color(0xFFFFB300))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "نهاية الأسبوع: اليوم ليس من أيام الجدول الدراسي المعتمد.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }

        // 3. PERIODS SCROLLABLE CONTENT
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // PERIOD 1: MORNING 08:00 -> 10:00
            item {
                PeriodHeaderCard(
                    title = "الفترة الصباحية الأولى",
                    timeRange = "08:00 ← 10:00",
                    accentColor = Color(0xFF1B5E20)
                )
            }

            if (periodBeforeRecess.isEmpty()) {
                item { EmptyPeriodCard() }
            } else {
                items(periodBeforeRecess, key = { it.id }) { slot ->
                    TimetableSlotCard(slot = slot)
                }
            }

            // RECESS SEPARATOR CARD
            item {
                RecessCard()
            }

            // PERIOD 2: MORNING 10:00 -> 12:00
            item {
                PeriodHeaderCard(
                    title = "الفترة الصباحية الثانية",
                    timeRange = "بعد الاستراحة ← 12:00",
                    accentColor = Color(0xFFE65100)
                )
            }

            if (periodAfterRecess.isEmpty()) {
                item { EmptyPeriodCard() }
            } else {
                items(periodAfterRecess, key = { it.id }) { slot ->
                    TimetableSlotCard(slot = slot)
                }
            }

            // PERIOD 3: AFTERNOON 12:00 -> 14:00
            item {
                PeriodHeaderCard(
                    title = "الفترة المسائية",
                    timeRange = "12:00 ← 14:00",
                    accentColor = Color(0xFF006064)
                )
            }

            if (periodAfternoon.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        border = BorderStroke(0.8.dp, BentoGrayOutline.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "لا توجد حصص مبرمجة في الفترة المسائية",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDesc.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            } else {
                items(periodAfternoon, key = { it.id }) { slot ->
                    TimetableSlotCard(slot = slot)
                }
            }
        }
    }

    // 4. HIJRI ADJUSTMENT CALENDAR PICKER DIALOG
    if (showAdjustDialog) {
        Dialog(onDismissRequest = { showAdjustDialog = false }) {
            var tempDay by remember { mutableStateOf(hijriDay) }
            var tempMonth by remember { mutableStateOf(hijriMonth) }
            var tempYear by remember { mutableStateOf(hijriYear) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                border = BorderStroke(1.5.dp, BentoPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "تعديل التاريخ الهجري محلياً",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = BentoPrimary,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.3f))

                    // 1. Year Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (tempYear > 1440) tempYear-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "السنة السابقة", tint = BentoPrimary)
                        }
                        Text(
                            text = "السنة: $tempYear هـ",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = BentoPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(onClick = { tempYear++ }) {
                            Icon(Icons.Default.Add, contentDescription = "السنة التالية", tint = BentoPrimary)
                        }
                    }

                    // 2. Month selector (Chips row)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "الشهر الهجري المعتمد:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDesc
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            itemsIndexed(hijriMonthNames) { idx, name ->
                                val monthNum = idx + 1
                                val isSelected = tempMonth == monthNum
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) BentoPrimary else Color(0xFFF1F5F9))
                                        .clickable { tempMonth = monthNum }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else BentoPrimary
                                    )
                                }
                            }
                        }
                    }

                    // 3. Day picker (Grid of 30 days)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "يوم الشهر:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDesc
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            for (row in 0..4) { // 5 rows
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    for (col in 1..6) { // 6 columns = 30 days total
                                        val dayNum = row * 6 + col
                                        val isSelected = tempDay == dayNum
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1.2f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) BentoPrimary else Color(0xFFF1F5F9))
                                                .clickable { tempDay = dayNum },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                color = if (isSelected) Color.White else BentoPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Live preview text
                    val selectedMonthName = hijriMonthNames.getOrNull(tempMonth - 1) ?: ""
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = BentoLightLavender.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "التاريخ الهجري المحدد: $tempDay $selectedMonthName $tempYear هـ",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TextButton(
                            onClick = { showAdjustDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc)
                        }

                        TextButton(
                            onClick = {
                                hijriOffset = 0L
                                prefs.edit().putLong("hijri_offset_days_v2", 0L).apply()
                                showAdjustDialog = false
                            },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("إعادة الضبط", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                try {
                                    val selectedHijri = HijrahDate.of(tempYear, tempMonth, tempDay)
                                    val targetGregorian = LocalDate.from(selectedHijri)
                                    val computedOffset = ChronoUnit.DAYS.between(LocalDate.now(), targetGregorian)
                                    hijriOffset = computedOffset
                                    prefs.edit().putLong("hijri_offset_days_v2", computedOffset).apply()
                                } catch (e: Exception) {
                                    try {
                                        // fallback for short month lengths
                                        val selectedHijri = HijrahDate.of(tempYear, tempMonth, 29)
                                        val targetGregorian = LocalDate.from(selectedHijri)
                                        val computedOffset = ChronoUnit.DAYS.between(LocalDate.now(), targetGregorian)
                                        hijriOffset = computedOffset
                                        prefs.edit().putLong("hijri_offset_days_v2", computedOffset).apply()
                                    } catch (e2: Exception) {
                                        // ignore
                                    }
                                }
                                showAdjustDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("حفظ التعديل", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodHeaderCard(
    title: String,
    timeRange: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp, 16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accentColor)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = BentoPrimary
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = accentColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = timeRange,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun EmptyPeriodCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = BorderStroke(0.8.dp, BentoGrayOutline.copy(alpha = 0.3f))
    ) {
        Text(
            text = "لا توجد حصص مبرمجة في هذه الفترة",
            fontSize = 11.sp,
            color = BentoPrimaryDesc.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun RecessCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
        border = BorderStroke(1.dp, Color(0xFF4DB6AC).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            tint = Color(0xFF00695C),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "استراحة مبرمجة بيداغوجياً",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00695C)
                )
            }

            Text(
                text = "15 دقيقة",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00695C)
            )
        }
    }
}

@Composable
private fun TimetableSlotCard(
    slot: OfficialTimetableSlot
) {
    val icon = remember(slot.subject, slot.branch) {
        val sub = slot.subject.lowercase()
        val br = slot.branch.lowercase()
        when {
            sub.contains("قرآن") || sub.contains("عقيدة") || sub.contains("حديث") || sub.contains("عبادات") || sub.contains("سيرة") || sub.contains("أخلاق") || sub.contains("إسلام") -> Icons.Default.Star
            sub.contains("عرب") || sub.contains("قراءة") || sub.contains("كتابة") || sub.contains("تعبير") || sub.contains("إملاء") || sub.contains("خط") || sub.contains("نحو") || sub.contains("صرف") -> Icons.Default.Translate
            slot.isFrench || sub.contains("fran") -> Icons.Default.Language
            sub.contains("رياضيات") || sub.contains("حساب") || sub.contains("قياس") || sub.contains("هندسة") -> Icons.Default.Calculate
            sub.contains("رياضة") || sub.contains("بدنية") -> Icons.Default.SportsBasketball
            sub.contains("علم") || sub.contains("علوم") || sub.contains("بيئة") || sub.contains("صحة") || sub.contains("سلامة") || sub.contains("غذاء") || sub.contains("تغذية") || sub.contains("تجريب") -> Icons.Default.Science
            sub.contains("مدنية") || sub.contains("مواطنة") || sub.contains("حياتية") || sub.contains("سلوك") || sub.contains("جغرافيا") || sub.contains("تاريخ") || sub.contains("إعلام") || sub.contains("اتصال") -> Icons.Default.Public
            sub.contains("فنية") || sub.contains("يدوية") || sub.contains("رسم") || sub.contains("أعمال") -> Icons.Default.Palette
            else -> Icons.Default.MenuBook
        }
    }

    val cardBorderColor = if (slot.isFrench) Color(0xFFBBDEFB) else BentoGrayOutline.copy(alpha = 0.5f)
    val iconBgColor = if (slot.isFrench) Color(0xFFE3F2FD) else BentoLightLavender

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.2.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconBgColor,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (slot.branch.isNotBlank() && slot.branch != slot.subject) {
                            "${slot.subject} (${slot.branch})"
                        } else {
                            slot.subject
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (slot.isFrench) {
                        Text(
                            text = "اللغة الفرنسية • Programme de Français",
                            fontSize = 10.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Text(
                    text = "${slot.durationMinutes} دقيقة",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoPrimaryDesc,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
