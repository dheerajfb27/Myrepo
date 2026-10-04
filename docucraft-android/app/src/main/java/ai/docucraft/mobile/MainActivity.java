package ai.docucraft.mobile;

import android.app.DownloadManager;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.text.InputType;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {
    private static final String APP_URL = "https://docucraft-ai.hatchable.site";
    private static final int FILE_CHOOSER_REQUEST = 4101;

    private WebView webView;
    private SwipeRefreshLayout refreshLayout;
    private ValueCallback<Uri[]> filePathCallback;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        refreshLayout = findViewById(R.id.refresh_layout);
        webView = findViewById(R.id.web_view);
        Button aiButton = findViewById(R.id.ai_button);

        configureWebView();
        refreshLayout.setOnRefreshListener(() -> webView.reload());
        webView.setDownloadListener((url, ua, cd, mime, len) -> download(url, ua, cd, mime));

        if (b == null) webView.loadUrl(APP_URL);
        else webView.restoreState(b);

        aiButton.setOnClickListener(v -> showAiDialog());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            public void handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack(); else finish();
            }
        });
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String h = u.getHost();
                if (h != null && (h.equals("docucraft-ai.hatchable.site") || h.endsWith(".hatchable.site")))
                    return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); return true; }
                catch (Exception e) { return false; }
            }
            @Override public void onPageFinished(WebView v, String u) {
                refreshLayout.setRefreshing(false);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = cb;
                try {
                    startActivityForResult(p.createIntent(), FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
            }
        });
    }

    private void showAiDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        box.setPadding(pad, dp(8), pad, 0);

        EditText key = new EditText(this);
        key.setHint("Groq API key");
        key.setSingleLine(true);
        key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        String saved = ApiKeyStore.load(this);
        if (saved != null) key.setText(saved);

        EditText prompt = new EditText(this);
        prompt.setHint("Ask DocuCraft AI to create or improve something...");
        prompt.setGravity(android.view.Gravity.TOP);
        prompt.setMinLines(5);
        prompt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        prompt.setText("Create an ATS-friendly professional summary for a Salesforce QA Lead with 11+ years of experience.");

        box.addView(key, new LinearLayout.LayoutParams(-1, dp(58)));
        box.addView(prompt, new LinearLayout.LayoutParams(-1, dp(150)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("DocuCraft AI • Groq")
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String apiKey = key.getText().toString().trim();
            String userPrompt = prompt.getText().toString().trim();

            if (apiKey.isEmpty()) {
                key.setError("Enter your Groq API key");
                return;
            }
            if (userPrompt.isEmpty()) {
                prompt.setError("Enter a prompt");
                return;
            }

            try {
                ApiKeyStore.save(this, apiKey);
            } catch (Exception e) {
                Toast.makeText(this, "Could not securely save the API key", Toast.LENGTH_LONG).show();
                return;
            }

            dialog.dismiss();
            showGenerating();
            GroqApiClient.generate(apiKey, userPrompt, new GroqApiClient.Callback() {
                @Override public void onSuccess(String text) {
                    runOnUiThread(() -> showResult(text));
                }
                @Override public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }));

        dialog.show();
    }

    private void showGenerating() {
        Toast.makeText(this, "DocuCraft AI is generating…", Toast.LENGTH_SHORT).show();
    }

    private void showResult(String text) {
        ScrollView scroll = new ScrollView(this);
        TextView result = new TextView(this);
        result.setText(text);
        result.setTextIsSelectable(true);
        result.setTextSize(16);
        result.setPadding(dp(20), dp(12), dp(20), dp(20));
        scroll.addView(result);

        new AlertDialog.Builder(this)
                .setTitle("DocuCraft AI Result")
                .setView(scroll)
                .setPositiveButton("Done", null)
                .setNeutralButton("Copy", (d, w) -> {
                    ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                            .setPrimaryClip(android.content.ClipData.newPlainText("DocuCraft AI", text));
                    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void download(String url, String ua, String cd, String mime) {
        try {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setMimeType(mime);
            r.addRequestHeader("User-Agent", ua);
            String c = CookieManager.getInstance().getCookie(url);
            if (c != null) r.addRequestHeader("Cookie", c);
            r.setTitle("DocuCraft AI download");
            r.setDescription("Downloading generated file");
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, guessName(cd, mime));
            ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
            catch (Exception ignored) {}
        }
    }

    private String guessName(String cd, String mime) {
        if (cd != null && cd.contains("filename=")) {
            String n = cd.substring(cd.indexOf("filename=") + 9).replace("\"", "").trim();
            if (!n.isEmpty()) return n;
        }
        if ("application/pdf".equalsIgnoreCase(mime)) return "DocuCraft-document.pdf";
        if (mime != null && mime.contains("wordprocessingml")) return "DocuCraft-resume.docx";
        if (mime != null && mime.contains("presentationml")) return "DocuCraft-presentation.pptx";
        return "DocuCraft-download";
    }

    @Override protected void onActivityResult(int req, int result, @Nullable Intent data) {
        super.onActivityResult(req, result, data);
        if (req == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] r = null;
            if (result == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int n = data.getClipData().getItemCount();
                    r = new Uri[n];
                    for (int i = 0; i < n; i++) r[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    r = new Uri[]{data.getData()};
                }
            }
            filePathCallback.onReceiveValue(r);
            filePathCallback = null;
        }
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        webView.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
