package com.example.data.repository

import com.example.data.dao.RealUsDao
import com.example.data.model.CalendarEvent
import com.example.data.model.ChatMessage
import com.example.data.model.CoupleSettings
import com.example.data.model.JournalEntry
import com.example.data.model.LoveNote
import com.example.data.model.StoryMilestone
import com.example.data.model.UserMood
import kotlinx.coroutines.flow.Flow

class RealUsRepository(private val dao: RealUsDao) {
  val chatMessages: Flow<List<ChatMessage>> = dao.getChatMessages()
  val loveNotes: Flow<List<LoveNote>> = dao.getLoveNotes()
  val storyMilestones: Flow<List<StoryMilestone>> = dao.getStoryMilestones()
  val journalEntries: Flow<List<JournalEntry>> = dao.getJournalEntries()
  val calendarEvents: Flow<List<CalendarEvent>> = dao.getCalendarEvents()
  val latestMood: Flow<UserMood?> = dao.getLatestMood()
  val allMoods: Flow<List<UserMood>> = dao.getAllMoods()
  val coupleSettings: Flow<CoupleSettings?> = dao.getCoupleSettings()

  suspend fun sendChatMessage(message: ChatMessage) = dao.insertChatMessage(message)
  suspend fun addLoveNote(note: LoveNote) = dao.insertLoveNote(note)
  suspend fun deleteLoveNote(id: Long) = dao.deleteLoveNote(id)
  suspend fun addMilestone(milestone: StoryMilestone) = dao.insertStoryMilestone(milestone)
  suspend fun addJournalEntry(entry: JournalEntry) = dao.insertJournalEntry(entry)
  suspend fun addCalendarEvent(event: CalendarEvent) = dao.insertCalendarEvent(event)
  suspend fun setMood(mood: UserMood) {
    dao.insertMood(mood)
    // also update partnerMood in coupleSettings
    val current = dao.getCoupleSettings()
    // update settings partner mood
  }
  suspend fun updateSettings(settings: CoupleSettings) = dao.insertCoupleSettings(settings)
  suspend fun saveCoupleSettings(settings: CoupleSettings) = dao.insertCoupleSettings(settings)

  suspend fun clearAllData() {
    dao.clearChatMessages()
    dao.clearLoveNotes()
    dao.clearStoryMilestones()
    dao.clearJournalEntries()
    dao.clearCalendarEvents()
    dao.clearMoods()
    dao.clearCoupleSettings()
  }
}
