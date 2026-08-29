package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CalendarEvent
import com.example.data.model.ChatMessage
import com.example.data.model.CoupleSettings
import com.example.data.model.JournalEntry
import com.example.data.model.LoveNote
import com.example.data.model.StoryMilestone
import com.example.data.model.UserMood
import kotlinx.coroutines.flow.Flow

@Dao
interface RealUsDao {

  // Chat
  @Query("SELECT * FROM chat_messages ORDER BY createdAt ASC")
  fun getChatMessages(): Flow<List<ChatMessage>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChatMessage(message: ChatMessage)

  // Love Notes
  @Query("SELECT * FROM love_notes ORDER BY isPinned DESC, createdAt DESC")
  fun getLoveNotes(): Flow<List<LoveNote>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLoveNote(note: LoveNote)

  @Query("DELETE FROM love_notes WHERE id = :id")
  suspend fun deleteLoveNote(id: Long)

  // Story Milestones
  @Query("SELECT * FROM story_milestones ORDER BY createdAt ASC")
  fun getStoryMilestones(): Flow<List<StoryMilestone>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStoryMilestone(milestone: StoryMilestone)

  // Journal Entries
  @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
  fun getJournalEntries(): Flow<List<JournalEntry>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJournalEntry(entry: JournalEntry)

  // Calendar Events
  @Query("SELECT * FROM calendar_events ORDER BY dayOfMonth ASC")
  fun getCalendarEvents(): Flow<List<CalendarEvent>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCalendarEvent(event: CalendarEvent)

  // Moods
  @Query("SELECT * FROM user_moods ORDER BY timestamp DESC LIMIT 1")
  fun getLatestMood(): Flow<UserMood?>

  @Query("SELECT * FROM user_moods ORDER BY timestamp DESC")
  fun getAllMoods(): Flow<List<UserMood>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMood(mood: UserMood)

  // Couple Settings
  @Query("SELECT * FROM couple_settings WHERE id = 1")
  fun getCoupleSettings(): Flow<CoupleSettings?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCoupleSettings(settings: CoupleSettings)

  @Update
  suspend fun updateCoupleSettings(settings: CoupleSettings)

  @Query("DELETE FROM chat_messages")
  suspend fun clearChatMessages()

  @Query("DELETE FROM love_notes")
  suspend fun clearLoveNotes()

  @Query("DELETE FROM story_milestones")
  suspend fun clearStoryMilestones()

  @Query("DELETE FROM journal_entries")
  suspend fun clearJournalEntries()

  @Query("DELETE FROM calendar_events")
  suspend fun clearCalendarEvents()

  @Query("DELETE FROM user_moods")
  suspend fun clearMoods()

  @Query("DELETE FROM couple_settings")
  suspend fun clearCoupleSettings()
}
