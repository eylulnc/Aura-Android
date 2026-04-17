package com.github.eylulnc.aura.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.eylulnc.aura.model.MoodEntry
import com.github.eylulnc.aura.preferences.AppPreferences
import com.github.eylulnc.aura.repository.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class SortOrder { Newest, Oldest }

data class HistoryUiState(
    val allEntries: List<MoodEntry> = emptyList(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDay: Int? = null,
    val sortOrder: SortOrder = SortOrder.Newest,
    val sheetEntry: MoodEntry? = null,
    val pendingMoodId: Int? = null,
    val note: String = "",
    val isSheetOpen: Boolean = false,
    val isEditMode: Boolean = false
) {
    val monthEntries: List<MoodEntry>
        get() {
            val all = allEntries
                .filter { it.date.startsWith(selectedMonth.toString()) }
                .let { if (sortOrder == SortOrder.Newest) it.sortedByDescending { e -> e.timestamp } else it.sortedBy { e -> e.timestamp } }
            return if (selectedDay != null)
                all.filter { it.date.takeLast(2).toInt() == selectedDay }
            else all
        }

    val entryByDay: Map<Int, MoodEntry>
        get() = allEntries
            .filter { it.date.startsWith(selectedMonth.toString()) }
            .associateBy { it.date.takeLast(2).toInt() }

}

class HistoryViewModel(
    private val repository: MoodRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    val firstLaunchMonth: YearMonth = YearMonth.from(prefs.getFirstLaunchDate())

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllFlow().collect { entries ->
                _uiState.update { it.copy(allEntries = entries) }
            }
        }
    }

    fun prevMonth() {
        _uiState.update {
            if (it.selectedMonth > firstLaunchMonth)
                it.copy(selectedMonth = it.selectedMonth.minusMonths(1), selectedDay = null)
            else it
        }
    }

    fun nextMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1), selectedDay = null) }
    }

    fun selectDay(day: Int) {
        _uiState.update { it.copy(selectedDay = if (it.selectedDay == day) null else day) }
    }

    fun toggleSort() {
        _uiState.update {
            it.copy(sortOrder = if (it.sortOrder == SortOrder.Newest) SortOrder.Oldest else SortOrder.Newest)
        }
    }

    fun openSheet(entry: MoodEntry) {
        val isToday = entry.date == LocalDate.now().toString()
        _uiState.update {
            it.copy(
                sheetEntry = entry,
                pendingMoodId = entry.mood,
                note = entry.note ?: "",
                isSheetOpen = true,
                isEditMode = isToday
            )
        }
    }

    fun closeSheet() {
        _uiState.update { it.copy(isSheetOpen = false) }
    }

    fun selectMood(id: Int) {
        _uiState.update { it.copy(pendingMoodId = id) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun updateEntry() {
        val state = _uiState.value
        val entry = state.sheetEntry ?: return
        val moodId = state.pendingMoodId ?: return
        viewModelScope.launch {
            repository.editMood(entry, moodId, state.note)
            _uiState.update { it.copy(isSheetOpen = false) }
        }
    }

    fun deleteEntry() {
        val entry = _uiState.value.sheetEntry ?: return
        viewModelScope.launch {
            repository.deleteMood(entry)
            _uiState.update { it.copy(isSheetOpen = false) }
        }
    }
}
