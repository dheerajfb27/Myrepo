package com.skyscape.weatherwidget

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SkyScapeHome() }
    }
}

@Composable
private fun SkyScapeHome() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var weather by remember { mutableStateOf(WeatherRepository.cached(context)) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun refreshWeather() {
        if (refreshing) return
        scope.launch {
            refreshing = true
            try {
                weather = WeatherRepository.refresh(context)
                context.sendBroadcast(
                    Intent(context, SkyScapeWidgetReceiver::class.java)
                        .setAction("com.skyscape.weatherwidget.ACTION_RENDER_CACHED")
                )
            } finally {
                refreshing = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshWeather() }
    val ink = Color(0xFF080B10)
    Surface(modifier = Modifier.fillMaxSize(), color = ink) {
        Column(
            modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF182632), ink))).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text("SkyScape", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("WEATHER WIDGET", color = Color(0xFF79D9FF), letterSpacing = 3.sp, fontSize = 12.sp)
            Text("Nature on your home screen", color = Color(0xFFD0D9E1), fontSize = 16.sp)
            GlassWeatherPreview(weather)
            Text("A scenic road, rainbow accents and a 5-day forecast.", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "The preview animates gently. Android home-screen widgets use launcher-hosted RemoteViews, so continuous motion is restricted; the widget keeps weather updates battery-friendly.",
                color = Color(0xFFADB8C4), fontSize = 14.sp, lineHeight = 21.sp
            )
            Button(
                onClick = { refreshWeather() },
                enabled = !refreshing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17445A))
            ) {
                Text(if (refreshing) "Refreshing weather…" else "Refresh weather")
            }
            Spacer(Modifier.weight(1f))
            Text("SCENIC ROAD  •  5-DAY FORECAST  •  SHORTCUT DOCK", color = Color(0xFF79D9FF), fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun GlassWeatherPreview(weather: WeatherSnapshot) {
    val transition = rememberInfiniteTransition(label = "scenic-motion")
    val drift = transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 2200), repeatMode = RepeatMode.Reverse),
        label = "bird-drift"
    )
    val glow = transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1800), repeatMode = RepeatMode.Reverse),
        label = "sky-glow"
    )
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp)
            .background(
                Brush.linearGradient(listOf(
                    Color(0xFF283944).copy(alpha = glow.value),
                    Color(0xE6090D13),
                    Color(0xFF15212B).copy(alpha = glow.value)
                )),
                RoundedCornerShape(28.dp)
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🌳   🐦   ☁️", modifier = Modifier.offset(x = drift.value.dp), color = Color.White, fontSize = 28.sp)
            Text("Nature scene", color = Color(0xFFB5C5D1), fontSize = 12.sp)
            Text("SkyScape glass", color = Color.White, fontWeight = FontWeight.Medium)
            Text(weather.description, color = Color(0xFF9FC9E3), fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val temperature = if (weather.temperature >= 0) "${weather.temperature}°" else "--°"
            Text("${weather.symbol}  $temperature", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Light)
            Text("Bengaluru", color = Color(0xFF9FC9E3), fontSize = 12.sp)
            if (weather.high >= 0 && weather.low >= 0) {
                Text("H ${weather.high}°  L ${weather.low}°", color = Color(0xFFB5C5D1), fontSize = 11.sp)
            }
        }
    }
}
