package com.github.eylulnc.aura.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.eylulnc.aura.model.MoodEntry

@Database(
    entities = [MoodEntry::class],
    version = 2,
    exportSchema = false
)

abstract class AuraDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_mood_entries_date ON mood_entries(date)")
            }
        }
    }
}
