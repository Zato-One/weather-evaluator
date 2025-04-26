package cz.savic.weatherevaluator.forecastfetcher.adapter

import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoErrorResponse(
    val error: Boolean,
    val reason: String
)
