package com.quoteglow.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 32, 28, 32)
        }
        root.addView(TextView(this).apply {
            text = "QuoteGlow Widgets"
            textSize = 28f
            setPadding(0, 0, 0, 8)
        })
        root.addView(TextView(this).apply {
            text = "Wallpaper features are removed. Add a 2×1 or 4×1 quote widget from your home-screen widget picker."
            textSize = 16f
            setPadding(0, 0, 0, 24)
        })
        root.addView(TextView(this).apply { text = "Available styles: Minimal • Glass • Neon • Gradient • AMOLED • Elegant • Handwritten • Bold • Retro • Transparent" })
        root.addView(TextView(this).apply { text = "\nOpacity is adjustable per widget from 10% to 100%." })
        val info = TextView(this).apply { text = "\n2×1 = compact quote\n4×1 = full-width quote" }
        root.addView(info)
        val add = Button(this).apply {
            text = "Open Widget Picker"
            setOnClickListener { openWidgetPicker() }
        }
        root.addView(add)
        setContentView(root)
    }

    private fun openWidgetPicker() {
        val manager = AppWidgetManager.getInstance(this)
        val id = manager.allocateAppWidgetId()
        startActivityForResult(Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id), 100)
    }
}
