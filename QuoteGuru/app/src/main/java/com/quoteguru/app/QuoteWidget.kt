package com.quoteguru.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.sp

class QuoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val settings = QuotePrefs.flow(context).collectAsState(initial = QuoteSettings()).value
            QuoteWidgetContent(settings)
        }
    }

    @Composable
    private fun QuoteWidgetContent(settings: QuoteSettings) {
        val size = LocalSize.current
        val width = size.width.value
        val height = size.height.value
        val font = (minOf(width, height) / 9f).coerceIn(12f, 28f).sp
        val authorFont = (font.value * .66f).coerceIn(10f, 19f).sp
        val radius: Dp = when (settings.shape) {
            1 -> 1000.dp
            2 -> 0.dp
            3 -> 10.dp
            else -> 24.dp
        }
        val base = if (settings.dark) ColorProvider(android.graphics.Color.rgb(12, 25, 38))
                   else ColorProvider(android.graphics.Color.rgb(45, 48, 52))
        Box(
            modifier = GlanceModifier.fillMaxSize().background(base).cornerRadius(radius).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(android.graphics.Color.argb(70, 44, 83, 100)))
                    .cornerRadius(radius),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "“${settings.quote}”",
                        style = TextStyle(color = ColorProvider(android.graphics.Color.WHITE), fontSize = font, fontWeight = FontWeight.Bold)
                    )
                    Spacer(GlanceModifier.size(8.dp))
                    Text(
                        text = "— ${settings.author}",
                        style = TextStyle(color = ColorProvider(android.graphics.Color.WHITE), fontSize = authorFont)
                    )
                }
            }
        }
    }
}
