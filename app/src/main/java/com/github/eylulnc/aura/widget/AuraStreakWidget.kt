package com.github.eylulnc.aura.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import com.github.eylulnc.aura.MainActivity

class AuraStreakWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }

    @Composable
    private fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val streak = prefs[KEY_STREAK] ?: 0

        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(WidgetColors.background)
                    .cornerRadius(WidgetDimens.cornerRadius)
                    .padding(WidgetDimens.padding)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Streak", style = WidgetTextStyle.title)
                    Spacer(GlanceModifier.height(6.dp))
                    if(streak != 0) {
                        Text(text = "🔥", style = WidgetTextStyle.fire)
                        Spacer(GlanceModifier.height(4.dp))
                    }
                    Text(text = streak.toString(), style = WidgetTextStyle.streakNumber)
                    Spacer(GlanceModifier.height(2.dp))
                    Text(text = if (streak == 1) "day" else "days", style = WidgetTextStyle.label)
                }
            }
        }
    }

    companion object {
        val KEY_STREAK = intPreferencesKey("widget_streak")
    }
}
