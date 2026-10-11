package com.dheeraj.skyscape

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
    private val skinNames = arrayOf("Coffee Glass", "Glass Green")
    private val skinValues = arrayOf("coffee", "glass_green")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("weather_cache", MODE_PRIVATE)
        val green = prefs.getString("skin", "coffee") == "glass_green"
        val bg = if (green) Color.rgb(7, 29, 19) else Color.rgb(42, 31, 26)
        val accent = if (green) Color.rgb(154, 255, 87) else Color.rgb(212, 163, 115)
        val text = if (green) Color.rgb(240, 255, 232) else Color.rgb(232, 216, 196)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(28, 32, 28, 32)
            setBackgroundColor(bg)
        }
        root.addView(TextView(this).apply {
            this.text = "☾ SkyScape Weather"
            textSize = 28f
            setTextColor(text)
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            this.text = "Choose your glass skin and Indian city"
            textSize = 15f
            setTextColor(accent)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 22)
        })
        root.addView(TextView(this).apply {
            this.text = "Widget skin"
            textSize = 16f
            setTextColor(text)
        })
        val skinSpinner = Spinner(this)
        skinSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, skinNames)
        skinSpinner.setSelection(skinValues.indexOf(prefs.getString("skin", "coffee")).coerceAtLeast(0))
        root.addView(skinSpinner)

        root.addView(TextView(this).apply {
            this.text = "Choose Indian city"
            textSize = 16f
            setTextColor(text)
            setPadding(0, 20, 0, 0)
        })
        val citySpinner = Spinner(this)
        citySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cityNames)
        citySpinner.setSelection(cityNames.indexOf(prefs.getString("city", "Bengaluru")).coerceAtLeast(0))
        root.addView(citySpinner)

        val save = TextView(this).apply {
            this.text = "APPLY SKIN & SAVE CITY"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(bg)
            setBackgroundColor(accent)
            setPadding(18, 16, 18, 16)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.topMargin = 18
            layoutParams = params
        }
        root.addView(save)
        save.setOnClickListener {
            val cityIndex = citySpinner.selectedItemPosition
            val skinIndex = skinSpinner.selectedItemPosition
            prefs.edit().putString("city", cityNames[cityIndex])
                .putString("skin", skinValues[skinIndex])
                .putFloat("latitude", coordinates[cityIndex][0].toFloat())
                .putFloat("longitude", coordinates[cityIndex][1].toFloat()).apply()
            sendBroadcast(Intent(this, SkyScapeWidgetProvider::class.java).setAction("com.dheeraj.skyscape.REFRESH"))
            save.text = "SAVED • WIDGET REFRESHING"
        }
        root.addView(TextView(this).apply {
            this.text = "Glass Green uses a deep emerald glass panel with parrot-green accents. AQI uses the US AQI scale (not India's CPCB scale). CO₂ is a modelled outdoor estimate. Add or resize the widget from your home screen: long-press → Widgets → SkyScape Weather."
            textSize = 13f
            setTextColor(text)
            setPadding(0, 24, 0, 0)
        })
        setContentView(root)
    }
}
