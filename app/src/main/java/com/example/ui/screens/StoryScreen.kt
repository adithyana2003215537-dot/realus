package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CalendarEvent
import com.example.data.model.CoupleSettings
import com.example.data.model.JournalEntry
import com.example.data.model.LoveNote
import com.example.data.model.StoryMilestone
import com.example.data.model.UserMood
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceClay
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerLow
import com.example.ui.theme.DarkSurfaceOchre
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.PrimarySienna
import com.example.ui.theme.PrimarySiennaDark
import com.example.ui.theme.SecondaryGold
import com.example.ui.theme.TextGoldMuted
import com.example.ui.theme.TextOffWhite
import com.example.ui.theme.AppTheme
import com.example.ui.theme.paperBackground

/**
 * Unified timeline item model representing aggregated stories, activities, notes, and mood syncs.
 */
sealed class TimelineStoryItem(
  val idKey: String,
  val sortTimestamp: Long,
  val dateDisplay: String,
  val categoryKey: String
) {
  data class MilestoneStory(val milestone: StoryMilestone) :
    TimelineStoryItem("milestone_${milestone.id}", milestone.createdAt, milestone.dateStr, "milestones")

  data class LoveNoteStory(val note: LoveNote) :
    TimelineStoryItem("note_${note.id}", note.createdAt, note.timeAgo, "notes")

  data class MoodUpdateStory(val mood: UserMood) :
    TimelineStoryItem("mood_${mood.id}", mood.timestamp, mood.dateLabel, "moods")

  data class JournalStory(val entry: JournalEntry) :
    TimelineStoryItem("journal_${entry.id}", entry.createdAt, entry.dateStr, "journal")

  data class CalendarStory(val event: CalendarEvent) :
    TimelineStoryItem("calendar_${event.id}", event.createdAt, event.dateStr, "events")
}

@Composable
fun StoryScreen(
  milestones: List<StoryMilestone>,
  loveNotes: List<LoveNote> = emptyList(),
  moodHistory: List<UserMood> = emptyList(),
  journalEntries: List<JournalEntry> = emptyList(),
  calendarEvents: List<CalendarEvent> = emptyList(),
  coupleSettings: CoupleSettings? = null,
  onAddMilestone: (title: String, date: String, description: String, imageUrl: String) -> Unit,
  onAddLoveNote: (text: String, bg: String) -> Unit = { _, _ -> },
  onAddJournalEntry: (title: String, category: String, body: String, imageUrl: String) -> Unit = { _, _, _, _ -> },
  onAddCalendarEvent: (title: String, date: String, day: Int, icon: String) -> Unit = { _, _, _, _ -> },
  onTriggerReaction: (emoji: String) -> Unit = {},
  onNavigateSubScreen: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val partner1Name = coupleSettings?.partner1Name ?: "Alex"
  val partner2Name = coupleSettings?.partner2Name ?: "Jamie"
  val daysTogether = coupleSettings?.daysTogether ?: 248

  var selectedFilter by remember { mutableStateOf("all") }
  var searchQuery by remember { mutableStateOf("") }
  var isSearchVisible by remember { mutableStateOf(false) }
  var isSortDescending by remember { mutableStateOf(true) }

  var showCreationDialog by remember { mutableStateOf(false) }
  var creationInitialTab by remember { mutableIntStateOf(0) }
  var selectedDetailItem by remember { mutableStateOf<TimelineStoryItem?>(null) }

  val likedItems = remember { mutableStateMapOf<String, Boolean>() }

  val allTimelineItems = remember(
    milestones,
    loveNotes,
    moodHistory,
    journalEntries,
    calendarEvents,
    isSortDescending
  ) {
    val items = mutableListOf<TimelineStoryItem>()
    milestones.forEach { items.add(TimelineStoryItem.MilestoneStory(it)) }
    loveNotes.forEach { items.add(TimelineStoryItem.LoveNoteStory(it)) }
    moodHistory.forEach { items.add(TimelineStoryItem.MoodUpdateStory(it)) }
    journalEntries.forEach { items.add(TimelineStoryItem.JournalStory(it)) }
    calendarEvents.forEach { items.add(TimelineStoryItem.CalendarStory(it)) }

    if (isSortDescending) {
      items.sortedByDescending { it.sortTimestamp }
    } else {
      items.sortedBy { it.sortTimestamp }
    }
  }

  val filteredItems = remember(allTimelineItems, selectedFilter, searchQuery) {
    allTimelineItems.filter { item ->
      val matchesCategory = (selectedFilter == "all" || item.categoryKey == selectedFilter)
      if (!matchesCategory) return@filter false

      if (searchQuery.isBlank()) return@filter true

      val q = searchQuery.trim().lowercase()
      when (item) {
        is TimelineStoryItem.MilestoneStory ->
          item.milestone.title.lowercase().contains(q) ||
            item.milestone.description.lowercase().contains(q) ||
            item.milestone.dateStr.lowercase().contains(q)

        is TimelineStoryItem.LoveNoteStory ->
          item.note.text.lowercase().contains(q) ||
            item.note.author.lowercase().contains(q)

        is TimelineStoryItem.MoodUpdateStory ->
          item.mood.moodLabel.lowercase().contains(q) ||
            item.mood.partnerMoodLabel.lowercase().contains(q) ||
            item.mood.note.lowercase().contains(q)

        is TimelineStoryItem.JournalStory ->
          item.entry.title.lowercase().contains(q) ||
            item.entry.body.lowercase().contains(q) ||
            item.entry.category.lowercase().contains(q)

        is TimelineStoryItem.CalendarStory ->
          item.event.title.lowercase().contains(q) ||
            item.event.dateStr.lowercase().contains(q)
      }
    }
  }

  val appColors = AppTheme.colors

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperBackground()
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {
      // 1. Hero Album & Scrapbook Header
      item {
        TimelineHeroHeader(
          partner1Name = partner1Name,
          partner2Name = partner2Name,
          daysTogether = daysTogether,
          totalMomentsCount = allTimelineItems.size,
          isSearchVisible = isSearchVisible,
          searchQuery = searchQuery,
          onToggleSearch = {
            isSearchVisible = !isSearchVisible
            if (!isSearchVisible) searchQuery = ""
          },
          onSearchQueryChange = { searchQuery = it },
          isSortDescending = isSortDescending,
          onToggleSort = { isSortDescending = !isSortDescending },
          onOpenCreation = { tab ->
            creationInitialTab = tab
            showCreationDialog = true
          }
        )
      }

      // 2. Filter Category Chips Row
      item {
        TimelineFilterRow(
          selectedFilter = selectedFilter,
          totalCount = allTimelineItems.size,
          milestonesCount = milestones.size,
          notesCount = loveNotes.size,
          moodsCount = moodHistory.size,
          journalCount = journalEntries.size,
          eventsCount = calendarEvents.size,
          onSelectFilter = { selectedFilter = it }
        )
      }

      // 3. Timeline Items / Empty State
      if (filteredItems.isEmpty()) {
        item {
          EmptyTimelineCard(
            filter = selectedFilter,
            searchQuery = searchQuery,
            onAddFirst = {
              creationInitialTab = when (selectedFilter) {
                "notes" -> 1
                "journal" -> 2
                "events" -> 3
                else -> 0
              }
              showCreationDialog = true
            },
            onClearFilter = {
              selectedFilter = "all"
              searchQuery = ""
            }
          )
        }
      } else {
        itemsIndexed(filteredItems, key = { _, item -> item.idKey }) { index, storyItem ->
          val isLast = index == filteredItems.size - 1
          val isLiked = likedItems[storyItem.idKey] == true

          TimelineStoryCardWrapper(
            storyItem = storyItem,
            isLast = isLast,
            isLiked = isLiked,
            partner1Name = partner1Name,
            partner2Name = partner2Name,
            onToggleLike = {
              val current = likedItems[storyItem.idKey] ?: false
              likedItems[storyItem.idKey] = !current
              if (!current) {
                onTriggerReaction("💖")
              }
            },
            onTriggerReaction = onTriggerReaction,
            onCardClick = { selectedDetailItem = storyItem }
          )
        }
      }
    }

    // Floating Universal "Add Story / Moment" FAB
    FloatingActionButton(
      onClick = {
        creationInitialTab = 0
        showCreationDialog = true
      },
      containerColor = appColors.primary,
      contentColor = appColors.onPrimary,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 80.dp)
        .testTag("add_story_fab")
    ) {
      Icon(imageVector = Icons.Default.Add, contentDescription = "Add Story")
    }

    // Detail Preview Modal
    selectedDetailItem?.let { item ->
      TimelineItemDetailDialog(
        item = item,
        partner1Name = partner1Name,
        partner2Name = partner2Name,
        onDismiss = { selectedDetailItem = null },
        onReaction = { emoji ->
          onTriggerReaction(emoji)
        }
      )
    }

    // Universal Multi-Category Creation Modal
    if (showCreationDialog) {
      UniversalTimelineAddDialog(
        initialTab = creationInitialTab,
        partner1Name = partner1Name,
        partner2Name = partner2Name,
        onDismiss = { showCreationDialog = false },
        onAddMilestone = { t, d, desc, img ->
          onAddMilestone(t, d, desc, img)
          onTriggerReaction("📸")
          showCreationDialog = false
        },
        onAddLoveNote = { text, bg ->
          onAddLoveNote(text, bg)
          onTriggerReaction("💌")
          showCreationDialog = false
        },
        onAddJournalEntry = { title, cat, body, img ->
          onAddJournalEntry(title, cat, body, img)
          onTriggerReaction("✨")
          showCreationDialog = false
        },
        onAddCalendarEvent = { title, date, day, icon ->
          onAddCalendarEvent(title, date, day, icon)
          onTriggerReaction("🗓️")
          showCreationDialog = false
        }
      )
    }
  }
}

/**
 * Top Scrapbook Hero Banner with Dual Avatars & Memory Metrics
 */
@Composable
private fun TimelineHeroHeader(
  partner1Name: String,
  partner2Name: String,
  daysTogether: Int,
  totalMomentsCount: Int,
  isSearchVisible: Boolean,
  searchQuery: String,
  onToggleSearch: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  isSortDescending: Boolean,
  onToggleSort: () -> Unit,
  onOpenCreation: (Int) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 18.dp, vertical = 12.dp)
  ) {
    // Top Bar Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Timeline Story",
            fontFamily = FontFamily.Serif,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextOffWhite
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(PrimarySienna.copy(alpha = 0.2f))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "$totalMomentsCount Memories",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = SecondaryGold
            )
          }
        }
        Text(
          text = "All our shared moments, love notes & emotional journeys",
          fontSize = 12.sp,
          color = TextGoldMuted,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
          onClick = onToggleSearch,
          modifier = Modifier.size(38.dp).testTag("toggle_search_btn")
        ) {
          Icon(
            imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
            contentDescription = "Search",
            tint = if (isSearchVisible) PrimarySienna else TextOffWhite
          )
        }

        IconButton(
          onClick = onToggleSort,
          modifier = Modifier.size(38.dp).testTag("sort_toggle_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Sort,
            contentDescription = "Sort Order",
            tint = if (isSortDescending) SecondaryGold else TextGoldMuted
          )
        }
      }
    }

    // Expandable Search Bar
    AnimatedVisibility(visible = isSearchVisible) {
      Column(modifier = Modifier.padding(top = 10.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = {
            Text("Search memories, notes, activities...", fontSize = 13.sp, color = TextGoldMuted)
          },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PrimarySienna)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextGoldMuted)
              }
            }
          },
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceContainer,
            unfocusedContainerColor = DarkSurfaceContainer,
            focusedBorderColor = PrimarySienna,
            unfocusedBorderColor = OutlineVariant.copy(alpha = 0.35f),
            focusedTextColor = TextOffWhite,
            unfocusedTextColor = TextOffWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_search_input")
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive Hero Card
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainerHigh),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, OutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Dual Connected Avatars
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                  model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCr3fVoQ3DuG0CGaMULkrVwYXnqw6pJ5HUcX2EdI7iqeF9Fn6_ajHYQ2ZLv1i3HhrkI4H-96sP18wDGIU0oxFPDEZD357n0OHCOGu6ggMr_vRsiyXPFGf4_OHLfVRFE2xvDZaE23woLUmY2DHXnpkYJlszIE0y7Y1Ak1zN7Axp2tgmCYSpCXvyqZGjqhEWe5WQHEbHRgFcZimZEwwnU3K5Dl5lzFHvJVUDqA2jo8HC2X2A1UXhVNg",
                  contentDescription = partner1Name,
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(2.dp, PrimarySienna, CircleShape),
                  contentScale = ContentScale.Crop
                )
                AsyncImage(
                  model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q",
                  contentDescription = partner2Name,
                  modifier = Modifier
                    .offset(x = (-12).dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(2.dp, SecondaryGold, CircleShape),
                  contentScale = ContentScale.Crop
                )
              }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
              Text(
                text = "$partner1Name & $partner2Name",
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextOffWhite
              )
              Text(
                text = "Together for $daysTogether Days",
                fontSize = 11.sp,
                color = SecondaryGold
              )
            }
          }

          // Live Resonance Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(PrimarySiennaDark.copy(alpha = 0.25f))
              .border(1.dp, PrimarySienna.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = if (isSortDescending) "⏳ Newest First" else "📅 Chronological",
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = PrimarySienna
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.25f))
        Spacer(modifier = Modifier.height(12.dp))

        // Quick Multi-Add Shortcut Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          QuickAddPill(
            icon = Icons.Default.PhotoCamera,
            label = "+ Moment",
            color = PrimarySienna,
            onClick = { onOpenCreation(0) }
          )
          QuickAddPill(
            icon = Icons.Default.PushPin,
            label = "+ Note",
            color = SecondaryGold,
            onClick = { onOpenCreation(1) }
          )
          QuickAddPill(
            icon = Icons.Default.AutoStories,
            label = "+ Journal",
            color = Color(0xFF70D2FA),
            onClick = { onOpenCreation(2) }
          )
          QuickAddPill(
            icon = Icons.Default.CalendarMonth,
            label = "+ Date",
            color = Color(0xFF00E676),
            onClick = { onOpenCreation(3) }
          )
        }
      }
    }
  }
}

@Composable
private fun QuickAddPill(
  icon: ImageVector,
  label: String,
  color: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(DarkSurfaceContainerLow)
      .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextOffWhite
      )
    }
  }
}

/**
 * Filter Chips Row
 */
@Composable
private fun TimelineFilterRow(
  selectedFilter: String,
  totalCount: Int,
  milestonesCount: Int,
  notesCount: Int,
  moodsCount: Int,
  journalCount: Int,
  eventsCount: Int,
  onSelectFilter: (String) -> Unit
) {
  val filters = listOf(
    Triple("all", "All Story", totalCount),
    Triple("milestones", "📸 Moments", milestonesCount),
    Triple("notes", "💌 Notes", notesCount),
    Triple("moods", "✨ Moods", moodsCount),
    Triple("journal", "📖 Journal", journalCount),
    Triple("events", "🗓️ Dates", eventsCount)
  )

  LazyRow(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    contentPadding = PaddingValues(horizontal = 18.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(filters) { (key, label, count) ->
      val isSelected = selectedFilter == key

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(if (isSelected) PrimarySienna else DarkSurfaceContainer)
          .border(
            width = 1.dp,
            color = if (isSelected) PrimarySienna else OutlineVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(14.dp)
          )
          .clickable { onSelectFilter(key) }
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .testTag("timeline_filter_$key")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else TextOffWhite
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(if (isSelected) Color.Black.copy(alpha = 0.2f) else DarkSurfaceContainerHigh)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "$count",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) Color.Black else SecondaryGold
            )
          }
        }
      }
    }
  }
}

/**
 * Vertical Timeline Card with Connecting Track & Node Indicator
 */
@Composable
private fun TimelineStoryCardWrapper(
  storyItem: TimelineStoryItem,
  isLast: Boolean,
  isLiked: Boolean,
  partner1Name: String,
  partner2Name: String,
  onToggleLike: () -> Unit,
  onTriggerReaction: (String) -> Unit,
  onCardClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 18.dp, vertical = 4.dp)
  ) {
    // Left: Continuous Timeline Line & Glowing Node Icon
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.width(36.dp)
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      val (nodeIcon, nodeColor) = when (storyItem) {
        is TimelineStoryItem.MilestoneStory -> Pair(Icons.Default.PhotoCamera, PrimarySienna)
        is TimelineStoryItem.LoveNoteStory -> Pair(Icons.Default.PushPin, SecondaryGold)
        is TimelineStoryItem.MoodUpdateStory -> Pair(Icons.Default.AutoAwesome, Color(0xFFFF80AB))
        is TimelineStoryItem.JournalStory -> Pair(Icons.Default.AutoStories, Color(0xFF70D2FA))
        is TimelineStoryItem.CalendarStory -> Pair(Icons.Default.CalendarMonth, Color(0xFF00E676))
      }

      Box(
        modifier = Modifier
          .size(26.dp)
          .clip(CircleShape)
          .background(DarkSurfaceContainerHigh)
          .border(1.5.dp, nodeColor, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = nodeIcon,
          contentDescription = null,
          tint = nodeColor,
          modifier = Modifier.size(13.dp)
        )
      }

      if (!isLast) {
        Box(
          modifier = Modifier
            .width(2.dp)
            .height(280.dp)
            .background(
              Brush.verticalGradient(
                listOf(
                  nodeColor.copy(alpha = 0.6f),
                  PrimarySiennaDark.copy(alpha = 0.25f)
                )
              )
            )
        )
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Right: Specific Card View
    Box(
      modifier = Modifier
        .weight(1f)
        .padding(bottom = 12.dp)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null
        ) { onCardClick() }
    ) {
      when (storyItem) {
        is TimelineStoryItem.MilestoneStory -> {
          MilestoneCardView(
            milestone = storyItem.milestone,
            isLiked = isLiked,
            onToggleLike = onToggleLike
          )
        }
        is TimelineStoryItem.LoveNoteStory -> {
          LoveNoteCardView(
            note = storyItem.note,
            partner1Name = partner1Name,
            partner2Name = partner2Name,
            isLiked = isLiked,
            onToggleLike = onToggleLike
          )
        }
        is TimelineStoryItem.MoodUpdateStory -> {
          MoodUpdateCardView(
            mood = storyItem.mood,
            partner1Name = partner1Name,
            partner2Name = partner2Name,
            isLiked = isLiked,
            onToggleLike = onToggleLike,
            onSendReaction = onTriggerReaction
          )
        }
        is TimelineStoryItem.JournalStory -> {
          JournalActivityCardView(
            entry = storyItem.entry,
            isLiked = isLiked,
            onToggleLike = onToggleLike
          )
        }
        is TimelineStoryItem.CalendarStory -> {
          CalendarEventCardView(
            event = storyItem.event,
            isLiked = isLiked,
            onToggleLike = onToggleLike
          )
        }
      }
    }
  }
}

/**
 * 1. Milestone / Photo Story Card (Polaroid aesthetic)
 */
@Composable
private fun MilestoneCardView(
  milestone: StoryMilestone,
  isLiked: Boolean,
  onToggleLike: () -> Unit
) {
  val scale by animateFloatAsState(
    targetValue = if (isLiked) 1.02f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "polaroid_like_scale"
  )

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainerLow),
    modifier = Modifier
      .fillMaxWidth()
      .scale(scale)
      .rotate(milestone.rotation.coerceIn(-2.5f, 2.5f))
      .border(1.dp, OutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
      .testTag("milestone_card_${milestone.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(PrimarySienna.copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "📸 Milestone",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = PrimarySienna
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = milestone.dateStr,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryGold
          )
        }

        IconButton(
          onClick = onToggleLike,
          modifier = Modifier.size(28.dp).testTag("like_milestone_${milestone.id}")
        ) {
          Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLiked) PrimarySienna else TextGoldMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      if (milestone.imageUrl.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        AsyncImage(
          model = milestone.imageUrl,
          contentDescription = milestone.title,
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp)),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = milestone.title,
        fontFamily = FontFamily.Serif,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = TextOffWhite
      )

      if (milestone.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = milestone.description,
          fontSize = 12.5.sp,
          lineHeight = 18.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

/**
 * 2. Love Note Card (Parchment & Handwritten aesthetic)
 */
@Composable
private fun LoveNoteCardView(
  note: LoveNote,
  partner1Name: String,
  partner2Name: String,
  isLiked: Boolean,
  onToggleLike: () -> Unit
) {
  val containerBg = when (note.bgType.lowercase()) {
    "ochre" -> DarkSurfaceOchre
    "clay" -> DarkSurfaceClay
    "sage" -> Color(0xFF232D26)
    "slate" -> Color(0xFF222830)
    else -> DarkSurfaceContainer
  }

  val authorDisplay = if (note.author.equals("YOU", ignoreCase = true)) partner1Name else partner2Name

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = containerBg),
    modifier = Modifier
      .fillMaxWidth()
      .rotate(note.rotation.coerceIn(-2f, 2f))
      .border(1.dp, OutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
      .testTag("lovenote_card_${note.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.PushPin,
            contentDescription = null,
            tint = SecondaryGold,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "From $authorDisplay",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryGold
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${note.timeAgo}",
            fontSize = 10.sp,
            color = TextGoldMuted
          )
        }

        IconButton(
          onClick = onToggleLike,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLiked) PrimarySienna else TextGoldMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      if (note.isAudioNote || note.audioFilePath.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimarySienna.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = "Voice Whisper",
            tint = PrimarySienna,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Voice Whisper (${note.audioDurationSec.coerceAtLeast(8)}s)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimarySienna
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
          imageVector = Icons.Default.FormatQuote,
          contentDescription = null,
          tint = PrimarySienna.copy(alpha = 0.6f),
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = note.text,
          fontFamily = FontFamily.Serif,
          fontSize = 14.sp,
          lineHeight = 20.sp,
          fontStyle = FontStyle.Italic,
          color = TextOffWhite,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

/**
 * 3. Mood Update & Emotional Harmony Card
 */
@Composable
private fun MoodUpdateCardView(
  mood: UserMood,
  partner1Name: String,
  partner2Name: String,
  isLiked: Boolean,
  onToggleLike: () -> Unit,
  onSendReaction: (String) -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, OutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
      .testTag("mood_card_${mood.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFFF80AB).copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "✨ Mood Sync",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFF80AB)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = mood.dateLabel.ifBlank { "Recent" },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryGold
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PrimarySienna.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "${mood.synergyScore}% Synergy",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = PrimarySienna
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Dual Partner Mood Display
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(DarkSurfaceContainerLow)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = mood.moodIcon, fontSize = 22.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(text = partner1Name, fontSize = 10.sp, color = TextGoldMuted)
            Text(text = mood.moodLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOffWhite)
          }
        }

        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = SecondaryGold,
          modifier = Modifier.size(16.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(horizontalAlignment = Alignment.End) {
            Text(text = partner2Name, fontSize = 10.sp, color = TextGoldMuted)
            Text(text = mood.partnerMoodLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOffWhite)
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = mood.partnerMoodIcon, fontSize = 22.sp)
        }
      }

      if (mood.note.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "💬 \"${mood.note}\"",
          fontSize = 12.sp,
          fontStyle = FontStyle.Italic,
          color = TextGoldMuted
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Micro Reaction Triggers
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("💖", "🫂", "✨").forEach { emoji ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceContainerHigh)
                .clickable { onSendReaction(emoji) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(text = emoji, fontSize = 12.sp)
            }
          }
        }

        IconButton(
          onClick = onToggleLike,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLiked) PrimarySienna else TextGoldMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

/**
 * 4. Shared Activity & Journal Story Card
 */
@Composable
private fun JournalActivityCardView(
  entry: JournalEntry,
  isLiked: Boolean,
  onToggleLike: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, OutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
      .testTag("journal_card_${entry.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF70D2FA).copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "📖 ${entry.category}",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF70D2FA)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = entry.dateStr,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryGold
          )
        }

        IconButton(
          onClick = onToggleLike,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLiked) PrimarySienna else TextGoldMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      if (entry.imageUrl.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        AsyncImage(
          model = entry.imageUrl,
          contentDescription = entry.title,
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp)),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = entry.title,
        fontFamily = FontFamily.Serif,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextOffWhite
      )

      if (entry.isAudioAttached || entry.audioFilePath.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PrimarySienna.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = PrimarySienna,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Voice Memo (${entry.audioDurationSec.coerceAtLeast(10)}s)",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimarySienna
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = entry.body,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * 5. Calendar Milestone / Shared Date Card
 */
@Composable
private fun CalendarEventCardView(
  event: CalendarEvent,
  isLiked: Boolean,
  onToggleLike: () -> Unit
) {
  val iconVector = when (event.iconType.lowercase()) {
    "flight" -> Icons.Default.Flight
    "star" -> Icons.Default.Star
    "dining", "food" -> Icons.Default.Restaurant
    else -> Icons.Default.Favorite
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, OutlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
      .testTag("event_card_${event.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceClay),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = PrimarySienna,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = event.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextOffWhite
          )
          Text(
            text = "${event.dateStr} • ${event.daysRemainingText}",
            fontSize = 11.sp,
            color = SecondaryGold
          )
        }
      }

      IconButton(
        onClick = onToggleLike,
        modifier = Modifier.size(28.dp)
      ) {
        Icon(
          imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = "Like",
          tint = if (isLiked) PrimarySienna else TextGoldMuted,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

/**
 * Empty Timeline State
 */
@Composable
private fun EmptyTimelineCard(
  filter: String,
  searchQuery: String,
  onAddFirst: () -> Unit,
  onClearFilter: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainerLow),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 18.dp, vertical = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = "✨", fontSize = 36.sp)
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = if (searchQuery.isNotEmpty()) "No Matching Memories" else "No Moments Yet",
        fontFamily = FontFamily.Serif,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextOffWhite
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = if (searchQuery.isNotEmpty()) {
          "No timeline moments found for \"$searchQuery\". Try clearing your search query."
        } else {
          "Start filling your shared love timeline with polaroid photos, sweet notes, and daily adventures!"
        },
        fontSize = 12.sp,
        color = TextGoldMuted,
        textAlign = TextAlign.Center,
        lineHeight = 17.sp
      )
      Spacer(modifier = Modifier.height(16.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (searchQuery.isNotEmpty() || filter != "all") {
          OutlinedButton(
            onClick = onClearFilter,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimarySienna)
          ) {
            Text("Show All Stories", fontSize = 12.sp)
          }
        }
        Button(
          onClick = onAddFirst,
          colors = ButtonDefaults.buttonColors(containerColor = PrimarySiennaDark)
        ) {
          Text("+ Add First Story", color = Color.White, fontSize = 12.sp)
        }
      }
    }
  }
}

/**
 * Full Item Inspection Modal Dialog
 */
@Composable
private fun TimelineItemDetailDialog(
  item: TimelineStoryItem,
  partner1Name: String,
  partner2Name: String,
  onDismiss: () -> Unit,
  onReaction: (String) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceContainerHigh,
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = PrimarySiennaDark)
      ) {
        Text("Close", color = Color.White)
      }
    },
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = when (item) {
            is TimelineStoryItem.MilestoneStory -> item.milestone.title
            is TimelineStoryItem.LoveNoteStory -> "Love Note"
            is TimelineStoryItem.MoodUpdateStory -> "Emotional Connection Log"
            is TimelineStoryItem.JournalStory -> item.entry.title
            is TimelineStoryItem.CalendarStory -> item.event.title
          },
          fontFamily = FontFamily.Serif,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextOffWhite
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        when (item) {
          is TimelineStoryItem.MilestoneStory -> {
            if (item.milestone.imageUrl.isNotBlank()) {
              AsyncImage(
                model = item.milestone.imageUrl,
                contentDescription = item.milestone.title,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(200.dp)
                  .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
              )
            }
            Text(
              text = "Date: ${item.milestone.dateStr}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = SecondaryGold
            )
            Text(
              text = item.milestone.description,
              fontSize = 13.sp,
              lineHeight = 19.sp,
              color = TextOffWhite
            )
          }

          is TimelineStoryItem.LoveNoteStory -> {
            Text(
              text = "Written by ${if (item.note.author.equals("YOU", true)) partner1Name else partner2Name} • ${item.note.timeAgo}",
              fontSize = 12.sp,
              color = SecondaryGold
            )
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceContainerLow)
                .padding(14.dp)
            ) {
              Text(
                text = "“${item.note.text}”",
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                color = TextOffWhite,
                lineHeight = 22.sp
              )
            }
          }

          is TimelineStoryItem.MoodUpdateStory -> {
            Text(
              text = "Harmony Level: ${item.mood.synergyScore}%",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = PrimarySienna
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.mood.moodIcon, fontSize = 32.sp)
                Text(text = "$partner1Name: ${item.mood.moodLabel}", fontSize = 12.sp, color = TextOffWhite)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.mood.partnerMoodIcon, fontSize = 32.sp)
                Text(text = "$partner2Name: ${item.mood.partnerMoodLabel}", fontSize = 12.sp, color = TextOffWhite)
              }
            }
            if (item.mood.note.isNotBlank()) {
              Text(text = "Note: ${item.mood.note}", fontSize = 12.sp, color = TextGoldMuted)
            }
          }

          is TimelineStoryItem.JournalStory -> {
            if (item.entry.imageUrl.isNotBlank()) {
              AsyncImage(
                model = item.entry.imageUrl,
                contentDescription = item.entry.title,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
              )
            }
            Text(
              text = "Category: ${item.entry.category} • ${item.entry.dateStr}",
              fontSize = 12.sp,
              color = SecondaryGold
            )
            Text(
              text = item.entry.body,
              fontSize = 13.sp,
              lineHeight = 19.sp,
              color = TextOffWhite
            )
          }

          is TimelineStoryItem.CalendarStory -> {
            Text(
              text = "Scheduled for ${item.event.dateStr} (${item.event.daysRemainingText})",
              fontSize = 13.sp,
              color = SecondaryGold
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Reaction Row inside Dialog
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          listOf("💖", "🥰", "✨", "🫂", "🍷").forEach { emoji ->
            Box(
              modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(CircleShape)
                .background(DarkSurfaceContainerLow)
                .clickable {
                  onReaction(emoji)
                  onDismiss()
                }
                .padding(8.dp)
            ) {
              Text(text = emoji, fontSize = 18.sp)
            }
          }
        }
      }
    }
  )
}

/**
 * Universal Multi-Category Creation Sheet / Dialog
 */
@Composable
private fun UniversalTimelineAddDialog(
  initialTab: Int,
  partner1Name: String,
  partner2Name: String,
  onDismiss: () -> Unit,
  onAddMilestone: (title: String, date: String, description: String, imageUrl: String) -> Unit,
  onAddLoveNote: (text: String, bg: String) -> Unit,
  onAddJournalEntry: (title: String, category: String, body: String, imageUrl: String) -> Unit,
  onAddCalendarEvent: (title: String, date: String, day: Int, icon: String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(initialTab) }
  val tabTitles = listOf("📸 Moment", "💌 Note", "📖 Journal", "🗓️ Date")

  // State for Milestone
  var milestoneTitle by remember { mutableStateOf("") }
  var milestoneDate by remember { mutableStateOf("Today") }
  var milestoneDesc by remember { mutableStateOf("") }
  var milestoneImg by remember { mutableStateOf("") }

  // State for Love Note
  var noteText by remember { mutableStateOf("") }
  var noteBgType by remember { mutableStateOf("clay") }

  // State for Journal
  var journalTitle by remember { mutableStateOf("") }
  var journalCategory by remember { mutableStateOf("Our Moments") }
  var journalBody by remember { mutableStateOf("") }
  var journalImg by remember { mutableStateOf("") }

  // State for Event
  var eventTitle by remember { mutableStateOf("") }
  var eventDate by remember { mutableStateOf("Oct 25") }
  var eventDay by remember { mutableIntStateOf(25) }
  var eventIcon by remember { mutableStateOf("heart") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceContainer,
    title = {
      Column {
        Text(
          text = "Add to Timeline Story",
          fontFamily = FontFamily.Serif,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextOffWhite
        )
        Text(
          text = "Record a memory, letter, or activity into your story",
          fontSize = 11.sp,
          color = TextGoldMuted
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Tab Row
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = DarkSurfaceContainerLow,
          contentColor = PrimarySienna,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = PrimarySienna,
              height = 2.5.dp
            )
          }
        ) {
          tabTitles.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  text = title,
                  fontSize = 11.sp,
                  fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                  color = if (selectedTab == index) PrimarySienna else TextGoldMuted
                )
              },
              modifier = Modifier.testTag("add_dialog_tab_$index")
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (selectedTab) {
          0 -> {
            // Milestone Form
            OutlinedTextField(
              value = milestoneTitle,
              onValueChange = { milestoneTitle = it },
              label = { Text("Milestone Title (e.g. Kyoto Sunset)") },
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = milestoneDate,
              onValueChange = { milestoneDate = it },
              label = { Text("Date (e.g. Oct 14, 2024)") },
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = milestoneDesc,
              onValueChange = { milestoneDesc = it },
              label = { Text("The Story / Memory") },
              maxLines = 3,
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = milestoneImg,
              onValueChange = { milestoneImg = it },
              label = { Text("Photo URL (Optional)") },
              modifier = Modifier.fillMaxWidth()
            )
          }

          1 -> {
            // Love Note Form
            OutlinedTextField(
              value = noteText,
              onValueChange = { noteText = it },
              label = { Text("Write a sweet note for $partner2Name...") },
              placeholder = { Text("I can't stop thinking about our morning walk ☕") },
              maxLines = 4,
              modifier = Modifier.fillMaxWidth()
            )

            Text(text = "Card Style / Paper Tint:", fontSize = 11.sp, color = TextGoldMuted)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf("clay" to "Clay", "ochre" to "Ochre", "sage" to "Sage", "slate" to "Slate").forEach { (type, name) ->
                val isSelected = noteBgType == type
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PrimarySienna else DarkSurfaceContainerLow)
                    .clickable { noteBgType = type }
                    .padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.Black else TextOffWhite
                  )
                }
              }
            }
          }

          2 -> {
            // Journal Form
            OutlinedTextField(
              value = journalTitle,
              onValueChange = { journalTitle = it },
              label = { Text("Activity Title (e.g. Making Homemade Pasta)") },
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = journalCategory,
              onValueChange = { journalCategory = it },
              label = { Text("Category / Location (e.g. Sunday Cooking)") },
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = journalBody,
              onValueChange = { journalBody = it },
              label = { Text("Journal Reflection") },
              maxLines = 3,
              modifier = Modifier.fillMaxWidth()
            )
          }

          3 -> {
            // Calendar Event Form
            OutlinedTextField(
              value = eventTitle,
              onValueChange = { eventTitle = it },
              label = { Text("Event Name (e.g. Anniversary Dinner)") },
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = eventDate,
              onValueChange = { eventDate = it },
              label = { Text("Date (e.g. Nov 12)") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          when (selectedTab) {
            0 -> {
              if (milestoneTitle.isNotBlank()) {
                onAddMilestone(milestoneTitle, milestoneDate, milestoneDesc, milestoneImg)
              }
            }
            1 -> {
              if (noteText.isNotBlank()) {
                onAddLoveNote(noteText, noteBgType)
              }
            }
            2 -> {
              if (journalTitle.isNotBlank()) {
                onAddJournalEntry(journalTitle, journalCategory, journalBody, journalImg)
              }
            }
            3 -> {
              if (eventTitle.isNotBlank()) {
                onAddCalendarEvent(eventTitle, eventDate, eventDay, eventIcon)
              }
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = PrimarySiennaDark)
      ) {
        Text("Save to Timeline", color = Color.White)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  )
}
