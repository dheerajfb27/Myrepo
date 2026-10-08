package com.sunweather.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WeatherRepository {
    data class ApiTestResult(val ok:Boolean,val message:String)

    private const val PREFS="sun_weather"
    private const val KEY="openweather_key"
    private const val CITY="city"
    private const val THEME="theme"
    private const val ICON_SET="icon_set"
    private const val SHORTCUTS="shortcuts"
    private const val MODEL="model"

    fun prefs(context:Context)=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
    fun apiKey(context:Context)=prefs(context).getString(KEY,"")?:""
    fun city(context:Context)=prefs(context).getString(CITY,"Bengaluru")?:"Bengaluru"
    fun theme(context:Context)=prefs(context).getString(THEME,"Sunrise")?:"Sunrise"
    fun iconSet(context:Context)=prefs(context).getString(ICON_SET,"Colorful")?:"Colorful"
    fun shortcuts(context:Context):Set<String> = prefs(context).getStringSet(SHORTCUTS,defaultShortcuts())?:defaultShortcuts()

    fun saveSettings(context:Context,key:String,city:String,theme:String,iconSet:String,shortcuts:Set<String>){
        prefs(context).edit().putString(KEY,key.trim()).putString(CITY,city.trim().ifBlank{"Bengaluru"})
            .putString(THEME,theme).putString(ICON_SET,iconSet).putStringSet(SHORTCUTS,shortcuts).apply()
    }
    fun defaultShortcuts()=linkedSetOf("phone","messages","camera","twitter","telegram","spotify")

    fun testApiKey(key:String,city:String):ApiTestResult{
        if(key.isBlank()) return ApiTestResult(false,"API key is empty")
        if(city.isBlank()) return ApiTestResult(false,"City is empty")
        return try{
            val q=URLEncoder.encode(city.trim(),"UTF-8")
            val c=URL("https://api.openweathermap.org/data/2.5/weather?q="+q+"&units=metric&appid="+key.trim())
                .openConnection() as HttpURLConnection
            c.connectTimeout=10000
            c.readTimeout=10000
            c.requestMethod="GET"
            val code=c.responseCode
            val body=(if(code in 200..299)c.inputStream else c.errorStream)?.bufferedReader()?.use{it.readText()}?:""
            when(code){
                200->ApiTestResult(true,"API Connected ✓")
                401->ApiTestResult(false,"Invalid or inactive API key")
                404->ApiTestResult(false,"City not found")
                429->ApiTestResult(false,"API rate limit exceeded")
                else->{
                    val msg=try{JSONObject(body).optString("message")}catch(_:Exception){""}
                    ApiTestResult(false,if(msg.isNotBlank())"API error ($code): $msg" else "API error (HTTP $code)")
                }
            }
        }catch(_:Exception){
            ApiTestResult(false,"Network error — check internet connection")
        }
    }

    fun loadModel(context:Context):WeatherModel{
        val raw=prefs(context).getString(MODEL,null)
        return try{if(raw==null)demo() else fromJson(JSONObject(raw))}catch(_:Exception){demo()}
    }

    fun refresh(context:Context):Boolean{
        val key=apiKey(context)
        if(key.isBlank()){
            prefs(context).edit().putString(MODEL,JSONObject(toMap(demo())).toString()).apply()
            return true
        }
        return try{
            val q=URLEncoder.encode(city(context),"UTF-8")
            val current=getJson("https://api.openweathermap.org/data/2.5/weather?q="+q+"&units=metric&appid="+key)
            val forecast=getJson("https://api.openweathermap.org/data/2.5/forecast?q="+q+"&units=metric&appid="+key)
            prefs(context).edit().putString(MODEL,JSONObject(toMap(parse(current,forecast))).toString()).apply()
            true
        }catch(_:Exception){false}
    }

    private fun getJson(url:String):JSONObject{
        val c=URL(url).openConnection() as HttpURLConnection
        c.connectTimeout=10000;c.readTimeout=10000;c.requestMethod="GET"
        return c.inputStream.bufferedReader().use{JSONObject(it.readText())}
    }

    private fun parse(current:JSONObject,forecast:JSONObject):WeatherModel{
        val main=current.getJSONObject("main")
        val w=current.getJSONArray("weather").getJSONObject(0)
        val now=Date()
        val date=SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault()).format(now)
        val time=SimpleDateFormat("hh:mm a",Locale.getDefault()).format(now)
        val list=forecast.getJSONArray("list")
        val days=linkedMapOf<String,ForecastDay>()
        for(i in 0 until list.length()){
            val item=list.getJSONObject(i)
            val dt=item.getString("dt_txt")
            val parsed=try{SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).parse(dt)}catch(_:Exception){null}?:now
            val day=SimpleDateFormat("EEE",Locale.getDefault()).format(parsed)
            if(day==SimpleDateFormat("EEE",Locale.getDefault()).format(now)) continue
            val m=item.getJSONObject("main");val iw=item.getJSONArray("weather").getJSONObject(0)
            val old=days[day]
            if(old==null)days[day]=ForecastDay(day,iconFor(iw.optInt("id",800)),m.optDouble("temp_max").toInt(),m.optDouble("temp_min").toInt())
            else days[day]=old.copy(max=maxOf(old.max,m.optDouble("temp_max").toInt()),min=minOf(old.min,m.optDouble("temp_min").toInt()))
            if(days.size==3)break
        }
        val temp=main.optDouble("temp").toInt()
        return WeatherModel(current.optString("name",contextDummy()),date,time,temp,
            main.optDouble("temp_min",temp.toDouble()).toInt(),main.optDouble("temp_max",temp.toDouble()).toInt(),
            w.optString("description","Weather").replaceFirstChar{it.uppercase()},iconFor(w.optInt("id",800)),
            main.optDouble("feels_like",temp.toDouble()).toInt(),main.optInt("humidity"),(current.optJSONObject("wind")?.optDouble("speed",0.0)?.times(3.6))?.toInt()?:0,days.values.toList())
    }

    private fun contextDummy()="Bengaluru"
    private fun iconFor(id:Int)=when{
        id in 200..232->"⛈";id in 300..321->"🌦";id in 500..531->"🌧";id in 600..622->"❄"
        id in 701..781->"🌫";id==800->"☀";id in 801..804->"⛅";else->"☁"
    }
    private fun demo()=WeatherModel("Bengaluru","Wednesday, 7 October 2026","09:22 AM",20,18,20,"Scattered Showers","🌦",20,72,12,
        listOf(ForecastDay("Mon","⛅",20,18),ForecastDay("Tue","🌦",20,18),ForecastDay("Wed","🌧",20,17)))
    private fun toMap(m:WeatherModel):Map<String,Any> = mapOf(
        "city" to m.city,"date" to m.date,"time" to m.time,"temperature" to m.temperature,"min" to m.min,"max" to m.max,
        "condition" to m.condition,"icon" to m.icon,"feelsLike" to m.feelsLike,"humidity" to m.humidity,"wind" to m.wind,
        "forecast" to JSONArray(m.forecast.map{JSONObject(mapOf("day" to it.day,"icon" to it.icon,"max" to it.max,"min" to it.min))})
    )
    private fun fromJson(j:JSONObject):WeatherModel{
        val a=j.optJSONArray("forecast")?:JSONArray()
        val days=(0 until a.length()).map{val x=a.getJSONObject(it);ForecastDay(x.optString("day"),x.optString("icon"),x.optInt("max"),x.optInt("min"))}
        return WeatherModel(j.optString("city","Bengaluru"),j.optString("date"),j.optString("time"),j.optInt("temperature"),j.optInt("min"),j.optInt("max"),
            j.optString("condition"),j.optString("icon","☀"),j.optInt("feelsLike"),j.optInt("humidity"),j.optInt("wind"),days)
    }
}
