package com.dheeraj.skyscape

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class SkyScapeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> showLoading(context, manager, id) }
        fetchWeather(context.applicationContext)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, SkyScapeWidgetProvider::class.java))
                .forEach { showLoading(context, manager, it) }
            fetchWeather(context.applicationContext)
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.dheeraj.skyscape.REFRESH"
        private val executor = Executors.newSingleThreadExecutor()

        private fun showLoading(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_skyscape)
            views.setTextViewText(R.id.updated, "Updating weather…")
            val refresh = Intent(context, SkyScapeWidgetProvider::class.java).setAction(ACTION_REFRESH)
            val pending = PendingIntent.getBroadcast(
                context, id, refresh,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.refresh, pending)
            views.setOnClickPendingIntent(R.id.widget_root, pending)
            manager.updateAppWidget(id, views)
        }

        private fun fetchWeather(context: Context) {
            executor.execute {
                try {
                    val endpoint = "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=${context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE).getFloat("latitude", 12.9716f)}&longitude=${context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE).getFloat("longitude", 77.5946f)}" +
                        "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m,uv_index,surface_pressure" +
                        "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                        "&timezone=Asia%2FKolkata&forecast_days=7"
                    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 10000
                        readTimeout = 10000
                        setRequestProperty("Accept", "application/json")
                    }
                    val response = try {
                        if (connection.responseCode !in 200..299) error("Weather service HTTP ${connection.responseCode}")
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } finally {
                        connection.disconnect()
                    }
                    val json = JSONObject(response)
                    context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
                        .edit().putString("last_response", response).apply()
                    renderAll(context, json, false)
            fetchAirQuality(context)
                } catch (_: Exception) {
                    val cached = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
                        .getString("last_response", null)
                    if (cached != null) {
                        try { renderAll(context, JSONObject(cached), true) }
                        catch (_: Exception) { renderError(context) }
                    } else renderError(context)
                }
            }
        }

        private fun fetchAirQuality(context: Context) {
            executor.execute {
                try {
                    val endpoint = "https://air-quality-api.open-meteo.com/v1/air-quality" +
                        "?latitude=${context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE).getFloat("latitude", 12.9716f)}&longitude=${context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE).getFloat("longitude", 77.5946f)}" +
                        "&hourly=carbon_dioxide,european_aqi,us_aqi" +
                        "&timezone=Asia%2FKolkata&forecast_days=2"
                    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 10000
                        readTimeout = 10000
                        setRequestProperty("Accept", "application/json")
                    }
                    val response = try {
                        if (connection.responseCode !in 200..299) error("Air quality HTTP ${connection.responseCode}")
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } finally { connection.disconnect() }
                    val root = JSONObject(response)
                    val hourly = root.getJSONObject("hourly")
                    val times = hourly.getJSONArray("time")
                    val now = SimpleDateFormat("yyyy-MM-dd'T'HH:00", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    }.format(Date())
                    var index = 0
                    for (i in 0 until times.length()) {
                        if (times.getString(i) <= now) index = i else break
                    }
                    val co2 = hourly.getJSONArray("carbon_dioxide").optDouble(index, Double.NaN)
                    val euAqi = hourly.getJSONArray("european_aqi").optDouble(index, Double.NaN)
                    val usAqi = hourly.getJSONArray("us_aqi").optDouble(index, Double.NaN)
                    val manager = AppWidgetManager.getInstance(context)
                    val ids = manager.getAppWidgetIds(ComponentName(context, SkyScapeWidgetProvider::class.java))
                    ids.forEach { id ->
                        val views = RemoteViews(context.packageName, R.layout.widget_skyscape)
                        views.setTextViewText(R.id.aqi, if (usAqi.isNaN()) "--" else usAqi.roundToInt().toString())
                        views.setTextViewText(R.id.co2, if (co2.isNaN()) "-- ppm" else "${co2.roundToInt()} ppm")
                        views.setTextViewText(R.id.aqi_detail, "US AQI scale" + if (euAqi.isNaN()) "" else " • EU ${euAqi.roundToInt()}")
                        manager.partiallyUpdateAppWidget(id, views)
                    }
                } catch (_: Exception) {
                    val manager = AppWidgetManager.getInstance(context)
                    manager.getAppWidgetIds(ComponentName(context, SkyScapeWidgetProvider::class.java)).forEach { id ->
                        val views = RemoteViews(context.packageName, R.layout.widget_skyscape)
                        views.setTextViewText(R.id.aqi, "--")
                        views.setTextViewText(R.id.co2, "-- ppm")
                        views.setTextViewText(R.id.aqi_detail, "Air data unavailable")
                        manager.partiallyUpdateAppWidget(id, views)
                    }
                }
            }
        }

        private fun renderAll(context: Context, json: JSONObject, stale: Boolean) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SkyScapeWidgetProvider::class.java))
            val current = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")
            val temp = current.getDouble("temperature_2m").roundToInt()
            val feels = current.getDouble("apparent_temperature").roundToInt()
            val code = current.getInt("weather_code")
            val high = daily.getJSONArray("temperature_2m_max").getDouble(0).roundToInt()
            val low = daily.getJSONArray("temperature_2m_min").getDouble(0).roundToInt()
            val time = SimpleDateFormat("EEE, d MMM · h:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }.format(Date())
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_skyscape)
                views.setTextViewText(R.id.location, "⌖ " + context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE).getString("city", "Bengaluru"))
                views.setTextViewText(R.id.updated, time + if (stale) " · cached" else " · live")
                views.setTextViewText(R.id.temperature, "${temp}°")
                views.setTextViewText(R.id.condition, condition(code))
                views.setTextViewText(R.id.weather_icon, icon(code, current.optInt("is_day", 1) == 1))
                views.setTextViewText(R.id.high_low, "H ${high}°  ·  L ${low}°")
                views.setTextViewText(R.id.feels_like, "Feels ${feels}°")
                views.setTextViewText(R.id.wind, "Wind ${current.getDouble("wind_speed_10m").roundToInt()} km/h")
                views.setTextViewText(R.id.humidity, "${current.getInt("relative_humidity_2m")}%")
                views.setTextViewText(R.id.uv, current.optDouble("uv_index", 0.0).roundToInt().toString())
                views.setTextViewText(R.id.pressure, "${current.getDouble("surface_pressure").roundToInt()} hPa")
                val row = RemoteViews(context.packageName, R.layout.widget_forecast_cell)
                views.removeAllViews(R.id.forecast_row)
                val dates = daily.getJSONArray("time")
                val codes = daily.getJSONArray("weather_code")
                val highs = daily.getJSONArray("temperature_2m_max")
                val lows = daily.getJSONArray("temperature_2m_min")
                for (i in 0 until minOf(7, dates.length())) {
                    val cell = RemoteViews(context.packageName, R.layout.widget_forecast_cell)
                    val dayLabel = if (i == 0) "Today" else try {
                        SimpleDateFormat("EEE", Locale.getDefault()).format(
                            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dates.getString(i))!!
                        )
                    } catch (_: Exception) { "Day" }
                    cell.setTextViewText(R.id.forecast_day, dayLabel)
                    cell.setTextViewText(R.id.forecast_icon, icon(codes.getInt(i), true))
                    cell.setTextViewText(R.id.forecast_high, "${highs.getDouble(i).roundToInt()}°")
                    cell.setTextViewText(R.id.forecast_low, "${lows.getDouble(i).roundToInt()}°")
                    views.addView(R.id.forecast_row, cell)
                }
                val refresh = Intent(context, SkyScapeWidgetProvider::class.java).setAction(ACTION_REFRESH)
                val pending = PendingIntent.getBroadcast(
                    context, id, refresh,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.refresh, pending)
                views.setOnClickPendingIntent(R.id.widget_root, pending)
                manager.updateAppWidget(id, views)
            }
        }

        private fun renderError(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, SkyScapeWidgetProvider::class.java)).forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_skyscape)
                views.setTextViewText(R.id.updated, "Weather unavailable · tap refresh")
                views.setTextViewText(R.id.condition, "Check internet connection")
                val intent = Intent(context, SkyScapeWidgetProvider::class.java).setAction(ACTION_REFRESH)
                views.setOnClickPendingIntent(R.id.refresh, PendingIntent.getBroadcast(
                    context, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getBroadcast(
                    context, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                manager.updateAppWidget(id, views)
            }
        }

        private fun condition(code: Int): String = when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Fog"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67 -> "Rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Weather update"
        }

        private fun icon(code: Int, isDay: Boolean): String = when (code) {
            0 -> if (isDay) "☀" else "☾"
            1, 2 -> if (isDay) "🌤" else "☁"
            3, 45, 48 -> "☁"
            in 51..67, in 80..82 -> "🌧"
            in 71..77, 85, 86 -> "❄"
            else -> "⛈"
        }
    }
}
