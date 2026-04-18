package com.github.eylulnc.aura.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.eylulnc.aura.auth.AuthRepository
import com.github.eylulnc.aura.preferences.AppPreferences
import com.github.eylulnc.aura.repository.MoodRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class SettingsViewModel(
    private val prefs: AppPreferences,
    private val repository: MoodRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _themeMode = MutableStateFlow(prefs.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.currentUser)

    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.setThemeMode(mode)
        _themeMode.value = mode
    }

    fun signIn(activityContext: Context) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activityContext)
            if (result.isSuccess) {
                repository.syncAllToFirestore()
            } else {
                _signInError.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun clearSignInError() {
        _signInError.value = null
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
