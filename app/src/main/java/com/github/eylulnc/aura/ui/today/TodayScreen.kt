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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.eylulnc.aura.constants.getMoodFace
import com.github.eylulnc.aura.ui.components.MoodPicker
import com.github.eylulnc.aura.ui.components.MoodSvgImage
import com.github.eylulnc.aura.ui.theme.auraColors
import com.github.eylulnc.aura.viewmodel.TodayViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.graphics.toColorInt

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = auraColors
    val showPicker = state.todayEntry == null || state.isEditing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {

        Text(
            text = formattedDate(),
            fontSize = 16.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

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
    colors: com.github.eylulnc.aura.ui.theme.AuraColors,
    onSelect: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = "How are you feeling?",
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.textPrimary
    )

    Spacer(modifier = Modifier.height(16.dp))

    MoodPicker(
        selected = state.pendingMoodId,
        onSelect = onSelect,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Note input
    OutlinedTextField(
        value = state.note,
        onValueChange = onNoteChange,
        placeholder = {
            Text("Add a note… (optional)", color = colors.textSecondary)
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.accent
        ),
        maxLines = 5
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.isEditing) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text("Cancel", color = colors.textSecondary)
            }
        }

        val confirmed = state.pendingMoodId != null
        Button(
            onClick = onConfirm,
            enabled = confirmed,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                disabledContainerColor = colors.surfaceSubtle
            )
        ) {
            Text(
                text = if (state.isEditing) "Update" else "Log mood",
                color = if (confirmed) Color.White else colors.textSecondary
            )
        }
    }
}


@Composable
private fun LoggedContent(
    state: com.github.eylulnc.aura.viewmodel.TodayUiState,
    colors: com.github.eylulnc.aura.ui.theme.AuraColors,
    onEdit: () -> Unit
) {
    val entry = state.todayEntry ?: return
    val face = getMoodFace(entry.mood)
    val moodColor = Color(face.color.toColorInt())

    Text(
        text = "You're feeling",
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.textPrimary
    )

    Spacer(modifier = Modifier.height(32.dp))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        MoodSvgImage(
            moodFace = face,
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = face.label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = moodColor
        )

        Text(
            text = face.sub,
            fontSize = 14.sp,
            color = colors.textSecondary
        )
    }

    if (!entry.note.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .background(colors.surface, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Your note",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.note,
                fontSize = 15.sp,
                color = colors.textPrimary
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedButton(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
    ) {
        Text("Edit", color = colors.textSecondary)
    }
}

private fun formattedDate(): String =
    LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    )

