package com.skyscape.weatherwidget

import android.content.Context
import android.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimal compatibility-first widget renderer.
 * Keep data fetching, clickable actions, nested layouts and custom corners out of
 * the first render so launcher compatibility can be verified independently.
 */
class SkyScapeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text(
                text = "SkyScape Weather  •  Open app for forecast",
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color.rgb(16, 20, 28)))
                    .padding(16.dp),
                style = TextStyle(
                    color = ColorProvider(Color.WHITE),
                    fontSize = 14.sp
                )
            )
        }
    }
}

class SkyScapeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SkyScapeWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        runCatching { WeatherRefreshWorker.schedule(context) }
    }
}
