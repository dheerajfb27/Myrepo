package com.sunweather.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("https://sun-weather.hatchable.site/?_preview=eyJzbHVnIjoic3VuLXdlYXRoZXIiLCJleHAiOjE3OTEzODgwNjgsInJvbGUiOiJwdWJsaWMifQ.eac1c983d74b451cc0dd3854aeaee5dd4a3208825d8f4041603c2890ca3ce195");
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
