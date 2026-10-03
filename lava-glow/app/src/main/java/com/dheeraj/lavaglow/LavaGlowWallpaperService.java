package com.dheeraj.lavaglow;
import android.graphics.*;import android.os.*;import android.service.wallpaper.WallpaperService;import android.view.SurfaceHolder;
public class LavaGlowWallpaperService extends WallpaperService{
 public Engine onCreateEngine(){return new LavaEngine();}
 class LavaEngine extends Engine{
  Handler h=new Handler();Paint p=new Paint(3);float t;boolean on;Runnable r=()->drawFrame();
  public void onVisibilityChanged(boolean v){on=v;if(v)drawFrame();else h.removeCallbacks(r);}
  public void onSurfaceChanged(SurfaceHolder s,int f,int w,int he){drawFrame();}
  public void onSurfaceDestroyed(SurfaceHolder s){on=false;h.removeCallbacks(r);}
  void drawFrame(){Canvas c=null;try{c=getSurfaceHolder().lockCanvas();if(c!=null)render(c,c.getWidth(),c.getHeight());}finally{if(c!=null)getSurfaceHolder().unlockCanvasAndPost(c);}t+=.018f;h.removeCallbacks(r);if(on)h.postDelayed(r,33);}
  void weather(Canvas c,float w,float h){p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(w*.042f);p.setColor(Color.WHITE);p.setAlpha(235);c.drawText("☀  24°  •  Partly Cloudy",w/2,h*.93f,p);}
  void render(Canvas c,float w,float h){
   // Full-screen animated abstract neon background; no lamp, clock, date, quote, or other UI.
   int[] a={Color.rgb(3,7,24),Color.rgb(30,3,48),Color.rgb(2,34,55),Color.rgb(45,4,28)};
   p.setShader(new LinearGradient(0,0,w,h,a[(int)(t*.12f)%a.length],a[((int)(t*.12f)+1)%a.length],Shader.TileMode.MIRROR));c.drawRect(0,0,w,h,p);p.setShader(null);
   for(int i=0;i<18;i++){
    float phase=i*.73f;
    float x=w*(.5f+.48f*(float)Math.sin(t*(.20f+i*.013f)+phase));
    float y=h*(.5f+.55f*(float)Math.cos(t*(.16f+i*.011f)+phase*1.7f));
    float rr=Math.min(w,h)*(.045f+.018f*(float)Math.sin(t*.7f+phase));
    int col=i%4==0?Color.rgb(255,30,180):i%4==1?Color.rgb(0,235,255):i%4==2?Color.rgb(135,50,255):Color.rgb(30,255,145);
    p.setColor(col);p.setAlpha(125);p.setMaskFilter(new BlurMaskFilter(rr*2.8f,BlurMaskFilter.Blur.NORMAL));c.drawCircle(x,y,rr,p);p.setMaskFilter(null);
    p.setAlpha(180);c.drawCircle(x,y,rr*.28f,p);
   }
   // Slow-moving luminous waves.
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,w*.004f));
   for(int k=0;k<5;k++){Path path=new Path();for(int x=0;x<=w;x+=Math.max(8,(int)w/90)){float yy=h*(.15f+k*.18f)+h*.055f*(float)Math.sin(x/w*7+t*.65f+k);if(x==0)path.moveTo(x,yy);else path.lineTo(x,yy);}p.setColor(Color.argb(75,80,230,255));c.drawPath(path,p);}p.setStyle(Paint.Style.FILL);
   // Keep weather as the only visible UI element.
   weather(c,w,h);
  }
 }
}