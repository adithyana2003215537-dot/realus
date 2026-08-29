package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * RealUs Signature Fire & Water Infinity Emblem.
 * Represents passion (fire on the left) and tranquility/depth (water on the right)
 * intertwining into an eternal bond.
 */
@Composable
fun RealUsLogo(
  modifier: Modifier = Modifier,
  size: Dp = 160.dp,
  showText: Boolean = true,
  isAnimated: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
  
  val glowPulse by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_pulse"
  )

  val shimmerPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmer"
  )

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.size(size)
    ) {
      // Infinity & Ambient Canvas
      Canvas(
        modifier = Modifier.size(size)
      ) {
        val pulse = if (isAnimated) glowPulse else 1f
        // Left Fire soft radial underglow
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFF5500).copy(alpha = 0.35f * pulse),
              Color(0xFFFF5500).copy(alpha = 0.12f * pulse),
              Color.Transparent
            ),
            center = Offset(size.toPx() * 0.32f, size.toPx() * 0.5f),
            radius = size.toPx() * 0.42f
          ),
          radius = size.toPx() * 0.42f,
          center = Offset(size.toPx() * 0.32f, size.toPx() * 0.5f)
        )

        // Right Water soft radial underglow
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF0099FF).copy(alpha = 0.35f * pulse),
              Color(0xFF0099FF).copy(alpha = 0.12f * pulse),
              Color.Transparent
            ),
            center = Offset(size.toPx() * 0.68f, size.toPx() * 0.5f),
            radius = size.toPx() * 0.42f
          ),
          radius = size.toPx() * 0.42f,
          center = Offset(size.toPx() * 0.68f, size.toPx() * 0.5f)
        )

        drawRealUsInfinity(
          canvasWidth = this.size.width,
          canvasHeight = this.size.height,
          shimmer = if (isAnimated) shimmerPhase else 0.5f
        )
      }
    }

    if (showText) {
      Spacer(modifier = Modifier.height(size * 0.08f))
      Text(
        text = "R E A L U S",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = (size.value * 0.12f).sp,
        letterSpacing = 5.sp,
        color = Color(0xFFF0CCA0)
      )
    }
  }
}

private fun DrawScope.drawRealUsInfinity(
  canvasWidth: Float,
  canvasHeight: Float,
  shimmer: Float
) {
  val cx = canvasWidth / 2f
  val cy = canvasHeight / 2f
  val loopWidth = canvasWidth * 0.40f
  val loopHeight = canvasHeight * 0.32f
  val strokeThickness = canvasWidth * 0.09f
  val innerStrokeThickness = strokeThickness * 0.6f

  // Left Fire Loop Path (Left side lemniscate)
  val leftFirePath = Path().apply {
    moveTo(cx, cy)
    // Left loop top curve
    cubicTo(
      cx - loopWidth * 0.45f, cy - loopHeight * 1.05f,
      cx - loopWidth * 1.15f, cy - loopHeight * 0.95f,
      cx - loopWidth * 1.05f, cy
    )
    // Left loop bottom curve back to center
    cubicTo(
      cx - loopWidth * 1.15f, cy + loopHeight * 0.95f,
      cx - loopWidth * 0.45f, cy + loopHeight * 1.05f,
      cx, cy
    )
  }

  // Right Water Loop Path (Right side lemniscate)
  val rightWaterPath = Path().apply {
    moveTo(cx, cy)
    // Right loop top curve
    cubicTo(
      cx + loopWidth * 0.45f, cy + loopHeight * 1.05f,
      cx + loopWidth * 1.15f, cy + loopHeight * 0.95f,
      cx + loopWidth * 1.05f, cy
    )
    // Right loop bottom curve back to center
    cubicTo(
      cx + loopWidth * 1.15f, cy - loopHeight * 0.95f,
      cx + loopWidth * 0.45f, cy - loopHeight * 1.05f,
      cx, cy
    )
  }

  // 1. Draw Left Fire Base & Outer Glow
  val fireBrushGlow = Brush.linearGradient(
    colors = listOf(
      Color(0xFFFF2200).copy(alpha = 0.5f),
      Color(0xFFFF6600).copy(alpha = 0.6f),
      Color(0xFFFFB300).copy(alpha = 0.5f)
    ),
    start = Offset(cx - loopWidth * 1.2f, cy + loopHeight),
    end = Offset(cx, cy - loopHeight)
  )
  drawPath(
    path = leftFirePath,
    brush = fireBrushGlow,
    style = Stroke(
      width = strokeThickness * 1.35f,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // 1b. Left Main Fire Body
  val fireBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFFE61B00), // Deep Crimson Fire
      Color(0xFFFF4800), // Flame Orange
      Color(0xFFFF8C00), // Amber
      Color(0xFFFFD54F)  // Warm Yellow Core
    ),
    start = Offset(cx - loopWidth * 1.1f, cy + loopHeight),
    end = Offset(cx, cy - loopHeight * 0.5f)
  )
  drawPath(
    path = leftFirePath,
    brush = fireBrush,
    style = Stroke(
      width = strokeThickness,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // 1c. Left Inner Core Fiery Highlight
  val fireCoreBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFFFF7A00),
      Color(0xFFFFD54F),
      Color(0xFFFFF9C4)
    ),
    start = Offset(cx - loopWidth * 0.9f, cy + loopHeight * 0.6f),
    end = Offset(cx, cy - loopHeight * 0.3f)
  )
  drawPath(
    path = leftFirePath,
    brush = fireCoreBrush,
    style = Stroke(
      width = innerStrokeThickness * 0.5f,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // 2. Draw Right Water Base & Outer Glow
  val waterBrushGlow = Brush.linearGradient(
    colors = listOf(
      Color(0xFF003C9E).copy(alpha = 0.5f),
      Color(0xFF0088FF).copy(alpha = 0.6f),
      Color(0xFF00E5FF).copy(alpha = 0.5f)
    ),
    start = Offset(cx, cy + loopHeight * 0.5f),
    end = Offset(cx + loopWidth * 1.2f, cy - loopHeight)
  )
  drawPath(
    path = rightWaterPath,
    brush = waterBrushGlow,
    style = Stroke(
      width = strokeThickness * 1.35f,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // 2b. Right Main Water Body
  val waterBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFF0044B8), // Deep Ocean Blue
      Color(0xFF0084FF), // Vivid Aqua
      Color(0xFF00C8FF), // Cyan Splash
      Color(0xFFB2EBF2)  // Crisp Wave Highlight
    ),
    start = Offset(cx, cy + loopHeight * 0.5f),
    end = Offset(cx + loopWidth * 1.1f, cy - loopHeight)
  )
  drawPath(
    path = rightWaterPath,
    brush = waterBrush,
    style = Stroke(
      width = strokeThickness,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // 2c. Right Inner Core Water Crest Highlight
  val waterCoreBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFF40C4FF),
      Color(0xFFE0F7FA),
      Color(0xFFFFFFFF)
    ),
    start = Offset(cx + loopWidth * 0.3f, cy + loopHeight * 0.4f),
    end = Offset(cx + loopWidth * 0.9f, cy - loopHeight * 0.6f)
  )
  drawPath(
    path = rightWaterPath,
    brush = waterCoreBrush,
    style = Stroke(
      width = innerStrokeThickness * 0.5f,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // 3. Flame Tongues and Flickers (Left side decorative organic flames)
  val flame1 = Path().apply {
    moveTo(cx - loopWidth * 0.85f, cy - loopHeight * 0.85f)
    quadraticBezierTo(
      cx - loopWidth * 0.95f, cy - loopHeight * 1.35f,
      cx - loopWidth * 0.75f, cy - loopHeight * 1.55f
    )
    quadraticBezierTo(
      cx - loopWidth * 0.70f, cy - loopHeight * 1.25f,
      cx - loopWidth * 0.60f, cy - loopHeight * 0.95f
    )
    close()
  }
  drawPath(
    path = flame1,
    brush = Brush.verticalGradient(
      listOf(Color(0xFFFFD54F), Color(0xFFFF5722)),
      startY = cy - loopHeight * 1.6f,
      endY = cy - loopHeight * 0.8f
    )
  )

  val flame2 = Path().apply {
    moveTo(cx - loopWidth * 1.15f, cy - loopHeight * 0.3f)
    quadraticBezierTo(
      cx - loopWidth * 1.45f, cy - loopHeight * 0.75f,
      cx - loopWidth * 1.35f, cy - loopHeight * 1.15f
    )
    quadraticBezierTo(
      cx - loopWidth * 1.20f, cy - loopHeight * 0.70f,
      cx - loopWidth * 1.05f, cy - loopHeight * 0.55f
    )
    close()
  }
  drawPath(
    path = flame2,
    brush = Brush.verticalGradient(
      listOf(Color(0xFFFF9800), Color(0xFFE64A19)),
      startY = cy - loopHeight * 1.2f,
      endY = cy - loopHeight * 0.3f
    )
  )

  // Floating Fire Spark Particles
  val sparkColor = Color(0xFFFFCC80)
  drawCircle(
    color = sparkColor,
    radius = canvasWidth * 0.015f,
    center = Offset(cx - loopWidth * 0.55f, cy - loopHeight * 1.3f)
  )
  drawCircle(
    color = Color(0xFFFFAB40),
    radius = canvasWidth * 0.012f,
    center = Offset(cx - loopWidth * 1.35f, cy - loopHeight * 0.2f)
  )
  drawCircle(
    color = Color(0xFFFFE082),
    radius = canvasWidth * 0.018f,
    center = Offset(cx - loopWidth * 1.1f, cy + loopHeight * 1.05f)
  )
  drawCircle(
    color = Color(0xFFFF6E40),
    radius = canvasWidth * 0.014f,
    center = Offset(cx - loopWidth * 0.3f, cy - loopHeight * 0.9f)
  )

  // 4. Water Splash Crests & Wave Droplets (Right side dynamic splashes)
  val splash1 = Path().apply {
    moveTo(cx + loopWidth * 0.65f, cy - loopHeight * 0.95f)
    quadraticBezierTo(
      cx + loopWidth * 0.85f, cy - loopHeight * 1.45f,
      cx + loopWidth * 1.05f, cy - loopHeight * 1.35f
    )
    quadraticBezierTo(
      cx + loopWidth * 0.95f, cy - loopHeight * 1.10f,
      cx + loopWidth * 0.85f, cy - loopHeight * 0.85f
    )
    close()
  }
  drawPath(
    path = splash1,
    brush = Brush.verticalGradient(
      listOf(Color(0xFFE0F7FA), Color(0xFF0091EA)),
      startY = cy - loopHeight * 1.5f,
      endY = cy - loopHeight * 0.8f
    )
  )

  val splash2 = Path().apply {
    moveTo(cx + loopWidth * 1.15f, cy + loopHeight * 0.35f)
    quadraticBezierTo(
      cx + loopWidth * 1.45f, cy + loopHeight * 0.75f,
      cx + loopWidth * 1.35f, cy + loopHeight * 1.15f
    )
    quadraticBezierTo(
      cx + loopWidth * 1.20f, cy + loopHeight * 0.70f,
      cx + loopWidth * 1.05f, cy + loopHeight * 0.55f
    )
    close()
  }
  drawPath(
    path = splash2,
    brush = Brush.verticalGradient(
      listOf(Color(0xFF80D8FF), Color(0xFF0077C2)),
      startY = cy + loopHeight * 0.3f,
      endY = cy + loopHeight * 1.2f
    )
  )

  // Floating Water Droplets
  val dropletColor = Color(0xFFB3E5FC)
  drawCircle(
    color = dropletColor,
    radius = canvasWidth * 0.016f,
    center = Offset(cx + loopWidth * 0.55f, cy - loopHeight * 1.35f)
  )
  drawCircle(
    color = Color(0xFFE1F5FE),
    radius = canvasWidth * 0.012f,
    center = Offset(cx + loopWidth * 1.15f, cy - loopHeight * 1.4f)
  )
  drawCircle(
    color = Color(0xFF81D4FA),
    radius = canvasWidth * 0.018f,
    center = Offset(cx + loopWidth * 1.38f, cy + loopHeight * 0.25f)
  )
  drawCircle(
    color = Color(0xFF4FC3F7),
    radius = canvasWidth * 0.014f,
    center = Offset(cx + loopWidth * 0.35f, cy + loopHeight * 1.15f)
  )
  drawCircle(
    color = Color(0xFFE0F7FA),
    radius = canvasWidth * 0.010f,
    center = Offset(cx + loopWidth * 1.25f, cy + loopHeight * 1.3f)
  )

  // 5. Center Intersection Swirl (Fire meets Water seamlessly)
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        Color(0xFFFFE082).copy(alpha = 0.9f),
        Color(0xFF80D8FF).copy(alpha = 0.5f),
        Color.Transparent
      ),
      center = Offset(cx, cy),
      radius = canvasWidth * 0.08f
    ),
    radius = canvasWidth * 0.08f,
    center = Offset(cx, cy)
  )
}
