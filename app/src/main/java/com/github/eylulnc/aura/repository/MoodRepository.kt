package com.github.eylulnc.aura.repository

import com.github.eylulnc.aura.auth.AuthRepository
import com.github.eylulnc.aura.constants.SyncState
import com.github.eylulnc.aura.model.MoodEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
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

    private fun currentUid(): String = authRepository.currentUser?.uid ?: ""

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllFlow(): Flow<List<MoodEntry>> =
        authRepository.authStateFlow.flatMapLatest { user ->
            dao.getAllFlow(user?.uid ?: "")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTodayFlow(): Flow<MoodEntry?> =
        authRepository.authStateFlow.flatMapLatest { user ->
            dao.getByDateFlow(user?.uid ?: "", today())
        }

    suspend fun getAll(): List<MoodEntry> = dao.getAll(currentUid())

    suspend fun getEarliestEntryDate(): String? = dao.getEarliestDate(currentUid())

    suspend fun getToday(): MoodEntry? = dao.getByDate(currentUid(), today())

    suspend fun logMood(moodId: Int, note: String?) {
        val userId = currentUid()
        val existing = dao.getByDate(userId, today())
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

    private suspend fun syncToFirestore(entry: MoodEntry) {
        val userId = authRepository.currentUser?.uid ?: return
        val now = System.currentTimeMillis()
        val synced = entry.copy(userId = userId, syncedAt = now)
        dao.update(synced)
        firestore.userEntries(userId).document(synced.id).set(synced.toMap()).await()
    }

    suspend fun checkSyncStateOnLogin(): SyncState {
        val userId = authRepository.currentUser?.uid ?: return SyncState.NO_DATA

        // Check if there is local guest data
        val localGuestEntries = dao.getAll("")
        val hasLocalData = localGuestEntries.isNotEmpty()

        // Check if the remote account already has data
        val remoteDocs = firestore.userEntries(userId).limit(1).get().await()
        val hasRemoteData = !remoteDocs.isEmpty

        return when {
            hasLocalData && !hasRemoteData -> SyncState.UPLOAD_LOCAL
            !hasLocalData && hasRemoteData -> SyncState.DOWNLOAD_REMOTE
            hasLocalData && hasRemoteData -> SyncState.CONFLICT
            else -> SyncState.NO_DATA
        }
    }

    // Option A: Keep Device Data (Upload local, overwrite remote)
    suspend fun pushLocalToRemote() {
        val userId = authRepository.currentUser?.uid ?: return
        val now = System.currentTimeMillis()

        // Grab guest entries and assign them to the new user
        val localEntries = dao.getAll("")
        localEntries.forEach { entry ->
            val withUser = entry.copy(userId = userId, syncedAt = now)
            dao.update(withUser)
            firestore.userEntries(userId).document(withUser.id).set(withUser.toMap()).await()
        }
    }

    // Option B: Keep Account Data (Wipe local guest data, download remote)
    suspend fun pullRemoteToLocal() {
        val userId = authRepository.currentUser?.uid ?: return

        // Wipe the local guest data
        dao.deleteAll()

        // Fetch and save all remote entries locally
        val remoteEntries = firestore.userEntries(userId).get().await()
        remoteEntries.forEach { doc ->
            val entry = MoodEntry(
                id = doc.id,
                userId = doc.getString("userId") ?: userId,
                date = doc.getString("date") ?: return@forEach,
                timestamp = doc.getLong("timestamp") ?: return@forEach,
                mood = doc.getLong("mood")?.toInt() ?: return@forEach,
                note = doc.getString("note"),
                syncedAt = doc.getLong("syncedAt")
            )
            dao.insert(entry)
        }
    }

    suspend fun seedDemoData() {
        val userId = currentUid()
        val start = LocalDate.of(2025, 11, 1)
        val end = LocalDate.now().minusDays(1)
        val weightedMoods = listOf(3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 9, 10, 11, 12, 1, 2, 13)
        var date = start
        while (!date.isAfter(end)) {
            if (Random.nextFloat() > 0.2f) {
                val dateStr = date.toString()
                if (dao.getByDate(userId, dateStr) == null) {
                    dao.insert(
                        MoodEntry(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            date = dateStr,
                            timestamp = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000,
                            mood = weightedMoods.random()
                        )
                    )
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

