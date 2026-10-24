package com.quoteglow.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.widget.RemoteViews
import kotlin.math.max

object QuoteGlowStore {
    private const val FILE = "quoteglow"
    private fun p(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    fun prefs(c: Context) = p(c)
    fun quote(c: Context,id:Int)=p(c).getString("quote_$id","Dream it. Build it. Live it.")?:"Dream it. Build it. Live it."
    fun author(c: Context,id:Int)=p(c).getString("author_$id","")?:""
    fun font(c: Context,id:Int)=p(c).getString("font_$id","Sans")?:"Sans"
    fun mode(c: Context,id:Int)=p(c).getString("mode_$id","Rainbow")?:"Rainbow"
    fun textOpacity(c: Context,id:Int)=p(c).getInt("textOpacity_$id",100).coerceIn(10,100)
    fun bgOpacity(c: Context,id:Int)=p(c).getInt("bgOpacity_$id",75).coerceIn(0,100)
    fun textSize(c: Context,id:Int)=p(c).getInt("textSize_$id",20).coerceIn(12,36)
    fun bgStyle(c: Context,id:Int)=p(c).getString("bgStyle_$id","Glass")?:"Glass"
}

private fun typeface(name:String):Typeface=when(name){"Serif"->Typeface.create("serif",Typeface.NORMAL);"Elegant"->Typeface.create("serif",Typeface.ITALIC);"Mono"->Typeface.create("monospace",Typeface.BOLD);"Handwritten"->Typeface.create("cursive",Typeface.NORMAL);"Bold"->Typeface.create("sans-serif",Typeface.BOLD);else->Typeface.create("sans-serif",Typeface.NORMAL)}
private fun colors(mode:String)=when(mode){"Rainbow"->intArrayOf(0xFFFF3B30.toInt(),0xFFFFCC00.toInt(),0xFF34C759.toInt(),0xFF00C7BE.toInt(),0xFF5856D6.toInt(),0xFFFF2D55.toInt());"Ocean"->intArrayOf(0xFF00D4FF.toInt(),0xFF4776E6.toInt());"Sunset"->intArrayOf(0xFFFF512F.toInt(),0xFFF09819.toInt());"Purple"->intArrayOf(0xFF8E2DE2.toInt(),0xFFDA22FF.toInt());"Mint"->intArrayOf(0xFF00F5A0.toInt(),0xFF00D9F5.toInt());"Gold"->intArrayOf(0xFFFFD166.toInt(),0xFFFF8C42.toInt());else->intArrayOf(Color.WHITE)}
private fun background(style:String)=when(style){"Neon"->0xFF250033.toInt();"Gradient"->0xFF162B45.toInt();"AMOLED"->Color.BLACK;"Elegant"->0xFF272033.toInt();"Handwritten"->0xFF342A23.toInt();"Bold"->0xFF18212D.toInt();"Retro"->0xFF443022.toInt();"Transparent"->0xFF111827.toInt();else->0xFF20283A.toInt()}

object QuoteGlowRenderer {
    fun render(c:Context,id:Int,width:Int,height:Int):Bitmap{
        val w=max(width,180); val h=max(height,70); val b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); val canvas=Canvas(b)
        val alpha=QuoteGlowStore.bgOpacity(c,id)*255/100
        val card=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=(alpha shl 24) or (background(QuoteGlowStore.bgStyle(c,id)) and 0x00FFFFFF)}
        canvas.drawRoundRect(RectF(2f,2f,w-2f,h-2f),28f,28f,card)
        val size=QuoteGlowStore.textSize(c,id)*c.resources.displayMetrics.scaledDensity
        val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=typeface(QuoteGlowStore.font(c,id));textSize=size;textAlign=Paint.Align.CENTER}
        val cs=colors(QuoteGlowStore.mode(c,id))
        paint.shader=if(cs.size>1)LinearGradient(0f,0f,w.toFloat(),0f,cs,null,Shader.TileMode.CLAMP)else null
        if(cs.size==1)paint.color=(QuoteGlowStore.textOpacity(c,id)*255/100 shl 24) or (cs[0] and 0x00FFFFFF)
        val maxWidth=w*.88f; val lines=mutableListOf<String>(); var line=""
        for(word in QuoteGlowStore.quote(c,id).split(" ")){val t=if(line.isEmpty())word else "$line $word";if(paint.measureText(t)<=maxWidth)line=t else{if(line.isNotEmpty())lines.add(line);line=word}};if(line.isNotEmpty())lines.add(line)
        val lh=size*1.18f; val author=QuoteGlowStore.author(c,id); val total=lines.size*lh+if(author.isBlank())0f:size*.8f;var y=(h-total)/2f+size
        lines.forEach{canvas.drawText("❝ $it ❞",w/2f,y,paint);y+=lh}
        if(author.isNotBlank()){paint.shader=null;paint.color=(QuoteGlowStore.textOpacity(c,id)*255/100 shl 24) or 0x00DDDDDD;paint.textSize=size*.58f;canvas.drawText("— $author",w/2f,y,paint)}
        return b
    }
}

open class QuoteWidgetReceiver:AppWidgetProvider(){
    override fun onUpdate(c:Context,m:AppWidgetManager,ids:IntArray){ids.forEach{update(c,m,it)}}
    override fun onAppWidgetOptionsChanged(c:Context,m:AppWidgetManager,id:Int,o:Bundle){update(c,m,id)}
    companion object{fun update(c:Context,m:AppWidgetManager,id:Int){val o=m.getAppWidgetOptions(id);val d=c.resources.displayMetrics.density;val w=(o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,320)*d).toInt();val h=(o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,80)*d).toInt();val v=RemoteViews(c.packageName,R.layout.quote_widget);v.setImageViewBitmap(R.id.quote_image,QuoteGlowRenderer.render(c,id,w,h));m.updateAppWidget(id,v)}}}
class QuoteWidget4x1Receiver:QuoteWidgetReceiver()
