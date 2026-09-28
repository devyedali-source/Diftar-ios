package com.example.compat

import android.content.Intent
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.backgroundTask
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * تسجيل الدخول بحساب Google على الآيفون (OAuth 2.0 مع PKCE عبر نافذة Safari الآمنة).
 * يعطي نفس ما يعطيه Google Sign-In في أندرويد: رمز الهوية لـ Firebase ورمز الوصول لـ Drive.
 */
@OptIn(ExperimentalEncodingApi::class)
object GoogleOAuth {
    const val SIGN_IN_ACTION = "ios.google.SIGN_IN"
    private const val STORE = "__google_oauth"
    const val DEFAULT_SCOPES = "openid email profile https://www.googleapis.com/auth/drive.file"

    private fun get(k: String): String? = PlatformApi.prefGet(STORE, k) as? String
    private fun put(k: String, v: Any?) = PlatformApi.prefPut(STORE, k, v)

    fun lastAccount(): GoogleSignInAccount? {
        val email = get("email") ?: return null
        val scopes = (get("scopes") ?: "").split(' ').filter { it.isNotBlank() }.map { Scope(it) }.toSet()
        return GoogleSignInAccount(get("idToken"), email, get("name"), get("sub"), scopes)
    }

    fun signOut() {
        PlatformApi.prefClear(STORE)
    }

    private fun randomString(len: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..len).map { chars[kotlin.random.Random.nextInt(chars.length)] }.joinToString("")
    }

    private fun b64url(bytes: ByteArray): String = Base64.UrlSafe.encode(bytes).trimEnd('=')

    /** يفتح صفحة Google لاختيار الحساب ثم يعيد الحساب (أو رمز خطأ) على الخيط الرئيسي */
    fun signIn(scopes: String, callback: (GoogleSignInAccount?, Int, String?) -> Unit) {
        val clientId = FirebaseConfig.iosClientId
        val scheme = FirebaseConfig.reversedClientId
        if (clientId.isNullOrBlank() || scheme.isNullOrBlank()) {
            callback(null, 10, "ملف إعدادات Google للآيفون (GoogleService-Info.plist) غير موجود")
            return
        }
        val redirect = "$scheme:/oauth2redirect"
        val verifier = randomString(64)
        val challenge = b64url(PlatformApi.digest("SHA256", verifier.encodeToByteArray()))
        val state = randomString(24)
        val scopeStr = (if (scopes.isBlank()) DEFAULT_SCOPES else scopes).split(' ').filter { it.isNotBlank() }.distinct().joinToString(" ")
        val url = "https://accounts.google.com/o/oauth2/v2/auth" +
            "?client_id=" + android.net.Uri.encode(clientId) +
            "&redirect_uri=" + android.net.Uri.encode(redirect) +
            "&response_type=code" +
            "&scope=" + android.net.Uri.encode(scopeStr) +
            "&code_challenge=" + challenge +
            "&code_challenge_method=S256" +
            "&state=" + state +
            "&prompt=select_account"

        PlatformApi.startGoogleOAuth(url, scheme) { callbackUrl, error ->
            if (callbackUrl == null) {
                callback(null, 12501, error ?: "cancelled")
                return@startGoogleOAuth
            }
            val uri = android.net.Uri.parse(callbackUrl)
            val code = uri.getQueryParameter("code")
            if (code.isNullOrBlank() || uri.getQueryParameter("state") != state) {
                callback(null, 12501, uri.getQueryParameter("error") ?: "cancelled")
                return@startGoogleOAuth
            }
            backgroundTask {
                val form = "code=" + android.net.Uri.encode(code) +
                    "&client_id=" + android.net.Uri.encode(clientId) +
                    "&redirect_uri=" + android.net.Uri.encode(redirect) +
                    "&grant_type=authorization_code" +
                    "&code_verifier=" + verifier
                val o = tokenCall(form)
                saveTokens(o, scopeStr)
                lastAccount()!!
            }.addOnSuccessListener { acc -> callback(acc, 0, null) }
                .addOnFailureListener { e ->
                    val net = !PlatformApi.isOnline()
                    callback(null, if (net) 7 else 8, e.message)
                }
        }
    }

    private fun tokenCall(form: String): JsonObject {
        val res = PlatformApi.httpExecute(
            "POST", "https://oauth2.googleapis.com/token",
            mapOf("Content-Type" to "application/x-www-form-urlencoded"),
            form.encodeToByteArray(), 30.0
        )
        if (res.code == 0) throw java.io.IOException("network error")
        val o = Json.parseToJsonElement(res.text()).jsonObject
        if (!res.isSuccessful) {
            val err = o["error"]?.jsonPrimitive?.contentOrNull ?: "error"
            throw OAuthException(err, res.text())
        }
        return o
    }

    private fun saveTokens(o: JsonObject, scopes: String?) {
        fun s(k: String) = o[k]?.jsonPrimitive?.contentOrNull
        s("access_token")?.let { put("accessToken", it) }
        s("refresh_token")?.let { put("refreshToken", it) }
        val exp = s("expires_in")?.toLongOrNull() ?: 3600L
        put("accessExpiry", PlatformApi.currentTimeMillis() + (exp - 60) * 1000L)
        val granted = s("scope") ?: scopes
        if (granted != null) put("scopes", granted)
        val idToken = s("id_token")
        if (idToken != null) {
            put("idToken", idToken)
            try {
                val payload = idToken.split('.')[1]
                val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
                val claims = Json.parseToJsonElement(Base64.UrlSafe.decode(padded).decodeToString()).jsonObject
                claims["email"]?.jsonPrimitive?.contentOrNull?.let { put("email", it) }
                claims["name"]?.jsonPrimitive?.contentOrNull?.let { put("name", it) }
                claims["sub"]?.jsonPrimitive?.contentOrNull?.let { put("sub", it) }
            } catch (_: Exception) {}
        }
    }

    class OAuthException(val error: String, body: String) : Exception("$error: $body")

    private fun consentIntent(scope: String) =
        Intent(SIGN_IN_ACTION).putExtra("scopes", "$DEFAULT_SCOPES $scope")

    /** رمز وصول صالح (عملية مانعة) — أو استثناء يطلب موافقة المستخدم */
    fun accessTokenBlocking(scope: String): String {
        val granted = (get("scopes") ?: "").split(' ')
        if (scope.isNotBlank() && scope !in granted && get("refreshToken") == null) {
            throw UserRecoverableAuthException("consent required", consentIntent(scope))
        }
        val token = get("accessToken")
        val expiry = (PlatformApi.prefGet(STORE, "accessExpiry") as? Number)?.toLong() ?: 0L
        if (token != null && PlatformApi.currentTimeMillis() < expiry && (scope.isBlank() || scope in granted)) return token
        val rt = get("refreshToken") ?: throw UserRecoverableAuthException("consent required", consentIntent(scope))
        val clientId = FirebaseConfig.iosClientId ?: throw java.io.IOException("missing client id")
        try {
            val o = tokenCall(
                "client_id=" + android.net.Uri.encode(clientId) +
                    "&refresh_token=" + android.net.Uri.encode(rt) +
                    "&grant_type=refresh_token"
            )
            saveTokens(o, null)
        } catch (e: OAuthException) {
            if (e.error == "invalid_grant") {
                put("refreshToken", null); put("accessToken", null)
                throw UserRecoverableAuthException("consent required", consentIntent(scope))
            }
            throw java.io.IOException(e.message)
        }
        val newGranted = (get("scopes") ?: "").split(' ')
        if (scope.isNotBlank() && scope !in newGranted) {
            throw UserRecoverableAuthException("consent required", consentIntent(scope))
        }
        return get("accessToken") ?: throw java.io.IOException("no access token")
    }

    fun invalidateAccessToken(token: String?) {
        if (token != null && token == get("accessToken")) {
            put("accessToken", null); put("accessExpiry", 0L)
        }
    }
}
