package cz.savic.weatherevaluator.forecastwriter

import cz.savic.weatherevaluator.forecastwriter.config.loadConfig
import cz.savic.weatherevaluator.forecastwriter.kafka.ForecastEventConsumer
import cz.savic.weatherevaluator.forecastwriter.kafka.createKafkaConsumer
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting forecast-writer..." }

    val config = loadConfig()

    // TODO init service and persistence (DB, MyBatis, ...)

    val kafkaConsumer = createKafkaConsumer(config.kafka)
    val forecastEventConsumer = ForecastEventConsumer(kafkaConsumer, config.kafka)
    
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutting down forecast-writer..." }
        forecastEventConsumer.logStats()
        kafkaConsumer.close()
        logger.info { "Shutdown completed" }
    })

    // TODO poll periodically
    forecastEventConsumer.poll { event ->
        // TODO remove this log and pass events to the service when it's implemented
        logger.info { "Received forecast event: $event" }
    }

    logger.info { "All forecasts processed, wrapping up..." }
    forecastEventConsumer.logStats()
    kafkaConsumer.close()
    
    logger.info { "Service forecast-writer finished" }
}