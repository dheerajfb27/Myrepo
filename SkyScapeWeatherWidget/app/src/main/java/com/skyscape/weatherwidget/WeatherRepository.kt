package com.skyscape.weatherwidget

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ForecastDay(val day: String, val high: Int, val low: Int, val symbol: String)
data class WeatherSnapshot(val temp: Int = -1, val high: Int = -1, val low: Int = -1, val description: String = "Tap to refresh", val symbol: String = "☁️", val forecast: List<ForecastDay> = emptyList(), val diagnostic: String = "Not requested")
object WeatherRepository {
 private const val PREFS = "skyscape_fresh_weather"
 fun cached(context: Context): WeatherSnapshot {
  val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE); val days=mutableListOf<ForecastDay>()
  runCatching { val arr=org.json.JSONArray(p.getString("forecast","[]")); for(i in 0 until arr.length()){val o=arr.getJSONObject(i);days.add(ForecastDay(o.optString("day"),o.optInt("high",-1),o.optInt("low",-1),o.optString("symbol","☁️")))} }
  return WeatherSnapshot(p.getInt("temp",-1),p.getInt("high",-1),p.getInt("low",-1),p.getString("description","Tap to refresh")?:"Tap to refresh",p.getString("symbol","☁️")?:"☁️",days,p.getString("diagnostic","Not requested")?:"Not requested")
 }
 fun refresh(context: Context): WeatherSnapshot {
  val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);p.edit().putString("diagnostic","REQUEST STARTED").apply();var conn:HttpURLConnection?=null
  try {
   val url=URL("https://api.open-meteo.com/v1/forecast?latitude=12.9716&longitude=77.5946&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min,weather_code&timezone=Asia%2FKolkata&forecast_days=5")
   conn=(url.openConnection() as HttpURLConnection).apply{requestMethod="GET";connectTimeout=8000;readTimeout=8000;setRequestProperty("Accept","application/json")}
   val status=conn.responseCode;p.edit().putString("diagnostic","HTTP $status").apply();if(status !in 200..299) error("HTTP $status")
   val json=JSONObject(conn.inputStream.bufferedReader().use{it.readText()});val current=json.getJSONObject("current");val daily=json.getJSONObject("daily");val temp=current.getDouble("temperature_2m").toInt();val(desc,symbol)=condition(current.optInt("weather_code",2));val highs=daily.getJSONArray("temperature_2m_max");val lows=daily.getJSONArray("temperature_2m_min");val codes=daily.getJSONArray("weather_code");val dates=daily.getJSONArray("time")
   val days=(0 until minOf(5,dates.length(),highs.length(),lows.length())).map{i->val label=when(i){0->"Today";1->"Tmrw";else->SimpleDateFormat("EEE",Locale.US).format(SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(dates.getString(i))?:Date())};ForecastDay(label,highs.getDouble(i).toInt(),lows.getDouble(i).toInt(),condition(codes.optInt(i,2)).second)};check(days.isNotEmpty()){"No forecast days"}
   val arr=org.json.JSONArray();days.forEach{arr.put(JSONObject().put("day",it.day).put("high",it.high).put("low",it.low).put("symbol",it.symbol))}
   p.edit().putInt("temp",temp).putInt("high",days.first().high).putInt("low",days.first().low).putString("description",desc).putString("symbol",symbol).putString("forecast",arr.toString()).putString("diagnostic","JSON OK • ${temp}°C").apply();return cached(context)
  }catch(e:Exception){p.edit().putString("diagnostic",(e.javaClass.simpleName+": "+(e.message?:"request failed")).take(90)).apply();return cached(context)}finally{conn?.disconnect()}
 }
 private fun condition(code:Int):Pair<String,String> = when(code){0->"Clear sky" to "☀️";1->"Mostly clear" to "🌤️";2->"Partly cloudy" to "⛅";3->"Cloudy" to "☁️";45,48->"Fog" to "🌫️";in 51..67,in 80..82->"Rain" to "🌧️";in 71..77,85,86->"Snow" to "❄️";in 95..99->"Thunderstorm" to "⛈️";else->"Weather" to "🌤️"}
}
