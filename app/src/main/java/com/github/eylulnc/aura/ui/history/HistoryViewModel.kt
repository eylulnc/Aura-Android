package com.github.eylulnc.aura.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.eylulnc.aura.model.MoodEntry
import com.github.eylulnc.aura.repository.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class HistoryUiState(
    val allEntries: List<MoodEntry> = emptyList(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val sheetEntry: MoodEntry? = null,
    val pendingMoodId: Int? = null,
    val note: String = "",
    val isSheetOpen: Boolean = false
) {
    val monthEntries: List<MoodEntry>
        get() = allEntries
            .filter { it.date.startsWith(selectedMonth.toString()) }
            .sortedByDescending { it.timestamp }

    val entryByDay: Map<Int, MoodEntry>
        get() = monthEntries.associateBy { it.date.takeLast(2).toInt() }
}

class HistoryViewModel(private val repository: MoodRepository) : ViewModel() {

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
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
    }

    fun nextMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1)) }
    }

    fun openSheet(entry: MoodEntry) {
        _uiState.update {
            it.copy(
                sheetEntry = entry,
                pendingMoodId = entry.mood,
                note = entry.note ?: "",
                isSheetOpen = true
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
