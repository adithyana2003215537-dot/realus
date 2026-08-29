package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.model.SharedMusic
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.paperBackground

@Composable
fun MusicScreen(
  sharedMusic: SharedMusic?,
  isLoading: Boolean,
  error: String?,
  isPlaying: Boolean,
  moodSync: Float,
  partnerName: String,
  onLoadSong: (String) -> Unit,
  onTogglePlay: () -> Unit,
  onRemoveSong: () -> Unit,
  onMoodSyncChanged: (Float) -> Unit,
  onClearError: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val keyboardController = LocalSoftwareKeyboardController.current

  var urlInput by remember { mutableStateOf("") }
  var isMuted by remember { mutableStateOf(false) }
  var volumeLevel by remember { mutableFloatStateOf(80f) }
  var playerErrorState by remember { mutableStateOf<String?>(null) }
  var songDurationSeconds by remember(sharedMusic?.videoId) { mutableStateOf(0.0) }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .paperBackground()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Listening Together",
          fontFamily = FontFamily.Serif,
          fontSize = 24.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.textPrimary
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = appColors.primary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Synced in real-time with $partnerName",
            fontSize = 12.sp,
            color = appColors.primary
          )
        }
      }

      IconButton(
        onClick = onClose,
        modifier = Modifier
          .size(36.dp)
          .testTag("close_music_screen")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = appColors.textSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Paste Link Input Section Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, appColors.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            tint = appColors.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "PASTE MUSIC LINK",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = appColors.textPrimary
          )
        }

        OutlinedTextField(
          value = urlInput,
          onValueChange = {
            urlInput = it
            if (error != null) onClearError()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("music_link_input"),
          placeholder = { Text("https://www.youtube.com/watch?v=...", fontSize = 13.sp) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
            if (urlInput.isNotBlank()) {
              onLoadSong(urlInput)
            }
          }),
          trailingIcon = {
            if (urlInput.isNotEmpty()) {
              IconButton(onClick = { urlInput = "" }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear",
                  tint = appColors.textMuted
                )
              }
            }
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = appColors.primary,
            unfocusedBorderColor = appColors.outlineVariant
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = {
              val clipText = clipboardManager.getText()?.text
              if (!clipText.isNullOrBlank()) {
                urlInput = clipText
                if (error != null) onClearError()
              }
            },
            modifier = Modifier.testTag("paste_clipboard_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.primary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentPaste,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Paste from Clipboard", fontSize = 12.sp)
          }

          Button(
            onClick = {
              keyboardController?.hide()
              if (urlInput.isNotBlank()) {
                onLoadSong(urlInput)
              }
            },
            enabled = urlInput.isNotBlank() && !isLoading,
            modifier = Modifier.testTag("load_song_button"),
            colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
            shape = RoundedCornerShape(12.dp)
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = appColors.onPrimary,
                strokeWidth = 2.dp
              )
            } else {
              Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Load Song", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Display Validation Error if present
        if (error != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = ErrorRed.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = ErrorRed,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = error,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
              )
              IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Dismiss error",
                  tint = ErrorRed,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    if (sharedMusic != null) {
      // ACTIVE SONG DISPLAY CARD & PLAYER
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
        modifier = Modifier
          .fillMaxWidth()
          .shadow(8.dp, RoundedCornerShape(24.dp))
          .border(1.dp, appColors.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Song Header Info
          val formattedDuration = remember(songDurationSeconds) { formatSongDuration(songDurationSeconds) }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              AsyncImage(
                model = sharedMusic.thumbnailUrl,
                contentDescription = sharedMusic.title,
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = sharedMusic.title,
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                  )
                  if (formattedDuration.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "($formattedDuration)",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = appColors.primary
                    )
                  }
                }
                Text(
                  text = "${sharedMusic.artist} • Added by ${sharedMusic.addedBy}",
                  fontSize = 12.sp,
                  color = appColors.textMuted,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            IconButton(
              onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sharedMusic.videoUrl))
                context.startActivity(intent)
              }
            ) {
              Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open in YouTube",
                tint = appColors.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Embedded YouTube Player Container
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(210.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            YouTubePlayerView(
              videoId = sharedMusic.videoId,
              isPlaying = sharedMusic.isPlaying,
              isMuted = isMuted,
              volume = volumeLevel.toInt(),
              onPlayerError = { err ->
                playerErrorState = err
              },
              onDurationChange = { dur ->
                songDurationSeconds = dur
              },
              modifier = Modifier.fillMaxSize()
            )

            if (playerErrorState != null) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color.Black.copy(alpha = 0.85f))
                  .padding(16.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(32.dp)
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = playerErrorState ?: "Playback restricted by YouTube",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Button(
                    onClick = {
                      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sharedMusic.videoUrl))
                      context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                  ) {
                    Text("Watch on YouTube App", fontSize = 12.sp)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Playback & Audio Controls Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Mute / Unmute Toggle
            IconButton(
              onClick = { isMuted = !isMuted }
            ) {
              Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = if (isMuted) ErrorRed else appColors.textPrimary
              )
            }

            // Play / Pause Main Button
            IconButton(
              onClick = onTogglePlay,
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(appColors.warmButtonBrush)
                .testTag("main_music_play_pause_btn")
            ) {
              Icon(
                imageVector = if (sharedMusic.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (sharedMusic.isPlaying) "Pause" else "Play",
                tint = appColors.onPrimary,
                modifier = Modifier.size(36.dp)
              )
            }

            // Remove Song Button
            IconButton(
              onClick = onRemoveSong,
              modifier = Modifier.testTag("remove_song_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove Song",
                tint = ErrorRed
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Volume Slider
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.VolumeDown,
              contentDescription = null,
              tint = appColors.textMuted,
              modifier = Modifier.size(18.dp)
            )
            Slider(
              value = volumeLevel,
              onValueChange = {
                volumeLevel = it
                if (isMuted && it > 0f) isMuted = false
              },
              valueRange = 0f..100f,
              modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
              colors = SliderDefaults.colors(
                thumbColor = appColors.primary,
                activeTrackColor = appColors.primary,
                inactiveTrackColor = appColors.surfaceContainerHigh
              )
            )
            Icon(
              imageVector = Icons.Default.VolumeUp,
              contentDescription = null,
              tint = appColors.textMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    } else {
      // EMPTY STATE WHEN NO SONG IS SELECTED
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(appColors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MusicNote,
              contentDescription = null,
              tint = appColors.primary,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "No Song Playing",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Copy a YouTube or YouTube Music share link and paste it above to listen together in real-time!",
            fontSize = 13.sp,
            color = appColors.textMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Mood Sync Slider Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "MOOD SYNC",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = appColors.secondary
          )
          Text(
            text = when {
              moodSync < 0.35f -> "Chill 🕯️"
              moodSync < 0.7f -> "Vibe ✨"
              else -> "Energy 🔥"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.primary
          )
        }

        Slider(
          value = moodSync,
          onValueChange = onMoodSyncChanged,
          colors = SliderDefaults.colors(
            thumbColor = appColors.secondary,
            activeTrackColor = appColors.secondary,
            inactiveTrackColor = appColors.surfaceContainerHighest
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Chill", fontSize = 11.sp, color = appColors.textMuted)
          Text(text = "Vibe", fontSize = 11.sp, color = appColors.textMuted)
          Text(text = "Energy", fontSize = 11.sp, color = appColors.textMuted)
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun YouTubePlayerView(
  videoId: String,
  isPlaying: Boolean,
  isMuted: Boolean,
  volume: Int,
  onPlayerError: (String) -> Unit,
  onDurationChange: (Double) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var webViewRef by remember { mutableStateOf<WebView?>(null) }

  LaunchedEffect(isPlaying, webViewRef) {
    val wv = webViewRef ?: return@LaunchedEffect
    if (isPlaying) {
      wv.evaluateJavascript("playVideo()", null)
    } else {
      wv.evaluateJavascript("pauseVideo()", null)
    }
  }

  LaunchedEffect(isMuted, webViewRef) {
    val wv = webViewRef ?: return@LaunchedEffect
    if (isMuted) {
      wv.evaluateJavascript("mute()", null)
    } else {
      wv.evaluateJavascript("unMute()", null)
    }
  }

  LaunchedEffect(volume, webViewRef) {
    val wv = webViewRef ?: return@LaunchedEffect
    wv.evaluateJavascript("setVolume($volume)", null)
  }

  AndroidView(
    factory = { ctx ->
      WebView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.allowFileAccess = true
        settings.allowContentAccess = true

        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {
          override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
          ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            onPlayerError(description ?: "Playback error")
          }
        }

        addJavascriptInterface(object {
          @android.webkit.JavascriptInterface
          fun onPlayerReady() {
            if (isPlaying) {
              post { evaluateJavascript("playVideo()", null) }
            }
          }

          @android.webkit.JavascriptInterface
          fun onDurationReady(seconds: Double) {
            post { onDurationChange(seconds) }
          }

          @android.webkit.JavascriptInterface
          fun onError(errorCode: Int) {
            val msg = when (errorCode) {
              2 -> "Invalid YouTube link parameters."
              5 -> "HTML5 player error."
              100 -> "YouTube video not found or private."
              101, 150 -> "External playback disabled by video owner."
              else -> "YouTube error ($errorCode)"
            }
            post { onPlayerError(msg) }
          }
        }, "AndroidInterface")

        val htmlContent = getYouTubeIFrameHtml(videoId, isPlaying)
        loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "utf-8", null)
        webViewRef = this
      }
    },
    update = { wv ->
      webViewRef = wv
    },
    modifier = modifier
  )

  DisposableEffect(videoId) {
    onDispose {
      webViewRef?.let { wv ->
        wv.stopLoading()
        wv.loadUrl("about:blank")
        wv.destroy()
      }
      webViewRef = null
    }
  }
}

private fun formatSongDuration(seconds: Double): String {
  if (seconds <= 0) return ""
  val totalSec = seconds.toLong()
  val hrs = totalSec / 3600
  val mins = (totalSec % 3600) / 60
  val secs = totalSec % 60
  return if (hrs > 0) {
    String.format("%d:%02d:%02d", hrs, mins, secs)
  } else {
    String.format("%d:%02d", mins, secs)
  }
}

private fun getYouTubeIFrameHtml(videoId: String, autoplay: Boolean): String {
  val autoPlayVal = if (autoplay) 1 else 0
  return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
            body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #000; overflow: hidden; display: flex; align-items: center; justify-content: center; }
            #player { width: 100%; height: 100%; border: none; }
        </style>
    </head>
    <body>
        <div id="player"></div>
        <script>
            var tag = document.createElement('script');
            tag.src = "https://www.youtube.com/iframe_api";
            var firstScriptTag = document.getElementsByTagName('script')[0];
            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

            var player;
            function sendDuration() {
                if (window.AndroidInterface && window.AndroidInterface.onDurationReady && player && player.getDuration) {
                    var dur = player.getDuration();
                    if (dur > 0) {
                        window.AndroidInterface.onDurationReady(dur);
                    }
                }
            }

            function onYouTubeIframeAPIReady() {
                player = new YT.Player('player', {
                    height: '100%',
                    width: '100%',
                    videoId: '$videoId',
                    playerVars: {
                        'playsinline': 1,
                        'autoplay': $autoPlayVal,
                        'controls': 1,
                        'modestbranding': 1,
                        'rel': 0,
                        'enablejsapi': 1,
                        'origin': 'https://www.youtube.com'
                    },
                    events: {
                        'onReady': function(event) {
                            if (window.AndroidInterface && window.AndroidInterface.onPlayerReady) {
                                window.AndroidInterface.onPlayerReady();
                            }
                            sendDuration();
                        },
                        'onStateChange': function(event) {
                            sendDuration();
                        },
                        'onError': function(event) {
                            if (window.AndroidInterface && window.AndroidInterface.onError) {
                                window.AndroidInterface.onError(event.data);
                            }
                        }
                    }
                });
            }

            function playVideo() { if(player && player.playVideo) player.playVideo(); }
            function pauseVideo() { if(player && player.pauseVideo) player.pauseVideo(); }
            function seekTo(sec) { if(player && player.seekTo) player.seekTo(sec, true); }
            function mute() { if(player && player.mute) player.mute(); }
            function unMute() { if(player && player.unMute) player.unMute(); }
            function setVolume(vol) { if(player && player.setVolume) player.setVolume(vol); }
        </script>
    </body>
    </html>
  """.trimIndent()
}
