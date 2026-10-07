package com.sunweather.app;

import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.view.View;
import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;
import org.json.*;

public class WeatherView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler=new Handler(Looper.getMainLooper());
    private double temp=28, feels=29, humidity=62, wind=11, max=30, min=21;
    private int code=2;
    private String clock="", date="";
    private boolean loading=true;

    public WeatherView(Context c){ super(c); p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL)); fetch(); tick(); }

    private void tick(){
        Date now=new Date();
        clock=new SimpleDateFormat("hh:mm",Locale.US).format(now);
        date=new SimpleDateFormat("EEE, dd MMM yyyy",Locale.US).format(now).toUpperCase(Locale.US);
        invalidate(); handler.postDelayed(this::tick,1000);
    }

    private void fetch(){
        new Thread(() -> {
            try{
                URL u=new URL("https://api.open-meteo.com/v1/forecast?latitude=12.9716&longitude=77.5946&current=temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=4");
                HttpURLConnection c=(HttpURLConnection)u.openConnection();
                c.setConnectTimeout(8000); c.setReadTimeout(8000);
                JSONObject j=new JSONObject(read(c.getInputStream()));
                JSONObject cur=j.getJSONObject("current");
                JSONObject daily=j.getJSONObject("daily");
                temp=cur.getDouble("temperature_2m"); feels=cur.getDouble("apparent_temperature");
                humidity=cur.getDouble("relative_humidity_2m"); wind=cur.getDouble("wind_speed_10m"); code=cur.getInt("weather_code");
                max=daily.getJSONArray("temperature_2m_max").getDouble(0); min=daily.getJSONArray("temperature_2m_min").getDouble(0);
                loading=false;
            }catch(Exception ignored){} runOnUi();
        }).start();
    }
    private String read(InputStream in)throws Exception{
        BufferedReader r=new BufferedReader(new InputStreamReader(in)); StringBuilder b=new StringBuilder(); String s;
        while((s=r.readLine())!=null)b.append(s); r.close(); return b.toString();
    }
    private void runOnUi(){ post(this::invalidate); }

    private String condition(){
        if(code==0)return "Clear sky"; if(code<=3)return "Partly cloudy"; if(code<=48)return "Foggy";
        if(code<=67)return "Rain"; if(code<=77)return "Snow"; if(code<=82)return "Rain showers"; return "Thunderstorm";
    }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c); float w=getWidth(), h=getHeight();
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(8,105,126)); c.drawRect(0,0,w,h,p);

        p.setColor(Color.rgb(53,84,81)); c.drawRect(0,0,w,74,p);
        text(c,"SUN WEATHER",24,40,Color.WHITE,20,Typeface.BOLD);
        text(c,"LIVE",w-58,40,Color.rgb(146,235,216),11,Typeface.BOLD);

        p.setColor(Color.rgb(23,25,24)); c.drawRoundRect(3,92,w-3,210,25,25,p);
        pill(c,16,115,65,199,"MON",Color.rgb(239,138,34),24);
        pill(c,71,109,126,199,"TUE",Color.rgb(18,106,132),30);
        pill(c,132,103,187,199,"WED",Color.rgb(116,73,199),36);
        text(c,"FORECAST",210,124,Color.LTGRAY,11,Typeface.BOLD);
        text(c,String.format(Locale.US,"%.0f°",max),210,164,Color.WHITE,28,Typeface.BOLD);
        text(c,String.format(Locale.US,"%.0f°",min),270,164,Color.LTGRAY,18,Typeface.NORMAL);

        text(c,String.format(Locale.US,"%.0f°",temp),24,286,Color.WHITE,62,Typeface.BOLD);
        text(c,condition(),27,318,Color.WHITE,18,Typeface.BOLD);
        text(c,clock,24,374,Color.WHITE,52,Typeface.BOLD);
        text(c,date,27,401,Color.rgb(183,222,219),11,Typeface.BOLD);

        // stylized sun/cloud/hills
        p.setColor(Color.rgb(255,193,54)); c.drawCircle(w-92,335,43,p);
        p.setColor(Color.rgb(235,248,246)); c.drawCircle(w-165,410,35,p); c.drawCircle(w-128,396,45,p); c.drawCircle(w-92,412,31,p);
        Path hill=new Path(); hill.moveTo(0,465); hill.quadTo(w*.25f,390,w*.48f,470); hill.quadTo(w*.72f,390,w,460); hill.lineTo(w,560); hill.lineTo(0,560); hill.close();
        p.setColor(Color.rgb(43,119,105)); c.drawPath(hill,p);
        p.setColor(Color.rgb(40,40,39)); Path road=new Path(); road.moveTo(w*.38f,560); road.lineTo(w*.62f,560); road.lineTo(w*.55f,470); road.lineTo(w*.45f,470); road.close(); c.drawPath(road,p);

        p.setColor(Color.rgb(23,25,24)); c.drawRoundRect(0,575,w,h,28,28,p);
        text(c,"HUMIDITY",24,615,Color.GRAY,10,Typeface.BOLD); text(c,String.format(Locale.US,"%.0f%%",humidity),24,645,Color.WHITE,20,Typeface.BOLD);
        text(c,"WIND",w/2-25,615,Color.GRAY,10,Typeface.BOLD); text(c,String.format(Locale.US,"%.0f km/h",wind),w/2-25,645,Color.WHITE,20,Typeface.BOLD);
        text(c,"FEELS LIKE",w-120,615,Color.GRAY,10,Typeface.BOLD); text(c,String.format(Locale.US,"%.0f°",feels),w-95,645,Color.WHITE,20,Typeface.BOLD);
        text(c,"BENGALURU",24,h-25,Color.WHITE,13,Typeface.BOLD);
        text(c,loading?"Updating":"● Updated",w-110,h-25,Color.rgb(146,235,216),11,Typeface.BOLD);
    }

    private void pill(Canvas c,float l,float t,float r,float b,String s,int color,float radius){
        p.setColor(color); c.drawRoundRect(l,t,r,b,radius,radius,p);
        text(c,s,l+9,b-12,Color.WHITE,10,Typeface.BOLD);
    }
    private void text(Canvas c,String s,float x,float y,int color,float size,int style){
        p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif",style));p.setStyle(Paint.Style.FILL);c.drawText(s,x,y,p);
    }
}