package com.dheeraj.quotewallpaper;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
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
    FrameLayout preview;
    ImageView wallpaper;
    TextView quoteView;
    EditText input;
    Bitmap source;
    int position = Gravity.CENTER, quoteColor = Color.WHITE, fontIndex = 0;
    float quoteSize = 30f;

    final int[] colors = {
        Color.WHITE, Color.BLACK, Color.rgb(255,70,120),
        Color.rgb(0,225,180), Color.rgb(255,190,0),
        Color.rgb(110,150,255), Color.rgb(190,100,255)
    };
    final String[] fonts = {"Bold", "Cursive", "Serif", "Mono"};
    final String[] builtInQuotes = {
        "Believe in yourself ✨",
        "Make today beautiful 🌸",
        "Dream big. Start now. 🚀",
        "Create your own sunshine ☀️",
        "Stay wild, stay free 🌈",
        "Good things take time 💫",
        "Your vibe attracts your tribe ✨",
        "Be the reason someone smiles ❤️",
        "Life is better with a little magic 🪄",
        "Keep going. You are closer than you think 💪"
    };

    ActivityResultLauncher<String> picker =
        registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) loadWallpaper(uri); });

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(8,9,13));
        getWindow().setNavigationBarColor(Color.rgb(8,9,13));
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
    }

    TextView label(String s, float size) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(size); v.setTextColor(Color.WHITE);
        return v;
    }

    Button actionButton(String s) {
        Button b = new Button(this);
        b.setText(s); b.setTextSize(12); b.setAllCaps(false);
        return b;
    }

    TextView chip(String s) {
        TextView v = label(s, 13);
        v.setGravity(Gravity.CENTER);
        v.setPadding(16, 0, 16, 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(32,35,44));
        bg.setCornerRadius(22);
        v.setBackground(bg);
        return v;
    }

    void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(8,9,13));

        // Safe header: no overlap with status bar.
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(18, 10, 18, 8);

        TextView title = label("Quote Wallpaper", 22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, 52, 1));

        Button choose = actionButton("🖼  Wallpaper");
        choose.setOnClickListener(v -> picker.launch("image/*"));
        header.addView(choose, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 48));
        root.addView(header);

        // Compact portrait preview. Controls remain visible.
        preview = new FrameLayout(this);
        GradientDrawable previewBg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(38,40,52), Color.rgb(18,20,28)});
        preview.setBackground(previewBg);

        wallpaper = new ImageView(this);
        wallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));

        quoteView = label("Your quote ✨", quoteSize);
        quoteView.setGravity(Gravity.CENTER);
        quoteView.setTypeface(Typeface.DEFAULT_BOLD);
        quoteView.setPadding(30, 22, 30, 22);
        quoteView.setTextColor(quoteColor);
        quoteView.setShadowLayer(10, 3, 3, Color.BLACK);
        preview.addView(quoteView, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout.LayoutParams previewLp =
            new LinearLayout.LayoutParams(-1, 0, 0.48f);
        previewLp.setMargins(12, 0, 12, 8);
        root.addView(preview, previewLp);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout editor = new LinearLayout(this);
        editor.setOrientation(LinearLayout.VERTICAL);
        editor.setPadding(16, 0, 16, 18);

        // Quote input
        editor.addView(label("YOUR QUOTE", 12),
            new LinearLayout.LayoutParams(-1, 28));
        input = new EditText(this);
        input.setHint("Type your own quote...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.rgb(145,145,155));
        input.setTextSize(16);
        input.setSingleLine(false);
        input.setMaxLines(3);
        input.setPadding(16, 8, 16, 8);
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.rgb(25,27,34));
        inputBg.setCornerRadius(14);
        input.setBackground(inputBg);
        input.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int c,int d){}
            public void onTextChanged(CharSequence s,int a,int b,int c) {
                quoteView.setText(s.length()==0 ? "Your quote ✨" : s.toString());
            }
            public void afterTextChanged(android.text.Editable e){}
        });
        editor.addView(input, new LinearLayout.LayoutParams(-1, 64));

        // Built-in quotes
        TextView builtTitle = label("BUILT-IN QUOTES", 12);
        builtTitle.setPadding(0, 12, 0, 4);
        editor.addView(builtTitle, new LinearLayout.LayoutParams(-1, 36));

        HorizontalScrollView quoteScroll = new HorizontalScrollView(this);
        quoteScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout quoteRow = new LinearLayout(this);
        quoteRow.setOrientation(LinearLayout.HORIZONTAL);

        for (String q : builtInQuotes) {
            TextView card = chip(q);
            card.setOnClickListener(v -> {
                String selected = ((TextView)v).getText().toString();
                input.setText(selected);
                input.setSelection(input.length());
            });
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 46);
            cp.setMargins(0, 0, 8, 0);
            quoteRow.addView(card, cp);
        }
        quoteScroll.addView(quoteRow);
        editor.addView(quoteScroll, new LinearLayout.LayoutParams(-1, 54));

        // Position
        editor.addView(section("POSITION"));
        LinearLayout positions = new LinearLayout(this);
        String[] ps = {"TOP", "CENTER", "BOTTOM"};
        for (int i=0; i<3; i++) {
            Button b = actionButton(ps[i]);
            final int p=i;
            b.setOnClickListener(v -> {
                position = p==0 ? Gravity.TOP : p==1 ? Gravity.CENTER : Gravity.BOTTOM;
                updatePosition();
            });
            positions.addView(b, new LinearLayout.LayoutParams(0, 48, 1));
        }
        editor.addView(positions);

        // Font
        editor.addView(section("FONT STYLE"));
        HorizontalScrollView fontScroll = new HorizontalScrollView(this);
        fontScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout fontRow = new LinearLayout(this);
        for (int i=0; i<fonts.length; i++) {
            TextView f = chip(fonts[i]);
            final int fi=i;
            f.setOnClickListener(v -> { fontIndex=fi; applyFont(); });
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 44);
            fp.setMargins(0,0,8,0);
            fontRow.addView(f,fp);
        }
        fontScroll.addView(fontRow);
        editor.addView(fontScroll, new LinearLayout.LayoutParams(-1, 50));

        // Color
        editor.addView(section("TEXT COLOR"));
        LinearLayout colorRow = new LinearLayout(this);
        colorRow.setGravity(Gravity.CENTER_VERTICAL);
        for (int c : colors) {
            TextView dot = label("●", 30);
            dot.setGravity(Gravity.CENTER);
            dot.setTextColor(c);
            dot.setOnClickListener(v -> {
                quoteColor = ((TextView)v).getCurrentTextColor();
                quoteView.setTextColor(quoteColor);
            });
            colorRow.addView(dot, new LinearLayout.LayoutParams(48, 46));
        }
        editor.addView(colorRow);

        // Size
        editor.addView(section("TEXT SIZE"));
        SeekBar size = new SeekBar(this);
        size.setMax(46);
        size.setProgress(12);
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s,int p,boolean fromUser) {
                quoteSize = 18 + p;
                quoteView.setTextSize(quoteSize);
            }
            public void onStartTrackingTouch(SeekBar s){}
            public void onStopTrackingTouch(SeekBar s){}
        });
        editor.addView(size, new LinearLayout.LayoutParams(-1, 42));

        // Save
        Button save = actionButton("💾  SAVE WALLPAPER");
        save.setTextSize(15);
        save.setTypeface(Typeface.DEFAULT_BOLD);
        save.setOnClickListener(v -> saveWallpaper());
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(-1, 56);
        saveLp.setMargins(0, 12, 0, 0);
        editor.addView(save, saveLp);

        scroll.addView(editor);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 0.52f));
        setContentView(root);
    }

    TextView section(String s) {
        TextView v = label(s, 12);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setPadding(0, 10, 0, 3);
        return v;
    }

    void updatePosition() {
        FrameLayout.LayoutParams lp =
            (FrameLayout.LayoutParams) quoteView.getLayoutParams();
        lp.gravity = position;
        quoteView.setLayoutParams(lp);
    }

    void applyFont() {
        String name = fontIndex==1 ? "cursive" :
                      fontIndex==2 ? "serif" :
                      fontIndex==3 ? "monospace" : "sans";
        quoteView.setTypeface(Typeface.create(name, Typeface.BOLD));
        quoteView.setShadowLayer(10, 3, 3, Color.BLACK);
    }

    void loadWallpaper(Uri uri) {
        try {
            source = MediaStore.Images.Media.getBitmap(
                getContentResolver(), uri);
            wallpaper.setImageBitmap(source);
        } catch (Exception e) {
            toast("Could not load wallpaper");
        }
    }

    void saveWallpaper() {
        if (source == null) {
            toast("Choose a wallpaper first");
            return;
        }

        Bitmap out = Bitmap.createBitmap(
            preview.getWidth(), preview.getHeight(),
            Bitmap.Config.ARGB_8888);
        preview.draw(new Canvas(out));

        String name = "QuoteWallpaper_" +
            System.currentTimeMillis() + ".jpg";

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= 29)
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/Quote Wallpaper");

            Uri uri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream os = getContentResolver().openOutputStream(uri);
            out.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.close();
            toast("Saved to Pictures/Quote Wallpaper");
        } catch (Exception e) {
            toast("Save failed");
        }
    }

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
