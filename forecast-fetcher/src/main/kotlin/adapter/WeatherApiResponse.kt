package cz.savic.weatherevaluator.forecastfetcher.adapter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherApiResponse(
    val forecast: Forecast
)

@Serializable
data class Forecast(
    val forecastday: List<ForecastDay>
)

@Serializable
data class ForecastDay(
    val date: String,
    val day: Day,
    val hour: List<Hour>
)

@Serializable
data class Day(
    @SerialName("maxtemp_c") val maxtempC: Double,
    @SerialName("mintemp_c") val mintempC: Double,
    @SerialName("avgtemp_c") val avgtempC: Double,
    @SerialName("totalprecip_mm") val totalPrecipMm: Double,
    @SerialName("maxwind_kph") val maxWindKph: Double
)

@Serializable
data class Hour(
    val time: String,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("precip_mm") val precipMm: Double,
    @SerialName("wind_kph") val windKph: Double
)