package com.skyscape.weatherwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import androidx.glance.color.ColorProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment as GlanceAlignment
import androidx.glance.text.FontWeight
import android.graphics.Color

class SkyScapeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }
}

@Composable
private fun WidgetContent() {
    Row(
        modifier = GlanceModifier.fillMaxSize().background(ColorProvider(Color.rgb(14, 19, 26))).cornerRadius(24.dp).padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text("🌳  🐦  ☁️", style = TextStyle(color = ColorProvider(Color.rgb(228, 242, 249)), fontSize = 22.sp))
            Spacer(GlanceModifier.height(5.dp))
            Text("SkyScape", style = TextStyle(color = ColorProvider(Color.White), fontSize = 15.sp, fontWeight = FontWeight.Bold))
            Text("Tap to open weather", style = TextStyle(color = ColorProvider(Color.rgb(150, 192, 214)), fontSize = 11.sp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("☀️  --°", style = TextStyle(color = ColorProvider(Color.White), fontSize = 22.sp))
            Text("Weather setup", style = TextStyle(color = ColorProvider(Color.rgb(182, 199, 210)), fontSize = 10.sp))
        }
    }
}

class SkyScapeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SkyScapeWidget()
}
