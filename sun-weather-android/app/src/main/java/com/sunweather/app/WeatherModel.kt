package com.sunweather.app
data class ForecastDay(val day:String,val icon:String,val max:Int,val min:Int)
data class WeatherModel(
    val city:String,val date:String,val time:String,val temperature:Int,val min:Int,val max:Int,
    val condition:String,val icon:String,val feelsLike:Int,val humidity:Int,val wind:Int,
    val forecast:List<ForecastDay>
)
