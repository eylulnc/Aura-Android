package com.github.eylulnc.aura.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.foundation.shape.CircleShape
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onSignedOut: () -> Unit = {},
    onNavigateToDataPrivacy: () -> Unit = {}
) {
    val colors = auraColors
    val themeMode by viewModel.themeMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val signInError by viewModel.signInError.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    val showSyncConflict by viewModel.showSyncConflictDialog.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.setNotificationsEnabled(true)
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                    ) {
                        if (currentUser!!.photoUrl != null) {
                            AsyncImage(
                                model = currentUser!!.photoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(Spacing.xl + Spacing.m)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(Spacing.xl + Spacing.m)
                                    .clip(CircleShape)
                                    .background(colors.surfaceSubtle)
                            ) {
                                Text(
                                    text = currentUser!!.displayName?.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = FontSize.m,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.accent
                                )
                            }
                        }
                        Column {
                            currentUser!!.displayName?.let {
                                Text(text = it, fontSize = FontSize.m, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                            currentUser!!.email?.let {
                                Text(text = it, fontSize = FontSize.s, color = colors.textSecondary)
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.m))
                    Text(
                        text = stringResource(R.string.settings_sign_out),
                        fontSize = FontSize.m,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clickable { viewModel.signOut(onSignedOut) }
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

            SettingsGroup(label = stringResource(R.string.settings_section_reminders), colors = colors) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_reminders_toggle),
                        fontSize = FontSize.m,
                        color = colors.textPrimary
                    )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setNotificationsEnabled(true)
                                }
                            } else {
                                viewModel.setNotificationsEnabled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.surface,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surfaceSubtle
                        )
                    )
                }
                if (notificationsEnabled) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Spacing.s),
                        thickness = 0.5.dp,
                        color = colors.textSecondary.copy(alpha = 0.15f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_reminders_time),
                            fontSize = FontSize.m,
                            color = colors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Spacing.s))
                                .background(colors.surfaceSubtle)
                                .padding(horizontal = Spacing.m, vertical = Spacing.xs)
                        ) {
                            Text(
                                text = formatTime(reminderHour, reminderMinute),
                                fontSize = FontSize.s,
                                fontWeight = FontWeight.Medium,
                                color = colors.accent
                            )
                        }
                    }
                }
            }

            SettingsGroup(label = stringResource(R.string.settings_section_data_privacy), colors = colors) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDataPrivacy() }
                        .padding(vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_section_data_privacy),
                        fontSize = FontSize.m,
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
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

    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onConfirm = { h, m ->
                viewModel.setReminderTime(h, m)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            colors = colors
        )
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

    if (showSyncConflict) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncConflict() },
            title = { Text("Account Data Found") },
            text = {
                Text("Your account already has saved mood data, but you also have unsaved guest data on this device. Which data would you like to keep?\n\nThe other data will be permanently deleted.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.resolveSyncConflict(keepLocal = false, context) }) {
                    Text("Keep Account Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resolveSyncConflict(keepLocal = true, context) }) {
                    Text("Keep Device Data")
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
internal fun SettingsGroup(label: String, colors: AuraColors, content: @Composable ColumnScope.() -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    colors: AuraColors
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
                Spacer(Modifier.height(Spacing.l))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.settings_delete_cancel), color = colors.textSecondary)
                    }
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                        Text(stringResource(R.string.notification_time_confirm), color = colors.accent)
                    }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    val suffix = if (hour < 12) "AM" else "PM"
    return String.format(Locale.getDefault(), "%d:%02d %s", h, minute, suffix)
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
