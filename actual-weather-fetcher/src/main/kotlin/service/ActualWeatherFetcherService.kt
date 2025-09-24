package cz.savic.weatherevaluator.actualweatherfetcher.service

import cz.savic.weatherevaluator.actualweatherfetcher.adapter.ActualWeatherProvider
import cz.savic.weatherevaluator.actualweatherfetcher.adapter.CurrentWeatherResult
import cz.savic.weatherevaluator.actualweatherfetcher.kafka.WeatherObservedEventProducer
import cz.savic.weatherevaluator.common.model.Location
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ActualWeatherFetcherService(
    private val provider: ActualWeatherProvider,
    private val producer: WeatherObservedEventProducer
) {
    private val logger = KotlinLogging.logger {}

    suspend fun fetchAll(locations: List<Location>) = coroutineScope {
        val jobs = locations.map { location ->
            async {
                try {
                    val results = provider.fetchCurrent(location)

                    results.forEach { result ->
                        producer.send(result.toEvent())
                    }

                    val currentResults = results.filterIsInstance<CurrentWeatherResult>()
                    val adapterName = provider.javaClass.simpleName

                    logger.info { "[$adapterName] Processed ${currentResults.size} current weather observations for ${location.name}" }
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to fetch or send current weather for ${location.name} using ${provider.javaClass.simpleName}" }
                }
            }
        }

        jobs.awaitAll()
    }
}