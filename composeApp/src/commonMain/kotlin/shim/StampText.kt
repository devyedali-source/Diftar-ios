package com.example.compat

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/** يرسم نص الختم الدائري (أعلى وأسفل الحلقة) بخطّ عربي مُشكَّل */
expect fun drawStampRingText(
    scope: DrawScope,
    topText: String,
    bottomText: String,
    cx: Float,
    cy: Float,
    radius: Float,
    density: Float,
    color: Color
)
