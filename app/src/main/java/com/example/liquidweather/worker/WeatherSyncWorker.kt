package com.example.liquidweather.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.example.liquidweather.data.LocationTracker
import com.example.liquidweather.data.api.WeatherApi
import com.example.liquidweather.data.cache.WeatherCacheManager
import com.example.liquidweather.widget.LiquidWeatherWidget
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class WeatherSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApi::class.java)

        return try {
            val (lat, lon) = LocationTracker(appContext).getCurrentCoordinates()
            val response = api.getForecast(lat = lat, lon = lon)
            WeatherCacheManager(appContext).saveWeather(response)
            LiquidWeatherWidget().updateAll(appContext)
            Result.success()
        } catch (e: Exception) {
            LiquidWeatherWidget().updateAll(appContext)
            Result.retry()
        }
    }

    companion object {
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "open_meteo_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
