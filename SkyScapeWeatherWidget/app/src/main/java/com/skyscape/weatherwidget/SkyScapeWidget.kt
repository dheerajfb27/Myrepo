package com.skyscape.weatherwidget

import android.content.Context
import android.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SkyScapeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weather = runCatching { WeatherRepository.cached(context) }
            .getOrElse {
                WeatherSnapshot(-1, -1, -1, "Open app to refresh", "☁️")
            }

        provideContent {
            Row(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(Color.rgb(16, 20, 28)))
                    .cornerRadius(20.dp)
                    .padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        "SkyScape Weather",
                        style = TextStyle(
                            color = ColorProvider(Color.WHITE),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        weather.location,
                        style = TextStyle(color = ColorProvider(Color.rgb(165, 194, 215)), fontSize = 11.sp)
                    )
                    Text(
                        weather.description,
                        style = TextStyle(color = ColorProvider(Color.rgb(205, 218, 228)), fontSize = 12.sp)
                    )
                    if (weather.high >= 0 && weather.low >= 0) {
                        Text(
                            "H: ${weather.high}°  L: ${weather.low}°",
                            style = TextStyle(color = ColorProvider(Color.rgb(165, 194, 215)), fontSize = 11.sp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (weather.temperature >= 0) "${weather.temperature}°" else "--°",
                        style = TextStyle(color = ColorProvider(Color.WHITE), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        weather.symbol,
                        style = TextStyle(color = ColorProvider(Color.WHITE), fontSize = 18.sp)
                    )
                    Text(
                        "Open-Meteo",
                        style = TextStyle(color = ColorProvider(Color.rgb(116, 204, 242)), fontSize = 9.sp)
                    )
                }
            }
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
