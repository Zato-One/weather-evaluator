package cz.savic.weatherevaluator.forecastevaluator

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

private val logger = KotlinLogging.logger {}

fun main() = runBlocking {
    logger.info { "Starting forecast-evaluator service..." }

    try {
        val runner = ForecastEvaluatorRunner()
        setupShutdownHook(runner)
        runner.runOnce()
    } catch (e: Exception) {
        logger.error(e) { "Failed to run forecast-evaluator service" }
        throw e
    }
}

private fun setupShutdownHook(runner: ForecastEvaluatorRunner) {
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutdown hook triggered..." }
        runner.stop()
    })
}