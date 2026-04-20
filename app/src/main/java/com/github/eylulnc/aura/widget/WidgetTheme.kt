package com.github.eylulnc.aura.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.color.ColorProvider
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle

internal object WidgetColors {
    val background = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1C1C1A))
    val surfaceSubtle = ColorProvider(day = Color(0xFFF0EFEB), night = Color(0xFF252523))
    val textPrimary = ColorProvider(day = Color(0xFF1A1917), night = Color(0xFFEDECEA))
    val textSecondary = ColorProvider(day = Color(0xFF89857E), night = Color(0xFF706C65))
    val accent = ColorProvider(day = Color(0xFF7B96E8), night = Color(0xFFA8BCF0))
    val accentOnColor = ColorProvider(day = Color.White, night = Color(0xFF111110))
}

internal object WidgetDimens {
    val cornerRadius = 16.dp
    val padding = 10.dp
    val moodFaceSize = 85.dp
    val placeholderSize = 44.dp
    val placeholderRadius = 22.dp
}

internal object WidgetTextStyle {
    val title = TextStyle(
        color = WidgetColors.accent,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
    val label = TextStyle(color = WidgetColors.textSecondary, fontSize = 16.sp)
    val moodName = TextStyle(
        color = WidgetColors.textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
    val streakNumber =
        TextStyle(color = WidgetColors.accent, fontSize = 42.sp, fontWeight = FontWeight.Bold)
    val placeholder =
        TextStyle(color = WidgetColors.accent, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    val fire = TextStyle(fontSize = 28.sp)
}
