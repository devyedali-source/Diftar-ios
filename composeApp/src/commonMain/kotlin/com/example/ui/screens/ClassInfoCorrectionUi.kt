package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.data.models.ClassSection
import com.example.ui.ClassInfoRequest
import com.example.ui.ClassInfoStatus
import com.example.ui.TeacherViewModel
import com.example.ui.theme.BentoGrayOutline
import com.example.ui.theme.BentoLightLavender
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDesc
import com.example.ui.theme.BentoText
import com.example.ui.utils.uiT

// ============================================================
// تصحيح بيانات القسم (اسم المدرسة / الولاية / المقاطعة)
// القواعد (والتحقق النهائي يتم على الخادم):
//  - تصحيح مباشر مرة واحدة لكل قسم، بشرط ألا يكون القسم مدفوعاً.
//  - غير ذلك: طلب تصحيح يوافق عليه المدير.
// ============================================================

@Composable
fun EditClassInfoDialog(
    section: ClassSection,
    viewModel: TeacherViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var status by remember { mutableStateOf<ClassInfoStatus?>(null) }
    var loadKey by remember { mutableStateOf(0) }
    var schoolName by remember { mutableStateOf(section.schoolName) }
    var wilaya by remember { mutableStateOf(section.wilaya) }
    var moughataa by remember { mutableStateOf(section.moughataa) }
    var reason by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showConfirmDirect by remember { mutableStateOf(false) }

    LaunchedEffect(loadKey) {
        status = null
        viewModel.fetchClassInfoStatus(section.id) { status = it }
    }

    val currentStatus = status
    val isDirect = currentStatus != null && currentStatus.error == null && currentStatus.canEditDirectly

    if (showConfirmDirect) {
        AlertDialog(
            onDismissRequest = { if (!busy) showConfirmDirect = false },
            title = { Text(uiT("تأكيد التصحيح"), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "ستُحفظ البيانات الجديدة فوراً، ولن يمكنك التصحيح المباشر لهذا القسم مرة أخرى (أي تصحيح لاحق يحتاج موافقة المدير).\n\n" +
                        "المدرسة: ${schoolName.trim()}\nالولاية: $wilaya\nالمقاطعة: $moughataa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoText
                )
            },
            confirmButton = {
                Button(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        errorText = null
                        viewModel.applyDirectClassInfoCorrection(section.id, schoolName, wilaya, moughataa) { ok, msg ->
                            busy = false
                            showConfirmDirect = false
                            if (ok) {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                errorText = msg
                                loadKey++
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    if (busy) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(uiT("نعم، احفظ"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(enabled = !busy, onClick = { showConfirmDirect = false }) {
                    Text(uiT("رجوع"), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 640.dp)
                .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiT("تصحيح بيانات القسم ✏️"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoText,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { if (!busy) onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = BentoPrimaryDesc)
                    }
                }

                Text(
                    text = section.getFormattedName(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoPrimaryDesc
                )

                HorizontalDivider(color = BentoGrayOutline)

                if (currentStatus == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BentoPrimary)
                    }
                } else if (currentStatus.error != null) {
                    Text(
                        text = currentStatus.error,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = { errorText = null; loadKey++ },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(uiT("إعادة المحاولة"), fontWeight = FontWeight.Bold)
                    }
                } else {
                    // شرح الحالة
                    val infoText: String
                    val infoFg: Color
                    if (currentStatus.canEditDirectly) {
                        infoText = "يمكنك تصحيح البيانات الآن مباشرة، لكن مرة واحدة فقط لهذا القسم. تأكد من الكتابة الصحيحة قبل الحفظ."
                        infoFg = Color(0xFF2E7D32)
                    } else if (currentStatus.lockReason == "PAID") {
                        infoText = "هذا القسم مفعّل (مدفوع)، لذلك يحتاج أي تصحيح إلى موافقة المدير. اكتب البيانات الصحيحة وسبب التصحيح ثم أرسل الطلب."
                        infoFg = Color(0xFFE65100)
                    } else {
                        infoText = "استعملت التصحيح المباشر لهذا القسم من قبل، لذلك يحتاج التصحيح الآن إلى موافقة المدير. اكتب البيانات الصحيحة وسبب التصحيح ثم أرسل الطلب."
                        infoFg = Color(0xFFE65100)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, infoFg.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(text = infoText, color = infoFg, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                    }

                    val pending = currentStatus.pending
                    if (pending != null) {
                        val pSchool = pending["newSchoolName"] as? String ?: ""
                        Text(
                            text = "⏳ لديك طلب قيد المراجعة (المدرسة المطلوبة: $pSchool). إرسال طلب جديد سيستبدله.",
                            color = BentoPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    }

                    // الولاية والمقاطعة
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            MauritaniaDropdown(
                                label = "الولاية",
                                selectedValue = wilaya,
                                options = mauritanianWilayas,
                                onValueChanged = { w ->
                                    if (w != wilaya) {
                                        wilaya = w
                                        moughataa = moughataasByWilaya[w]?.firstOrNull() ?: ""
                                    }
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MauritaniaDropdown(
                                label = "المقاطعة",
                                selectedValue = moughataa,
                                options = moughataasByWilaya[wilaya] ?: emptyList(),
                                onValueChanged = { m -> moughataa = m }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { if (it.length <= 80) schoolName = it },
                        label = { Text(uiT("إسم المدرسة")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = !busy
                    )

                    if (!isDirect) {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { if (it.length <= 300) reason = it },
                            label = { Text(uiT("سبب التصحيح")) },
                            placeholder = { Text(uiT("مثال: كتبت اسم المدرسة بشكل خاطئ")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4,
                            enabled = !busy
                        )
                        Text(
                            text = "بعد الإرسال سيُفتح الواتساب لتنبيه المدير بالطلب.",
                            color = BentoPrimaryDesc,
                            fontSize = 11.sp
                        )
                    }

                    if (errorText != null) {
                        Text(text = errorText ?: "", color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                    }

                    val canSubmit = !busy && schoolName.trim().length >= 2 && (isDirect || reason.isNotBlank())
                    Button(
                        onClick = {
                            errorText = null
                            if (isDirect) {
                                showConfirmDirect = true
                            } else {
                                busy = true
                                viewModel.submitClassInfoRequest(section.id, schoolName, wilaya, moughataa, reason) { ok, msg ->
                                    busy = false
                                    if (ok) {
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        openClassInfoWhatsApp(context, section, schoolName.trim(), wilaya, moughataa, reason.trim())
                                        onDismiss()
                                    } else {
                                        errorText = msg
                                    }
                                }
                            }
                        },
                        enabled = canSubmit,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary, contentColor = Color.White)
                    ) {
                        if (busy) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (isDirect) "حفظ التصحيح (مرة واحدة)" else "إرسال الطلب للمدير 📨",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun openClassInfoWhatsApp(
    context: android.content.Context,
    section: ClassSection,
    newSchool: String,
    newWilaya: String,
    newMoughataa: String,
    reason: String
) {
    try {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val email = authUser?.email ?: ""
        val uid = authUser?.uid ?: ""
        val message = "السلام عليكم،\n" +
            "أنا معلم القسم: ${section.getFormattedName()}\nالسنة الدراسية: ${section.academicYear}\n" +
            "أطلب تصحيح بيانات هذا القسم.\n\n" +
            "المدرسة الحالية: ${section.schoolName}\n" +
            "المدرسة الصحيحة: $newSchool\n" +
            "الولاية: $newWilaya — المقاطعة: $newMoughataa\n" +
            "السبب: $reason" +
            if (uid.isNotBlank()) "\n\n------------------------------------\nرابط مراجعة الطلب:\nhttps://diftar-almoaalim.web.app/activate-class-info/${uid}_${section.id}" else ""
        val url = "https://api.whatsapp.com/send?phone=22237786585&text=${android.net.Uri.encode(message)}"
        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "تم حفظ الطلب، لكن تعذّر فتح الواتساب. سيراه المدير في لوحته.",
            Toast.LENGTH_LONG
        ).show()
    }
}

// ============================================================
// لوحة المدير: طلبات تصحيح بيانات الأقسام
// ============================================================

private fun infoLogLine(e: Map<String, Any>): String {
    val at = (e["at"] as? Number)?.toLong() ?: 0L
    val date = if (at > 0L) {
        java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date(at))
    } else "-"
    val type = when (e["type"] as? String) {
        "direct" -> "تصحيح مباشر"
        "approved" -> "طلب تمت الموافقة عليه"
        "rejected" -> "طلب مرفوض"
        else -> "تغيير"
    }
    val oldS = e["oldSchoolName"] as? String ?: ""
    val newS = e["newSchoolName"] as? String ?: ""
    return "$date • $type: «$oldS» إلى «$newS»"
}

@Composable
fun AdminClassInfoRequestsTab(viewModel: TeacherViewModel) {
    val context = LocalContext.current
    val requests by viewModel.classInfoRequests.collectAsState()
    var busyKey by remember { mutableStateOf<String?>(null) }
    var rejectTarget by remember { mutableStateOf<ClassInfoRequest?>(null) }
    var rejectNote by remember { mutableStateOf("") }

    val target = rejectTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text(uiT("رفض الطلب"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "سيصل المعلم إشعار بالرفض. يمكنك كتابة ملاحظة له (اختياري).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = rejectNote,
                        onValueChange = { if (it.length <= 200) rejectNote = it },
                        label = { Text(uiT("ملاحظة للمعلم")) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val key = "${target.teacherId}_${target.classId}"
                        busyKey = key
                        rejectTarget = null
                        viewModel.rejectClassInfoRequest(target, rejectNote) { _, msg ->
                            busyKey = null
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(uiT("رفض الطلب"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null }) { Text(uiT("إلغاء")) }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiT("طلبات تصحيح بيانات الأقسام ✏️"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                    Text(
                        text = "راجع المدرسة القديمة والجديدة والسبب، ثم وافق أو ارفض.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoPrimaryDesc
                    )
                }
                TextButton(onClick = {
                    viewModel.loadStatsData()
                    Toast.makeText(context, "تم تحديث الطلبات 🔄", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "تحديث", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(uiT("تحديث"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f))

            if (requests.isEmpty()) {
                Text(
                    text = "لا توجد طلبات تصحيح معلّقة ✅",
                    color = BentoPrimaryDesc,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                requests.forEach { req ->
                    val key = "${req.teacherId}_${req.classId}"
                    ClassInfoRequestCard(
                        req = req,
                        busy = busyKey == key,
                        anyBusy = busyKey != null,
                        onApprove = {
                            busyKey = key
                            viewModel.approveClassInfoRequest(req) { _, msg ->
                                busyKey = null
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        onReject = {
                            rejectNote = ""
                            rejectTarget = req
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClassInfoRequestCard(
    req: ClassInfoRequest,
    busy: Boolean,
    anyBusy: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var showLog by remember { mutableStateOf(false) }
    val dateStr = if (req.requestedAt > 0L) {
        java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date(req.requestedAt))
    } else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoGrayOutline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = BentoLightLavender.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = req.className.ifBlank { "قسم رقم ${req.classId}" },
                fontWeight = FontWeight.Bold,
                color = BentoText,
                fontSize = 14.sp
            )
            Text(
                text = "المعلم: ${req.displayName.ifBlank { "-" }} — ${req.email.ifBlank { req.teacherId }}",
                fontSize = 12.sp,
                color = BentoPrimaryDesc
            )
            Text(text = "تاريخ الطلب: $dateStr", fontSize = 12.sp, color = BentoPrimaryDesc)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (req.isPaid) "💰 القسم مدفوع" else "🆓 غير مدفوع",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (req.isPaid) Color(0xFFE65100) else Color(0xFF2E7D32)
                )
                Text(
                    text = "• تصحيحات مباشرة سابقة: ${req.editCount}",
                    fontSize = 11.sp,
                    color = BentoPrimaryDesc
                )
            }

            HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.6f))

            Text(text = "المدرسة الحالية: ${req.currentSchool.ifBlank { "-" }}", fontSize = 13.sp, color = BentoText)
            Text(
                text = "المدرسة المطلوبة: ${req.newSchool}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = BentoPrimary
            )
            if (req.newWilaya != req.currentWilaya || req.newMoughataa != req.currentMoughataa) {
                Text(
                    text = "الولاية/المقاطعة: ${req.currentWilaya.ifBlank { "-" }} / ${req.currentMoughataa.ifBlank { "-" }}" +
                        " إلى ${req.newWilaya.ifBlank { "-" }} / ${req.newMoughataa.ifBlank { "-" }}",
                    fontSize = 12.sp,
                    color = BentoText
                )
            }
            Text(text = "السبب: ${req.reason.ifBlank { "-" }}", fontSize = 12.sp, color = BentoText)

            if (req.changeLog.isNotEmpty()) {
                TextButton(onClick = { showLog = !showLog }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    Text(
                        text = if (showLog) "إخفاء سجل التغييرات ▲" else "عرض سجل التغييرات (${req.changeLog.size}) ▼",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                }
                if (showLog) {
                    req.changeLog.sortedByDescending { (it["at"] as? Number)?.toLong() ?: 0L }.forEach { entry ->
                        Text(text = infoLogLine(entry), fontSize = 11.sp, color = BentoPrimaryDesc, lineHeight = 16.sp)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    enabled = !anyBusy,
                    modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (busy) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(uiT("موافقة ✅"), fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !anyBusy,
                    modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(uiT("رفض"), color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

// ============================================================
// صفحة المدير التي يفتحها رابط الواتساب: طلب تصحيح واحد
// الرابط: https://diftar-almoaalim.web.app/activate-class-info/{uidالمعلم}_{رقمالقسم}
// ============================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ClassInfoRequestLinkView(
    requestKey: String,
    viewModel: TeacherViewModel,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin = currentUser?.email?.trim()?.lowercase() == "elyedalimoctar@gmail.com"
    val sep = requestKey.lastIndexOf('_')
    val teacherId = if (sep > 0) requestKey.substring(0, sep) else ""
    val classId = if (sep > 0) requestKey.substring(sep + 1).toLongOrNull() else null

    var req by remember { mutableStateOf<ClassInfoRequest?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var doneMessage by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }
    var showReject by remember { mutableStateOf(false) }
    var rejectNote by remember { mutableStateOf("") }

    LaunchedEffect(requestKey, reloadKey, isAdmin) {
        if (!isAdmin) return@LaunchedEffect
        if (teacherId.isBlank() || classId == null) {
            error = "رابط الطلب غير صالح."
            loading = false
            return@LaunchedEffect
        }
        loading = true
        error = null
        viewModel.loadClassInfoRequest(teacherId, classId) { r, e ->
            req = r
            error = e
            loading = false
        }
    }

    val current = req
    if (showReject && current != null) {
        AlertDialog(
            onDismissRequest = { showReject = false },
            title = { Text(uiT("رفض الطلب"), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = rejectNote,
                    onValueChange = { if (it.length <= 200) rejectNote = it },
                    label = { Text(uiT("ملاحظة للمعلم (اختياري)")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReject = false
                        busy = true
                        viewModel.rejectClassInfoRequest(current, rejectNote) { ok, msg ->
                            busy = false
                            if (ok) doneMessage = msg else Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text(uiT("رفض الطلب"), color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showReject = false }) { Text(uiT("إلغاء")) } }
        )
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = uiT("طلب تصحيح بيانات قسم ✏️"),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = BentoPrimary)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isAdmin) {
                // غير المدير: لا تُعرض أي رسالة، تُغلق الصفحة فوراً
                LaunchedEffect(Unit) { onClose() }
            } else if (doneMessage != null) {
                Text(text = doneMessage ?: "", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 15.sp)
            } else if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BentoPrimary)
                }
            } else if (current == null) {
                Text(text = error ?: "تعذّر تحميل الطلب.", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                Button(
                    onClick = { reloadKey++ },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(uiT("إعادة المحاولة 🔄"), fontWeight = FontWeight.Bold) }
            } else {
                ClassInfoRequestCard(
                    req = current,
                    busy = busy,
                    anyBusy = busy,
                    onApprove = {
                        busy = true
                        viewModel.approveClassInfoRequest(current) { ok, msg ->
                            busy = false
                            if (ok) doneMessage = msg else Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    onReject = {
                        rejectNote = ""
                        showReject = true
                    }
                )
            }
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                shape = RoundedCornerShape(12.dp)
            ) { Text(uiT("إغلاق والعودة 🏠"), fontWeight = FontWeight.Bold) }
        }
    }
}
