package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compat.AppApplication
import com.example.compat.DeviceAuth
import com.example.compat.PrintProgress
import com.example.compat.ToastHost
import com.example.ui.TeacherViewModel
import com.example.ui.screens.TeacherAppScreen
import com.example.ui.theme.MyApplicationTheme

/** الرابط الوارد (رابط التفعيل) — يقابل intent.data في MainActivity */
object DeepLinkBridge {
    var pending by mutableStateOf<android.net.Uri?>(null)
    fun handle(url: String) { pending = android.net.Uri.parse(url) }
}

private object AppState {
    var isAppUnlocked by mutableStateOf(false)
    var authStatusMessage by mutableStateOf<String?>(null)
    var viewModel: TeacherViewModel? = null

    fun showBiometricPrompt() {
        if (!DeviceAuth.canAuthenticate()) {
            isAppUnlocked = true
            authStatusMessage = null
            return
        }
        DeviceAuth.authenticate(
            "تطبيق المعلم المساعد يحمي بيانات الطلاب الحساسة. يرجى استخدام البصمة أو الوجه أو رمز قفل الشاشة لفتح التطبيق."
        ) { ok, error ->
            if (ok) {
                isAppUnlocked = true
                authStatusMessage = null
            } else {
                isAppUnlocked = false
                authStatusMessage = if (error != null) {
                    "يجب التحقق من الهوية لفتح التطبيق وتأمين بيانات الطلاب ($error)"
                } else "فشل التعرف على الهوية. يرجى المحاولة مرة أخرى."
            }
        }
    }
}

@Composable
fun App() {
    LaunchedEffect(Unit) { AppState.showBiometricPrompt() }
    MyApplicationTheme {
        Box(Modifier.fillMaxSize()) {
            if (AppState.isAppUnlocked) {
                val viewModel = remember {
                    AppState.viewModel ?: TeacherViewModel(AppApplication).also { AppState.viewModel = it }
                }
                LaunchedEffect(viewModel) { CiE2E.maybeRun(viewModel) }
                TeacherAppScreen(
                    viewModel = viewModel,
                    deepLinkUri = DeepLinkBridge.pending,
                    onDeepLinkConsumed = { DeepLinkBridge.pending = null }
                )
            } else {
                BiometricLockScreen(
                    statusMessage = AppState.authStatusMessage,
                    onRetry = { AppState.showBiometricPrompt() }
                )
            }
            PrintProgress.Overlay()
            ToastHost.Overlay()
        }
    }
}

@Composable
fun BiometricLockScreen(
    statusMessage: String?,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B5E20), Color(0xFF0D3B10), Color(0xFF001505))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(100.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Lock, contentDescription = "قفل الأمان", tint = Color.White, modifier = Modifier.size(54.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("بوابة الحماية والأمان 🔒", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                    Text(
                        "تطبيق المعلم المساعد محمّي لمنع الوصول غير المصرح به لبيانات الطلاب ودرجاتهم",
                        fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center, lineHeight = 20.sp
                    )
                }
                if (statusMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x33FFCDD2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66EF5350)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF8A80))
                            Text(statusMessage, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Start, modifier = Modifier.weight(1f))
                        }
                    }
                }
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("إعادة المحاولة / تأكيد الهوية 🔑", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
