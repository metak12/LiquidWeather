package com.example.liquidweather.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoResponse(
    @SerialName("current") val current: CurrentWeather,
    @SerialName("daily") val daily: DailyWeather
) {
    fun getWeatherAlert(): String? {
        return if (current.weatherCode in 95..99) "Внимание: Гроза и штормовой ветер!" else null
    }
}

@Serializable
data class CurrentWeather(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("surface_pressure") val pressureHpa: Double,
    @SerialName("relative_humidity_2m") val humidity: Int
) {
    val tempInt: Int get() = temperature.toInt()
    val pressureMm: Int get() = (pressureHpa * 0.750062).toInt()
}

@Serializable
data class DailyWeather(
    @SerialName("time") val time: List<String>,
    @SerialName("weather_code") val weatherCodes: List<Int>,
    @SerialName("temperature_2m_max") val tempMax: List<Double>,
    @SerialName("temperature_2m_min") val tempMin: List<Double>,
    @SerialName("precipitation_sum") val precipitationSum: List<Double>
)
