package com.github.eylulnc.aura.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentSize
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.caverock.androidsvg.SVG
import com.github.eylulnc.aura.MainActivity
import com.github.eylulnc.aura.constants.MOODS
import com.github.eylulnc.aura.constants.MoodFace
import androidx.core.graphics.createBitmap

class AuraMoodWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(setOf(SMALL_SIZE, WIDE_SIZE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent(context) }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = currentState<Preferences>()
        val moodId = prefs[KEY_MOOD_ID]
        val dateLabel = prefs[KEY_DATE_LABEL] ?: ""
        val size = LocalSize.current

        val mood = moodId?.let { id -> MOODS.find { it.id == id } }
        val bitmap = mood?.let { renderMoodBitmap(context, it.svgRes, 128) }

        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1C1C1A)))
                    .cornerRadius(16.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (size.width >= WIDE_SIZE.width) {
                    WideContent(mood, bitmap, dateLabel)
                } else {
                    SmallContent(mood, bitmap, dateLabel)
                }
            }
        }
    }

    @Composable
    private fun SmallContent(mood: MoodFace?, bitmap: Bitmap?, dateLabel: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateLabel,
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFF89857E), night = Color(0xFF706C65)),
                    fontSize = 11.sp
                )
            )
            Spacer(GlanceModifier.height(6.dp))
            if (bitmap != null && mood != null) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = mood.label,
                    modifier = GlanceModifier.size(52.dp)
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = mood.label,
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFF1A1917), night = Color(0xFFEDECEA)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            } else {
                MoodPlaceholder()
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = "Log today",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFF89857E), night = Color(0xFF706C65)),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun WideContent(mood: MoodFace?, bitmap: Bitmap?, dateLabel: String) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            if (bitmap != null && mood != null) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = mood.label,
                    modifier = GlanceModifier.size(60.dp)
                )
                Spacer(GlanceModifier.width(12.dp))
                Column(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mood.label,
                        style = TextStyle(
                            color = ColorProvider(
                                day = Color(0xFF1A1917),
                                night = Color(0xFFEDECEA)
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = dateLabel,
                        style = TextStyle(
                            color = ColorProvider(
                                day = Color(0xFF89857E),
                                night = Color(0xFF706C65)
                            ),
                            fontSize = 11.sp
                        )
                    )
                }
            } else {
                MoodPlaceholder()
                Spacer(GlanceModifier.width(12.dp))
                Column(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateLabel,
                        style = TextStyle(
                            color = ColorProvider(
                                day = Color(0xFF89857E),
                                night = Color(0xFF706C65)
                            ),
                            fontSize = 11.sp
                        )
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    LogMoodButton()
                }
            }
        }
    }

    @Composable
    private fun MoodPlaceholder() {
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .background(ColorProvider(day = Color(0xFFF0EFEB), night = Color(0xFF252523)))
                .cornerRadius(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "·",
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFF89857E), night = Color(0xFF706C65)),
                    fontSize = 20.sp
                )
            )
        }
    }

    @Composable
    private fun LogMoodButton() {
        Box(
            modifier = GlanceModifier
                .wrapContentSize()
                .background(ColorProvider(day = Color(0xFF7B96E8), night = Color(0xFFA8BCF0)))
                .cornerRadius(20.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log mood",
                style = TextStyle(
                    color = ColorProvider(Color.White, Color(0xFF111110)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }

    companion object {
        val SMALL_SIZE = DpSize(110.dp, 110.dp)
        val WIDE_SIZE = DpSize(250.dp, 110.dp)
        val KEY_MOOD_ID = intPreferencesKey("widget_mood_id")
        val KEY_DATE_LABEL = stringPreferencesKey("widget_date_label")
    }
}

private fun renderMoodBitmap(
    context: android.content.Context,
    @RawRes svgRes: Int,
    sizePx: Int
): Bitmap {
    val svg = SVG.getFromResource(context, svgRes)
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    svg.documentWidth = sizePx.toFloat()
    svg.documentHeight = sizePx.toFloat()
    svg.renderToCanvas(canvas)
    return bitmap
}
