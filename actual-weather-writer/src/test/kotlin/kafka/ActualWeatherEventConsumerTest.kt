package cz.savic.weatherevaluator.actualweatherwriter.kafka

import cz.savic.weatherevaluator.actualweatherwriter.config.KafkaConfig
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.model.Location
import cz.savic.weatherevaluator.common.util.serialization.WeatherJson
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

class ActualWeatherEventConsumerTest {

    @Test
    fun `should parse and process weather observed events`() {
        val mockConsumer = mockk<Consumer<String, String>>(relaxed = true)
        val config = KafkaConfig(topics = listOf("weather.observed"))

        val event = WeatherObservedEvent(
            source = "TestSource",
            location = Location("TestLocation", 50.0, 14.0),
            observedTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
            temperatureC = 20.0,
            precipitationMm = 5.0,
            windSpeedKph10m = 15.0
        )

        val serializedEvent = WeatherJson.encodeToString(WeatherObservedEvent.serializer(), event)
        val record = ConsumerRecord("weather.observed", 0, 0, "key", serializedEvent)
        val records = ConsumerRecords(mapOf(TopicPartition("weather.observed", 0) to listOf(record)))

        every { mockConsumer.assignment() } returns setOf(TopicPartition("weather.observed", 0))
        every { mockConsumer.poll(Duration.ofMillis(100)) } returns records

        val consumer = ActualWeatherEventConsumer(mockConsumer, config)
        val callback = mockk<(WeatherObservedEvent) -> Unit>(relaxed = true)

        consumer.poll(callback)

        verify { callback.invoke(event) }
    }

    @Test
    fun `should handle malformed JSON gracefully`() {
        val mockConsumer = mockk<Consumer<String, String>>(relaxed = true)
        val config = KafkaConfig(topics = listOf("weather.observed"))

        val record = ConsumerRecord("weather.observed", 0, 0, "key", "invalid-json")
        val records = ConsumerRecords(mapOf(TopicPartition("weather.observed", 0) to listOf(record)))

        every { mockConsumer.assignment() } returns setOf(TopicPartition("weather.observed", 0))
        every { mockConsumer.poll(Duration.ofMillis(100)) } returns records

        val consumer = ActualWeatherEventConsumer(mockConsumer, config)
        val callback = mockk<(WeatherObservedEvent) -> Unit>(relaxed = true)

        consumer.poll(callback)

        verify(exactly = 0) { callback.invoke(any()) }
    }
}