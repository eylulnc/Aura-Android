package com.github.eylulnc.aura.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.github.eylulnc.aura.ui.theme.auraColors

@Composable
fun TodayScreen() {
    val colors = auraColors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Text("Today", fontSize = 16.sp, color = colors.textSecondary)
    }
}
