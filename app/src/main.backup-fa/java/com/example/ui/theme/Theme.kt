package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElectricBlueLight,
    onPrimary = SlateNavyDark,
    primaryContainer = ElectricBlue,
    onPrimaryContainer = Color.White,
    secondary = CyanAccent,
    background = SlateNavyDark,
    surface = SlateNavySurface,
    surfaceVariant = SlateNavyCard,
    outline = SlateNavyBorder,
    outlineVariant = SlateNavyBorder.copy(alpha = 0.5f)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = CyanAccent,
    background = SlateLightBg,
    surface = SlateLightSurface,
    surfaceVariant = SlateLightCard,
    outline = SlateLightBorder,
    outlineVariant = SlateLightBorder.copy(alpha = 0.5f)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
