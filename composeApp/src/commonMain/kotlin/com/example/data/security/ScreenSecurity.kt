package com.example.data.security

import com.example.compat.*
import kotlinx.coroutines.IO

import com.google.firebase.auth.FirebaseAuth

/**
 * منع لقطات الشاشة وتسجيلها (FLAG_SECURE) يبقى مفعّلًا للجميع،
 * ويُستثنى منه حساب مدير الأقسام وحده.
 */
object ScreenSecurity {
    private const val CAPTURE_ALLOWED_EMAIL = "elyedalimoctar@gmail.com"

    /** هل المستخدم الحالي هو مدير الأقسام المسموح له بالتقاط الشاشة وتسجيلها؟ */
    fun isCaptureAllowedForCurrentUser(): Boolean {
        return try {
            val email = FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase()
            email == CAPTURE_ALLOWED_EMAIL
        } catch (e: Exception) {
            false
        }
    }
}
