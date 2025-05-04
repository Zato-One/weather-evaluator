package cz.savic.weatherevaluator.forecastwriter

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.slf4j.bridge.SLF4JBridgeHandler

private val logger = KotlinLogging.logger {}

fun main() = runBlocking {
    setupSlf4jBridgeHandler()

    ForecastWriterRunner().use { runner ->
        setupShutdownHook(runner)
        runner.pollOnce()
    }
}

private fun setupSlf4jBridgeHandler() {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
}

private fun setupShutdownHook(runner: AutoCloseable) {
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutdown hook triggered..." }
        runner.close()
    })
}