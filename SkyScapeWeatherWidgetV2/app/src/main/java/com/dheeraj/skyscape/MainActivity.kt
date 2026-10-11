package com.dheeraj.skyscape

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

class MainActivity : Activity() {
    private val cityNames = arrayOf("Bengaluru", "Delhi", "Mumbai", "Chennai", "Hyderabad", "Kolkata", "Pune", "Ahmedabad", "Jaipur", "Lucknow", "Chandigarh", "Bhopal", "Indore", "Patna", "Bhubaneswar", "Kochi", "Thiruvananthapuram", "Surat", "Nagpur", "Varanasi")
    private val coordinates = arrayOf(
        doubleArrayOf(12.9716,77.5946), doubleArrayOf(28.6139,77.2090), doubleArrayOf(19.0760,72.8777),
        doubleArrayOf(13.0827,80.2707), doubleArrayOf(17.3850,78.4867), doubleArrayOf(22.5726,88.3639),
        doubleArrayOf(18.5204,73.8567), doubleArrayOf(23.0225,72.5714), doubleArrayOf(26.9124,75.7873),
        doubleArrayOf(26.8467,80.9462), doubleArrayOf(30.7333,76.7794), doubleArrayOf(23.2599,77.4126),
        doubleArrayOf(22.7196,75.8577), doubleArrayOf(25.5941,85.1376), doubleArrayOf(20.2961,85.8245),
        doubleArrayOf(9.9312,76.2673), doubleArrayOf(8.5241,76.9366), doubleArrayOf(21.1702,72.8311),
        doubleArrayOf(21.1458,79.0882), doubleArrayOf(25.3176,82.9739)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("weather_cache", MODE_PRIVATE)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.rgb(42, 31, 26))
        }
        root.addView(TextView(this).apply {
            text = "☾ SkyScape Weather"
            textSize = 28f
            setTextColor(Color.rgb(232, 216, 196))
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Coffee Glass • Weather • City AQI • CO₂"
            textSize = 15f
            setTextColor(Color.rgb(212, 163, 115))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 22)
        })
        root.addView(TextView(this).apply {
            text = "Choose Indian city"
            textSize = 16f
            setTextColor(Color.rgb(232, 216, 196))
        })
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cityNames)
        spinner.setSelection(cityNames.indexOf(prefs.getString("city", "Bengaluru")).coerceAtLeast(0))
        root.addView(spinner)
        val save = TextView(this).apply {
            text = "SAVE CITY & REFRESH WIDGET"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(42, 31, 26))
            setBackgroundColor(Color.rgb(212, 163, 115))
            setPadding(18, 16, 18, 16)
        }
        root.addView(save)
        save.setOnClickListener {
            val i = spinner.selectedItemPosition
            prefs.edit().putString("city", cityNames[i])
                .putFloat("latitude", coordinates[i][0].toFloat())
                .putFloat("longitude", coordinates[i][1].toFloat()).apply()
            sendBroadcast(Intent(this, SkyScapeWidgetProvider::class.java).setAction("com.dheeraj.skyscape.REFRESH"))
            save.text = "SAVED • WIDGET REFRESHING"
        }
        root.addView(TextView(this).apply {
            text = "AQI uses the US AQI scale (not India's CPCB scale). CO₂ is a modelled outdoor estimate. Add the widget from your home screen: long-press → Widgets → SkyScape Weather."
            textSize = 13f
            setTextColor(Color.rgb(232, 216, 196))
            setPadding(0, 24, 0, 0)
        })
        setContentView(root)
    }
}