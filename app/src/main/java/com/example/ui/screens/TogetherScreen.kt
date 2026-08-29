package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CoupleSettings
import com.example.ui.theme.AppTheme
import com.example.ui.theme.paperBackground

@Composable
fun TogetherScreen(
  coupleSettings: CoupleSettings?,
  isPlayingMusic: Boolean,
  onToggleMusic: () -> Unit,
  onNavigateSubScreen: (String) -> Unit,
  onAnswerDailyQuestion: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  var showQuestionModal by remember { mutableStateOf(false) }
  var userDailyAnswer by remember { mutableStateOf("That quiet candlelit trattoria overlooking the sea 🌊") }
  var isAnswerRevealed by remember { mutableStateOf(false) }

  val infiniteRotation = rememberInfiniteTransition(label = "vinyl_spin")
  val vinylRotation by infiniteRotation.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 6000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "vinyl"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .paperBackground()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 90.dp)
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      Text(
        text = "Our World",
        fontFamily = FontFamily.Serif,
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold,
        color = appColors.textPrimary
      )
      Text(
        text = "${coupleSettings?.partner1Name ?: "Alex"} & ${coupleSettings?.partner2Name ?: "Jamie"}'s private shared space",
        fontSize = 13.sp,
        color = appColors.textMuted,
        modifier = Modifier.padding(top = 2.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Stat Badges Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        StatBadge(
          emoji = "🔥",
          number = "342",
          label = "Day Streak",
          modifier = Modifier.weight(1f)
        )
        StatBadge(
          emoji = "💫",
          number = "1.2k",
          label = "Moments",
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Main Sections
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Daily Question Card
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
          .testTag("together_daily_question_card")
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = appColors.secondary,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "DAILY QUESTION",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp,
              color = appColors.secondary
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "If we could teleport anywhere for dinner tonight, where would you take me?",
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            color = appColors.textPrimary
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Jamie's response capsule
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(appColors.surfaceContainerHigh)
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AsyncImage(
              model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q",
              contentDescription = "Jamie",
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "${coupleSettings?.partner2Name ?: "Jamie"}:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.primary
              )
              Text(
                text = "\"That rooftop in Florence overlooking the Duomo ✨\"",
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = appColors.textPrimary
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Your response capsule
          if (isAnswerRevealed) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(appColors.surfaceOchre)
                .border(1.dp, appColors.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(appColors.secondary),
                contentAlignment = Alignment.Center
              ) {
                Text("YOU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = appColors.onSecondary)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "You:",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = appColors.secondary
                )
                Text(
                  text = userDailyAnswer,
                  fontSize = 13.sp,
                  fontStyle = FontStyle.Italic,
                  color = appColors.textPrimary
                )
              }
            }
          } else {
            Button(
              onClick = { showQuestionModal = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = appColors.surfaceClay,
                contentColor = appColors.primary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .testTag("reveal_daily_answer_btn")
            ) {
              Text(
                text = "Tap to answer or reveal",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // 2. Sub-feature Cards Grid (Calendar, Mood Sync, Love Notes, Our Journal)
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureRowCard(
          icon = Icons.Default.VolunteerActivism,
          title = "Mood Sync & Trends",
          subtitle = "Real-time emotional sync & collective mood line charts",
          badgeText = "${coupleSettings?.partnerMood ?: "Loved"} 💖",
          onClick = { onNavigateSubScreen("mood_picker") }
        )
        FeatureRowCard(
          icon = Icons.Default.CalendarMonth,
          title = "Shared Calendar",
          subtitle = "2 upcoming milestones this month",
          badgeText = "Next: Oct 10",
          onClick = { onNavigateSubScreen("calendar") }
        )
        FeatureRowCard(
          icon = Icons.Default.AutoStories,
          title = "Our Journal & Love Notes",
          subtitle = "14 illustrated chapters & pinned sweet notes on your private corkboard",
          badgeText = "Chapters & Notes",
          onClick = { onNavigateSubScreen("journal_notes") }
        )
      }

      // 3. Music Together Widget
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
          .clickable { onNavigateSubScreen("listening_together") }
          .testTag("together_music_card")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Vinyl disc animation
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(if (appColors.isDark) Color.Black else Color(0xFF2A2421))
              .rotate(if (isPlayingMusic) vinylRotation else 0f)
              .border(2.dp, appColors.primary.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6b5w49Dj3A4PVSN5weFyV3OLZ7wOwlVl9I6e1wBqRPCWglTQLeDchOkgf0G3kJ-XWLa6kVXF11puARoddGpodLqMlJiBKz_9tuE9s6gc8wDpynqhAKp1UH0mmA0V46qTXxiGEiEj73TrTJIc2V4a-fr3XHQ9j9kD_dBraWpL9YjMOyO80-wyGCE5UA3lbHtUsxeAd3UIUcvk3UF3j-v6xXdziDkiGCFIAyviEbPvDPp-FCycgjw",
              contentDescription = "Vinyl center",
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = appColors.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "LISTENING TOGETHER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = appColors.primary
              )
            }
            Text(
              text = "Sunset Melodies",
              fontFamily = FontFamily.Serif,
              fontSize = 17.sp,
              fontWeight = FontWeight.SemiBold,
              color = appColors.textPrimary
            )
            Text(
              text = "Midnight Warmth Ensemble",
              fontSize = 12.sp,
              color = appColors.textMuted
            )
          }

          IconButton(
            onClick = onToggleMusic,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(appColors.warmButtonBrush)
              .testTag("toggle_music_playback_btn")
          ) {
            Icon(
              imageVector = if (isPlayingMusic) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlayingMusic) "Pause" else "Play",
              tint = appColors.onPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }

      // 4. Next Milestone Progress Card
      val daysTogether = coupleSettings?.daysTogether ?: 0
      val targetDays = if (daysTogether < 100) 100 else if (daysTogether < 365) 365 else ((daysTogether / 365) + 1) * 365
      val progressRatio = (daysTogether.toFloat() / targetDays.toFloat()).coerceIn(0f, 1f)
      val remainingDays = (targetDays - daysTogether).coerceAtLeast(0)

      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
          .padding(bottom = 12.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (daysTogether < 100) "Milestone: 100 Days" else "Milestone: $targetDays Days",
              fontFamily = FontFamily.Serif,
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold,
              color = appColors.textPrimary
            )
            if (!coupleSettings?.anniversaryDate.isNullOrBlank()) {
              Text(
                text = coupleSettings?.anniversaryDate ?: "",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.secondary
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          LinearProgressIndicator(
            progress = { progressRatio },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = appColors.primary,
            trackColor = appColors.surfaceContainerHighest,
            strokeCap = StrokeCap.Round
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "$daysTogether / $targetDays Days",
              fontSize = 12.sp,
              color = appColors.textMuted
            )
            Text(
              text = if (remainingDays > 0) "$remainingDays days remaining" else "Milestone achieved! 🎉",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = appColors.primary
            )
          }
        }
      }
    }
  }

  // Answer Daily Question Dialog
  if (showQuestionModal) {
    var tempAnswer by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showQuestionModal = false },
      containerColor = appColors.surfaceContainer,
      title = {
        Text(
          text = "Your Answer",
          fontFamily = FontFamily.Serif,
          color = appColors.textPrimary
        )
      },
      text = {
        Column {
          Text(
            text = "If we could teleport anywhere for dinner tonight, where would you take me?",
            fontSize = 14.sp,
            color = appColors.textSecondary
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = tempAnswer,
            onValueChange = { tempAnswer = it },
            placeholder = { Text("Enter your destination & thoughts...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (tempAnswer.isNotBlank()) {
              userDailyAnswer = tempAnswer
              isAnswerRevealed = true
              onAnswerDailyQuestion(tempAnswer)
            }
            showQuestionModal = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
        ) {
          Text("Reveal Answer", color = appColors.onPrimary)
        }
      },
      dismissButton = {
        TextButton(onClick = { showQuestionModal = false }) {
          Text("Cancel", color = appColors.textSecondary)
        }
      }
    )
  }
}

@Composable
private fun StatBadge(
  emoji: String,
  number: String,
  label: String,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
    modifier = modifier.border(1.dp, appColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = emoji, fontSize = 22.sp)
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = number,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = appColors.textPrimary
        )
        Text(
          text = label,
          fontSize = 11.sp,
          color = appColors.textMuted
        )
      }
    }
  }
}

@Composable
private fun FeatureRowCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  badgeText: String,
  onClick: () -> Unit
) {
  val appColors = AppTheme.colors
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, appColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .testTag("feature_card_${title.lowercase().replace(" ", "_")}")
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(appColors.surfaceClay),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = appColors.primary,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontFamily = FontFamily.Serif,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.textPrimary
        )
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = appColors.textMuted
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(appColors.secondary.copy(alpha = 0.15f))
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text(
          text = badgeText,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.secondary
        )
      }
    }
  }
}
