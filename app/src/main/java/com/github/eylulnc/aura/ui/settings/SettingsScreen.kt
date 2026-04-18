package com.github.eylulnc.aura.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.ui.theme.*

@Composable
fun SettingsScreen() {
    val colors = auraColors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Text(
            text = stringResource(R.string.settings_coming_soon),
            fontSize = FontSize.m,
            color = colors.textSecondary
        )
    }
}
