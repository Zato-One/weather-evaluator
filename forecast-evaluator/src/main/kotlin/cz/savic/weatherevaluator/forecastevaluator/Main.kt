package cz.savic.weatherevaluator.forecastevaluator

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

private val logger = KotlinLogging.logger {}

fun main() = runBlocking {
    logger.info { "Starting forecast-evaluator service..." }

    try {
        val runner = ForecastEvaluatorRunner()
        runner.start()
    } catch (e: Exception) {
        logger.error(e) { "Failed to start forecast-evaluator service" }
        throw e
    }
}