package com.github.eylulnc.aura.preferences

import android.content.Context
import java.time.LocalDate
import androidx.core.content.edit
import com.github.eylulnc.aura.ui.settings.ThemeMode

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("aura_prefs", Context.MODE_PRIVATE)

    fun recordFirstLaunchIfNeeded() {
        if (!prefs.contains(KEY_FIRST_LAUNCH)) {
            prefs.edit { putString(KEY_FIRST_LAUNCH, LocalDate.now().toString()) }
        }
    }

    fun getFirstLaunchDate(): LocalDate =
        prefs.getString(KEY_FIRST_LAUNCH, null)
            ?.let { LocalDate.parse(it) }
            ?: LocalDate.now()

    fun updateFirstLaunchIfEarlier(date: LocalDate) {
        if (date.isBefore(getFirstLaunchDate())) {
            prefs.edit { putString(KEY_FIRST_LAUNCH, date.toString()) }
        }
    }

    fun getThemeMode(): ThemeMode =
        ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)

    fun setThemeMode(mode: ThemeMode) =
        prefs.edit { putString(KEY_THEME, mode.name) }

    fun hasCompletedOnboarding(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingCompleted() = prefs.edit { putBoolean(KEY_ONBOARDING_DONE, true) }

    fun resetOnboarding() = prefs.edit { remove(KEY_ONBOARDING_DONE) }

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)

    fun setNotificationsEnabled(enabled: Boolean) =
        prefs.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }

    fun getReminderHour(): Int = prefs.getInt(KEY_REMINDER_HOUR, DEFAULT_REMINDER_HOUR)

    fun getReminderMinute(): Int = prefs.getInt(KEY_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE)

    fun setReminderTime(hour: Int, minute: Int) =
        prefs.edit {
            putInt(KEY_REMINDER_HOUR, hour)
            putInt(KEY_REMINDER_MINUTE, minute)
        }

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch_date"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val DEFAULT_REMINDER_HOUR = 20
        const val DEFAULT_REMINDER_MINUTE = 0
    }
}
