package cz.savic.weatherevaluator.forecastevaluator.persistence.entity

import cz.savic.weatherevaluator.common.model.ForecastHorizon
import cz.savic.weatherevaluator.forecastevaluator.accuracy.DailyAccuracyResult
import cz.savic.weatherevaluator.forecastevaluator.accuracy.HourlyAccuracyResult
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.max

private fun calculateToleranceAccuracy(mae: Double, tolerance: Double): Double {
    return if (mae <= tolerance) 100.0 else max(0.0, 100.0 - (mae / tolerance * 50.0))
}

data class HourlyAccuracyEntity(
    val id: Long? = null,
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val forecastTimeUtc: LocalDateTime,
    val targetDatetimeUtc: LocalDateTime,
    val forecastHorizon: String,

    val temperatureMae: Double,
    val temperatureBias: Double,

    val precipitationMae: Double,
    val precipitationBias: Double,

    val windSpeedMae: Double,
    val windSpeedBias: Double,

    val temperatureAccuracyPercent: Double,
    val precipitationAccuracyPercent: Double,
    val windSpeedAccuracyPercent: Double,

    val createdAt: LocalDateTime? = null
) {
    companion object {
        fun fromAccuracyResult(result: HourlyAccuracyResult): HourlyAccuracyEntity {
            return HourlyAccuracyEntity(
                source = result.source,
                locationName = result.locationName,
                latitude = result.latitude,
                longitude = result.longitude,
                forecastTimeUtc = result.forecastTime,
                targetDatetimeUtc = result.targetTime,
                forecastHorizon = result.forecastHorizon.name,

                temperatureMae = result.temperatureMae,
                temperatureBias = result.temperatureBias,

                precipitationMae = result.precipitationMae,
                precipitationBias = result.precipitationBias,

                windSpeedMae = result.windSpeedMae,
                windSpeedBias = result.windSpeedBias,

                temperatureAccuracyPercent = calculateToleranceAccuracy(result.temperatureMae, 2.0),
                precipitationAccuracyPercent = calculateToleranceAccuracy(result.precipitationMae, 1.0),
                windSpeedAccuracyPercent = calculateToleranceAccuracy(result.windSpeedMae, 3.0)
            )
        }
    }
}

data class DailyAccuracyEntity(
    val id: Long? = null,
    val source: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val forecastTimeUtc: LocalDateTime,
    val targetDate: LocalDate,
    val forecastHorizon: String,

    val temperatureMinMae: Double,
    val temperatureMaxMae: Double,
    val temperatureMeanMae: Double,

    val temperatureMinBias: Double,
    val temperatureMaxBias: Double,
    val temperatureMeanBias: Double,

    val precipitationMae: Double,
    val precipitationBias: Double,

    val windSpeedMae: Double,
    val windSpeedBias: Double,

    val temperatureMinAccuracyPercent: Double,
    val temperatureMaxAccuracyPercent: Double,
    val temperatureMeanAccuracyPercent: Double,
    val precipitationAccuracyPercent: Double,
    val windSpeedAccuracyPercent: Double,

    val createdAt: LocalDateTime? = null
) {
    companion object {
        fun fromAccuracyResult(result: DailyAccuracyResult): DailyAccuracyEntity {
            return DailyAccuracyEntity(
                source = result.source,
                locationName = result.locationName,
                latitude = result.latitude,
                longitude = result.longitude,
                forecastTimeUtc = result.forecastTime,
                targetDate = result.targetDate,
                forecastHorizon = result.forecastHorizon.name,

                temperatureMinMae = result.temperatureMinMae,
                temperatureMaxMae = result.temperatureMaxMae,
                temperatureMeanMae = result.temperatureMeanMae,

                temperatureMinBias = result.temperatureMinBias,
                temperatureMaxBias = result.temperatureMaxBias,
                temperatureMeanBias = result.temperatureMeanBias,

                precipitationMae = result.precipitationMae,
                precipitationBias = result.precipitationBias,

                windSpeedMae = result.windSpeedMae,
                windSpeedBias = result.windSpeedBias,

                temperatureMinAccuracyPercent = calculateToleranceAccuracy(result.temperatureMinMae, 2.0),
                temperatureMaxAccuracyPercent = calculateToleranceAccuracy(result.temperatureMaxMae, 2.0),
                temperatureMeanAccuracyPercent = calculateToleranceAccuracy(result.temperatureMeanMae, 2.0),
                precipitationAccuracyPercent = calculateToleranceAccuracy(result.precipitationMae, 2.0),
                windSpeedAccuracyPercent = calculateToleranceAccuracy(result.windSpeedMae, 3.0)
            )
        }
    }
}