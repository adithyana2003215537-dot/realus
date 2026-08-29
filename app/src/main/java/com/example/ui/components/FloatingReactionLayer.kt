package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FlyingReaction

@Composable
fun FloatingReactionLayer(
  reactions: List<FlyingReaction>,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxSize()) {
    reactions.forEach { reaction ->
      KeyedReactionParticle(reaction = reaction)
    }
  }
}

@Composable
private fun KeyedReactionParticle(reaction: FlyingReaction) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp
  val density = LocalDensity.current

  val startXPx = with(density) { (screenWidth * reaction.startXPercent).toPx() }
  val startYPx = with(density) { (screenHeight * 0.75f).toPx() }
  val targetYPx = with(density) { (screenHeight * 0.25f).toPx() }

  val yOffsetAnim = remember { Animatable(startYPx) }
  val alphaAnim = remember { Animatable(1f) }
  val scaleAnim = remember { Animatable(0.4f * reaction.scale) }

  LaunchedEffect(reaction.id) {
    // Animate scale up first
    scaleAnim.animateTo(
      targetValue = 1.3f * reaction.scale,
      animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )
    // Move up and fade out
    yOffsetAnim.animateTo(
      targetValue = targetYPx,
      animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(reaction.id) {
    alphaAnim.animateTo(
      targetValue = 0f,
      animationSpec = tween(durationMillis = 2000, delayMillis = 400)
    )
  }

  Box(
    modifier = Modifier
      .offset {
        IntOffset(
          x = startXPx.toInt(),
          y = yOffsetAnim.value.toInt()
        )
      }
      .alpha(alphaAnim.value)
      .scale(scaleAnim.value)
  ) {
    Text(
      text = reaction.emoji,
      fontSize = 32.sp
    )
  }
}
