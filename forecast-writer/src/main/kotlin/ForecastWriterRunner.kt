package cz.savic.weatherevaluator.forecastwriter

import cz.savic.weatherevaluator.forecastwriter.config.loadConfig
import cz.savic.weatherevaluator.forecastwriter.kafka.ForecastEventConsumer
import cz.savic.weatherevaluator.forecastwriter.kafka.createKafkaConsumer
import cz.savic.weatherevaluator.forecastwriter.persistence.DatabaseInitializer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.atomic.AtomicBoolean

class ForecastWriterRunner : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private val config = loadConfig()
    private val dbInitializer = DatabaseInitializer(config.database)
    private val closed = AtomicBoolean(false)

    init {
        logger.info { "Starting forecast-writer..." }

        logger.info { "Running Liquibase migrations..." }
        dbInitializer.initializeDatabase()
    }

    // TODO init service and persistence (DB, MyBatis, ...)

    private val kafkaConsumer = createKafkaConsumer(config.kafka)
    private val eventConsumer = ForecastEventConsumer(kafkaConsumer, config.kafka)

    suspend fun pollOnce() = coroutineScope {
        eventConsumer.poll { event ->
            // TODO remove this log and pass events to the service when it's implemented
            logger.trace { "Received forecast event: $event" }
        }
    }

    // TODO create method to poll periodically (remove pollOnce when it's created or keep it too?)

    override fun close() {
        if (closed.getAndSet(true)) return

        logger.info { "Closing forecast-writer..." }
        eventConsumer.logStats()
        kafkaConsumer.close()
        logger.info { "Shutdown completed" }
    }
}