package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.RealUsLogo
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimarySienna
import com.example.ui.theme.PrimarySiennaDark
import com.example.ui.theme.TextOffWhite

@Composable
fun WelcomeScreen(
  onGetStarted: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "float_trans")
  val floatOffset by infiniteTransition.animateFloat(
    initialValue = -8f,
    targetValue = 8f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "float"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.radialGradient(
          colors = listOf(
            PrimaryContainer.copy(alpha = 0.25f),
            DarkSurfaceContainer,
            DarkSurface
          ),
          radius = 1200f
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 28.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      // Center Hero with RealUs Fire & Water Infinity Logo
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
          .weight(1f)
          .offset(y = floatOffset.dp)
      ) {
        RealUsLogo(
          size = 230.dp,
          showText = true,
          isAnimated = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "A private digital home for the two of you.",
          fontSize = 15.sp,
          fontWeight = FontWeight.Light,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
          textAlign = TextAlign.Center,
          letterSpacing = 0.5.sp,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }

      // Bottom CTA button
      Button(
        onClick = onGetStarted,
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp)
          .shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(29.dp),
            ambientColor = PrimarySienna,
            spotColor = PrimarySienna
          )
          .testTag("get_started_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = PrimarySiennaDark,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(29.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = "Get Started",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.size(8.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
