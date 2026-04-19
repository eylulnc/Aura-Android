package com.github.eylulnc.aura.repository

import com.github.eylulnc.aura.auth.AuthRepository
import com.github.eylulnc.aura.model.MoodEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random

class MoodRepository(
    private val dao: MoodDao,
    private val authRepository: AuthRepository
) {

    private val firestore = FirebaseFirestore.getInstance()

    fun getAllFlow(): Flow<List<MoodEntry>> = dao.getAllFlow()

    fun getTodayFlow(): Flow<MoodEntry?> = dao.getByDateFlow(today())

    suspend fun getAll(): List<MoodEntry> = dao.getAll()

    suspend fun getEarliestEntryDate(): String? = dao.getEarliestDate()

    suspend fun getToday(): MoodEntry? = dao.getByDate(today())

    suspend fun logMood(moodId: Int, note: String?) {
        val userId = authRepository.currentUser?.uid
        val existing = dao.getByDate(today())
        if (existing != null) {
            val updated = existing.copy(
                mood = moodId,
                note = note?.ifBlank { null },
                timestamp = System.currentTimeMillis()
            )
            dao.update(updated)
            syncToFirestore(updated)
        } else {
            val entry = MoodEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = today(),
                timestamp = System.currentTimeMillis(),
                mood = moodId,
                note = note?.ifBlank { null }
            )
            dao.insert(entry)
            syncToFirestore(entry)
        }
    }

    suspend fun editMood(entry: MoodEntry, moodId: Int, note: String?) {
        val updated = entry.copy(mood = moodId, note = note?.ifBlank { null })
        dao.update(updated)
        syncToFirestore(updated)
    }

    suspend fun deleteMood(entry: MoodEntry) {
        dao.delete(entry)
        val userId = authRepository.currentUser?.uid ?: return
        firestore.userEntries(userId).document(entry.id).delete().await()
    }

    suspend fun deleteAllLocal() {
        dao.deleteAll()
    }

    suspend fun deleteAll() {
        dao.deleteAll()
        val userId = authRepository.currentUser?.uid ?: return
        val docs = firestore.userEntries(userId).get().await()
        docs.forEach { firestore.userEntries(userId).document(it.id).delete().await() }
    }

    suspend fun syncAllToFirestore() {
        val userId = authRepository.currentUser?.uid ?: return
        val now = System.currentTimeMillis()

        // Push local entries to Firestore
        val localEntries = dao.getAll()
        localEntries.forEach { entry ->
            val withUser = entry.copy(userId = userId, syncedAt = now)
            dao.update(withUser)
            firestore.userEntries(userId).document(withUser.id).set(withUser.toMap()).await()
        }

        // Pull remote entries not present locally
        val localIds = localEntries.map { it.id }.toSet()
        val remoteEntries = firestore.userEntries(userId).get().await()
        remoteEntries.forEach { doc ->
            if (doc.id !in localIds) {
                val entry = MoodEntry(
                    id = doc.id,
                    userId = doc.getString("userId"),
                    date = doc.getString("date") ?: return@forEach,
                    timestamp = doc.getLong("timestamp") ?: return@forEach,
                    mood = doc.getLong("mood")?.toInt() ?: return@forEach,
                    note = doc.getString("note"),
                    syncedAt = doc.getLong("syncedAt")
                )
                dao.insert(entry)
            }
        }
    }

    private suspend fun syncToFirestore(entry: MoodEntry) {
        val userId = authRepository.currentUser?.uid ?: return
        val now = System.currentTimeMillis()
        val synced = entry.copy(userId = userId, syncedAt = now)
        dao.update(synced)
        firestore.userEntries(userId).document(synced.id).set(synced.toMap()).await()
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

private fun FirebaseFirestore.userEntries(userId: String) =
    collection("users").document(userId).collection("mood_entries")

private fun MoodEntry.toMap() = mapOf(
    "id" to id,
    "userId" to userId,
    "date" to date,
    "timestamp" to timestamp,
    "mood" to mood,
    "note" to note,
    "syncedAt" to syncedAt
)
