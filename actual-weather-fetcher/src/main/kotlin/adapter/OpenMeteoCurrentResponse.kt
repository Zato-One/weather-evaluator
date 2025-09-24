package cz.savic.weatherevaluator.actualweatherfetcher.adapter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoCurrentResponse(
    val current: CurrentWeatherData
)

@Serializable
data class CurrentWeatherData(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("precipitation") val precipitation: Double,
    @SerialName("wind_speed_10m") val windSpeed: Double
)

@Serializable
data class OpenMeteoCurrentErrorResponse(
    val error: Boolean = false,
    val reason: String
)