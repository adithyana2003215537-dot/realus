package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sender: String, // "partner" or "you"
  val text: String,
  val timestamp: String,
  val isAudio: Boolean = false,
  val audioDuration: String = "",
  val isImage: Boolean = false,
  val imageUrl: String = "",
  val imageCaption: String = "",
  val isRead: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "love_notes")
data class LoveNote(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val author: String = "YOU",
  val text: String,
  val timeAgo: String,
  val bgType: String = "clay", // "clay", "ochre", "sage", "slate", "container"
  val isPinned: Boolean = false,
  val rotation: Float = 0f,
  val isAudioNote: Boolean = false,
  val audioFilePath: String = "",
  val audioDurationSec: Int = 0,
  val audioAmplitudes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "story_milestones")
data class StoryMilestone(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val dateStr: String,
  val title: String,
  val description: String,
  val imageUrl: String,
  val rotation: Float = 0f,
  val isHighlighted: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val dateStr: String,
  val category: String, // "Memory", "Reflection", "Trip", "Daily Question"
  val title: String,
  val body: String,
  val imageUrl: String = "",
  val weatherIcon: String = "sunny",
  val isFavorite: Boolean = false,
  val isAudioAttached: Boolean = false,
  val audioFilePath: String = "",
  val audioDurationSec: Int = 0,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val dateStr: String,
  val daysRemainingText: String,
  val dayOfMonth: Int,
  val monthYear: String,
  val iconType: String = "heart", // "heart", "flight", "star"
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_moods")
data class UserMood(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val moodKey: String, // "loved", "happy", "romantic", "tired", "need_you", "flirty", "sad", "angry", "missing", "cozy", "grateful"
  val moodLabel: String,
  val moodIcon: String,
  val moodScore: Float = 8.5f, // 1.0 to 10.0 scale for trendline
  val partnerMoodLabel: String = "Loved",
  val partnerMoodIcon: String = "💖",
  val partnerMoodScore: Float = 9.0f,
  val note: String = "",
  val synergyScore: Int = 92, // % synergy
  val dateLabel: String = "Today",
  val partnerName: String = "Partner",
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "couple_settings")
data class CoupleSettings(
  @PrimaryKey val id: Int = 1,
  val partner1Name: String = "You",
  val partner2Name: String = "Partner",
  val anniversaryDate: String = "",
  val daysTogether: Int = 0,
  val partnerMood: String = "Loved",
  val coupleCode: String = "",
  val userAvatarUrl: String = "",
  val partnerAvatarUrl: String = "",
  val themeName: String = "Night",
  val syncWithPartner: Boolean = true,
  val biometricLock: Boolean = false,
  val privateMode: Boolean = false,
  val intimacyNotifications: Boolean = true,
  val calendarNotifications: Boolean = true,
  val currentSongTitle: String = "Sunset Melodies",
  val currentArtist: String = "Midnight Warmth Ensemble",
  val isPlayingMusic: Boolean = false,
  val moodSyncLevel: Float = 0.65f // 0 to 1 (Chill to Energy)
)

data class SharedMusic(
  val videoId: String = "",
  val videoUrl: String = "",
  val title: String = "",
  val artist: String = "",
  val thumbnailUrl: String = "",
  val isPlaying: Boolean = false,
  val positionMs: Long = 0L,
  val moodSync: Float = 0.5f,
  val addedBy: String = "",
  val updatedAt: Long = System.currentTimeMillis()
)

