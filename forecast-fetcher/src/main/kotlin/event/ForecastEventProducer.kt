package cz.savic.weatherevaluator.forecastfetcher.event

import cz.savic.weatherevaluator.forecastfetcher.util.serialization.ForecastJson
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord

class ForecastEventProducer(
    private val kafkaProducer: Producer<String, String>,
    private val topic: String,
    private val json: Json = ForecastJson
) {
    private val logger = KotlinLogging.logger {}

    fun send(event: ForecastFetchedEvent) {
        val key = event.location.name
        
        val message = when (event) {
            is DailyForecastFetchedEvent -> json.encodeToString(event)
            is HourlyForecastFetchedEvent -> json.encodeToString(event)
        }
        
        val record = ProducerRecord(topic, key, message)

        kafkaProducer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.error(exception) { "Failed to send forecast event for ${event.location.name}" }
            } else {
                logger.info { "Sent forecast event for ${event.location.name} to ${metadata.topic()} [${metadata.partition()}]" }
            }
        }
    }
}
