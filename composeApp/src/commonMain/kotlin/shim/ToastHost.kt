package com.example.compat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** بديل Toast في أندرويد: رسالة قصيرة أسفل الشاشة */
object ToastHost {
    private data class Msg(val text: String, val long: Boolean, val id: Long)
    private var current by mutableStateOf<Msg?>(null)
    private var counter = 0L

    fun show(text: String, long: Boolean) {
        PlatformApi.runOnMain { current = Msg(text, long, ++counter) }
    }

    @Composable
    fun Overlay() {
        val msg = current
        LaunchedEffect(msg?.id) {
            if (msg != null) {
                delay(if (msg.long) 3500L else 2000L)
                if (current?.id == msg.id) current = null
            }
        }
        Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 64.dp), contentAlignment = Alignment.BottomCenter) {
            AnimatedVisibility(visible = msg != null, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = msg?.text ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 420.dp)
                        .background(Color(0xE6323232), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
