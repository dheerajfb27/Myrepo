package com.quoteglow.widgets

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

class QuoteWidget : GlanceAppWidget() {
 override suspend fun provideGlance(context:Context,id:GlanceId)=provideContent {
  val p=context.getSharedPreferences("quoteglow_prefs",Context.MODE_PRIVATE)
  val text=p.getString("selected_quote","Small steps every day lead to big results.")?:"Small steps every day lead to big results."
  val style=p.getString("style","Glass")?:"Glass"
  val bg=when(style){"Neon"->Color(0xFF220033);"Gradient"->Color(0xFF174C63);"Minimal"->Color(0xFF20242D);else->Color(0xFF18203A)}
  Box(GlanceModifier.fillMaxSize().background(ColorProvider(bg)).padding(16.dp),contentAlignment=Alignment.Center){
   Text(text,style=TextStyle(color=ColorProvider(Color.White)))
  }
 }
}
class QuoteWidgetReceiver:GlanceAppWidgetReceiver(){override val glanceAppWidget=QuoteWidget()}
