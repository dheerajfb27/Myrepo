package com.dheeraj.skyscape

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.roundToInt

open class CompactWeatherWidgetProvider : AppWidgetProvider() {
    protected open val greenGlow: Boolean = false
    protected open val widgetTitle: String = "Weather Mini"

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> renderLoading(context, manager, id) }
        fetchAndRender(context.applicationContext)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            widgetIds(context, manager).forEach { renderLoading(context, manager, it) }
            fetchAndRender(context.applicationContext)
        }
    }

    private fun widgetIds(context: Context, manager: AppWidgetManager): IntArray =
        manager.getAppWidgetIds(ComponentName(context, javaClass))

    private fun skinViews(context: Context, views: RemoteViews) {
        // The 4x1 widgets use the same saved skin selection as the full-size widget.
        val skinName = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
            .getString("skin", "coffee")
        val skin = when (skinName) {
            "glass_green" -> CompactSkin(R.drawable.widget_glass_green_background,
                Color.rgb(240,255,232), Color.rgb(154,255,87), Color.rgb(168,217,154))
            "amoled" -> CompactSkin(R.drawable.widget_glass_amoled,
                Color.rgb(239,246,255), Color.rgb(53,230,255), Color.rgb(156,180,207))
            "arctic" -> CompactSkin(R.drawable.widget_glass_arctic,
                Color.rgb(231,249,255), Color.rgb(139,233,255), Color.rgb(164,207,224))
            "sunset" -> CompactSkin(R.drawable.widget_glass_sunset,
                Color.rgb(255,239,230), Color.rgb(255,155,113), Color.rgb(232,177,190))
            "rose" -> CompactSkin(R.drawable.widget_glass_rose,
                Color.rgb(255,239,249), Color.rgb(255,139,203), Color.rgb(224,174,207))
            else -> CompactSkin(R.drawable.widget_glass_background,
                Color.rgb(232,216,196), Color.rgb(212,163,115), Color.rgb(188,162,140))
        }
        views.setInt(R.id.compact_root, "setBackgroundResource", skin.background)
        views.setTextColor(R.id.compact_city, skin.accent)
        views.setTextColor(R.id.compact_temp, skin.primary)
        views.setTextColor(R.id.compact_icon, skin.accent)
        views.setTextColor(R.id.compact_refresh, skin.accent)
        views.setTextColor(R.id.compact_message, skin.accent)
        views.setTextColor(R.id.compact_date, skin.muted)
        views.setTextColor(R.id.compact_feels, skin.primary)
        views.setTextColor(R.id.compact_wind, skin.accent)
        views.setTextColor(R.id.compact_high_low, skin.muted)
    }

    private data class CompactSkin(
        val background: Int,
        val primary: Int,
        val accent: Int,
        val muted: Int
    )

    private fun pending(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, javaClass).setAction(ACTION_REFRESH)
        return PendingIntent.getBroadcast(context, id + if (greenGlow) 5000 else 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun renderLoading(context: Context, manager: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather_compact)
        skinViews(context, views)
        views.setTextViewText(R.id.compact_city, widgetTitle)
        val click = pending(context, id)
        views.setOnClickPendingIntent(R.id.compact_refresh, click)
        views.setOnClickPendingIntent(R.id.compact_root, click)
        manager.updateAppWidget(id, views)
    }

    private fun fetchAndRender(context: Context) {
        executor.execute {
            val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
            try {
                val lat = prefs.getFloat("latitude", 12.9716f)
                val lon = prefs.getFloat("longitude", 77.5946f)
                val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,apparent_temperature,is_day,weather_code,wind_speed_10m" +
                    "&daily=temperature_2m_max,temperature_2m_min&timezone=Asia%2FKolkata&forecast_days=1"
                val connection = URL(endpoint).openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val response = try {
                    if (connection.responseCode !in 200..299) error("Weather HTTP ${connection.responseCode}")
                    connection.inputStream.bufferedReader().use { it.readText() }
                } finally { connection.disconnect() }
                prefs.edit().putString("compact_last_response", response).apply()
                render(context, JSONObject(response), false)
            } catch (_: Exception) {
                val cached = prefs.getString("compact_last_response", null)
                if (cached != null) {
                    try { render(context, JSONObject(cached), true) } catch (_: Exception) { renderFailure(context) }
                } else renderFailure(context)
            }
        }
    }

    private fun render(context: Context, json: JSONObject, stale: Boolean) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = widgetIds(context, manager)
        val current = json.getJSONObject("current")
        val daily = json.getJSONObject("daily")
        val code = current.optInt("weather_code", -1)
        val city = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
            .getString("city", "Bengaluru") ?: "Bengaluru"
        val temp = current.optDouble("temperature_2m", Double.NaN)
        val feels = current.optDouble("apparent_temperature", Double.NaN)
        val wind = current.optDouble("wind_speed_10m", Double.NaN)
        val high = daily?.optJSONArray("temperature_2m_max")?.optDouble(0, Double.NaN) ?: Double.NaN
        val low = daily?.optJSONArray("temperature_2m_min")?.optDouble(0, Double.NaN) ?: Double.NaN
        val date = java.text.SimpleDateFormat("EEE, d MMM", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
        }.format(java.util.Date())
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_weather_compact)
            skinViews(context, views)
            views.setTextViewText(R.id.compact_city, "⌖ $city")
            views.setTextViewText(R.id.compact_date, date + if (stale) " · cached" else "")
            views.setTextViewText(R.id.compact_message, weatherMessage(code, temp, wind, current.optInt("is_day", 1) == 1))
            views.setTextViewText(R.id.compact_temp, "${if (temp.isNaN()) "--" else temp.roundToInt().toString()}°")
            views.setTextViewText(R.id.compact_feels, "Feels ${if (feels.isNaN()) "--" else feels.roundToInt().toString()}°")
            views.setTextViewText(R.id.compact_wind, "Wind ${if (wind.isNaN()) "--" else wind.roundToInt().toString()} km/h")
            views.setTextViewText(R.id.compact_high_low, "H ${if (high.isNaN()) "--" else high.roundToInt().toString()}° · L ${if (low.isNaN()) "--" else low.roundToInt().toString()}°")
            views.setTextViewText(R.id.compact_icon, weatherIcon(code, current.optInt("is_day", 1) == 1))
            val click = pending(context, id)
            views.setOnClickPendingIntent(R.id.compact_refresh, click)
            views.setOnClickPendingIntent(R.id.compact_root, click)
            manager.updateAppWidget(id, views)
        }
    }

    private fun renderFailure(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        widgetIds(context, manager).forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_weather_compact)
            skinViews(context, views)
            views.setTextViewText(R.id.compact_city, widgetTitle)
            val click = pending(context, id)
            views.setOnClickPendingIntent(R.id.compact_refresh, click)
            views.setOnClickPendingIntent(R.id.compact_root, click)
            manager.updateAppWidget(id, views)
        }
    }

    private fun weatherMessage(code: Int, temp: Double, wind: Double, isDay: Boolean): String = when {
        code in 51..67 || code in 80..82 || code in 95..99 -> "Raining nearby"
        !isDay && !wind.isNaN() && wind >= 20 -> "Windy night"
        !temp.isNaN() && temp >= 32 -> if (isDay) "Today is hot" else "Warm night"
        !isDay && !temp.isNaN() && temp <= 20 -> "Cool, peaceful night"
        code == 0 || code == 1 || code == 2 -> if (!temp.isNaN() && temp in 20.0..29.0 && (wind.isNaN() || wind < 20)) "Lovely weather" else if (isDay) "A pleasant day ahead" else "Calm night skies"
        code == 3 || code == 45 || code == 48 -> "Cloudy skies today"
        !wind.isNaN() && wind >= 20 -> "Breezy weather"
        else -> "Have a lovely day"
    }

    private fun condition(code: Int) = when (code) {
        0 -> "Clear sky"; 1 -> "Mainly clear"; 2 -> "Partly cloudy"; 3 -> "Overcast"
        45,48 -> "Fog"; 51,53,55,56,57 -> "Drizzle"; 61,63,65,66,67 -> "Rain"
        71,73,75,77 -> "Snow"; 80,81,82 -> "Rain showers"; 85,86 -> "Snow showers"
        95,96,99 -> "Thunderstorm"; else -> "Current weather"
    }

    private fun weatherIcon(code: Int, day: Boolean) = when (code) {
        0 -> if (day) "☀" else "☾"
        1,2 -> if (day) "🌤" else "☁"
        3,45,48 -> "☁"
        in 51..67, in 80..82 -> "🌧"
        in 71..77,85,86 -> "❄"
        else -> "⛈"
    }

    companion object {
        private const val ACTION_REFRESH = "com.dheeraj.skyscape.COMPACT_REFRESH"
        private val executor = Executors.newCachedThreadPool()
    }
}

class ParrotGlowMiniWidgetProvider : CompactWeatherWidgetProvider() {
    override val greenGlow: Boolean = true
    override val widgetTitle: String = "Parrot Glow"
}

class GlassGreenMiniWidgetProvider : CompactWeatherWidgetProvider() {
    override val greenGlow: Boolean = true
    override val widgetTitle: String = "Glass Green"
}
