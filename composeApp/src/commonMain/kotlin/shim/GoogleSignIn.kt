@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package com.google.android.gms.auth.api.signin

import android.content.Context
import android.content.Intent
import com.example.compat.GoogleOAuth
import com.example.compat.Void
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class GoogleSignInOptions private constructor(val scopes: List<String>) {
    class Builder(base: GoogleSignInOptions? = null) {
        private val scopes = mutableListOf("openid", "email", "profile")
        fun requestIdToken(clientId: String) = this
        fun requestEmail() = this
        fun requestProfile() = this
        fun requestServerAuthCode(clientId: String) = this
        fun requestScopes(scope: Scope, vararg more: Scope) = apply {
            scopes.add(scope.scopeUri); more.forEach { scopes.add(it.scopeUri) }
        }
        fun build() = GoogleSignInOptions(scopes.distinct())
    }
    companion object {
        val DEFAULT_SIGN_IN = GoogleSignInOptions(listOf("openid", "email", "profile"))
    }
}

class GoogleSignInAccount internal constructor(
    val idToken: String?,
    val email: String?,
    val displayName: String?,
    val id: String?,
    val grantedScopes: Set<Scope>
) {
    val account: android.accounts.Account? get() = email?.let { android.accounts.Account(it, "com.google") }
    val photoUrl: android.net.Uri? get() = null
    val givenName: String? get() = displayName
    val serverAuthCode: String? get() = null
    fun isExpired(): Boolean = false
}

class GoogleSignInClient internal constructor(private val options: GoogleSignInOptions) {
    val signInIntent: Intent
        get() = Intent(GoogleOAuth.SIGN_IN_ACTION).putExtra("scopes", options.scopes.joinToString(" "))
    fun signOut(): Task<Void?> { GoogleOAuth.signOut(); return Tasks.forResult(null) }
    fun revokeAccess(): Task<Void?> { GoogleOAuth.signOut(); return Tasks.forResult(null) }
    fun silentSignIn(): Task<GoogleSignInAccount> {
        val acc = GoogleOAuth.lastAccount()
        return if (acc != null) Tasks.forResult(acc) else Tasks.forException(ApiException(4, "SIGN_IN_REQUIRED"))
    }
}

object GoogleSignIn {
    fun getClient(context: Context, options: GoogleSignInOptions): GoogleSignInClient = GoogleSignInClient(options)
    fun getClient(context: Any?, options: GoogleSignInOptions): GoogleSignInClient = GoogleSignInClient(options)
    fun getLastSignedInAccount(context: Context?): GoogleSignInAccount? = GoogleOAuth.lastAccount()
    fun hasPermissions(account: GoogleSignInAccount?, vararg scopes: Scope): Boolean =
        account != null && scopes.all { s -> account.grantedScopes.any { it.scopeUri == s.scopeUri } }

    fun getSignedInAccountFromIntent(data: Intent?): Task<GoogleSignInAccount> {
        val acc = data?.extras?.get("account") as? GoogleSignInAccount
        if (acc != null) return Tasks.forResult(acc)
        val code = (data?.extras?.get("statusCode") as? Int) ?: 12501
        val msg = data?.extras?.get("error")?.toString() ?: "cancelled"
        return Tasks.forException(ApiException(code, msg))
    }
}
