package com.github.eylulnc.aura.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPrivacyScreen(
    viewModel: SettingsViewModel,
    onAccountDeleted: () -> Unit,
    onBack: () -> Unit
) {
    val colors = auraColors
    val currentUser by viewModel.currentUser.collectAsState()
    val signInError by viewModel.signInError.collectAsState()
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_section_data_privacy),
                        fontSize = FontSize.m,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = Spacing.l)
                .padding(top = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            if (currentUser != null) {
                DestructiveCard(
                    title = stringResource(R.string.data_privacy_delete_account_title),
                    description = stringResource(R.string.data_privacy_delete_account_description),
                    buttonLabel = stringResource(R.string.data_privacy_delete_account_button),
                    colors = colors,
                    onClick = { showDeleteAccountDialog = true }
                )
            } else {
                DestructiveCard(
                    title = stringResource(R.string.data_privacy_delete_data_title),
                    description = stringResource(R.string.data_privacy_delete_data_description),
                    buttonLabel = stringResource(R.string.data_privacy_delete_data_button),
                    colors = colors,
                    onClick = { showDeleteDataDialog = true }
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

    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { Text(stringResource(R.string.data_privacy_delete_data_confirm_title)) },
            text = { Text(stringResource(R.string.data_privacy_delete_data_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDataDialog = false
                    viewModel.deleteAllData {}
                }) {
                    Text(
                        stringResource(R.string.data_privacy_delete_data_confirm_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) {
                    Text(stringResource(R.string.settings_delete_cancel))
                }
            },
            containerColor = colors.surface
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(stringResource(R.string.data_privacy_delete_account_confirm_title)) },
            text = { Text(stringResource(R.string.data_privacy_delete_account_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAccountDialog = false
                    viewModel.deleteAccount(context) { onAccountDeleted() }
                }) {
                    Text(
                        stringResource(R.string.data_privacy_delete_account_confirm_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.settings_delete_cancel))
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun DestructiveCard(
    title: String,
    description: String,
    buttonLabel: String,
    colors: AuraColors,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .background(colors.surface)
            .padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        Text(
            text = title,
            fontSize = FontSize.m,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
        Text(
            text = description,
            fontSize = FontSize.s,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(Spacing.xs))
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Spacing.radiusButton),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error
            )
        ) {
            Text(buttonLabel, color = MaterialTheme.colorScheme.error)
        }
    }
}
