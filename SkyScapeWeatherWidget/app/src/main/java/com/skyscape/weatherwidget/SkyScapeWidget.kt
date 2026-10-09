package com.skyscape.weatherwidget

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
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
        val weather = WeatherRepository.refresh(context)
        provideContent { WidgetContent(weather) }
    }
}

@Composable
private fun WidgetContent(weather: WeatherSnapshot) {
    Row(
        modifier = GlanceModifier.fillMaxSize()
            .background(ColorProvider(Color.rgb(10, 15, 22)))
            .cornerRadius(24.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text("🌳  🐦  ☁️", style = TextStyle(color = ColorProvider(Color.rgb(228, 242, 249)), fontSize = 22.sp))
            Spacer(GlanceModifier.height(5.dp))
            Text("SkyScape · ${weather.location}", style = TextStyle(color = ColorProvider(Color.WHITE), fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Text(weather.description, style = TextStyle(color = ColorProvider(Color.rgb(150, 192, 214)), fontSize = 11.sp))
            if (weather.high >= 0 && weather.low >= 0) {
                Text("↑ ${weather.high}°   ↓ ${weather.low}°", style = TextStyle(color = ColorProvider(Color.rgb(184, 202, 214)), fontSize = 11.sp))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val temp = if (weather.temperature >= 0) "${weather.temperature}°" else "--°"
            Text("${weather.symbol}  $temp", style = TextStyle(color = ColorProvider(Color.White), fontSize = 22.sp))
            Text("Open-Meteo", style = TextStyle(color = ColorProvider(Color.rgb(116, 204, 242)), fontSize = 10.sp))
        }
    }
}

class SkyScapeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SkyScapeWidget()
}
