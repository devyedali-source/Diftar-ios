package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.data.SchoolDirectory
import com.example.ui.theme.BentoGrayOutline
import com.example.ui.theme.BentoLightLavender
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDesc
import com.example.ui.theme.BentoText

/** مقاطعات الولاية: من الدليل الرسمي، وإن لم يوجد فالقائمة القديمة. */
@Composable
fun moughataaOptions(wilaya: String): List<String> {
    val context = LocalContext.current
    return remember(wilaya) {
        SchoolDirectory.moughataas(context, wilaya).ifEmpty { moughataasByWilaya[wilaya] ?: emptyList() }
    }
}

/** أول مقاطعة للولاية (لعند تغيير الولاية). */
fun firstMoughataa(context: android.content.Context, wilaya: String): String =
    SchoolDirectory.moughataas(context, wilaya).firstOrNull() ?: (moughataasByWilaya[wilaya]?.firstOrNull() ?: "")

/**
 * اختيار المدرسة من الدليل (بحث + قائمة) بدل الكتابة.
 * خيار «اكتب اسماً آخر» يبقى للمدرسة غير الموجودة في الدليل.
 */
@Composable
fun SchoolPickerField(
    wilaya: String,
    moughataa: String,
    schoolName: String,
    onSchoolChange: (String) -> Unit,
    enabled: Boolean = true,
    maxLength: Int = 80
) {
    val context = LocalContext.current
    val schools = remember(wilaya, moughataa) { SchoolDirectory.schools(context, wilaya, moughataa) }
    var showPicker by remember { mutableStateOf(false) }
    // الكتابة اليدوية: مفعّلة إن كان الاسم الحالي غير موجود في الدليل (أو لا يوجد دليل لهذه المقاطعة)
    var manual by remember(wilaya, moughataa) {
        mutableStateOf(schools.isEmpty() || (schoolName.isNotBlank() && schoolName.trim() !in schools))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!manual) {
            Text(
                text = "إسم المدرسة",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = BentoPrimaryDesc
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(BentoLightLavender, RoundedCornerShape(12.dp))
                    .border(1.dp, BentoGrayOutline, RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled) { showPicker = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = schoolName.ifBlank { "اضغط لاختيار المدرسة (${schools.size})" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (schoolName.isBlank()) BentoPrimaryDesc else BentoText,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = BentoPrimary)
                }
            }
            TextButton(onClick = { manual = true }, enabled = enabled) {
                Text("مدرستي غير موجودة؟ اكتب الاسم", fontSize = 12.sp)
            }
        } else {
            OutlinedTextField(
                value = schoolName,
                onValueChange = { if (it.length <= maxLength) onSchoolChange(it) },
                label = { Text("إسم المدرسة") },
                modifier = Modifier.fillMaxWidth().testTag("dialog_school_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = enabled
            )
            if (schools.isNotEmpty()) {
                TextButton(onClick = { manual = false; onSchoolChange("") }, enabled = enabled) {
                    Text("اختيار من قائمة المدارس", fontSize = 12.sp)
                }
            }
        }
    }

    if (showPicker) {
        var query by remember { mutableStateOf("") }
        val filtered = remember(query, schools) {
            val q = query.trim()
            if (q.isEmpty()) schools else schools.filter { it.contains(q) }
        }
        Dialog(onDismissRequest = { showPicker = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 560.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مدارس $moughataa (${schools.size})",
                            fontWeight = FontWeight.Bold,
                            color = BentoText,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showPicker = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = BentoPrimaryDesc)
                        }
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("ابحث عن مدرستك") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    if (filtered.isEmpty()) {
                        Text("لا توجد نتيجة. ارجع واختر «مدرستي غير موجودة».", color = BentoPrimaryDesc, fontSize = 12.sp)
                    }
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(filtered) { s ->
                            Text(
                                text = s,
                                fontSize = 14.sp,
                                fontWeight = if (s == schoolName.trim()) FontWeight.Bold else FontWeight.Normal,
                                color = if (s == schoolName.trim()) BentoPrimary else BentoText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSchoolChange(s); showPicker = false }
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                            )
                            HorizontalDivider(color = BentoGrayOutline)
                        }
                    }
                }
            }
        }
    }
}
