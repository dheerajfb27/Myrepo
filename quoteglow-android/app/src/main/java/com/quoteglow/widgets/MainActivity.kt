package com.quoteglow.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QuoteGlowDashboard(this) }
    }

    private fun pickWidget(receiver: Class<*>) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, ComponentName(this@MainActivity, receiver))
        }
        runCatching { startActivity(intent) }
    }
}

@Composable
private fun QuoteGlowDashboard(context: Context) {
    val opacity = remember { mutableFloatStateOf(90f) }
    val style = remember { mutableStateOf("Glass") }
    val styles = listOf("Minimal", "Glass", "Neon", "Gradient", "AMOLED", "Elegant", "Handwritten", "Bold", "Retro", "Transparent")

    MaterialTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("QuoteGlow Widgets", style = MaterialTheme.typography.headlineMedium)
                Text("Your quotes, directly on the home screen.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Widget sizes", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { (context as MainActivity).pickWidget(QuoteWidgetReceiver::class.java) }) { Text("2 × 1") }
                            Button(onClick = { (context as MainActivity).pickWidget(QuoteWidget4x1Receiver::class.java) }) { Text("4 × 1") }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Default widget style", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            styles.take(5).forEach { s -> FilterChip(selected = style.value == s, onClick = { style.value = s }, label = { Text(s, fontSize = 12.sp) }) }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Opacity: ${opacity.floatValue.toInt()}%")
                        Slider(value = opacity.floatValue, onValueChange = { opacity.floatValue = it }, valueRange = 10f..100f)
                        OutlinedButton(onClick = {
                            QuoteWidgetPrefs.saveDefault(context, style.value, opacity.floatValue.toInt())
                        }) { Text("Save defaults") }
                    }
                }
            }
            item {
                Text("Each widget instance can be configured independently after you add it. Wallpaper selection and wallpaper effects have been removed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
