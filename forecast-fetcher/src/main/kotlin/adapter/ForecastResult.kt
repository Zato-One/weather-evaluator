package cz.savic.weatherevaluator.forecastfetcher.adapter

data class ForecastResult(
    val source: String,
    val location: String,
    val temperature: Double,
    val timeUtc: String
)