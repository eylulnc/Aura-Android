package com.github.eylulnc.aura.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.github.eylulnc.aura.BuildConfig
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val colors = auraColors
    val themeMode by viewModel.themeMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val signInError by viewModel.signInError.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.l)
                .padding(top = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            SettingsGroup(label = stringResource(R.string.settings_section_account), colors = colors) {
                if (currentUser != null) {
                    Text(
                        text = currentUser!!.email ?: currentUser!!.displayName ?: "",
                        fontSize = FontSize.s,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.height(Spacing.m))
                    Text(
                        text = stringResource(R.string.settings_sign_out),
                        fontSize = FontSize.m,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clickable { viewModel.signOut() }
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.settings_sign_in),
                        fontSize = FontSize.m,
                        color = colors.accent,
                        modifier = Modifier
                            .clickable { viewModel.signIn(context) }
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs)
                    )
                }
            }

            SettingsGroup(label = stringResource(R.string.settings_section_appearance), colors = colors) {
                ThemePicker(selected = themeMode, onSelect = viewModel::setThemeMode, colors = colors)
            }

            if (BuildConfig.DEBUG) {
                SettingsGroup(label = "Dev", colors = colors) {
                    Text(
                        text = "Seed demo data (Nov → yesterday)",
                        fontSize = FontSize.m,
                        color = colors.accent,
                        modifier = Modifier
                            .clickable { viewModel.seedDemoData() }
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs)
                    )
                }
            }

            SettingsGroup(label = stringResource(R.string.settings_section_data), colors = colors) {
                Text(
                    text = stringResource(R.string.settings_delete_all),
                    fontSize = FontSize.m,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { showDeleteDialog = true }
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs)
                )
            }

            Spacer(Modifier.height(Spacing.s))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xl)
            ) {
                Text(
                    text = stringResource(R.string.app_name).uppercase(),
                    fontSize = FontSize.xs,
                    color = colors.textSecondary,
                    letterSpacing = TextUnit(1.5f, TextUnitType.Sp)
                )
                Text(
                    text = stringResource(R.string.settings_version, versionName),
                    fontSize = FontSize.xs,
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.settings_developer),
                    fontSize = FontSize.xs,
                    color = colors.textSecondary.copy(alpha = 0.5f)
                )
            }
        }
    }

    signInError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSignInError() },
            title = { Text(stringResource(R.string.settings_sign_in_error_title)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSignInError() }) {
                    Text(stringResource(R.string.settings_delete_cancel))
                }
            },
            containerColor = colors.surface
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
private fun SettingsGroup(label: String, colors: AuraColors, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(
            text = label.uppercase(),
            fontSize = FontSize.xs,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary,
            letterSpacing = TextUnit(1.5f, TextUnitType.Sp),
            modifier = Modifier.padding(horizontal = Spacing.s)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Spacing.radiusCard))
                .background(colors.surface)
                .padding(Spacing.l),
            content = content
        )
    }
}

@Composable
private fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, colors: AuraColors) {
    val options = listOf(
        ThemeMode.SYSTEM to R.string.settings_theme_system,
        ThemeMode.LIGHT to R.string.settings_theme_light,
        ThemeMode.DARK to R.string.settings_theme_dark,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.radiusButton))
            .background(colors.textSecondary.copy(alpha = 0.12f))
            .padding(Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        options.forEach { (mode, labelRes) ->
            val isSelected = selected == mode
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Spacing.s))
                    .background(if (isSelected) colors.background else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(vertical = Spacing.s)
            ) {
                Text(
                    text = stringResource(labelRes),
                    fontSize = FontSize.s,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) colors.textPrimary else colors.textSecondary
                )
            }
        }
    }
}
