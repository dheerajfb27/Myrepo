package com.sunweather.app;
import android.app.Activity;
import android.os.Bundle;
public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        startActivity(new android.content.Intent(this, SettingsActivity.class));
        finish();
    }
}
