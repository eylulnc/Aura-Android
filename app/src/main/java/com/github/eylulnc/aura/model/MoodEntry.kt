package com.github.eylulnc.aura.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries", indices = [Index(value = ["date"], unique = true)])
data class MoodEntry(
    @PrimaryKey val id: String,         // UUID
    val userId: String? = null,         // Firebase UID — null for guest
    val date: String,                   // YYYY-MM-DD — derived from timestamp, stored for fast day queries
    val timestamp: Long,
    val mood: Int,                      // 1–13 matching MoodFace.id
    val note: String? = null,
    val syncedAt: Long? = null          // null = not yet synced to Firestore
)
