package com.quoteglow.widgets

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class Quote(val text:String,val author:String,val category:String)
val quotes=listOf(
 Quote("Small steps every day lead to big results.","QuoteGlow","Motivation"),
 Quote("Believe in yourself and you are halfway there.","QuoteGlow","Life"),
 Quote("Good things take time.","QuoteGlow","Success"),
 Quote("A positive mind creates a positive life.","QuoteGlow","Positive"),
 Quote("Focus on progress, not perfection.","QuoteGlow","Self Growth"),
 Quote("Your future is created by what you do today.","QuoteGlow","Motivation"),
 Quote("Dream big. Start small. Act now.","QuoteGlow","Success"),
 Quote("Progress is progress, no matter how small.","QuoteGlow","Self Growth"),
 Quote("Make today count.","QuoteGlow","Life"),
 Quote("Choose courage over comfort.","QuoteGlow","Motivation"),
 Quote("You are capable of amazing things.","QuoteGlow","Positive"),
 Quote("Stay patient. Trust the journey.","QuoteGlow","Life"),
 Quote("Create a life you love.","QuoteGlow","Life"),
 Quote("One day or day one. You decide.","QuoteGlow","Motivation"),
 Quote("Keep going. Your story is still being written.","QuoteGlow","Motivation"),
 Quote("Small habits become big changes.","QuoteGlow","Self Growth"),
 Quote("Let your light be louder than your doubts.","QuoteGlow","Positive"),
 Quote("Success starts with showing up.","QuoteGlow","Success"),
 Quote("Be proud of how far you have come.","QuoteGlow","Self Growth"),
 Quote("New day. New mindset. New results.","QuoteGlow","Positive")
)
private const val PREFS="quoteglow_prefs"
private const val FAVORITES="favorites"
private const val DARK="dark"
private const val STYLE="style"
private const val SELECTED_QUOTE="selected_quote"
private fun prefs(c:Context)=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
private fun isFav(c:Context,q:Quote)=prefs(c).getStringSet(FAVORITES,emptySet())?.contains(q.text)==true
private fun saveFav(c:Context,q:Quote,value:Boolean){
 val s=prefs(c).getStringSet(FAVORITES,emptySet())?.toMutableSet()?:mutableSetOf()
 if(value)s.add(q.text)else s.remove(q.text)
 prefs(c).edit().putStringSet(FAVORITES,s).apply()
}
private fun saveQuote(c:Context,q:Quote)=prefs(c).edit().putString(SELECTED_QUOTE,q.text).apply()

class MainActivity:ComponentActivity(){
 override fun onCreate(state:Bundle?){super.onCreate(state);setContent{QuoteGlowApp(this)}}
}

@Composable fun QuoteGlowApp(context:Context){
 var dark by remember{mutableStateOf(prefs(context).getBoolean(DARK,true))}
 var tab by remember{mutableIntStateOf(0)}
 var selected by remember{mutableStateOf(quotes.first())}
 var refresh by remember{mutableIntStateOf(0)}
 val scheme=if(dark)darkColorScheme(primary=Color(0xFF8B5CF6),secondary=Color(0xFFFF4FD8),background=Color(0xFF070A18),surface=Color(0xFF11172A))else lightColorScheme(primary=Color(0xFF6750A4))
 MaterialTheme(colorScheme=scheme){
  Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={
   NavigationBar{listOf("Home","Explore","Widget","Favorites","Settings").forEachIndexed{i,n->
    NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(listOf("⌂","⌕","▦","♡","⚙")[i])},label={Text(n)})
   }}
  }){pad->
   when(tab){
    0->Home(pad,selected,context,{selected=it;saveQuote(context,it)},{saveQuote(context,selected);tab=2},{tab=1}){refresh++}
    1->Library(pad,context){selected=it;saveQuote(context,it);tab=2}
    2->WidgetBuilder(pad,selected,context)
    3->Favorites(pad,context,refresh){selected=it}
    else->Settings(pad,context,dark){dark=!dark;prefs(context).edit().putBoolean(DARK,dark).apply()}
   }
  }
 }
}

@Composable fun Header(title:String,subtitle:String){
 Row(Modifier.fillMaxWidth().padding(vertical=18.dp),verticalAlignment=Alignment.CenterVertically){
  Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(subtitle,color=Color(0xFF98A3C0))}
  Text("❝",style=MaterialTheme.typography.headlineLarge,color=Color(0xFFFF4FD8))
 }
}

@Composable fun QuoteCard(q:Quote,context:Context,onClick:()->Unit,onFav:()->Unit){
 var fav by remember(q.text){mutableStateOf(isFav(context,q))}
 Card(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable(onClick=onClick),shape=RoundedCornerShape(20.dp)){
  Column(Modifier.padding(16.dp)){
   Text(q.text,style=MaterialTheme.typography.titleMedium)
   Row(verticalAlignment=Alignment.CenterVertically){
    Text(q.category,color=Color(0xFF9FAAD0),modifier=Modifier.weight(1f))
    Text(if(fav)"♥" else "♡",color=Color(0xFFFF4FD8),style=MaterialTheme.typography.titleLarge,modifier=Modifier.clickable{fav=!fav;saveFav(context,q,fav);onFav()})
   }
  }
 }
}

@Composable fun Home(pad:PaddingValues,q:Quote,context:Context,onQ:(Quote)->Unit,onWidget:()->Unit,onExplore:()->Unit,onFav:()->Unit){
 LazyColumn(Modifier.padding(pad).padding(horizontal=16.dp)){
  item{Header("QuoteGlow","Quotes that live with you")}
  item{Card(shape=RoundedCornerShape(26.dp)){Column(Modifier.padding(20.dp)){
   Text("Today's Quote",color=Color(0xFF9FAAD0));Spacer(Modifier.height(12.dp))
   Box(Modifier.fillMaxWidth().height(235.dp).background(Brush.linearGradient(listOf(Color(0xFF4A1D64),Color(0xFF123B66),Color(0xFF0C172E))),RoundedCornerShape(22.dp)).padding(24.dp),contentAlignment=Alignment.Center){
    Column(horizontalAlignment=Alignment.CenterHorizontally){Text(q.text,style=MaterialTheme.typography.headlineSmall,color=Color.White);Spacer(Modifier.height(10.dp));Text("— "+q.author,color=Color.White.copy(.7f))}
   }
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.padding(top=14.dp).fillMaxWidth()){
    Button(onClick=onWidget,modifier=Modifier.weight(1f)){Text("Use as Widget")}
    OutlinedButton(onClick=onExplore,modifier=Modifier.weight(1f)){Text("Explore")}
   }
  }}}
  item{Text("Explore Quotes",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(16.dp))}
  items(quotes.take(10)){x->QuoteCard(x,context,{onQ(x)},onFav)}
 }
}

@Composable fun Library(pad:PaddingValues,context:Context,onQ:(Quote)->Unit){
 var filter by remember{mutableStateOf("All")}
 val cats=listOf("All","Motivation","Life","Success","Positive","Self Growth")
 val data=if(filter=="All")quotes else quotes.filter{it.category==filter}
 LazyColumn(Modifier.padding(pad).padding(horizontal=16.dp)){
  item{Header("Quote Library",quotes.size.toString()+" curated quotes")}
  item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){cats.forEach{c->FilterChip(selected=filter==c,onClick={filter=c},label={Text(c)})}}}
  items(data){q->QuoteCard(q,context,{onQ(q)},{})}
 }
}

@Composable fun WidgetBuilder(pad:PaddingValues,q:Quote,context:Context){
 val scope=rememberCoroutineScope()
 var style by remember{mutableStateOf(prefs(context).getString(STYLE,"Glass")?:"Glass")}
 LazyColumn(Modifier.padding(pad).padding(horizontal=16.dp)){
  item{Header("Widget Builder","Design your home-screen widget")}
  item{Card(shape=RoundedCornerShape(28.dp)){Box(Modifier.fillMaxWidth().height(205.dp).background(
   when(style){
    "Neon"->Brush.linearGradient(listOf(Color(0xFF26003D),Color(0xFF001E3D)))
    "Gradient"->Brush.linearGradient(listOf(Color(0xFF5A189A),Color(0xFF0B7285)))
    "Minimal"->Brush.linearGradient(listOf(Color(0xFF20242D),Color(0xFF101218)))
    else->Brush.linearGradient(listOf(Color(0xFF30205C),Color(0xFF102C52)))
   },RoundedCornerShape(28.dp)).padding(24.dp),contentAlignment=Alignment.Center){
    Column(horizontalAlignment=Alignment.CenterHorizontally){Text(q.text,style=MaterialTheme.typography.headlineSmall,color=Color.White);Text("— "+q.author,color=Color.White.copy(.7f));Text(style.uppercase(),color=Color(0xFFFF8FE9))}
  }}}
  item{Text("Widget style",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(vertical=14.dp))}
  item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Minimal","Glass","Neon","Gradient").forEach{s->FilterChip(selected=style==s,onClick={style=s;prefs(context).edit().putString(STYLE,s).apply()},label={Text(s)})}}}
  item{Button(onClick={saveQuote(context,q);scope.launch{QuoteWidget().updateAll(context)};context.startActivity(Intent("android.appwidget.action.APPWIDGET_PICK"))},modifier=Modifier.fillMaxWidth().padding(top=20.dp)){Text("Add to Home Screen")}}
  item{OutlinedButton(onClick={context.startActivity(Intent("android.service.wallpaper.LIVE_WALLPAPER_CHOOSER"))},modifier=Modifier.fillMaxWidth().padding(top=10.dp)){Text("Set QuoteGlow Live Wallpaper")}}
  item{Text("Uses official Android widget and wallpaper APIs. No overlays or AccessibilityService.",color=Color(0xFF9FAAD0),modifier=Modifier.padding(vertical=18.dp))}
 }
}

@Composable fun Favorites(pad:PaddingValues,context:Context,refresh:Int,onSelect:(Quote)->Unit){
 val set=prefs(context).getStringSet(FAVORITES,emptySet())?:emptySet()
 val data=quotes.filter{set.contains(it.text)}
 Column(Modifier.padding(pad).fillMaxSize().padding(horizontal=16.dp)){
  Header("Favorites",data.size.toString()+" saved quotes")
  if(data.isEmpty())Text("Tap ♡ on any quote to save it here.",color=Color(0xFF9FAAD0),modifier=Modifier.padding(18.dp))
  else LazyColumn{items(data){q->QuoteCard(q,context,{onSelect(q)},{})}}
 }
 @Suppress("UNUSED_VARIABLE") val ignored=refresh
}

@Composable fun Settings(pad:PaddingValues,context:Context,dark:Boolean,toggle:()->Unit){
 Column(Modifier.padding(pad).fillMaxSize().padding(horizontal=16.dp)){
  Header("Quote Settings","Preferences & appearance")
  Card{Column(Modifier.padding(18.dp)){
   Text("Appearance",style=MaterialTheme.typography.titleMedium)
   Text(if(dark)"Dark theme enabled" else "Light theme enabled",color=Color(0xFF9FAAD0))
   Button(onClick=toggle,modifier=Modifier.fillMaxWidth().padding(top=10.dp)){Text("Switch theme")}
   HorizontalDivider(Modifier.padding(vertical=16.dp))
   Text("Widget style: "+(prefs(context).getString(STYLE,"Glass")?:"Glass"))
   HorizontalDivider(Modifier.padding(vertical=16.dp))
   Text("QuoteGlow",style=MaterialTheme.typography.titleMedium)
   Text("Custom quotes, home-screen widgets and live wallpaper.",color=Color(0xFF9FAAD0))
  }}
 }
}
