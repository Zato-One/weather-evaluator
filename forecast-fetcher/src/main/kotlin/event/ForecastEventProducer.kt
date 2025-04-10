package cz.savic.weatherevaluator.forecastfetcher.event

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord

class ForecastEventProducer(
    private val kafkaProducer: Producer<String, String>,
    private val topic: String,
    private val json: Json = Json
) {
    private val logger = KotlinLogging.logger {}

    fun send(event: ForecastFetchedEvent) {
        val message = json.encodeToString(event)
        val record = ProducerRecord(topic, event.location, message)

        kafkaProducer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.error(exception) { "Failed to send forecast event for ${event.location}" }
            } else {
                logger.info { "Sent forecast event for ${event.location} to ${metadata.topic()} [${metadata.partition()}]" }
            }
        }
    }
}
