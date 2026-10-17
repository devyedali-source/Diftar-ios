package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO
import com.example.ui.utils.uiT
import com.example.ui.utils.uiTf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val AdmOkColor = Color(0xFF2E7D32)
private val AdmBadColor = Color(0xFFC62828)
private val AdmMutedColor = Color(0xFF6B6B6B)
private val AdmWarnColor = Color(0xFFE65100)

private fun admFormatMillis(millis: Long): String {
    if (millis <= 0L) return "—"
    val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
    return formatter.format(Date(millis))
}

private fun admFormatDate(millis: Long): String {
    if (millis <= 0L) return "—"
    val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    return formatter.format(Date(millis))
}

@Composable
fun CourseActivationAdminView(
    requestId: String,
    onClose: () -> Unit
) {
    var loading by remember(requestId) { mutableStateOf(true) }
    var notFound by remember(requestId) { mutableStateOf(false) }
    var errorText by remember(requestId) { mutableStateOf("") }
    var working by remember(requestId) { mutableStateOf(false) }

    var email by remember(requestId) { mutableStateOf("") }
    var specTitle by remember(requestId) { mutableStateOf("") }
    var specId by remember(requestId) { mutableStateOf("") }
    var userId by remember(requestId) { mutableStateOf("") }
    var days by remember(requestId) { mutableStateOf(45) }
    var status by remember(requestId) { mutableStateOf("") }
    var createdAt by remember(requestId) { mutableStateOf(0L) }
    var expiresAt by remember(requestId) { mutableStateOf(0L) }
    var loadFailed by remember(requestId) { mutableStateOf(false) }
    var reloadKey by remember(requestId) { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(requestId, reloadKey) {
        loading = true
        notFound = false
        loadFailed = false
        errorText = ""
        val snap = com.example.data.net.FirestoreRetry.getDoc(
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("course_activation_requests").document(requestId)
        )
        if (snap == null) {
            loadFailed = true
            errorText = "📶 الاتصال ضعيف: تعذّر جلب الطلب بعد عدة محاولات."
        } else if (!snap.exists()) {
            notFound = true
        } else {
            email = snap.getString("email") ?: ""
            specTitle = snap.getString("specTitle") ?: ""
            specId = snap.getString("specId") ?: ""
            userId = snap.getString("userId") ?: ""
            days = (snap.getLong("days") ?: 45L).toInt()
            status = snap.getString("status") ?: ""
            createdAt = snap.getLong("createdAt") ?: 0L
            expiresAt = snap.getLong("expiresAt") ?: 0L
        }
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
            Text(text = uiT("جارٍ جلب طلب تفعيل الدورة..."), fontSize = 14.sp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = uiT("تفعيل دورة تحضير مسابقات المعلمين"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = uiTf("رقم الطلب: {0}", requestId), fontSize = 11.sp, color = AdmMutedColor)
        Spacer(modifier = Modifier.height(14.dp))

        if (notFound) {
            Text(
                text = uiT("لم يُعثر على هذا الطلب. قد يكون المرسل في منطقة ضعيفة الشبكة ولم يصل طلبه بعد، فأعد المحاولة بعد قليل."),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AdmBadColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { reloadKey++ }, modifier = Modifier.fillMaxWidth()) {
                Text(text = uiT("إعادة المحاولة 🔄"), fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { onClose() }, modifier = Modifier.fillMaxWidth()) {
                Text(text = uiT("إغلاق والعودة"), fontSize = 15.sp)
            }
            return
        }

        if (loadFailed) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    text = errorText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdmBadColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Button(onClick = { reloadKey++ }, modifier = Modifier.fillMaxWidth()) {
                Text(text = uiT("إعادة المحاولة 🔄"), fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { onClose() }, modifier = Modifier.fillMaxWidth()) {
                Text(text = uiT("إغلاق والعودة"), fontSize = 15.sp)
            }
            return
        }

        if (errorText.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    text = uiTf("خطأ: {0}", errorText),
                    fontSize = 13.sp,
                    color = AdmBadColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = uiT("بريد المعلّم"), fontSize = 13.sp, color = AdmMutedColor)
                    Text(text = email.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = uiT("التخصّص"), fontSize = 13.sp, color = AdmMutedColor)
                    Text(
                        text = specTitle.ifBlank { specId.ifBlank { "—" } },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = uiT("المدّة"), fontSize = 13.sp, color = AdmMutedColor)
                    Text(text = uiTf("{0} يومًا", days), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = uiT("وقت الطلب"), fontSize = 13.sp, color = AdmMutedColor)
                    Text(text = admFormatMillis(createdAt), fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = uiT("الحالة"), fontSize = 13.sp, color = AdmMutedColor)
                    Text(
                        text = if (status == "completed") "مفعَّل" else "قيد الانتظار",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (status == "completed") AdmOkColor else AdmWarnColor
                    )
                }
                if (status == "completed" && expiresAt > 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = uiT("ينتهي في"), fontSize = 13.sp, color = AdmMutedColor)
                        Text(
                            text = admFormatDate(expiresAt),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdmOkColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (status == "completed") {
            Text(
                text = uiT("هذا الطلب مفعَّل بالفعل."),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AdmOkColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { onClose() }, modifier = Modifier.fillMaxWidth()) {
                Text(text = uiT("إغلاق والعودة"), fontSize = 15.sp)
            }
        } else {
            Button(
                onClick = {
                    if (working) return@Button
                    working = true
                    errorText = ""
                    // تاريخ الانتهاء يُحسب مرة واحدة لكل ضغطة حتى لا يتغير مع إعادة المحاولة
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, days)
                    val newExpiry = calendar.timeInMillis
                    val activatedAtNow = System.currentTimeMillis()
                    scope.launch {
                        val ok = com.example.data.net.FirestoreRetry.updateDoc(
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("course_activation_requests").document(requestId),
                            mapOf(
                                "status" to "completed",
                                "activatedAt" to activatedAtNow,
                                "expiresAt" to newExpiry
                            )
                        )
                        if (ok) {
                            status = "completed"
                            expiresAt = newExpiry
                        } else {
                            errorText = "📶 لم يتأكد التفعيل بسبب ضعف الاتصال. قد يكون تمّ فعلًا؛ اضغط «تفعيل» مرة أخرى للتأكد (آمن ولا يتكرر)."
                        }
                        working = false
                    }
                },
                enabled = !working,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (working) "جارٍ التفعيل..." else "تفعيل هذا التخصّص",
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { onClose() }, modifier = Modifier.fillMaxWidth()) {
                Text(text = uiT("إغلاق بلا تفعيل"), fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
