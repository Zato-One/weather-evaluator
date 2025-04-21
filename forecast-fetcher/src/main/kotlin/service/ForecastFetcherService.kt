package cz.savic.weatherevaluator.forecastfetcher.service

import cz.savic.weatherevaluator.forecastfetcher.adapter.DailyForecastResult
import cz.savic.weatherevaluator.forecastfetcher.adapter.ForecastProvider
import cz.savic.weatherevaluator.forecastfetcher.adapter.HourlyForecastResult
import cz.savic.weatherevaluator.forecastfetcher.event.ForecastEventProducer
import cz.savic.weatherevaluator.forecastfetcher.model.Location
import cz.savic.weatherevaluator.forecastfetcher.util.mapping.toEvent
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
                    val results = provider.fetch(location)
                    
                    results.forEach { result ->
                        val event = when (result) {
                            is DailyForecastResult -> result.toEvent()
                            is HourlyForecastResult -> result.toEvent()
                        }
                        
                        producer.send(event)
                        logger.info { "Sent forecast for ${location.name}" }
                    }
                    
                    logger.info { "Processed ${results.size} forecasts for ${location.name}" }
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to fetch or send forecast for ${location.name}" }
                }
            }
        }

        jobs.awaitAll()
    }
}
