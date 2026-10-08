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

        Box(GlanceModifier.fillMaxSize().padding(pad)) {
            Column(GlanceModifier.fillMaxSize()) {

                // Exact reference: blue Today pill and weather icon.
                Row(
                    GlanceModifier.fillMaxWidth()
                        .height(if (tiny) 30.dp else if (large) 44.dp else 36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        GlanceModifier
                            .background(cp(0xFF2CB9F5))
                            .cornerRadius(26.dp)
                            .padding(
                                horizontal = if (tiny) 12.dp else if (large) 19.dp else 15.dp,
                                vertical = if (tiny) 4.dp else if (large) 7.dp else 5.dp
                            )
                    ) {
                        Text(
                            "Today",
                            style = TextStyle(
                                color = cp(0xFF07111A),
                                fontSize = if (tiny) 18.sp else if (large) 28.sp else 23.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        model.icon,
                        style = TextStyle(
                            fontSize = if (tiny) 28.sp else if (large) 47.sp else 37.sp
                        )
                    )
                }

                Spacer(GlanceModifier.height(if (tiny) 2.dp else 4.dp))

                // Reference composition: card is the top layer; clock and forecast cards
                // sit in the lower layer and visually tuck underneath/alongside it.
                Box(
                    GlanceModifier.fillMaxWidth().defaultWeight()
                ) {
                    Box(
                        GlanceModifier
                            .fillMaxWidth()
                            .height(if (tiny) 70.dp else if (large) 110.dp else 90.dp)
                            .background(cp(0xF51A202B))
                            .cornerRadius(if (tiny) 23.dp else 30.dp)
                            .padding(
                                horizontal = if (tiny) 10.dp else if (large) 16.dp else 12.dp,
                                vertical = if (tiny) 6.dp else if (large) 11.dp else 8.dp
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
                                        color = cp(0xFFE8EDF3),
                                        fontSize = if (tiny) 8.sp else if (large) 14.sp else 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    model.temperature.toString() + "°" + model.min.toString() + "°",
                                    style = TextStyle(
                                        color = cp(0xFFFFFFFF),
                                        fontSize = if (tiny) 24.sp else if (large) 40.sp else 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    model.condition,
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = cp(0xFFF3F5F8),
                                        fontSize = if (tiny) 9.sp else if (large) 14.sp else 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Box(
                                GlanceModifier
                                    .width(if (tiny) 94.dp else if (large) 145.dp else 118.dp)
                                    .background(cp(0xFF54FF86))
                                    .cornerRadius(26.dp)
                                    .padding(1.dp)
                            ) {
                                Row(
                                    GlanceModifier
                                        .fillMaxWidth()
                                        .background(cp(0xFF063B20))
                                        .cornerRadius(25.dp)
                                        .padding(
                                            horizontal = if (tiny) 8.dp else if (large) 14.dp else 10.dp,
                                            vertical = if (tiny) 5.dp else if (large) 8.dp else 6.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Weather\nForecast",
                                        style = TextStyle(
                                            color = cp(0xFFF3FFF6),
                                            fontSize = if (tiny) 8.sp else if (large) 13.sp else 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(GlanceModifier.defaultWeight())
                                    Text(
                                        "›",
                                        style = TextStyle(
                                            color = cp(0xFF7CFF9E),
                                            fontSize = if (tiny) 17.sp else if (large) 23.sp else 19.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Lower layer: clock left, three tall cards right.
                    if (!tiny) {
                        Row(
                            GlanceModifier
                                .fillMaxWidth()
                                .padding(top = if (large) 94.dp else 76.dp)
                                .height(if (large) 142.dp else 118.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                GlanceModifier
                                    .width(if (large) 185.dp else 145.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    clock,
                                    style = TextStyle(
                                        color = cp(0xFF080C12),
                                        fontSize = if (large) 43.sp else 36.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Row(
                                GlanceModifier
                                    .fillMaxHeight()
                                    .defaultWeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                model.forecast.take(3).forEachIndexed { i, day ->
                                    val colors = listOf(0xFFFF9800, 0xFF20AEE8, 0xFF9060E8)
                                    Box(
                                        GlanceModifier
                                            .defaultWeight()
                                            .fillMaxHeight()
                                            .padding(horizontal = if (large) 2.dp else 1.dp)
                                            .background(cp(colors[i]))
                                            .cornerRadius(if (large) 25.dp else 20.dp)
                                            .padding(
                                                vertical = if (large) 9.dp else 7.dp,
                                                horizontal = 1.dp
                                            )
                                    ) {
                                        Column(
                                            GlanceModifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(day.day, style = TextStyle(
                                                color = cp(0xFF111820),
                                                fontSize = if (large) 13.sp else 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ))
                                            Text(day.icon, style = TextStyle(
                                                fontSize = if (large) 28.sp else 21.sp
                                            ))
                                            Text(day.max.toString() + "°/" + day.min.toString() + "°",
                                                style = TextStyle(
                                                    color = cp(0xFF111820),
                                                    fontSize = if (large) 10.sp else 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                ))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (tiny && shortcuts.isNotEmpty()) {
                    Spacer(GlanceModifier.height(3.dp))
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
