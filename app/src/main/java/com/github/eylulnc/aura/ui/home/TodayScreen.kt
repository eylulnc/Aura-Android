package com.github.eylulnc.aura.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.toColorInt
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.constants.getMoodFace
import com.github.eylulnc.aura.model.MoodEntry
import com.github.eylulnc.aura.ui.components.MoodSvgImage
import com.github.eylulnc.aura.ui.events.MoodLoggerEvents
import com.github.eylulnc.aura.ui.home.components.LogMoodSheet
import com.github.eylulnc.aura.ui.home.components.MoodStatCard
import com.github.eylulnc.aura.ui.home.components.MoodTrendCard
import com.github.eylulnc.aura.ui.home.components.TextStatCard
import com.github.eylulnc.aura.ui.home.components.TopMoodsCard
import com.github.eylulnc.aura.ui.theme.AuraColors
import com.github.eylulnc.aura.ui.theme.FontSize
import com.github.eylulnc.aura.ui.theme.Spacing
import com.github.eylulnc.aura.ui.theme.auraColors
import com.github.eylulnc.aura.viewmodel.TodayViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(viewModel: TodayViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val colors = auraColors
    var showSheet by remember { mutableStateOf(false) }

    val openMoodRequest by MoodLoggerEvents.openRequest.collectAsState()
    var lastHandledOpenRequest by remember { mutableStateOf(0L) }
    LaunchedEffect(openMoodRequest, state.isLoading) {
        if (openMoodRequest != 0L &&
            openMoodRequest != lastHandledOpenRequest &&
            !state.isLoading
        ) {
            lastHandledOpenRequest = openMoodRequest
            showSheet = true
        }
    }

    if (state.isLoading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.background)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.l)
            .padding(top = Spacing.l, bottom = Spacing.xl)
    ) {
        Text(
            text = greeting(),
            fontSize = FontSize.xl,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        Text(
            text = formattedDate(),
            fontSize = FontSize.s,
            color = colors.textSecondary
        )

        Spacer(Modifier.height(Spacing.l))

        TodayCard(entry = state.todayEntry, colors = colors, onTap = { showSheet = true })

        Spacer(Modifier.height(Spacing.l))

        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            TextStatCard(
                label = stringResource(R.string.today_stat_streak),
                value = state.streak.toString(),
                unit = if (state.streak == 1) stringResource(R.string.today_stat_streak_unit_single) else stringResource(R.string.today_stat_streak_unit),
                valueColor = if (state.streak > 0) colors.accent else colors.textPrimary,
                backgroundColor = if (state.streak > 0) colors.accent.copy(alpha = 0.08f) else colors.surface,
                colors = colors,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            TextStatCard(
                label = stringResource(R.string.today_stat_days_logged),
                value = if (state.daysThisMonth > 0) state.daysThisMonth.toString() else "—",
                unit = if (state.daysThisMonth > 0) stringResource(R.string.today_stat_days_unit) else null,
                valueColor = colors.textPrimary,
                backgroundColor = colors.surface,
                colors = colors,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(Modifier.height(Spacing.m))

        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            if (state.topMoodsThisMonth.isNotEmpty()) {
                MoodStatCard(
                    label = stringResource(R.string.today_stat_top_mood),
                    face = state.topMoodsThisMonth.first().first,
                    colors = colors,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                TextStatCard(
                    label = stringResource(R.string.today_stat_top_mood),
                    value = "—",
                    unit = null,
                    valueColor = colors.textSecondary,
                    backgroundColor = colors.surface,
                    colors = colors,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            TextStatCard(
                label = stringResource(R.string.today_stat_positive),
                value = state.positivePercent?.let { "$it%" } ?: "—",
                unit = if (state.positivePercent != null) stringResource(R.string.today_stat_positive_unit) else null,
                valueColor = if (state.positivePercent != null) colors.accent else colors.textSecondary,
                backgroundColor = colors.surface,
                colors = colors,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(Modifier.height(Spacing.l))

        MoodTrendCard(weekEntries = state.weekEntries, colors = colors)

        if (state.topMoodsThisMonth.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.l))
            TopMoodsCard(topMoods = state.topMoodsThisMonth, colors = colors)
        }
    }

    if (showSheet) {
        LogMoodSheet(
            initialMoodId = state.todayEntry?.mood,
            initialNote = state.todayEntry?.note ?: "",
            isEditing = state.todayEntry != null,
            onConfirm = { moodId, note ->
                viewModel.confirmMood(moodId, note)
                showSheet = false
            },
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
private fun TodayCard(entry: MoodEntry?, colors: AuraColors, onTap: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .background(colors.surface)
            .clickable(onClick = onTap)
            .padding(Spacing.l),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (entry == null) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.today_prompt_empty),
                    fontSize = FontSize.m,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(
                    text = stringResource(R.string.today_tap_to_log),
                    fontSize = FontSize.s,
                    color = colors.textSecondary
                )
            }
            Icon(Icons.Default.Add, contentDescription = null, tint = colors.accent)
        } else {
            val face = getMoodFace(entry.mood)
            val moodColor = Color(face.color.toColorInt())
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                MoodSvgImage(face, Modifier.size(Spacing.moodFaceMediumSize))
                Spacer(Modifier.width(Spacing.m))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        face.label,
                        fontSize = FontSize.m,
                        fontWeight = FontWeight.SemiBold,
                        color = moodColor
                    )
                    Text(face.sub, fontSize = FontSize.s, color = colors.textSecondary)
                    if (!entry.note.isNullOrBlank()) {
                        Text(
                            text = entry.note,
                            fontSize = FontSize.xs,
                            color = colors.textSecondary
                        )
                    }
                }
            }
            Icon(
                Icons.Default.Edit,
                contentDescription = stringResource(R.string.today_action_edit),
                tint = colors.accent
            )
        }
    }
}

private fun greeting(): String = when {
    LocalTime.now().hour < 12 -> "Good morning"
    LocalTime.now().hour < 17 -> "Good afternoon"
    else -> "Good evening"
}

private fun formattedDate(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
