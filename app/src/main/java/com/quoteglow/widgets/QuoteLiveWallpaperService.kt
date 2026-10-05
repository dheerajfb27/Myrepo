package com.quoteglow.widgets
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
class QuoteLiveWallpaperService:WallpaperService(){
 override fun onCreateEngine():Engine=object:Engine(){
  override fun onVisibilityChanged(visible:Boolean){if(visible)draw()}
  override fun onSurfaceChanged(holder:SurfaceHolder,format:Int,width:Int,height:Int){draw()}
  private fun draw(){val c=surfaceHolder.lockCanvas()?:return;try{c.drawColor(android.graphics.Color.rgb(7,10,24));val p=android.graphics.Paint().apply{color=android.graphics.Color.WHITE;textSize=58f;textAlign=android.graphics.Paint.Align.CENTER;isAntiAlias=true};c.drawText("Small steps",c.width/2f,c.height/2f-30,p);c.drawText("every day lead",c.width/2f,c.height/2f+40,p);c.drawText("to big results.",c.width/2f,c.height/2f+110,p)}finally{surfaceHolder.unlockCanvasAndPost(c)}}
 }
}