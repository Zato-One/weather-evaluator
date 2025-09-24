package cz.savic.weatherevaluator.actualweatherwriter.persistence.service

import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class ActualWeatherBatchProcessor(
    private val persistFunc: (List<WeatherObservedEvent>) -> Unit,
    private val batchSize: Int = 50,
    private val maxBatchTimeMs: Long = 5000
) {
    private val logger = KotlinLogging.logger {}
    private val queue = ConcurrentLinkedQueue<WeatherObservedEvent>()
    private val pendingCount = AtomicInteger(0)
    private var lastProcessTime = Instant.now()
    private val lock = Any()

    fun submit(event: WeatherObservedEvent) {
        queue.add(event)

        if (pendingCount.incrementAndGet() >= batchSize ||
            Duration.between(lastProcessTime, Instant.now()).toMillis() >= maxBatchTimeMs) {
            processBatch()
        }
    }

    fun forceBatchProcessing() {
        if (pendingCount.get() > 0) {
            processBatch()
        }
    }

    private fun processBatch() = synchronized(lock) {
        val events = mutableListOf<WeatherObservedEvent>()
        var e: WeatherObservedEvent? = queue.poll()

        while (e != null) {
            events.add(e)
            e = queue.poll()
        }

        if (events.isNotEmpty()) {
            try {
                persistFunc(events)
                lastProcessTime = Instant.now()
                pendingCount.set(0)
                logger.trace { "Processed batch of ${events.size} events" }
            } catch (ex: Exception) {
                logger.error(ex) { "Error processing batch of ${events.size} events" }
            }
        }
    }
}