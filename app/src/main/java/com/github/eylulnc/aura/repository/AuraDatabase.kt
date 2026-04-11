package com.github.eylulnc.aura.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.eylulnc.aura.model.MoodEntry

@Database(
    entities = [MoodEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
}
