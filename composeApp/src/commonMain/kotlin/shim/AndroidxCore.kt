@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package androidx.core.content

import android.content.Context
import com.example.compat.PlatformApi

object ContextCompat {
    fun checkSelfPermission(context: Context, permission: String): Int =
        if (permission == android.Manifest.permission.POST_NOTIFICATIONS && !NotificationPermissionState.asked) {
            android.content.pm.PackageManager.PERMISSION_DENIED
        } else android.content.pm.PackageManager.PERMISSION_GRANTED
    fun getMainExecutor(context: Context): Any = Unit
}

object NotificationPermissionState {
    val asked: Boolean get() = PlatformApi.prefGet("__app_state", "notif_asked") == true
    fun markAsked() { PlatformApi.prefPut("__app_state", "notif_asked", true) }
}

object FileProvider {
    fun getUriForFile(context: Context, authority: String, file: java.io.File): android.net.Uri = android.net.Uri.fromFile(file)
}
