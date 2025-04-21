package cz.savic.weatherevaluator.forecastfetcher.event

import cz.savic.weatherevaluator.forecastfetcher.model.Location
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
sealed interface ForecastFetchedEvent {
    val source: String
    @Contextual val location: Location
    @Contextual val forecastTimeUtc: LocalDateTime
}

@Serializable
data class DailyForecastFetchedEvent(
    override val source: String,
    @Contextual override val location: Location,
    @Contextual override val forecastTimeUtc: LocalDateTime,
    @Contextual val targetDate: LocalDate,
    val temperatureMinC: Double,
    val temperatureMaxC: Double,
    val temperatureMeanC: Double,
    val precipitationMmSum: Double,
    val windSpeedKph10mMax: Double
) : ForecastFetchedEvent

@Serializable
data class HourlyForecastFetchedEvent(
    override val source: String,
    @Contextual override val location: Location,
    @Contextual override val forecastTimeUtc: LocalDateTime,
    @Contextual val targetDateTimeUtc: LocalDateTime,
    val temperatureC: Double,
    val precipitationMm: Double,
    val windSpeedKph10m: Double
) : ForecastFetchedEvent
