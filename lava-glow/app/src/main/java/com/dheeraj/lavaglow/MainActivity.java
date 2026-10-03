package com.dheeraj.lavaglow;
import android.app.*;import android.app.WallpaperManager;import android.content.*;import android.os.*;import android.service.wallpaper.WallpaperService;import android.view.*;import android.widget.*;
public class MainActivity extends Activity{
 public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);findViewById(R.id.setWallpaper).setOnClickListener(v->{Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,LavaGlowWallpaperService.class));startActivity(i);});}
}