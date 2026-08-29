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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.ShowChart
import coil.compose.AsyncImage
import com.example.data.model.CoupleSettings
import com.example.data.model.UserMood
import com.example.ui.components.MoodTrendLineChart
import com.example.ui.components.RealUsLogo
import com.example.ui.theme.AppTheme
import com.example.ui.theme.paperBackground

@Composable
fun HomeScreen(
  coupleSettings: CoupleSettings?,
  moodHistory: List<UserMood> = emptyList(),
  onNavigateTab: (String) -> Unit,
  onNavigateSubScreen: (String) -> Unit,
  onSendHug: () -> Unit,
  onAnswerDailyPrompt: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  var showDailyPromptDialog by remember { mutableStateOf(false) }
  var dailyPromptAnswer by remember { mutableStateOf("") }

  val infiniteHeart = rememberInfiniteTransition(label = "pulse_heart")
  val heartScale by infiniteHeart.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "heart"
  )

  Box(modifier = modifier.fillMaxSize().paperBackground()) {
    // Ambient couple background banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
    ) {
      AsyncImage(
        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCn64GnZ3Zt8W_OYyjg-h7pA_fjoxKjqw5FLHD-U83VgmY0jUAiD2gvHP-taMjnTy7dT_5qkr-_iVaxCAsuvnjn6sEGZOPX__rH3V06W0s7Ww9XNifnuyqTVNdA2fFCzPuMdmeYrrAAzGm-HEBL9Nh-lwfjs8Aj6eXzq9EsGci0QW3y28_46M9wTMs1SBIQZGBb-xkeLcm2zp5yKam9A5XEIrIWaesx2ara5AR2HhPGrA-5mULSIw",
        contentDescription = "Couple Header Background",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                appColors.background.copy(alpha = 0.45f),
                appColors.background.copy(alpha = 0.85f),
                appColors.background
              )
            )
          )
      )
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 90.dp)
    ) {
      // Couple Avatars & Title
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp, bottom = 12.dp)
      ) {
        val defaultUserAvatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuCr3fVoQ3DuG0CGaMULkrVwYXnqw6pJ5HUcX2EdI7iqeF9Fn6_ajHYQ2ZLv1i3HhrkI4H-96sP18wDGIU0oxFPDEZD357n0OHCOGu6ggMr_vRsiyXPFGf4_OHLfVRFE2xvDZaE23woLUmY2DHXnpkYJlszIE0y7Y1Ak1zN7Axp2tgmCYSpCXvyqZGjqhEWe5WQHEbHRgFcZimZEwwnU3K5Dl5lzFHvJVUDqA2jo8HC2X2A1UXhVNg"
        val defaultPartnerAvatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q"

        val userAvatarUrl = coupleSettings?.userAvatarUrl?.ifBlank { defaultUserAvatar } ?: defaultUserAvatar
        val partnerAvatarUrl = coupleSettings?.partnerAvatarUrl?.ifBlank { defaultPartnerAvatar } ?: defaultPartnerAvatar

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(bottom = 12.dp)
        ) {
          // Partner 1 Avatar
          AsyncImage(
            model = userAvatarUrl,
            contentDescription = "User avatar",
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .border(3.dp, appColors.surfaceContainerHighest, CircleShape)
              .clickable { onNavigateTab("us") }
              .zIndex(1f),
            contentScale = ContentScale.Crop
          )

          // Center RealUs Fire & Water Infinity Emblem
          RealUsLogo(
            size = 46.dp,
            showText = false,
            isAnimated = true,
            modifier = Modifier
              .padding(horizontal = 4.dp)
              .zIndex(2f)
          )

          // Partner 2 Avatar
          AsyncImage(
            model = partnerAvatarUrl,
            contentDescription = "Partner avatar",
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .border(3.dp, appColors.surfaceContainerHighest, CircleShape)
              .clickable { onNavigateTab("us") }
              .zIndex(1f),
            contentScale = ContentScale.Crop
          )
        }

        val name1 = coupleSettings?.partner1Name?.ifBlank { "You" } ?: "You"
        val name2 = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner"
        val days = coupleSettings?.daysTogether ?: 0

        Text(
          text = "$name1 & $name2",
          fontFamily = FontFamily.Serif,
          fontSize = 30.sp,
          fontWeight = FontWeight.Normal,
          color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (days > 0) "$days DAYS TOGETHER" else "CONNECTED IN LOVE",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 2.sp,
          color = appColors.secondary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Cards Section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 1. Partner Mood Card
        val partnerDisplayName = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner"
        val partnerMoodStatus = coupleSettings?.partnerMood ?: "Loved"

        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable { onNavigateSubScreen("mood_picker") }
            .testTag("partner_mood_card")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "PARTNER MOOD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = appColors.textMuted
              )
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = appColors.primary,
                modifier = Modifier
                  .size(20.dp)
                  .scale(heartScale)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "$partnerDisplayName is feeling ",
              fontSize = 22.sp,
              fontFamily = FontFamily.Serif,
              color = appColors.textPrimary
            )
            Text(
              text = partnerMoodStatus,
              fontSize = 24.sp,
              fontFamily = FontFamily.Serif,
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Medium,
              color = appColors.primary
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              OutlinedButton(
                onClick = onSendHug,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = appColors.surface,
                  contentColor = appColors.primary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                  brush = Brush.horizontalGradient(listOf(appColors.outlineVariant, appColors.outlineVariant))
                ),
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("send_hug_button")
              ) {
                Text(
                  text = "Send a Hug",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Button(
                onClick = { onNavigateTab("chat") },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color.Transparent,
                  contentColor = appColors.onPrimary
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(appColors.warmButtonBrush)
                  .testTag("message_partner_button")
              ) {
                Text(
                  text = "Message",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = appColors.onPrimary
                )
              }
            }
          }
        }

        // 2. Mood Sync 7-Day Trend Graph Card (D3/Recharts-inspired smooth cubic Bézier line visualization)
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .testTag("dashboard_mood_sync_graph_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.ShowChart,
                  contentDescription = "Mood Sync Trends",
                  tint = appColors.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "MOOD SYNC • PAST WEEK",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.5.sp,
                  color = appColors.textMuted
                )
              }

              TextButton(
                onClick = { onNavigateSubScreen("mood_picker") },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("open_mood_history_btn")
              ) {
                Text(
                  text = "Log Mood →",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = appColors.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MoodTrendLineChart(
              moods = moodHistory,
              userName = coupleSettings?.partner1Name?.ifBlank { "You" } ?: "You",
              partnerName = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner",
              timeRange = "7D",
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // 3. Love Note Pinned Card
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHighest),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .clickable { onNavigateSubScreen("love_notes") }
            .testTag("home_love_note_card")
        ) {
          Box(modifier = Modifier.padding(20.dp)) {
            // Push pin icon
            Icon(
              imageVector = Icons.Default.PushPin,
              contentDescription = "Pinned",
              tint = appColors.textMuted.copy(alpha = 0.4f),
              modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .rotate(-15f)
            )

            Column {
              Text(
                text = "LOVE NOTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = appColors.textMuted
              )

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "\"You make every single day feel like an adventure. So grateful for our shared life together! 🍷✨\"",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                color = appColors.textPrimary,
                modifier = Modifier.padding(end = 28.dp)
              )

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = "— Yours, $partnerDisplayName",
                fontSize = 13.sp,
                color = appColors.textMuted,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.End)
              )
            }
          }
        }

        // 3. Daily Prompt Card
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .testTag("home_daily_prompt_card")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = appColors.primary,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "DAILY PROMPT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = appColors.textMuted
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "What's your absolute favorite memory of us from this past year?",
              fontFamily = FontFamily.Serif,
              fontSize = 20.sp,
              lineHeight = 26.sp,
              color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
              onClick = { showDailyPromptDialog = true },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = appColors.textMuted
              ),
              border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(appColors.outlineVariant, appColors.outlineVariant))
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("answer_daily_prompt_btn")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = appColors.textMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Tap to answer",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // 4. Quick Action Grid (4 Columns: Chat, Call, Music, Albums)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionButton(
            icon = Icons.Default.ChatBubble,
            label = "CHAT",
            onClick = { onNavigateTab("chat") },
            modifier = Modifier.weight(1f)
          )
          QuickActionButton(
            icon = Icons.Default.Call,
            label = "CALL",
            onClick = { onNavigateSubScreen("active_call") },
            modifier = Modifier.weight(1f)
          )
          QuickActionButton(
            icon = Icons.Default.MusicNote,
            label = "MUSIC",
            onClick = { onNavigateSubScreen("music") },
            modifier = Modifier.weight(1f)
          )
          QuickActionButton(
            icon = Icons.Default.PhotoLibrary,
            label = "ALBUMS",
            onClick = { onNavigateTab("story") },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }

  // Answer Daily Prompt Modal Dialog
  if (showDailyPromptDialog) {
    AlertDialog(
      onDismissRequest = { showDailyPromptDialog = false },
      containerColor = appColors.surfaceContainer,
      title = {
        Text(
          text = "Favorite Memory",
          fontFamily = FontFamily.Serif,
          color = appColors.textPrimary
        )
      },
      text = {
        Column {
          Text(
            text = "What's your absolute favorite memory of us from this past year?",
            fontSize = 14.sp,
            color = appColors.textSecondary
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = dailyPromptAnswer,
            onValueChange = { dailyPromptAnswer = it },
            placeholder = { Text("Write your reflection here...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (dailyPromptAnswer.isNotBlank()) {
              onAnswerDailyPrompt(dailyPromptAnswer)
              dailyPromptAnswer = ""
            }
            showDailyPromptDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryDark)
        ) {
          Text("Save & Share", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDailyPromptDialog = false }) {
          Text("Cancel", color = appColors.textSecondary)
        }
      }
    )
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
    modifier = modifier
      .aspectRatio(0.9f)
      .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("quick_action_${label.lowercase()}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(appColors.primary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = appColors.primary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = appColors.textMuted
      )
    }
  }
}
