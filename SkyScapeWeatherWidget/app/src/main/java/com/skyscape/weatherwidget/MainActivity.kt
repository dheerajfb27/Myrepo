package com.skyscape.weatherwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SkyScapeHome() }
    }
}

@Composable
private fun SkyScapeHome() {
    val ink = Color(0xFF080B10)
    Surface(modifier = Modifier.fillMaxSize(), color = ink) {
        Column(
            modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF182632), ink))).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text("SkyScape", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("WEATHER WIDGET", color = Color(0xFF79D9FF), letterSpacing = 3.sp, fontSize = 12.sp)
            Text("Nature on your home screen", color = Color(0xFFD0D9E1), fontSize = 16.sp)
            GlassWeatherPreview()
            Text("Your weather, framed by nature.", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text("Add the resizable SkyScape widget from your Pixel home-screen widget picker. Weather provider integration is being added next.", color = Color(0xFFADB8C4), fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(Modifier.weight(1f))
            Text("BLACK GLASS  •  NATURE SCENE  •  RESIZABLE", color = Color(0xFF79D9FF), fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun GlassWeatherPreview() {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF283944), Color(0xE6090D13), Color(0xFF15212B))), RoundedCornerShape(28.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🌳  🐦  ☁️", fontSize = 28.sp)
            Text("Nature scene", color = Color(0xFFB5C5D1), fontSize = 12.sp)
            Text("SkyScape glass", color = Color.White, fontWeight = FontWeight.Medium)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("☀️  --°", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Light)
            Text("Live weather", color = Color(0xFF9FC9E3), fontSize = 12.sp)
            Text("Forecast ready", color = Color(0xFFB5C5D1), fontSize = 11.sp)
        }
    }
}
