package com.github.eylulnc.aura.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    if (state.isSheetOpen && state.sheetEntry != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeSheet,
            containerColor = colors.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            if (state.isEditMode) {
                EntryEditSheet(
                    state = state,
                    colors = colors,
                    onSelectMood = viewModel::selectMood,
                    onNoteChange = viewModel::setNote,
                    onUpdate = viewModel::updateEntry,
                    onDelete = viewModel::deleteEntry,
                    onCancel = viewModel::closeSheet
                )
            } else {
                EntryViewSheet(
                    entry = state.sheetEntry!!,
                    colors = colors
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentPadding = PaddingValues(horizontal = Spacing.l, vertical = Spacing.l)
        ) {
            item {
                MonthHeader(
                    month = state.selectedMonth,
                    earliestMonth = state.firstEntryMonth,
                    onPrev = viewModel::prevMonth,
                    onNext = viewModel::nextMonth,
                    onGoToToday = viewModel::goToToday,
                    colors = colors
                )
                Spacer(Modifier.height(Spacing.l))
                CalendarGrid(
                    month = state.selectedMonth,
                    entryByDay = state.entryByDay,
                    selectedDay = state.selectedDay,
                    colors = colors,
                    onDayClick = viewModel::selectDay
                )
                Spacer(Modifier.height(Spacing.l))
                HorizontalDivider(color = colors.border)
                Spacer(Modifier.height(Spacing.s))
                SortRow(sortOrder = state.sortOrder, onToggle = viewModel::toggleSort, colors = colors)
                Spacer(Modifier.height(Spacing.s))
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
                val todayStr = LocalDate.now().toString()
                items(state.monthEntries, key = { it.id }) { entry ->
                    EntryRow(
                        entry = entry,
                        colors = colors,
                        onEdit = if (entry.date == todayStr) ({ viewModel.openSheet(entry) }) else null
                    )
                    Spacer(Modifier.height(Spacing.s))
                }
            }
        }

        AnimatedVisibility(
            visible = showFab,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.l)
        ) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = colors.surface,
                contentColor = colors.textPrimary
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SortRow(
    sortOrder: SortOrder,
    onToggle: () -> Unit,
    colors: AuraColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        FilterChip(
            selected = true,
            onClick = onToggle,
            label = {
                Text(
                    text = if (sortOrder == SortOrder.Newest) "Newest first" else "Oldest first",
                    fontSize = FontSize.xs
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.surface,
                selectedLabelColor = colors.textPrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = true,
                borderColor = colors.border,
                selectedBorderColor = colors.border
            )
        )
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    earliestMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onGoToToday: () -> Unit,
    colors: AuraColors
) {
    val label = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    val canGoPrev = month > earliestMonth
    val canGoNext = month < YearMonth.now()
    val isCurrentMonth = month == YearMonth.now()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onPrev, enabled = canGoPrev) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = if (canGoPrev) colors.textPrimary else colors.border
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = FontSize.l,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            if (!isCurrentMonth) {
                Text(
                    text = "Back to today",
                    fontSize = FontSize.xs,
                    color = colors.accent,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onGoToToday)
                )
            }
        }
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
    selectedDay: Int?,
    colors: AuraColors,
    onDayClick: (Int) -> Unit
) {
    val today = LocalDate.now()
    val isCurrentMonth = month.year == today.year && month.monthValue == today.monthValue
    val firstDayOffset = month.atDay(1).dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()
    val totalCells = firstDayOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
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
                            val isSelected = selectedDay == day
                            DayCell(
                                day = day,
                                entry = entry,
                                isToday = isToday,
                                isSelected = isSelected,
                                colors = colors,
                                onClick = { if (entry != null) onDayClick(day) }
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
    isSelected: Boolean,
    colors: AuraColors,
    onClick: () -> Unit
) {
    val face = entry?.let { getMoodFace(it.mood) }
    val moodColor = face?.let { Color(it.color.toColorInt()) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
            .clickable(enabled = entry != null, onClick = onClick)
    ) {
        val borderColor = when {
            isSelected -> colors.accent
            isToday -> moodColor ?: colors.accent
            else -> colors.border
        }
        val borderWidth = if (isToday || isSelected) 2.dp else 1.dp

        if (face != null && moodColor != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(moodColor.copy(alpha = 0.18f))
                    .border(borderWidth, borderColor, CircleShape)
            ) {
                MoodSvgImage(
                    moodFace = face,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(borderWidth, borderColor, CircleShape)
            ) {
                Text(
                    text = day.toString(),
                    fontSize = FontSize.xs,
                    color = if (isToday) colors.accent else colors.textSecondary,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun EntryRow(
    entry: MoodEntry,
    colors: AuraColors,
    onEdit: (() -> Unit)?
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
                    color = colors.textSecondary
                )
            }
        }
        if (onEdit != null) {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = colors.accent
                )
            }
        }
    }
}

@Composable
private fun EntryViewSheet(
    entry: MoodEntry,
    colors: AuraColors
) {
    val face = getMoodFace(entry.mood)
    val moodColor = Color(face.color.toColorInt())
    val dateLabel = LocalDate.parse(entry.date)
        .format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.l)
            .padding(bottom = Spacing.xxl)
    ) {
        MoodSvgImage(
            moodFace = face,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(Spacing.m))
        Text(
            text = face.label,
            fontSize = FontSize.l,
            fontWeight = FontWeight.SemiBold,
            color = moodColor
        )
        Spacer(Modifier.height(Spacing.s))
        Text(
            text = dateLabel,
            fontSize = FontSize.s,
            color = colors.textSecondary
        )
        if (!entry.note.isNullOrBlank()) {
            Spacer(Modifier.height(Spacing.m))
            Text(
                text = entry.note,
                fontSize = FontSize.s,
                color = colors.textPrimary
            )
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
            onValueChange = { if (it.length <= 150) onNoteChange(it) },
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
            maxLines = 4,
            supportingText = {
                Text(
                    text = "${state.note.length} / 150",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = if (state.note.length >= 150) MaterialTheme.colorScheme.error
                            else colors.textSecondary,
                    fontSize = FontSize.xs
                )
            }
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
