package com.github.eylulnc.aura.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.github.eylulnc.aura.model.MoodEntry
import com.github.eylulnc.aura.repository.MoodDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate

class AuraStreakWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = AuraStreakWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        requestUpdate(context)
    }

    companion object {
        fun requestUpdate(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getKoin().get<MoodDao>()
                val entries = dao.getAll()
                val streak = computeStreak(entries)
                val glanceIds = GlanceAppWidgetManager(context)
                    .getGlanceIds(AuraStreakWidget::class.java)
                glanceIds.forEach { id ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[AuraStreakWidget.KEY_STREAK] = streak
                        }
                    }
                    AuraStreakWidget().update(context, id)
                }
            }
        }

        private fun computeStreak(entries: List<MoodEntry>): Int {
            val dateSet = entries.map { it.date }.toSet()
            var date = LocalDate.now()
            if (!dateSet.contains(date.toString())) date = date.minusDays(1)
            var count = 0
            while (dateSet.contains(date.toString())) {
                count++
                date = date.minusDays(1)
            }
            return count
        }
    }
}
