package com.github.eylulnc.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.eylulnc.aura.ui.navigation.AppNavigation
import com.github.eylulnc.aura.ui.settings.SettingsViewModel
import com.github.eylulnc.aura.ui.settings.ThemeMode
import com.github.eylulnc.aura.ui.theme.AuraTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            AuraTheme(darkTheme = darkTheme) {
                AppNavigation(settingsViewModel)
            }
        }
    }
}
