package com.github.eylulnc.aura.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val colors = auraColors
    val themeMode by viewModel.themeMode.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = Spacing.l, vertical = Spacing.xl)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = FontSize.xl,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )

        Spacer(Modifier.height(Spacing.xl))

        SettingsSectionLabel(stringResource(R.string.settings_section_appearance), colors)
        Spacer(Modifier.height(Spacing.m))
        ThemePicker(selected = themeMode, onSelect = viewModel::setThemeMode, colors = colors)

        Spacer(Modifier.height(Spacing.xl))

        SettingsSectionLabel(stringResource(R.string.settings_section_data), colors)
        Spacer(Modifier.height(Spacing.m))
        SettingsRow(
            label = stringResource(R.string.settings_delete_all),
            labelColor = MaterialTheme.colorScheme.error,
            colors = colors,
            onClick = { showDeleteDialog = true }
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(R.string.settings_version, versionName),
            fontSize = FontSize.xs,
            color = colors.textSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.settings_delete_confirm_title)) },
            text = { Text(stringResource(R.string.settings_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAllData {}
                }) {
                    Text(
                        stringResource(R.string.settings_delete_confirm_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.settings_delete_cancel))
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, colors: AuraColors) {
    val options = listOf(
        ThemeMode.LIGHT to R.string.settings_theme_light,
        ThemeMode.SYSTEM to R.string.settings_theme_system,
        ThemeMode.DARK to R.string.settings_theme_dark,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.radiusButton))
            .border(Spacing.borderWidth, colors.border, RoundedCornerShape(Spacing.radiusButton))
    ) {
        options.forEachIndexed { index, (mode, labelRes) ->
            val isSelected = selected == mode
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSelected) colors.accent else colors.surface)
                    .clickable { onSelect(mode) }
                    .padding(vertical = Spacing.m)
            ) {
                Text(
                    text = stringResource(labelRes),
                    fontSize = FontSize.s,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) colors.surface else colors.textSecondary
                )
            }
            if (index < options.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(Spacing.borderWidth)
                        .height(Spacing.xl + Spacing.m)
                        .background(colors.border)
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(label: String, colors: AuraColors) {
    Text(
        text = label.uppercase(),
        fontSize = FontSize.xs,
        fontWeight = FontWeight.Medium,
        color = colors.textSecondary,
        letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp)
    )
}

@Composable
private fun SettingsRow(
    label: String,
    labelColor: androidx.compose.ui.graphics.Color,
    colors: AuraColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .background(colors.surface)
            .border(Spacing.borderWidth, colors.border, RoundedCornerShape(Spacing.radiusCard))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.l, vertical = Spacing.l)
    ) {
        Text(text = label, fontSize = FontSize.m, color = labelColor)
    }
}
