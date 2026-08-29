package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LocalThemeController
import com.example.ui.theme.paperBackground

@Composable
fun RealUsDrawer(
  isOpen: Boolean,
  onClose: () -> Unit,
  onNavigateTab: (String) -> Unit,
  onNavigateSubScreen: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  if (!isOpen) return

  val appColors = AppTheme.colors
  val themeController = LocalThemeController.current
  val currentMode = themeController.currentMode

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.65f))
      .clickable { onClose() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .width(310.dp)
        .paperBackground()
        .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
        .clickable(enabled = false) {}
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
    ) {
      // Header inside sidebar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Dual Avatars
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCr3fVoQ3DuG0CGaMULkrVwYXnqw6pJ5HUcX2EdI7iqeF9Fn6_ajHYQ2ZLv1i3HhrkI4H-96sP18wDGIU0oxFPDEZD357n0OHCOGu6ggMr_vRsiyXPFGf4_OHLfVRFE2xvDZaE23woLUmY2DHXnpkYJlszIE0y7Y1Ak1zN7Axp2tgmCYSpCXvyqZGjqhEWe5WQHEbHRgFcZimZEwwnU3K5Dl5lzFHvJVUDqA2jo8HC2X2A1UXhVNg",
            contentDescription = "Alex",
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .border(2.dp, appColors.surfaceContainer, CircleShape),
            contentScale = ContentScale.Crop
          )
          AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q",
            contentDescription = "Jamie",
            modifier = Modifier
              .offset(x = (-10).dp)
              .size(38.dp)
              .clip(CircleShape)
              .border(2.dp, appColors.surfaceContainer, CircleShape),
            contentScale = ContentScale.Crop
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Alex & Jamie",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.textPrimary
          )
          Text(
            text = "Connected Sanctuary",
            fontSize = 11.sp,
            color = appColors.textMuted
          )
        }

        IconButton(
          onClick = onClose,
          modifier = Modifier.size(32.dp).testTag("close_drawer_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Menu",
            tint = appColors.textSecondary
          )
        }
      }

      // Theme Mode Switcher Box
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(appColors.surfaceContainerHigh)
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
          .padding(12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "THEME & COMFORT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = appColors.textMuted
          )
          Spacer(modifier = Modifier.weight(1f))
          Text(
            text = when (currentMode) {
              AppThemeMode.DAY -> "☀️ Day"
              AppThemeMode.EVENING -> "🕯️ Evening"
              AppThemeMode.NIGHT -> "🌙 Night"
              AppThemeMode.AUTO -> "⏱️ Auto"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.primary
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3-Option Segmented Control: Day / Night / Evening Comfort
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(appColors.surface)
            .padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          ThemeSegmentButton(
            icon = Icons.Default.WbSunny,
            label = "Day",
            isSelected = currentMode == AppThemeMode.DAY,
            onClick = { themeController.setMode(AppThemeMode.DAY) },
            modifier = Modifier.weight(1f)
          )
          ThemeSegmentButton(
            icon = Icons.Default.DarkMode,
            label = "Night",
            isSelected = currentMode == AppThemeMode.NIGHT,
            onClick = { themeController.setMode(AppThemeMode.NIGHT) },
            modifier = Modifier.weight(1f)
          )
          ThemeSegmentButton(
            icon = Icons.Default.Bedtime,
            label = "Evening",
            isSelected = currentMode == AppThemeMode.EVENING,
            onClick = { themeController.setMode(AppThemeMode.EVENING) },
            modifier = Modifier.weight(1f)
          )
        }
      }

      HorizontalDivider(
        color = appColors.outlineVariant.copy(alpha = 0.25f),
        modifier = Modifier.padding(vertical = 10.dp)
      )

      // Main Links
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
      ) {
        DrawerLink(
          icon = Icons.Default.Home,
          title = "Home",
          isSelected = true,
          onClick = {
            onNavigateTab("home")
            onClose()
          }
        )
        DrawerLink(
          icon = Icons.Default.VolunteerActivism,
          title = "Together Hub",
          onClick = {
            onNavigateTab("together")
            onClose()
          }
        )
        DrawerLink(
          icon = Icons.Default.CalendarMonth,
          title = "Shared Calendar",
          onClick = {
            onNavigateSubScreen("calendar")
            onClose()
          }
        )
        DrawerLink(
          icon = Icons.Default.MusicNote,
          title = "Shared Music",
          onClick = {
            onNavigateSubScreen("music")
            onClose()
          }
        )
        DrawerLink(
          icon = Icons.Default.MenuBook,
          title = "Our Story",
          onClick = {
            onNavigateTab("story")
            onClose()
          }
        )
        DrawerLink(
          icon = Icons.Default.AutoStories,
          title = "Journal & Love Notes",
          onClick = {
            onNavigateSubScreen("journal_notes")
            onClose()
          }
        )
        DrawerLink(
          icon = Icons.Default.TouchApp,
          title = "Virtual Touch",
          onClick = {
            onNavigateSubScreen("mood_picker")
            onClose()
          }
        )
      }

      Spacer(modifier = Modifier.weight(1f))
      HorizontalDivider(color = appColors.outlineVariant.copy(alpha = 0.25f))

      // Secondary links
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 12.dp)
      ) {
        DrawerLink(
          icon = Icons.Default.Settings,
          title = "Settings & Themes",
          onClick = {
            onNavigateSubScreen("settings")
            onClose()
          }
        )
        DrawerLink(
          icon = Icons.Default.PrivacyTip,
          title = "Privacy",
          onClick = {
            onNavigateSubScreen("settings")
            onClose()
          }
        )
        DrawerLink(
          icon = Icons.Default.Logout,
          title = "Log Out",
          textColor = ErrorRed,
          onClick = {
            onClose()
          }
        )
      }
    }
  }
}

@Composable
private fun ThemeSegmentButton(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(9.dp))
      .then(
        if (isSelected) {
          Modifier.background(appColors.warmButtonBrush)
        } else {
          Modifier.background(Color.Transparent)
        }
      )
      .clickable { onClick() }
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isSelected) appColors.onPrimary else appColors.textMuted,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      color = if (isSelected) appColors.onPrimary else appColors.textMuted,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
  }
}

@Composable
private fun DrawerLink(
  icon: ImageVector,
  title: String,
  isSelected: Boolean = false,
  textColor: Color = AppTheme.colors.textSecondary,
  onClick: () -> Unit
) {
  val appColors = AppTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) appColors.secondary.copy(alpha = 0.14f) else Color.Transparent)
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = if (isSelected) appColors.secondary else textColor,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
      text = title,
      color = if (isSelected) appColors.secondary else textColor,
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
