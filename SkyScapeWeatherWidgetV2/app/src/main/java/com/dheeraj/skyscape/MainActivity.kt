package com.dheeraj.skyscape

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.rgb(42, 31, 26))
        }
        root.addView(TextView(this).apply {
            text = "☾  SkyScape Weather"
            textSize = 28f
            setTextColor(Color.rgb(232, 216, 196))
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Coffee Glass • Live Weather • AQI • CO₂"
            textSize = 15f
            setTextColor(Color.rgb(212, 163, 115))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 18)
        })
        root.addView(TextView(this).apply {
            text = "Add the SkyScape Weather widget to your home screen:\n\n1. Touch and hold an empty area on your home screen.\n2. Tap Widgets.\n3. Find SkyScape Weather and drag the widget onto the screen.\n4. Tap the refresh icon to update weather and air-quality data."
            textSize = 16f
            setTextColor(Color.rgb(232, 216, 196))
            setLineSpacing(5f, 1f)
        })
        setContentView(root)
    }
}