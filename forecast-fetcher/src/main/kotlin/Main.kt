package cz.savic.weatherevaluator.forecastfetcher

import cz.savic.weatherevaluator.forecastfetcher.adapter.OpenMeteoAdapter
import cz.savic.weatherevaluator.forecastfetcher.config.loadConfig
import cz.savic.weatherevaluator.forecastfetcher.event.ForecastEventProducer
import cz.savic.weatherevaluator.forecastfetcher.event.createKafkaProducer
import cz.savic.weatherevaluator.forecastfetcher.service.ForecastFetcherService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

suspend fun main() {
    logger.info { "Starting forecast-fetcher..." }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutting down forecast-fetcher..." }
    })

    val config = loadConfig()
    logger.info { "Loaded config: ${config.locations.size} locations, Kafka topic: ${config.kafka.topic}" }

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    val adapter = OpenMeteoAdapter(client)
    val kafkaProducer = createKafkaProducer(config.kafka.bootstrapServers)
    val eventProducer = ForecastEventProducer(kafkaProducer, config.kafka.topic)
    val service = ForecastFetcherService(adapter, eventProducer)

    service.fetchAll(config.locations)

    eventProducer.logFinalSummary()

    logger.info { "Flushing producer..." }
    kafkaProducer.flush()
    
    logger.info { "Closing producer..." }
    kafkaProducer.close()
    
    logger.info { "Application completed successfully" }
}