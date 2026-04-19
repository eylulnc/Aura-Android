package com.github.eylulnc.aura.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.toColorInt
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.constants.MOOD_SLIDER_ORDER
import com.github.eylulnc.aura.ui.components.MoodSvgImage
import com.github.eylulnc.aura.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMoodSheet(
    initialMoodId: Int?,
    initialNote: String,
    isEditing: Boolean,
    onConfirm: (moodId: Int, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = auraColors
    val initialIndex = initialMoodId
        ?.let { id -> MOOD_SLIDER_ORDER.indexOfFirst { it.id == id }.takeIf { it >= 0 } }
        ?: 6

    var sliderIndex by remember { mutableIntStateOf(initialIndex) }
    var note by remember { mutableStateOf(initialNote) }
    val selectedMood = MOOD_SLIDER_ORDER[sliderIndex]
    val moodColor = Color(selectedMood.color.toColorInt())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.background,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxl + Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MoodSvgImage(
                moodFace = selectedMood,
                modifier = Modifier.size(Spacing.moodFaceLargeSize)
            )

            Spacer(Modifier.height(Spacing.m))

            Text(
                text = selectedMood.label,
                fontSize = FontSize.xl,
                fontWeight = FontWeight.Bold,
                color = moodColor
            )
            Text(
                text = selectedMood.sub,
                fontSize = FontSize.s,
                color = colors.textSecondary
            )

            Spacer(Modifier.height(Spacing.xl))

            Slider(
                value = sliderIndex.toFloat(),
                onValueChange = { sliderIndex = it.roundToInt() },
                valueRange = 0f..12f,
                steps = 11,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = moodColor,
                    activeTrackColor = moodColor,
                    inactiveTrackColor = colors.border
                )
            )

            Spacer(Modifier.height(Spacing.xl))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = {
                    Text(
                        stringResource(R.string.today_note_placeholder),
                        color = colors.textSecondary
                    )
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

            Spacer(Modifier.height(Spacing.l))

            Button(
                onClick = { onConfirm(selectedMood.id, note) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Spacing.radiusButton),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text(
                    text = stringResource(if (isEditing) R.string.today_action_update else R.string.today_action_log),
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
