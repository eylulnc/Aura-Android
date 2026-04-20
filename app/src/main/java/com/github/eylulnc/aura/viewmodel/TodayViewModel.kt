package com.github.eylulnc.aura.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.eylulnc.aura.constants.POSITIVE_MOOD_IDS
import com.github.eylulnc.aura.constants.MoodFace
import com.github.eylulnc.aura.constants.getMoodFace
import com.github.eylulnc.aura.model.MoodEntry
import com.github.eylulnc.aura.repository.MoodRepository
import com.github.eylulnc.aura.widget.AuraMoodWidgetReceiver
import com.github.eylulnc.aura.widget.AuraStreakWidgetReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayUiState(
    val todayEntry: MoodEntry? = null,
    val isLoading: Boolean = true,
    val streak: Int = 0,
    val weekEntries: List<MoodEntry?> = List(7) { null },
    val topMoodsThisMonth: List<Pair<MoodFace, Int>> = emptyList(),
    val daysThisMonth: Int = 0,
    val positivePercent: Int? = null
)

class TodayViewModel(
    application: Application,
    private val repository: MoodRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllFlow().collect { all ->
                val today = LocalDate.now().toString()
                val todayEntry = all.firstOrNull { it.date == today }
                val weekEntries = computeWeekEntries(all)
                _uiState.update {
                    it.copy(
                        todayEntry = todayEntry,
                        isLoading = false,
                        streak = computeStreak(all),
                        weekEntries = weekEntries,
                        topMoodsThisMonth = computeTopMoodsThisMonth(all),
                        daysThisMonth = countDaysThisMonth(all),
                        positivePercent = computePositivePercent(weekEntries)
                    )
                }
            }
        }
    }

    fun confirmMood(moodId: Int, note: String) {
        viewModelScope.launch {
            val entry = _uiState.value.todayEntry
            if (entry == null) repository.logMood(moodId, note)
            else repository.editMood(entry, moodId, note)
            AuraMoodWidgetReceiver.requestUpdate(getApplication())
            AuraStreakWidgetReceiver.requestUpdate(getApplication())
        }
    }

    private fun computeStreak(entries: List<MoodEntry>): Int {
        val dateSet = entries.map { it.date }.toSet()
        val today = LocalDate.now()
        // If today isn't logged yet, preserve streak — start counting from yesterday
        var date = if (dateSet.contains(today.toString())) today else today.minusDays(1)
        var count = 0
        while (dateSet.contains(date.toString())) {
            count++
            date = date.minusDays(1)
        }
        return count
    }

    private fun computeWeekEntries(entries: List<MoodEntry>): List<MoodEntry?> {
        val entryMap = entries.associateBy { it.date }
        val today = LocalDate.now()
        return (6 downTo 0).map { daysAgo ->
            entryMap[today.minusDays(daysAgo.toLong()).toString()]
        }
    }

    private fun computeTopMoodsThisMonth(entries: List<MoodEntry>): List<Pair<MoodFace, Int>> {
        val monthPrefix = LocalDate.now().toString().substring(0, 7)
        return entries
            .filter { it.date.startsWith(monthPrefix) }
            .groupBy { it.mood }
            .map { (moodId, list) -> getMoodFace(moodId) to list.size }
            .sortedByDescending { it.second }
            .take(3)
    }

    private fun countDaysThisMonth(entries: List<MoodEntry>): Int {
        val monthPrefix = LocalDate.now().toString().substring(0, 7)
        return entries.count { it.date.startsWith(monthPrefix) }
    }

    private fun computePositivePercent(weekEntries: List<MoodEntry?>): Int? {
        val existing = weekEntries.filterNotNull()
        if (existing.size < 3) return null
        return existing.count { it.mood in POSITIVE_MOOD_IDS } * 100 / existing.size
    }
}
