package com.quoteglow.widgets

import android.graphics.Color
import android.graphics.Paint
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class QuoteLiveWallpaperService: WallpaperService() {
 override fun onCreateEngine(): Engine = QuoteEngine()

 inner class QuoteEngine: Engine() {
  private var visible = false

  override fun onVisibilityChanged(isVisible: Boolean) {
   visible = isVisible
   if (isVisible) draw()
  }

  override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
   super.onSurfaceChanged(holder, format, width, height)
   draw()
  }

  override fun onSurfaceCreated(holder: SurfaceHolder) {
   super.onSurfaceCreated(holder)
   draw()
  }

  override fun onSurfaceDestroyed(holder: SurfaceHolder) {
   visible = false
   super.onSurfaceDestroyed(holder)
  }

  private fun draw() {
   val holder = surfaceHolder
   if (!visible && holder.surface == null) return
   val canvas = try { holder.lockCanvas() } catch (_: Exception) { null } ?: return
   try {
    canvas.drawColor(Color.rgb(7, 10, 24))
    val prefs = getSharedPreferences("quoteglow_prefs", MODE_PRIVATE)
    val quote = prefs.getString("selected_quote", "Small steps every day lead to big results.")
      ?: "Small steps every day lead to big results."

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
     color = Color.WHITE
     textSize = (canvas.width.coerceAtMost(canvas.height) * 0.075f).coerceAtLeast(34f)
     textAlign = Paint.Align.CENTER
     isFakeBoldText = true
     setShadowLayer(12f, 2f, 2f, Color.BLACK)
    }

    val maxWidth = canvas.width * 0.82f
    val words = quote.split(" ")
    val lines = mutableListOf<String>()
    var line = ""
    for (word in words) {
     val candidate = if (line.isEmpty()) word else "$line $word"
     if (paint.measureText(candidate) <= maxWidth) line = candidate
     else {
      if (line.isNotEmpty()) lines.add(line)
      line = word
     }
    }
    if (line.isNotEmpty()) lines.add(line)

    val lineHeight = paint.textSize * 1.3f
    var y = canvas.height / 2f - (lines.size - 1) * lineHeight / 2f
    for (text in lines) {
     canvas.drawText(text, canvas.width / 2f, y, paint)
     y += lineHeight
    }
   } finally {
    holder.unlockCanvasAndPost(canvas)
   }
  }
 }
}