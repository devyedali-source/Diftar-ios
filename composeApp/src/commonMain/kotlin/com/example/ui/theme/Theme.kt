package com.example.ui.theme

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BentoPrimary,
    secondary = BentoSecondary,
    tertiary = BentoSolidViolet,
    background = BentoBg,
    surface = BentoLightLavender,
    onBackground = BentoText,
    onSurface = BentoText
  )


private val LightColorScheme =
  lightColorScheme(
    primary = BentoPrimary,
    onPrimary = Color.White,
    secondary = BentoSecondary,
    onSecondary = Color.White,
    primaryContainer = BentoSoftPurpleContainer,
    onPrimaryContainer = BentoSecondary,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoSecondary,
    tertiary = BentoSolidViolet,
    background = BentoBg,
    surface = BentoLightLavender,
    onBackground = BentoText,
    onSurface = BentoText,
    surfaceVariant = BentoLightLavender,
    onSurfaceVariant = BentoPrimaryDesc,
    outline = BentoGrayOutline
  )


@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val family = rememberAppFontFamily()
  AppFonts.family = family
  val typography = androidx.compose.runtime.remember(family) { Typography.withFamily(family) }
  MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
}
