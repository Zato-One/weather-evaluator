package cz.savic.weatherevaluator.forecastfetcher.kafka

import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.common.util.serialization.WeatherJson
import cz.savic.weatherevaluator.forecastfetcher.config.KafkaConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.concurrent.atomic.AtomicInteger

class ForecastEventProducer(
    private val kafkaProducer: Producer<String, String>,
    private val config: KafkaConfig,
    private val json: Json = WeatherJson
) {
    private val logger = KotlinLogging.logger {}
    private val sentCount = AtomicInteger(0)

    fun send(event: ForecastFetchedEvent) {
        val key = event.location.name

        val message = when (event) {
            is DailyForecastFetchedEvent -> json.encodeToString(event)
            is HourlyForecastFetchedEvent -> json.encodeToString(event)
        }

        val record = ProducerRecord(config.topic, key, message)

        try {
            kafkaProducer.send(record)
            sentCount.incrementAndGet()

            if (logger.isTraceEnabled()) {
                logger.trace { "Sent forecast event $event" }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to send forecast event for ${event.location.name}" }
        }
    }

    fun logStats() {
        val count = sentCount.getAndSet(0)
        if (count > 0) {
            logger.info { "Sent forecast events - Total: $count, since last log" }
        }
    }
}