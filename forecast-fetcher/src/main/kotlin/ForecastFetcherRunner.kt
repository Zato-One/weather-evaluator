package cz.savic.weatherevaluator.forecastfetcher

import cz.savic.weatherevaluator.forecastfetcher.adapter.ForecastProvider
import cz.savic.weatherevaluator.forecastfetcher.adapter.OpenMeteoAdapter
import cz.savic.weatherevaluator.forecastfetcher.adapter.WeatherApiAdapter
import cz.savic.weatherevaluator.forecastfetcher.config.SecretLoader
import cz.savic.weatherevaluator.forecastfetcher.config.loadConfig
import cz.savic.weatherevaluator.forecastfetcher.kafka.ForecastEventProducer
import cz.savic.weatherevaluator.forecastfetcher.kafka.createKafkaProducer
import cz.savic.weatherevaluator.forecastfetcher.service.ForecastFetcherService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class ForecastFetcherRunner : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private val config = loadConfig()

    init {
        logger.info { "Starting forecast-fetcher..." }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val adapters = createAdapters(client)
    private val kafkaProducer = createKafkaProducer(config.kafka)
    private val eventProducer = ForecastEventProducer(kafkaProducer, config.kafka)

    suspend fun fetchAllOnce() = coroutineScope {
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
        logger.info { "All forecasts fetched" }
    }

    override fun close() {
        logger.info { "Closing forecast-fetcher..." }
        eventProducer.logStats()
        kafkaProducer.flush()
        kafkaProducer.close()
        logger.info { "Shutdown completed" }
    }

    private fun createAdapters(client: HttpClient): List<ForecastProvider> {
        val adapters = mutableListOf<ForecastProvider>()
        adapters.add(createOpenMeteoAdapter(client))
        createWeatherApiAdapter(client)?.let { adapter ->
            adapters.add(adapter)
        }

        logger.info { "Created ${adapters.size} weather providers" }
        return adapters
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
}