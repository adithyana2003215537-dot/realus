package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.RealUsDao
import com.example.data.model.CalendarEvent
import com.example.data.model.ChatMessage
import com.example.data.model.CoupleSettings
import com.example.data.model.JournalEntry
import com.example.data.model.LoveNote
import com.example.data.model.StoryMilestone
import com.example.data.model.UserMood

@Database(
  entities = [
    ChatMessage::class,
    LoveNote::class,
    StoryMilestone::class,
    JournalEntry::class,
    CalendarEvent::class,
    UserMood::class,
    CoupleSettings::class
  ],
  version = 4,
  exportSchema = false
)
abstract class RealUsDatabase : RoomDatabase() {
  abstract fun realUsDao(): RealUsDao

  companion object {
    @Volatile
    private var INSTANCE: RealUsDatabase? = null

    fun getDatabase(context: Context): RealUsDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          RealUsDatabase::class.java,
          "realus_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
