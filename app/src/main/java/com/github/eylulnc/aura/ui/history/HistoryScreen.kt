package com.github.eylulnc.aura.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.eylulnc.aura.R
import androidx.core.graphics.toColorInt
import com.github.eylulnc.aura.constants.getMoodFace
import com.github.eylulnc.aura.model.MoodEntry
import com.github.eylulnc.aura.ui.components.MoodPicker
import com.github.eylulnc.aura.ui.components.MoodSvgImage
import com.github.eylulnc.aura.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val colors = auraColors

    if (state.isSheetOpen && state.sheetEntry != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeSheet,
            containerColor = colors.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EntryEditSheet(
                state = state,
                colors = colors,
                onSelectMood = viewModel::selectMood,
                onNoteChange = viewModel::setNote,
                onUpdate = viewModel::updateEntry,
                onDelete = viewModel::deleteEntry,
                onCancel = viewModel::closeSheet
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(horizontal = Spacing.l, vertical = Spacing.l)
    ) {

        item {
            MonthHeader(
                month = state.selectedMonth,
                onPrev = viewModel::prevMonth,
                onNext = viewModel::nextMonth,
                colors = colors
            )
            Spacer(Modifier.height(Spacing.l))
            CalendarGrid(
                month = state.selectedMonth,
                entryByDay = state.entryByDay,
                colors = colors,
                onDayClick = viewModel::openSheet
            )
            Spacer(Modifier.height(Spacing.l))
            HorizontalDivider(color = colors.border)
            Spacer(Modifier.height(Spacing.l))
        }

        if (state.monthEntries.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xxl)
                ) {
                    Text(
                        text = stringResource(R.string.history_no_entries),
                        fontSize = FontSize.s,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            items(state.monthEntries, key = { it.id }) { entry ->
                EntryRow(entry = entry, colors = colors, onClick = { viewModel.openSheet(entry) })
                Spacer(Modifier.height(Spacing.s))
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    colors: AuraColors
) {
    val label = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    val canGoNext = month < YearMonth.now()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = colors.textPrimary
            )
        }
        Text(
            text = label,
            fontSize = FontSize.l,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (canGoNext) colors.textPrimary else colors.border
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    entryByDay: Map<Int, MoodEntry>,
    colors: AuraColors,
    onDayClick: (MoodEntry) -> Unit
) {
    val today = LocalDate.now()
    val isCurrentMonth = month.year == today.year && month.monthValue == today.monthValue
    val firstDayOffset = month.atDay(1).dayOfWeek.value - 1 // 0-based Monday offset
    val daysInMonth = month.lengthOfMonth()
    val totalCells = firstDayOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
        // Day-of-week headers Mon → Sun
        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { dow ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        fontSize = FontSize.xs,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.s))

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - firstDayOffset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day in 1..daysInMonth) {
                            val entry = entryByDay[day]
                            val isToday = isCurrentMonth && today.dayOfMonth == day
                            DayCell(
                                day = day,
                                entry = entry,
                                isToday = isToday,
                                colors = colors,
                                onClick = { if (entry != null) onDayClick(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    entry: MoodEntry?,
    isToday: Boolean,
    colors: AuraColors,
    onClick: () -> Unit
) {
    val moodColor = entry?.let { Color(getMoodFace(it.mood).color.toColorInt()) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .then(
                if (isToday) Modifier.border(
                    Spacing.borderWidth,
                    colors.accent,
                    RoundedCornerShape(Spacing.radiusCard)
                ) else Modifier
            )
            .clickable(enabled = entry != null, onClick = onClick)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = FontSize.xs,
                color = when {
                    isToday -> colors.accent
                    entry != null -> colors.textPrimary
                    else -> colors.textSecondary
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.height(2.dp))
            if (moodColor != null) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(moodColor)
                )
            } else {
                Spacer(Modifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun EntryRow(
    entry: MoodEntry,
    colors: AuraColors,
    onClick: () -> Unit
) {
    val face = getMoodFace(entry.mood)
    val moodColor = Color(face.color.toColorInt())
    val dateLabel = LocalDate.parse(entry.date)
        .format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(Spacing.m)
    ) {
        MoodSvgImage(
            moodFace = face,
            modifier = Modifier.size(Spacing.moodFaceSize)
        )
        Spacer(Modifier.width(Spacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = face.label,
                fontSize = FontSize.m,
                fontWeight = FontWeight.SemiBold,
                color = moodColor
            )
            Text(
                text = dateLabel,
                fontSize = FontSize.xs,
                color = colors.textSecondary
            )
            if (!entry.note.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.note,
                    fontSize = FontSize.s,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EntryEditSheet(
    state: HistoryUiState,
    colors: AuraColors,
    onSelectMood: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val entry = state.sheetEntry ?: return
    val dateLabel = LocalDate.parse(entry.date)
        .format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.l)
            .padding(bottom = Spacing.xxl)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = dateLabel,
                fontSize = FontSize.m,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.history_action_delete),
                    tint = colors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(Spacing.l))

        MoodPicker(
            selected = state.pendingMoodId,
            onSelect = onSelectMood,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(Spacing.l))

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

        Spacer(Modifier.height(Spacing.l))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(Spacing.radiusButton),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(stringResource(R.string.today_action_cancel), color = colors.textSecondary)
            }
            val confirmed = state.pendingMoodId != null
            Button(
                onClick = onUpdate,
                enabled = confirmed,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(Spacing.radiusButton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    disabledContainerColor = colors.surfaceSubtle
                )
            ) {
                Text(
                    text = stringResource(R.string.today_action_update),
                    color = if (confirmed) Color.White else colors.textSecondary
                )
            }
        }
    }
}
