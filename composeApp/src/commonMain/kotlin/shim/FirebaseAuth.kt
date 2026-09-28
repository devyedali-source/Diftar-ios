@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package com.google.firebase.auth

import com.example.compat.FirebaseConfig
import com.example.compat.PlatformApi
import com.example.compat.Void
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.backgroundTask
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

open class FirebaseAuthException(val errorCode: String, message: String?) : FirebaseException(message)
class FirebaseAuthInvalidUserException(code: String, message: String?) : FirebaseAuthException(code, message)
open class FirebaseAuthInvalidCredentialsException(code: String, message: String?) : FirebaseAuthException(code, message)
class FirebaseAuthWeakPasswordException(code: String, message: String?) : FirebaseAuthInvalidCredentialsException(code, message) {
    val reason: String? get() = message
}
class FirebaseAuthUserCollisionException(code: String, message: String?) : FirebaseAuthException(code, message)
class FirebaseAuthRecentLoginRequiredException(code: String, message: String?) : FirebaseAuthException(code, message)

open class AuthCredential(val provider: String)
class GoogleAuthCredential(val idToken: String?, val accessToken: String?) : AuthCredential("google.com")

object GoogleAuthProvider {
    const val PROVIDER_ID = "google.com"
    fun getCredential(idToken: String?, accessToken: String?): AuthCredential = GoogleAuthCredential(idToken, accessToken)
}

class UserProfileChangeRequest(val displayName: String?, val photoUri: android.net.Uri?) {
    class Builder {
        var displayName: String? = null
        var photoUri: android.net.Uri? = null
        fun setDisplayName(name: String?) = apply { displayName = name }
        fun setPhotoUri(uri: android.net.Uri?) = apply { photoUri = uri }
        fun build() = UserProfileChangeRequest(displayName, photoUri)
    }
}

fun userProfileChangeRequest(init: UserProfileChangeRequest.Builder.() -> Unit): UserProfileChangeRequest =
    UserProfileChangeRequest.Builder().apply(init).build()

class GetTokenResult(val token: String?)

class AuthResult(val user: FirebaseUser?)

class FirebaseUser internal constructor(
    val uid: String,
    email: String?,
    displayName: String?,
    photoUrl: String?
) {
    var email: String? = email
        internal set
    var displayName: String? = displayName
        internal set
    private var photo: String? = photoUrl
    val photoUrl: android.net.Uri? get() = photo?.let { android.net.Uri.parse(it) }
    val isAnonymous: Boolean get() = false
    val isEmailVerified: Boolean get() = true
    val providerId: String get() = "firebase"

    fun getIdToken(forceRefresh: Boolean): Task<GetTokenResult> = backgroundTask {
        GetTokenResult(FirebaseAuth.getInstance().freshIdToken(forceRefresh))
    }

    fun updateProfile(request: UserProfileChangeRequest): Task<Void?> = backgroundTask {
        val auth = FirebaseAuth.getInstance()
        val body = buildJsonObject {
            put("idToken", auth.freshIdToken(false))
            request.displayName?.let { put("displayName", it) }
            request.photoUri?.let { put("photoUrl", it.toString()) }
            put("returnSecureToken", true)
        }
        auth.identityCall("accounts:update", body)
        request.displayName?.let { displayName = it }
        request.photoUri?.let { photo = it.toString() }
        auth.persist()
        PlatformApi.runOnMain { auth.notifyListeners() }
        null
    }

    fun delete(): Task<Void?> = backgroundTask {
        val auth = FirebaseAuth.getInstance()
        auth.identityCall("accounts:delete", buildJsonObject { put("idToken", auth.freshIdToken(false)) })
        auth.clearSession()
        PlatformApi.runOnMain { auth.notifyListeners() }
        null
    }

    fun reload(): Task<Void?> = backgroundTask { null }
}

/**
 * تسجيل الدخول عبر Firebase Authentication REST API
 * (نفس الحسابات ونفس معرّفات المستخدمين الموجودة في نسخة أندرويد).
 */
class FirebaseAuth private constructor() {

    fun interface AuthStateListener {
        fun onAuthStateChanged(auth: FirebaseAuth)
    }

    private val store = "__firebase_auth"
    private val listeners = mutableListOf<AuthStateListener>()

    private var idToken: String? = null
    private var refreshToken: String? = null
    private var expiresAtMs: Long = 0L

    var currentUser: FirebaseUser? = null
        private set

    init {
        val uid = PlatformApi.prefGet(store, "uid") as? String
        if (!uid.isNullOrBlank()) {
            currentUser = FirebaseUser(
                uid,
                PlatformApi.prefGet(store, "email") as? String,
                PlatformApi.prefGet(store, "displayName") as? String,
                PlatformApi.prefGet(store, "photoUrl") as? String
            )
            idToken = PlatformApi.prefGet(store, "idToken") as? String
            refreshToken = PlatformApi.prefGet(store, "refreshToken") as? String
            expiresAtMs = (PlatformApi.prefGet(store, "expiresAt") as? Number)?.toLong() ?: 0L
        }
    }

    fun addAuthStateListener(listener: AuthStateListener) {
        listeners.add(listener)
        PlatformApi.runOnMain { listener.onAuthStateChanged(this) }
    }

    fun removeAuthStateListener(listener: AuthStateListener) {
        listeners.remove(listener)
    }

    internal fun notifyListeners() {
        listeners.toList().forEach { it.onAuthStateChanged(this) }
    }

    fun signOut() {
        clearSession()
        notifyListeners()
    }

    internal fun clearSession() {
        currentUser = null
        idToken = null
        refreshToken = null
        expiresAtMs = 0L
        PlatformApi.prefClear(store)
    }

    internal fun persist() {
        val u = currentUser
        PlatformApi.prefPut(store, "uid", u?.uid)
        PlatformApi.prefPut(store, "email", u?.email)
        PlatformApi.prefPut(store, "displayName", u?.displayName)
        PlatformApi.prefPut(store, "photoUrl", u?.photoUrl?.toString())
        PlatformApi.prefPut(store, "idToken", idToken)
        PlatformApi.prefPut(store, "refreshToken", refreshToken)
        PlatformApi.prefPut(store, "expiresAt", expiresAtMs)
    }

    fun signInWithCredential(credential: AuthCredential): Task<AuthResult> = backgroundTask {
        val c = credential as? GoogleAuthCredential ?: throw FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "Unsupported credential")
        val postBody = buildString {
            if (c.idToken != null) append("id_token=").append(c.idToken)
            if (c.accessToken != null) {
                if (isNotEmpty()) append('&')
                append("access_token=").append(c.accessToken)
            }
            append("&providerId=google.com")
        }
        val res = identityCall("accounts:signInWithIdp", buildJsonObject {
            put("postBody", postBody)
            put("requestUri", "http://localhost")
            put("returnIdpCredential", true)
            put("returnSecureToken", true)
        })
        applySignIn(res)
        PlatformApi.runOnMain { notifyListeners() }
        AuthResult(currentUser)
    }

    fun signInWithEmailAndPassword(email: String, password: String): Task<AuthResult> = backgroundTask {
        val res = identityCall("accounts:signInWithPassword", buildJsonObject {
            put("email", email); put("password", password); put("returnSecureToken", true)
        })
        applySignIn(res)
        PlatformApi.runOnMain { notifyListeners() }
        AuthResult(currentUser)
    }

    fun createUserWithEmailAndPassword(email: String, password: String): Task<AuthResult> = backgroundTask {
        val res = identityCall("accounts:signUp", buildJsonObject {
            put("email", email); put("password", password); put("returnSecureToken", true)
        })
        applySignIn(res)
        PlatformApi.runOnMain { notifyListeners() }
        AuthResult(currentUser)
    }

    fun sendPasswordResetEmail(email: String): Task<Void?> = backgroundTask {
        identityCall("accounts:sendOobCode", buildJsonObject {
            put("requestType", "PASSWORD_RESET"); put("email", email)
        })
        null
    }

    private fun applySignIn(res: JsonObject) {
        fun s(k: String) = res[k]?.jsonPrimitive?.contentOrNull
        val uid = s("localId") ?: throw FirebaseAuthException("ERROR_INTERNAL", "No user id")
        idToken = s("idToken")
        refreshToken = s("refreshToken")
        expiresAtMs = PlatformApi.currentTimeMillis() + ((s("expiresIn")?.toLongOrNull() ?: 3600L) - 60) * 1000L
        currentUser = FirebaseUser(uid, s("email"), s("displayName") ?: s("fullName"), s("photoUrl"))
        persist()
    }

    /** رمز الهوية الحالي، مع تجديده عند انتهاء صلاحيته (عملية مانعة: تُستدعى من الخلفية) */
    internal fun freshIdToken(force: Boolean): String {
        val now = PlatformApi.currentTimeMillis()
        val token = idToken
        if (!force && token != null && now < expiresAtMs) return token
        val rt = refreshToken ?: throw FirebaseAuthInvalidUserException("ERROR_USER_TOKEN_EXPIRED", "Not signed in")
        val form = "grant_type=refresh_token&refresh_token=" + android.net.Uri.encode(rt)
        val res = try {
            PlatformApi.httpExecute(
                "POST",
                "https://securetoken.googleapis.com/v1/token?key=${FirebaseConfig.apiKey}",
                FirebaseConfig.apiHeaders() + mapOf("Content-Type" to "application/x-www-form-urlencoded"),
                form.encodeToByteArray(),
                30.0
            )
        } catch (e: Exception) {
            throw FirebaseNetworkException(e.message)
        }
        if (res.code == 0) throw FirebaseNetworkException("network error")
        if (!res.isSuccessful) throw mapError(res.text())
        val o = Json.parseToJsonElement(res.text()).jsonObject
        idToken = o["id_token"]?.jsonPrimitive?.contentOrNull
        refreshToken = o["refresh_token"]?.jsonPrimitive?.contentOrNull ?: rt
        expiresAtMs = now + ((o["expires_in"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 3600L) - 60) * 1000L
        persist()
        return idToken ?: throw FirebaseAuthException("ERROR_INTERNAL", "Token refresh failed")
    }

    /** رمز الهوية إن وُجد مستخدم، وإلا null (لطلبات Firestore) */
    fun idTokenOrNull(): String? = if (currentUser == null) null else try { freshIdToken(false) } catch (e: Exception) { null }

    internal fun identityCall(method: String, body: JsonObject): JsonObject {
        val res = try {
            PlatformApi.httpExecute(
                "POST",
                "https://identitytoolkit.googleapis.com/v1/$method?key=${FirebaseConfig.apiKey}",
                FirebaseConfig.apiHeaders() + mapOf("Content-Type" to "application/json"),
                body.toString().encodeToByteArray(),
                30.0
            )
        } catch (e: Exception) {
            throw FirebaseNetworkException(e.message)
        }
        if (res.code == 0) throw FirebaseNetworkException("A network error has occurred")
        if (!res.isSuccessful) throw mapError(res.text())
        return Json.parseToJsonElement(res.text()).jsonObject
    }

    private fun mapError(text: String): Exception {
        val msg = try {
            Json.parseToJsonElement(text).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) { null } ?: text
        val code = msg.substringBefore(' ').substringBefore(':')
        return when {
            code.startsWith("USER_NOT_FOUND") || code.startsWith("USER_DISABLED") || code.startsWith("TOKEN_EXPIRED") || code.startsWith("INVALID_REFRESH_TOKEN") ->
                FirebaseAuthInvalidUserException("ERROR_$code", msg)
            code.startsWith("INVALID") || code.startsWith("MISSING") || code.startsWith("WEAK_PASSWORD") ->
                FirebaseAuthInvalidCredentialsException("ERROR_$code", msg)
            code.startsWith("EMAIL_EXISTS") || code.startsWith("FEDERATED_USER_ID_ALREADY_LINKED") ->
                FirebaseAuthUserCollisionException("ERROR_$code", msg)
            code.startsWith("CREDENTIAL_TOO_OLD") ->
                FirebaseAuthRecentLoginRequiredException("ERROR_$code", msg)
            else -> FirebaseAuthException("ERROR_$code", msg)
        }
    }

    companion object {
        private val instance by lazy { FirebaseAuth() }
        fun getInstance(): FirebaseAuth = instance
    }
}

/** للاستعمال بصيغة Firebase.auth */
val com.google.firebase.FirebaseApp.auth: FirebaseAuth get() = FirebaseAuth.getInstance()
