package cz.savic.weatherevaluator.actualweatherwriter.persistence.service

import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class ActualWeatherBatchProcessor(
    private val persistFunc: (List<WeatherObservedEvent>) -> Unit,
    private val batchSize: Int = 100
) {
    private val logger = KotlinLogging.logger {}
    private val events = ConcurrentLinkedQueue<WeatherObservedEvent>()
    private val totalSubmitted = AtomicInteger(0)

    fun submit(event: WeatherObservedEvent) {
        events.offer(event)
        totalSubmitted.incrementAndGet()

        if (events.size >= batchSize) {
            processBatch()
        }
    }

    private fun processBatch() {
        val batch = mutableListOf<WeatherObservedEvent>()
        repeat(batchSize) {
            val event = events.poll()
            if (event != null) {
                batch.add(event)
            }
        }

        if (batch.isNotEmpty()) {
            try {
                persistFunc(batch)
                logger.trace { "Processed batch of ${batch.size} events" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to process batch of ${batch.size} events" }
                throw e
            }
        }
    }
}