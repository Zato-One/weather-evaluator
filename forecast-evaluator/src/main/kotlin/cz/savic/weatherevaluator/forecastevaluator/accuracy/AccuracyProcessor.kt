package cz.savic.weatherevaluator.forecastevaluator.accuracy

import cz.savic.weatherevaluator.forecastevaluator.persistence.service.AccuracyPersistenceService
import cz.savic.weatherevaluator.forecastevaluator.persistence.service.DataRetrievalService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

class AccuracyProcessor(
    private val dataRetrievalService: DataRetrievalService,
    private val accuracyPersistenceService: AccuracyPersistenceService,
    private val accuracyCalculator: AccuracyCalculator,
    private val batchSize: Int = 1000
) {

    fun processHourlyAccuracy(): ProcessingResult {
        logger.info { "Starting hourly accuracy processing with batch size $batchSize" }

        var totalProcessed = 0
        var totalErrors = 0

        while (true) {
            val forecasts = dataRetrievalService.getReadyHourlyForecasts(batchSize)
            if (forecasts.isEmpty()) {
                logger.info { "No more hourly forecasts to process" }
                break
            }

            logger.info { "Processing batch of ${forecasts.size} hourly forecasts" }

            val (processedForecasts, accuracyResults, errors) = processHourlyBatch(forecasts)

            if (accuracyResults.isNotEmpty()) {
                accuracyPersistenceService.persistHourlyAccuracyBatch(accuracyResults)
                logger.info { "Persisted ${accuracyResults.size} hourly accuracy results" }
            }

            if (processedForecasts.isNotEmpty()) {
                dataRetrievalService.markHourlyForecastsBatchProcessed(processedForecasts)
                logger.info { "Marked ${processedForecasts.size} hourly forecasts as processed" }
            }

            totalProcessed += processedForecasts.size
            totalErrors += errors

            if (forecasts.size < batchSize) {
                logger.info { "Reached end of available hourly forecasts" }
                break
            }
        }

        val result = ProcessingResult(
            type = "HOURLY",
            totalProcessed = totalProcessed,
            totalErrors = totalErrors
        )

        logger.info { "Completed hourly accuracy processing: $result" }
        return result
    }

    fun processDailyAccuracy(): ProcessingResult {
        logger.info { "Starting daily accuracy processing with batch size $batchSize" }

        var totalProcessed = 0
        var totalErrors = 0

        while (true) {
            val forecasts = dataRetrievalService.getReadyDailyForecasts(batchSize)
            if (forecasts.isEmpty()) {
                logger.info { "No more daily forecasts to process" }
                break
            }

            logger.info { "Processing batch of ${forecasts.size} daily forecasts" }

            val (processedForecasts, accuracyResults, errors) = processDailyBatch(forecasts)

            if (accuracyResults.isNotEmpty()) {
                accuracyPersistenceService.persistDailyAccuracyBatch(accuracyResults)
                logger.info { "Persisted ${accuracyResults.size} daily accuracy results" }
            }

            if (processedForecasts.isNotEmpty()) {
                dataRetrievalService.markDailyForecastsBatchProcessed(processedForecasts)
                logger.info { "Marked ${processedForecasts.size} daily forecasts as processed" }
            }

            totalProcessed += processedForecasts.size
            totalErrors += errors

            if (forecasts.size < batchSize) {
                logger.info { "Reached end of available daily forecasts" }
                break
            }
        }

        val result = ProcessingResult(
            type = "DAILY",
            totalProcessed = totalProcessed,
            totalErrors = totalErrors
        )

        logger.info { "Completed daily accuracy processing: $result" }
        return result
    }

    private fun processHourlyBatch(forecasts: List<HourlyForecastData>): BatchProcessingResult<HourlyForecastData, HourlyAccuracyResult> {
        val processedForecasts = mutableListOf<HourlyForecastData>()
        val accuracyResults = mutableListOf<HourlyAccuracyResult>()
        val missingForecasts = mutableListOf<HourlyForecastData>()
        var errors = 0

        for (forecast in forecasts) {
            try {
                val actualWeather = dataRetrievalService.getActualWeatherForHour(
                    forecast.locationName,
                    forecast.targetTime
                )

                if (actualWeather != null) {
                    val accuracy = accuracyCalculator.calculateHourlyAccuracy(forecast, actualWeather)
                    accuracyResults.add(accuracy)
                    processedForecasts.add(forecast)
                } else {
                    missingForecasts.add(forecast)
                    processedForecasts.add(forecast)
                }
            } catch (e: Exception) {
                logger.error(e) { "Error processing hourly forecast: ${forecast.locationName} at ${forecast.targetTime}" }
                errors++
                processedForecasts.add(forecast)
            }
        }

        // Log incomplete days (group missing data by day)
        if (missingForecasts.isNotEmpty()) {
            val incompleteDays = missingForecasts
                .groupBy { "${it.locationName} ${it.targetTime.toLocalDate()}" }
                .filter { it.value.size >= 3 } // Only log if 3+ hours missing

            incompleteDays.forEach { (dayLocation, missing) ->
                logger.warn { "Incomplete day: $dayLocation (${missing.size} hours missing)" }
            }
        }

        return BatchProcessingResult(processedForecasts, accuracyResults, errors)
    }

    private fun processDailyBatch(forecasts: List<DailyForecastData>): BatchProcessingResult<DailyForecastData, DailyAccuracyResult> {
        val processedForecasts = mutableListOf<DailyForecastData>()
        val accuracyResults = mutableListOf<DailyAccuracyResult>()
        var errors = 0

        for (forecast in forecasts) {
            try {
                val actualWeatherHours = dataRetrievalService.getActualWeatherForDay(
                    forecast.locationName,
                    forecast.targetDate
                )

                if (actualWeatherHours.isNotEmpty()) {
                    val accuracy = accuracyCalculator.calculateDailyAccuracy(forecast, actualWeatherHours)
                    accuracyResults.add(accuracy)
                    processedForecasts.add(forecast)

                } else {
                    logger.warn {
                        "No actual weather data found for daily forecast: " +
                        "${forecast.locationName} on ${forecast.targetDate}"
                    }
                    processedForecasts.add(forecast)
                }
            } catch (e: Exception) {
                logger.error(e) { "Error processing daily forecast: ${forecast.locationName} on ${forecast.targetDate}" }
                errors++
                processedForecasts.add(forecast)
            }
        }

        return BatchProcessingResult(processedForecasts, accuracyResults, errors)
    }
}

private data class BatchProcessingResult<F, A>(
    val processedForecasts: List<F>,
    val accuracyResults: List<A>,
    val errors: Int
)

data class ProcessingResult(
    val type: String,
    val totalProcessed: Int,
    val totalErrors: Int
) {
    val successRate: Double = if (totalProcessed > 0) {
        (totalProcessed - totalErrors).toDouble() / totalProcessed * 100
    } else {
        0.0
    }

    override fun toString(): String {
        return "ProcessingResult(type=$type, processed=$totalProcessed, errors=$totalErrors, successRate=${"%.2f".format(successRate)}%)"
    }
}