package cz.savic.weatherevaluator.forecastfetcher.event

import cz.savic.weatherevaluator.forecastfetcher.util.serialization.ForecastJson
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.concurrent.atomic.AtomicInteger

class ForecastEventProducer(
    private val kafkaProducer: Producer<String, String>,
    private val topic: String,
    private val json: Json = ForecastJson
) {
    private val logger = KotlinLogging.logger {}
    private val messageCount = AtomicInteger(0)

    fun send(event: ForecastFetchedEvent) {
        val key = event.location.name

        val message = when (event) {
            is DailyForecastFetchedEvent -> json.encodeToString(event)
            is HourlyForecastFetchedEvent -> json.encodeToString(event)
        }

        val record = ProducerRecord(topic, key, message)

        try {
            kafkaProducer.send(record)
            messageCount.incrementAndGet()

            if (logger.isTraceEnabled()) {
                logger.trace { "Sent forecast event $event" }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to send forecast event for ${event.location.name}" }
        }
    }

    fun logFinalSummary() {
        val count = messageCount.getAndSet(0)
        if (count > 0) {
            logger.info { "Final summary: Sent $count forecast events since last log" }
        }
    }
}