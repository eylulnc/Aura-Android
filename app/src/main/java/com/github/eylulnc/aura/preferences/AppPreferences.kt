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

    fun getThemeMode(): ThemeMode =
        ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)

    fun setThemeMode(mode: ThemeMode) =
        prefs.edit { putString(KEY_THEME, mode.name) }

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch_date"
        private const val KEY_THEME = "theme_mode"
    }
}
