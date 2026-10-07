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
    override val sizeMode = SizeMode.Responsive(
        setOf(
            androidx.compose.ui.unit.DpSize(320.dp, 220.dp),
            androidx.compose.ui.unit.DpSize(420.dp, 300.dp),
            androidx.compose.ui.unit.DpSize(520.dp, 360.dp)
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
        val compact = LocalSize.current.width < 380.dp
        val ultraCompact = LocalSize.current.width < 340.dp

        Box(GlanceModifier.fillMaxSize().cornerRadius(28.dp)) {
            Image(
                provider = ImageProvider(R.drawable.weather_scene),
                contentDescription = "Sunrise road weather background",
                modifier = GlanceModifier.fillMaxSize()
            )

            Column(
                GlanceModifier.fillMaxSize().padding(if (compact) 11.dp else 15.dp)
            ) {
                Row(
                    GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(GlanceModifier.defaultWeight()) {
                        Text(
                            "Today",
                            style = TextStyle(
                                color = cp(0xFF101820),
                                fontSize = if (compact) 23.sp else 29.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            model.date,
                            maxLines = 1,
                            style = TextStyle(
                                color = cp(0xFF172027),
                                fontSize = if (compact) 9.sp else 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        model.icon,
                        style = TextStyle(fontSize = if (compact) 30.sp else 38.sp)
                    )
                }

                Spacer(GlanceModifier.height(if (compact) 5.dp else 9.dp))

                Box(
                    GlanceModifier
                        .fillMaxWidth()
                        .background(cp(0xE81B2027))
                        .cornerRadius(26.dp)
                        .padding(
                            horizontal = if (compact) 11.dp else 16.dp,
                            vertical = if (compact) 9.dp else 13.dp
                        )
                ) {
                    Row(
                        GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(GlanceModifier.defaultWeight()) {
                            Text(
                                model.time,
                                style = TextStyle(
                                    color = cp(0xFFF5F7F8),
                                    fontSize = if (compact) 10.sp else 12.sp
                                )
                            )
                            Text(
                                model.temperature.toString() + "°/" + model.min.toString() + "°",
                                style = TextStyle(
                                    color = cp(0xFFFFFFFF),
                                    fontSize = if (compact) 28.sp else 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                model.condition,
                                maxLines = 1,
                                style = TextStyle(
                                    color = cp(0xFFF1F5F8),
                                    fontSize = if (compact) 11.sp else 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            "Weather\nForecast",
                            style = TextStyle(
                                color = cp(0xFF20242A),
                                fontSize = if (compact) 9.sp else 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = GlanceModifier
                                .background(cp(0xFFF4F6F7))
                                .cornerRadius(24.dp)
                                .padding(
                                    horizontal = if (compact) 9.dp else 13.dp,
                                    vertical = if (compact) 7.dp else 10.dp
                                )
                        )
                    }
                }

                Spacer(GlanceModifier.height(if (compact) 6.dp else 9.dp))

                Row(
                    GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    model.forecast.take(3).forEachIndexed { index, day ->
                        val colors = listOf(0xFFF58A00, 0xFF23A8CF, 0xFF8060DD)
                        Box(
                            GlanceModifier
                                .defaultWeight()
                                .padding(horizontal = 3.dp)
                                .background(cp(colors[index]))
                                .cornerRadius(24.dp)
                                .padding(
                                    vertical = if (compact) 7.dp else 10.dp,
                                    horizontal = 3.dp
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
                                        fontSize = if (ultraCompact) 9.sp else 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    day.icon,
                                    style = TextStyle(
                                        fontSize = if (ultraCompact) 17.sp else 23.sp
                                    )
                                )
                                Text(
                                    day.max.toString() + "°/" + day.min.toString() + "°",
                                    style = TextStyle(
                                        color = cp(0xFF111820),
                                        fontSize = if (ultraCompact) 8.sp else 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(GlanceModifier.defaultWeight())

                if (!ultraCompact) {
                    Row(
                        GlanceModifier
                            .fillMaxWidth()
                            .background(cp(0xE91C2230))
                            .cornerRadius(25.dp)
                            .padding(horizontal = 7.dp, vertical = 6.dp),
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
                                    .size(40.dp)
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
                                        fontSize = 19.sp,
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
