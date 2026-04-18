package com.github.eylulnc.aura.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.eylulnc.aura.model.MoodEntry
import com.github.eylulnc.aura.repository.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodayUiState(
    val todayEntry: MoodEntry? = null,
    val isEditing: Boolean = false,
    val pendingMoodId: Int? = null,
    val note: String = "",
    val isLoading: Boolean = true
)

class TodayViewModel(private val repository: MoodRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTodayFlow().collect { entry ->
                _uiState.update {
                    if (it.isEditing) it.copy(todayEntry = entry, isLoading = false)
                    else it.copy(todayEntry = entry, pendingMoodId = entry?.mood, note = entry?.note ?: "", isLoading = false)
                }
            }
        }
    }

    fun selectMood(id: Int) {
        _uiState.update { it.copy(pendingMoodId = id) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun confirm() {
        val state = _uiState.value
        val moodId = state.pendingMoodId ?: return

        viewModelScope.launch {
            if (state.todayEntry == null) {
                repository.logMood(moodId, state.note)
            } else {
                repository.editMood(state.todayEntry, moodId, state.note)
            }
            _uiState.update { it.copy(isEditing = false) }
        }
    }

    fun startEdit() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun cancelEdit() {
        val entry = _uiState.value.todayEntry
        _uiState.update {
            it.copy(
                isEditing = false,
                pendingMoodId = entry?.mood,
                note = entry?.note ?: ""
            )
        }
    }
}
