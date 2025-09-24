package cz.savic.weatherevaluator.actualweatherfetcher

import cz.savic.weatherevaluator.actualweatherfetcher.adapter.ActualWeatherProvider
import cz.savic.weatherevaluator.actualweatherfetcher.adapter.OpenMeteoCurrentAdapter
import cz.savic.weatherevaluator.actualweatherfetcher.adapter.WeatherApiCurrentAdapter
import cz.savic.weatherevaluator.actualweatherfetcher.config.SecretLoader
import cz.savic.weatherevaluator.actualweatherfetcher.config.loadConfig
import cz.savic.weatherevaluator.actualweatherfetcher.kafka.WeatherObservedEventProducer
import cz.savic.weatherevaluator.actualweatherfetcher.kafka.createKafkaProducer
import cz.savic.weatherevaluator.actualweatherfetcher.service.ActualWeatherFetcherService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean

class ActualWeatherFetcherRunner : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private val config = loadConfig()
    private val closed = AtomicBoolean(false)

    init {
        logger.info { "Starting actual-weather-fetcher..." }
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
    private val eventProducer = WeatherObservedEventProducer(kafkaProducer, config.kafka)

    suspend fun fetchAllOnce() = coroutineScope {
        val jobs = adapters.map { adapter ->
            async {
                try {
                    val service = ActualWeatherFetcherService(adapter, eventProducer)
                    service.fetchAll(config.locations)
                    logger.info { "Completed fetching from ${adapter.javaClass.simpleName}" }
                } catch (e: Exception) {
                    logger.error(e) { "Error running adapter ${adapter.javaClass.simpleName}" }
                }
            }
        }
        jobs.awaitAll()
        logger.info { "All current weather observations fetched" }
    }

    override fun close() {
        if (closed.getAndSet(true)) return

        logger.info { "Closing actual-weather-fetcher..." }
        eventProducer.logStats()
        kafkaProducer.flush()
        kafkaProducer.close()
        client.close()
        logger.info { "Shutdown completed" }
    }

    private fun createAdapters(client: HttpClient): List<ActualWeatherProvider> {
        val adapters = mutableListOf<ActualWeatherProvider>()
        adapters.add(createOpenMeteoAdapter(client))
        createWeatherApiAdapter(client)?.let { adapter ->
            adapters.add(adapter)
        }

        logger.info { "Created ${adapters.size} weather providers" }
        return adapters
    }

    private fun createOpenMeteoAdapter(client: HttpClient): ActualWeatherProvider {
        logger.info { "Creating OpenMeteo current weather adapter" }
        return OpenMeteoCurrentAdapter(client)
    }

    private fun createWeatherApiAdapter(client: HttpClient): ActualWeatherProvider? {
        val weatherApiKey = SecretLoader.getWeatherApiKey()
        return if (weatherApiKey.isNotEmpty()) {
            logger.info { "Creating WeatherApi current weather adapter" }
            WeatherApiCurrentAdapter(client, weatherApiKey)
        } else {
            logger.warn { "WeatherApi adapter not created - missing API key" }
            null
        }
    }
}