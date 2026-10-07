package com.sunweather.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity:ComponentActivity(){
    override fun onCreate(state:Bundle?){
        super.onCreate(state);WeatherWorker.schedule(this)
        setContent{
            MaterialTheme{
                var key by remember{mutableStateOf(WeatherRepository.apiKey(this))}
                var city by remember{mutableStateOf(WeatherRepository.city(this))}
                var theme by remember{mutableStateOf(WeatherRepository.theme(this))}
                var iconSet by remember{mutableStateOf(WeatherRepository.iconSet(this))}
                var selected by remember{mutableStateOf(WeatherRepository.shortcuts(this))}
                Scaffold(topBar={TopAppBar(title={Text("Sun Weather Widgets")})}){pad->
                    Column(Modifier.fillMaxSize().padding(pad).padding(18.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
                        Text("Stylized weather dashboard",style=MaterialTheme.typography.headlineSmall)
                        Text("Add the resizable widget to your home screen. Live data comes from OpenWeather.")
                        OutlinedTextField(key,{key=it},label={Text("OpenWeather API key")},modifier=Modifier.fillMaxWidth(),singleLine=true,visualTransformation=PasswordVisualTransformation())
                        OutlinedTextField(city,{city=it},label={Text("City")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                        Text("Background theme",style=MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Sunrise","Ocean","Sunset","Lavender").forEach{FilterChip(theme==it,{theme=it},{Text(it)})}}
                        Text("Weather icon set",style=MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Colorful","Minimal").forEach{FilterChip(iconSet==it,{iconSet=it},{Text(it)})}}
                        Text("Bottom dock shortcuts",style=MaterialTheme.typography.titleMedium)
                        listOf("phone" to "Phone","messages" to "Messages","camera" to "Camera","twitter" to "Twitter / X","telegram" to "Telegram","spotify" to "Spotify").forEach{(id,label)->
                            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label);Switch(selected.contains(id),{on->selected=selected.toMutableSet().apply{if(on)add(id)else remove(id)}})}
                        }
                        Button({WeatherRepository.saveSettings(this@SettingsActivity,key,city,theme,iconSet,selected);WeatherWorker.enqueueNow(this@SettingsActivity);finish()},Modifier.fillMaxWidth()){Text("Save & refresh widget")}
                        Text("Without an API key, built-in demo weather is shown until OpenWeather is configured.",style=MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
