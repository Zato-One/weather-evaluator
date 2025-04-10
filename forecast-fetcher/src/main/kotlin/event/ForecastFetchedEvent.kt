package cz.savic.weatherevaluator.forecastfetcher.event

import kotlinx.serialization.Serializable

@Serializable
data class ForecastFetchedEvent(
    val source: String,
    val location: String,
    val temperature: Double,
    val timeUtc: String
)
