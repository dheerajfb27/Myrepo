package ai.docucraft.mobile;

import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import androidx.core.content.FileProvider;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;

public class MainActivity extends AppCompatActivity {
    LinearLayout content; final int BG=Color.rgb(10,12,18),CARD=Color.rgb(25,28,40),WHITE=Color.WHITE,MUTED=Color.rgb(175,180,198),ACCENT=Color.rgb(124,92,255);
    @Override public void onCreate(Bundle b){super.onCreate(b);shell();home();}
    void shell(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        ScrollView sv=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(18),dp(16),dp(18),dp(12));sv.addView(content);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this);nav.setBackgroundColor(CARD);
        nav.addView(nav("Home",v->home()));nav.addView(nav("Resume",v->resume()));nav.addView(nav("PPT",v->ppt()));nav.addView(nav("Settings",v->settings()));
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(62)));setContentView(root);
    }
    Button nav(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextColor(WHITE);b.setAllCaps(false);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(l);b.setLayoutParams(new LinearLayout.LayoutParams(0,-1,1));return b;}
    void clear(String h,String sub){content.removeAllViews();TextView t=label(h,27,WHITE);t.setTypeface(null,1);content.addView(t);content.addView(label(sub,14,MUTED));}
    void home(){clear("DocuCraft AI","Native Android document & presentation studio");card(label("Create smarter documents",21,WHITE),label("No WebView. Native screens, native navigation, secure local key storage and direct AI integration.",14,MUTED),action("Create ATS Resume",v->resume()),action("Create Presentation",v->ppt()));content.addView(label("Native capabilities\n• ATS resume generation\n• Presentation outlines\n• Secure Android Keystore\n• Native PDF export\n• Local result storage",15,MUTED));}
    void resume(){clear("AI Resume Builder","Build an ATS-focused resume from your real experience");EditText profile=field("Profile / existing resume facts",6),jd=field("Target job description",6),skills=field("Skills, certifications, achievements",4);content.addView(profile);content.addView(jd);content.addView(skills);content.addView(action("Generate ATS Resume",v->{String p=profile.getText().toString(),j=jd.getText().toString(),s=skills.getText().toString();if(p.trim().isEmpty()||j.trim().isEmpty()){toast("Add profile and target JD");return;}generate("Create a professional ATS-friendly resume. Use only supplied facts and never invent experience. Return Summary, Skills, Experience, Education and Certifications.\nTARGET JD:\n"+j+"\nPROFILE:\n"+p+"\nSKILLS:\n"+s);}));}
    void ppt(){clear("AI Presentation Builder","Turn an idea into a slide-ready presentation");EditText topic=field("Presentation topic / brief",6),aud=field("Audience",2);content.addView(topic);content.addView(aud);content.addView(action("Generate Presentation Outline",v->{if(topic.getText().toString().trim().isEmpty()){toast("Enter a topic");return;}generate("Create a slide-ready presentation outline for: "+topic.getText()+"\nAudience: "+aud.getText()+". Include title, agenda, slide-by-slide content, speaker notes and suggested visuals.");}));}
    void settings(){clear("Settings","Local configuration");EditText key=field("Groq API key",1);String k=ApiKeyStore.load(this);if(k!=null)key.setText(k);key.setInputType(0x81);content.addView(key);content.addView(action("Save API key securely",v->{try{ApiKeyStore.save(this,key.getText().toString().trim());toast("Saved securely");}catch(Exception e){toast("Could not save key");}}));content.addView(action("Clear API key",v->{ApiKeyStore.clear(this);key.setText("");toast("Cleared");}));}
    void generate(String prompt){String key=ApiKeyStore.load(this);if(key==null||key.trim().isEmpty()){settings();toast("Save your Groq API key first");return;}ProgressBar p=new ProgressBar(this);content.addView(p);GroqApiClient.generate(key,prompt,new GroqApiClient.Callback(){public void onSuccess(String r){runOnUiThread(()->{p.setVisibility(View.GONE);getPreferences(0).edit().putString("last_result",r).apply();result(r);});}public void onError(String e){runOnUiThread(()->{p.setVisibility(View.GONE);toast(e);});}});}
    void result(String r){clear("Generated Result","Native AI output");content.addView(card(label(r,15,WHITE),action("Export PDF",v->pdf(r)),action("Copy",v->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("DocuCraft",r));toast("Copied");}))); }
    void pdf(String body){try{File f=new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"DocuCraft-result.pdf");PdfDocument d=new PdfDocument();int page=1,y=45;PdfDocument.Page pg=d.startPage(new PdfDocument.PageInfo.Builder(612,792,page).create());Paint p=new Paint();p.setColor(Color.BLACK);p.setTextSize(11);for(String line:body.replace("\r","").split("\\n")){if(y>755){d.finishPage(pg);pg=d.startPage(new PdfDocument.PageInfo.Builder(612,792,++page).create());y=45;}pg.getCanvas().drawText(line.length()>95?line.substring(0,95):line,36,y,p);y+=17;}d.finishPage(pg);FileOutputStream o=new FileOutputStream(f);d.writeTo(o);o.close();d.close();Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");Uri u=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",f);i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share PDF"));}catch(Exception e){toast("PDF export failed");}}
    EditText field(String h,int lines){EditText e=new EditText(this);e.setHint(h);e.setHintTextColor(MUTED);e.setTextColor(WHITE);e.setTextSize(15);e.setMinLines(lines);e.setGravity(Gravity.TOP);e.setPadding(dp(12),dp(10),dp(12),dp(10));e.setBackgroundColor(CARD);LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,lines>1?dp(120):dp(58));q.setMargins(0,dp(5),0,dp(8));e.setLayoutParams(q);return e;}
    Button action(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextColor(WHITE);b.setAllCaps(false);b.setBackgroundColor(ACCENT);b.setOnClickListener(l);LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,dp(52));q.setMargins(0,dp(5),0,dp(7));b.setLayoutParams(q);return b;}
    LinearLayout card(View...v){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(15),dp(15),dp(15),dp(15));l.setBackgroundColor(CARD);for(View x:v)l.addView(x);content.addView(l);return l;}
    TextView label(String s,float z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setPadding(0,dp(5),0,dp(5));return t;}
    int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}