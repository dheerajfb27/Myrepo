package com.skyscape.weatherwidget

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
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
        var connection: HttpURLConnection? = null
        try {
            // Open-Meteo is the primary source: no API key and a simple stable JSON schema.
            val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=12.9716&longitude=77.5946&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min,weather_code&timezone=Asia%2FKolkata&forecast_days=5"
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "SkyScapeWeatherWidget/0.1.9")
            }
            val status = connection.responseCode
            if (status !in 200..299) throw IllegalStateException("Weather service HTTP $status")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val current = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")
            val temp = current.getDouble("temperature_2m").toInt()
            val (description, symbol) = openMeteoCondition(current.optInt("weather_code", 2))
            val highs = daily.getJSONArray("temperature_2m_max")
            val lows = daily.getJSONArray("temperature_2m_min")
            val codes = daily.optJSONArray("weather_code")
            val dates = daily.getJSONArray("time")
            val forecast = mutableListOf<ForecastDay>()
            for (i in 0 until minOf(5, dates.length(), highs.length(), lows.length())) {
                val date = dates.getString(i)
                val dayLabel = when (i) {
                    0 -> "Today"
                    1 -> "Tmrw"
                    else -> SimpleDateFormat("EEE", Locale.US).format(
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date) ?: Date()
                    )
                }
                val (_, daySymbol) = openMeteoCondition(codes?.optInt(i, 2) ?: 2)
                forecast.add(
                    ForecastDay(
                        dayLabel,
                        highs.getDouble(i).toInt(),
                        lows.getDouble(i).toInt(),
                        description,
                        daySymbol
                    )
                )
            }
            if (forecast.isEmpty()) throw IllegalStateException("Weather service returned no forecast days")
            val high = forecast.first().high
            val low = forecast.first().low
            prefs.edit()
                .putInt(KEY_TEMP, temp)
                .putInt(KEY_HIGH, high)
                .putInt(KEY_LOW, low)
                .putString(KEY_DESCRIPTION, description)
                .putString(KEY_SYMBOL, symbol)
                .putString(KEY_FORECAST, toForecastJson(forecast).toString())
                .putString("last_error", "")
                .apply()
            WeatherSnapshot(temp, high, low, description, symbol, forecast = forecast)
        } catch (e: Exception) {
            // Keep the last successful values if offline; don't silently pretend a fetch succeeded.
            prefs.edit().putString("last_error", e.javaClass.simpleName + ": " + (e.message ?: "request failed")).apply()
            cached(context)
        } finally {
            connection?.disconnect()
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

    private fun toForecastJson(forecast: List<ForecastDay>) = org.json.JSONArray().also { arr ->
        forecast.forEach { day ->
            arr.put(JSONObject().put("day", day.day).put("high", day.high).put("low", day.low)
                .put("description", day.description).put("symbol", day.symbol))
        }
    }

    private fun buildForecast(series: org.json.JSONArray, fallbackTemp: Int): List<ForecastDay> {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val localDay = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") }
        val label = SimpleDateFormat("EEE", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") }
        data class Bucket(var min: Int, var max: Int, var code: String, val date: Date)
        val buckets = linkedMapOf<String, Bucket>()
        val todayKey = localDay.format(Date())
        for (i in 0 until series.length()) {
            val point = series.getJSONObject(i)
            val date = runCatching { parser.parse(point.optString("time")) }.getOrNull() ?: continue
            val data = point.optJSONObject("data") ?: continue
            val temp = data.optJSONObject("instant")?.optJSONObject("details")?.optDouble("air_temperature", fallbackTemp.toDouble())?.toInt() ?: fallbackTemp
            val code = data.optJSONObject("next_1_hours")?.optJSONObject("summary")?.optString("symbol_code", "cloudy") ?: "cloudy"
            val key = localDay.format(date)
            val bucket = buckets[key]
            if (bucket == null) buckets[key] = Bucket(temp, temp, code, date)
            else {
                bucket.min = minOf(bucket.min, temp)
                bucket.max = maxOf(bucket.max, temp)
                if (i % 4 == 0) bucket.code = code
            }
        }
        return buckets.entries.take(5).mapIndexed { index, entry ->
            val b = entry.value
            val (desc, icon) = condition(b.code)
            ForecastDay(if (entry.key == todayKey || index == 0) "Today" else if (index == 1) "Tmrw" else label.format(b.date),
                b.max, b.min, desc, icon)
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
