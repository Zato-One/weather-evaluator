package cz.savic.weatherevaluator.forecastfetcher

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

private val logger = KotlinLogging.logger {}

fun main() = runBlocking {
    ForecastFetcherRunner().use { runner ->
        setupShutdownHook(runner)
        runner.fetchAllOnce()
    }
}

private fun setupShutdownHook(runner: AutoCloseable) {
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutdown hook triggered..." }
        runner.close()
    })
}