package com.github.eylulnc.aura.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import com.caverock.androidsvg.SVG
import com.github.eylulnc.aura.MainActivity
import com.github.eylulnc.aura.constants.MOODS
import com.github.eylulnc.aura.constants.MoodFace
import androidx.core.graphics.createBitmap

class AuraMoodWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent(context) }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = currentState<Preferences>()
        val moodId = prefs[KEY_MOOD_ID]
        val dateLabel = prefs[KEY_DATE_LABEL] ?: ""
        val mood = moodId?.let { id -> MOODS.find { it.id == id } }
        val bitmap = mood?.let { renderMoodBitmap(context, it.svgRes, 192) }

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
                    Text(text = "Today's Mood", style = WidgetTextStyle.title)
                    Spacer(GlanceModifier.height(6.dp))
                    if (bitmap != null && mood != null) {
                        LoggedContent(mood, bitmap, dateLabel)
                    } else {
                        EmptyContent(dateLabel)
                    }
                }
            }
        }
    }

    @Composable
    private fun LoggedContent(mood: MoodFace, bitmap: Bitmap, dateLabel: String) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = mood.label,
            modifier = GlanceModifier.size(WidgetDimens.moodFaceSize)
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(text = mood.label, style = WidgetTextStyle.moodName)
        Spacer(GlanceModifier.height(4.dp))
        Text(text = dateLabel, style = WidgetTextStyle.label)
    }

    @Composable
    private fun EmptyContent(dateLabel: String) {
        Box(
            modifier = GlanceModifier
                .size(WidgetDimens.placeholderSize)
                .background(WidgetColors.surfaceSubtle)
                .cornerRadius(WidgetDimens.placeholderRadius),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", style = WidgetTextStyle.placeholder)
        }
        Spacer(GlanceModifier.height(8.dp))
        Text(text = "Log your mood", style = WidgetTextStyle.moodName)
        Spacer(GlanceModifier.height(4.dp))
        Text(text = dateLabel, style = WidgetTextStyle.label)
    }

    companion object {
        val KEY_MOOD_ID    = intPreferencesKey("widget_mood_id")
        val KEY_DATE_LABEL = stringPreferencesKey("widget_date_label")
    }
}

private fun renderMoodBitmap(context: Context, @RawRes svgRes: Int, sizePx: Int): Bitmap {
    val svg = SVG.getFromResource(context, svgRes)
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    svg.documentWidth = sizePx.toFloat()
    svg.documentHeight = sizePx.toFloat()
    svg.renderToCanvas(canvas)
    return bitmap
}
