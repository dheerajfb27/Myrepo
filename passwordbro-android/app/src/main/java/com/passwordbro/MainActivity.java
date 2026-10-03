package com.passwordbro;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.graphics.Typeface;
import android.net.Uri;

import android.widget.*;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends Activity {
    private static final String PREFS = "passwordbro";
    private static final String PIN_HASH = "pin_hash";
    private static final String PIN_SALT = "pin_salt";
    private static final String VAULT = "vault";
    private static final String KEY_ALIAS = "PasswordBroVaultKey";
    private LinearLayout root, list;
    private SharedPreferences prefs;
    private boolean unlocked = false;
    private int failed = 0;
    private long lockUntil = 0;
    private final ArrayList<Entry> entries = new ArrayList<>();
    private String currentPin = null;
    private static final int REQUEST_CREATE_BACKUP = 7001;
    private static final int REQUEST_RESTORE_BACKUP = 7002;

    static class Entry {
        String site, user, pass, note;
        Entry(String s,String u,String p,String n){site=s;user=u;pass=p;note=n;}
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!prefs.contains(PIN_HASH)) showSetup(); else showLock();
    }

    @Override protected void onPause() {
        super.onPause();
        if (unlocked) { unlocked=false; showLock(); }
    }

    private GradientDrawable bg(int color, float r) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(r); return d;
    }
    private TextView text(String s,float size,int color){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setPadding(18,12,18,12); return t;
    }
    private Button button(String s){
        Button b=new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setTextSize(15); b.setAllCaps(false);
        b.setBackground(bg(Color.rgb(21,151,255),24)); return b;
    }
    private EditText input(String hint){
        EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(Color.rgb(150,170,195)); e.setTextColor(Color.WHITE);
        e.setSingleLine(true); e.setPadding(18,4,18,4); e.setBackground(bg(Color.rgb(16,35,58),18)); return e;
    }
    private void base(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(20,22,20,18);
        root.setBackgroundColor(Color.rgb(7,20,38)); setContentView(root);
    }

    private void showSetup(){
        base();
        Space top=new Space(this); root.addView(top,new LinearLayout.LayoutParams(1,45));
        TextView icon=text("🔐",54,Color.WHITE); icon.setGravity(Gravity.CENTER); root.addView(icon,new LinearLayout.LayoutParams(-1,75));
        TextView title=text("PasswordBro",30,Color.WHITE); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub=text("Create your 6-digit master PIN",16,Color.rgb(156,176,200)); sub.setGravity(Gravity.CENTER); root.addView(sub);
        EditText p=input("6-digit PIN"); p.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); p.setGravity(Gravity.CENTER); p.setTextSize(22);
        root.addView(p, new LinearLayout.LayoutParams(-1,58));
        Space sp=new Space(this); root.addView(sp,new LinearLayout.LayoutParams(1,14));
        EditText c=input("Confirm PIN"); c.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); c.setGravity(Gravity.CENTER); c.setTextSize(22);
        root.addView(c,new LinearLayout.LayoutParams(-1,58));
        TextView info=text("Your vault stays on this device and is encrypted.",14,Color.rgb(156,176,200)); info.setGravity(Gravity.CENTER); root.addView(info);
        Space s2=new Space(this); root.addView(s2,new LinearLayout.LayoutParams(1,15));
        Button save=button("Create Secure Vault"); root.addView(save,new LinearLayout.LayoutParams(-1,54));
        save.setOnClickListener(v->{
            String a=p.getText().toString(), d=c.getText().toString();
            if(a.length()!=6 || !a.matches("\\d{6}")) { toast("PIN must contain exactly 6 digits"); return; }
            if(!a.equals(d)){toast("PINs do not match");return;}
            try{byte[] salt=random(16); prefs.edit().putString(PIN_SALT,b64(salt)).putString(PIN_HASH,b64(derive(a,salt))).apply(); createKey(); currentPin=a; unlocked=true; load(); showVault();}
            catch(Exception e){toast("Could not initialize secure vault");}
        });
    }

    private void showLock(){
        base();
        Space top=new Space(this); root.addView(top,new LinearLayout.LayoutParams(1,55));
        TextView icon=text("🛡️",56,Color.WHITE); icon.setGravity(Gravity.CENTER); root.addView(icon,new LinearLayout.LayoutParams(-1,78));
        TextView title=text("PasswordBro",30,Color.WHITE); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub=text("Enter your 6-digit PIN",16,Color.rgb(156,176,200)); sub.setGravity(Gravity.CENTER); root.addView(sub);
        EditText pin=input("••••••"); pin.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); pin.setGravity(Gravity.CENTER); pin.setTextSize(25);
        root.addView(pin,new LinearLayout.LayoutParams(-1,62));
        Space sp=new Space(this); root.addView(sp,new LinearLayout.LayoutParams(1,15));
        Button unlock=button("Unlock Vault"); root.addView(unlock,new LinearLayout.LayoutParams(-1,54));
        TextView hint=text("6 digits • Local • Encrypted",13,Color.rgb(120,145,170)); hint.setGravity(Gravity.CENTER); root.addView(hint);
        unlock.setOnClickListener(v->{
            if(System.currentTimeMillis()<lockUntil){toast("Too many attempts. Try again shortly.");return;}
            String a=pin.getText().toString();
            if(a.length()!=6){toast("Enter all 6 digits");return;}
            try{
                byte[] salt=Base64.getDecoder().decode(prefs.getString(PIN_SALT,""));
                boolean ok=MessageDigest.isEqual(derive(a,salt),Base64.getDecoder().decode(prefs.getString(PIN_HASH,"")));
                if(ok){failed=0; currentPin=a; unlocked=true; load(); showVault();} else {failed++; if(failed>=5){lockUntil=System.currentTimeMillis()+30000;failed=0;toast("5 failed attempts. Locked for 30 seconds.");} else toast("Incorrect PIN");}
            }catch(Exception e){toast("Vault verification failed");}
        });
        pin.setOnEditorActionListener((v,a,e)->{unlock.performClick();return true;});
    }

    private void showVault(){
        base();
        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=text("Password",27,Color.WHITE); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView bro=text("Bro",27,Color.rgb(53,200,255)); bro.setTypeface(null,1);
        head.addView(title,new LinearLayout.LayoutParams(0,60,1)); head.addView(bro,new LinearLayout.LayoutParams(-2,60));
        root.addView(head);
        LinearLayout bar=new LinearLayout(this); bar.setPadding(0,0,0,8);
        EditText search=input("Search passwords…"); bar.addView(search,new LinearLayout.LayoutParams(0,52,1));
        Button add=button("+"); LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(56,52); ap.setMargins(8,0,0,0); bar.addView(add,ap);
        root.addView(bar);
        list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        ScrollView sv=new ScrollView(this); sv.addView(list); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        add.setOnClickListener(v->showEntryDialog(null,-1));
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){} public void onTextChanged(CharSequence s,int a,int b,int c){render(s.toString());} public void afterTextChanged(android.text.Editable e){}});
        render("");
    }

    private void render(String q){
        list.removeAllViews();
        int shown=0;
        for(int i=0;i<entries.size();i++){
            Entry e=entries.get(i); if(!q.isEmpty()&&!e.site.toLowerCase().contains(q.toLowerCase())&&!e.user.toLowerCase().contains(q.toLowerCase())) continue;
            shown++;
            LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(12,8,12,8); card.setBackground(bg(Color.rgb(16,35,58),20));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,86); cp.setMargins(0,0,0,10);
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
            TextView name=text(e.site,18,Color.WHITE); name.setTypeface(null,1); row.addView(name,new LinearLayout.LayoutParams(0,42,1));
            Button copy=button("Copy"); row.addView(copy,new LinearLayout.LayoutParams(82,44)); card.addView(row);
            TextView user=text(e.user,13,Color.rgb(156,176,200)); card.addView(user);
            final int idx=i;
            copy.setOnClickListener(v->copyPassword(entries.get(idx).pass));
            card.setOnClickListener(v->showEntryDialog(entries.get(idx),idx));
            list.addView(card,cp);
        }
        if(shown==0){TextView empty=text(entries.isEmpty()?"No passwords yet. Tap + to add your first one.":"No matching passwords.",16,Color.rgb(156,176,200)); empty.setGravity(Gravity.CENTER); list.addView(empty,new LinearLayout.LayoutParams(-1,120));}
    }

    private void showEntryDialog(Entry existing,int index){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(12,4,12,4);
        EditText site=input("Website / App"); EditText user=input("Username / Email"); EditText pass=input("Password"); EditText note=input("Notes (optional)");
        pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if(existing!=null){site.setText(existing.site);user.setText(existing.user);pass.setText(existing.pass);note.setText(existing.note);}
        box.addView(site,new LinearLayout.LayoutParams(-1,54));box.addView(user,new LinearLayout.LayoutParams(-1,54));box.addView(pass,new LinearLayout.LayoutParams(-1,54));box.addView(note,new LinearLayout.LayoutParams(-1,54));
        LinearLayout actions=new LinearLayout(this); Button gen=button("Generate"); Button save=button(existing==null?"Save":"Update"); actions.addView(gen,new LinearLayout.LayoutParams(0,52,1)); LinearLayout.LayoutParams sm=new LinearLayout.LayoutParams(0,52,1);sm.setMargins(8,0,0,0);actions.addView(save,sm); box.addView(actions);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle(existing==null?"Add Password":"Edit Password").setView(box).create();
        gen.setOnClickListener(v->pass.setText(generatePassword(16)));
        save.setOnClickListener(v->{String s=site.getText().toString().trim(),u=user.getText().toString().trim(),p=pass.getText().toString(),n=note.getText().toString();if(s.isEmpty()||p.isEmpty()){toast("Website and password are required");return;}if(existing==null)entries.add(new Entry(s,u,p,n));else entries.set(index,new Entry(s,u,p,n));saveVault();dlg.dismiss();render("");});
        dlg.setButton(AlertDialog.BUTTON_NEGATIVE,"Cancel",(d,w)->d.dismiss());
        if(existing!=null) dlg.setButton(AlertDialog.BUTTON_NEUTRAL,"Delete",(d,w)->{entries.remove(index);saveVault();d.dismiss();render("");});
        dlg.show();
    }

    private void chooseBackupLocation(){
        if(currentPin==null){toast("Unlock the vault first");return;}
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/octet-stream");
        i.putExtra(Intent.EXTRA_TITLE,"PasswordBro-Backup.pbpro");
        startActivityForResult(i,REQUEST_CREATE_BACKUP);
    }

    private void chooseBackupFile(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/octet-stream");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,REQUEST_RESTORE_BACKUP);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK || data==null || data.getData()==null)return;
        try{
            if(requestCode==REQUEST_CREATE_BACKUP) exportBackup(data.getData());
            else if(requestCode==REQUEST_RESTORE_BACKUP) importBackup(data.getData());
        }catch(Exception e){toast("Backup operation failed");}
    }

    private void exportBackup(Uri uri) throws Exception{
        byte[] salt=random(16); byte[] keyBytes=derive(currentPin,salt); byte[] iv=random(12);
        Cipher c=Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(keyBytes,"AES"),new GCMParameterSpec(128,iv));
        StringBuilder payload=new StringBuilder("PasswordBro|1\n");
        for(Entry e:entries){
            payload.append(b64(e.site.getBytes(StandardCharsets.UTF_8))).append(".")
                .append(b64(e.user.getBytes(StandardCharsets.UTF_8))).append(".")
                .append(b64(e.pass.getBytes(StandardCharsets.UTF_8))).append(".")
                .append(b64(e.note.getBytes(StandardCharsets.UTF_8))).append("\n");
        }
        byte[] ct=c.doFinal(payload.toString().getBytes(StandardCharsets.UTF_8));
        String file="PBRO1\n"+b64(salt)+"\n"+b64(iv)+"\n"+b64(ct)+"\n";
        try(java.io.OutputStream os=getContentResolver().openOutputStream(uri)){
            os.write(file.getBytes(StandardCharsets.UTF_8));
        }
        toast("Encrypted backup created");
    }

    private void importBackup(Uri uri) throws Exception{
        byte[] bytes;
        try(java.io.InputStream is=getContentResolver().openInputStream(uri)){
            java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
            byte[] buf=new byte[8192]; int n;
            while((n=is.read(buf))>0)out.write(buf,0,n);
            bytes=out.toByteArray();
        }
        String[] parts=new String(bytes,StandardCharsets.UTF_8).split("\n",-1);
        if(parts.length<4 || !"PBRO1".equals(parts[0]))throw new SecurityException("Invalid backup");
        final EditText pin=input("Backup PIN");
        pin.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pin.setGravity(Gravity.CENTER);
        new AlertDialog.Builder(this)
            .setTitle("Restore PasswordBro Backup")
            .setMessage("Enter the 6-digit PIN used when this backup was created.")
            .setView(pin).setNegativeButton("Cancel",null)
            .setPositiveButton("Restore",(d,w)->{
                try{
                    String p=pin.getText().toString();
                    if(p.length()!=6 || !p.matches("\\d{6}"))throw new SecurityException("PIN");
                    byte[] kb=derive(p,Base64.getDecoder().decode(parts[1]));
                    Cipher cc=Cipher.getInstance("AES/GCM/NoPadding");
                    cc.init(Cipher.DECRYPT_MODE,new SecretKeySpec(kb,"AES"),new GCMParameterSpec(128,Base64.getDecoder().decode(parts[2])));
                    String payload=new String(cc.doFinal(Base64.getDecoder().decode(parts[3])),StandardCharsets.UTF_8);
                    String[] lines=payload.split("\n",-1);
                    if(lines.length==0 || !"PasswordBro|1".equals(lines[0]))throw new SecurityException("Version");
                    ArrayList<Entry> restored=new ArrayList<>();
                    for(int i=1;i<lines.length;i++){
                        if(lines[i].isEmpty())continue;
                        String[] x=lines[i].split("\\.",-1);
                        if(x.length!=4)throw new SecurityException("Corrupt backup");
                        restored.add(new Entry(
                            new String(Base64.getDecoder().decode(x[0]),StandardCharsets.UTF_8),
                            new String(Base64.getDecoder().decode(x[1]),StandardCharsets.UTF_8),
                            new String(Base64.getDecoder().decode(x[2]),StandardCharsets.UTF_8),
                            new String(Base64.getDecoder().decode(x[3]),StandardCharsets.UTF_8)));
                    }
                    entries.clear(); entries.addAll(restored); saveVault(); render("");
                    toast("Restored "+entries.size()+" passwords");
                }catch(Exception e){toast("Restore failed: wrong PIN or damaged backup");}
            }).show();
    }

    private void copyPassword(String p){
        ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Password",p));
        toast("Password copied. Clipboard will clear in 30 seconds.");
        new Handler().postDelayed(()->{android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE); if(cm.hasPrimaryClip())cm.setPrimaryClip(ClipData.newPlainText("",""));},30000);
    }

    private String generatePassword(int len){
        String chars="ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*";
        SecureRandom r=new SecureRandom(); StringBuilder s=new StringBuilder();
        for(int i=0;i<len;i++)s.append(chars.charAt(r.nextInt(chars.length()))); return s.toString();
    }

    private void load() throws Exception{
        entries.clear(); String enc=prefs.getString(VAULT,""); if(enc.isEmpty())return;
        String plain=decrypt(Base64.getDecoder().decode(enc));
        if(plain.isEmpty())return;
        for(String line:plain.split("\\n",-1)){if(line.trim().isEmpty())continue;String[] x=line.split("\\|",-1);if(x.length>=4)entries.add(new Entry(unesc(x[0]),unesc(x[1]),unesc(x[2]),unesc(x[3])));}
    }
    private void saveVault(){
        try{StringBuilder s=new StringBuilder();for(Entry e:entries)s.append(esc(e.site)).append("|").append(esc(e.user)).append("|").append(esc(e.pass)).append("|").append(esc(e.note)).append("\\n");prefs.edit().putString(VAULT,Base64.getEncoder().encodeToString(encrypt(s.toString()))).apply();}catch(Exception e){toast("Could not save encrypted vault");}
    }
    private String esc(String s){return s.replace("\\","\\\\").replace("|","\\p").replace("\n","\\n");}
    private String unesc(String s){return s.replace("\\n","\n").replace("\\p","|").replace("\\\\","\\");}

    private void createKey() throws Exception{
        KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
        if(!ks.containsAlias(KEY_ALIAS)){KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");kg.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build());kg.generateKey();}
    }
    private SecretKey key() throws Exception{KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);return ((KeyStore.SecretKeyEntry)ks.getEntry(KEY_ALIAS,null)).getSecretKey();}
    private byte[] encrypt(String s) throws Exception{byte[] iv=random(12);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key(),new GCMParameterSpec(128,iv));byte[] ct=c.doFinal(s.getBytes(StandardCharsets.UTF_8));byte[] out=new byte[iv.length+ct.length];System.arraycopy(iv,0,out,0,iv.length);System.arraycopy(ct,0,out,iv.length,ct.length);return out;}
    private String decrypt(byte[] b) throws Exception{byte[] iv=Arrays.copyOfRange(b,0,12),ct=Arrays.copyOfRange(b,12,b.length);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,iv));return new String(c.doFinal(ct),StandardCharsets.UTF_8);}
    private byte[] derive(String pin,byte[] salt)throws Exception{PBEKeySpec s=new PBEKeySpec(pin.toCharArray(),salt,120000,256);return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(s).getEncoded();}
    private byte[] random(int n){byte[] b=new byte[n];new SecureRandom().nextBytes(b);return b;}
    private String b64(byte[] b){return Base64.getEncoder().encodeToString(b);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
