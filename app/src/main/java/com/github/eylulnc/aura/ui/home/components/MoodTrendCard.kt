package com.github.eylulnc.aura.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.constants.MOOD_VALENCE
import com.github.eylulnc.aura.constants.getMoodFace
import com.github.eylulnc.aura.model.MoodEntry
import com.github.eylulnc.aura.ui.theme.AuraColors
import com.github.eylulnc.aura.ui.theme.FontSize
import com.github.eylulnc.aura.ui.theme.Spacing
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MoodTrendCard(weekEntries: List<MoodEntry?>, colors: AuraColors) {
    val today = LocalDate.now()
    val dayLabels = (6 downTo 0).map { daysAgo ->
        today.minusDays(daysAgo.toLong()).dayOfWeek.getDisplayName(
            TextStyle.SHORT,
            Locale.getDefault()
        )
    }
    val dotColors = weekEntries.map { entry ->
        entry?.let { Color(getMoodFace(it.mood).color.toColorInt()) }
    }
    val accentColor = colors.accent
    val surfaceColor = colors.surface
    val borderColor = colors.border
    val posZoneColor = Color(0xFF1D9E75)
    val negZoneColor = Color(0xFF378ADD)

    fun normalizedValence(moodId: Int): Float {
        val v = MOOD_VALENCE[moodId] ?: 0f
        return (v + 2f) / 4f  // 0..1
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .background(surfaceColor)
            .padding(Spacing.l)
    ) {
        Text(
            text = stringResource(R.string.today_trend_title),
            fontSize = FontSize.s,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(Spacing.m))

        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)) {
            val w = size.width
            val h = size.height
            val padH = 8.dp.toPx()
            val padV = 8.dp.toPx()
            val chartW = w - padH * 2
            val chartH = h - padV * 2
            val bottomY = padV + chartH

            fun xFor(i: Int) = padH + (i / 6f) * chartW
            fun yFor(moodId: Int) = padV + chartH - normalizedValence(moodId) * chartH

            // Zone bands
            drawRect(
                color = posZoneColor.copy(alpha = 0.07f),
                topLeft = Offset(padH, padV),
                size = androidx.compose.ui.geometry.Size(chartW, chartH * 0.38f)
            )
            drawRect(
                color = negZoneColor.copy(alpha = 0.07f),
                topLeft = Offset(padH, padV + chartH * 0.62f),
                size = androidx.compose.ui.geometry.Size(chartW, chartH * 0.38f)
            )

            // Build continuous segments
            data class Seg(val indices: List<Int>)

            val segments = mutableListOf<Seg>()
            var current = mutableListOf<Int>()
            weekEntries.forEachIndexed { i, entry ->
                if (entry != null) current.add(i)
                else {
                    if (current.isNotEmpty()) {
                        segments.add(Seg(current.toList())); current = mutableListOf()
                    }
                }
            }
            if (current.isNotEmpty()) segments.add(Seg(current))

            // Area gradient fill + line per segment
            segments.forEach { seg ->
                val pts = seg.indices.map { i -> Offset(xFor(i), yFor(weekEntries[i]!!.mood)) }
                if (pts.isEmpty()) return@forEach

                // Area path
                val areaPath = Path()
                areaPath.moveTo(pts.first().x, pts.first().y)
                pts.drop(1).forEach { areaPath.lineTo(it.x, it.y) }
                areaPath.lineTo(pts.last().x, bottomY)
                areaPath.lineTo(pts.first().x, bottomY)
                areaPath.close()
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            accentColor.copy(alpha = 0.02f)
                        ),
                        startY = padV,
                        endY = bottomY
                    )
                )

                // Line
                val linePath = Path()
                linePath.moveTo(pts.first().x, pts.first().y)
                pts.drop(1).forEach { linePath.lineTo(it.x, it.y) }
                drawPath(
                    linePath,
                    color = accentColor,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Dots (mood color fill + surface stroke)
            weekEntries.forEachIndexed { i, entry ->
                if (entry != null) {
                    val center = Offset(xFor(i), yFor(entry.mood))
                    val r = 5.dp.toPx()
                    val sw = 2.5.dp.toPx()
                    drawCircle(color = dotColors[i] ?: accentColor, radius = r, center = center)
                    drawCircle(
                        color = surfaceColor,
                        radius = r,
                        center = center,
                        style = Stroke(width = sw)
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.xs))
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = FontSize.xs,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
