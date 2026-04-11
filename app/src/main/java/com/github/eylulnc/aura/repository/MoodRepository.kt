package com.github.eylulnc.aura.repository

import com.github.eylulnc.aura.model.MoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class MoodRepository(private val dao: MoodDao) {

    fun getAllFlow(): Flow<List<MoodEntry>> = dao.getAllFlow()

    suspend fun getAll(): List<MoodEntry> = dao.getAll()

    suspend fun getToday(): MoodEntry? = dao.getByDate(today())


    suspend fun logMood(moodId: Int, note: String?) {
        val entry = MoodEntry(
            id = UUID.randomUUID().toString(),
            date = today(),
            timestamp = System.currentTimeMillis(),
            mood = moodId,
            note = note?.ifBlank { null }
        )
        dao.insert(entry)
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

    private fun today(): String =
        DateTimeFormatter.ISO_LOCAL_DATE.format(
            Instant.now().atZone(ZoneId.systemDefault())
        )
}
