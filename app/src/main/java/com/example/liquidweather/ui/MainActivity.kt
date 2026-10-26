package com.example.liquidweather.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.liquidweather.data.cache.WeatherCacheManager
import com.example.liquidweather.worker.WeatherSyncWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cache = WeatherCacheManager(this)
        val workManager = WorkManager.getInstance(this)
        WeatherSyncWorker.schedulePeriodicSync(this)

        setContent {
            LiquidWeatherTheme {
                var weatherState by remember { mutableStateOf(cache.getCachedWeather()) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) triggerImmediateSync(workManager)
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (weatherState.first == null) {
                        triggerImmediateSync(workManager)
                    }
                }

                WeatherMainScreen(
                    weather = weatherState.first,
                    lastUpdatedTime = weatherState.second,
                    onRefreshClick = {
                        val req = OneTimeWorkRequestBuilder<WeatherSyncWorker>().build()
                        workManager.enqueue(req)
                        workManager.getWorkInfoByIdLiveData(req.id).observe(this@MainActivity) { info ->
                            if (info?.state == WorkInfo.State.SUCCEEDED) {
                                weatherState = cache.getCachedWeather()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun triggerImmediateSync(workManager: WorkManager) {
        workManager.enqueue(OneTimeWorkRequestBuilder<WeatherSyncWorker>().build())
    }
}
