package cz.savic.weatherevaluator.forecastwriter.model

import java.time.LocalDate
import java.time.LocalDateTime

data class DailyForecast(
    val id: Long? = null,
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val forecastTimeUtc: LocalDateTime,
    val targetDate: LocalDate,
    val temperatureMinC: Double,
    val temperatureMaxC: Double,
    val temperatureMeanC: Double,
    val precipitationMmSum: Double,
    val windSpeedKph10mMax: Double
)