package com.github.eylulnc.aura.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.eylulnc.aura.auth.AuthRepository
import com.github.eylulnc.aura.constants.SyncState
import com.github.eylulnc.aura.notification.NotificationScheduler
import com.github.eylulnc.aura.preferences.AppPreferences
import com.github.eylulnc.aura.repository.MoodRepository
import com.github.eylulnc.aura.widget.AuraMoodWidgetReceiver
import com.github.eylulnc.aura.widget.AuraStreakWidgetReceiver
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class SettingsViewModel(
    private val prefs: AppPreferences,
    private val repository: MoodRepository,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _themeMode = MutableStateFlow(prefs.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.isNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(prefs.getReminderHour())
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(prefs.getReminderMinute())
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.currentUser)

    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _showSyncConflictDialog = MutableStateFlow(false)
    val showSyncConflictDialog: StateFlow<Boolean> = _showSyncConflictDialog.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.setThemeMode(mode)
        _themeMode.value = mode
    }

    fun signIn(activityContext: Context, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val result = authRepository.signInWithGoogle(activityContext)
                if (result.isSuccess) {
                    when (repository.checkSyncStateOnLogin()) {
                        SyncState.UPLOAD_LOCAL -> {
                            repository.pushLocalToRemote()
                            finishSignInSetup(activityContext, onResult)
                        }

                        SyncState.DOWNLOAD_REMOTE -> {
                            repository.pullRemoteToLocal()
                            finishSignInSetup(activityContext, onResult)
                        }

                        SyncState.CONFLICT -> {
                            _isSyncing.value = false
                            _showSyncConflictDialog.value = true
                        }

                        SyncState.NO_DATA -> {
                            finishSignInSetup(activityContext, onResult)
                        }
                    }
                } else {
                    _signInError.value = result.exceptionOrNull()?.message
                    _isSyncing.value = false
                    onResult?.invoke(false)
                }
            } catch (e: Exception) {
                _isSyncing.value = false
            }
        }
    }

    fun resolveSyncConflict(
        keepLocal: Boolean,
        activityContext: Context,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            _showSyncConflictDialog.value = false
            try {
                if (keepLocal) {
                    repository.pushLocalToRemote()
                } else {
                    repository.pullRemoteToLocal()
                }
                finishSignInSetup(activityContext, onResult)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private suspend fun finishSignInSetup(
        activityContext: Context,
        onResult: ((Boolean) -> Unit)?
    ) {
        repository.getEarliestEntryDate()?.let { dateStr ->
            prefs.updateFirstLaunchIfEarlier(java.time.LocalDate.parse(dateStr))
        }
        AuraMoodWidgetReceiver.requestUpdate(activityContext)
        AuraStreakWidgetReceiver.requestUpdate(activityContext)
        onResult?.invoke(true)
    }

    fun dismissSyncConflict() {
        _showSyncConflictDialog.value = false
        signOut {}
    }

    fun completeOnboarding() = prefs.setOnboardingCompleted()

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.deleteAllLocal()
                authRepository.signOut()
                prefs.resetOnboarding()
                delay(400)
                onComplete()
            } catch (e: Exception) {
                _isSyncing.value = false
            }
        }
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

    fun clearSyncing() {
        _isSyncing.value = false
    }

    fun deleteAccount(activityContext: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.deleteAll()
                val result = authRepository.deleteAccount(activityContext)
                if (result.isSuccess) {
                    prefs.resetOnboarding()
                    delay(400)
                    onComplete()
                } else {
                    _isSyncing.value = false
                    _signInError.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _isSyncing.value = false
                _signInError.value = e.message
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.setNotificationsEnabled(enabled)
        _notificationsEnabled.value = enabled
        if (enabled) {
            notificationScheduler.schedule(prefs.getReminderHour(), prefs.getReminderMinute())
        } else {
            notificationScheduler.cancel()
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.setReminderTime(hour, minute)
        _reminderHour.value = hour
        _reminderMinute.value = minute
        if (prefs.isNotificationsEnabled()) {
            notificationScheduler.schedule(hour, minute)
        }
    }

    fun seedDemoData() {
        viewModelScope.launch { repository.seedDemoData() }
    }
}
