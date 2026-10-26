package com.example.liquidweather.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liquidweather.data.api.OpenMeteoResponse
import com.example.liquidweather.widget.translateWeatherCode

@Composable
fun LiquidWeatherTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
fun WeatherMainScreen(
    weather: OpenMeteoResponse?,
    lastUpdatedTime: String,
    onRefreshClick: () -> Unit
) {
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .systemBarsPadding()
    ) {
        if (weather == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Загрузка погоды от Open-Meteo...")
            }
            return@Box
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LiquidGlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Сейчас • обновлено в $lastUpdatedTime",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${weather.current.tempInt}°C",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = translateWeatherCode(weather.current.weatherCode),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Ветер ${weather.current.windSpeed} м/с • Давление ${weather.current.pressureMm} мм • Влажность ${weather.current.humidity}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AnimatedWeatherIcon(code = weather.current.weatherCode)
                    }
                }
            }

            val alert = weather.getWeatherAlert()
            item {
                AnimatedVisibility(visible = alert != null) {
                    LiquidGlassCard(borderColor = MaterialTheme.colorScheme.error) {
                        Text(
                            text = alert.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Прогноз на 7 дней",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onRefreshClick) {
                        Text("Обновить")
                    }
                }
            }

            items(weather.daily.time.size) { i ->
                WeeklyForecastRow(
                    date = weather.daily.time[i],
                    code = weather.daily.weatherCodes[i],
                    maxTemp = weather.daily.tempMax[i],
                    minTemp = weather.daily.tempMin[i],
                    prec = weather.daily.precipitationSum[i]
                )
            }
        }
    }
}

@Composable
fun LiquidGlassCard(
    borderColor: Color = Color.White.copy(alpha = 0.45f),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                )
            )
            .border(width = 1.2.dp, color = borderColor, shape = shape)
            .padding(18.dp),
        content = content
    )
}

@Composable
fun WeeklyForecastRow(date: String, code: Int, maxTemp: Double, minTemp: Double, prec: Double) {
    LiquidGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = translateWeatherCode(code),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (prec > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "💧 ${prec.toInt()} мм",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            Text(
                text = "${maxTemp.toInt()}° / ${minTemp.toInt()}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnimatedWeatherIcon(code: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "sun_rotation"
    )
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)), label = "rain_drop"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(72.dp)) {
        if (code in 51..82 || code in 95..99) {
            for (i in 0..3) {
                val x = size.width * (0.2f + i * 0.2f)
                val y = (rainOffset + i * 12f) % size.height
                drawLine(
                    color = primaryColor,
                    start = Offset(x, y),
                    end = Offset(x - 4f, y + 14f),
                    strokeWidth = 5f
                )
            }
        } else {
            rotate(rotation) {
                for (angle in 0 until 360 step 45) {
                    rotate(angle.toFloat()) {
                        drawLine(
                            color = Color(0xFFFFB300),
                            start = Offset(size.width / 2, 4f),
                            end = Offset(size.width / 2, 16f),
                            strokeWidth = 6f
                        )
                    }
                }
            }
            drawCircle(color = Color(0xFFFFCA28), radius = size.minDimension * 0.26f)
        }
    }
}
