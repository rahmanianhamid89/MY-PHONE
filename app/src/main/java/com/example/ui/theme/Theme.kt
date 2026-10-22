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

private val GreenLightColorScheme =
  lightColorScheme(
    primary = ThemeGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = Color(0xFF14532D),
    secondary = Color(0xFF22C55E),
    background = Color(0xFFF0FDF4),
    surface = Color(0xFFF7FEF9),
    surfaceVariant = Color(0xFFE8F5EC),
    outline = Color(0xFFBBF7D0),
    outlineVariant = SlateLightBorder.copy(alpha = 0.5f)
  )

private val GreenDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF4ADE80),
    onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0xFF166534),
    onPrimaryContainer = Color(0xFFDCFCE7),
    secondary = Color(0xFF22C55E),
    background = Color(0xFF0A1F12),
    surface = Color(0xFF10261A),
    surfaceVariant = Color(0xFF183524),
    outline = Color(0xFF28543A),
    outlineVariant = SlateNavyBorder.copy(alpha = 0.5f)
  )

private val PurpleLightColorScheme =
  lightColorScheme(
    primary = ThemePurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFFA855F7),
    background = Color(0xFFF5F3FF),
    surface = Color(0xFFFAF9FF),
    surfaceVariant = Color(0xFFEDE9FE),
    outline = Color(0xFFDDD6FE),
    outlineVariant = SlateLightBorder.copy(alpha = 0.5f)
  )

private val PurpleDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF2E1065),
    primaryContainer = Color(0xFF5B21B6),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Color(0xFFC084FC),
    background = Color(0xFF160D2B),
    surface = Color(0xFF20133D),
    surfaceVariant = Color(0xFF302052),
    outline = Color(0xFF58408A),
    outlineVariant = SlateNavyBorder.copy(alpha = 0.5f)
  )

private val OrangeLightColorScheme =
  lightColorScheme(
    primary = ThemeOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = Color(0xFF7C2D12),
    secondary = Color(0xFFF97316),
    background = Color(0xFFFFF7ED),
    surface = Color(0xFFFFFBF5),
    surfaceVariant = Color(0xFFFFEDD5),
    outline = Color(0xFFFED7AA),
    outlineVariant = SlateLightBorder.copy(alpha = 0.5f)
  )

private val OrangeDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFFB923C),
    onPrimary = Color(0xFF431407),
    primaryContainer = Color(0xFFC2410C),
    onPrimaryContainer = Color(0xFFFFEDD5),
    secondary = Color(0xFFF97316),
    background = Color(0xFF1B1208),
    surface = Color(0xFF24180C),
    surfaceVariant = Color(0xFF352311),
    outline = Color(0xFF6B4524),
    outlineVariant = SlateNavyBorder.copy(alpha = 0.5f)
  )

private val RoseLightColorScheme =
  lightColorScheme(
    primary = ThemeRose,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFF881337),
    secondary = Color(0xFFF43F5E),
    background = Color(0xFFFFF1F2),
    surface = Color(0xFFFFF8F8),
    surfaceVariant = Color(0xFFFFE4E6),
    outline = Color(0xFFFECDD3),
    outlineVariant = SlateLightBorder.copy(alpha = 0.5f)
  )

private val RoseDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFFB7185),
    onPrimary = Color(0xFF4C0519),
    primaryContainer = Color(0xFFBE123C),
    onPrimaryContainer = Color(0xFFFFE4E6),
    secondary = Color(0xFFF43F5E),
    background = Color(0xFF210D12),
    surface = Color(0xFF2B1118),
    surfaceVariant = Color(0xFF3A1720),
    outline = Color(0xFF692733),
    outlineVariant = SlateNavyBorder.copy(alpha = 0.5f)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  themeColor: Int = 0,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeColor) {
    1 -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
    2 -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
    3 -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
    4 -> if (darkTheme) RoseDarkColorScheme else RoseLightColorScheme
    else -> if (darkTheme) DarkColorScheme else LightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
