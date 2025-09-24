package cz.savic.weatherevaluator.actualweatherfetcher.adapter

import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.model.Location
import cz.savic.weatherevaluator.actualweatherfetcher.util.mapping.toEvent
import java.time.LocalDateTime

sealed interface ActualWeatherResult {
    val source: String
    val location: Location
    val observedTimeUtc: LocalDateTime

    fun toEvent(): WeatherObservedEvent = toEvent(this)
}

data class CurrentWeatherResult(
    override val source: String,
    override val location: Location,
    override val observedTimeUtc: LocalDateTime,
    val temperatureC: Double,
    val precipitationMm: Double,
    val windSpeedKph10m: Double
) : ActualWeatherResult