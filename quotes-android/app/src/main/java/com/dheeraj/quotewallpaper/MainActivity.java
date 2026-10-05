package com.dheeraj.quotewallpaper;

import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;

public class MainActivity extends AppCompatActivity {
    FrameLayout preview; ImageView wallpaper; TextView quoteView; EditText input;
    Bitmap source; int position=Gravity.CENTER, color=Color.WHITE, font=0;
    final int[] colors={Color.WHITE,Color.BLACK,Color.rgb(255,70,120),Color.rgb(0,230,180),Color.rgb(255,190,0),Color.rgb(110,150,255),Color.rgb(190,100,255)};
    final String[] fonts={"Bold","Cursive","Serif","Mono"};
    ActivityResultLauncher<String> picker=registerForActivityResult(new ActivityResultContracts.GetContent(),u->{if(u!=null)load(u);});

    public void onCreate(Bundle b){super.onCreate(b); ui();}

    TextView text(String s,float z){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.WHITE);return v;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(12);return b;}

    void ui(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(16,12,16,10);root.setBackgroundColor(Color.rgb(8,9,13));
        TextView title=text("Quote Wallpaper",23);title.setTypeface(Typeface.DEFAULT_BOLD);root.addView(title,new LinearLayout.LayoutParams(-1,52));

        preview=new FrameLayout(this);preview.setBackgroundColor(Color.DKGRAY);
        wallpaper=new ImageView(this);wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);preview.addView(wallpaper,new FrameLayout.LayoutParams(-1,-1));
        quoteView=text("Your quote ✨",30);quoteView.setGravity(Gravity.CENTER);quoteView.setTypeface(Typeface.DEFAULT_BOLD);quoteView.setPadding(25,20,25,20);preview.addView(quoteView,new FrameLayout.LayoutParams(-1,-1));
        root.addView(preview,new LinearLayout.LayoutParams(-1,0,1));

        input=new EditText(this);input.setHint("Type your quote...");input.setTextColor(Color.WHITE);input.setHintTextColor(Color.GRAY);input.setTextSize(17);input.setMaxLines(3);
        input.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){quoteView.setText(s.length()==0?"Your quote ✨":s.toString());}public void afterTextChanged(android.text.Editable e){}});
        root.addView(input,new LinearLayout.LayoutParams(-1,62));

        LinearLayout p=new LinearLayout(this);
        String[] ps={"TOP","CENTER","BOTTOM"};for(int i=0;i<3;i++){Button b=btn(ps[i]);final int q=i;b.setOnClickListener(v->{position=q==0?Gravity.TOP:q==1?Gravity.CENTER:Gravity.BOTTOM;pos();});p.addView(b,new LinearLayout.LayoutParams(0,48,1));}root.addView(p);

        LinearLayout row=new LinearLayout(this);
        Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,fonts));
        sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,View v,int p,long id){font=p;font();}});
        row.addView(sp,new LinearLayout.LayoutParams(0,52,1));
        Button wp=btn("WALLPAPER");wp.setOnClickListener(v->picker.launch("image/*"));row.addView(wp,new LinearLayout.LayoutParams(0,52,1));root.addView(row);

        root.addView(text("COLOR",11),new LinearLayout.LayoutParams(-1,28));
        LinearLayout cr=new LinearLayout(this);cr.setGravity(Gravity.CENTER);
        for(int c:colors){TextView d=text("●",30);d.setTextColor(c);d.setGravity(Gravity.CENTER);d.setOnClickListener(v->{color=c;quoteView.setTextColor(c);});cr.addView(d,new LinearLayout.LayoutParams(44,42));}root.addView(cr);

        SeekBar size=new SeekBar(this);size.setMax(50);size.setProgress(18);size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){quoteView.setTextSize(18+p);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});root.addView(size,new LinearLayout.LayoutParams(-1,40));

        Button save=btn("SAVE WALLPAPER");save.setOnClickListener(v->save());root.addView(save,new LinearLayout.LayoutParams(-1,54));
        setContentView(root);
    }

    void pos(){FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)quoteView.getLayoutParams();lp.gravity=position;quoteView.setLayoutParams(lp);}
    void font(){String n=font==1?"cursive":font==2?"serif":font==3?"monospace":"sans";quoteView.setTypeface(Typeface.create(n,Typeface.BOLD));quoteView.setShadowLayer(10,3,3,Color.BLACK);}
    void load(Uri u){try{source=MediaStore.Images.Media.getBitmap(getContentResolver(),u);wallpaper.setImageBitmap(source);}catch(Exception e){toast("Could not load image");}}
    void save(){
        if(source==null){toast("Choose a wallpaper first");return;}
        Bitmap out=Bitmap.createBitmap(preview.getWidth(),preview.getHeight(),Bitmap.Config.ARGB_8888);preview.draw(new Canvas(out));
        String name="QuoteWallpaper_"+System.currentTimeMillis()+".jpg";try{
            ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,name);v.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
            if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Quote Wallpaper");
            Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);OutputStream os=getContentResolver().openOutputStream(u);out.compress(Bitmap.CompressFormat.JPEG,95,os);os.close();toast("Saved to Pictures/Quote Wallpaper");
        }catch(Exception e){toast("Save failed");}
    }
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
