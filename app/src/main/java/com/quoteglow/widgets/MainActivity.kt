package com.quoteglow.widgets

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Quote(val text: String, val author: String, val category: String)
val quotes = listOf(
 Quote("Small steps every day lead to big results.","QuoteGlow","Motivation"),
 Quote("Believe in yourself and you are halfway there.","QuoteGlow","Life"),
 Quote("Good things take time.","QuoteGlow","Success"),
 Quote("A positive mind creates a positive life.","QuoteGlow","Positive"),
 Quote("Focus on progress, not perfection.","QuoteGlow","Self Growth")
)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent { QuoteGlowApp(this) }
 }
}

@Composable fun QuoteGlowApp(context: Context) {
 var tab by remember { mutableIntStateOf(0) }
 var selected by remember { mutableStateOf(quotes.first()) }
 var dark by remember { mutableStateOf(true) }
 val colors = if (dark) darkColorScheme(primary=Color(0xFF6D7CFF), secondary=Color(0xFFFF4FD8)) else lightColorScheme()
 MaterialTheme(colorScheme=colors) {
  Scaffold(containerColor=if(dark) Color(0xFF070A18) else Color(0xFFF6F7FB), bottomBar={
   NavigationBar { listOf("Home","Explore","Widget","Favorites","Settings").forEachIndexed { i,n ->
    NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(listOf("⌂","⌕","▦","♡","⚙")[i])},label={Text(n)})
   }}
  }) { pad ->
   when(tab) {
    0 -> Home(pad,selected,{selected=it},{tab=2},{tab=1})
    1 -> Library(pad,{selected=it},{tab=2})
    2 -> WidgetBuilder(pad,selected,context)
    3 -> Favorites(pad)
    else -> Settings(pad,{dark=!dark})
   }
  }
 }
}

@Composable fun Header(title:String, subtitle:String) {
 Row(Modifier.fillMaxWidth().padding(vertical=18.dp),verticalAlignment=Alignment.CenterVertically) {
  Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.headlineSmall);Text(subtitle,color=Color(0xFF98A3C0))}
  Text("❝",style=MaterialTheme.typography.headlineLarge,color=Color(0xFFFF4FD8))
 }
}

@Composable fun Home(pad:PaddingValues,q:Quote,onQ:(Quote)->Unit,widget:()->Unit,library:()->Unit) {
 LazyColumn(Modifier.padding(pad).padding(horizontal=16.dp)) {
  item { Header("QuoteGlow","Widgets • Quotes That Live With You") }
  item { Card(shape=RoundedCornerShape(26.dp)) {
   Column(Modifier.padding(20.dp)) {
    Text("Today's Quote",color=Color(0xFF9FAAD0)); Spacer(Modifier.height(12.dp))
    Box(Modifier.fillMaxWidth().height(220.dp).background(Brush.linearGradient(listOf(Color(0xFF38204F),Color(0xFF163B62))),RoundedCornerShape(20.dp)).padding(20.dp),contentAlignment=Alignment.Center) {
     Text(q.text,style=MaterialTheme.typography.headlineMedium,color=Color.White)
    }
    Spacer(Modifier.height(10.dp)); Text("— "+q.author,color=Color(0xFF9FAAD0))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.padding(top=14.dp)){Button(onClick=widget){Text("Use as Widget")};OutlinedButton(onClick=library){Text("Explore")}}
   }
  }}
  item { Text("Explore Quotes",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(16.dp)) }
  items(quotes) { x -> Card(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable{onQ(x)}) { Row(Modifier.padding(16.dp)) { Column { Text(x.text); Text(x.category,color=Color(0xFF9FAAD0)) } } } }
 }
}

@Composable fun Library(pad:PaddingValues,onQ:(Quote)->Unit,widget:()->Unit) {
 LazyColumn(Modifier.padding(pad).padding(16.dp)){item{Header("Quote Library","200+ beautiful quotes")}
  items((1..40).map{quotes[it%quotes.size]}){q->ListItem(headlineContent={Text(q.text)},supportingContent={Text(q.category)},modifier=Modifier.clickable{onQ(q);widget()})}}
}

@Composable fun WidgetBuilder(pad:PaddingValues,q:Quote,context:Context) {
 var style by remember{mutableStateOf("Glass")}
 LazyColumn(Modifier.padding(pad).padding(16.dp)){item{Header("Widget Builder","Design your home-screen widget")}
  item{Card(shape=RoundedCornerShape(28.dp)){Box(Modifier.fillMaxWidth().height(190.dp).background(Brush.linearGradient(listOf(Color(0xFF30205C),Color(0xFF102C52))),RoundedCornerShape(28.dp)).padding(24.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(q.text,style=MaterialTheme.typography.headlineSmall);Text("— "+q.author,color=Color.White.copy(.7f))}}}}
  item{Text("Widget style",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(vertical=14.dp))}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Minimal","Glass","Neon","Gradient").forEach{s->FilterChip(selected=style==s,onClick={style=s},label={Text(s)})}}}
  item{Button(onClick={context.startActivity(Intent("android.appwidget.action.APPWIDGET_PICK"))},modifier=Modifier.fillMaxWidth().padding(top=20.dp)){Text("Add to Home Screen")}}
  item{Text("Lock-screen widgets are only shown when the Android/device host provides an official third-party surface. QuoteGlow never uses overlays, AccessibilityService, or fake lock screens.",color=Color(0xFF9FAAD0),modifier=Modifier.padding(top=14.dp))}
 }
}

@Composable fun Favorites(pad:PaddingValues){Column(Modifier.padding(pad).fillMaxSize()){Header("Favorites","Your saved quotes");Text("Favorite quotes will appear here.",modifier=Modifier.padding(18.dp))}}
@Composable fun Settings(pad:PaddingValues,toggle:()->Unit){Column(Modifier.padding(pad).fillMaxSize()){Header("Quote Settings","Preferences & appearance");Card(Modifier.padding(16.dp)){Column(Modifier.padding(16.dp)){Text("Auto Change Quote");Text("Every day • Every 6 hours • Every 12 hours • Manual",color=Color(0xFF9FAAD0),modifier=Modifier.padding(vertical=8.dp));HorizontalDivider();Text("Theme",modifier=Modifier.padding(top=14.dp));Button(onClick=toggle){Text("Toggle Dark / Light")};Text("Wallpaper uses official Android WallpaperManager APIs.",color=Color(0xFF9FAAD0),modifier=Modifier.padding(top=16.dp))}}}}
