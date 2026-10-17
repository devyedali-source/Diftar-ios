package com.example.data.net

import com.example.compat.*
import kotlinx.coroutines.IO

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * أدوات مساعدة للشبكات الضعيفة: قراءة/تحديث وثيقة Firestore مع مهلة لكل محاولة وإعادة محاولة تلقائية.
 * لا تغيّر أي سلوك آخر في التطبيق؛ تُستعمل فقط في شاشات طلبات التفعيل والموافقات.
 */
object FirestoreRetry {

    /** يجلب الوثيقة. يعيد null إذا فشلت كل المحاولات (شبكة ضعيفة)، ويعيد وثيقة (قد لا تكون موجودة) عند الوصول للخادم. */
    suspend fun getDoc(
        ref: DocumentReference,
        attempts: Int = 4,
        perTryMs: Long = 15_000L
    ): DocumentSnapshot? {
        var i = 0
        while (i < attempts) {
            i++
            val snap: DocumentSnapshot? = withTimeoutOrNull(perTryMs) {
                suspendCancellableCoroutine<DocumentSnapshot?> { cont ->
                    ref.get()
                        .addOnSuccessListener { if (cont.isActive) cont.resumeWith(Result.success(it)) }
                        .addOnFailureListener { if (cont.isActive) cont.resumeWith(Result.success(null)) }
                }
            }
            if (snap != null) return snap
            if (i < attempts) delay(1_500L * i)
        }
        return null
    }

    /** يحدّث حقولًا في الوثيقة ويعيد true فقط عند تأكيد الخادم. التحديثات المستعملة هنا آمنة للتكرار. */
    suspend fun updateDoc(
        ref: DocumentReference,
        data: Map<String, Any>,
        attempts: Int = 3,
        perTryMs: Long = 20_000L
    ): Boolean {
        var i = 0
        while (i < attempts) {
            i++
            val ok: Boolean = withTimeoutOrNull(perTryMs) {
                suspendCancellableCoroutine<Boolean> { cont ->
                    ref.update(data)
                        .addOnSuccessListener { if (cont.isActive) cont.resumeWith(Result.success(true)) }
                        .addOnFailureListener { if (cont.isActive) cont.resumeWith(Result.success(false)) }
                }
            } ?: false
            if (ok) return true
            if (i < attempts) delay(1_500L * i)
        }
        return false
    }
}
