package com.github.eylulnc.aura.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Spacing {
    // Spacing
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    // Corner radius
    val radiusCard = 12.dp
    val radiusButton = 12.dp

    // Borders
    val borderWidth = 1.dp
    val selectionBorderWidth = 2.dp

    // Mood face sizes
    val moodFaceSize = 52.dp   // SVG inside picker cell
    val moodFaceWrapperSize = 56.dp   // tap target / selection ring
    val moodFaceLargeSize = 96.dp   // logged state display

    // Component sizes
    val noteInputMinHeight = 80.dp

    // Login screen
    val loginIconSize = 160.dp
    val loginIconRadius = 36.dp
    val loginIconShadowElevation = 32.dp
    val loginDividerWidth = 80.dp
    val radiusPill = 50.dp
}

object FontSize {
    val xs = 12.sp   // labels, captions
    val s = 14.sp   // secondary text, sub-labels
    val m = 16.sp   // body text
    val l = 22.sp   // section headings
    val xl = 24.sp   // mood label, primary headings
}
