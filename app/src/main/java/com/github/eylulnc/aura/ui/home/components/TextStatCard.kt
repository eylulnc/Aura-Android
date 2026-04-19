package com.github.eylulnc.aura.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.eylulnc.aura.ui.theme.AuraColors
import com.github.eylulnc.aura.ui.theme.FontSize
import com.github.eylulnc.aura.ui.theme.Spacing

@Composable
fun TextStatCard(
    label: String,
    value: String,
    unit: String?,
    valueColor: Color,
    backgroundColor: Color,
    colors: AuraColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.radiusCard))
            .background(backgroundColor)
            .padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(text = label, fontSize = FontSize.xs, color = colors.textSecondary)
        Spacer(Modifier.height(Spacing.s))
        Text(text = value, fontSize = FontSize.xl, fontWeight = FontWeight.Bold, color = valueColor)
        if (unit != null) {
            Text(text = unit, fontSize = FontSize.xs, color = colors.textSecondary)
        } else {
            Spacer(Modifier.height(FontSize.xs.value.dp))
        }
    }
}