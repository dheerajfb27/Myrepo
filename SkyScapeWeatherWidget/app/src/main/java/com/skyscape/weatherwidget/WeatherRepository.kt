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
            // Default city coordinates are Bengaluru; later settings can make this user-selectable.
            val url = URL("https://api.open-meteo.com/v1/forecast?latitude=12.9716&longitude=77.5946&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&forecast_days=1&timezone=Asia%2FKolkata")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
            }
            val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            connection.disconnect()
            val current = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")
            val temp = current.getDouble("temperature_2m").toInt()
            val code = current.getInt("weather_code")
            val high = daily.getJSONArray("temperature_2m_max").getDouble(0).toInt()
            val low = daily.getJSONArray("temperature_2m_min").getDouble(0).toInt()
            val (description, symbol) = condition(code)
            prefs.edit().putInt(KEY_TEMP, temp).putInt(KEY_HIGH, high).putInt(KEY_LOW, low)
                .putString(KEY_DESCRIPTION, description).putString(KEY_SYMBOL, symbol).apply()
            WeatherSnapshot(temp, high, low, description, symbol)
        } catch (_: Exception) {
            WeatherSnapshot(
                prefs.getInt(KEY_TEMP, -1), prefs.getInt(KEY_HIGH, -1), prefs.getInt(KEY_LOW, -1),
                prefs.getString(KEY_DESCRIPTION, "Tap to retry") ?: "Tap to retry",
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

    private fun condition(code: Int): Pair<String, String> = when (code) {
        0 -> "Clear sky" to "☀️"
        1, 2 -> "Partly cloudy" to "🌤️"
        3 -> "Overcast" to "☁️"
        45, 48 -> "Foggy" to "🌫️"
        51, 53, 55, 56, 57 -> "Drizzle" to "🌦️"
        61, 63, 65, 66, 67, 80, 81, 82 -> "Rain" to "🌧️"
        71, 73, 75, 77, 85, 86 -> "Snow" to "❄️"
        95, 96, 99 -> "Thunderstorm" to "⛈️"
        else -> "Current weather" to "🌤️"
    }
}
