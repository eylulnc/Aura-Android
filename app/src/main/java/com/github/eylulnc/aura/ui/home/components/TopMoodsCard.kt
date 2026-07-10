package com.github.eylulnc.aura.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.constants.MoodFace
import com.github.eylulnc.aura.ui.components.MoodSvgImage
import com.github.eylulnc.aura.ui.theme.AuraColors
import com.github.eylulnc.aura.ui.theme.FontSize
import com.github.eylulnc.aura.ui.theme.Spacing

@Composable
fun TopMoodsCard(topMoods: List<Pair<MoodFace, Int>>, colors: AuraColors) {
    val maxCount = topMoods.maxOf { it.second }.coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .background(colors.surface)
            .padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Text(
            text = stringResource(R.string.today_top_moods_title),
            fontSize = FontSize.s,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
        topMoods.forEach { (face, count) ->
            val moodColor = Color(face.color.toColorInt())
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                MoodSvgImage(face, Modifier.size(Spacing.moodFaceSmallSize))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(face.label, fontSize = FontSize.s, color = colors.textPrimary)
                        Text("$count", fontSize = FontSize.s, color = colors.textSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(count.toFloat() / maxCount)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(moodColor)
                        )
                    }
                }
            }
        }
    }
}