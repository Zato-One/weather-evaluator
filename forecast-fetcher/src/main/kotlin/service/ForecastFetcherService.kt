package cz.savic.weatherevaluator.forecastfetcher.service

import cz.savic.weatherevaluator.forecastfetcher.adapter.ForecastProvider
import cz.savic.weatherevaluator.forecastfetcher.event.ForecastEventProducer
import cz.savic.weatherevaluator.forecastfetcher.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.forecastfetcher.model.Location
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ForecastFetcherService(
    private val provider: ForecastProvider,
    private val producer: ForecastEventProducer
) {
    private val logger = KotlinLogging.logger {}

    suspend fun fetchAll(locations: List<Location>) = coroutineScope {
        val jobs = locations.map { location ->
            async {
                try {
                    val result = provider.fetch(location)

                    val event = ForecastFetchedEvent(
                        source = result.source,
                        location = result.location,
                        temperature = result.temperature,
                        timeUtc = result.timeUtc
                    )

                    producer.send(event)
                    logger.info { "Fetched and sent forecast for ${result.location}" }

                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to fetch or send forecast for ${location.name}" }
                }
            }
        }

        jobs.awaitAll()
    }
}
