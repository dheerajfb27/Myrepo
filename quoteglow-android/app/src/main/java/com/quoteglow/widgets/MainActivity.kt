package com.quoteglow.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity:Activity(){
    override fun onCreate(state:Bundle?){super.onCreate(state);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,40,32,32)}
        root.addView(TextView(this).apply{text="QuoteGlow Widgets";textSize=28f});root.addView(TextView(this).apply{text="Stylish quotes for your home screen.\n\n2×1 and 4×1 • fonts • multicolor • gradient • opacity • resize";textSize=16f})
        root.addView(Button(this).apply{text="Refresh Widgets";setOnClickListener{val m=AppWidgetManager.getInstance(this@MainActivity);val a=m.getAppWidgetIds(ComponentName(this@MainActivity,QuoteWidgetReceiver::class.java));QuoteWidgetReceiver.update(this@MainActivity,m,a.firstOrNull()?:return@setOnClickListener)}})
        setContentView(root)
    }
}
