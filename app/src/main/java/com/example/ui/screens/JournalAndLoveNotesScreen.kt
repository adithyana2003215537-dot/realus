package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.CoupleSettings
import com.example.data.model.JournalEntry
import com.example.data.model.LoveNote
import com.example.ui.audio.AudioLoveNoteManager
import com.example.ui.audio.RecorderStatus
import com.example.ui.theme.AppTheme

enum class JournalSectionTab(val title: String, val icon: ImageVector) {
  ALL("All Memories", Icons.Default.AutoStories),
  JOURNAL("Journal Chapters", Icons.Default.AutoStories),
  LOVE_NOTES("Love Notes Board", Icons.Default.PushPin)
}

@Composable
fun JournalAndLoveNotesScreen(
  journalEntries: List<JournalEntry>,
  loveNotes: List<LoveNote>,
  coupleSettings: CoupleSettings? = null,
  initialTab: JournalSectionTab = JournalSectionTab.ALL,
  onAddJournalEntry: (title: String, category: String, body: String, imageUrl: String) -> Unit,
  onAddLoveNote: (text: String, bgType: String) -> Unit,
  onAddAudioLoveNote: ((audioPath: String, durationSec: Int, captionText: String, bgType: String, isPinned: Boolean) -> Unit)? = null,
  onAddAudioJournalEntry: ((title: String, category: String, body: String, audioPath: String, durationSec: Int, imageUrl: String) -> Unit)? = null,
  onDeleteLoveNote: (id: Long) -> Unit,
  onTriggerReaction: ((String) -> Unit)? = null,
  onClose: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val appColors = AppTheme.colors
  val partner1Name = coupleSettings?.partner1Name ?: "Alex"
  val partner2Name = coupleSettings?.partner2Name ?: "Jamie"

  // Audio Manager Setup
  val audioManager = remember { AudioLoveNoteManager(context) }
  DisposableEffect(Unit) {
    onDispose {
      audioManager.release()
    }
  }

  val recorderStatus by audioManager.recorderStatus.collectAsStateWithLifecycle()
  val recordingDurationSec by audioManager.recordingDurationSec.collectAsStateWithLifecycle()
  val previewDurationSec by audioManager.previewDurationSec.collectAsStateWithLifecycle()
  val previewProgress by audioManager.previewProgress.collectAsStateWithLifecycle()
  val liveAmplitudes by audioManager.liveAmplitudes.collectAsStateWithLifecycle()
  val currentlyPlayingId by audioManager.currentlyPlayingId.collectAsStateWithLifecycle()
  val activePlaybackProgress by audioManager.activePlaybackProgress.collectAsStateWithLifecycle()
  val activePlaybackCurrentSec by audioManager.activePlaybackCurrentSec.collectAsStateWithLifecycle()
  val activePlaybackTotalSec by audioManager.activePlaybackTotalSec.collectAsStateWithLifecycle()

  // Permission Launcher
  var permissionGranted by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    permissionGranted = isGranted
    if (isGranted) {
      audioManager.startRecording()
    }
  }

  var selectedTab by remember { mutableStateOf(initialTab) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryFilter by remember { mutableStateOf("All") }

  var showCreateSheet by remember { mutableStateOf(false) }
  var showAddJournalModal by remember { mutableStateOf(false) }
  var showAddNoteModal by remember { mutableStateOf(false) }
  var showRecordVoiceModal by remember { mutableStateOf(false) }
  var showPromptModal by remember { mutableStateOf(false) }

  // Local likes tracking
  val favoriteJournals = remember { mutableStateMapOf<Long, Boolean>() }

  // Filtered lists
  val filteredLoveNotes = remember(loveNotes, searchQuery, selectedCategoryFilter) {
    loveNotes.filter { note ->
      val matchesSearch = searchQuery.isBlank() ||
        note.text.contains(searchQuery, ignoreCase = true) ||
        note.author.contains(searchQuery, ignoreCase = true)

      val matchesCategory = when (selectedCategoryFilter) {
        "All" -> true
        "Voice Notes" -> note.isAudioNote || note.audioFilePath.isNotBlank()
        "Pinned" -> note.isPinned
        "Jamie" -> note.author.contains("Jamie", ignoreCase = true) || note.author.contains(partner2Name, ignoreCase = true)
        "YOU" -> note.author.contains("YOU", ignoreCase = true) || note.author.contains(partner1Name, ignoreCase = true)
        else -> true
      }
      matchesSearch && matchesCategory
    }
  }

  val filteredJournalEntries = remember(journalEntries, searchQuery, selectedCategoryFilter) {
    journalEntries.filter { entry ->
      val matchesSearch = searchQuery.isBlank() ||
        entry.title.contains(searchQuery, ignoreCase = true) ||
        entry.body.contains(searchQuery, ignoreCase = true) ||
        entry.category.contains(searchQuery, ignoreCase = true)

      val matchesCategory = when (selectedCategoryFilter) {
        "All" -> true
        "Voice Notes" -> entry.isAudioAttached || entry.audioFilePath.isNotBlank()
        "Favorites" -> favoriteJournals[entry.id] ?: entry.isFavorite
        "Reflections" -> entry.category.contains("Reflection", ignoreCase = true)
        "Trips" -> entry.category.contains("Trip", ignoreCase = true) || entry.category.contains("Kyoto", ignoreCase = true)
        else -> true
      }
      matchesSearch && matchesCategory
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(appColors.background)
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
      // 1. Unified Header Banner with Romantic Background
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
          AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCn64GnZ3Zt8W_OYyjg-h7pA_fjoxKjqw5FLHD-U83VgmY0jUAiD2gvHP-taMjnTy7dT_5qkr-_iVaxCAsuvnjn6sEGZOPX__rH3V06W0s7Ww9XNifnuyqTVNdA2fFCzPuMdmeYrrAAzGm-HEBL9Nh-lwfjs8Aj6eXzq9EsGci0QW3y28_46M9wTMs1SBIQZGBb-xkeLcm2zp5yKam9A5XEIrIWaesx2ara5AR2HhPGrA-5mULSIw",
            contentDescription = "Journal & Notes Header",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    appColors.background.copy(alpha = 0.35f),
                    appColors.background.copy(alpha = 0.85f),
                    appColors.background
                  )
                )
              )
          )

          if (onClose != null) {
            IconButton(
              onClick = onClose,
              modifier = Modifier
                .padding(start = 12.dp, top = 12.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(appColors.surfaceContainer.copy(alpha = 0.8f))
                .align(Alignment.TopStart)
                .testTag("journal_back_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = appColors.textPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Our Journal & Love Notes",
                  fontFamily = FontFamily.Serif,
                  fontSize = 25.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = appColors.textPrimary
                )
                Text(
                  text = "Voice whispers, reflections & sweet notes",
                  fontSize = 12.sp,
                  color = appColors.textMuted,
                  modifier = Modifier.padding(top = 2.dp)
                )
              }

              // Quick stats chip
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(appColors.surfaceContainerHigh)
                  .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = "${journalEntries.size} Chapters • ${loveNotes.size} Notes",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = appColors.secondary
                )
              }
            }
          }
        }
      }

      // 2. Tab Selector Row (All Memories, Journal Chapters, Love Notes Board)
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = appColors.surfaceContainerLow,
            contentColor = appColors.primary,
            indicator = { tabPositions ->
              TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = appColors.primary,
                height = 3.dp
              )
            },
            divider = {},
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
          ) {
            JournalSectionTab.values().forEach { tab ->
              val isSelected = selectedTab == tab
              Tab(
                selected = isSelected,
                onClick = {
                  selectedTab = tab
                  audioManager.stopAllPlayback()
                },
                modifier = Modifier.padding(vertical = 10.dp),
                text = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                  ) {
                    Icon(
                      imageVector = tab.icon,
                      contentDescription = null,
                      tint = if (isSelected) appColors.primary else appColors.textMuted,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = tab.title,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) appColors.primary else appColors.textMuted
                    )
                  }
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Search and Filters Bar
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search voice notes, sticky notes, memories...", fontSize = 13.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = appColors.textMuted,
                modifier = Modifier.size(18.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = appColors.textMuted,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("journal_notes_search_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = appColors.surfaceContainerLow,
              unfocusedContainerColor = appColors.surfaceContainerLow,
              focusedBorderColor = appColors.primary,
              unfocusedBorderColor = appColors.outlineVariant.copy(alpha = 0.3f),
              focusedTextColor = appColors.textPrimary,
              unfocusedTextColor = appColors.textPrimary
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Filter tags row based on current tab
          val filterTags = when (selectedTab) {
            JournalSectionTab.ALL -> listOf("All", "🎙️ Voice Notes", "Pinned", "Favorites", "Jamie", "YOU", "Reflections", "Trips")
            JournalSectionTab.JOURNAL -> listOf("All", "🎙️ Voice Notes", "Favorites", "Reflections", "Trips")
            JournalSectionTab.LOVE_NOTES -> listOf("All", "🎙️ Voice Notes", "Pinned", "Jamie", "YOU")
          }

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
          ) {
            items(filterTags) { tag ->
              val isSelected = selectedCategoryFilter == tag
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSelected) appColors.primary else appColors.surfaceContainerHigh)
                  .border(
                    1.dp,
                    if (isSelected) appColors.primary else appColors.outlineVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                  )
                  .clickable { selectedCategoryFilter = tag }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = tag,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (isSelected) appColors.onPrimary else appColors.textSecondary
                )
              }
            }
          }
        }
      }

      // 3. Shared Prompt & Quick Voice Record Action Bar
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceOchre),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, appColors.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .testTag("journal_daily_prompt_banner")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = appColors.secondary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "OUR DAILY PROMPT",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = appColors.secondary
                )
              }

              // Voice memo badge
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(appColors.primary.copy(alpha = 0.15f))
                  .clickable {
                    showRecordVoiceModal = true
                  }
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = null,
                  tint = appColors.primary,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Whisper Note",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = appColors.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "What was the sweetest surprise from our Kyoto trip?",
              fontFamily = FontFamily.Serif,
              fontSize = 16.sp,
              lineHeight = 22.sp,
              color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = { showPromptModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .height(36.dp)
                  .testTag("reflect_prompt_btn")
              ) {
                Text("Reflect Together", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
              }

              Button(
                onClick = { showRecordVoiceModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = appColors.secondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .height(36.dp)
                  .testTag("record_audio_note_btn")
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Record Voice Note", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
              }
            }
          }
        }
      }

      // 4. Featured Pinned Love Notes Horizontal Carousel
      if (selectedTab == JournalSectionTab.ALL || selectedTab == JournalSectionTab.JOURNAL) {
        val pinnedNotes = loveNotes.filter { it.isPinned }
        if (pinnedNotes.isNotEmpty()) {
          item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = appColors.secondary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "PINNED LOVE NOTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = appColors.textMuted
                  )
                }

                Text(
                  text = "View Board →",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = appColors.primary,
                  modifier = Modifier.clickable { selectedTab = JournalSectionTab.LOVE_NOTES }
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                items(pinnedNotes, key = { "carousel_note_${it.id}" }) { note ->
                  val isPlaying = currentlyPlayingId == "note_${note.id}"
                  PinnedNoteMiniCard(
                    note = note,
                    isPlaying = isPlaying,
                    onPlayAudio = {
                      val audioPath = note.audioFilePath.ifBlank { "simulated_note_${note.id}" }
                      audioManager.playCardAudio("note_${note.id}", audioPath, note.audioDurationSec.coerceAtLeast(10))
                    },
                    onDelete = { onDeleteLoveNote(note.id) },
                    onReact = { onTriggerReaction?.invoke("💖") }
                  )
                }
              }
            }
          }
        }
      }

      // 5. Main Content Stream
      when (selectedTab) {
        JournalSectionTab.ALL -> {
          item {
            Text(
              text = "RECENT CHAPTERS & NOTES",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = appColors.textMuted,
              modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
            )
          }

          items(filteredJournalEntries, key = { "journal_${it.id}" }) { entry ->
            val isFav = favoriteJournals[entry.id] ?: entry.isFavorite
            val isAudioPlaying = currentlyPlayingId == "journal_${entry.id}"
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
              JournalEntryCard(
                entry = entry,
                isFavorite = isFav,
                isAudioPlaying = isAudioPlaying,
                playbackProgress = if (isAudioPlaying) activePlaybackProgress else 0f,
                playbackCurrentSec = if (isAudioPlaying) activePlaybackCurrentSec else 0,
                onToggleFavorite = { favoriteJournals[entry.id] = !isFav },
                onPlayAudio = {
                  val audioPath = entry.audioFilePath.ifBlank { "simulated_journal_${entry.id}" }
                  audioManager.playCardAudio("journal_${entry.id}", audioPath, entry.audioDurationSec.coerceAtLeast(15))
                }
              )
            }
          }

          items(filteredLoveNotes, key = { "note_${it.id}" }) { note ->
            val isAudioPlaying = currentlyPlayingId == "note_${note.id}"
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
              StickyLoveNoteCard(
                note = note,
                isAudioPlaying = isAudioPlaying,
                playbackProgress = if (isAudioPlaying) activePlaybackProgress else 0f,
                playbackCurrentSec = if (isAudioPlaying) activePlaybackCurrentSec else 0,
                onPlayAudio = {
                  val audioPath = note.audioFilePath.ifBlank { "simulated_note_${note.id}" }
                  audioManager.playCardAudio("note_${note.id}", audioPath, note.audioDurationSec.coerceAtLeast(10))
                },
                onDelete = { onDeleteLoveNote(note.id) },
                onReact = { onTriggerReaction?.invoke(it) }
              )
            }
          }
        }

        JournalSectionTab.JOURNAL -> {
          item {
            Text(
              text = "CHRONICLED CHAPTERS (${filteredJournalEntries.size})",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = appColors.textMuted,
              modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
            )
          }

          if (filteredJournalEntries.isEmpty()) {
            item {
              EmptyStateBox(
                icon = Icons.Default.AutoStories,
                title = "No Chapters Found",
                subtitle = "Start recording memories together by creating a new journal chapter or audio memo."
              )
            }
          } else {
            items(filteredJournalEntries, key = { "chapter_${it.id}" }) { entry ->
              val isFav = favoriteJournals[entry.id] ?: entry.isFavorite
              val isAudioPlaying = currentlyPlayingId == "journal_${entry.id}"
              Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                JournalEntryCard(
                  entry = entry,
                  isFavorite = isFav,
                  isAudioPlaying = isAudioPlaying,
                  playbackProgress = if (isAudioPlaying) activePlaybackProgress else 0f,
                  playbackCurrentSec = if (isAudioPlaying) activePlaybackCurrentSec else 0,
                  onToggleFavorite = { favoriteJournals[entry.id] = !isFav },
                  onPlayAudio = {
                    val audioPath = entry.audioFilePath.ifBlank { "simulated_journal_${entry.id}" }
                    audioManager.playCardAudio("journal_${entry.id}", audioPath, entry.audioDurationSec.coerceAtLeast(15))
                  }
                )
              }
            }
          }
        }

        JournalSectionTab.LOVE_NOTES -> {
          item {
            Text(
              text = "SHARED CORKBOARD (${filteredLoveNotes.size} NOTES)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = appColors.textMuted,
              modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
            )
          }

          if (filteredLoveNotes.isEmpty()) {
            item {
              EmptyStateBox(
                icon = Icons.Default.PushPin,
                title = "No Love Notes Found",
                subtitle = "Record a sweet voice love note or write an affirmation for your partner."
              )
            }
          } else {
            items(filteredLoveNotes, key = { "board_note_${it.id}" }) { note ->
              val isAudioPlaying = currentlyPlayingId == "note_${note.id}"
              Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                StickyLoveNoteCard(
                  note = note,
                  isAudioPlaying = isAudioPlaying,
                  playbackProgress = if (isAudioPlaying) activePlaybackProgress else 0f,
                  playbackCurrentSec = if (isAudioPlaying) activePlaybackCurrentSec else 0,
                  onPlayAudio = {
                    val audioPath = note.audioFilePath.ifBlank { "simulated_note_${note.id}" }
                    audioManager.playCardAudio("note_${note.id}", audioPath, note.audioDurationSec.coerceAtLeast(10))
                  },
                  onDelete = { onDeleteLoveNote(note.id) },
                  onReact = { onTriggerReaction?.invoke(it) }
                )
              }
            }
          }
        }
      }
    }

    // Unified Creation Floating Action Button
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 20.dp)
    ) {
      FloatingActionButton(
        onClick = { showCreateSheet = true },
        containerColor = appColors.primaryDark,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(56.dp)
          .testTag("unified_journal_notes_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Chapter or Note")
      }
    }
  }

  // Quick Action Choice Dialog
  if (showCreateSheet) {
    AlertDialog(
      onDismissRequest = { showCreateSheet = false },
      containerColor = appColors.surfaceContainer,
      title = {
        Text(
          text = "Create for Us",
          fontFamily = FontFamily.Serif,
          color = appColors.textPrimary
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // Option 1: Record Voice Love Note
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(appColors.surfaceContainerHigh)
              .clickable {
                showCreateSheet = false
                showRecordVoiceModal = true
              }
              .padding(14.dp)
              .testTag("action_record_audio_note"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(appColors.primary.copy(alpha = 0.18f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = appColors.primary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Record Voice Love Note",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
              )
              Text(
                text = "Whisper a voice memo using microphone",
                fontSize = 11.sp,
                color = appColors.textMuted
              )
            }
          }

          // Option 2: Journal Chapter
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(appColors.surfaceContainerHigh)
              .clickable {
                showCreateSheet = false
                showAddJournalModal = true
              }
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(appColors.secondary.copy(alpha = 0.18f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = appColors.secondary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "New Journal Chapter",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
              )
              Text(
                text = "Long-form reflections, trips & memories with photos",
                fontSize = 11.sp,
                color = appColors.textMuted
              )
            }
          }

          // Option 3: Sticky Love Note
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(appColors.surfaceContainerHigh)
              .clickable {
                showCreateSheet = false
                showAddNoteModal = true
              }
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF8D6E63).copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = appColors.secondary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Pin a Sticky Love Note",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
              )
              Text(
                text = "Colorful digital corkboard note with sticky styles",
                fontSize = 11.sp,
                color = appColors.textMuted
              )
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showCreateSheet = false }) {
          Text("Cancel", color = appColors.textSecondary)
        }
      }
    )
  }

  // Audio Voice Recording Studio Modal
  if (showRecordVoiceModal) {
    AudioRecordLoveNoteModal(
      recorderStatus = recorderStatus,
      recordingDurationSec = recordingDurationSec,
      previewDurationSec = previewDurationSec,
      previewProgress = previewProgress,
      liveAmplitudes = liveAmplitudes,
      onStartRecording = {
        if (permissionGranted) {
          audioManager.startRecording()
        } else {
          permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
      },
      onStopRecording = {
        audioManager.stopRecording()
      },
      onCancelRecording = {
        audioManager.cancelRecording()
        showRecordVoiceModal = false
      },
      onPlayPreview = {
        audioManager.playPreview()
      },
      onPausePreview = {
        audioManager.pausePreview()
      },
      onReRecord = {
        audioManager.cancelRecording()
        if (permissionGranted) {
          audioManager.startRecording()
        } else {
          permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
      },
      onSaveVoiceNote = { caption, bgType, isPinned, saveAsChapter ->
        val recordedPath = audioManager.getFinalRecordedAudioPath() ?: "local_recording_${System.currentTimeMillis()}"
        val duration = previewDurationSec.coerceAtLeast(recordingDurationSec).coerceAtLeast(1)

        if (saveAsChapter) {
          if (onAddAudioJournalEntry != null) {
            onAddAudioJournalEntry(
              caption.ifBlank { "Voice Memory" },
              "Voice Memo",
              caption.ifBlank { "Recorded voice note for us." },
              recordedPath,
              duration,
              ""
            )
          } else {
            onAddJournalEntry(caption.ifBlank { "Voice Memory" }, "Voice Memo", "🎙️ Spoken voice note", "")
          }
        } else {
          if (onAddAudioLoveNote != null) {
            onAddAudioLoveNote(recordedPath, duration, caption, bgType, isPinned)
          } else {
            onAddLoveNote(caption.ifBlank { "Voice whisper for you 🎙️" }, bgType)
          }
        }
        audioManager.resetRecorderState()
        showRecordVoiceModal = false
      },
      onDismiss = {
        audioManager.cancelRecording()
        showRecordVoiceModal = false
      }
    )
  }

  // Add Journal Chapter Modal
  if (showAddJournalModal) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Memories") }
    var body by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showAddJournalModal = false },
      containerColor = appColors.surfaceContainer,
      title = {
        Text(
          text = "New Journal Chapter",
          fontFamily = FontFamily.Serif,
          color = appColors.textPrimary
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Chapter Title") },
            placeholder = { Text("e.g. Stargazing on the Coast") },
            modifier = Modifier.fillMaxWidth().testTag("journal_title_input")
          )
          OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (e.g. Kyoto Trip, Date Night)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Your reflection & thoughts...") },
            maxLines = 5,
            modifier = Modifier.fillMaxWidth().testTag("journal_body_input")
          )
          OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Photo URL (Optional)") },
            placeholder = { Text("https://...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank() && body.isNotBlank()) {
              onAddJournalEntry(title, category, body, imageUrl)
              showAddJournalModal = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryDark),
          modifier = Modifier.testTag("save_journal_btn")
        ) {
          Text("Save Chapter", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddJournalModal = false }) {
          Text("Cancel", color = appColors.textSecondary)
        }
      }
    )
  }

  // Add Love Note Modal
  if (showAddNoteModal) {
    var noteText by remember { mutableStateOf("") }
    var selectedBg by remember { mutableStateOf("clay") }

    AlertDialog(
      onDismissRequest = { showAddNoteModal = false },
      containerColor = appColors.surfaceContainer,
      title = {
        Text(
          text = "Pin a Love Note",
          fontFamily = FontFamily.Serif,
          color = appColors.textPrimary
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            placeholder = { Text("Write something sweet for your partner...") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().testTag("note_text_input")
          )

          Text(
            text = "Select Note Theme",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.textMuted
          )

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val noteColors = listOf(
              "clay" to appColors.surfaceClay,
              "ochre" to appColors.surfaceOchre,
              "sage" to Color(0xFF26332C),
              "slate" to Color(0xFF2B333B)
            )
            noteColors.forEach { (type, color) ->
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(color)
                  .border(
                    width = if (selectedBg == type) 2.5.dp else 1.dp,
                    color = if (selectedBg == type) appColors.primary else appColors.outlineVariant,
                    shape = CircleShape
                  )
                  .clickable { selectedBg = type }
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (noteText.isNotBlank()) {
              onAddLoveNote(noteText, selectedBg)
              showAddNoteModal = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryDark),
          modifier = Modifier.testTag("pin_note_btn")
        ) {
          Text("Pin to Corkboard", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddNoteModal = false }) {
          Text("Cancel", color = appColors.textSecondary)
        }
      }
    )
  }

  // Answer Daily Prompt Modal
  if (showPromptModal) {
    var promptAnswer by remember { mutableStateOf("") }
    var saveAsType by remember { mutableStateOf("chapter") }

    AlertDialog(
      onDismissRequest = { showPromptModal = false },
      containerColor = appColors.surfaceContainer,
      title = {
        Text(
          text = "Reflect Together",
          fontFamily = FontFamily.Serif,
          color = appColors.textPrimary
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Daily Reflection Prompt",
              fontSize = 11.sp,
              color = appColors.textMuted
            )
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(appColors.primary.copy(alpha = 0.15f))
                .clickable {
                  val aiPrompts = listOf(
                    "What is one tiny detail about your partner that made you fall deeper in love?",
                    "If you could teleport to any quiet romantic spot in the world right now, where would you take your partner?",
                    "What song instantly reminds you of the first month you started dating?",
                    "Describe a moment this past week when your partner made you feel completely safe and cherished.",
                    "What is 1 silly inside joke that only the two of you understand?",
                    "Write 3 things you are deeply grateful for in your life together today."
                  )
                  promptAnswer = aiPrompts.random()
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Spark",
                tint = appColors.primary,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "AI Spark",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.primary
              )
            }
          }

          Text(
            text = "What was the sweetest surprise from our journey together?",
            fontSize = 13.sp,
            color = appColors.secondary,
            fontWeight = FontWeight.Medium
          )

          OutlinedTextField(
            value = promptAnswer,
            onValueChange = { promptAnswer = it },
            placeholder = { Text("Write your reflection...") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (saveAsType == "chapter") appColors.primary else appColors.surfaceContainerHigh)
                .clickable { saveAsType = "chapter" }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "📖 Journal Entry",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (saveAsType == "chapter") appColors.onPrimary else appColors.textSecondary
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (saveAsType == "note") appColors.primary else appColors.surfaceContainerHigh)
                .clickable { saveAsType = "note" }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "📌 Sticky Note",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (saveAsType == "note") appColors.onPrimary else appColors.textSecondary
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (promptAnswer.isNotBlank()) {
              if (saveAsType == "chapter") {
                onAddJournalEntry("Kyoto Reflection", "Reflection", promptAnswer, "")
              } else {
                onAddLoveNote(promptAnswer, "ochre")
              }
              showPromptModal = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryDark)
        ) {
          Text("Save Answer", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPromptModal = false }) {
          Text("Cancel", color = appColors.textSecondary)
        }
      }
    )
  }
}

/**
 * Audio Recording Studio Bottom Sheet / Modal Dialog
 */
@Composable
private fun AudioRecordLoveNoteModal(
  recorderStatus: RecorderStatus,
  recordingDurationSec: Int,
  previewDurationSec: Int,
  previewProgress: Float,
  liveAmplitudes: List<Float>,
  onStartRecording: () -> Unit,
  onStopRecording: () -> Unit,
  onCancelRecording: () -> Unit,
  onPlayPreview: () -> Unit,
  onPausePreview: () -> Unit,
  onReRecord: () -> Unit,
  onSaveVoiceNote: (caption: String, bgType: String, isPinned: Boolean, saveAsChapter: Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  val appColors = AppTheme.colors
  var captionText by remember { mutableStateOf("") }
  var selectedBg by remember { mutableStateOf("ochre") }
  var isPinned by remember { mutableStateOf(false) }
  var saveAsChapter by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = appColors.surfaceContainer,
    modifier = Modifier.testTag("audio_record_modal"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = appColors.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (recorderStatus == RecorderStatus.RECORDING) "Recording Voice Note..." else "Voice Love Note",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            color = appColors.textPrimary
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = appColors.textMuted)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Stage 1: IDLE / READY TO RECORD
        if (recorderStatus == RecorderStatus.IDLE) {
          Text(
            text = "Speak your heart directly to your partner. Short whispers, bedtime thoughts, or loving morning words.",
            fontSize = 12.sp,
            color = appColors.textSecondary,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Big Record Button
          Box(
            modifier = Modifier
              .size(90.dp)
              .clip(CircleShape)
              .background(appColors.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(appColors.primaryDark)
                .clickable { onStartRecording() }
                .testTag("start_recording_btn"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start Recording",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
              )
            }
          }

          Text(
            text = "Tap microphone to record",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = appColors.textMuted
          )
        }

        // Stage 2: ACTIVE RECORDING
        if (recorderStatus == RecorderStatus.RECORDING) {
          // Live Timer
          val minutes = recordingDurationSec / 60
          val seconds = recordingDurationSec % 60
          val timeFormatted = String.format("%02d:%02d", minutes, seconds)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFE53935))
            )
            Text(
              text = timeFormatted,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = appColors.textPrimary
            )
          }

          // Live Amplitude Waveform
          LiveWaveformBars(
            amplitudes = liveAmplitudes,
            primaryColor = appColors.primary,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Pulsing Stop Button
          Box(
            modifier = Modifier
              .size(90.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .background(Color(0xFFE53935).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFFE53935))
                .clickable { onStopRecording() }
                .testTag("stop_recording_btn"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Recording",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }
          }

          Text(
            text = "Tap to finish recording",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = appColors.textMuted
          )
        }

        // Stage 3: PREVIEW_READY or PLAYING_PREVIEW
        if (recorderStatus == RecorderStatus.PREVIEW_READY || recorderStatus == RecorderStatus.PLAYING_PREVIEW) {
          // Playback preview card
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  IconButton(
                    onClick = {
                      if (recorderStatus == RecorderStatus.PLAYING_PREVIEW) {
                        onPausePreview()
                      } else {
                        onPlayPreview()
                      }
                    },
                    modifier = Modifier
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(appColors.primary)
                      .testTag("play_preview_btn")
                  ) {
                    Icon(
                      imageVector = if (recorderStatus == RecorderStatus.PLAYING_PREVIEW) Icons.Default.Pause else Icons.Default.PlayArrow,
                      contentDescription = "Play/Pause Preview",
                      tint = Color.White,
                      modifier = Modifier.size(26.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Text(
                      text = "Recorded Whisper",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = appColors.textPrimary
                    )
                    val totalSec = previewDurationSec.coerceAtLeast(recordingDurationSec).coerceAtLeast(1)
                    val curSec = (previewProgress * totalSec).toInt()
                    Text(
                      text = String.format("%02d:%02d / %02d:%02d", curSec / 60, curSec % 60, totalSec / 60, totalSec % 60),
                      fontSize = 11.sp,
                      color = appColors.textMuted,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }

                IconButton(
                  onClick = onReRecord,
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Re-record",
                    tint = appColors.textMuted
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              LinearProgressIndicator(
                progress = { previewProgress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = appColors.primary,
                trackColor = appColors.surfaceContainerLow
              )
            }
          }

          // Optional caption / whisper text
          OutlinedTextField(
            value = captionText,
            onValueChange = { captionText = it },
            placeholder = { Text("Add a written whisper or caption (Optional)...") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().testTag("audio_caption_input"),
            shape = RoundedCornerShape(12.dp)
          )

          // Options: Theme and Destination
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Note Theme:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = appColors.textSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              val noteColors = listOf(
                "ochre" to appColors.surfaceOchre,
                "clay" to appColors.surfaceClay,
                "sage" to Color(0xFF26332C),
                "slate" to Color(0xFF2B333B)
              )
              noteColors.forEach { (type, color) ->
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                      width = if (selectedBg == type) 2.dp else 1.dp,
                      color = if (selectedBg == type) appColors.primary else appColors.outlineVariant,
                      shape = CircleShape
                    )
                    .clickable { selectedBg = type }
                )
              }
            }
          }

          // Pin toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = if (isPinned) appColors.secondary else appColors.textMuted,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Pin to Top Shelf",
                fontSize = 12.sp,
                color = appColors.textPrimary
              )
            }
            Switch(
              checked = isPinned,
              onCheckedChange = { isPinned = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = appColors.primary
              )
            )
          }

          // Destination: Sticky note vs Journal chapter
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (!saveAsChapter) appColors.primary else appColors.surfaceContainerHigh)
                .clickable { saveAsChapter = false }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "📌 Love Note",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (!saveAsChapter) appColors.onPrimary else appColors.textSecondary
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (saveAsChapter) appColors.primary else appColors.surfaceContainerHigh)
                .clickable { saveAsChapter = true }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "📖 Journal Memo",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (saveAsChapter) appColors.onPrimary else appColors.textSecondary
              )
            }
          }
        }
      }
    },
    confirmButton = {
      if (recorderStatus == RecorderStatus.PREVIEW_READY || recorderStatus == RecorderStatus.PLAYING_PREVIEW) {
        Button(
          onClick = {
            onSaveVoiceNote(captionText, selectedBg, isPinned, saveAsChapter)
          },
          colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryDark),
          modifier = Modifier.testTag("save_audio_note_btn")
        ) {
          Text("Save Note", color = Color.White)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = appColors.textSecondary)
      }
    }
  )
}

/**
 * Animated Live Microphone Waveform Bars
 */
@Composable
private fun LiveWaveformBars(
  amplitudes: List<Float>,
  primaryColor: Color,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  Canvas(modifier = modifier) {
    val barWidth = 4.dp.toPx()
    val gap = 3.dp.toPx()
    val totalBars = 24
    val height = size.height

    for (i in 0 until totalBars) {
      val amp = if (i < amplitudes.size) amplitudes[i] else 0.1f
      val barHeight = (height * amp).coerceIn(4.dp.toPx(), height)
      val x = i * (barWidth + gap)
      val y = (height - barHeight) / 2f

      drawRoundRect(
        color = primaryColor,
        topLeft = Offset(x, y),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )
    }
  }
}

/**
 * Interactive Sticky Note Card with Voice Memo playback
 */
@Composable
private fun StickyLoveNoteCard(
  note: LoveNote,
  isAudioPlaying: Boolean,
  playbackProgress: Float,
  playbackCurrentSec: Int,
  onPlayAudio: () -> Unit,
  onDelete: () -> Unit,
  onReact: (String) -> Unit
) {
  val appColors = AppTheme.colors
  val isAudio = note.isAudioNote || note.audioFilePath.isNotBlank()

  val containerBg = when (note.bgType) {
    "ochre" -> appColors.surfaceOchre
    "sage" -> Color(0xFF26332C)
    "slate" -> Color(0xFF2B333B)
    "container" -> appColors.surfaceContainerHighest
    else -> appColors.surfaceClay
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = containerBg),
    modifier = Modifier
      .fillMaxWidth()
      .rotate(note.rotation)
      .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp))
      .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
      .testTag("love_note_${note.id}")
  ) {
    Box(modifier = Modifier.padding(18.dp)) {
      // Push pin top corner
      Icon(
        imageVector = Icons.Default.PushPin,
        contentDescription = if (note.isPinned) "Pinned" else null,
        tint = if (note.isPinned) appColors.secondary else appColors.textMuted.copy(alpha = 0.4f),
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size(22.dp)
          .rotate(-15f)
      )

      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(appColors.primary.copy(alpha = 0.18f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = if (note.author.equals("Jamie", true)) "FROM JAMIE" else "FROM YOU",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = appColors.primary
              )
            }

            if (isAudio) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(appColors.secondary.copy(alpha = 0.2f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = appColors.secondary,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "VOICE NOTE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.secondary
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // If audio note, show audio player interface
        if (isAudio) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surface.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = onPlayAudio,
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(appColors.primaryDark)
                    .testTag("play_note_audio_${note.id}")
                ) {
                  Icon(
                    imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isAudioPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                  )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = if (isAudioPlaying) "Playing Whisper..." else "Tap to Play",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = appColors.textPrimary
                    )
                    val totalSec = note.audioDurationSec.coerceAtLeast(12)
                    val curSec = if (isAudioPlaying) playbackCurrentSec else 0
                    Text(
                      text = String.format("%02d:%02d / %02d:%02d", curSec / 60, curSec % 60, totalSec / 60, totalSec % 60),
                      fontSize = 10.sp,
                      color = appColors.textMuted,
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  LinearProgressIndicator(
                    progress = { if (isAudioPlaying) playbackProgress else 0f },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(5.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = appColors.primary,
                    trackColor = appColors.outlineVariant.copy(alpha = 0.2f)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
        }

        // Note written text / caption
        Text(
          text = note.text,
          fontFamily = FontFamily.Serif,
          fontStyle = FontStyle.Italic,
          fontSize = 17.sp,
          lineHeight = 24.sp,
          color = appColors.textPrimary,
          modifier = Modifier.padding(end = 24.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = note.timeAgo,
            fontSize = 11.sp,
            color = appColors.textMuted
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Quick Love reaction emoji
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(appColors.surface.copy(alpha = 0.5f))
                .clickable { onReact("💖") }
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(text = "💖", fontSize = 12.sp)
            }

            IconButton(
              onClick = onDelete,
              modifier = Modifier.size(28.dp).testTag("delete_note_${note.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Note",
                tint = appColors.textMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Journal Entry Card with optional attached voice note memo
 */
@Composable
private fun JournalEntryCard(
  entry: JournalEntry,
  isFavorite: Boolean,
  isAudioPlaying: Boolean,
  playbackProgress: Float,
  playbackCurrentSec: Int,
  onToggleFavorite: () -> Unit,
  onPlayAudio: () -> Unit
) {
  val appColors = AppTheme.colors
  val isAudioAttached = entry.isAudioAttached || entry.audioFilePath.isNotBlank()

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
      .testTag("journal_card_${entry.id}")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = entry.dateStr,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = appColors.secondary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "•",
            color = appColors.textMuted.copy(alpha = 0.5f)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = entry.category.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = appColors.primary
          )

          if (isAudioAttached) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = "Audio Attached",
              tint = appColors.secondary,
              modifier = Modifier.size(13.dp)
            )
          }
        }

        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) appColors.primary else appColors.textMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = entry.title,
        fontFamily = FontFamily.Serif,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = appColors.textPrimary
      )

      if (entry.imageUrl.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        AsyncImage(
          model = entry.imageUrl,
          contentDescription = entry.title,
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp)),
          contentScale = ContentScale.Crop
        )
      }

      // If audio attached, show voice player
      if (isAudioAttached) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onPlayAudio,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(appColors.primaryDark)
            ) {
              Icon(
                imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play voice memo",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Spoken Audio Memory",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = appColors.textPrimary
                )
                val totalSec = entry.audioDurationSec.coerceAtLeast(18)
                val curSec = if (isAudioPlaying) playbackCurrentSec else 0
                Text(
                  text = String.format("%02d:%02d / %02d:%02d", curSec / 60, curSec % 60, totalSec / 60, totalSec % 60),
                  fontSize = 10.sp,
                  color = appColors.textMuted,
                  fontFamily = FontFamily.Monospace
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { if (isAudioPlaying) playbackProgress else 0f },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp)),
                color = appColors.primary,
                trackColor = appColors.outlineVariant.copy(alpha = 0.2f)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = entry.body,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = appColors.textSecondary
      )
    }
  }
}

@Composable
private fun PinnedNoteMiniCard(
  note: LoveNote,
  isPlaying: Boolean,
  onPlayAudio: () -> Unit,
  onDelete: () -> Unit,
  onReact: () -> Unit
) {
  val appColors = AppTheme.colors
  val isAudio = note.isAudioNote || note.audioFilePath.isNotBlank()

  val containerBg = when (note.bgType) {
    "ochre" -> appColors.surfaceOchre
    "sage" -> Color(0xFF26332C)
    "slate" -> Color(0xFF2B333B)
    else -> appColors.surfaceClay
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerBg),
    modifier = Modifier
      .width(230.dp)
      .shadow(4.dp, RoundedCornerShape(16.dp))
      .border(1.dp, appColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = if (note.author.equals("Jamie", true)) "JAMIE" else "YOU",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = appColors.primary
          )
          if (isAudio) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = "Voice note",
              tint = appColors.secondary,
              modifier = Modifier.size(11.dp)
            )
          }
        }
        Icon(
          imageVector = Icons.Default.PushPin,
          contentDescription = "Pinned",
          tint = appColors.secondary,
          modifier = Modifier.size(14.dp)
        )
      }

      if (isAudio) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(appColors.surface.copy(alpha = 0.4f))
            .clickable { onPlayAudio() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = appColors.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isPlaying) "Playing audio..." else "Play voice note (${note.audioDurationSec.coerceAtLeast(10)}s)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = note.text,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        maxLines = 3,
        color = appColors.textPrimary
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = note.timeAgo,
        fontSize = 10.sp,
        color = appColors.textMuted
      )
    }
  }
}

@Composable
private fun EmptyStateBox(
  icon: ImageVector,
  title: String,
  subtitle: String
) {
  val appColors = AppTheme.colors
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 32.dp, vertical = 40.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(54.dp)
        .clip(CircleShape)
        .background(appColors.surfaceContainerHigh),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = appColors.primary,
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = title,
      fontFamily = FontFamily.Serif,
      fontSize = 18.sp,
      fontWeight = FontWeight.SemiBold,
      color = appColors.textPrimary
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = subtitle,
      fontSize = 12.sp,
      color = appColors.textMuted,
      lineHeight = 18.sp
    )
  }
}
