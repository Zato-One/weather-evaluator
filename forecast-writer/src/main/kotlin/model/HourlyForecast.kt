package cz.savic.weatherevaluator.forecastwriter.model

import java.time.LocalDateTime

data class HourlyForecast(
    val id: Long? = null,
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val forecastTimeUtc: LocalDateTime,
    val targetDateTimeUtc: LocalDateTime,
    val temperatureC: Double,
    val precipitationMm: Double,
    val windSpeedKph10m: Double
)