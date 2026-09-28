package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.resources.Res
import com.example.resources.noto_naskh_arabic
import org.jetbrains.compose.resources.Font

/**
 * خطّ أندرويد العربي (Noto Naskh Arabic) ليبدو التطبيق على الآيفون كما هو على أندرويد.
 * الحروف اللاتينية غير الموجودة فيه تُعرض بخطّ النظام.
 */
object AppFonts {
    var family: FontFamily = FontFamily.Default
        internal set
}

@Composable
internal fun rememberAppFontFamily(): FontFamily {
    val weights = listOf(
        FontWeight.Light, FontWeight.Normal, FontWeight.Medium,
        FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold, FontWeight.Black
    )
    val fonts = weights.map { w ->
        Font(
            Res.font.noto_naskh_arabic,
            weight = w,
            variationSettings = FontVariation.Settings(FontVariation.weight(w.weight.coerceIn(400, 700)))
        )
    }
    return remember(fonts) { FontFamily(fonts) }
}

internal fun Typography.withFamily(family: FontFamily): Typography {
    fun TextStyle.f() = copy(fontFamily = family)
    return copy(
        displayLarge = displayLarge.f(), displayMedium = displayMedium.f(), displaySmall = displaySmall.f(),
        headlineLarge = headlineLarge.f(), headlineMedium = headlineMedium.f(), headlineSmall = headlineSmall.f(),
        titleLarge = titleLarge.f(), titleMedium = titleMedium.f(), titleSmall = titleSmall.f(),
        bodyLarge = bodyLarge.f(), bodyMedium = bodyMedium.f(), bodySmall = bodySmall.f(),
        labelLarge = labelLarge.f(), labelMedium = labelMedium.f(), labelSmall = labelSmall.f()
    )
}
