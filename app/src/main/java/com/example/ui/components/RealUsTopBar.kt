package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalThemeController

@Composable
fun RealUsTopBar(
  onMenuClick: (() -> Unit)? = null,
  onPartnerClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val themeController = LocalThemeController.current
  val appColors = AppTheme.colors
  val currentMode = themeController.currentMode

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(appColors.surface.copy(alpha = 0.94f))
      .statusBarsPadding()
      .height(60.dp)
      .padding(horizontal = 14.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (onMenuClick != null) {
        IconButton(
          onClick = onMenuClick,
          modifier = Modifier
            .size(40.dp)
            .testTag("menu_button")
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Open Navigation Menu",
            tint = appColors.textPrimary
          )
        }
      }

      // App Icon / Logo
      AsyncImage(
        model = "https://lh3.googleusercontent.com/aida/AEtjO1X5rlG9rQNmzqV2rBNdG7HAI-KcONbowZNaY7joof1ESqHr1CK_nDE3TGOcQLLAUFaUS11YfUUVoy-kn9y3jI8-IqLRauwju9_ZRdQlkB7PtRM2dBj9Y7y_6FFLAJU-m21beNddh8MAhDnIca0084vIxkEFXHQtDCbGgMMjCKf_tp0KYh9E65YRcJ_Q35NU5_M1dlpCVCKjzqx8kDz3YZ4a0q9duEcMUkMvSs9g8DgGhSJ3iOzu9AkNpSN30O5v-UMpjIqHRA",
        contentDescription = "RealUs Logo",
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape),
        contentScale = ContentScale.Crop
      )

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = "RealUs",
        style = MaterialTheme.typography.titleLarge.copy(
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.SemiBold,
          color = appColors.primary,
          fontSize = 22.sp,
          letterSpacing = 0.5.sp
        )
      )

      Spacer(modifier = Modifier.weight(1f))

      // Day / Night / Evening Mode Quick Switcher Pill
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(appColors.surfaceContainerHigh)
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
          .clickable { themeController.toggleDayNight() }
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("theme_toggle_btn")
      ) {
        AnimatedContent(
          targetState = currentMode,
          transitionSpec = {
            (scaleIn(spring(stiffness = Spring.StiffnessMedium)) + fadeIn()).togetherWith(
              scaleOut(spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
            )
          },
          label = "theme_icon_anim"
        ) { mode ->
          when (mode) {
            AppThemeMode.DAY -> {
              Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Switch to Night Mode",
                tint = Color(0xFFD97706),
                modifier = Modifier.size(16.dp)
              )
            }
            AppThemeMode.EVENING -> {
              Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = "Evening Comfort Mode Active",
                tint = Color(0xFFFFAD7A),
                modifier = Modifier.size(16.dp)
              )
            }
            else -> {
              Icon(
                imageVector = Icons.Default.LightMode,
                contentDescription = "Switch to Day Mode",
                tint = Color(0xFFFFB68E),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(5.dp))

        Text(
          text = when (currentMode) {
            AppThemeMode.DAY -> "Day"
            AppThemeMode.EVENING -> "Evening"
            else -> "Night"
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.textPrimary
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      // Dual Avatars: Partner + YOU overlapping
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(CircleShape)
          .clickable { onPartnerClick?.invoke() }
          .padding(2.dp)
          .testTag("top_avatars")
      ) {
        Box(
          modifier = Modifier.size(32.dp)
        ) {
          // Partner avatar
          AsyncImage(
            model = "https://lh3.googleusercontent.com/aida/AEtjO1X5rlG9rQNmzqV2rBNdG7HAI-KcONbowZNaY7joof1ESqHr1CK_nDE3TGOcQLLAUFaUS11YfUUVoy-kn9y3jI8-IqLRauwju9_ZRdQlkB7PtRM2dBj9Y7y_6FFLAJU-m21beNddh8MAhDnIca0084vIxkEFXHQtDCbGgMMjCKf_tp0KYh9E65YRcJ_Q35NU5_M1dlpCVCKjzqx8kDz3YZ4a0q9duEcMUkMvSs9g8DgGhSJ3iOzu9AkNpSN30O5v-UMpjIqHRA",
            contentDescription = "Partner Avatar",
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .border(1.5.dp, appColors.surface, CircleShape)
              .zIndex(1f),
            contentScale = ContentScale.Crop
          )
        }

        // YOU badge
        Box(
          modifier = Modifier
            .offset(x = (-8).dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(appColors.secondaryFixed)
            .border(1.5.dp, appColors.surface, CircleShape)
            .zIndex(2f),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "YOU",
            color = Color(0xFF241A00),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        }
      }
    }
  }
}
