package com.skyscape.weatherwidget

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ForecastDay(
    val day: String,
    val high: Int,
    val low: Int,
    val description: String,
    val symbol: String
)

data class WeatherSnapshot(
    val temperature: Int,
    val high: Int,
    val low: Int,
    val description: String,
    val symbol: String,
    val location: String = "Bengaluru",
    val forecast: List<ForecastDay> = emptyList()
)

object WeatherRepository {
    private const val PREFS = "skyscape_weather"
    private const val KEY_TEMP = "temperature"
    private const val KEY_HIGH = "high"
    private const val KEY_LOW = "low"
    private const val KEY_DESCRIPTION = "description"
    private const val KEY_SYMBOL = "symbol"
    private const val KEY_FORECAST = "forecast_json"

    suspend fun refresh(context: Context): WeatherSnapshot = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        try {
            val url = URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=12.9716&lon=77.5946")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "SkyScapeWeatherWidget/0.1.3 (Android weather widget)")
                setRequestProperty("Accept", "application/json")
            }
            val status = connection.responseCode
            if (status !in 200..299) {
                connection.disconnect()
                throw IllegalStateException("MET Norway returned HTTP $status")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val root = JSONObject(body)
            val series = root.getJSONObject("properties").getJSONArray("timeseries")
            if (series.length() == 0) throw IllegalStateException("Empty forecast")
            val firstData = series.getJSONObject(0).getJSONObject("data")
            val details = firstData.getJSONObject("instant").getJSONObject("details")
            val temp = details.getDouble("air_temperature").toInt()
            val nextHour = firstData.optJSONObject("next_1_hours")
            val symbolCode = nextHour?.optJSONObject("summary")?.optString("symbol_code", "cloudy") ?: "cloudy"
            val (description, symbol) = condition(symbolCode)

            var high = temp
            var low = temp
            for (i in 0 until minOf(24, series.length())) {
                val item = series.getJSONObject(i).getJSONObject("data").getJSONObject("instant").getJSONObject("details")
                val t = item.optDouble("air_temperature", temp.toDouble()).toInt()
                high = maxOf(high, t)
                low = minOf(low, t)
            }

            val forecast = buildForecast(series, temp)
            val forecastJson = org.json.JSONArray()
            forecast.forEach { day ->
                forecastJson.put(JSONObject().put("day", day.day).put("high", day.high).put("low", day.low)
                    .put("description", day.description).put("symbol", day.symbol))
            }
            prefs.edit().putInt(KEY_TEMP, temp).putInt(KEY_HIGH, high).putInt(KEY_LOW, low)
                .putString(KEY_DESCRIPTION, description).putString(KEY_SYMBOL, symbol)
                .putString(KEY_FORECAST, forecastJson.toString()).apply()
            WeatherSnapshot(temp, high, low, description, symbol, forecast = forecast)
        } catch (_: Exception) {
            // Fallback provider: still show current temperature if MET Norway is unavailable.
            runCatching {
                val fallbackUrl = URL("https://api.open-meteo.com/v1/forecast?latitude=12.9716&longitude=77.5946&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=Asia%2FKolkata&forecast_days=3")
                val conn = (fallbackUrl.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                }
                val status = conn.responseCode
                if (status !in 200..299) throw IllegalStateException("Fallback weather HTTP $status")
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                conn.disconnect()
                val current = json.getJSONObject("current")
                val daily = json.getJSONObject("daily")
                val temp = current.getDouble("temperature_2m").toInt()
                val (description, symbol) = openMeteoCondition(current.optInt("weather_code", 2))
                val highs = daily.getJSONArray("temperature_2m_max")
                val lows = daily.getJSONArray("temperature_2m_min")
                val high = if (highs.length() > 0) highs.getDouble(0).toInt() else temp
                val low = if (lows.length() > 0) lows.getDouble(0).toInt() else temp
                val forecast = mutableListOf<ForecastDay>()
                val dates = daily.getJSONArray("time")
                for (i in 0 until minOf(3, dates.length())) {
                    val dayLabel = when (i) {
                        0 -> "Today"
                        1 -> "Tomorrow"
                        else -> dates.getString(i).takeLast(2)
                    }
                    forecast.add(ForecastDay(dayLabel, highs.getDouble(i).toInt(), lows.getDouble(i).toInt(), description, symbol))
                }
                val savedForecast = org.json.JSONArray()
                forecast.forEach { day ->
                    savedForecast.put(JSONObject().put("day", day.day).put("high", day.high).put("low", day.low)
                        .put("description", day.description).put("symbol", day.symbol))
                }
                prefs.edit().putInt(KEY_TEMP, temp).putInt(KEY_HIGH, high).putInt(KEY_LOW, low)
                    .putString(KEY_DESCRIPTION, description).putString(KEY_SYMBOL, symbol)
                    .putString(KEY_FORECAST, savedForecast.toString()).apply()
                WeatherSnapshot(temp, high, low, description, symbol, forecast = forecast)
            }.getOrElse { cached(context) }
        }
    }

    fun cached(context: Context): WeatherSnapshot {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val forecast = mutableListOf<ForecastDay>()
        runCatching {
            val arr = org.json.JSONArray(p.getString(KEY_FORECAST, "[]"))
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                forecast.add(ForecastDay(item.optString("day", "Day"), item.optInt("high", -1), item.optInt("low", -1),
                    item.optString("description", "Forecast"), item.optString("symbol", "☁️")))
            }
        }
        return WeatherSnapshot(
            p.getInt(KEY_TEMP, -1), p.getInt(KEY_HIGH, -1), p.getInt(KEY_LOW, -1),
            p.getString(KEY_DESCRIPTION, "Tap to refresh") ?: "Tap to refresh",
            p.getString(KEY_SYMBOL, "☁️") ?: "☁️", forecast = forecast
        )
    }

    private fun buildForecast(series: org.json.JSONArray, fallbackTemp: Int): List<ForecastDay> {
        val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val localDay = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") }
        val dayLabel = SimpleDateFormat("EEE", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") }
        val buckets = linkedMapOf<String, MutableList<Any>>()
        val now = Date()
        val todayKey = localDay.format(now)
        for (i in 0 until series.length()) {
            val point = series.getJSONObject(i)
            val date = runCatching { utcParser.parse(point.optString("time")) }.getOrNull() ?: continue
            val key = localDay.format(date)
            val data = point.optJSONObject("data") ?: continue
            val temp = data.optJSONObject("instant")?.optJSONObject("details")?.optDouble("air_temperature", fallbackTemp.toDouble())?.toInt() ?: fallbackTemp
            val next = data.optJSONObject("next_1_hours")?.optJSONObject("summary")?.optString("symbol_code", "cloudy") ?: "cloudy"
            val bucket = buckets.getOrPut(key) { mutableListOf(Int.MAX_VALUE, Int.MIN_VALUE, next, date) }
            bucket[0] = minOf(bucket[0] as Int, temp)
            bucket[1] = maxOf(bucket[1] as Int, temp)
            if (i % 4 == 0) bucket[2] = next
        }
        return buckets.entries.take(3).mapIndexed { index, entry ->
            val bucket = entry.value
            val code = bucket[2] as String
            val (desc, icon) = condition(code)
            val date = bucket[3] as Date
            ForecastDay(if (entry.key == todayKey || index == 0) "Today" else dayLabel.format(date),
                bucket[1] as Int, bucket[0] as Int, desc, icon)
        }
    }

    private fun openMeteoCondition(code: Int): Pair<String, String> = when (code) {
        0 -> "Clear sky" to "☀️"
        1 -> "Mostly clear" to "🌤️"
        2 -> "Partly cloudy" to "⛅"
        3 -> "Cloudy" to "☁️"
        45, 48 -> "Foggy" to "🌫️"
        in 51..67, in 80..82 -> "Rain" to "🌧️"
        in 71..77, 85, 86 -> "Snow" to "❄️"
        in 95..99 -> "Thunderstorm" to "⛈️"
        else -> "Current weather" to "🌤️"
    }

    private fun condition(code: String): Pair<String, String> {
        val normalized = code.lowercase(Locale.US)
        return when {
            normalized.startsWith("clearsky") -> "Clear sky" to "☀️"
            normalized.startsWith("fair") -> "Mostly clear" to "🌤️"
            normalized.startsWith("partlycloudy") -> "Partly cloudy" to "⛅"
            normalized.startsWith("cloudy") -> "Cloudy" to "☁️"
            normalized.startsWith("fog") -> "Foggy" to "🌫️"
            normalized.contains("lightrain") || normalized.contains("rainshowers") -> "Light rain" to "🌦️"
            normalized.contains("rain") -> "Rain" to "🌧️"
            normalized.contains("sleet") -> "Sleet" to "🌨️"
            normalized.contains("snow") -> "Snow" to "❄️"
            normalized.contains("thunder") -> "Thunderstorm" to "⛈️"
            else -> "Current weather" to "🌤️"
        }
    }
}
