package cz.savic.weatherevaluator.forecastwriter

import cz.savic.weatherevaluator.forecastwriter.config.loadConfig
import cz.savic.weatherevaluator.forecastwriter.kafka.ForecastEventConsumer
import cz.savic.weatherevaluator.forecastwriter.kafka.createKafkaConsumer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope

class ForecastWriterRunner : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private val config = loadConfig()

    init {
        logger.info { "Starting forecast-writer..." }
    }

    // TODO init service and persistence (DB, MyBatis, ...)

    private val kafkaConsumer = createKafkaConsumer(config.kafka)
    private val eventConsumer = ForecastEventConsumer(kafkaConsumer, config.kafka)

    // TODO create method to poll periodically (instead of this one or keep pollOnce too?)
    suspend fun pollOnce() = coroutineScope {
        eventConsumer.poll { event ->
            // TODO remove this log and pass events to the service when it's implemented
            logger.trace { "Received forecast event: $event" }
        }
    }

    override fun close() {
        logger.info { "Closing forecast-writer..." }
        eventConsumer.logStats()
        kafkaConsumer.close()
        logger.info { "Shutdown completed" }
    }
}