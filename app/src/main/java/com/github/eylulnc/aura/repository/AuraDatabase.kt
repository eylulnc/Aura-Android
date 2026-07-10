package com.github.eylulnc.aura.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.eylulnc.aura.model.MoodEntry

@Database(
    entities = [MoodEntry::class],
    version = 3,
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

        // Swap the unique index from (date) to (userId, date). Guest rows get
        // userId = ''; if duplicate (userId, date) rows already exist, keep the
        // row with the highest timestamp and drop the rest.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE mood_entries_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL DEFAULT '',
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        mood INTEGER NOT NULL,
                        note TEXT,
                        syncedAt INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO mood_entries_new (id, userId, date, timestamp, mood, note, syncedAt)
                    SELECT id, COALESCE(userId, ''), date, timestamp, mood, note, syncedAt
                    FROM mood_entries m1
                    WHERE NOT EXISTS (
                        SELECT 1 FROM mood_entries m2
                        WHERE COALESCE(m2.userId, '') = COALESCE(m1.userId, '')
                          AND m2.date = m1.date
                          AND (m2.timestamp > m1.timestamp
                               OR (m2.timestamp = m1.timestamp AND m2.id > m1.id))
                    )
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE mood_entries")
                db.execSQL("ALTER TABLE mood_entries_new RENAME TO mood_entries")
                db.execSQL(
                    "CREATE UNIQUE INDEX index_mood_entries_userId_date ON mood_entries(userId, date)"
                )
            }
        }
    }
}
