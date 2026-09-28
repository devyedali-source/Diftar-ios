@file:Suppress("unused", "PackageDirectoryMismatch")

package androidx.activity.result.contract

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.example.compat.GoogleOAuth
import com.example.compat.PlatformApi

abstract class ActivityResultContract<I, O> {
    internal abstract fun launch(input: I, deliver: (O) -> Unit)
}

object ActivityResultContracts {
    class StartActivityForResult : ActivityResultContract<Intent, ActivityResult>() {
        override fun launch(input: Intent, deliver: (ActivityResult) -> Unit) {
            if (input.action == GoogleOAuth.SIGN_IN_ACTION) {
                val scopes = input.extras["scopes"]?.toString() ?: GoogleOAuth.DEFAULT_SCOPES
                GoogleOAuth.signIn(scopes) { account, code, error ->
                    val data = Intent()
                    if (account != null) data.putExtra("account", account)
                    data.putExtra("statusCode", code)
                    data.putExtra("error", error)
                    deliver(ActivityResult(if (account != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, data))
                }
            } else {
                try {
                    com.example.compat.AppActivity.startActivity(input)
                    deliver(ActivityResult(Activity.RESULT_OK, null))
                } catch (e: Exception) {
                    deliver(ActivityResult(Activity.RESULT_CANCELED, null))
                }
            }
        }
    }

    class RequestPermission : ActivityResultContract<String, Boolean>() {
        override fun launch(input: String, deliver: (Boolean) -> Unit) {
            androidx.core.content.NotificationPermissionState.markAsked()
            if (PlatformApi.ciFlag("DAFTAR_SKIP_LOCK")) { deliver(false); return }
            PlatformApi.requestNotificationPermission { granted -> PlatformApi.runOnMain { deliver(granted) } }
        }
    }
}
