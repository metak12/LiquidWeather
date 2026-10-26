package com.example.liquidweather.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.example.liquidweather.data.cache.WeatherCacheManager
import com.example.liquidweather.widget.LiquidWeatherWidget
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val cache = WeatherCacheManager(this)

        setContent {
            LiquidWeatherTheme {
                var alpha by remember { mutableFloatStateOf(cache.getGlassAlpha()) }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Настройка жидкого стекла", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(24.dp))

                        LiquidGlassCard(
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = alpha + 0.2f)
                        ) {
                            Text("Превью прозрачности: ${(alpha * 100).toInt()}%")
                            Text("Температура: +18°C • Ясно ☀️", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("Плотность оттенка обоев")
                        Slider(
                            value = alpha,
                            onValueChange = { alpha = it },
                            valueRange = 0.05f..0.85f
                        )

                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                cache.saveGlassAlpha(alpha)
                                lifecycleScope.launch {
                                    LiquidWeatherWidget().updateAll(this@WidgetConfigActivity)
                                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                    setResult(Activity.RESULT_OK, resultValue)
                                    finish()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Применить к виджету")
                        }
                    }
                }
            }
        }
    }
}
