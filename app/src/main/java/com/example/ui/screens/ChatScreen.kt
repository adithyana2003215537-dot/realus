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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.CoupleSettings
import com.example.ui.theme.AppTheme
import com.example.ui.theme.paperBackground

@Composable
fun ChatScreen(
  messages: List<ChatMessage>,
  coupleSettings: CoupleSettings?,
  isVoiceNotePlaying: Boolean,
  voiceNoteProgress: Float,
  onSendMessage: (String) -> Unit,
  onTriggerReaction: (String) -> Unit,
  onToggleVoiceNote: () -> Unit,
  onStartAudioCall: () -> Unit,
  onStartVideoCall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .paperBackground()
      .imePadding()
  ) {
    val partnerDisplayName = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner"

    // Chat Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(appColors.surfaceContainerLow)
        .border(1.dp, appColors.outlineVariant.copy(alpha = 0.25f))
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(modifier = Modifier.size(42.dp)) {
        AsyncImage(
          model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q",
          contentDescription = partnerDisplayName,
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .border(1.5.dp, appColors.primary, CircleShape),
          contentScale = ContentScale.Crop
        )

        // Green Online Indicator Dot
        Box(
          modifier = Modifier
            .size(11.dp)
            .clip(CircleShape)
            .background(Color(0xFF2E7D32))
            .border(1.5.dp, appColors.surfaceContainerLow, CircleShape)
            .align(Alignment.BottomEnd)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = partnerDisplayName,
          fontFamily = FontFamily.Serif,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.textPrimary
        )
        Text(
          text = "Connected in RealUs",
          fontSize = 12.sp,
          color = Color(0xFF2E7D32)
        )
      }

      IconButton(
        onClick = onStartAudioCall,
        modifier = Modifier.size(40.dp).testTag("chat_audio_call_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Call,
          contentDescription = "Audio Call",
          tint = appColors.textSecondary
        )
      }

      IconButton(
        onClick = onStartVideoCall,
        modifier = Modifier.size(40.dp).testTag("chat_video_call_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Videocam,
          contentDescription = "Video Call",
          tint = appColors.textSecondary
        )
      }
    }

    // Message History List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        // Date Header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "TODAY, 10:42 AM",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = appColors.textMuted,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(appColors.surfaceContainerHigh.copy(alpha = 0.8f))
              .padding(horizontal = 12.dp, vertical = 4.dp)
          )
        }
      }

      items(messages, key = { it.id }) { message ->
        val isMe = message.sender == "you"
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
          verticalAlignment = Alignment.Bottom
        ) {
          if (!isMe) {
            AsyncImage(
              model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q",
              contentDescription = "Partner Avatar",
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .padding(bottom = 2.dp),
              contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
          }

          Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.82f)
          ) {
            if (message.isAudio) {
              // Audio Player Bubble
              AudioMessageBubble(
                isPlaying = isVoiceNotePlaying,
                progress = voiceNoteProgress,
                durationText = message.audioDuration.ifEmpty { "0:42" },
                onTogglePlay = onToggleVoiceNote
              )
            } else if (message.isImage) {
              // Image Message Bubble
              ImageMessageBubble(
                imageUrl = message.imageUrl,
                caption = message.imageCaption
              )
            } else {
              // Regular Text Bubble
              val bubbleBg = if (isMe) {
                if (appColors.isDark) appColors.surfaceOchre else appColors.primaryContainer
              } else {
                if (appColors.isDark) appColors.surfaceContainerHigh else appColors.surfaceContainerLow
              }
              val bubbleTextColor = if (isMe) {
                if (appColors.isDark) appColors.textPrimary else appColors.onPrimaryContainer
              } else {
                appColors.textPrimary
              }

              Card(
                shape = RoundedCornerShape(
                  topStart = 18.dp,
                  topEnd = 18.dp,
                  bottomStart = if (isMe) 18.dp else 4.dp,
                  bottomEnd = if (isMe) 4.dp else 18.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleBg),
                modifier = Modifier.border(
                  width = 1.dp,
                  color = if (isMe) appColors.primary.copy(alpha = 0.35f) else appColors.outlineVariant.copy(alpha = 0.35f),
                  shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 18.dp
                  )
                )
              ) {
                Text(
                  text = message.text,
                  color = bubbleTextColor,
                  fontSize = 15.sp,
                  lineHeight = 21.sp,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
              }
            }

            // Timestamp and Status
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            ) {
              Text(
                text = message.timestamp,
                fontSize = 10.sp,
                color = appColors.textMuted
              )
              if (isMe) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.DoneAll,
                  contentDescription = "Read",
                  tint = appColors.primary,
                  modifier = Modifier.size(13.dp)
                )
              }
            }
          }
        }
      }

      // Partner is typing indicator
      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 4.dp)
        ) {
          AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q",
            contentDescription = "Typing partner",
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape),
            contentScale = ContentScale.Crop
          )
          Spacer(modifier = Modifier.width(8.dp))
          TypingIndicator()
        }
      }
    }

    // Floating Intimacy Reaction Buttons Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      ReactionPill(emoji = "❤️", label = "Love", onClick = { onTriggerReaction("❤️") })
      Spacer(modifier = Modifier.width(12.dp))
      ReactionPill(emoji = "🫂", label = "Hug", onClick = { onTriggerReaction("🫂") })
      Spacer(modifier = Modifier.width(12.dp))
      ReactionPill(emoji = "💋", label = "Kiss", onClick = { onTriggerReaction("💋") })
    }

    // Input Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(appColors.surfaceContainerLow)
        .border(1.dp, appColors.outlineVariant.copy(alpha = 0.25f))
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = {
          onSendMessage("Shared a photo with love 📸")
        },
        modifier = Modifier.size(40.dp).testTag("chat_attach_btn")
      ) {
        Icon(
          imageVector = Icons.Default.AddPhotoAlternate,
          contentDescription = "Attach Photo",
          tint = appColors.textSecondary
        )
      }

      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = {
          Text(
            text = "Say something sweet...",
            fontSize = 14.sp,
            color = appColors.textMuted
          )
        },
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 6.dp)
          .testTag("chat_input_field"),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = appColors.surfaceContainerHigh,
          unfocusedContainerColor = appColors.surfaceContainerHigh,
          focusedBorderColor = appColors.primary,
          unfocusedBorderColor = Color.Transparent,
          focusedTextColor = appColors.textPrimary,
          unfocusedTextColor = appColors.textPrimary
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
          onSend = {
            if (inputText.isNotBlank()) {
              onSendMessage(inputText)
              inputText = ""
            }
          }
        ),
        maxLines = 3
      )

      if (inputText.isNotBlank()) {
        IconButton(
          onClick = {
            onSendMessage(inputText)
            inputText = ""
          },
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(appColors.warmButtonBrush)
            .testTag("chat_send_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = appColors.onPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      } else {
        IconButton(
          onClick = onToggleVoiceNote,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(appColors.surfaceContainerHighest)
            .testTag("chat_mic_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Record Voice Note",
            tint = appColors.primary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun AudioMessageBubble(
  isPlaying: Boolean,
  progress: Float,
  durationText: String,
  onTogglePlay: () -> Unit
) {
  val appColors = AppTheme.colors
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
    modifier = Modifier
      .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
      .clickable { onTogglePlay() }
      .testTag("audio_message_bubble")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(appColors.primary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "Pause" else "Play",
          tint = appColors.onPrimary,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Simulated Waveform Bars
      Row(
        modifier = Modifier.width(120.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val barHeights = listOf(12, 22, 16, 28, 18, 24, 14, 20, 26, 12, 18, 24, 16, 20)
        barHeights.forEachIndexed { index, height ->
          val barProgress = index.toFloat() / barHeights.size
          val isPassed = barProgress <= progress
          Box(
            modifier = Modifier
              .width(3.dp)
              .height(height.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(if (isPassed) appColors.primary else appColors.outlineVariant)
          )
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Text(
        text = durationText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = appColors.textMuted
      )
    }
  }
}

@Composable
private fun ImageMessageBubble(
  imageUrl: String,
  caption: String
) {
  val appColors = AppTheme.colors
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
    modifier = Modifier
      .border(1.dp, appColors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
      .testTag("image_message_bubble")
  ) {
    Column(modifier = Modifier.padding(6.dp)) {
      AsyncImage(
        model = imageUrl,
        contentDescription = caption,
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
          .clip(RoundedCornerShape(14.dp)),
        contentScale = ContentScale.Crop
      )
      if (caption.isNotBlank()) {
        Text(
          text = caption,
          fontSize = 12.sp,
          color = appColors.textPrimary,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
private fun TypingIndicator() {
  val appColors = AppTheme.colors
  val infiniteTransition = rememberInfiniteTransition(label = "dots")
  val dot1 by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot1"
  )
  val dot2 by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot2"
  )
  val dot3 by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot3"
  )

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(appColors.surfaceContainerHigh)
      .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.size(6.dp).scale(dot1).clip(CircleShape).background(appColors.primary))
    Box(modifier = Modifier.size(6.dp).scale(dot2).clip(CircleShape).background(appColors.primary))
    Box(modifier = Modifier.size(6.dp).scale(dot3).clip(CircleShape).background(appColors.primary))
  }
}

@Composable
private fun ReactionPill(
  emoji: String,
  label: String,
  onClick: () -> Unit
) {
  val appColors = AppTheme.colors
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(appColors.surfaceContainerHigh)
      .border(1.dp, appColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .testTag("reaction_pill_$label"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = emoji, fontSize = 16.sp)
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = appColors.textPrimary
    )
  }
}
