package com.sunweather.app
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.glance.action.ActionParameters
class ShortcutActivity:Activity(){
    override fun onCreate(state:Bundle?){
        super.onCreate(state)
        val key=intent.getStringExtra(KEY).orEmpty()
        val pkg=when(key){"phone"->"com.google.android.dialer";"messages"->"com.google.android.apps.messaging";"camera"->"com.google.android.GoogleCamera";"twitter"->"com.twitter.android";"telegram"->"org.telegram.messenger";"spotify"->"com.spotify.music";else->null}
        try{
            val launch=pkg?.let{packageManager.getLaunchIntentForPackage(it)}
            if(launch!=null)startActivity(launch) else{
                val url=when(key){"twitter"->"https://x.com";"telegram"->"https://telegram.org";"spotify"->"https://open.spotify.com";else->"https://www.google.com"}
                startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))
            }
        }catch(_:Exception){}
        finish()
    }
    companion object{const val KEY="shortcut"; val ACTION_KEY=ActionParameters.Key<String>(KEY)}
}
