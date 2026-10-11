package com.skyscape.weatherwidget

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.concurrent.Executors

class MainActivity:Activity(){private lateinit var status:TextView;private val executor=Executors.newSingleThreadExecutor()
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(28,32,28,32);setBackgroundColor(Color.rgb(7,18,38))};val title=TextView(this).apply{text="SkyScape Weather";textSize=28f;setTextColor(Color.WHITE);gravity=Gravity.CENTER};status=TextView(this).apply{text="Ready to fetch Bengaluru weather";textSize=16f;setTextColor(Color.rgb(205,226,255));gravity=Gravity.CENTER;setPadding(0,24,0,24)};val refresh=Button(this).apply{text="Refresh weather";setOnClickListener{status.text="REQUEST STARTED…";executor.execute{val d=WeatherRepository.refresh(this@MainActivity);runOnUiThread{status.text=if(d.temp>=0)"${d.temp}°C • ${d.description}\nAPI: ${d.diagnostic}" else "Weather not loaded\nAPI: ${d.diagnostic}"}}}};root.addView(title);root.addView(status);root.addView(refresh);setContentView(root)}
}
