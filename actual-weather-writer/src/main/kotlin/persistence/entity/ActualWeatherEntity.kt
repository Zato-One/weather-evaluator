package cz.savic.weatherevaluator.actualweatherwriter.persistence.entity

import java.time.LocalDateTime

data class ActualWeatherEntity(
    val id: Long? = null,
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val observedTimeUtc: LocalDateTime,
    val temperatureC: Double,
    val precipitationMm: Double,
    val windSpeedKph10m: Double,
    val createdAt: LocalDateTime? = null
)