package com.skyscape.weatherwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.graphics.Color
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

/**
 * Kept temporarily for existing app-side update calls while the home-screen
 * receiver is tested with native RemoteViews.
 */
class SkyScapeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text(
                text = "SkyScape Weather • Open app for forecast",
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color.rgb(16, 20, 28)))
                    .padding(16.dp),
                style = TextStyle(color = ColorProvider(Color.WHITE), fontSize = 14.sp)
            )
        }
    }
}

/** Native Android widget receiver used for the compatibility diagnostic. */
class SkyScapeWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.skyscape_widget_loading)
            views.setTextViewText(R.id.widget_message, "SkyScape native widget is working")
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
