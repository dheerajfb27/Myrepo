package com.quoteglow.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.app.WallpaperManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
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
private const val QUOTE_SIZE="quote_size"
private const val QUOTE_OFFSET="quote_offset"
private const val QUOTE_X="quote_x"
private fun prefs(c:Context)=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
private fun isFav(c:Context,q:Quote)=prefs(c).getStringSet(FAVORITES,emptySet())?.contains(q.text)==true
private fun saveFav(c:Context,q:Quote,value:Boolean){
 val s=prefs(c).getStringSet(FAVORITES,emptySet())?.toMutableSet()?:mutableSetOf()
 if(value)s.add(q.text)else s.remove(q.text)
 prefs(c).edit().putStringSet(FAVORITES,s).apply()
}
private fun saveQuote(c:Context,q:Quote)=prefs(c).edit().putString(SELECTED_QUOTE,q.text).apply()

class MainActivity:ComponentActivity(){
 var selectedWallpaperUri:Uri?=null
 var wallpaperVersion by mutableIntStateOf(0)
 private val wallpaperPicker=registerForActivityResult(ActivityResultContracts.GetContent()){uri->
  if(uri!=null){selectedWallpaperUri=uri;wallpaperVersion++;Toast.makeText(this,"Wallpaper selected",Toast.LENGTH_SHORT).show()}
 }
 override fun onCreate(state:Bundle?){super.onCreate(state);setContent{QuoteGlowApp(this)}}
 fun pickWallpaper(){wallpaperPicker.launch("image/*")}
 fun applySelectedWallpaper(quote:String,position:String,style:String,size:Int,xOffset:Int,yOffset:Int){
  val uri=selectedWallpaperUri
  if(uri==null){Toast.makeText(this,"Choose a wallpaper first",Toast.LENGTH_SHORT).show();return}
  try{
   val b=BitmapFactory.Options().apply{inJustDecodeBounds=true}
   contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,b)}
   if(b.outWidth<=0||b.outHeight<=0)throw IllegalArgumentException("Invalid image")
   val sample=calculateSample(b.outWidth,b.outHeight,1080,1920)
   val source=contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample})}
     ?:throw IllegalArgumentException("Unable to read image")
   val output=createQuoteWallpaper(source,quote,position,style,size,xOffset,yOffset);source.recycle()
   val wm=WallpaperManager.getInstance(this)
   if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) wm.setBitmap(output,null,true,WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK) else wm.setBitmap(output)
   output.recycle();Toast.makeText(this,"Wallpaper applied successfully",Toast.LENGTH_LONG).show()
  }catch(e:Exception){Toast.makeText(this,"Could not apply wallpaper: ${e.message?: "try another image"}",Toast.LENGTH_LONG).show()}
 }
 private fun calculateSample(w:Int,h:Int,tw:Int,th:Int):Int{var s=1;while(w/(s*2)>=tw&&h/(s*2)>=th)s*=2;return s}
 private fun createQuoteWallpaper(source:Bitmap,quote:String,position:String,style:String,size:Int,xOffset:Int,yOffset:Int):Bitmap{
  val tw=1080;val th=1920;val out=Bitmap.createBitmap(tw,th,Bitmap.Config.ARGB_8888);val canvas=Canvas(out)
  val scale=maxOf(tw.toFloat()/source.width,th.toFloat()/source.height);val dw=(source.width*scale).toInt();val dh=(source.height*scale).toInt()
  canvas.drawBitmap(source,null,android.graphics.Rect((tw-dw)/2,(th-dh)/2,(tw-dw)/2+dw,(th-dh)/2+dh),Paint(Paint.ANTI_ALIAS_FLAG))
  val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=android.graphics.Color.WHITE;textSize=(56f*size/100f).coerceIn(28f,100f);typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);textAlign=Paint.Align.CENTER;setShadowLayer(8f,0f,4f,android.graphics.Color.BLACK)}
  val lines=wrapQuote(quote,p,tw-140f);val lh=(72f*size/100f).coerceIn(38f,110f);val total=lines.size*lh
  val base=when(position){"Top"->220f;"Bottom"->th-220f-total;else->(th-total)/2f}+yOffset.coerceIn(-900,900)
  val textX=(tw/2f+xOffset.coerceIn(-500,500))
  when(style){"Glass"->canvas.drawRoundRect(55f,base-85f,tw-55f,base+total+45f,36f,36f,Paint().apply{color=android.graphics.Color.argb(120,0,0,0)})
   "Gradient"->canvas.drawRect(0f,base-120f,tw.toFloat(),base+total+90f,Paint().apply{color=android.graphics.Color.argb(75,90,24,154)})
   "Neon"->{p.setShadowLayer(18f,0f,0f,android.graphics.Color.MAGENTA);canvas.drawRoundRect(45f,base-95f,tw-45f,base+total+55f,40f,40f,Paint().apply{this.style=Paint.Style.STROKE;strokeWidth=5f;color=android.graphics.Color.argb(190,255,79,216)})}}
  lines.forEachIndexed{i,line->canvas.drawText(line,textX,base+(i+1)*lh,p)};return out
 }
 private fun wrapQuote(text:String,p:Paint,maxWidth:Float):List<String>{
  val words=text.trim().split(Regex("\\s+"));val lines=mutableListOf<String>();var current=""
  for(word in words){val candidate=if(current.isEmpty())word else "$current $word";if(p.measureText(candidate)<=maxWidth)current=candidate else{if(current.isNotEmpty())lines.add(current);current=word}}
  if(current.isNotEmpty())lines.add(current);return lines
 }
}

@Composable fun QuoteGlowApp(context:Context){
 var dark by remember{mutableStateOf(prefs(context).getBoolean(DARK,true))}
 var tab by remember{mutableIntStateOf(0)}
 var selected by remember{mutableStateOf(quotes.first())}
 var refresh by remember{mutableIntStateOf(0)}
 val activity=context as MainActivity
 val wallpaperTick=activity.wallpaperVersion
 val scheme=if(dark)darkColorScheme(primary=Color(0xFF8B5CF6),secondary=Color(0xFFFF4FD8),background=Color(0xFF070A18),surface=Color(0xFF11172A))else lightColorScheme(primary=Color(0xFF6750A4))
 MaterialTheme(colorScheme=scheme){
  Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={
   NavigationBar{listOf("Home","Explore","Widget","Favorites","Settings").forEachIndexed{i,n->
    NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(listOf("⌂","⌕","▦","♡","⚙")[i])},label={Text(n)})
   }}
  }){pad->
   when(tab){
    0->Home(pad,selected,context,wallpaperTick,{selected=it;saveQuote(context,it)},{saveQuote(context,selected);tab=2},{tab=1}){refresh++}
    1->Library(pad,context){selected=it;saveQuote(context,it);tab=2}
    2->WidgetBuilder(pad,selected,context,{activity.pickWallpaper()},{position,style,size,offset->activity.applySelectedWallpaper(selected.text,position,style,size,0,offset)})
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

@Composable fun Home(pad:PaddingValues,q:Quote,context:Context,wallpaperTick:Int,onQ:(Quote)->Unit,onWidget:()->Unit,onExplore:()->Unit,onFav:()->Unit){
 val activity=context as MainActivity
 var size by remember{mutableFloatStateOf(prefs(context).getInt(QUOTE_SIZE,100).toFloat())}
 var x by remember{mutableFloatStateOf(prefs(context).getInt(QUOTE_X,0).toFloat())}
 var y by remember{mutableFloatStateOf(prefs(context).getInt(QUOTE_OFFSET,0).toFloat())}
 var style by remember{mutableStateOf(prefs(context).getString(STYLE,"Glass")?:"Glass")}
 var bitmap by remember(wallpaperTick){mutableStateOf<Bitmap?>(null)}
 LaunchedEffect(wallpaperTick){
  activity.selectedWallpaperUri?.let{uri->runCatching{activity.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it)}}.onSuccess{bitmap=it}}
 }
 LazyColumn(Modifier.padding(pad).padding(horizontal=16.dp)){
  item{Header("QuoteGlow","Edit your wallpaper directly")}
  item{
   Card(shape=RoundedCornerShape(26.dp),modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(12.dp)){
     Box(Modifier.fillMaxWidth().height(390.dp).background(Color.Black,RoundedCornerShape(22.dp)).pointerInput(Unit){detectDragGestures{_,drag->
       x=(x+drag.x*1080f/360f).coerceIn(-500f,500f); y=(y+drag.y*1920f/390f).coerceIn(-900f,900f)
      }} ,contentAlignment=Alignment.Center){
      if(bitmap!=null) androidx.compose.foundation.Image(bitmap=bitmap!!.asImageBitmap(),contentDescription="Wallpaper",modifier=Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop)
      else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF30205C),Color(0xFF102C52)))))
      Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
       Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.offset(x=(x/4f).dp,y=(y/5f).dp)){
        Text(q.text,fontSize=(25f*size/100f).coerceIn(14f,40f).sp,color=Color.White,fontWeight=FontWeight.Bold)
        Text("— "+q.author,fontSize=(14f*size/100f).coerceIn(10f,22f).sp,color=Color.White.copy(.75f))
       }
      }
     }
     Text("Drag the quote to reposition",color=Color(0xFF9FAAD0),modifier=Modifier.padding(top=8.dp))
     Text("Size: "+size.toInt()+"%",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=10.dp))
     Slider(value=size,onValueChange={size=it},valueRange=50f..160f,steps=21,onValueChangeFinished={prefs(context).edit().putInt(QUOTE_SIZE,size.toInt()).apply()})
     Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Top","Center","Bottom").forEach{p->FilterChip(selected=false,onClick={y=when(p){"Top"->-500f;"Bottom"->500f;else->0f};x=0f;prefs(context).edit().putInt(QUOTE_X,0).putInt(QUOTE_OFFSET,y.toInt()).apply()},label={Text(p)})}}
     Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.padding(top=10.dp)){listOf("Minimal","Glass","Neon","Gradient").forEach{s->FilterChip(selected=style==s,onClick={style=s;prefs(context).edit().putString(STYLE,s).apply()},label={Text(s)})}}
     Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth().padding(top=12.dp)){
      OutlinedButton(onClick={activity.pickWallpaper()},modifier=Modifier.weight(1f)){Text("Wallpaper")}
      Button(onClick={prefs(context).edit().putInt(QUOTE_SIZE,size.toInt()).putInt(QUOTE_X,x.toInt()).putInt(QUOTE_OFFSET,y.toInt()).apply();activity.applySelectedWallpaper(q.text,"Center",style,size.toInt(),x.toInt(),y.toInt())},modifier=Modifier.weight(1f)){Text("Apply")}
     }
    }
   }
  }
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth().padding(vertical=12.dp)){Button(onClick=onWidget,modifier=Modifier.weight(1f)){Text("Widgets")};OutlinedButton(onClick=onExplore,modifier=Modifier.weight(1f)){Text("Explore")}}}
  item{Text("Explore Quotes",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(16.dp))}
  items(quotes.take(10)){xq->QuoteCard(xq,context,{onQ(xq)},onFav)}
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

@Composable fun WidgetBuilder(pad:PaddingValues,q:Quote,context:Context,onPickWallpaper:()->Unit,onApplyWallpaper:(String,String,Int,Int)->Unit){
 var style by remember{mutableStateOf(prefs(context).getString(STYLE,"Glass")?:"Glass")}
 var position by remember{mutableStateOf("Center")}
 var size by remember{mutableFloatStateOf(prefs(context).getInt(QUOTE_SIZE,100).toFloat())}
 var offset by remember{mutableFloatStateOf(prefs(context).getInt(QUOTE_OFFSET,0).toFloat())}
 LazyColumn(Modifier.padding(pad).padding(horizontal=16.dp)){
  item{Header("Widget & Wallpaper","Resize and position your quote exactly where you want it")}
  item{Card(shape=RoundedCornerShape(28.dp)){Box(Modifier.fillMaxWidth().height(230.dp).background(
   when(style){"Neon"->Brush.linearGradient(listOf(Color(0xFF26003D),Color(0xFF001E3D)));"Gradient"->Brush.linearGradient(listOf(Color(0xFF5A189A),Color(0xFF0B7285)));"Minimal"->Brush.linearGradient(listOf(Color(0xFF20242D),Color(0xFF101218)));else->Brush.linearGradient(listOf(Color(0xFF30205C),Color(0xFF102C52)))},RoundedCornerShape(28.dp)).padding(20.dp),contentAlignment=Alignment.Center){
    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.offset(y=(offset/12).dp)){
     Text(q.text,style=MaterialTheme.typography.titleLarge,color=Color.White,fontWeight=FontWeight.Bold)
     Text("— "+q.author,color=Color.White.copy(.7f),style=MaterialTheme.typography.bodyMedium)
     Text(style.uppercase(),color=Color(0xFFFF8FE9),style=MaterialTheme.typography.labelSmall)
    }
  }}}
  item{Text("Quote size: "+size.toInt()+"%",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=18.dp,bottom=4.dp))}
  item{Slider(value=size,onValueChange={size=it},valueRange=50f..160f,steps=21,onValueChangeFinished={prefs(context).edit().putInt(QUOTE_SIZE,size.toInt()).apply()})}
  item{Text("Vertical position: "+offset.toInt(),style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=8.dp,bottom=4.dp))}
  item{Slider(value=offset,onValueChange={offset=it},valueRange=-700f..700f,steps=27,onValueChangeFinished={prefs(context).edit().putInt(QUOTE_OFFSET,offset.toInt()).apply()})}
  item{Text("Quick position",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=8.dp,bottom=8.dp))}
  item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Top","Center","Bottom").forEach{x->FilterChip(selected=position==x,onClick={position=x;offset=when(x){"Top"->-500f;"Bottom"->500f;else->0f};prefs(context).edit().putInt(QUOTE_OFFSET,offset.toInt()).apply()},label={Text(x)})}}}
  item{Text("Quote style",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=18.dp,bottom=8.dp))}
  item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Minimal","Glass","Neon","Gradient").forEach{x->FilterChip(selected=style==x,onClick={style=x;prefs(context).edit().putString(STYLE,x).apply()},label={Text(x)})}}}
  item{Text("Wallpaper",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=20.dp,bottom=10.dp))}
  item{OutlinedButton(onClick=onPickWallpaper,modifier=Modifier.fillMaxWidth()){Text("Choose Wallpaper Image")}}
  item{Button(onClick={prefs(context).edit().putInt(QUOTE_SIZE,size.toInt()).putInt(QUOTE_OFFSET,offset.toInt()).apply();onApplyWallpaper(position,style,size.toInt(),offset.toInt())},modifier=Modifier.fillMaxWidth().padding(top=16.dp)){Text("Apply Quote to Wallpaper")}}

  item{Text("Tip: size and position are remembered for your next wallpaper.",color=Color(0xFF9FAAD0),modifier=Modifier.padding(vertical=18.dp))}
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
   Row(Modifier.fillMaxWidth().padding(top=10.dp),verticalAlignment=Alignment.CenterVertically){Text("Dark theme",modifier=Modifier.weight(1f));Switch(checked=dark,onCheckedChange={toggle()})}
   HorizontalDivider(Modifier.padding(vertical=16.dp))
   Text("Widget style: "+(prefs(context).getString(STYLE,"Glass")?:"Glass"))
   HorizontalDivider(Modifier.padding(vertical=16.dp))
   Text("QuoteGlow",style=MaterialTheme.typography.titleMedium)
   Text("Custom quotes, home-screen widgets and live wallpaper.",color=Color(0xFF9FAAD0))
  }}
 }
}

// QuoteGlow feature update: resize, repositioning and dark theme controls.
