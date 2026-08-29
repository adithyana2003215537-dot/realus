package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.paperBackground

data class NavItem(
  val tabKey: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
)

val NAV_ITEMS = listOf(
  NavItem("home", "HOME", Icons.Filled.Home, Icons.Outlined.Home),
  NavItem("chat", "CHAT", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
  NavItem("story", "STORY", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
  NavItem("together", "TOGETHER", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism),
  NavItem("us", "US", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
)

@Composable
fun RealUsBottomNav(
  currentTab: String,
  onTabSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors

  Column(
    modifier = modifier
      .fillMaxWidth()
      .paperBackground()
      .border(
        width = 1.dp,
        color = appColors.outlineVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
      )
      .navigationBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NAV_ITEMS.forEach { item ->
        val isSelected = currentTab == item.tabKey
        val scale by animateFloatAsState(
          targetValue = if (isSelected) 1.08f else 1.0f,
          animationSpec = spring(dampingRatio = 0.7f),
          label = "nav_scale"
        )
        val tintColor by animateColorAsState(
          targetValue = if (isSelected) appColors.primary else appColors.textMuted,
          label = "nav_tint"
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .size(width = 64.dp, height = 56.dp)
            .scale(scale)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) {
              onTabSelected(item.tabKey)
            }
            .testTag("nav_tab_${item.tabKey}")
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .then(
                if (isSelected) {
                  Modifier
                    .background(appColors.primary.copy(alpha = if (appColors.isDark) 0.18f else 0.14f))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                } else {
                  Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
              contentDescription = item.label,
              tint = tintColor,
              modifier = Modifier.size(24.dp)
            )
          }
          Text(
            text = item.label,
            color = tintColor,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 1.dp)
          )
        }
      }
    }
  }
}
