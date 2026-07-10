package com.github.eylulnc.aura.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.eylulnc.aura.ui.theme.*

@Composable
fun FullScreenLoading(message: String) {
    val colors = auraColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = colors.accent)
        Spacer(modifier = Modifier.height(Spacing.l))
        Text(
            text = message,
            fontSize = FontSize.s,
            color = colors.textSecondary
        )
    }
}
