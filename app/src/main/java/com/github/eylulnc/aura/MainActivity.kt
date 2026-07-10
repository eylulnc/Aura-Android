package com.github.eylulnc.aura

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.eylulnc.aura.preferences.AppPreferences
import com.github.eylulnc.aura.ui.events.MoodLoggerEvents
import com.github.eylulnc.aura.ui.navigation.AppNavigation
import com.github.eylulnc.aura.ui.settings.SettingsViewModel
import com.github.eylulnc.aura.ui.settings.ThemeMode
import com.github.eylulnc.aura.ui.theme.AuraTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val prefs: AppPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val showOnboarding = !prefs.hasCompletedOnboarding()
        handleIntent(intent)
        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            AuraTheme(darkTheme = darkTheme) {
                AppNavigation(settingsViewModel, showOnboarding)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_MOOD_LOGGER, false) == true) {
            intent.removeExtra(EXTRA_OPEN_MOOD_LOGGER)
            MoodLoggerEvents.requestOpen()
        }
    }

    companion object {
        const val EXTRA_OPEN_MOOD_LOGGER = "open_mood_logger"
    }
}
