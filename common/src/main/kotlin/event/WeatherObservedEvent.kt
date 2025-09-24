package cz.savic.weatherevaluator.common.event

import cz.savic.weatherevaluator.common.model.Location
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class WeatherObservedEvent(
    val source: String,
    @Contextual val location: Location,
    @Contextual val observedTimeUtc: LocalDateTime,
    val temperatureC: Double,
    val precipitationMm: Double,
    val windSpeedKph10m: Double
)