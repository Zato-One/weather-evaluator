package cz.savic.weatherevaluator.forecastevaluator.accuracy

import cz.savic.weatherevaluator.common.model.ForecastHorizon
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private val logger = KotlinLogging.logger {}

/**
 * Calculates accuracy metrics for weather forecasts by comparing predicted vs actual values.
 */
class AccuracyCalculator {

    /**
     * Calculate hourly accuracy metrics for a single forecast vs actual observation.
     */
    fun calculateHourlyAccuracy(
        forecast: HourlyForecastData,
        actual: HourlyActualData
    ): HourlyAccuracyResult {
        val horizon = ForecastHorizon.fromDuration(forecast.forecastTime, forecast.targetTime)

        return HourlyAccuracyResult(
            source = forecast.source,
            locationName = forecast.locationName,
            latitude = forecast.latitude,
            longitude = forecast.longitude,
            forecastTime = forecast.forecastTime,
            targetTime = forecast.targetTime,
            forecastHorizon = horizon,

            temperatureMae = calculateMae(forecast.temperature, actual.temperature),
            temperatureBias = calculateBias(forecast.temperature, actual.temperature),

            precipitationMae = calculateMae(forecast.precipitation, actual.precipitation),
            precipitationBias = calculateBias(forecast.precipitation, actual.precipitation),

            windSpeedMae = calculateMae(forecast.windSpeed, actual.windSpeed),
            windSpeedBias = calculateBias(forecast.windSpeed, actual.windSpeed)
        )
    }

    /**
     * Calculate daily accuracy metrics by comparing daily forecast aggregates vs actual aggregates.
     */
    fun calculateDailyAccuracy(
        forecast: DailyForecastData,
        actualHourlyData: List<HourlyActualData>
    ): DailyAccuracyResult {
        if (actualHourlyData.isEmpty()) {
            throw IllegalArgumentException("Cannot calculate daily accuracy with empty actual data")
        }

        val horizon = ForecastHorizon.fromDuration(forecast.forecastTime, forecast.targetDate.atStartOfDay())

        // Calculate actual daily aggregates from hourly data
        val actualTemperatures = actualHourlyData.map { it.temperature }
        val actualPrecipitations = actualHourlyData.map { it.precipitation }
        val actualWindSpeeds = actualHourlyData.map { it.windSpeed }

        val actualTempMin = actualTemperatures.minOrNull() ?: 0.0
        val actualTempMax = actualTemperatures.maxOrNull() ?: 0.0
        val actualTempMean = actualTemperatures.average()
        val actualPrecipSum = actualPrecipitations.sum()
        val actualWindMax = actualWindSpeeds.maxOrNull() ?: 0.0

        return DailyAccuracyResult(
            source = forecast.source,
            locationName = forecast.locationName,
            latitude = forecast.latitude,
            longitude = forecast.longitude,
            forecastTime = forecast.forecastTime,
            targetDate = forecast.targetDate,
            forecastHorizon = horizon,

            temperatureMinMae = calculateMae(forecast.temperatureMin, actualTempMin),
            temperatureMaxMae = calculateMae(forecast.temperatureMax, actualTempMax),
            temperatureMeanMae = calculateMae(forecast.temperatureMean, actualTempMean),

            temperatureMinBias = calculateBias(forecast.temperatureMin, actualTempMin),
            temperatureMaxBias = calculateBias(forecast.temperatureMax, actualTempMax),
            temperatureMeanBias = calculateBias(forecast.temperatureMean, actualTempMean),

            precipitationMae = calculateMae(forecast.precipitationSum, actualPrecipSum),
            precipitationBias = calculateBias(forecast.precipitationSum, actualPrecipSum),

            windSpeedMae = calculateMae(forecast.windSpeedMax, actualWindMax),
            windSpeedBias = calculateBias(forecast.windSpeedMax, actualWindMax)
        )
    }

    /**
     * Calculate Mean Absolute Error (MAE).
     */
    private fun calculateMae(forecast: Double, actual: Double): Double {
        return abs(forecast - actual)
    }

    /**
     * Calculate bias (forecast - actual). Positive = overestimate, negative = underestimate.
     */
    private fun calculateBias(forecast: Double, actual: Double): Double {
        return forecast - actual
    }

    /**
     * Calculate Root Mean Square Error (RMSE) for multiple values.
     */
    private fun calculateRmse(forecasts: List<Double>, actuals: List<Double>): Double {
        require(forecasts.size == actuals.size) { "Forecast and actual lists must have same size" }

        val squaredErrors = forecasts.zip(actuals) { f, a -> (f - a).pow(2) }
        val meanSquaredError = squaredErrors.average()
        return sqrt(meanSquaredError)
    }
}