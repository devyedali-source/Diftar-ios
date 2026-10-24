package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.ClassSection
import com.example.ui.CoverDesign
import com.example.ui.CoverInfo
import com.example.ui.utils.uiT

@Composable
private fun CoverPreview(section: ClassSection, title: String, info: CoverInfo, design: Int, stampHtml: String) {
    val context = LocalContext.current
    val html = remember(section, title, info, design, stampHtml) { CoverDesign.previewHtml(context, section, title, info, design, stampHtml) }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val heightDp = maxWidth * (CoverDesign.H_MM.toFloat() / CoverDesign.W_MM.toFloat())
        HtmlPreview(html = html, modifier = Modifier.fillMaxWidth().height(heightDp))
    }
}

/**
 * واجهة الغلاف: الخطوة 1 معلومات الغلاف (الاسم، الدليل المالي، الهاتف، صورة اختيارية)،
 * الخطوة 2 اختيار تصميم الغلاف. المدرسة والقسم والسنة تؤخذ من إعدادات القسم.
 * onResult يستلم شريحة HTML الخاصة بالغلاف لتُدرج في أول الدفتر.
 */
@Composable
fun CoverDialog(
    viewModel: com.example.ui.TeacherViewModel,
    section: ClassSection,
    bookTitle: String,
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(0) }
    val saved = remember { CoverDesign.load(context) }
    var teacher by remember { mutableStateOf(saved.teacher) }
    var finance by remember { mutableStateOf(saved.finance) }
    var phone by remember { mutableStateOf(saved.phone) }
    var photo by remember { mutableStateOf(saved.photoB64) }
    var design by remember { mutableStateOf(0) }

    val info = CoverInfo(teacher, finance, phone, photo)
    // طابع المعلم كما في إعدادات الطابع (يظهر فقط إن كان مفعّلاً في الإعدادات)
    val stampHtml = remember(section.schoolName) {
        com.example.ui.HtmlReportHelper.getTeacherStampHtml(viewModel, section.schoolName, 110)
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.94f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text(uiT("غلاف الدفتر") + " — " + bookTitle, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(6.dp))
                if (step == 0) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Text(
                            uiT("المدرسة والقسم والسنة الدراسية تؤخذ تلقائيًا من إعدادات القسم. اترك أي خانة فارغة إن أردت كتابتها بالقلم."),
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = teacher, onValueChange = { teacher = it },
                            label = { Text(uiT("اسم المعلم(ة)")) }, singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = finance, onValueChange = { finance = it },
                            label = { Text(uiT("الدليل المالي")) }, singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phone, onValueChange = { phone = it },
                            label = { Text(uiT("رقم الهاتف")) }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = onDismiss) { Text(uiT("إلغاء")) }
                        Button(onClick = { step = 1 }) { Text(uiT("التالي: اختيار التصميم")) }
                    }
                } else {
                    Text(uiT("اضغط على الغلاف الذي يعجبك."), fontSize = 12.sp)
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CoverDesign.names.forEachIndexed { i, n ->
                            val selected = design == i
                            val shape = RoundedCornerShape(12.dp)
                            Column(
                                Modifier.fillMaxWidth().clip(shape)
                                    .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFBBBBBB), shape)
                                    .clickable { design = i }.padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selected, onClick = { design = i })
                                    Text(uiT(n), fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Color.White)) {
                                    CoverPreview(section, bookTitle, info, i, stampHtml)
                                    Box(Modifier.matchParentSize().clickable { design = i })
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { step = 0 }) { Text(uiT("رجوع")) }
                        Button(onClick = {
                            CoverDesign.save(context, info)
                            onResult(CoverDesign.body(context, section, bookTitle, info, design, stampHtml))
                        }) { Text(uiT("اعتماد الغلاف وسحب الدفتر")) }
                    }
                }
            }
        }
    }
}
