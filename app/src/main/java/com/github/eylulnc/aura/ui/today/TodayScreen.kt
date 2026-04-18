package com.github.eylulnc.aura.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.toColorInt
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.constants.getMoodFace
import com.github.eylulnc.aura.ui.components.MoodPicker
import com.github.eylulnc.aura.ui.components.MoodSvgImage
import com.github.eylulnc.aura.ui.theme.*
import com.github.eylulnc.aura.viewmodel.TodayViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = auraColors

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        )
        return
    }

    val showPicker = state.todayEntry == null || state.isEditing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.l)
            .padding(top = Spacing.l, bottom = Spacing.l)
    ) {
        Text(
            text = formattedDate(),
            fontSize = FontSize.m,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(Spacing.l))

        if (showPicker) {
            PickerContent(
                state = state,
                colors = colors,
                onSelect = viewModel::selectMood,
                onNoteChange = viewModel::setNote,
                onConfirm = viewModel::confirm,
                onCancel = viewModel::cancelEdit
            )
        } else {
            LoggedContent(
                state = state,
                colors = colors,
                onEdit = viewModel::startEdit
            )
        }
    }
}

@Composable
private fun PickerContent(
    state: com.github.eylulnc.aura.viewmodel.TodayUiState,
    colors: AuraColors,
    onSelect: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = stringResource(R.string.today_prompt_empty),
        fontSize = FontSize.xl,
        fontWeight = FontWeight.SemiBold,
        color = colors.textPrimary
    )

    Spacer(modifier = Modifier.height(Spacing.l))

    MoodPicker(
        selected = state.pendingMoodId,
        onSelect = onSelect,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(Spacing.l))

    OutlinedTextField(
        value = state.note,
        onValueChange = onNoteChange,
        placeholder = {
            Text(stringResource(R.string.today_note_placeholder), color = colors.textSecondary)
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.noteInputMinHeight),
        shape = RoundedCornerShape(Spacing.radiusCard),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.accent
        ),
        maxLines = 5
    )

    Spacer(modifier = Modifier.height(Spacing.l))

    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
        if (state.isEditing) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(Spacing.radiusButton),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(stringResource(R.string.today_action_cancel), color = colors.textSecondary)
            }
        }

        val confirmed = state.pendingMoodId != null
        Button(
            onClick = onConfirm,
            enabled = confirmed,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(Spacing.radiusButton),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                disabledContainerColor = colors.surfaceSubtle
            )
        ) {
            Text(
                text = stringResource(
                    if (state.isEditing) R.string.today_action_update else R.string.today_action_log
                ),
                color = if (confirmed) Color.White else colors.textSecondary
            )
        }
    }
}

@Composable
private fun LoggedContent(
    state: com.github.eylulnc.aura.viewmodel.TodayUiState,
    colors: AuraColors,
    onEdit: () -> Unit
) {
    val entry = state.todayEntry ?: return
    val face = getMoodFace(entry.mood)
    val moodColor = Color(face.color.toColorInt())

    Text(
        text = stringResource(R.string.today_prompt_logged),
        fontSize = FontSize.l,
        fontWeight = FontWeight.SemiBold,
        color = colors.textPrimary
    )

    Spacer(modifier = Modifier.height(Spacing.xxl))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        MoodSvgImage(
            moodFace = face,
            modifier = Modifier.size(Spacing.moodFaceLargeSize)
        )

        Spacer(modifier = Modifier.height(Spacing.m))

        Text(
            text = face.label,
            fontSize = FontSize.xl,
            fontWeight = FontWeight.Bold,
            color = moodColor
        )

        Text(
            text = face.sub,
            fontSize = FontSize.s,
            color = colors.textSecondary
        )
    }

    if (!entry.note.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(Spacing.borderWidth, colors.border, RoundedCornerShape(Spacing.radiusCard))
                .background(colors.surface, RoundedCornerShape(Spacing.radiusCard))
                .padding(Spacing.l)
        ) {
            Text(
                text = stringResource(R.string.today_note_label),
                fontSize = FontSize.xs,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = entry.note,
                fontSize = FontSize.m,
                color = colors.textPrimary
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.xl))

    OutlinedButton(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.radiusButton),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
    ) {
        Text(stringResource(R.string.today_action_edit), color = colors.textSecondary)
    }
}

private fun formattedDate(): String =
    LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    )
