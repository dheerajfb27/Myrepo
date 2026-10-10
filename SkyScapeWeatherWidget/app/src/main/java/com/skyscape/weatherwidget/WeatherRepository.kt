package com.skyscape.weatherwidget

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WeatherSnapshot(
    val temperature: Int,
    val high: Int,
    val low: Int,
    val description: String,
    val symbol: String,
    val location: String = "Bengaluru"
)

object WeatherRepository {
    private const val PREFS = "skyscape_weather"
    private const val KEY_TEMP = "temperature"
    private const val KEY_HIGH = "high"
    private const val KEY_LOW = "low"
    private const val KEY_DESCRIPTION = "description"
    private const val KEY_SYMBOL = "symbol"

    suspend fun refresh(context: Context): WeatherSnapshot = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        try {
            // MET Norway Locationforecast: global forecast, no API key required.
            // Identify this client as required by api.met.no usage guidance.
            val url = URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=12.9716&lon=77.5946")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "SkyScapeWeatherWidget/0.1.2 (Android weather widget)")
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
            val summary = nextHour?.optJSONObject("summary")
            val symbolCode = summary?.optString("symbol_code", "cloudy") ?: "cloudy"
            val (description, symbol) = condition(symbolCode)

            var high = temp
            var low = temp
            // Estimate today's range from the next 24 forecast points.
            for (i in 0 until minOf(24, series.length())) {
                val item = series.getJSONObject(i).getJSONObject("data")
                    .getJSONObject("instant").getJSONObject("details")
                val t = item.optDouble("air_temperature", temp.toDouble()).toInt()
                high = maxOf(high, t)
                low = minOf(low, t)
            }

            prefs.edit().putInt(KEY_TEMP, temp).putInt(KEY_HIGH, high).putInt(KEY_LOW, low)
                .putString(KEY_DESCRIPTION, description).putString(KEY_SYMBOL, symbol).apply()
            WeatherSnapshot(temp, high, low, description, symbol)
        } catch (_: Exception) {
            // Keep the last successful forecast visible if the service is unreachable.
            WeatherSnapshot(
                prefs.getInt(KEY_TEMP, -1), prefs.getInt(KEY_HIGH, -1), prefs.getInt(KEY_LOW, -1),
                prefs.getString(KEY_DESCRIPTION, "Weather unavailable — tap to retry") ?: "Weather unavailable — tap to retry",
                prefs.getString(KEY_SYMBOL, "☁️") ?: "☁️"
            )
        }
    }

    fun cached(context: Context): WeatherSnapshot {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return WeatherSnapshot(p.getInt(KEY_TEMP, -1), p.getInt(KEY_HIGH, -1), p.getInt(KEY_LOW, -1),
            p.getString(KEY_DESCRIPTION, "Tap to refresh") ?: "Tap to refresh",
            p.getString(KEY_SYMBOL, "☁️") ?: "☁️")
    }

    private fun condition(code: String): Pair<String, String> {
        val normalized = code.lowercase()
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
