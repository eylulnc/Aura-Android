package com.github.eylulnc.aura.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.toColorInt
import com.github.eylulnc.aura.constants.MOODS
import com.github.eylulnc.aura.ui.theme.*

@Composable
fun MoodPicker(
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = auraColors

    Column(modifier = modifier.padding(vertical = Spacing.s)) {
        MOODS.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { mood ->
                    val isSelected = selected == mood.id
                    val moodColor = Color(mood.color.toColorInt())

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(Spacing.s / 2)
                            .clickable { onSelect(mood.id) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(Spacing.moodFaceWrapperSize)
                                .then(
                                    if (isSelected) Modifier.border(
                                        Spacing.selectionBorderWidth,
                                        moodColor,
                                        CircleShape
                                    )
                                    else Modifier
                                )
                        ) {
                            MoodSvgImage(
                                moodFace = mood,
                                modifier = Modifier.size(Spacing.moodFaceSize)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        Text(
                            text = mood.label,
                            fontSize = FontSize.xs,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) moodColor else colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
