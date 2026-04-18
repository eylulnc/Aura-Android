package com.github.eylulnc.aura.repository

import com.github.eylulnc.aura.model.MoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random

class MoodRepository(private val dao: MoodDao) {

    fun getAllFlow(): Flow<List<MoodEntry>> = dao.getAllFlow()

    fun getTodayFlow(): Flow<MoodEntry?> = dao.getByDateFlow(today())

    suspend fun getAll(): List<MoodEntry> = dao.getAll()

    suspend fun getToday(): MoodEntry? = dao.getByDate(today())


    suspend fun logMood(moodId: Int, note: String?) {
        val existing = dao.getByDate(today())
        if (existing != null) {
            dao.update(existing.copy(mood = moodId, note = note?.ifBlank { null }, timestamp = System.currentTimeMillis()))
        } else {
            dao.insert(MoodEntry(
                id = UUID.randomUUID().toString(),
                date = today(),
                timestamp = System.currentTimeMillis(),
                mood = moodId,
                note = note?.ifBlank { null }
            ))
        }
        // TODO: if signed in → sync to Firestore
    }

    suspend fun editMood(entry: MoodEntry, moodId: Int, note: String?) {
        dao.update(entry.copy(mood = moodId, note = note?.ifBlank { null }))
        // TODO: if signed in → sync to Firestore
    }

    suspend fun deleteMood(entry: MoodEntry) {
        dao.delete(entry)
        // TODO: if signed in → delete from Firestore
    }

    suspend fun deleteAll() {
        dao.deleteAll()
        // TODO: if signed in → delete all from Firestore
    }

    suspend fun seedDemoData() {
        val start = LocalDate.of(2025, 11, 1)
        val end = LocalDate.now().minusDays(1)
        val weightedMoods = listOf(3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 9, 10, 11, 12, 1, 2, 13)
        var date = start
        while (!date.isAfter(end)) {
            if (Random.nextFloat() > 0.2f) {
                val dateStr = date.toString()
                if (dao.getByDate(dateStr) == null) {
                    dao.insert(MoodEntry(
                        id = UUID.randomUUID().toString(),
                        date = dateStr,
                        timestamp = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000,
                        mood = weightedMoods.random()
                    ))
                }
            }
            date = date.plusDays(1)
        }
    }

    private fun today(): String =
        DateTimeFormatter.ISO_LOCAL_DATE.format(
            Instant.now().atZone(ZoneId.systemDefault())
        )
}
