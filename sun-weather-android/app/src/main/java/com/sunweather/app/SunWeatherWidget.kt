package com.sunweather.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun cp(hex: Long) = ColorProvider(Color(hex), Color(hex))

class SunWeatherWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(320.dp, 180.dp),
            DpSize(420.dp, 220.dp),
            DpSize(520.dp, 300.dp),
            DpSize(640.dp, 360.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }

    @Composable
    private fun WidgetContent() {
        val context = LocalContext.current
        val model = WeatherRepository.loadModel(context)
        val shortcuts = WeatherRepository.shortcuts(context)
        val w = LocalSize.current.width
        val h = LocalSize.current.height

        val tiny = w < 360.dp || h < 195.dp
        val medium = w < 500.dp
        val large = w >= 500.dp
        val pad = if (tiny) 4.dp else if (medium) 7.dp else 9.dp

        val clock = SimpleDateFormat("hh:mm\na", Locale.getDefault()).format(Date())

        Box(
            GlanceModifier.fillMaxSize().padding(pad)
        ) {
            Column(GlanceModifier.fillMaxSize()) {

                // TOP: pill and weather icon share the same left/right edges as the card below.
                Row(
                    GlanceModifier.fillMaxWidth().height(if (tiny) 27.dp else if (large) 43.dp else 35.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        GlanceModifier
                            .background(cp(0xFF2BB8F4))
                            .cornerRadius(24.dp)
                            .padding(
                                horizontal = if (tiny) 11.dp else if (large) 18.dp else 14.dp,
                                vertical = if (tiny) 3.dp else if (large) 7.dp else 5.dp
                            )
                    ) {
                        Text(
                            SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date()),
                            style = TextStyle(
                                color = cp(0xFF07111A),
                                fontSize = if (tiny) 17.sp else if (large) 27.sp else 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        model.icon,
                        style = TextStyle(
                            fontSize = if (tiny) 25.sp else if (large) 44.sp else 35.sp
                        )
                    )
                }

                Spacer(GlanceModifier.height(if (tiny) 2.dp else 5.dp))

                // MAIN CARD: fixed proportions prevent text/button collisions.
                Box(
                    GlanceModifier
                        .fillMaxWidth()
                        .height(if (tiny) 68.dp else if (large) 108.dp else 88.dp)
                        .background(cp(0xF51A202B))
                        .cornerRadius(if (tiny) 23.dp else 30.dp)
                        .padding(
                            horizontal = if (tiny) 9.dp else if (large) 16.dp else 12.dp,
                            vertical = if (tiny) 5.dp else if (large) 11.dp else 8.dp
                        )
                ) {
                    Row(
                        GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(GlanceModifier.defaultWeight()) {
                            Text(
                                "Today",
                                style = TextStyle(
                                    color = cp(0xFFE7ECF2),
                                    fontSize = if (tiny) 8.sp else if (large) 13.sp else 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                model.temperature.toString() + "°" + model.min.toString() + "°",
                                style = TextStyle(
                                    color = cp(0xFFFFFFFF),
                                    fontSize = if (tiny) 23.sp else if (large) 39.sp else 31.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                model.condition,
                                maxLines = 1,
                                style = TextStyle(
                                    color = cp(0xFFF2F5F8),
                                    fontSize = if (tiny) 8.sp else if (large) 14.sp else 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Box(
                            GlanceModifier
                                .width(if (tiny) 92.dp else if (large) 142.dp else 116.dp)
                                .background(cp(0xFF54FF86))
                                .cornerRadius(25.dp)
                                .padding(1.dp)
                        ) {
                            Box(
                                GlanceModifier
                                    .fillMaxWidth()
                                    .background(cp(0xFF063B20))
                                    .cornerRadius(24.dp)
                                    .padding(
                                        horizontal = if (tiny) 7.dp else if (large) 13.dp else 10.dp,
                                        vertical = if (tiny) 4.dp else 7.dp
                                    )
                            ) {
                                Row(
                                    GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Weather\nForecast",
                                        style = TextStyle(
                                            color = cp(0xFFF2FFF6),
                                            fontSize = if (tiny) 8.sp else if (large) 12.sp else 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(GlanceModifier.defaultWeight())
                                    Text(
                                        "›",
                                        style = TextStyle(
                                            color = cp(0xFF7CFF9E),
                                            fontSize = if (tiny) 16.sp else 21.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(GlanceModifier.height(if (tiny) 2.dp else 5.dp))

                // BOTTOM: clock gets a stable left column; forecast cards get a stable right column.
                Row(
                    GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        GlanceModifier
                            .defaultWeight()
                            .padding(start = if (tiny) 0.dp else 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            clock,
                            style = TextStyle(
                                color = cp(0xFF080C12),
                                fontSize = if (tiny) 27.sp else if (large) 43.sp else 37.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Row(
                        GlanceModifier.defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        model.forecast.take(3).forEachIndexed { i, day ->
                            val colors = listOf(0xFFFF9800, 0xFF20AEE8, 0xFF9060E8)
                            Box(
                                GlanceModifier
                                    .defaultWeight()
                                    .padding(horizontal = if (tiny) 1.dp else 2.dp)
                                    .background(cp(colors[i]))
                                    .cornerRadius(if (tiny) 17.dp else 24.dp)
                                    .padding(
                                        vertical = if (tiny) 4.dp else if (large) 9.dp else 6.dp,
                                        horizontal = 1.dp
                                    )
                            ) {
                                Column(
                                    GlanceModifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        day.day,
                                        style = TextStyle(
                                            color = cp(0xFF10151B),
                                            fontSize = if (tiny) 7.sp else if (large) 13.sp else 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        day.icon,
                                        style = TextStyle(
                                            fontSize = if (tiny) 13.sp else if (large) 26.sp else 20.sp
                                        )
                                    )
                                    Text(
                                        day.max.toString() + "°/" + day.min.toString() + "°",
                                        style = TextStyle(
                                            color = cp(0xFF10151B),
                                            fontSize = if (tiny) 6.sp else if (large) 10.sp else 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (h >= 330.dp && shortcuts.isNotEmpty()) {
                    Spacer(GlanceModifier.height(4.dp))
                    Row(
                        GlanceModifier.fillMaxWidth()
                            .background(cp(0xE91C2230))
                            .cornerRadius(22.dp)
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val labels = mapOf("phone" to "☎", "messages" to "✉", "camera" to "◉", "twitter" to "𝕏", "telegram" to "➤", "spotify" to "♪")
                        shortcuts.take(6).forEach { key ->
                            Box(
                                GlanceModifier.defaultWeight()
                                    .size(if (large) 42.dp else 35.dp)
                                    .clickable(actionStartActivity<ShortcutActivity>(
                                        actionParametersOf(ShortcutActivity.ACTION_KEY to key)
                                    )),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    labels[key] ?: "•",
                                    style = TextStyle(color = cp(0xFFFFFFFF), fontSize = if (large) 20.sp else 17.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class SunWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SunWeatherWidget()
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWorker.schedule(context)
        WeatherWorker.enqueueNow(context)
    }
}
