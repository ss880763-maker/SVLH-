package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = VibrantSecondary,
    secondary = VibrantPrimary,
    tertiary = VibrantSecondary,
    background = Color(0xFF141218),
    surface = Color(0xFF1D1B20),
    onPrimary = VibrantOnSecondary,
    onSecondary = VibrantOnPrimary,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = VibrantPrimary,
    onPrimary = VibrantOnPrimary,
    secondary = VibrantSecondary,
    onSecondary = VibrantOnSecondary,
    primaryContainer = VibrantPrimaryContainer,
    onPrimaryContainer = VibrantOnPrimaryContainer,
    background = VibrantBackground,
    onBackground = VibrantOnBackground,
    surface = VibrantSurface,
    onSurface = VibrantOnSurface,
    error = VibrantError,
    onError = VibrantOnError,
    outline = VibrantOutline,
    surfaceVariant = VibrantSurfaceVariant,
    onSurfaceVariant = VibrantOnSurfaceVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to maintain custom design brand look
  dynamicColor: Boolean = false,
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
