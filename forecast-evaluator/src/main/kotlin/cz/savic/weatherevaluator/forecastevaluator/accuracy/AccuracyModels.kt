package cz.savic.weatherevaluator.forecastevaluator.accuracy

import cz.savic.weatherevaluator.common.model.ForecastHorizon
import java.time.LocalDate
import java.time.LocalDateTime

// Input data models for accuracy calculation

data class HourlyForecastData(
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val forecastTime: LocalDateTime,
    val targetTime: LocalDateTime,
    val temperature: Double,
    val precipitation: Double,
    val windSpeed: Double
)

data class HourlyActualData(
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val observedTime: LocalDateTime,
    val temperature: Double,
    val precipitation: Double,
    val windSpeed: Double
)

data class DailyForecastData(
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val forecastTime: LocalDateTime,
    val targetDate: LocalDate,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val temperatureMean: Double,
    val precipitationSum: Double,
    val windSpeedMax: Double
)

// Output accuracy result models

data class HourlyAccuracyResult(
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val forecastTime: LocalDateTime,
    val targetTime: LocalDateTime,
    val forecastHorizon: ForecastHorizon,

    val temperatureMae: Double,
    val temperatureBias: Double,

    val precipitationMae: Double,
    val precipitationBias: Double,

    val windSpeedMae: Double,
    val windSpeedBias: Double
)

data class DailyAccuracyResult(
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val forecastTime: LocalDateTime,
    val targetDate: LocalDate,
    val forecastHorizon: ForecastHorizon,

    val temperatureMinMae: Double,
    val temperatureMaxMae: Double,
    val temperatureMeanMae: Double,

    val temperatureMinBias: Double,
    val temperatureMaxBias: Double,
    val temperatureMeanBias: Double,

    val precipitationMae: Double,
    val precipitationBias: Double,

    val windSpeedMae: Double,
    val windSpeedBias: Double
)