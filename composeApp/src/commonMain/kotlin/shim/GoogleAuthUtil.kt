@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package com.google.android.gms.auth

import android.content.Context
import android.content.Intent
import com.example.compat.GoogleOAuth

open class GoogleAuthException(message: String?) : Exception(message)

class UserRecoverableAuthException(message: String?, val intent: Intent?) : GoogleAuthException(message)

object GoogleAuthUtil {
    /** رمز وصول صالح للنطاق المطلوب (عملية مانعة — تُستدعى من الخلفية) */
    fun getToken(context: Context?, account: android.accounts.Account?, scope: String): String {
        return GoogleOAuth.accessTokenBlocking(scope.removePrefix("oauth2:"))
    }
    fun clearToken(context: Context?, token: String?) {
        GoogleOAuth.invalidateAccessToken(token)
    }
}
