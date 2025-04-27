package cz.savic.weatherevaluator.forecastwriter

import cz.savic.weatherevaluator.forecastwriter.config.loadConfig
import cz.savic.weatherevaluator.forecastwriter.kafka.ForecastEventConsumer
import cz.savic.weatherevaluator.forecastwriter.kafka.createKafkaConsumer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.use

private val logger = KotlinLogging.logger {}

fun main() = runBlocking {
    ForecastWriterRunner().use { runner ->
        setupShutdownHook(runner)
        runner.pollOnce()
    }
}

private fun setupShutdownHook(runner: AutoCloseable) {
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutdown hook triggered..." }
        runner.close()
    })
}