package com.skyscape.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.concurrent.Executors

class SkyScapeWidgetReceiver : AppWidgetProvider() {
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){ids.forEach{render(context,manager,it,WeatherRepository.cached(context))};refreshAll(context)}
 override fun onReceive(context:Context,intent:Intent){super.onReceive(context,intent);if(intent.action==ACTION_REFRESH)refreshAll(context)}
 private fun refreshAll(context:Context){val app=context.applicationContext;Executors.newSingleThreadExecutor().execute{val snapshot=WeatherRepository.refresh(app);val manager=AppWidgetManager.getInstance(app);manager.getAppWidgetIds(ComponentName(app,SkyScapeWidgetReceiver::class.java)).forEach{render(app,manager,it,snapshot)}}}
 private fun render(context:Context,manager:AppWidgetManager,id:Int,data:WeatherSnapshot){
  val v=RemoteViews(context.packageName,R.layout.skyscape_widget);v.setTextViewText(R.id.widget_location,"BENGALURU • WEATHER");v.setTextViewText(R.id.widget_symbol,data.symbol);v.setTextViewText(R.id.widget_temperature,if(data.temp>=0)"${data.temp}°" else "--°");v.setTextViewText(R.id.widget_condition,if(data.temp>=0)data.description else data.diagnostic);v.setTextViewText(R.id.widget_range,if(data.high>=0&&data.low>=0)"H ${data.high}°  L ${data.low}°" else "H --°  L --°")
  val dayIds=intArrayOf(R.id.day1,R.id.day2,R.id.day3,R.id.day4,R.id.day5);val iconIds=intArrayOf(R.id.icon1,R.id.icon2,R.id.icon3,R.id.icon4,R.id.icon5);val tempIds=intArrayOf(R.id.temp1,R.id.temp2,R.id.temp3,R.id.temp4,R.id.temp5);val fallback=listOf("Today","Tmrw","Wed","Thu","Fri")
  for(i in 0..4){val d=data.forecast.getOrNull(i);v.setTextViewText(dayIds[i],d?.day?:fallback[i]);v.setTextViewText(iconIds[i],d?.symbol?:"☁️");v.setTextViewText(tempIds[i],if(d!=null)"${d.high}°/${d.low}°" else "--°/--°")}
  val pi=PendingIntent.getBroadcast(context,id,Intent(context,SkyScapeWidgetReceiver::class.java).setAction(ACTION_REFRESH),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);v.setOnClickPendingIntent(R.id.widget_root,pi);manager.updateAppWidget(id,v)
 }
 companion object{private const val ACTION_REFRESH="com.skyscape.weatherwidget.ACTION_REFRESH"}
}
