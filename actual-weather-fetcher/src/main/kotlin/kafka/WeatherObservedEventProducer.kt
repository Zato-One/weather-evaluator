package cz.savic.weatherevaluator.actualweatherfetcher.kafka

import cz.savic.weatherevaluator.actualweatherfetcher.config.KafkaConfig
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.util.serialization.WeatherJson
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.concurrent.atomic.AtomicInteger

class WeatherObservedEventProducer(
    private val kafkaProducer: Producer<String, String>,
    private val config: KafkaConfig,
    private val json: Json = WeatherJson
) {
    private val logger = KotlinLogging.logger {}
    private val sentCount = AtomicInteger(0)

    fun send(event: WeatherObservedEvent) {
        val key = event.location.name
        val message = json.encodeToString(WeatherObservedEvent.serializer(), event)
        val record = ProducerRecord(config.topic, key, message)

        try {
            kafkaProducer.send(record)
            sentCount.incrementAndGet()

            if (logger.isTraceEnabled()) {
                logger.trace { "Sent weather observed event $event" }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to send weather observed event for ${event.location.name}" }
        }
    }

    fun logStats() {
        val count = sentCount.getAndSet(0)
        if (count > 0) {
            logger.info { "Sent weather observed events - Total: $count, since last log" }
        }
    }
}