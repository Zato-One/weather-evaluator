package cz.savic.weatherevaluator.forecastevaluator

import cz.savic.weatherevaluator.forecastevaluator.accuracy.AccuracyCalculator
import cz.savic.weatherevaluator.forecastevaluator.accuracy.AccuracyProcessor
import cz.savic.weatherevaluator.forecastevaluator.config.AppConfig
import cz.savic.weatherevaluator.forecastevaluator.persistence.DatabaseInitializer
import cz.savic.weatherevaluator.forecastevaluator.persistence.service.AccuracyPersistenceService
import cz.savic.weatherevaluator.forecastevaluator.persistence.service.DataRetrievalService
import cz.savic.weatherevaluator.forecastevaluator.validator.DataValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

class ForecastEvaluatorRunner {
    private val running = AtomicBoolean(false)

    suspend fun runOnce() {
        logger.info { "Initializing forecast-evaluator..." }

        val config = AppConfig.load()
        logger.info { "Configuration loaded successfully" }

        DatabaseInitializer.initialize(config.database)
        logger.info { "Database initialized successfully" }

        val dataValidator = DataValidator(config.database, config.validator)
        logger.info { "Data validator initialized" }

        val sqlSessionFactory = DatabaseInitializer.getSqlSessionFactory()
        val dataRetrievalService = DataRetrievalService(sqlSessionFactory)
        val accuracyPersistenceService = AccuracyPersistenceService(sqlSessionFactory)
        val accuracyCalculator = AccuracyCalculator()
        val accuracyProcessor = AccuracyProcessor(
            dataRetrievalService,
            accuracyPersistenceService,
            accuracyCalculator,
            config.validator.batchSize
        )
        logger.info { "Accuracy processor initialized" }

        try {
            logger.info { "Running single validation cycle..." }
            dataValidator.validateAndUpdateStates()
            logger.info { "Validation cycle completed successfully" }

            logger.info { "Starting accuracy processing..." }
            val hourlyResult = accuracyProcessor.processHourlyAccuracy()
            val dailyResult = accuracyProcessor.processDailyAccuracy()

            logger.info { "Accuracy processing completed - Hourly: $hourlyResult, Daily: $dailyResult" }

        } catch (e: Exception) {
            logger.error(e) { "Error during processing cycle" }
            throw e
        }

        logger.info { "Forecast-evaluator finished" }
    }

    suspend fun start() {
        logger.info { "Starting forecast-evaluator in continuous mode..." }

        val config = AppConfig.load()
        logger.info { "Configuration loaded successfully" }

        DatabaseInitializer.initialize(config.database)
        logger.info { "Database initialized successfully" }

        val dataValidator = DataValidator(config.database, config.validator)
        logger.info { "Data validator initialized" }

        val sqlSessionFactory = DatabaseInitializer.getSqlSessionFactory()
        val dataRetrievalService = DataRetrievalService(sqlSessionFactory)
        val accuracyPersistenceService = AccuracyPersistenceService(sqlSessionFactory)
        val accuracyCalculator = AccuracyCalculator()
        val accuracyProcessor = AccuracyProcessor(
            dataRetrievalService,
            accuracyPersistenceService,
            accuracyCalculator,
            config.validator.batchSize
        )
        logger.info { "Accuracy processor initialized" }

        running.set(true)
        logger.info { "Starting processing loop with interval: ${config.validator.intervalMinutes} minutes" }

        while (running.get()) {
            try {
                logger.debug { "Running validation cycle..." }
                dataValidator.validateAndUpdateStates()
                logger.debug { "Validation cycle completed" }

                logger.debug { "Running accuracy processing..." }
                val hourlyResult = accuracyProcessor.processHourlyAccuracy()
                val dailyResult = accuracyProcessor.processDailyAccuracy()
                logger.debug { "Accuracy processing completed - Hourly: $hourlyResult, Daily: $dailyResult" }

                delay(config.validator.intervalMinutes * 60 * 1000L)
            } catch (e: Exception) {
                logger.error(e) { "Error during processing cycle" }
                delay(30000) // Wait 30 seconds before retry
            }
        }

        logger.info { "Forecast-evaluator stopped" }
    }

    fun stop() {
        logger.info { "Stopping forecast-evaluator..." }
        running.set(false)
    }
}