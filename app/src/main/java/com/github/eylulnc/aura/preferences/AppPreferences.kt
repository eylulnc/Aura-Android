package com.github.eylulnc.aura.preferences

import android.content.Context
import java.time.LocalDate
import androidx.core.content.edit

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

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch_date"
    }
}
