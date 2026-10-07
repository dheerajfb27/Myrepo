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
        val width = LocalSize.current.width
        val height = LocalSize.current.height

        val compact = width < 380.dp
        val small = height < 205.dp
        val large = width >= 500.dp
        val ultra = width < 340.dp || height < 190.dp

        val clock = SimpleDateFormat("hh:mm\na", Locale.getDefault()).format(Date())
        val todayLabel = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())

        Box(
            GlanceModifier
                .fillMaxSize()
                .cornerRadius(30.dp)
                .padding(if (compact) 5.dp else 8.dp)
        ) {
            Column(GlanceModifier.fillMaxSize()) {

                // Approved top row: blue Today pill + weather icon.
                Row(
                    GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        GlanceModifier
                            .background(cp(0xFF28B9F4))
                            .cornerRadius(28.dp)
                            .padding(
                                horizontal = if (ultra) 13.dp else if (large) 20.dp else 16.dp,
                                vertical = if (ultra) 5.dp else if (large) 8.dp else 6.dp
                            )
                    ) {
                        Text(
                            "Today",
                            style = TextStyle(
                                color = cp(0xFF06101A),
                                fontSize = if (ultra) 18.sp else if (large) 28.sp else 23.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(GlanceModifier.defaultWeight())

                    Text(
                        model.icon,
                        style = TextStyle(
                            fontSize = if (ultra) 28.sp else if (large) 46.sp else 38.sp
                        )
                    )
                }

                Spacer(GlanceModifier.height(if (ultra) 3.dp else 5.dp))

                // Main dark rounded weather card.
                Box(
                    GlanceModifier
                        .fillMaxWidth()
                        .height(
                            if (ultra) 76.dp
                            else if (large) 112.dp
                            else if (small) 88.dp
                            else 100.dp
                        )
                        .background(cp(0xF51A202B))
                        .cornerRadius(30.dp)
                        .padding(
                            horizontal = if (compact) 10.dp else if (large) 17.dp else 13.dp,
                            vertical = if (ultra) 6.dp else if (large) 12.dp else 9.dp
                        )
                ) {
                    Column(GlanceModifier.fillMaxSize()) {
                        Text(
                            "Today",
                            style = TextStyle(
                                color = cp(0xFFE5EAF0),
                                fontSize = if (ultra) 9.sp else if (large) 14.sp else 11.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = GlanceModifier.fillMaxWidth()
                        ) {
                            Column(GlanceModifier.defaultWeight()) {
                                Text(
                                    model.temperature.toString() + "° " + model.min.toString() + "°",
                                    style = TextStyle(
                                        color = cp(0xFFFFFFFF),
                                        fontSize = if (ultra) 24.sp else if (large) 39.sp else 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    model.condition,
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = cp(0xFFF3F5F8),
                                        fontSize = if (ultra) 9.sp else if (large) 14.sp else 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            if (!ultra) {
                                Box(
                                    GlanceModifier
                                        .background(cp(0xFF54FF86))
                                        .cornerRadius(28.dp)
                                        .padding(1.dp)
                                ) {
                                    Box(
                                        GlanceModifier
                                            .background(cp(0xFF063B20))
                                            .cornerRadius(27.dp)
                                            .padding(
                                                horizontal = if (compact) 9.dp else if (large) 17.dp else 12.dp,
                                                vertical = if (large) 9.dp else 7.dp
                                            )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "Weather\nForecast",
                                                style = TextStyle(
                                                    color = cp(0xFFF2FFF6),
                                                    fontSize = if (large) 13.sp else 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(GlanceModifier.width(5.dp))
                                            Text(
                                                "›",
                                                style = TextStyle(
                                                    color = cp(0xFF7CFF9E),
                                                    fontSize = if (large) 23.sp else 19.sp,
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

                if (!small) {
                    Spacer(GlanceModifier.height(4.dp))

                    // Bottom composition matches the supplied reference:
                    // large clock on the left, three vertical forecast cards on the right.
                    Row(
                        GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            GlanceModifier
                                .defaultWeight()
                                .padding(start = if (compact) 4.dp else 8.dp, bottom = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                clock,
                                style = TextStyle(
                                    color = cp(0xFF090D13),
                                    fontSize = if (large) 43.sp else if (compact) 34.sp else 39.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Row(
                            GlanceModifier
                                .defaultWeight()
                                .padding(end = 2.dp),
                            horizontalAlignment = Alignment.End,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            model.forecast.take(3).forEachIndexed { index, day ->
                                val colors = listOf(0xFFFF9800, 0xFF20AEE8, 0xFF9060E8)
                                Box(
                                    GlanceModifier
                                        .defaultWeight()
                                        .padding(horizontal = 2.dp)
                                        .background(cp(colors[index]))
                                        .cornerRadius(25.dp)
                                        .padding(
                                            vertical = if (large) 10.dp else 7.dp,
                                            horizontal = 2.dp
                                        )
                                ) {
                                    Column(
                                        GlanceModifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            day.day,
                                            style = TextStyle(
                                                color = cp(0xFF111820),
                                                fontSize = if (large) 13.sp else 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            day.icon,
                                            style = TextStyle(fontSize = if (large) 27.sp else 20.sp)
                                        )
                                        Text(
                                            day.max.toString() + "°/" + day.min.toString() + "°",
                                            style = TextStyle(
                                                color = cp(0xFF111820),
                                                fontSize = if (large) 10.sp else 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Shortcuts stay available only when there is enough vertical room.
                if (height >= 330.dp && shortcuts.isNotEmpty()) {
                    Spacer(GlanceModifier.height(4.dp))
                    Row(
                        GlanceModifier
                            .fillMaxWidth()
                            .background(cp(0xE91C2230))
                            .cornerRadius(24.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
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
                                    .size(if (large) 42.dp else 36.dp)
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
                                        fontSize = if (large) 20.sp else 17.sp,
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
