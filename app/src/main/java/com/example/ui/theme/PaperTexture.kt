package com.example.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Modifier that applies a tactile, artisanal paper texture in Day Mode,
 * and a deep smooth ambient background in Evening / Night modes.
 */
fun Modifier.paperBackground(
  isCard: Boolean = false,
  alpha: Float = 1.0f
): Modifier = composed {
  val appColors = AppTheme.colors
  val isDay = !appColors.isDark

  val textureIntensity by animateFloatAsState(
    targetValue = if (isDay) 1.0f else 0.0f,
    animationSpec = tween(durationMillis = 400),
    label = "paper_texture_intensity"
  )

  this
    .background(if (isCard) appColors.surfaceCard else appColors.background)
    .drawWithCache {
      if (textureIntensity <= 0.01f) {
        onDrawBehind {
          // Night / Evening modes: subtle warm radial glow
          if (appColors.isEveningComfort) {
            drawRect(
              brush = Brush.radialGradient(
                colors = listOf(
                  appColors.glowAmber.copy(alpha = 0.06f),
                  Color.Transparent
                ),
                center = Offset(size.width * 0.5f, 0f),
                radius = size.width * 0.9f
              )
            )
          }
        }
      } else {
        val width = size.width
        val height = size.height

        // Precompute deterministic paper fiber flecks & laid paper lines
        // We use a fixed seed based on aspect to keep it consistent across recompositions
        val random = Random(42)
        val numFlecks = if (isCard) 45 else 120
        val fleckPoints = List(numFlecks) {
          Triple(
            Offset(random.nextFloat() * width, random.nextFloat() * height),
            random.nextFloat() * 1.8f + 0.6f, // radius / length
            random.nextFloat() * 0.035f + 0.015f // subtle alpha
          )
        }

        val numFibers = if (isCard) 18 else 40
        val fibers = List(numFibers) {
          val startX = random.nextFloat() * width
          val startY = random.nextFloat() * height
          val length = random.nextFloat() * 24f + 8f
          val angle = (random.nextFloat() * 0.8f - 0.4f) // slight angle
          val endX = startX + length * kotlin.math.cos(angle)
          val endY = startY + length * kotlin.math.sin(angle)
          Pair(Offset(startX, startY), Offset(endX, endY))
        }

        // Laid paper subtle horizontal grain lines (Japanese washi / French laid paper style)
        val lineSpacing = 38f
        val lineCount = (height / lineSpacing).toInt().coerceAtMost(80)

        // Sunlit warm gradient overlay
        val sunlitGradient = Brush.radialGradient(
          colors = listOf(
            Color(0xFFFFF9EE).copy(alpha = 0.55f * textureIntensity * alpha),
            Color(0xFFF6ECE0).copy(alpha = 0.25f * textureIntensity * alpha),
            Color(0xFFEDE0D0).copy(alpha = 0.15f * textureIntensity * alpha)
          ),
          center = Offset(width * 0.25f, 0f),
          radius = width * 1.2f
        )

        val fiberColorWarm = Color(0xFF8B6B52)
        val fiberColorCool = Color(0xFF705E51)
        val grainLineColor = Color(0xFF9E8472).copy(alpha = 0.022f * textureIntensity * alpha)

        onDrawBehind {
          // 1. Draw subtle sunlit warmth gradient
          drawRect(brush = sunlitGradient)

          // 2. Draw micro laid-paper horizontal watermarked grain lines
          for (i in 0..lineCount) {
            val y = i * lineSpacing + (if (i % 2 == 0) 1.5f else 0f)
            drawLine(
              color = grainLineColor,
              start = Offset(0f, y),
              end = Offset(width, y),
              strokeWidth = 0.75f
            )
          }

          // 3. Draw fine organic paper fibers
          fibers.forEachIndexed { index, (start, end) ->
            val fiberColor = if (index % 2 == 0) fiberColorWarm else fiberColorCool
            drawLine(
              color = fiberColor.copy(alpha = 0.028f * textureIntensity * alpha),
              start = start,
              end = end,
              strokeWidth = 1.0f,
              cap = StrokeCap.Round
            )
          }

          // 4. Draw tiny organic speckles / botanical paper flecks
          fleckPoints.forEachIndexed { index, (pos, radius, fleckAlpha) ->
            val fleckColor = if (index % 3 == 0) fiberColorWarm else fiberColorCool
            drawCircle(
              color = fleckColor.copy(alpha = fleckAlpha * textureIntensity * alpha),
              radius = radius,
              center = pos
            )
          }
        }
      }
    }
}

/**
 * Convenience modifier for cards with paper cardstock texture and warm borders
 */
fun Modifier.paperCardBackground(
  shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
): Modifier = this.paperBackground(isCard = true)
