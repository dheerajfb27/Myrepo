package com.sunweather.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

private fun cp(hex: Long) = ColorProvider(Color(hex), Color(hex))

class SunWeatherWidget : GlanceAppWidget() {
    // Android launcher controls the final size. These breakpoints make the
    // approved design adapt cleanly while the user drags the resize handles.
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(320.dp, 180.dp), // Small
            DpSize(420.dp, 220.dp), // Medium
            DpSize(520.dp, 300.dp), // Large
            DpSize(640.dp, 360.dp)  // Extra large
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
        val width = LocalSize.current.width
        val height = LocalSize.current.height
        val compact = width < 380.dp
        val small = height < 205.dp
        val large = width >= 500.dp
        val ultra = width < 340.dp || height < 190.dp

        Box(
            GlanceModifier
                .fillMaxSize()
                .cornerRadius(30.dp)
                .padding(if (compact) 6.dp else 9.dp)
        ) {
            Column(
                GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clean transparent outer area: no wallpaper/background image.
                Row(
                    GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Today",
                        style = TextStyle(
                            color = cp(0xFF111827),
                            fontSize = when {
                                ultra -> 20.sp
                                large -> 31.sp
                                else -> 27.sp
                            },
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        model.icon,
                        style = TextStyle(
                            fontSize = when {
                                ultra -> 25.sp
                                large -> 43.sp
                                else -> 36.sp
                            }
                        )
                    )
                }

                if (!small) {
                    Spacer(GlanceModifier.height(if (compact) 5.dp else 8.dp))
                }

                Box(
                    GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .background(cp(0xF51A202B))
                        .cornerRadius(28.dp)
                        .padding(
                            horizontal = if (compact) 10.dp else if (large) 18.dp else 14.dp,
                            vertical = if (ultra) 7.dp else if (large) 13.dp else 10.dp
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
                                    color = cp(0xFFE9EEF5),
                                    fontSize = if (compact) 10.sp else 12.sp
                                )
                            )
                            Text(
                                model.temperature.toString() + "°" + model.min.toString() + "°",
                                style = TextStyle(
                                    color = cp(0xFFFFFFFF),
                                    fontSize = when {
                                        ultra -> 25.sp
                                        large -> 40.sp
                                        else -> 33.sp
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                model.condition,
                                maxLines = 1,
                                style = TextStyle(
                                    color = cp(0xFFF2F5F8),
                                    fontSize = if (compact) 10.sp else 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        if (!ultra) {
                            Box(
                                GlanceModifier
                                    .background(cp(0xFF54FF86))
                                    .cornerRadius(25.dp)
                                    .padding(1.dp)
                            ) {
                                Box(
                                    GlanceModifier
                                        .background(cp(0xFF063B20))
                                        .cornerRadius(24.dp)
                                        .padding(
                                            horizontal = if (compact) 9.dp else if (large) 18.dp else 13.dp,
                                            vertical = if (compact) 7.dp else 10.dp
                                        )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Weather\nForecast",
                                            style = TextStyle(
                                                color = cp(0xFFF2FFF6),
                                                fontSize = if (compact) 9.sp else if (large) 13.sp else 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(GlanceModifier.width(6.dp))
                                        Text(
                                            "›",
                                            style = TextStyle(
                                                color = cp(0xFF7CFF9E),
                                                fontSize = if (compact) 18.sp else 23.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (!small) {
                    Spacer(GlanceModifier.height(if (compact) 5.dp else 8.dp))

                    Row(
                        GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        model.forecast.take(3).forEachIndexed { index, day ->
                            val colors = listOf(0xFFF58A00, 0xFF23A8CF, 0xFF8060DD)
                            Box(
                                GlanceModifier
                                    .defaultWeight()
                                    .padding(horizontal = if (ultra) 2.dp else 3.dp)
                                    .background(cp(colors[index]))
                                    .cornerRadius(25.dp)
                                    .padding(
                                        vertical = if (compact) 5.dp else if (large) 11.dp else 8.dp,
                                        horizontal = 2.dp
                                    )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = GlanceModifier.fillMaxWidth()
                                ) {
                                    Text(
                                        day.day,
                                        style = TextStyle(
                                            color = cp(0xFF111820),
                                            fontSize = if (ultra) 8.sp else if (large) 13.sp else 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        day.icon,
                                        style = TextStyle(fontSize = if (ultra) 15.sp else if (large) 27.sp else 22.sp)
                                    )
                                    Text(
                                        day.max.toString() + "°/" + day.min.toString() + "°",
                                        style = TextStyle(
                                            color = cp(0xFF111820),
                                            fontSize = if (ultra) 7.sp else if (large) 11.sp else 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (!ultra && height >= 270.dp) {
                    Spacer(GlanceModifier.height(6.dp))
                    Row(
                        GlanceModifier
                            .fillMaxWidth()
                            .background(cp(0xE91C2230))
                            .cornerRadius(25.dp)
                            .padding(horizontal = 5.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val labels = mapOf(
                            "phone" to "☎",
                            "messages" to "✉",
                            "camera" to "◉",
                            "twitter" to "𝕏",
                            "telegram" to "➤",
                            "spotify" to "♪"
                        )
                        shortcuts.take(6).forEach { key ->
                            Box(
                                GlanceModifier
                                    .defaultWeight()
                                    .size(if (large) 44.dp else 38.dp)
                                    .clickable(
                                        actionStartActivity<ShortcutActivity>(
                                            actionParametersOf(ShortcutActivity.ACTION_KEY to key)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    labels[key] ?: "•",
                                    style = TextStyle(
                                        color = cp(0xFFFFFFFF),
                                        fontSize = if (large) 20.sp else 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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
