@file:Suppress("unused", "PackageDirectoryMismatch")

package androidx.compose.ui.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** بديل LocalContext: سياق التطبيق على الآيفون */
val LocalContext = staticCompositionLocalOf<android.content.Context> { com.example.compat.AppActivity }
