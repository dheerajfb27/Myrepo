package com.dheeraj.lavaglow;
import android.graphics.*; import android.os.Handler; import android.service.wallpaper.WallpaperService; import android.view.SurfaceHolder;
public class LavaGlowWallpaperService extends WallpaperService {
 public Engine onCreateEngine(){return new LavaEngine();}
 class LavaEngine extends Engine { Handler h=new Handler(); Paint p=new Paint(3); float t; boolean on;
  Runnable r=()->drawFrame(); public void onVisibilityChanged(boolean v){on=v;if(v)drawFrame();else h.removeCallbacks(r);} public void onSurfaceChanged(SurfaceHolder s,int f,int w,int he){drawFrame();} public void onSurfaceDestroyed(SurfaceHolder s){on=false;h.removeCallbacks(r);}
  void drawFrame(){Canvas c=null;try{c=getSurfaceHolder().lockCanvas();if(c!=null)render(c,c.getWidth(),c.getHeight());}finally{if(c!=null)getSurfaceHolder().unlockCanvasAndPost(c);}t+=.035f;h.removeCallbacks(r);if(on)h.postDelayed(r,33);}
  void render(Canvas c,float w,float h){p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,0,w,h,Color.rgb(4,7,18),Color.rgb(35,4,50),Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);float cx=w/2,top=h*.12f,bottom=h*.88f,lw=Math.min(w*.62f,h*.78f);
   p.setColor(Color.rgb(0,220,255));p.setAlpha(35);p.setMaskFilter(new BlurMaskFilter(lw*.28f,BlurMaskFilter.Blur.NORMAL));c.drawCircle(cx,h*.52f,lw*.38f,p);p.setMaskFilter(null);
   Path g=new Path();g.moveTo(cx-lw*.18f,top);g.cubicTo(cx-lw*.30f,top+lw*.15f,cx-lw*.30f,bottom-lw*.14f,cx-lw*.18f,bottom);g.quadTo(cx,bottom+lw*.06f,cx+lw*.18f,bottom);g.cubicTo(cx+lw*.30f,bottom-lw*.14f,cx+lw*.30f,top+lw*.15f,cx+lw*.18f,top);g.close();p.setShader(new LinearGradient(cx-lw*.3f,top,cx+lw*.3f,bottom,Color.argb(85,40,235,255),Color.argb(22,255,0,180),Shader.TileMode.CLAMP));c.drawPath(g,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(lw*.018f);p.setColor(Color.argb(170,190,255,255));c.drawPath(g,p);p.setStyle(Paint.Style.FILL);
   for(int i=0;i<7;i++){float q=i*1.1f;float y=top+lw*.08f+(float)(.5+.5*Math.sin(t*(.55+i*.06)+q))*(bottom-top-lw*.15f);float x=cx+(float)Math.sin(t*(.35+i*.05)+q)*lw*.13f;float rr=lw*(.05f+.018f*(float)Math.sin(t+q));int col=i%3==0?Color.rgb(255,50,170):i%3==1?Color.rgb(0,235,255):Color.rgb(145,60,255);p.setColor(col);p.setAlpha(235);p.setMaskFilter(new BlurMaskFilter(lw*.035f,BlurMaskFilter.Blur.NORMAL));c.drawOval(x-rr*1.15f,y-rr*1.35f,x+rr*1.15f,y+rr*1.35f,p);p.setMaskFilter(null);}
   p.setAlpha(255);p.setShader(new LinearGradient(cx-lw*.31f,bottom,cx+lw*.31f,bottom+lw*.16f,Color.rgb(12,15,28),Color.rgb(90,110,130),Shader.TileMode.CLAMP));c.drawRoundRect(cx-lw*.31f,bottom-lw*.02f,cx+lw*.31f,bottom+lw*.16f,lw*.05f,lw*.05f,p);c.drawRoundRect(cx-lw*.20f,top-lw*.035f,cx+lw*.20f,top+lw*.035f,lw*.03f,lw*.03f,p);p.setShader(null);p.setColor(Color.argb(90,255,255,255));c.drawRoundRect(cx-lw*.20f,top+lw*.05f,cx-lw*.16f,bottom-lw*.1f,lw*.02f,lw*.02f,p);
  }
 }
}
