package com.quoteglow.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.ArrayAdapter

class QuoteWidgetConfigActivity : Activity() {
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        val p = QuoteGlowStore.prefs(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 28, 28, 28) }
        val quote = EditText(this).apply { setText(p.getString("quote_$widgetId", "Dream it. Build it. Live it.")); hint = "Quote" }
        root.addView(quote)
        val styles = arrayOf("Minimal", "Glass", "Neon", "Gradient", "AMOLED", "Elegant", "Handwritten", "Bold", "Retro", "Transparent")
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@QuoteWidgetConfigActivity, android.R.layout.simple_spinner_dropdown_item, styles)
            setSelection(styles.indexOf(p.getString("style_$widgetId", "Glass")).coerceAtLeast(0))
        }
        root.addView(spinner)
        val opacity = SeekBar(this).apply { max = 90; progress = p.getInt("opacity_$widgetId", 90) - 10 }
        root.addView(opacity)
        val save = Button(this).apply {
            text = "Save Widget"
            setOnClickListener {
                QuoteGlowStore.save(this@QuoteWidgetConfigActivity, widgetId, quote.text.toString().ifBlank { "Dream it. Build it. Live it." }, styles[spinner.selectedItemPosition], opacity.progress + 10)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        root.addView(save)
        setContentView(root)
    }
}
