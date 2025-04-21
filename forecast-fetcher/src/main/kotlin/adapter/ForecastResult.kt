package cz.savic.weatherevaluator.forecastfetcher.adapter

import cz.savic.weatherevaluator.forecastfetcher.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.forecastfetcher.model.Location
import cz.savic.weatherevaluator.forecastfetcher.util.mapping.toEvent
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface ForecastResult {
    val source: String
    val location: Location
    val forecastTimeUtc: LocalDateTime
    
    fun toEvent(): ForecastFetchedEvent = toEvent(this)
}

data class DailyForecastResult(
    override val source: String,
    override val location: Location,
    override val forecastTimeUtc: LocalDateTime,
    val targetDate: LocalDate,
    val temperatureMinC: Double,
    val temperatureMaxC: Double,
    val temperatureMeanC: Double,
    val precipitationMmSum: Double,
    val windSpeedKph10mMax: Double
) : ForecastResult

data class HourlyForecastResult(
    override val source: String,
    override val location: Location,
    override val forecastTimeUtc: LocalDateTime,
    val targetDateTimeUtc: LocalDateTime,
    val temperatureC: Double,
    val precipitationMm: Double,
    val windSpeedKph10m: Double
) : ForecastResult