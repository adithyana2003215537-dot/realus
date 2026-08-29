package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
fun RealUsTheme(
  themeName: String = "Midnight Warmth",
  isEveningComfort: Boolean = false,
  onThemeChanged: ((String) -> Unit)? = null,
  content: @Composable () -> Unit
) {
  // Determine effective mode and colors
  val effectiveMode = when {
    themeName.equals("Day", ignoreCase = true) || themeName.equals("Sunlit Warmth", ignoreCase = true) -> AppThemeMode.DAY
    themeName.equals("Evening", ignoreCase = true) || themeName.equals("Evening Comfort", ignoreCase = true) -> AppThemeMode.EVENING
    themeName.equals("Auto", ignoreCase = true) -> if (isCurrentlyEveningOrNight()) AppThemeMode.EVENING else AppThemeMode.DAY
    isEveningComfort -> AppThemeMode.EVENING
    else -> AppThemeMode.NIGHT
  }

  val targetExtendedColors: AppExtendedColors = when (effectiveMode) {
    AppThemeMode.DAY -> DayExtendedColors
    AppThemeMode.EVENING -> EveningExtendedColors
    AppThemeMode.NIGHT -> NightExtendedColors
    AppThemeMode.AUTO -> if (isCurrentlyEveningOrNight()) EveningExtendedColors else DayExtendedColors
  }

  // Animated extended colors for smooth transitions
  val animatedColors = rememberAnimatedExtendedColors(targetExtendedColors)

  val colorScheme: ColorScheme = when (effectiveMode) {
    AppThemeMode.DAY -> DayM3ColorScheme
    AppThemeMode.EVENING -> EveningM3ColorScheme
    AppThemeMode.NIGHT -> NightM3ColorScheme
    AppThemeMode.AUTO -> if (isCurrentlyEveningOrNight()) EveningM3ColorScheme else DayM3ColorScheme
  }

  val controller = remember(effectiveMode, onThemeChanged) {
    object : ThemeController {
      override val currentMode: AppThemeMode = effectiveMode
      override val isNight: Boolean = effectiveMode != AppThemeMode.DAY
      override val isEveningComfort: Boolean = effectiveMode == AppThemeMode.EVENING

      override fun setMode(mode: AppThemeMode) {
        val targetName = when (mode) {
          AppThemeMode.DAY -> "Day"
          AppThemeMode.NIGHT -> "Night"
          AppThemeMode.EVENING -> "Evening Comfort"
          AppThemeMode.AUTO -> "Auto"
        }
        onThemeChanged?.invoke(targetName)
      }

      override fun toggleDayNight() {
        val nextMode = if (effectiveMode == AppThemeMode.DAY) {
          if (isCurrentlyEveningOrNight()) AppThemeMode.EVENING else AppThemeMode.NIGHT
        } else {
          AppThemeMode.DAY
        }
        setMode(nextMode)
      }
    }
  }

  CompositionLocalProvider(
    LocalAppExtendedColors provides animatedColors,
    LocalThemeMode provides effectiveMode,
    LocalThemeController provides controller
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
