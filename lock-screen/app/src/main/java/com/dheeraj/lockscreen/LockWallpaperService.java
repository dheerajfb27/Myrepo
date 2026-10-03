package com.dheeraj.lockscreen;

import android.graphics.*;import android.os.*;import android.service.wallpaper.WallpaperService;import android.view.SurfaceHolder;import java.text.SimpleDateFormat;import java.util.*;

public class LockWallpaperService extends WallpaperService {
 public Engine onCreateEngine(){return new E();}
 class E extends Engine{
  Paint p=new Paint(3); Handler h=new Handler(Looper.getMainLooper()); boolean vis; float phase;
  Runnable draw=new Runnable(){public void run(){render();if(vis)h.postDelayed(this,33);}};
  public void onVisibilityChanged(boolean v){vis=v;if(v)draw.run();else h.removeCallbacks(draw);}
  public void onSurfaceChanged(SurfaceHolder sh,int f,int w,int ht){render();}
  public void onSurfaceDestroyed(SurfaceHolder sh){vis=false;h.removeCallbacks(draw);}
  void render(){SurfaceHolder sh=getSurfaceHolder();Canvas c=null;try{c=sh.lockCanvas();if(c==null)return;int w=c.getWidth(),ht=c.getHeight();
    float t=phase+=0.025f; LinearGradient g=new LinearGradient(0,0,w,ht,new int[]{0xFF050816,0xFF12062A,0xFF061B2C,0xFF020308},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,ht,p);p.setShader(null);
    for(int i=0;i<8;i++){float x=(float)(w*(0.15+i*0.12)+Math.sin(t+i)*90),y=ht*(0.18f+i*0.09f);p.setColor(Color.HSVToColor(new float[]{(t*35+i*45)%360,0.7f,0.9f}));p.setAlpha(80);p.setMaskFilter(new BlurMaskFilter(55,BlurMaskFilter.Blur.NORMAL));c.drawCircle(x,y,110,p);}p.setMaskFilter(null);p.setAlpha(255);
    String time=new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date());String date=new SimpleDateFormat("EEE, dd MMM",Locale.getDefault()).format(new Date());
    p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.min(w,ht)*0.15f);p.setColor(Color.WHITE);c.drawText(time,w/2f,ht*0.23f,p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(28);p.setColor(0xFFE0E7FF);c.drawText(date,w/2f,ht*0.28f,p);
    p.setTextSize(24);p.setColor(0xFFBFD8FF);c.drawText("☀ 24°  •  Partly Cloudy",w/2f,ht*0.88f,p);p.setTextSize(18);p.setColor(0xFF9AA6BD);c.drawText("Stay curious • Stay moving",w/2f,ht*0.93f,p);
   }finally{if(c!=null)sh.unlockCanvasAndPost(c);}}
 }
}
