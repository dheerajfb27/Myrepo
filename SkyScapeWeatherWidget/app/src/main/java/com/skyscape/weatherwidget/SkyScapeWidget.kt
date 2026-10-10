package com.skyscape.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
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

/** Native RemoteViews provider. Fetching is asynchronous so onUpdate never blocks the launcher. */
class SkyScapeWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> render(context, manager, id, WeatherRepository.cached(context)) }
        refreshAll(context, manager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(android.content.ComponentName(context, SkyScapeWidgetReceiver::class.java))
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
        views.setTextViewText(R.id.widget_location, weather.location)
        views.setTextViewText(R.id.widget_temperature, if (weather.temperature >= 0) "${weather.temperature}°" else "--°")
        views.setTextViewText(R.id.widget_condition, "${weather.symbol}  ${weather.description}")
        views.setTextViewText(
            R.id.widget_range,
            if (weather.high >= 0 && weather.low >= 0) "H ${weather.high}°   •   L ${weather.low}°" else "Tap to refresh forecast"
        )
        val refreshIntent = Intent(context, SkyScapeWidgetReceiver::class.java).setAction(ACTION_REFRESH)
        val pending = PendingIntent.getBroadcast(
            context, id, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, pending)
        views.setOnClickPendingIntent(R.id.widget_temperature, pending)
        manager.updateAppWidget(id, views)
    }

    companion object {
        private const val ACTION_REFRESH = "com.skyscape.weatherwidget.ACTION_REFRESH"
    }
}
