package com.quoteglow.widgets

import android.content.Context
import android.graphics.Color
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
import androidx.glance.unit.sp

object QuoteGlowStore {
    private const val FILE = "quoteglow"
    fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    fun quote(context: Context, id: Int) = prefs(context).getString("quote_$id", "Dream it. Build it. Live it.") ?: "Dream it. Build it. Live it."
    fun style(context: Context, id: Int) = prefs(context).getString("style_$id", "Glass") ?: "Glass"
    fun opacity(context: Context, id: Int) = prefs(context).getInt("opacity_$id", 90).coerceIn(10, 100)
    fun textOpacity(context: Context, id: Int) = prefs(context).getInt("text_opacity_$id", 100).coerceIn(10, 100)
    fun textSize(context: Context, id: Int) = prefs(context).getInt("text_size_$id", 18).coerceIn(10, 34)
    fun colorMode(context: Context, id: Int) = prefs(context).getString("color_mode_$id", "White") ?: "White"
    fun save(context: Context, id: Int, quote: String, style: String, opacity: Int, textOpacity: Int, textSize: Int, colorMode: String) {
        prefs(context).edit().putString("quote_$id", quote).putString("style_$id", style)
            .putInt("opacity_$id", opacity.coerceIn(10, 100)).putInt("text_opacity_$id", textOpacity.coerceIn(10, 100))
            .putInt("text_size_$id", textSize.coerceIn(10, 34)).putString("color_mode_$id", colorMode).apply()
    }
}

private fun bg(style: String, opacity: Int): Int {
    val alpha = (opacity * 255 / 100) shl 24
    val rgb = when (style) {
        "Neon" -> 0x002A003F; "Gradient" -> 0x001B4260; "AMOLED" -> 0x00000000; "Elegant" -> 0x002D2440
        "Handwritten" -> 0x003D3024; "Bold" -> 0x001B2432; "Retro" -> 0x00453525; "Transparent" -> 0x00111827
        "Minimal" -> 0x00212631; else -> 0x00182036
    }
    return alpha or rgb
}

private fun textColor(mode: String, opacity: Int): Int {
    val a = (opacity * 255 / 100) shl 24
    val rgb = when (mode) {
        "Sunset" -> 0xFFFF6B35; "Ocean" -> 0xFF35C9FF; "Purple" -> 0xFFB66DFF; "Mint" -> 0xFF56F2B3
        "Gold" -> 0xFFFFD166; "Pink" -> 0xFFFF5CAD; else -> Color.WHITE
    }
    return a or rgb
}

private fun secondaryColor(mode: String): Int = when (mode) {
    "Sunset" -> 0xFFFFD166; "Ocean" -> 0xFF8A7CFF; "Purple" -> 0xFFFF6FD8; "Mint" -> 0xFF6CCBFF
    "Gold" -> 0xFFFF8A3D; "Pink" -> 0xFF8A7CFF; else -> Color.LTGRAY
}

class QuoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val numericId = id.toString().filter(Char::isDigit).toIntOrNull() ?: 0
            val style = QuoteGlowStore.style(context, numericId)
            val opacity = QuoteGlowStore.opacity(context, numericId)
            val quote = QuoteGlowStore.quote(context, numericId)
            val textOpacity = QuoteGlowStore.textOpacity(context, numericId)
            val textSize = QuoteGlowStore.textSize(context, numericId)
            val mode = QuoteGlowStore.colorMode(context, numericId)
            val primary = textColor(mode, textOpacity)
            Box(GlanceModifier.fillMaxSize().background(ColorProvider(bg(style, opacity))).cornerRadius(18.dp).padding(12.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❝ $quote ❞", style = TextStyle(color = ColorProvider(primary), fontSize = textSize.sp))
                    if (mode != "White") Text("✦ ${mode.uppercase()} ✦", style = TextStyle(color = ColorProvider(secondaryColor(mode)), fontSize = 9.sp))
                    else if (style == "Elegant") Text("— QuoteGlow", style = TextStyle(color = ColorProvider(Color.LTGRAY), fontSize = 9.sp))
                }
            }
        }
    }
}

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = QuoteWidget() }
class QuoteWidget4x1Receiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = QuoteWidget() }
