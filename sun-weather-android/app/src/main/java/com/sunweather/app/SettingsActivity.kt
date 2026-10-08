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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity:ComponentActivity(){
    override fun onCreate(state:Bundle?){
        super.onCreate(state)
        WeatherWorker.schedule(this)
        setContent{
            MaterialTheme{
                var key by remember{mutableStateOf(WeatherRepository.apiKey(this))}
                var city by remember{mutableStateOf(WeatherRepository.city(this))}
                var theme by remember{mutableStateOf(WeatherRepository.theme(this))}
                var iconSet by remember{mutableStateOf(WeatherRepository.iconSet(this))}
                var selected by remember{mutableStateOf(WeatherRepository.shortcuts(this))}
                var testing by remember{mutableStateOf(false)}
                var apiStatus by remember{mutableStateOf<WeatherRepository.ApiTestResult?>(null)}
                val scope=rememberCoroutineScope()

                Scaffold(topBar={TopAppBar(title={Text("Sun Weather Widgets")})}){pad->
                    Column(
                        Modifier.fillMaxSize().padding(pad).padding(18.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement=Arrangement.spacedBy(12.dp)
                    ){
                        Text("Stylized weather dashboard",style=MaterialTheme.typography.headlineSmall)
                        Text("Add the resizable widget to your home screen. Live data comes from OpenWeather.")

                        OutlinedTextField(
                            key,{key=it},
                            label={Text("OpenWeather API key")},
                            modifier=Modifier.fillMaxWidth(),
                            singleLine=true,
                            visualTransformation=PasswordVisualTransformation()
                        )
                        OutlinedTextField(
                            city,{city=it},
                            label={Text("City")},
                            modifier=Modifier.fillMaxWidth(),
                            singleLine=true
                        )

                        Button(
                            onClick={
                                testing=true
                                apiStatus=null
                                scope.launch{
                                    val result=withContext(Dispatchers.IO){
                                        WeatherRepository.testApiKey(key,city)
                                    }
                                    apiStatus=result
                                    testing=false
                                }
                            },
                            enabled=!testing,
                            modifier=Modifier.fillMaxWidth()
                        ){
                            Text(if(testing)"Testing..." else "Test API Key")
                        }

                        apiStatus?.let{
                            val statusColor=when{
                                it.ok->Color(0xFF168A3A)
                                it.message.contains("rate limit",ignoreCase=true)->Color(0xFFB86B00)
                                else->MaterialTheme.colorScheme.error
                            }
                            Text(
                                text=if(it.ok)"🟢 ${it.message}" else "🔴 ${it.message}",
                                color=statusColor,
                                style=MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text("Background theme",style=MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            listOf("Sunrise","Ocean","Sunset","Lavender").forEach{
                                FilterChip(theme==it,{theme=it},{Text(it)})
                            }
                        }

                        Text("Weather icon set",style=MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            listOf("Colorful","Minimal").forEach{
                                FilterChip(iconSet==it,{iconSet=it},{Text(it)})
                            }
                        }

                        Text("Bottom dock shortcuts",style=MaterialTheme.typography.titleMedium)
                        listOf(
                            "phone" to "Phone","messages" to "Messages","camera" to "Camera",
                            "twitter" to "Twitter / X","telegram" to "Telegram","spotify" to "Spotify"
                        ).forEach{(id,label)->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement=Arrangement.SpaceBetween
                            ){
                                Text(label)
                                Switch(
                                    selected.contains(id),
                                    {on->selected=selected.toMutableSet().apply{
                                        if(on)add(id)else remove(id)
                                    }}
                                )
                            }
                        }

                        Button(
                            onClick={
                                WeatherRepository.saveSettings(
                                    this@SettingsActivity,key,city,theme,iconSet,selected
                                )
                                WeatherWorker.enqueueNow(this@SettingsActivity)
                                finish()
                            },
                            modifier=Modifier.fillMaxWidth()
                        ){
                            Text("Save & refresh widget")
                        }

                        Text(
                            "Without an API key, built-in demo weather is shown until OpenWeather is configured.",
                            style=MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
