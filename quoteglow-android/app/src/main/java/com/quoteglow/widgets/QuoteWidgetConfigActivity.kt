package com.quoteglow.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll

class QuoteWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val defaults = getSharedPreferences("quoteglow_widget_prefs", MODE_PRIVATE)
        val initialQuote = defaults.getString("quote_$appWidgetId", "Small steps every day lead to big results.") ?: "Small steps every day lead to big results."
        val initialStyle = defaults.getString("style_$appWidgetId", defaults.getString("default_style", "Glass")) ?: "Glass"
        val initialOpacity = defaults.getInt("opacity_$appWidgetId", defaults.getInt("default_opacity", 90)).toFloat()

        setContent {
            val quote = remember { mutableStateOf(initialQuote) }
            val style = remember { mutableStateOf(initialStyle) }
            val opacity = remember { mutableFloatStateOf(initialOpacity) }
            val styles = listOf("Minimal", "Glass", "Neon", "Gradient", "AMOLED", "Elegant", "Handwritten", "Bold", "Retro", "Transparent")

            MaterialTheme {
                Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Customize QuoteGlow", style = MaterialTheme.typography.headlineSmall)
                    Text("This configuration belongs only to this widget.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextField(value = quote.value, onValueChange = { quote.value = it }, label = { Text("Quote") }, modifier = Modifier.fillMaxWidth())
                    Text("Style")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        styles.take(5).forEach { s -> FilterChip(selected = style.value == s, onClick = { style.value = s }, label = { Text(s) }) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        styles.drop(5).forEach { s -> FilterChip(selected = style.value == s, onClick = { style.value = s }, label = { Text(s) }) }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Opacity: ${opacity.floatValue.toInt()}%")
                    Slider(value = opacity.floatValue, onValueChange = { opacity.floatValue = it }, valueRange = 10f..100f)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { finish() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(onClick = {
                            QuoteWidgetPrefs.save(this@QuoteWidgetConfigActivity, appWidgetId, quote.value, style.value, opacity.floatValue.toInt())
                            setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
                            QuoteWidget().updateAll(this@QuoteWidgetConfigActivity)
                            finish()
                        }, modifier = Modifier.weight(1f)) { Text("Save") }
                    }
                }
            }
        }
    }
}
