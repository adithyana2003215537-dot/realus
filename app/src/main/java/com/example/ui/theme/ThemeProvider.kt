package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Calendar

/**
 * AppThemeMode defines the theme preference of the user.
 * - DAY: High-legibility, warm linen & sunlit daylight theme with tactile paper aesthetics.
 * - NIGHT: Deep midnight obsidian palette with glowing sienna & starry gold.
 * - EVENING: Specially tuned eye-safe amber candlelight comfort mode with reduced blue-light and softened contrast for shared bedroom/bedtime use.
 * - AUTO: Automatically shifts to Day during daytime (7:00 AM - 7:00 PM) and Evening/Night mode after sunset.
 */
enum class AppThemeMode(val displayName: String, val description: String) {
  DAY("Day Mode", "Warm sunlit linen & tactile paper palette for inviting daytime readability"),
  NIGHT("Night Mode", "Deep midnight obsidian with warm sienna highlights"),
  EVENING("Evening Comfort", "Candlelight amber glow designed for cozy bedtime sharing"),
  AUTO("Auto Schedule", "Follows sunrise and sunset for gentle eye comfort")
}

@Immutable
data class AppExtendedColors(
  val background: Color,
  val surface: Color,
  val surfaceDim: Color,
  val surfaceBright: Color,
  val surfaceContainer: Color,
  val surfaceContainerLow: Color,
  val surfaceContainerHigh: Color,
  val surfaceContainerHighest: Color,
  val surfaceOchre: Color,
  val surfaceClay: Color,
  val surfaceCard: Color,
  val primary: Color,
  val primaryDark: Color,
  val onPrimary: Color,
  val primaryContainer: Color,
  val onPrimaryContainer: Color,
  val secondary: Color,
  val secondaryFixed: Color,
  val onSecondary: Color,
  val tertiary: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val textGoldMuted: Color,
  val outline: Color,
  val outlineVariant: Color,
  val glowAmber: Color,
  val error: Color = ErrorRed,
  val isDark: Boolean,
  val isEveningComfort: Boolean = false,
  val mode: AppThemeMode
) {
  val warmButtonBrush: Brush
    get() = if (!isDark) {
      Brush.horizontalGradient(listOf(Color(0xFFD46222), Color(0xFFB8480F)))
    } else if (isEveningComfort) {
      Brush.horizontalGradient(listOf(Color(0xFFE67D34), Color(0xFFC05917)))
    } else {
      Brush.horizontalGradient(listOf(Color(0xFFFFB68E), Color(0xFFC2652A)))
    }

  val warmActiveChipBrush: Brush
    get() = if (!isDark) {
      Brush.horizontalGradient(listOf(Color(0xFFD46222), Color(0xFFB8480F)))
    } else if (isEveningComfort) {
      Brush.horizontalGradient(listOf(Color(0xFFD46F2B), Color(0xFFA64F18)))
    } else {
      Brush.horizontalGradient(listOf(Color(0xFFC2652A), Color(0xFFA64F18)))
    }

  val warmActiveSurfaceBrush: Brush
    get() = if (!isDark) {
      Brush.verticalGradient(listOf(Color(0xFFFFEBDD), Color(0xFFFFDBC6)))
    } else {
      Brush.verticalGradient(listOf(Color(0xFF382A20), Color(0xFF2A1E16)))
    }
}

// --- Color Palettes ---

// 1. NIGHT MODE (Midnight Warmth)
val NightExtendedColors = AppExtendedColors(
  background = Color(0xFF131313),
  surface = Color(0xFF131313),
  surfaceDim = Color(0xFF0E0E0E),
  surfaceBright = Color(0xFF393939),
  surfaceContainer = Color(0xFF201F1F),
  surfaceContainerLow = Color(0xFF1C1B1B),
  surfaceContainerHigh = Color(0xFF2A2A2A),
  surfaceContainerHighest = Color(0xFF353534),
  surfaceOchre = Color(0xFF2A241F),
  surfaceClay = Color(0xFF3D342E),
  surfaceCard = Color(0xFF1E1D1D),
  primary = Color(0xFFFFB68E),
  primaryDark = Color(0xFFC2652A),
  onPrimary = Color(0xFF542200),
  primaryContainer = Color(0xFFD9773A),
  onPrimaryContainer = Color(0xFF491C00),
  secondary = Color(0xFFE9C349),
  secondaryFixed = Color(0xFFFFE088),
  onSecondary = Color(0xFF241A00),
  tertiary = Color(0xFF70D2FA),
  textPrimary = Color(0xFFF5F5F5),
  textSecondary = Color(0xFFE5E2E1),
  textMuted = Color(0xFFB5A499),
  textGoldMuted = Color(0xFFBFA17F),
  outline = Color(0xFFA38C80),
  outlineVariant = Color(0xFF554339),
  glowAmber = Color(0xFFFF9E44),
  isDark = true,
  isEveningComfort = false,
  mode = AppThemeMode.NIGHT
)

// 2. DAY MODE (Sunlit Warm Linen & Tactile Paper)
val DayExtendedColors = AppExtendedColors(
  background = Color(0xFFFAF6EE),
  surface = Color(0xFFFAF6EE),
  surfaceDim = Color(0xFFECE3D4),
  surfaceBright = Color(0xFFFFFFFF),
  surfaceContainer = Color(0xFFF3ECE0),
  surfaceContainerLow = Color(0xFFF8F3EB),
  surfaceContainerHigh = Color(0xFFEDE3D5),
  surfaceContainerHighest = Color(0xFFE4D7C7),
  surfaceOchre = Color(0xFFF6EBDC),
  surfaceClay = Color(0xFFEFE2D2),
  surfaceCard = Color(0xFFFFFFFF),
  primary = Color(0xFFCD581C),
  primaryDark = Color(0xFFA1400D),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0xFFFFDFCE),
  onPrimaryContainer = Color(0xFF3E1400),
  secondary = Color(0xFFC28100),
  secondaryFixed = Color(0xFFFFDF88),
  onSecondary = Color(0xFFFFFFFF),
  tertiary = Color(0xFF0F6E8F),
  textPrimary = Color(0xFF281A12),
  textSecondary = Color(0xFF4E3A2F),
  textMuted = Color(0xFF7D6859),
  textGoldMuted = Color(0xFF96734E),
  outline = Color(0xFF968072),
  outlineVariant = Color(0xFFDCCFC3),
  glowAmber = Color(0xFFE8751A),
  isDark = false,
  isEveningComfort = false,
  mode = AppThemeMode.DAY
)

// 3. EVENING COMFORT MODE (Candlelight Glow - Bedroom Shared Mode)
val EveningExtendedColors = AppExtendedColors(
  background = Color(0xFF140F0B),
  surface = Color(0xFF140F0B),
  surfaceDim = Color(0xFF0F0B08),
  surfaceBright = Color(0xFF38291D),
  surfaceContainer = Color(0xFF221A13),
  surfaceContainerLow = Color(0xFF1A130E),
  surfaceContainerHigh = Color(0xFF2C2119),
  surfaceContainerHighest = Color(0xFF382A20),
  surfaceOchre = Color(0xFF332213),
  surfaceClay = Color(0xFF422C1A),
  surfaceCard = Color(0xFF1F1711),
  primary = Color(0xFFFFAD7A),
  primaryDark = Color(0xFFD46F2B),
  onPrimary = Color(0xFF441800),
  primaryContainer = Color(0xFFA64F18),
  onPrimaryContainer = Color(0xFFFFDBCA),
  secondary = Color(0xFFFFC947),
  secondaryFixed = Color(0xFFFFD770),
  onSecondary = Color(0xFF2E2000),
  tertiary = Color(0xFF7AD6FF),
  textPrimary = Color(0xFFF9EFE7),
  textSecondary = Color(0xFFE2D1C4),
  textMuted = Color(0xFFC7A991),
  textGoldMuted = Color(0xFFD4B188),
  outline = Color(0xFF9E7E69),
  outlineVariant = Color(0xFF563E2F),
  glowAmber = Color(0xFFFF8F28),
  isDark = true,
  isEveningComfort = true,
  mode = AppThemeMode.EVENING
)

// Material 3 Color Schemes
val DayM3ColorScheme: ColorScheme = lightColorScheme(
  primary = DayExtendedColors.primary,
  onPrimary = DayExtendedColors.onPrimary,
  primaryContainer = DayExtendedColors.primaryContainer,
  onPrimaryContainer = DayExtendedColors.onPrimaryContainer,
  inversePrimary = DayExtendedColors.primaryDark,
  secondary = DayExtendedColors.secondary,
  onSecondary = DayExtendedColors.onSecondary,
  secondaryContainer = Color(0xFFFFE088),
  onSecondaryContainer = Color(0xFF241A00),
  tertiary = DayExtendedColors.tertiary,
  onTertiary = Color.White,
  background = DayExtendedColors.background,
  onBackground = DayExtendedColors.textPrimary,
  surface = DayExtendedColors.surface,
  onSurface = DayExtendedColors.textPrimary,
  surfaceVariant = DayExtendedColors.surfaceContainerHigh,
  onSurfaceVariant = DayExtendedColors.textSecondary,
  surfaceContainer = DayExtendedColors.surfaceContainer,
  surfaceContainerHigh = DayExtendedColors.surfaceContainerHigh,
  surfaceContainerHighest = DayExtendedColors.surfaceContainerHighest,
  surfaceContainerLow = DayExtendedColors.surfaceContainerLow,
  surfaceContainerLowest = DayExtendedColors.surfaceDim,
  outline = DayExtendedColors.outline,
  outlineVariant = DayExtendedColors.outlineVariant,
  error = Color(0xFFBA1A1A),
  onError = Color.White
)

val NightM3ColorScheme: ColorScheme = darkColorScheme(
  primary = NightExtendedColors.primary,
  onPrimary = NightExtendedColors.onPrimary,
  primaryContainer = NightExtendedColors.primaryContainer,
  onPrimaryContainer = NightExtendedColors.onPrimaryContainer,
  inversePrimary = NightExtendedColors.primaryDark,
  secondary = NightExtendedColors.secondary,
  onSecondary = NightExtendedColors.onSecondary,
  secondaryContainer = Color(0xFFAF8D11),
  onSecondaryContainer = Color(0xFF342800),
  tertiary = NightExtendedColors.tertiary,
  onTertiary = Color(0xFF002E3D),
  background = NightExtendedColors.background,
  onBackground = NightExtendedColors.textPrimary,
  surface = NightExtendedColors.surface,
  onSurface = NightExtendedColors.textPrimary,
  surfaceVariant = NightExtendedColors.surfaceContainerHigh,
  onSurfaceVariant = NightExtendedColors.textSecondary,
  surfaceContainer = NightExtendedColors.surfaceContainer,
  surfaceContainerHigh = NightExtendedColors.surfaceContainerHigh,
  surfaceContainerHighest = NightExtendedColors.surfaceContainerHighest,
  surfaceContainerLow = NightExtendedColors.surfaceContainerLow,
  surfaceContainerLowest = NightExtendedColors.surfaceDim,
  outline = NightExtendedColors.outline,
  outlineVariant = NightExtendedColors.outlineVariant,
  error = ErrorRed,
  onError = Color(0xFF690005)
)

val EveningM3ColorScheme: ColorScheme = darkColorScheme(
  primary = EveningExtendedColors.primary,
  onPrimary = EveningExtendedColors.onPrimary,
  primaryContainer = EveningExtendedColors.primaryContainer,
  onPrimaryContainer = EveningExtendedColors.onPrimaryContainer,
  inversePrimary = EveningExtendedColors.primaryDark,
  secondary = EveningExtendedColors.secondary,
  onSecondary = EveningExtendedColors.onSecondary,
  secondaryContainer = Color(0xFF9E7B10),
  onSecondaryContainer = Color(0xFFFFEDB5),
  tertiary = EveningExtendedColors.tertiary,
  onTertiary = Color(0xFF002E3D),
  background = EveningExtendedColors.background,
  onBackground = EveningExtendedColors.textPrimary,
  surface = EveningExtendedColors.surface,
  onSurface = EveningExtendedColors.textPrimary,
  surfaceVariant = EveningExtendedColors.surfaceContainerHigh,
  onSurfaceVariant = EveningExtendedColors.textSecondary,
  surfaceContainer = EveningExtendedColors.surfaceContainer,
  surfaceContainerHigh = EveningExtendedColors.surfaceContainerHigh,
  surfaceContainerHighest = EveningExtendedColors.surfaceContainerHighest,
  surfaceContainerLow = EveningExtendedColors.surfaceContainerLow,
  surfaceContainerLowest = EveningExtendedColors.surfaceDim,
  outline = EveningExtendedColors.outline,
  outlineVariant = EveningExtendedColors.outlineVariant,
  error = ErrorRed,
  onError = Color(0xFF690005)
)

// Theme Controller Interface
interface ThemeController {
  val currentMode: AppThemeMode
  val isNight: Boolean
  val isEveningComfort: Boolean
  fun setMode(mode: AppThemeMode)
  fun toggleDayNight()
}

val LocalAppExtendedColors = staticCompositionLocalOf { NightExtendedColors }
val LocalThemeMode = compositionLocalOf { AppThemeMode.NIGHT }
val LocalThemeController = staticCompositionLocalOf<ThemeController> {
  object : ThemeController {
    override val currentMode: AppThemeMode = AppThemeMode.NIGHT
    override val isNight: Boolean = true
    override val isEveningComfort: Boolean = false
    override fun setMode(mode: AppThemeMode) {}
    override fun toggleDayNight() {}
  }
}

/**
 * Convenience accessor for the active theme's extended colors
 */
object AppTheme {
  val colors: AppExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppExtendedColors.current

  val mode: AppThemeMode
    @Composable
    @ReadOnlyComposable
    get() = LocalThemeMode.current

  val controller: ThemeController
    @Composable
    @ReadOnlyComposable
    get() = LocalThemeController.current
}

/**
 * Returns whether current time is considered evening / night (7 PM to 7 AM)
 */
fun isCurrentlyEveningOrNight(): Boolean {
  val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
  return hour >= 19 || hour < 7
}

/**
 * Helper to animate colors smoothly when switching modes
 */
@Composable
fun rememberAnimatedExtendedColors(targetColors: AppExtendedColors): AppExtendedColors {
  val bg = animateColorAsState(targetColors.background, tween(400), label = "anim_bg").value
  val surface = animateColorAsState(targetColors.surface, tween(400), label = "anim_surface").value
  val surfaceDim = animateColorAsState(targetColors.surfaceDim, tween(400), label = "anim_surfaceDim").value
  val surfaceBright = animateColorAsState(targetColors.surfaceBright, tween(400), label = "anim_surfaceBright").value
  val surfaceContainer = animateColorAsState(targetColors.surfaceContainer, tween(400), label = "anim_container").value
  val surfaceContainerLow = animateColorAsState(targetColors.surfaceContainerLow, tween(400), label = "anim_c_low").value
  val surfaceContainerHigh = animateColorAsState(targetColors.surfaceContainerHigh, tween(400), label = "anim_c_high").value
  val surfaceContainerHighest = animateColorAsState(targetColors.surfaceContainerHighest, tween(400), label = "anim_c_highest").value
  val surfaceOchre = animateColorAsState(targetColors.surfaceOchre, tween(400), label = "anim_ochre").value
  val surfaceClay = animateColorAsState(targetColors.surfaceClay, tween(400), label = "anim_clay").value
  val surfaceCard = animateColorAsState(targetColors.surfaceCard, tween(400), label = "anim_card").value
  val primary = animateColorAsState(targetColors.primary, tween(400), label = "anim_primary").value
  val primaryDark = animateColorAsState(targetColors.primaryDark, tween(400), label = "anim_primaryDark").value
  val onPrimary = animateColorAsState(targetColors.onPrimary, tween(400), label = "anim_onPrimary").value
  val primaryContainer = animateColorAsState(targetColors.primaryContainer, tween(400), label = "anim_p_container").value
  val onPrimaryContainer = animateColorAsState(targetColors.onPrimaryContainer, tween(400), label = "anim_on_p_container").value
  val secondary = animateColorAsState(targetColors.secondary, tween(400), label = "anim_secondary").value
  val secondaryFixed = animateColorAsState(targetColors.secondaryFixed, tween(400), label = "anim_sec_fixed").value
  val onSecondary = animateColorAsState(targetColors.onSecondary, tween(400), label = "anim_onSec").value
  val tertiary = animateColorAsState(targetColors.tertiary, tween(400), label = "anim_tertiary").value
  val textPrimary = animateColorAsState(targetColors.textPrimary, tween(400), label = "anim_textPri").value
  val textSecondary = animateColorAsState(targetColors.textSecondary, tween(400), label = "anim_textSec").value
  val textMuted = animateColorAsState(targetColors.textMuted, tween(400), label = "anim_textMuted").value
  val textGoldMuted = animateColorAsState(targetColors.textGoldMuted, tween(400), label = "anim_goldMuted").value
  val outline = animateColorAsState(targetColors.outline, tween(400), label = "anim_outline").value
  val outlineVariant = animateColorAsState(targetColors.outlineVariant, tween(400), label = "anim_outlineVar").value
  val glowAmber = animateColorAsState(targetColors.glowAmber, tween(400), label = "anim_glow").value

  return remember(bg, surface, surfaceContainer, primary, textPrimary, targetColors.mode) {
    AppExtendedColors(
      background = bg,
      surface = surface,
      surfaceDim = surfaceDim,
      surfaceBright = surfaceBright,
      surfaceContainer = surfaceContainer,
      surfaceContainerLow = surfaceContainerLow,
      surfaceContainerHigh = surfaceContainerHigh,
      surfaceContainerHighest = surfaceContainerHighest,
      surfaceOchre = surfaceOchre,
      surfaceClay = surfaceClay,
      surfaceCard = surfaceCard,
      primary = primary,
      primaryDark = primaryDark,
      onPrimary = onPrimary,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      secondary = secondary,
      secondaryFixed = secondaryFixed,
      onSecondary = onSecondary,
      tertiary = tertiary,
      textPrimary = textPrimary,
      textSecondary = textSecondary,
      textMuted = textMuted,
      textGoldMuted = textGoldMuted,
      outline = outline,
      outlineVariant = outlineVariant,
      glowAmber = glowAmber,
      error = targetColors.error,
      isDark = targetColors.isDark,
      isEveningComfort = targetColors.isEveningComfort,
      mode = targetColors.mode
    )
  }
}
