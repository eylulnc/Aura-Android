package com.github.eylulnc.aura.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.toColorInt
import com.github.eylulnc.aura.constants.MoodFace
import com.github.eylulnc.aura.ui.components.MoodSvgImage
import com.github.eylulnc.aura.ui.theme.AuraColors
import com.github.eylulnc.aura.ui.theme.FontSize
import com.github.eylulnc.aura.ui.theme.Spacing

@Composable
fun MoodStatCard(
    label: String,
    face: MoodFace,
    colors: AuraColors,
    modifier: Modifier = Modifier
) {
    val moodColor = Color(face.color.toColorInt())
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .background(colors.surface)
            .padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(text = label, fontSize = FontSize.xs, color = colors.textSecondary)
        Spacer(Modifier.height(Spacing.s))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoodSvgImage(face, Modifier.size(Spacing.moodFaceSmallSize))
            Spacer(Modifier.width(Spacing.xs))
            Text(text = face.label, fontSize = FontSize.s, fontWeight = FontWeight.SemiBold, color = moodColor)
        }
    }
}