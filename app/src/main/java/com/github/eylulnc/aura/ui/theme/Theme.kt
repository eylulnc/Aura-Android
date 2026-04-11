package com.github.eylulnc.aura.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AuraColors(
    val background: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val border: Color,
    val isDark: Boolean
)

val LightAuraColors = AuraColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceSubtle = LightSurfaceSubtle,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    accent = LightAccent,
    border = LightBorder,
    isDark = false
)

val DarkAuraColors = AuraColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceSubtle = DarkSurfaceSubtle,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    accent = DarkAccent,
    border = DarkBorder,
    isDark = true
)

val LocalAuraColors = staticCompositionLocalOf { LightAuraColors }

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val auraColors = if (darkTheme) DarkAuraColors else LightAuraColors

    val m3Scheme = if (darkTheme) {
        darkColorScheme(
            primary = DarkAccent,
            background = DarkBackground,
            surface = DarkSurface
        )
    } else {
        lightColorScheme(
            primary = LightAccent,
            background = LightBackground,
            surface = LightSurface
        )
    }

    CompositionLocalProvider(LocalAuraColors provides auraColors) {
        MaterialTheme(
            colorScheme = m3Scheme,
            content = content
        )
    }
}

val auraColors: AuraColors
    @Composable get() = LocalAuraColors.current
