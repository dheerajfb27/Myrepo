package ai.docucraft.mobile;

import android.app.DownloadManager;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.view.View;
import android.webkit.*;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {
 private static final String APP_URL="https://docucraft-ai.hatchable.site";
 private static final int FILE_CHOOSER_REQUEST=4101;
 private WebView webView; private SwipeRefreshLayout refreshLayout;
 private ValueCallback<Uri[]> filePathCallback;

 @Override protected void onCreate(Bundle b){
  super.onCreate(b); setContentView(R.layout.activity_main);
  refreshLayout=findViewById(R.id.refresh_layout); webView=findViewById(R.id.web_view);
  configureWebView(); refreshLayout.setOnRefreshListener(()->webView.reload());
  webView.setDownloadListener((url,ua,cd,mime,len)->download(url,ua,cd,mime));
  if(b==null) webView.loadUrl(APP_URL); else webView.restoreState(b);
  getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){
   public void handleOnBackPressed(){if(webView.canGoBack())webView.goBack();else finish();}
  });
 }
 private void configureWebView(){
  WebSettings s=webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
  s.setDatabaseEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true);
  s.setJavaScriptCanOpenWindowsAutomatically(true); s.setMediaPlaybackRequiresUserGesture(false);
  s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  CookieManager.getInstance().setAcceptCookie(true);
  CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
  webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
  webView.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r){
    Uri u=r.getUrl(); String h=u.getHost();
    if(h!=null&&(h.equals("docucraft-ai.hatchable.site")||h.endsWith(".hatchable.site")))return false;
    try{startActivity(new Intent(Intent.ACTION_VIEW,u));return true;}catch(Exception e){return false;}
   }
   @Override public void onPageFinished(WebView v,String u){refreshLayout.setRefreshing(false);}
  });
  webView.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams p){
    if(filePathCallback!=null)filePathCallback.onReceiveValue(null); filePathCallback=cb;
    try{startActivityForResult(p.createIntent(),FILE_CHOOSER_REQUEST);return true;}
    catch(Exception e){filePathCallback=null;return false;}
   }
  });
 }
 private void download(String url,String ua,String cd,String mime){
  try{
   DownloadManager.Request r=new DownloadManager.Request(Uri.parse(url));
   r.setMimeType(mime); r.addRequestHeader("User-Agent",ua);
   String c=CookieManager.getInstance().getCookie(url); if(c!=null)r.addRequestHeader("Cookie",c);
   r.setTitle("DocuCraft AI download"); r.setDescription("Downloading generated file");
   r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
   r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,guessName(cd,mime));
   ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
   Toast.makeText(this,"Download started",Toast.LENGTH_SHORT).show();
  }catch(Exception e){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception ignored){}}
 }
 private String guessName(String cd,String mime){
  if(cd!=null&&cd.contains("filename=")){String n=cd.substring(cd.indexOf("filename=")+9).replace(""","").trim();if(!n.isEmpty())return n;}
  if("application/pdf".equalsIgnoreCase(mime))return "DocuCraft-document.pdf";
  if(mime!=null&&mime.contains("wordprocessingml"))return "DocuCraft-resume.docx";
  if(mime!=null&&mime.contains("presentationml"))return "DocuCraft-presentation.pptx";
  return "DocuCraft-download";
 }
 @Override protected void onActivityResult(int req,int result,@Nullable Intent data){
  super.onActivityResult(req,result,data);
  if(req==FILE_CHOOSER_REQUEST&&filePathCallback!=null){
   Uri[] r=null; if(result==RESULT_OK&&data!=null){
    if(data.getClipData()!=null){int n=data.getClipData().getItemCount();r=new Uri[n];for(int i=0;i<n;i++)r[i]=data.getClipData().getItemAt(i).getUri();}
    else if(data.getData()!=null)r=new Uri[]{data.getData()};
   }
   filePathCallback.onReceiveValue(r);filePathCallback=null;
  }
 }
 @Override protected void onSaveInstanceState(Bundle out){webView.saveState(out);super.onSaveInstanceState(out);}
 @Override protected void onDestroy(){if(webView!=null){webView.stopLoading();webView.destroy();}super.onDestroy();}
}