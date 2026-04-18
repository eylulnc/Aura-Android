package com.github.eylulnc.aura.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.eylulnc.aura.preferences.AppPreferences
import com.github.eylulnc.aura.repository.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class SettingsViewModel(
    private val prefs: AppPreferences,
    private val repository: MoodRepository
) : ViewModel() {

    private val _themeMode = MutableStateFlow(prefs.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.setThemeMode(mode)
        _themeMode.value = mode
    }

    fun deleteAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAll()
            onComplete()
        }
    }

    fun seedDemoData() {
        viewModelScope.launch { repository.seedDemoData() }
    }
}
