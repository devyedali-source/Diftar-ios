@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.compat

import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

actual object DeviceAuth {
    actual fun canAuthenticate(): Boolean = LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null)

    actual fun authenticate(reason: String, onResult: (Boolean, String?) -> Unit) {
        val ctx = LAContext()
        ctx.evaluatePolicy(LAPolicyDeviceOwnerAuthentication, localizedReason = reason) { ok, error ->
            PlatformApi.runOnMain { onResult(ok, if (ok) null else error?.localizedDescription) }
        }
    }
}
