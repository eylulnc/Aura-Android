package com.github.eylulnc.aura.repository

import androidx.room.*
import com.github.eylulnc.aura.model.MoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry)

    @Update
    suspend fun update(entry: MoodEntry)

    @Delete
    suspend fun delete(entry: MoodEntry)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    suspend fun getAll(): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    suspend fun getByDate(date: String): MoodEntry?

    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    fun getByDateFlow(date: String): Flow<MoodEntry?>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getById(id: String): MoodEntry?
}
