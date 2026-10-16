package com.quoteglow.widgets

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

object QuoteWidgetPrefs {
    private const val PREFS = "quoteglow_widget_prefs"
    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun save(context: Context, id: Int, quote: String, style: String, opacity: Int) = prefs(context).edit()
        .putString("quote_$id", quote).putString("style_$id", style)
        .putInt("opacity_$id", opacity.coerceIn(10, 100)).apply()
    fun saveDefault(context: Context, style: String, opacity: Int) = prefs(context).edit()
        .putString("default_style", style).putInt("default_opacity", opacity.coerceIn(10, 100)).apply()
    fun quote(context: Context, id: Int) = prefs(context).getString("quote_$id", null)
        ?: "Small steps every day lead to big results."
    fun style(context: Context, id: Int) = prefs(context).getString("style_$id", null)
        ?: prefs(context).getString("default_style", "Glass") ?: "Glass"
    fun opacity(context: Context, id: Int) = prefs(context).getInt("opacity_$id", prefs(context).getInt("default_opacity", 90)).coerceIn(10, 100)
}

private fun argb(opacity: Int, rgb: Long): Color {
    val a = (opacity.coerceIn(0, 100) * 255 / 100).toLong()
    val value = (a shl 24) or (rgb and 0x00FFFFFF)
    return Color(value.toULong().toLong())
}

private fun widgetBackground(style: String, opacity: Int): Color = when (style) {
    "Neon" -> argb(opacity, 0x00240038)
    "Gradient" -> argb(opacity, 0x001B4260)
    "AMOLED" -> argb(opacity, 0x00000000)
    "Elegant" -> argb(opacity, 0x00231E33)
    "Handwritten" -> argb(opacity, 0x003B3028)
    "Bold" -> argb(opacity, 0x001D2430)
    "Retro" -> argb(opacity, 0x00433223)
    "Transparent" -> argb(opacity / 2, 0x00111827)
    "Minimal" -> argb(opacity, 0x00212631)
    else -> argb(opacity, 0x00182036)
}

class QuoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val appWidgetId = id.toString().filter { it.isDigit() }.toIntOrNull() ?: 0
            val quote = QuoteWidgetPrefs.quote(context, appWidgetId)
            val style = QuoteWidgetPrefs.style(context, appWidgetId)
            val opacity = QuoteWidgetPrefs.opacity(context, appWidgetId)
            Box(
                GlanceModifier.fillMaxSize().background(ColorProvider(widgetBackground(style, opacity))).cornerRadius(18.dp).padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val textColor = if (style == "Handwritten") Color(0xFFFFE7C2) else Color.White
                    Text("❝  $quote  ❞", style = TextStyle(color = ColorProvider(textColor)))
                    if (style == "Neon") Text("✦ QUOTE GLOW ✦", style = TextStyle(color = ColorProvider(Color(0xFFFF4FD8))))
                    if (style == "Elegant") Text("— QuoteGlow", style = TextStyle(color = ColorProvider(Color(0xFFD6C7FF))))
                }
            }
        }
    }
}

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = QuoteWidget() }
class QuoteWidget4x1Receiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = QuoteWidget() }
