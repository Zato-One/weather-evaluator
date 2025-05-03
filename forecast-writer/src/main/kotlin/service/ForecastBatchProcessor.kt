package cz.savic.weatherevaluator.forecastwriter.service

import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * The batch processor handles events in batches to improve database write performance.
 * It accumulates events until a certain count or time limit is reached, then processes them all at once.
 */
class ForecastBatchProcessor(
    private val processor: (List<ForecastFetchedEvent>) -> Unit,
    private val batchSize: Int = 50,
    private val maxBatchTimeMs: Long = 5000
) {
    private val logger = KotlinLogging.logger {}
    private val queue = ConcurrentLinkedQueue<ForecastFetchedEvent>()
    private val pendingCount = AtomicInteger(0)
    private var lastProcessTime = Instant.now()
    private val lock = Any()
    
    fun submit(event: ForecastFetchedEvent) {
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
        val events = mutableListOf<ForecastFetchedEvent>()
        var e: ForecastFetchedEvent? = queue.poll()
        
        while (e != null) {
            events.add(e)
            e = queue.poll()
        }
        
        if (events.isNotEmpty()) {
            try {
                processor(events)
                lastProcessTime = Instant.now()
                pendingCount.set(0)
            } catch (ex: Exception) {
                logger.error(ex) { "Error processing batch of ${events.size} events" }
            }
        }
    }
}