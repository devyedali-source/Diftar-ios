package com.example.ui.update

import com.example.compat.*
import kotlinx.coroutines.IO
import android.content.Context
import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * نسخة الآيفون من مدير التحديثات: تتحقق من App Store (بدل Google Play)
 * وتعرض نفس النافذة ونفس الأزرار.
 */
object InAppUpdateManager {
    const val IN_APP_UPDATE_REQUEST_CODE = 8901
    private var hasCheckedInThisSession = false
    private var storeUrl: String? = null

    var showUpdatePromptDialog by androidx.compose.runtime.mutableStateOf(false)
    var isUpdateDownloadedState by androidx.compose.runtime.mutableStateOf(false)

    fun checkForUpdatesOncePerSession(context: Context) {
        if (hasCheckedInThisSession) return
        hasCheckedInThisSession = true
        com.google.android.gms.tasks.backgroundTask {
            val res = PlatformApi.httpExecute(
                "GET",
                "https://itunes.apple.com/lookup?bundleId=" + PlatformApi.bundleId() + "&country=mr",
                emptyMap(), null, 15.0
            )
            if (!res.isSuccessful) return@backgroundTask null
            val root = org.json.JSONObject(res.text())
            val results = root.optJSONArray("results") ?: return@backgroundTask null
            if (results.length() == 0) return@backgroundTask null
            val item = results.getJSONObject(0)
            item.optString("version", "") to item.optString("trackViewUrl", "")
        }.addOnSuccessListener { pair ->
            if (pair != null) {
                val (storeVersion, url) = pair
                storeUrl = url.ifBlank { null }
                if (isNewer(storeVersion, PlatformApi.appVersionName())) showUpdatePromptDialog = true
            }
        }
    }

    private fun isNewer(store: String, current: String): Boolean {
        val a = store.split('.').map { it.toIntOrNull() ?: 0 }
        val b = current.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }; val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    fun startFlexibleUpdate(activity: Activity) {
        showUpdatePromptDialog = false
        openPlayStore(activity)
    }

    fun dismissUpdateDialog() {
        showUpdatePromptDialog = false
    }

    fun completeUpdate() {
        isUpdateDownloadedState = false
        storeUrl?.let { PlatformApi.openUrl(it) }
    }

    fun onResume(context: Context) {}

    /** يفتح صفحة التطبيق في App Store */
    fun openPlayStore(context: Context) {
        showUpdatePromptDialog = false
        val url = storeUrl ?: ("itms-apps://itunes.apple.com/app/id" + (PlatformApi.googleServiceValue("APP_STORE_ID") ?: ""))
        PlatformApi.openUrl(url)
    }
}

@Composable
fun InAppUpdateDialog(
    onUpdateNow: () -> Unit,
    onLater: () -> Unit
) {
    Dialog(
        onDismissRequest = onLater,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "تحديث جديد",
                            tint = BentoPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = "يتوفر إصدار جديد",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = BentoText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "يتوفر إصدار جديد من تطبيق دفتر المعلم. ننصح بالتحديث للحصول على أحدث التحسينات والإصلاحات.",
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onLater,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "لاحقًا",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = onUpdateNow,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "تحديث الآن",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Floating / Top Banner when Flexible Update has finished downloading.
 */
@Composable
fun UpdateDownloadedNotificationCard(
    onInstallNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DownloadDone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تم تنزيل التحديث الجديد.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = "جاهز للتثبيت لتطبيق أحدث الميزات والإصلاحات",
                    fontSize = 10.5.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Button(
                onClick = onInstallNow,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1B5E20)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "تثبيت الآن",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
