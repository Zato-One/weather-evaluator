package cz.savic.weatherevaluator.actualweatherwriter.kafka

import cz.savic.weatherevaluator.actualweatherwriter.config.KafkaConfig
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.util.serialization.WeatherJson
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.Consumer
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class ActualWeatherEventConsumer(
    private val kafkaConsumer: Consumer<String, String>,
    private val config: KafkaConfig,
    private val json: Json = WeatherJson
) {
    private val logger = KotlinLogging.logger {}
    private val processedCount = AtomicInteger(0)

    fun poll(callback: (WeatherObservedEvent) -> Unit) {
        waitForAssignment()

        kafkaConsumer.poll(Duration.ofMillis(config.pollTimeoutMs)).forEach { record ->
            try {
                val event = json.decodeFromString<WeatherObservedEvent>(record.value())

                callback(event)
                processedCount.incrementAndGet()
            } catch (e: Exception) {
                logger.error(e) { "Error processing Kafka record: ${record.value()}" }
            }
        }
    }

    fun waitForAssignment() {
        while (kafkaConsumer.assignment().isEmpty()) {
            kafkaConsumer.poll(Duration.ofMillis(100))
        }
    }

    fun logStats() {
        logger.info {
            "Processed weather observed events - Total: ${processedCount.get()}, since last log"
        }
        processedCount.set(0)
    }
}