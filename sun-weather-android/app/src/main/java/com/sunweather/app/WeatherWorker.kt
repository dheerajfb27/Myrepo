package com.sunweather.app
import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.*
import java.util.concurrent.TimeUnit

class WeatherWorker(appContext:Context,params:WorkerParameters):CoroutineWorker(appContext,params){
    override suspend fun doWork():Result{
        val ok=WeatherRepository.refresh(applicationContext)
        if(ok) SunWeatherWidget().updateAll(applicationContext)
        return if(ok) Result.success() else Result.retry()
    }
    companion object{
        fun enqueueNow(context:Context){
            val req=OneTimeWorkRequestBuilder<WeatherWorker>().setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
            WorkManager.getInstance(context).enqueue(req)
        }
        fun schedule(context:Context){
            val req=PeriodicWorkRequestBuilder<WeatherWorker>(30,TimeUnit.MINUTES).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("weather-refresh",ExistingPeriodicWorkPolicy.UPDATE,req)
        }
    }
}
