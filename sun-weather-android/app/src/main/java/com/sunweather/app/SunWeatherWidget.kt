package com.sunweather.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.compose.ui.graphics.Color
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.*
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

private fun cp(hex: Long) = ColorProvider(Color(hex), Color(hex))

class SunWeatherWidget:GlanceAppWidget(){
    override val sizeMode=SizeMode.Responsive(setOf(androidx.compose.ui.unit.DpSize(320.dp,220.dp),androidx.compose.ui.unit.DpSize(420.dp,360.dp)))
    override suspend fun provideGlance(context:Context,id:GlanceId){provideContent{WidgetContent()}}
    @Composable private fun WidgetContent(){
        val context=LocalContext.current
        val model=WeatherRepository.loadModel(context)
        val theme=WeatherRepository.theme(context)
        val shortcuts=WeatherRepository.shortcuts(context)
        val compact=LocalSize.current.width<380.dp
        val accent=when(theme){"Ocean"->0xFF39B6D8; "Sunset"->0xFFFF8A3D; "Lavender"->0xFF8F6BE8; else->0xFFFFA726}
        Box(GlanceModifier.fillMaxSize().cornerRadius(28.dp)){
            Image(ImageProvider(R.drawable.weather_scene),"Weather road sunrise background",ContentScale.Crop,GlanceModifier.fillMaxSize())
            Column(GlanceModifier.fillMaxSize().padding(14.dp)){
                Row(GlanceModifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                    Column(GlanceModifier.defaultWeight()){
                        Text("TODAY",style=TextStyle(color=cp(0xFF10151A),fontSize=24.sp,fontWeight=FontWeight.Bold))
                        Text(model.date,maxLines=1,style=TextStyle(color=cp(0xFF172027),fontSize=10.sp))
                    }
                    Text(model.icon,style=TextStyle(fontSize=34.sp))
                }
                Spacer(GlanceModifier.height(7.dp))
                Box(GlanceModifier.fillMaxWidth().background(cp(0xE61B2027)).cornerRadius(24.dp).padding(12.dp)){
                    Row(GlanceModifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                        Column(GlanceModifier.defaultWeight()){
                            Text(model.time,style=TextStyle(color=cp(0xFFFFFFFF),fontSize=11.sp))
                            Text(model.temperature.toString()+"°/"+model.min.toString()+"°",style=TextStyle(color=cp(0xFFFFFFFF),fontSize=30.sp,fontWeight=FontWeight.Bold))
                            Text(model.condition,maxLines=1,style=TextStyle(color=cp(0xFFF1F5F8),fontSize=12.sp))
                        }
                        Text("Weather\nForecast",style=TextStyle(color=cp(0xFF20242A),fontSize=10.sp,fontWeight=FontWeight.Bold),
                            modifier=GlanceModifier.background(cp(0xFFF1F4F5)).cornerRadius(20.dp).padding(horizontal=10.dp,vertical=8.dp))
                    }
                }
                Spacer(GlanceModifier.height(8.dp))
                Row(GlanceModifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){
                    model.forecast.take(3).forEachIndexed{index,day->
                        val colors=listOf(0xFFF57C00,0xFF159BC1,0xFF7D55D8)
                        Box(GlanceModifier.defaultWeight().padding(horizontal=3.dp).background(ColorProvider(colors[index])).cornerRadius(22.dp).padding(vertical=7.dp,horizontal=4.dp)){
                            Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=GlanceModifier.fillMaxWidth()){
                                Text(day.day,style=TextStyle(color=cp(0xFF101820),fontSize=if(compact)10.sp else 11.sp,fontWeight=FontWeight.Bold))
                                Text(day.icon,style=TextStyle(fontSize=if(compact)18.sp else 21.sp))
                                Text(day.max.toString()+"°/"+day.min.toString()+"°",style=TextStyle(color=cp(0xFF101820),fontSize=if(compact)9.sp else 10.sp,fontWeight=FontWeight.Bold))
                            }
                        }
                    }
                }
                Spacer(GlanceModifier.defaultWeight())
                Row(GlanceModifier.fillMaxWidth().background(cp(0xE91C2230)).cornerRadius(24.dp).padding(horizontal=7.dp,vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    val labels=mapOf("phone" to "☎","messages" to "✉","camera" to "◉","twitter" to "𝕏","telegram" to "➤","spotify" to "♪")
                    shortcuts.take(6).forEach{key->
                        Box(GlanceModifier.defaultWeight().size(42.dp).clickable(actionStartActivity<ShortcutActivity>(actionParametersOf(ShortcutActivity.ACTION_KEY to key))),contentAlignment=Alignment.Center){
                            Text(labels[key]?:"•",style=TextStyle(color=cp(0xFFFFFFFF),fontSize=19.sp,fontWeight=FontWeight.Bold))
                        }
                    }
                }
                Text(model.city+"  •  "+model.humidity+"% humidity  •  "+model.wind+" km/h",
                    style=TextStyle(color=ColorProvider(accent),fontSize=9.sp,fontWeight=FontWeight.Bold),
                    modifier=GlanceModifier.padding(top=5.dp).clickable(actionStartActivity<SettingsActivity>()))
            }
        }
    }
}
class SunWeatherWidgetReceiver:GlanceAppWidgetReceiver(){override val glanceAppWidget:GlanceAppWidget=SunWeatherWidget()
    override fun onEnabled(context:Context){super.onEnabled(context);WeatherWorker.schedule(context);WeatherWorker.enqueueNow(context)}
}
