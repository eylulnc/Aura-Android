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

    @Query("SELECT * FROM mood_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllFlow(userId: String): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAll(userId: String): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getByDate(userId: String, date: String): MoodEntry?

    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND date = :date LIMIT 1")
    fun getByDateFlow(userId: String, date: String): Flow<MoodEntry?>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getById(id: String): MoodEntry?

    @Query("SELECT MIN(date) FROM mood_entries WHERE userId = :userId")
    suspend fun getEarliestDate(userId: String): String?
}
