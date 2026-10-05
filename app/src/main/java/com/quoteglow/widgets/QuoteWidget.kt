package com.quoteglow.widgets
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.dp

class QuoteWidget: GlanceAppWidget() {
 override suspend fun provideGlance(context: android.content.Context, id: GlanceId) = provideContent {
  Box(GlanceModifier.fillMaxSize().background(ColorProvider(Color(0xFF18203A))).padding(16.dp),contentAlignment=Alignment.Center) {
   Text("Small steps every day lead to big results.",style=TextStyle(color=ColorProvider(Color.White)))
  }
 }
}
class QuoteWidgetReceiver:GlanceAppWidgetReceiver(){ override val glanceAppWidget=QuoteWidget() }
