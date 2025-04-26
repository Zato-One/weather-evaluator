package cz.savic.weatherevaluator.forecastfetcher.adapter

import kotlinx.serialization.Serializable

@Serializable
data class WeatherApiErrorResponse(
    val error: ErrorDetails
)

@Serializable
data class ErrorDetails(
    val code: Int,
    val message: String
)
