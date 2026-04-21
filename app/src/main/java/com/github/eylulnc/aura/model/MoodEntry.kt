package com.github.eylulnc.aura.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mood_entries",
    indices = [Index(value = ["userId", "date"], unique = true)]
)
data class MoodEntry(
    @PrimaryKey val id: String,
    @ColumnInfo(defaultValue = "") val userId: String = "",
    val date: String,
    val timestamp: Long,
    val mood: Int,
    val note: String? = null,
    val syncedAt: Long? = null
)
