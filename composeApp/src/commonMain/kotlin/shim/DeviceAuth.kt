package com.example.compat

/** قفل التطبيق بالبصمة/الوجه/رمز الجهاز (بديل BiometricPrompt) */
expect object DeviceAuth {
    fun canAuthenticate(): Boolean
    fun authenticate(reason: String, onResult: (Boolean, String?) -> Unit)
}
