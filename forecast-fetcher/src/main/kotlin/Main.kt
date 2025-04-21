package cz.savic.weatherevaluator.forecastfetcher

import cz.savic.weatherevaluator.forecastfetcher.adapter.ForecastProvider
import cz.savic.weatherevaluator.forecastfetcher.adapter.OpenMeteoAdapter
import cz.savic.weatherevaluator.forecastfetcher.adapter.WeatherApiAdapter
import cz.savic.weatherevaluator.forecastfetcher.config.SecretLoader
import cz.savic.weatherevaluator.forecastfetcher.config.loadConfig
import cz.savic.weatherevaluator.forecastfetcher.event.ForecastEventProducer
import cz.savic.weatherevaluator.forecastfetcher.event.createKafkaProducer
import cz.savic.weatherevaluator.forecastfetcher.service.ForecastFetcherService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

suspend fun main() = coroutineScope {
    logger.info { "Starting forecast-fetcher..." }

    val config = loadConfig()
    logger.info { "Loaded config: ${config.locations.size} locations, Kafka topic: ${config.kafka.topic}" }

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    val adapters = mutableListOf<ForecastProvider>()
    
    val openMeteoAdapter = createOpenMeteoAdapter(client)
    adapters.add(openMeteoAdapter)
    
    createWeatherApiAdapter(client)?.let { adapter ->
        adapters.add(adapter)
    }
    
    logger.info { "Using ${adapters.size} weather providers" }

    val kafkaProducer = createKafkaProducer(config.kafka.bootstrapServers)
    val eventProducer = ForecastEventProducer(kafkaProducer, config.kafka.topic)
    
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutting down forecast-fetcher..." }
        eventProducer.logFinalSummary()
        kafkaProducer.flush()
        kafkaProducer.close()
        logger.info { "Shutdown completed" }
    })

    val jobs = adapters.map { adapter ->
        async {
            try {
                val service = ForecastFetcherService(adapter, eventProducer)
                service.fetchAll(config.locations)
                logger.info { "Completed fetching from ${adapter.javaClass.simpleName}" }
            } catch (e: Exception) {
                logger.error(e) { "Error running adapter ${adapter.javaClass.simpleName}" }
            }
        }
    }
    
    jobs.awaitAll()
    
    logger.info { "All forecasts fetched, wrapping up..." }
    eventProducer.logFinalSummary()
    
    kafkaProducer.flush()
    kafkaProducer.close()
    
    logger.info { "Application completed successfully" }
}

private fun createOpenMeteoAdapter(client: HttpClient): ForecastProvider {
    logger.info { "Creating OpenMeteo adapter" }
    return OpenMeteoAdapter(client)
}

private fun createWeatherApiAdapter(client: HttpClient): ForecastProvider? {
    val weatherApiKey = SecretLoader.getWeatherApiKey()
    return if (weatherApiKey.isNotEmpty()) {
        logger.info { "Creating WeatherApi adapter" }
        WeatherApiAdapter(client, weatherApiKey)
    } else {
        logger.warn { "WeatherApi adapter not created - missing API key" }
        null
    }
}