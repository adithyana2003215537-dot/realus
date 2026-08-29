package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerHighest
import com.example.ui.theme.DarkSurfaceContainerLow
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.PrimarySienna
import com.example.ui.theme.SecondaryGold
import com.example.ui.theme.TextGoldMuted
import com.example.ui.theme.TextOffWhite

@Composable
fun IncomingCallScreen(
  partnerName: String,
  onAccept: () -> Unit,
  onDecline: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkSurface)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header text
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "INCOMING CALL",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp,
          color = PrimarySienna
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = partnerName,
          fontFamily = FontFamily.Serif,
          fontSize = 36.sp,
          fontWeight = FontWeight.Normal,
          color = TextOffWhite
        )
        Text(
          text = "RealUs Encrypted Audio",
          fontSize = 13.sp,
          color = TextGoldMuted
        )
      }

      // Partner avatar with pulsing glow rings
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
      ) {
        Box(
          modifier = Modifier
            .size(200.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(PrimarySienna.copy(alpha = 0.2f))
        )
        AsyncImage(
          model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBXJBfS2sICynevz8QCCjhfkhZYcfgp_O0Adr0td4Y3_q30mMTH-EuZdBsVQcO3n_hT_viH1oopv4y0gB_J6u29HIop4QApXJLoylxzfzdspeS1R1_kh4gynrvtd3E1CSzTenw_syEyd1ZqkoHvjcwHK6Dl-lB3EBfdBm3M3GIOGuVNLNn6Q3pwDpwffLVrWiVW3M0XbPyhyVzbsGo2jJQna-CBwkWJfThAKjJxVq25soLg9TSgEQ",
          contentDescription = partnerName,
          modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .border(3.dp, PrimarySienna, CircleShape),
          contentScale = ContentScale.Crop
        )
      }

      // Accept / Decline Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Decline Button (Red)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color(0xFFE53935))
              .clickable { onDecline() }
              .testTag("decline_call_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CallEnd,
              contentDescription = "Decline",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Decline", fontSize = 13.sp, color = TextOffWhite)
        }

        // Accept Button (Green)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color(0xFF43A047))
              .clickable { onAccept() }
              .testTag("accept_call_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Accept",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Accept", fontSize = 13.sp, color = TextOffWhite)
        }
      }
    }
  }
}

@Composable
fun ActiveCallScreen(
  partnerName: String,
  callSeconds: Int,
  isMuted: Boolean,
  isVideoOn: Boolean,
  isSpeakerOn: Boolean,
  onToggleMute: () -> Unit,
  onToggleVideo: () -> Unit,
  onToggleSpeaker: () -> Unit,
  onEndCall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val minutes = callSeconds / 60
  val seconds = callSeconds % 60
  val timerFormatted = String.format("%02d:%02d", minutes, seconds)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkSurface)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header Capsule with Encryption & Timer
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceContainerHigh)
            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = PrimarySienna,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "End-to-End Encrypted",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = PrimarySienna
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = partnerName,
          fontFamily = FontFamily.Serif,
          fontSize = 32.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextOffWhite
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = timerFormatted,
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          letterSpacing = 1.sp,
          color = SecondaryGold
        )
      }

      // Center Portrait
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
      ) {
        AsyncImage(
          model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBXJBfS2sICynevz8QCCjhfkhZYcfgp_O0Adr0td4Y3_q30mMTH-EuZdBsVQcO3n_hT_viH1oopv4y0gB_J6u29HIop4QApXJLoylxzfzdspeS1R1_kh4gynrvtd3E1CSzTenw_syEyd1ZqkoHvjcwHK6Dl-lB3EBfdBm3M3GIOGuVNLNn6Q3pwDpwffLVrWiVW3M0XbPyhyVzbsGo2jJQna-CBwkWJfThAKjJxVq25soLg9TSgEQ",
          contentDescription = partnerName,
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .border(3.dp, PrimarySienna, CircleShape),
          contentScale = ContentScale.Crop
        )
      }

      // Audio waveform simulation dots
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        listOf(14, 28, 20, 36, 24, 32, 18, 24).forEach { height ->
          Box(
            modifier = Modifier
              .width(4.dp)
              .height(height.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(PrimarySienna)
          )
        }
      }

      // Call Controls (Mute, Video, Speaker, End)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        CallActionButton(
          icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
          label = if (isMuted) "Unmute" else "Mute",
          isActive = isMuted,
          onClick = onToggleMute
        )
        CallActionButton(
          icon = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
          label = "Video",
          isActive = isVideoOn,
          onClick = onToggleVideo
        )
        CallActionButton(
          icon = Icons.Default.VolumeUp,
          label = "Speaker",
          isActive = isSpeakerOn,
          onClick = onToggleSpeaker
        )

        // End Call Button (Red)
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFFE53935))
            .clickable { onEndCall() }
            .testTag("end_active_call_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun CallActionButton(
  icon: ImageVector,
  label: String,
  isActive: Boolean,
  onClick: () -> Unit
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
        .background(if (isActive) PrimarySienna else DarkSurfaceContainerHighest)
        .clickable { onClick() },
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isActive) Color(0xFF542200) else TextOffWhite,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      color = TextGoldMuted
    )
  }
}
