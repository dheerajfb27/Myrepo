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
    private val skinNames = arrayOf("Coffee Glass", "Glass Green", "AMOLED Neon", "Arctic Blue", "Sunset Glow", "Rose Quartz")
    private val skinValues = arrayOf("coffee", "glass_green", "amoled", "arctic", "sunset", "rose")
    private val backgrounds = intArrayOf(Color.rgb(42,31,26), Color.rgb(7,29,19), Color.rgb(5,5,12), Color.rgb(15,42,58), Color.rgb(54,21,48), Color.rgb(49,27,43))
    private val accents = intArrayOf(Color.rgb(212,163,115), Color.rgb(154,255,87), Color.rgb(53,230,255), Color.rgb(139,233,255), Color.rgb(255,155,113), Color.rgb(255,139,203))
    private val texts = intArrayOf(Color.rgb(232,216,196), Color.rgb(240,255,232), Color.rgb(239,246,255), Color.rgb(231,249,255), Color.rgb(255,239,230), Color.rgb(255,239,249))

    private fun themedSpinnerAdapter(items: Array<String>, primary: Int, accent: Int): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            private fun style(view: android.view.View, color: Int): android.view.View {
                val label = view as TextView
                label.setTextColor(color)
                label.textSize = 16f
                label.setPadding(12, 12, 12, 12)
                return label
            }

            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                return style(super.getView(position, convertView, parent), primary)
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                return style(super.getDropDownView(position, convertView, parent), accent)
            }
        }.also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("weather_cache", MODE_PRIVATE)
        val currentSkin = skinValues.indexOf(prefs.getString("skin", "coffee")).coerceAtLeast(0)
        val bg = backgrounds[currentSkin]
        val accent = accents[currentSkin]
        val text = texts[currentSkin]
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
            this.text = "Choose a glass skin and Indian city"
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
        skinSpinner.adapter = themedSpinnerAdapter(skinNames, text, accent)
        skinSpinner.setSelection(currentSkin)
        root.addView(skinSpinner)

        root.addView(TextView(this).apply {
            this.text = "Choose Indian city"
            textSize = 16f
            setTextColor(text)
            setPadding(0, 20, 0, 0)
        })
        val citySpinner = Spinner(this)
        citySpinner.adapter = themedSpinnerAdapter(cityNames, text, accent)
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
            save.setTextColor(texts[skinIndex])
            save.setBackgroundColor(accents[skinIndex])
        }
        root.addView(TextView(this).apply {
            this.text = "Glass Green is the signature emerald look. AMOLED Neon, Arctic Blue, Sunset Glow and Rose Quartz are extra themes. AQI currently uses the US AQI scale (not India's CPCB scale); CO₂ is a modelled outdoor estimate. Add or resize the widget from your home screen: long-press → Widgets → SkyScape Weather."
            textSize = 13f
            setTextColor(text)
            setPadding(0, 24, 0, 0)
        })
        setContentView(root)
    }
}
