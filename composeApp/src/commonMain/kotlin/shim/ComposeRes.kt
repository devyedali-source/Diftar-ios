@file:Suppress("unused", "PackageDirectoryMismatch")

package androidx.compose.ui.res

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun painterResource(id: Int): Painter = org.jetbrains.compose.resources.painterResource(com.example.R.drawableResource(id))
