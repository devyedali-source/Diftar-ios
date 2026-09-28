package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

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

    LaunchedEffect(requestId) {
        loading = true
        notFound = false
        errorText = ""
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("course_activation_requests")
                .document(requestId)
                .get()
                .addOnSuccessListener { snap ->
                    if (!snap.exists()) {
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
                .addOnFailureListener { e ->
                    errorText = e.message ?: "تعذّر جلب الطلب"
                    loading = false
                }
        } catch (e: Exception) {
            errorText = e.message ?: "تعذّر جلب الطلب"
            loading = false
        }
    }

    if (loading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "جارٍ جلب طلب تفعيل الدورة...", fontSize = 14.sp)
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
            text = "تفعيل دورة تحضير مسابقات المعلمين",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "رقم الطلب: $requestId", fontSize = 11.sp, color = AdmMutedColor)
        Spacer(modifier = Modifier.height(14.dp))

        if (notFound) {
            Text(
                text = "لم يُعثر على هذا الطلب في قاعدة البيانات.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AdmBadColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { onClose() }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "إغلاق والعودة", fontSize = 15.sp)
            }
            return
        }

        if (errorText.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    text = "خطأ: $errorText",
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
                    Text(text = "بريد المعلّم", fontSize = 13.sp, color = AdmMutedColor)
                    Text(text = email.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "التخصّص", fontSize = 13.sp, color = AdmMutedColor)
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
                    Text(text = "المدّة", fontSize = 13.sp, color = AdmMutedColor)
                    Text(text = "$days يومًا", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "وقت الطلب", fontSize = 13.sp, color = AdmMutedColor)
                    Text(text = admFormatMillis(createdAt), fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "الحالة", fontSize = 13.sp, color = AdmMutedColor)
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
                        Text(text = "ينتهي في", fontSize = 13.sp, color = AdmMutedColor)
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
                text = "هذا الطلب مفعَّل بالفعل.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AdmOkColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { onClose() }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "إغلاق والعودة", fontSize = 15.sp)
            }
        } else {
            Button(
                onClick = {
                    if (working) return@Button
                    working = true
                    errorText = ""
                    try {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, days)
                        val newExpiry = calendar.timeInMillis
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val updates = mapOf(
                            "status" to "completed",
                            "activatedAt" to System.currentTimeMillis(),
                            "expiresAt" to newExpiry
                        )
                        db.collection("course_activation_requests")
                            .document(requestId)
                            .update(updates)
                            .addOnSuccessListener {
                                status = "completed"
                                expiresAt = newExpiry
                                working = false
                            }
                            .addOnFailureListener { e ->
                                errorText = e.message ?: "تعذّر التفعيل"
                                working = false
                            }
                    } catch (e: Exception) {
                        errorText = e.message ?: "تعذّر التفعيل"
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
                Text(text = "إغلاق بلا تفعيل", fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
