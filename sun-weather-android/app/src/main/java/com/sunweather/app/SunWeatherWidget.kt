package com.sunweather.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
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
        val size = LocalSize.current
        val tiny = size.width < 360.dp || size.height < 195.dp
        // Isolated production build trigger: 2026-10-08
        val large = size.width >= 500.dp

        val outerPad = when {
            tiny -> 4.dp
            large -> 9.dp
            else -> 7.dp
        }
        val topHeight = when {
            tiny -> 28.dp
            large -> 40.dp
            else -> 32.dp
        }
        val cardHeight = when {
            tiny -> 62.dp
            large -> 88.dp
            else -> 74.dp
        }
        val forecastHeight = when {
            tiny -> 70.dp
            large -> 140.dp
            else -> 88.dp
        }
        val clockWidth = when {
            tiny -> 68.dp
            large -> 145.dp
            else -> 72.dp
        }

        val clock = SimpleDateFormat("hh:mm\na", Locale.getDefault()).format(Date())

        Column(
            GlanceModifier.fillMaxSize().padding(outerPad),
            verticalAlignment = Alignment.Top
        ) {
            // Top reference pill.
            Row(
                GlanceModifier.fillMaxWidth().height(topHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    GlanceModifier
                        .background(cp(0xFF2CB9F5))
                        .cornerRadius(50.dp)
                        .padding(
                            horizontal = if (tiny) 11.dp else if (large) 18.dp else 14.dp,
                            vertical = 3.dp
                        )
                ) {
                    Text(
                        "Today",
                        style = TextStyle(
                            color = cp(0xFF07111A),
                            fontSize = if (tiny) 16.sp else if (large) 25.sp else 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    model.icon,
                    style = TextStyle(
                        fontSize = if (tiny) 25.sp else if (large) 43.sp else 33.sp
                    )
                )
            }

            Spacer(GlanceModifier.height(if (tiny) 2.dp else 4.dp))

            // Main weather card.
            Box(
                GlanceModifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .background(cp(0xF51A202B))
                    .cornerRadius(if (tiny) 21.dp else 27.dp)
                    .padding(
                        horizontal = if (tiny) 9.dp else if (large) 15.dp else 11.dp,
                        vertical = if (tiny) 4.dp else if (large) 6.dp else 4.dp
                    )
            ) {
                Row(
                    GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        GlanceModifier.defaultWeight().fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Today",
                            style = TextStyle(
                                color = cp(0xFFE8EDF3),
                                fontSize = if (tiny) 8.sp else if (large) 13.sp else 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            model.temperature.toString() + "°" + WeatherRepository.unit(context),
                            style = TextStyle(
                                color = cp(0xFFFFFFFF),
                                fontSize = if (tiny) 22.sp else if (large) 37.sp else 27.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            model.condition,
                            maxLines = 1,
                            style = TextStyle(
                                color = cp(0xFFF3F5F8),
                                fontSize = if (tiny) 8.sp else if (large) 13.sp else 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Real pill, not a rectangular button.
                    Box(
                        GlanceModifier
                            .width(if (tiny) 42.dp else if (large) 62.dp else 50.dp)
                            .height(if (tiny) 42.dp else if (large) 58.dp else 48.dp)
                            .cornerRadius(50.dp)
                            .background(cp(0xFF101820)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "🌤️",
                            style = TextStyle(
                                fontSize = if (tiny) 22.sp else if (large) 31.sp else 25.sp
                            )
                        )
                    }

                    Spacer(GlanceModifier.width(if (tiny) 4.dp else if (large) 8.dp else 6.dp))

                    Box(
                        GlanceModifier
                            .width(if (tiny) 91.dp else if (large) 140.dp else 112.dp)
                            .height(if (tiny) 42.dp else if (large) 58.dp else 48.dp)
                            .background(cp(0xFF54FF86))
                            .cornerRadius(50.dp)
                            .padding(1.dp)
                    ) {
                        Row(
                            GlanceModifier
                                .fillMaxSize()
                                .background(cp(0xFF063B20))
                                .cornerRadius(50.dp)
                                .padding(horizontal = if (tiny) 7.dp else 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                GlanceModifier.defaultWeight().fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    "Weather\nForecast",
                                    style = TextStyle(
                                        color = cp(0xFFF3FFF6),
                                        fontSize = if (tiny) 7.sp else if (large) 12.sp else 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(GlanceModifier.width(if (tiny) 4.dp else 7.dp))
                            Text(
                                "›",
                                style = TextStyle(
                                    color = cp(0xFF7CFF9E),
                                    fontSize = if (tiny) 17.sp else if (large) 22.sp else 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(GlanceModifier.height(if (tiny) 3.dp else if (large) 6.dp else 4.dp))

            // Lower row is intentionally non-overlapping so all three forecast pills
            // remain visible at every supported resize tier.
            Row(
                GlanceModifier.fillMaxWidth().height(forecastHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    GlanceModifier.width(clockWidth).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        clock,
                        style = TextStyle(
                            color = cp(0xFF080C12),
                            fontSize = if (tiny) 22.sp else if (large) 40.sp else 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(GlanceModifier.width(if (tiny) 2.dp else 4.dp))

                Row(
                    GlanceModifier.defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val colors = listOf(0xFFFF9800, 0xFF20AEE8, 0xFF9060E8)
                    val days = model.forecast.take(3)
                    repeat(3) { i ->
                        val day = days.getOrNull(i) ?: ForecastDay(
                            listOf("Mon", "Tue", "Wed")[i],
                            listOf("⛅", "🌦", "🌧")[i],
                            model.max,
                            model.min
                        )
                        Box(
                            GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                                .padding(horizontal = if (large) 2.dp else 1.dp)
                                .background(cp(colors[i]))
                                .cornerRadius(if (large) 23.dp else 18.dp)
                                .padding(vertical = if (large) 8.dp else 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    day.day,
                                    style = TextStyle(
                                        color = cp(0xFF111820),
                                        fontSize = if (tiny) 9.sp else if (large) 13.sp else 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    day.icon,
                                    style = TextStyle(
                                        fontSize = if (tiny) 18.sp else if (large) 27.sp else 20.sp
                                    )
                                )
                                Text(
                                    day.max.toString() + "°/" + day.min.toString() + "°" + WeatherRepository.unit(context),
                                    style = TextStyle(
                                        color = cp(0xFF111820),
                                        fontSize = if (tiny) 7.sp else if (large) 10.sp else 8.sp,
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
