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
import androidx.glance.appwidget.GlanceAppWidget
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

/** Kept for compatibility with existing app-side update calls. */
class SkyScapeWidget : GlanceAppWidget() {
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

/** Native, resizable home-screen widget with live weather, three-day outlook and app shortcuts. */
class SkyScapeWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> render(context, manager, id, WeatherRepository.cached(context)) }
        refreshAll(context, manager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SkyScapeWidgetReceiver::class.java))
            refreshAll(context, manager, ids)
        }
    }

    private fun refreshAll(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val snapshot = WeatherRepository.refresh(context)
            ids.forEach { id -> render(context, manager, id, snapshot) }
        }
    }

    private fun render(context: Context, manager: AppWidgetManager, id: Int, weather: WeatherSnapshot) {
        val views = RemoteViews(context.packageName, R.layout.skyscape_widget_loading)
        val now = SimpleDateFormat("EEE, d MMM  •  Bengaluru", Locale.US).format(Date())
        views.setTextViewText(R.id.widget_date, now.uppercase(Locale.US))
        views.setTextViewText(R.id.widget_temperature, if (weather.temperature >= 0) "${weather.temperature}°" else "--°")
        views.setTextViewText(R.id.widget_condition, "${weather.symbol}  ${weather.description}")
        views.setTextViewText(
            R.id.widget_range,
            if (weather.high >= 0 && weather.low >= 0) "HIGH ${weather.high}°   •   LOW ${weather.low}°" else "Tap widget to refresh"
        )
        val forecastIds = listOf(
            Triple(R.id.forecast_day_1, R.id.forecast_icon_1, R.id.forecast_temp_1),
            Triple(R.id.forecast_day_2, R.id.forecast_icon_2, R.id.forecast_temp_2),
            Triple(R.id.forecast_day_3, R.id.forecast_icon_3, R.id.forecast_temp_3)
        )
        forecastIds.forEachIndexed { index, ids ->
            val day = weather.forecast.getOrNull(index)
            views.setTextViewText(ids.first, day?.day ?: listOf("Today", "Tomorrow", "Day 3")[index])
            views.setTextViewText(ids.second, day?.symbol ?: "☁️")
            views.setTextViewText(ids.third, if (day != null && day.high >= 0 && day.low >= 0) "${day.high}° / ${day.low}°" else "--° / --°")
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
            // Keep the widget functional when the optional app is not installed.
            val fallback = PendingIntent.getBroadcast(context, requestCode,
                Intent(context, SkyScapeWidgetReceiver::class.java).setAction(ACTION_REFRESH),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(viewId, fallback)
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.skyscape.weatherwidget.ACTION_REFRESH"
    }
}
