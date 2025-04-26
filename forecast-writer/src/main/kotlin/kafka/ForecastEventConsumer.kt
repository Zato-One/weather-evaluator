package cz.savic.weatherevaluator.forecastwriter.kafka

import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.common.model.ForecastGranularity
import cz.savic.weatherevaluator.common.util.serialization.WeatherJson
import cz.savic.weatherevaluator.forecastwriter.config.KafkaConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.apache.kafka.clients.consumer.Consumer
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class ForecastEventConsumer(
    private val kafkaConsumer: Consumer<String, String>,
    private val config: KafkaConfig,
    private val json: Json = WeatherJson
) {
    private val logger = KotlinLogging.logger {}
    private val processedCount = AtomicInteger(0)
    private val processedDaily = AtomicInteger(0)
    private val processedHourly = AtomicInteger(0)

    fun poll(callback: (ForecastFetchedEvent) -> Unit) {
        kafkaConsumer.poll(Duration.ofMillis(config.pollTimeoutMs)).forEach { record ->
            try {
                val event = parseForecastEvent(record.value())

                callback(event)
                processedCount.incrementAndGet()

                when (event.granularity) {
                    ForecastGranularity.DAILY -> processedDaily.incrementAndGet()
                    ForecastGranularity.HOURLY -> processedHourly.incrementAndGet()
                }
            } catch (e: Exception) {
                logger.error(e) { "Error processing Kafka record: ${record.value()}" }
            }
        }
    }

    private fun parseForecastEvent(message: String): ForecastFetchedEvent {
        val jsonElement = json.parseToJsonElement(message)
        val granularityElement = jsonElement.jsonObject["granularity"]
            ?: throw IllegalArgumentException("Missing granularity")
        val granularity = json.decodeFromJsonElement<ForecastGranularity>(granularityElement)

        return when (granularity) {
            ForecastGranularity.DAILY -> json.decodeFromJsonElement<DailyForecastFetchedEvent>(jsonElement)
            ForecastGranularity.HOURLY -> json.decodeFromJsonElement<HourlyForecastFetchedEvent>(jsonElement)
        }
    }

    fun logStats() {
        val count = processedCount.getAndSet(0)
        if (count > 0) {
            logger.info {
                "Processed forecast events - Total: $count, " +
                        "Daily: ${processedDaily.get()}, " +
                        "Hourly: ${processedHourly.get()}, " +
                        "since last log"
            }
        }
        processedDaily.set(0)
        processedHourly.set(0)
    }
}