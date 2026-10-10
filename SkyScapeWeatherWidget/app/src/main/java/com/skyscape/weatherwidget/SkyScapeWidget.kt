package com.skyscape.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Compatibility Glance widget. The visible feature-rich widget is the native RemoteViews receiver below. */
class SkyScapeWidget : androidx.glance.appwidget.GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text(
                text = "SkyScape Weather • Open app for forecast",
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(Color.rgb(16, 20, 28)))
                    .padding(16.dp),
                style = TextStyle(color = ColorProvider(Color.WHITE), fontSize = 14.sp)
            )
        }
    }
}

class SkyScapeWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> render(context, manager, id, WeatherRepository.cached(context), false) }
        context.sendBroadcast(Intent(context, SkyScapeWidgetReceiver::class.java).setAction(ACTION_REFRESH))
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        if (intent.action == ACTION_RENDER_CACHED) {
            renderAll(appContext, WeatherRepository.cached(appContext))
            return
        }
        if (intent.action != ACTION_REFRESH) {
            super.onReceive(context, intent)
            return
        }
        val pending = goAsync()
        val manager = AppWidgetManager.getInstance(appContext)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val snapshot = runCatching { WeatherRepository.refresh(appContext) }
                    .getOrElse { WeatherRepository.cached(appContext) }
                val ids = runCatching {
                    manager.getAppWidgetIds(ComponentName(appContext, SkyScapeWidgetReceiver::class.java))
                }.getOrDefault(intArrayOf())
                ids.forEach { id -> runCatching { render(appContext, manager, id, snapshot, false) } }
            } finally {
                pending.finish()
            }
        }
    }

    private fun renderAll(context: Context, weather: WeatherSnapshot) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = runCatching {
            manager.getAppWidgetIds(ComponentName(context, SkyScapeWidgetReceiver::class.java))
        }.getOrDefault(intArrayOf())
        ids.forEach { id -> runCatching { render(context, manager, id, weather, false) } }
    }

    private fun render(context: Context, manager: AppWidgetManager, id: Int, weather: WeatherSnapshot, loading: Boolean) {
        val views = RemoteViews(context.packageName, R.layout.skyscape_widget_loading)
        val now = SimpleDateFormat("EEE, d MMM  •  Bengaluru", Locale.US).format(Date())
        views.setTextViewText(R.id.widget_date, now.uppercase(Locale.US))
        views.setTextViewText(R.id.widget_temperature, if (weather.temperature >= 0) "${weather.temperature}°" else "--°")
        views.setTextViewText(R.id.widget_condition, if (weather.temperature >= 0) "${weather.symbol}  ${weather.description}" else if (loading) "Connecting to weather…" else "Weather unavailable • tap to retry")
        views.setTextViewText(R.id.widget_range,
            if (weather.high >= 0 && weather.low >= 0) "HIGH ${weather.high}°   •   LOW ${weather.low}°" else if (loading) "Fetching current conditions…" else "Check internet and tap to retry")

        val forecastIds = listOf(
            Triple(R.id.forecast_day_1, R.id.forecast_icon_1, R.id.forecast_temp_1),
            Triple(R.id.forecast_day_2, R.id.forecast_icon_2, R.id.forecast_temp_2),
            Triple(R.id.forecast_day_3, R.id.forecast_icon_3, R.id.forecast_temp_3),
            Triple(R.id.forecast_day_4, R.id.forecast_icon_4, R.id.forecast_temp_4),
            Triple(R.id.forecast_day_5, R.id.forecast_icon_5, R.id.forecast_temp_5)
        )
        val fallbackDays = listOf("Today", "Tmrw", "Day 3", "Day 4", "Day 5")
        forecastIds.forEachIndexed { index, ids ->
            val day = weather.forecast.getOrNull(index)
            views.setTextViewText(ids.first, day?.day ?: fallbackDays[index])
            views.setTextViewText(ids.second, day?.symbol ?: "☁️")
            views.setTextViewText(ids.third, if (day != null && day.high >= 0 && day.low >= 0) "${day.high}°/${day.low}°" else "--°/--°")
        }

        val refreshIntent = Intent(context, SkyScapeWidgetReceiver::class.java).setAction(ACTION_REFRESH)
        val refreshPending = PendingIntent.getBroadcast(context, id, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPending)
        views.setOnClickPendingIntent(R.id.widget_temperature, refreshPending)
        bindActivityShortcut(context, views, R.id.shortcut_phone, 1001, Intent(Intent.ACTION_DIAL))
        bindActivityShortcut(context, views, R.id.shortcut_messages, 1002, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")))
        bindActivityShortcut(context, views, R.id.shortcut_camera, 1003, Intent("android.media.action.IMAGE_CAPTURE"))
        bindPackageShortcut(context, views, R.id.shortcut_telegram, 1004, "org.telegram.messenger")
        bindPackageShortcut(context, views, R.id.shortcut_spotify, 1005, "com.spotify.music")
        manager.updateAppWidget(id, views)
    }

    private fun bindActivityShortcut(context: Context, views: RemoteViews, viewId: Int, requestCode: Int, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pending = PendingIntent.getActivity(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(viewId, pending)
    }

    private fun bindPackageShortcut(context: Context, views: RemoteViews, viewId: Int, requestCode: Int, packageName: String) {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pending = PendingIntent.getActivity(context, requestCode, launch,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(viewId, pending)
        } else {
            val fallback = PendingIntent.getBroadcast(context, requestCode,
                Intent(context, SkyScapeWidgetReceiver::class.java).setAction(ACTION_REFRESH),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(viewId, fallback)
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.skyscape.weatherwidget.ACTION_REFRESH"
        private const val ACTION_RENDER_CACHED = "com.skyscape.weatherwidget.ACTION_RENDER_CACHED"
    }
}
