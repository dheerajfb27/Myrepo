package com.dheeraj.lockscreen;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
  static final String[] CATS={"All","Games","Animated","Characters","Nature","Cities","Cars","Neon","Space"};
  static final String[] THEMES={"Cyber Racer","Neon Ninja","Pixel Quest","Dragon Flame","Anime Sky","Forest Aurora","Ocean Glow","Mountain Dawn","Tokyo Night","New York Lights","Dubai Neon","Supercar Carbon","Electric GT","Retro Arcade","Cosmic Portal","Galaxy Drift","Liquid Neon","Aurora Glass"};
  public void onCreate(Bundle b){super.onCreate(b); build();}
  void build(){
    LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,32,24,16); root.setBackgroundColor(0xFF080A10);
    TextView title=t("DREAMLOCK",28,0xFFFFFFFF); root.addView(title,new LinearLayout.LayoutParams(-1,70));
    TextView sub=t("Choose a live wallpaper for your lock-screen experience",14,0xFFB8C0D0); root.addView(sub,new LinearLayout.LayoutParams(-1,50));
    HorizontalScrollView hsv=new HorizontalScrollView(this); LinearLayout cats=new LinearLayout(this); cats.setOrientation(LinearLayout.HORIZONTAL);
    for(String c:CATS){Button x=new Button(this);x.setText(c);x.setOnClickListener(v->toast(c+" wallpapers"));cats.addView(x,new LinearLayout.LayoutParams(-2,60));} hsv.addView(cats);root.addView(hsv);
    GridLayout grid=new GridLayout(this); grid.setColumnCount(2); grid.setUseDefaultMargins(true);
    for(String theme:THEMES){Button card=new Button(this);card.setText(theme+"\nLIVE");card.setTextSize(15);card.setOnClickListener(v->openPicker());grid.addView(card,new ViewGroup.LayoutParams(0,170)); GridLayout.LayoutParams p=(GridLayout.LayoutParams)card.getLayoutParams();p.width=0;p.height=170;p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);card.setLayoutParams(p);}
    ScrollView sv=new ScrollView(this);sv.addView(grid);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); 
    Button set=new Button(this);set.setText("SET WALLPAPER");set.setOnClickListener(v->openPicker());root.addView(set,new LinearLayout.LayoutParams(-1,64));setContentView(root);
  }
  TextView t(String s,int z,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
  void openPicker(){try{Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,LockWallpaperService.class));startActivity(i);}catch(Exception e){startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));}}
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}