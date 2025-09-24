package cz.savic.weatherevaluator.actualweatherfetcher.adapter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherApiCurrentResponse(
    val current: WeatherApiCurrent
)

@Serializable
data class WeatherApiCurrent(
    @SerialName("temp_c") val tempC: Double,
    @SerialName("precip_mm") val precipMm: Double,
    @SerialName("wind_kph") val windKph: Double
)

@Serializable
data class WeatherApiCurrentErrorResponse(
    val error: WeatherApiError
)

@Serializable
data class WeatherApiError(
    val code: Int,
    val message: String
)