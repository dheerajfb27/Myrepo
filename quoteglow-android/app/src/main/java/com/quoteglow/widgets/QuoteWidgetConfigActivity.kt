package com.quoteglow.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView

class QuoteWidgetConfigActivity:Activity(){
    private var id=AppWidgetManager.INVALID_APPWIDGET_ID
    override fun onCreate(state:Bundle?){super.onCreate(state);id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);if(id<0){setResult(RESULT_CANCELED);finish();return}
        val p=QuoteGlowStore.prefs(this);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(28,28,28,28)}
        val q=EditText(this).apply{setText(p.getString("quote_$id","Dream it. Build it. Live it."));hint="Quote"};root.addView(q)
        val a=EditText(this).apply{setText(p.getString("author_$id", ""));hint="Author (optional)"};root.addView(a)
        fun addLabel(s:String){root.addView(TextView(this).apply{text=s;setPadding(0,16,0,4)})}
        fun spinner(values:Array<String>,selected:String):Spinner{val s=Spinner(this);s.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,values);s.setSelection(values.indexOf(selected).coerceAtLeast(0));return s}
        addLabel("Font");val fonts= arrayOf("Sans","Serif","Elegant","Handwritten","Bold","Mono");val fs=spinner(fonts,p.getString("font_$id","Sans")!!);root.addView(fs)
        addLabel("Text colors / gradient");val modes=arrayOf("Rainbow","Ocean","Sunset","Purple","Mint","Gold","White");val ms=spinner(modes,p.getString("mode_$id","Rainbow")!!);root.addView(ms)
        addLabel("Background");val bgs=arrayOf("Glass","Minimal","Neon","Gradient","AMOLED","Elegant","Handwritten","Bold","Retro","Transparent");val bs=spinner(bgs,p.getString("bgStyle_$id","Glass")!!);root.addView(bs)
        addLabel("Text opacity: ${p.getInt("textOpacity_$id",100)}%");val to=SeekBar(this).apply{max=90;progress=p.getInt("textOpacity_$id",100)-10};root.addView(to)
        addLabel("Background opacity: ${p.getInt("bgOpacity_$id",75)}%");val bo=SeekBar(this).apply{max=100;progress=p.getInt("bgOpacity_$id",75)};root.addView(bo)
        addLabel("Text size: ${p.getInt("textSize_$id",20)}sp");val ts=SeekBar(this).apply{max=24;progress=p.getInt("textSize_$id",20)-12};root.addView(ts)
        val save=Button(this).apply{text="Save Widget";setOnClickListener{p.edit().putString("quote_$id",q.text.toString().ifBlank{"Dream it. Build it. Live it."}).putString("author_$id",a.text.toString()).putString("font_$id",fonts[fs.selectedItemPosition]).putString("mode_$id",modes[ms.selectedItemPosition]).putString("bgStyle_$id",bgs[bs.selectedItemPosition]).putInt("textOpacity_$id",to.progress+10).putInt("bgOpacity_$id",bo.progress).putInt("textSize_$id",ts.progress+12).apply();setResult(RESULT_OK,Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id));QuoteWidgetReceiver.update(this,AppWidgetManager.getInstance(this),id);finish()}};root.addView(save);setContentView(root)
    }
}
