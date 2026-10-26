package com.example.liquidweather.data.cache

import android.content.Context
import com.example.liquidweather.data.api.OpenMeteoResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherCacheManager(context: Context) {
    private val prefs = context.getSharedPreferences("weather_offline_cache", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun saveWeather(data: OpenMeteoResponse) {
        prefs.edit()
            .putString("cached_json", json.encodeToString(data))
            .putLong("saved_time_ms", System.currentTimeMillis())
            .apply()
    }

    fun getCachedWeather(): Pair<OpenMeteoResponse?, String> {
        val raw = prefs.getString("cached_json", null) ?: return null to "Нет данных"
        val timeMs = prefs.getLong("saved_time_ms", 0L)
        val timeStr = if (timeMs > 0) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMs))
        } else "--:--"

        return try {
            json.decodeFromString<OpenMeteoResponse>(raw) to timeStr
        } catch (e: Exception) {
            null to "--:--"
        }
    }

    fun saveGlassAlpha(alpha: Float) {
        prefs.edit().putFloat("glass_alpha", alpha).apply()
    }

    fun getGlassAlpha(): Float = prefs.getFloat("glass_alpha", 0.28f)
}
