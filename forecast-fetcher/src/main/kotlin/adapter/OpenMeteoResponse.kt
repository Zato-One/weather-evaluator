package cz.savic.weatherevaluator.forecastfetcher.adapter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoResponse(
    val daily: DailyData,
    val hourly: HourlyData
)

@Serializable
data class DailyData(
    val time: List<String>,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerialName("temperature_2m_mean") val temperatureMean: List<Double>,
    @SerialName("precipitation_sum") val precipitationSum: List<Double>,
    @SerialName("wind_speed_10m_max") val windSpeedMax: List<Double>
)

@Serializable
data class HourlyData(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double>,
    @SerialName("precipitation") val precipitation: List<Double>,
    @SerialName("wind_speed_10m") val windSpeed: List<Double>
)
