package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserMood
import com.example.ui.components.MoodTrendLineChart
import com.example.ui.theme.AppTheme
import com.example.ui.theme.paperBackground
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MoodOption(
  val key: String,
  val label: String,
  val subtitle: String,
  val emoji: String,
  val defaultScore: Float
)

val CURATED_MOODS = listOf(
  MoodOption("loved", "Loved", "Warm & adored", "💖", 9.5f),
  MoodOption("happy", "Radiant", "Joyful energy", "😊", 8.8f),
  MoodOption("romantic", "Romantic", "Longing for you", "🥰", 9.4f),
  MoodOption("cozy", "Cozy", "Blanket & warmth", "☕", 8.5f),
  MoodOption("need_you", "Need You", "Come hold me", "🫂", 7.5f),
  MoodOption("flirty", "Playful", "Sparks flying", "😏", 9.0f),
  MoodOption("missing", "Missing You", "Counting hours", "🥺", 8.0f),
  MoodOption("tired", "Low Energy", "Gentle rest", "😴", 6.5f),
  MoodOption("grateful", "Grateful", "Deeply thankful", "✨", 9.6f)
)

data class FloatingParticle(
  val id: Long,
  val emoji: String,
  val startXFraction: Float,
  val startYFraction: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
  currentMoodName: String?,
  moodHistory: List<UserMood> = emptyList(),
  partnerName: String = "Jamie",
  userName: String = "Alex",
  onSelectMood: (key: String, label: String, icon: String, note: String, score: Float) -> Unit,
  onAddCustomMood: (label: String, note: String) -> Unit,
  onSendNudge: (emoji: String) -> Unit = {},
  onClose: () -> Unit,
  celebrationMood: UserMood? = null,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf("✨ Sync Today", "📈 Collective Trends")

  // Sync Form State
  var selectedMoodKey by remember {
    mutableStateOf(currentMoodName?.lowercase() ?: "loved")
  }
  var selectedScore by remember { mutableFloatStateOf(9.2f) }
  var moodNote by remember { mutableStateOf("") }
  var showCustomDialog by remember { mutableStateOf(false) }

  // Particle micro-burst system
  val activeParticles = remember { mutableStateListOf<FloatingParticle>() }

  fun triggerParticleBurst(emoji: String, count: Int = 4) {
    val baseId = System.currentTimeMillis()
    repeat(count) { i ->
      activeParticles.add(
        FloatingParticle(
          id = baseId + i + Random.nextLong(1000),
          emoji = emoji,
          startXFraction = 0.2f + Random.nextFloat() * 0.6f,
          startYFraction = 0.65f + Random.nextFloat() * 0.15f
        )
      )
    }
  }

  // Trends Screen State
  var timeRangeFilter by remember { mutableStateOf("7D") } // "7D", "14D", "30D"

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperBackground()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top App Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          IconButton(
            onClick = onClose,
            modifier = Modifier
              .size(40.dp)
              .testTag("close_mood_screen")
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Back",
              tint = appColors.textPrimary
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Text(
              text = "Mood Sync",
              fontFamily = FontFamily.Serif,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = appColors.textPrimary
            )
            Text(
              text = "Real-time emotional harmony with $partnerName",
              fontSize = 11.sp,
              color = appColors.textMuted
            )
          }
        }

        // Live Partner Sync Status Pill with subtle breathing pulse
        val infinitePulse = rememberInfiniteTransition(label = "pulse_glow")
        val pulseAlpha by infinitePulse.animateFloat(
          initialValue = 0.4f,
          targetValue = 1f,
          animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
          ),
          label = "pulse_alpha"
        )

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(appColors.surfaceContainerHigh)
            .border(
              width = 1.dp,
              color = Color(0xFF00E676).copy(alpha = pulseAlpha * 0.7f),
              shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFF00E676).copy(alpha = pulseAlpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "$partnerName: ${currentMoodName ?: "Loved"}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = appColors.textPrimary
            )
          }
        }
      }

      // Tab Navigation Bar
      TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = appColors.background,
        contentColor = appColors.primary,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
            color = appColors.primary,
            height = 3.dp
          )
        },
        divider = {
          Divider(color = appColors.outlineVariant.copy(alpha = 0.35f))
        }
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTabIndex == index,
            onClick = { selectedTabIndex = index },
            text = {
              Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTabIndex == index) appColors.primary else appColors.textMuted
              )
            },
            modifier = Modifier.testTag("mood_tab_$index")
          )
        }
      }

      // Tab Content
      when (selectedTabIndex) {
        0 -> {
          // Tab 1: Sync Today
          SyncTodayContent(
            partnerName = partnerName,
            currentPartnerMood = currentMoodName ?: "Loved",
            selectedMoodKey = selectedMoodKey,
            selectedScore = selectedScore,
            moodNote = moodNote,
            onMoodSelected = { key, score, emoji ->
              selectedMoodKey = key
              selectedScore = score
              triggerParticleBurst(emoji, count = 3)
            },
            onScoreChanged = { selectedScore = it },
            onNoteChanged = { moodNote = it },
            onSyncClick = {
              val opt = CURATED_MOODS.find { it.key == selectedMoodKey } ?: CURATED_MOODS.first()
              triggerParticleBurst(opt.emoji, count = 7)
              onSelectMood(opt.key, opt.label, opt.emoji, moodNote, selectedScore)
            },
            onAddCustomClick = { showCustomDialog = true },
            onSendNudge = { emoji ->
              triggerParticleBurst(emoji, count = 4)
              onSendNudge(emoji)
            }
          )
        }
        1 -> {
          // Tab 2: Collective Trends & Line Chart
          CollectiveTrendsContent(
            moodHistory = moodHistory,
            userName = userName,
            partnerName = partnerName,
            timeRange = timeRangeFilter,
            onTimeRangeChanged = { timeRangeFilter = it },
            onQuickSyncPrompt = { selectedTabIndex = 0 }
          )
        }
      }
    }

    // Floating Particles Layer
    activeParticles.forEach { particle ->
      FloatingParticleItem(
        particle = particle,
        onFinished = { activeParticles.remove(particle) }
      )
    }

    // Celebration Overlay when mood is synced
    AnimatedVisibility(
      visible = celebrationMood != null,
      enter = fadeIn(tween(250)) + scaleIn(tween(300, easing = FastOutSlowInEasing)),
      exit = fadeOut(tween(200)) + scaleOut(tween(200))
    ) {
      if (celebrationMood != null) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable { /* dismiss */ },
          contentAlignment = Alignment.Center
        ) {
          val infiniteSpin = rememberInfiniteTransition(label = "celebration_halo")
          val haloRotation by infiniteSpin.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
              animation = tween(6000, easing = LinearEasing),
              repeatMode = RepeatMode.Restart
            ),
            label = "halo_rotation"
          )

          val animatedSynergy by animateIntAsState(
            targetValue = celebrationMood.synergyScore,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            label = "synergy_counter"
          )

          Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
            modifier = Modifier
              .padding(32.dp)
              .border(
                1.5.dp,
                Brush.sweepGradient(
                  listOf(
                    appColors.primary,
                    appColors.secondary,
                    Color(0xFF00E676),
                    appColors.primary
                  )
                ),
                RoundedCornerShape(28.dp)
              )
          ) {
            Column(
              modifier = Modifier.padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(contentAlignment = Alignment.Center) {
                Canvas(
                  modifier = Modifier
                    .size(90.dp)
                    .rotate(haloRotation)
                ) {
                  drawCircle(
                    brush = Brush.radialGradient(
                      colors = listOf(
                        appColors.primary.copy(alpha = 0.35f),
                        Color.Transparent
                      )
                    ),
                    radius = size.minDimension / 1.8f
                  )
                }

                Text(
                  text = celebrationMood.moodIcon,
                  fontSize = 54.sp,
                  modifier = Modifier.scale(1.05f)
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = "Mood Synced!",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "$partnerName can now see that you're feeling ${celebrationMood.moodLabel} (${String.format("%.1f", celebrationMood.moodScore)}/10)",
                fontSize = 13.sp,
                color = appColors.textMuted,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(16.dp))

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(14.dp))
                  .background(appColors.primary.copy(alpha = 0.2f))
                  .padding(horizontal = 16.dp, vertical = 8.dp)
              ) {
                Text(
                  text = "✨ $animatedSynergy% Collective Harmony",
                  color = appColors.secondary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

    // Custom Mood Dialog
    if (showCustomDialog) {
      var customMoodText by remember { mutableStateOf("") }
      var customNoteText by remember { mutableStateOf("") }

      AlertDialog(
        onDismissRequest = { showCustomDialog = false },
        containerColor = appColors.surfaceContainer,
        title = {
          Text(
            text = "Create Custom Mood",
            fontFamily = FontFamily.Serif,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = customMoodText,
              onValueChange = { customMoodText = it },
              label = { Text("Mood Name") },
              placeholder = { Text("e.g., Excited for tonight 🍷") },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appColors.primary,
                unfocusedBorderColor = appColors.outlineVariant,
                focusedTextColor = appColors.textPrimary,
                unfocusedTextColor = appColors.textPrimary
              ),
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = customNoteText,
              onValueChange = { customNoteText = it },
              label = { Text("Personal note for $partnerName") },
              placeholder = { Text("Can't wait for our reservation!") },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appColors.primary,
                unfocusedBorderColor = appColors.outlineVariant,
                focusedTextColor = appColors.textPrimary,
                unfocusedTextColor = appColors.textPrimary
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (customMoodText.isNotBlank()) {
                onAddCustomMood(customMoodText.trim(), customNoteText.trim())
                showCustomDialog = false
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
          ) {
            Text("Set Custom Mood", color = appColors.onPrimary)
          }
        },
        dismissButton = {
          TextButton(onClick = { showCustomDialog = false }) {
            Text("Cancel", color = appColors.textSecondary)
          }
        }
      )
    }
  }
}

/**
 * Floating particle for subtle delightful feedback when emotions are tapped or synced
 */
@Composable
private fun FloatingParticleItem(
  particle: FloatingParticle,
  onFinished: () -> Unit
) {
  var animationProgress by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(particle.id) {
    val duration = 1100
    val startTime = System.currentTimeMillis()
    while (true) {
      val elapsed = System.currentTimeMillis() - startTime
      val fraction = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
      animationProgress = fraction
      if (fraction >= 1f) break
      delay(16)
    }
    onFinished()
  }

  val particleAlpha = (1f - animationProgress).coerceIn(0f, 1f)
  val offsetY = -150.dp * animationProgress
  val offsetX = (Random.nextFloat() - 0.5f) * 35.dp.value * animationProgress
  val particleScale = 0.8f + (animationProgress * 0.4f)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(
        start = (particle.startXFraction * 320).dp,
        top = (particle.startYFraction * 600).dp
      )
  ) {
    Text(
      text = particle.emoji,
      fontSize = 20.sp,
      modifier = Modifier
        .offset { IntOffset(offsetX.toInt(), offsetY.toPx().toInt()) }
        .scale(particleScale)
        .rotate(offsetX * 0.4f)
        .alpha(particleAlpha)
    )
  }
}

/**
 * Tab 1: Live Emotional State Selection & Direct Sync with Partner
 */
@Composable
private fun SyncTodayContent(
  partnerName: String,
  currentPartnerMood: String,
  selectedMoodKey: String,
  selectedScore: Float,
  moodNote: String,
  onMoodSelected: (key: String, defaultScore: Float, emoji: String) -> Unit,
  onScoreChanged: (Float) -> Unit,
  onNoteChanged: (String) -> Unit,
  onSyncClick: () -> Unit,
  onAddCustomClick: () -> Unit,
  onSendNudge: (emoji: String) -> Unit
) {
  val appColors = AppTheme.colors
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 18.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Partner Current State Live Card with gentle reaction pulse
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(appColors.surfaceClay),
                contentAlignment = Alignment.Center
              ) {
                Text(text = "💖", fontSize = 22.sp)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "$partnerName's Current Vibe",
                  fontSize = 12.sp,
                  color = appColors.textMuted
                )
                Text(
                  text = currentPartnerMood,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = appColors.textPrimary
                )
              }
            }

            // Quick Nudge Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedButton(
                onClick = { onSendNudge("🫂") },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Text(text = "🫂 Hug", fontSize = 11.sp, color = appColors.primary)
              }
              OutlinedButton(
                onClick = { onSendNudge("💖") },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Text(text = "💖 Love", fontSize = 11.sp, color = appColors.secondary)
              }
            }
          }
        }
      }
    }

    // 2. Mood Grid Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Choose Your Current State",
          fontFamily = FontFamily.Serif,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.textPrimary
        )
        TextButton(
          onClick = onAddCustomClick,
          contentPadding = PaddingValues(0.dp)
        ) {
          Text(
            text = "+ Custom",
            fontSize = 12.sp,
            color = appColors.primary,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // 3. 3x3 Mood Selection Grid with spring animations, emoji tilts, and glowing halo
    item {
      val infiniteGlow = rememberInfiniteTransition(label = "glow_tile")
      val glowPulse by infiniteGlow.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
          animation = tween(1200, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
      )

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val rows = CURATED_MOODS.chunked(3)
        rows.forEach { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            rowItems.forEach { item ->
              val isSelected = selectedMoodKey.equals(item.key, ignoreCase = true) ||
                selectedMoodKey.equals(item.label, ignoreCase = true)

              val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                animationSpec = spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessLow
                ),
                label = "tile_spring_scale"
              )

              val emojiRotation by animateFloatAsState(
                targetValue = if (isSelected) 6f else 0f,
                animationSpec = spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessMedium
                ),
                label = "emoji_tilt"
              )

              val animatedContainerColor by animateColorAsState(
                targetValue = if (isSelected) appColors.surfaceOchre else appColors.surfaceContainerLow,
                animationSpec = tween(220),
                label = "tile_bg_color"
              )

              val animatedBorderColor by animateColorAsState(
                targetValue = if (isSelected) appColors.primary.copy(alpha = glowPulse) else appColors.outlineVariant.copy(alpha = 0.35f),
                animationSpec = tween(200),
                label = "tile_border_color"
              )

              Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
                modifier = Modifier
                  .weight(1f)
                  .aspectRatio(0.92f)
                  .scale(scale)
                  .border(
                    width = if (isSelected) 1.8.dp else 1.dp,
                    color = animatedBorderColor,
                    shape = RoundedCornerShape(18.dp)
                  )
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                  ) {
                    onMoodSelected(item.key, item.defaultScore, item.emoji)
                  }
                  .testTag("mood_tile_${item.key}")
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = item.emoji,
                    fontSize = 28.sp,
                    modifier = Modifier.rotate(emojiRotation)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = item.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) appColors.primary else appColors.textPrimary,
                    textAlign = TextAlign.Center
                  )
                  Text(
                    text = item.subtitle,
                    fontSize = 8.5.sp,
                    color = appColors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 1.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 4. Emotional Intensity & Score Slider with Animated Reaction & Score Meter
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainer),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Intensity & Energy Level",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
              )
              Spacer(modifier = Modifier.width(6.dp))
              // Dynamic Intensity Adjective with AnimatedContent
              AnimatedContent(
                targetState = when {
                  selectedScore >= 9.0f -> "✨ Radiant"
                  selectedScore >= 7.5f -> "💖 Warm"
                  selectedScore >= 5.0f -> "☕ Balanced"
                  else -> "🌧️ Gentle"
                },
                transitionSpec = {
                  fadeIn(tween(180)) + slideInVertically { it / 2 } togetherWith
                    fadeOut(tween(150)) + slideOutVertically { -it / 2 }
                },
                label = "intensity_label"
              ) { adjective ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(appColors.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = adjective,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary
                  )
                }
              }
            }

            Text(
              text = "${String.format("%.1f", selectedScore)} / 10",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = appColors.primary
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Slider(
            value = selectedScore,
            onValueChange = onScoreChanged,
            valueRange = 1.0f..10.0f,
            steps = 18,
            colors = SliderDefaults.colors(
              thumbColor = appColors.primary,
              activeTrackColor = appColors.primary,
              inactiveTrackColor = appColors.surfaceClay
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mood_intensity_slider")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "🌧️ Gentle Rest", fontSize = 10.sp, color = appColors.textMuted)
            Text(text = "☕ Steady Calm", fontSize = 10.sp, color = appColors.textMuted)
            Text(text = "✨ Radiant Bliss", fontSize = 10.sp, color = appColors.textMuted)
          }
        }
      }
    }

    // 5. Personal Note for Partner
    item {
      OutlinedTextField(
        value = moodNote,
        onValueChange = onNoteChanged,
        placeholder = {
          Text(
            text = "Add a quick message for $partnerName (e.g. Can't wait for our walk tonight)...",
            fontSize = 12.sp,
            color = appColors.textMuted
          )
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = appColors.surfaceContainer,
          unfocusedContainerColor = appColors.surfaceContainer,
          focusedBorderColor = appColors.primary,
          unfocusedBorderColor = appColors.outlineVariant.copy(alpha = 0.35f),
          focusedTextColor = appColors.textPrimary,
          unfocusedTextColor = appColors.textPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("mood_note_input")
      )
    }

    // 6. Action Button: Sync & Display to Partner with Tactile Scale
    item {
      var isPressed by remember { mutableStateOf(false) }
      val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
      )

      Button(
        onClick = {
          isPressed = true
          onSyncClick()
        },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Transparent,
          contentColor = appColors.onPrimary
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .scale(buttonScale)
          .clip(RoundedCornerShape(18.dp))
          .background(appColors.warmButtonBrush)
          .testTag("sync_mood_button")
      ) {
        LaunchedEffect(isPressed) {
          if (isPressed) {
            delay(120)
            isPressed = false
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = null,
            tint = appColors.onPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Sync & Display to $partnerName",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.onPrimary
          )
        }
      }
    }
  }
}

/**
 * Tab 2: Collective Trends, D3/Recharts-inspired Interactive Line Chart & History Timeline
 */
@Composable
private fun CollectiveTrendsContent(
  moodHistory: List<UserMood>,
  userName: String,
  partnerName: String,
  timeRange: String,
  onTimeRangeChanged: (String) -> Unit,
  onQuickSyncPrompt: () -> Unit
) {
  val appColors = AppTheme.colors
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 18.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Time Range Filter Selector
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Historical Overview",
          fontFamily = FontFamily.Serif,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.textPrimary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("7D", "14D", "30D").forEach { range ->
            val isSelected = timeRange == range
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) appColors.primary else appColors.surfaceContainer)
                .border(
                  width = 1.dp,
                  color = if (isSelected) appColors.primary else appColors.outlineVariant.copy(alpha = 0.4f),
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { onTimeRangeChanged(range) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("time_range_$range")
            ) {
              Text(
                text = range,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) appColors.onPrimary else appColors.textMuted
              )
            }
          }
        }
      }
    }

    // 2. Interactive Line Chart (D3 / Recharts style dual Bezier line chart)
    item {
      MoodTrendLineChart(
        moods = moodHistory,
        userName = userName,
        partnerName = partnerName,
        timeRange = timeRange
      )
    }

    // 3. Collective Harmony Analytics Stat Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Average Synergy Card
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainer),
          modifier = Modifier
            .weight(1f)
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Collective Harmony",
              fontSize = 11.sp,
              color = appColors.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "94%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.secondary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "avg",
                fontSize = 10.sp,
                color = appColors.textMuted,
                modifier = Modifier.padding(bottom = 3.dp)
              )
            }
            Text(
              text = "High Mutual Resonance",
              fontSize = 10.sp,
              color = appColors.primary
            )
          }
        }

        // Shared Peak Vibe Card
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainer),
          modifier = Modifier
            .weight(1f)
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Top Shared Vibe",
              fontSize = 11.sp,
              color = appColors.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Loved 💖",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
              )
            }
            Text(
              text = "6 days this month",
              fontSize = 10.sp,
              color = appColors.textMuted
            )
          }
        }
      }
    }

    // 4. Collective History Timeline Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Mood Logs",
          fontFamily = FontFamily.Serif,
          fontSize = 17.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.textPrimary
        )
        Text(
          text = "${moodHistory.size} entries",
          fontSize = 12.sp,
          color = appColors.textMuted
        )
      }
    }

    // 5. Timeline History Items
    if (moodHistory.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "✨", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No Mood History Yet",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Sync your first emotional state today to start building your collective mood line chart!",
              fontSize = 12.sp,
              color = appColors.textMuted,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onQuickSyncPrompt,
              colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
            ) {
              Text("Sync Today's Mood", color = appColors.onPrimary, fontSize = 12.sp)
            }
          }
        }
      }
    } else {
      items(moodHistory, key = { it.id }) { item ->
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainer),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .testTag("mood_log_item_${item.id}")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = item.dateLabel.ifBlank { "Recent" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.secondary
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(appColors.primary.copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "${item.synergyScore}% Synergy",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = appColors.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two Partner Columns
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // You
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Text(text = item.moodIcon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "$userName (${String.format("%.1f", item.moodScore)})",
                    fontSize = 11.sp,
                    color = appColors.textMuted
                  )
                  Text(
                    text = item.moodLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                  )
                }
              }

              // Partner
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Text(text = item.partnerMoodIcon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "$partnerName (${String.format("%.1f", item.partnerMoodScore)})",
                    fontSize = 11.sp,
                    color = appColors.textMuted
                  )
                  Text(
                    text = item.partnerMoodLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                  )
                }
              }
            }

            if (item.note.isNotBlank()) {
              Spacer(modifier = Modifier.height(10.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(appColors.surfaceContainerLow)
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = "💬 \"${item.note}\"",
                  fontSize = 11.sp,
                  color = appColors.textMuted,
                  fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
              }
            }
          }
        }
      }
    }
  }
}
