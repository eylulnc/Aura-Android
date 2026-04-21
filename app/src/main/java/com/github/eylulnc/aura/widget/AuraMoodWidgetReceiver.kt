package com.github.eylulnc.aura.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.github.eylulnc.aura.constants.MOODS
import com.github.eylulnc.aura.repository.MoodDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AuraMoodWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = AuraMoodWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        requestUpdate(context)
    }

    companion object {
        fun requestUpdate(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getKoin().get<MoodDao>()
                val today = LocalDate.now()
                val entry = dao.getByDate(today.toString())
                val mood = entry?.let { e -> MOODS.find { it.id == e.mood } }
                val dateLabel = today.format(
                    DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
                )
                val glanceIds = GlanceAppWidgetManager(context)
                    .getGlanceIds(AuraMoodWidget::class.java)
                glanceIds.forEach { id ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                        prefs.toMutablePreferences().apply {
                            if (mood != null) this[AuraMoodWidget.KEY_MOOD_ID] = mood.id
                            else remove(AuraMoodWidget.KEY_MOOD_ID)
                            this[AuraMoodWidget.KEY_DATE_LABEL] = dateLabel
                        }
                    }
                    AuraMoodWidget().update(context, id)
                }
            }
        }
    }
}
