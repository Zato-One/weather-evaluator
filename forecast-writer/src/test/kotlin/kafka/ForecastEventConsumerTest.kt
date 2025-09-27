package cz.savic.weatherevaluator.forecastwriter.kafka

import cz.savic.weatherevaluator.forecastwriter.config.KafkaConfig
import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
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
import java.time.LocalDate
import java.time.LocalDateTime

class ForecastEventConsumerTest {

    @Test
    fun `should parse and process daily forecast events`() {
        val mockConsumer = mockk<Consumer<String, String>>(relaxed = true)
        val config = KafkaConfig(topics = listOf("forecast.fetched"))

        val event = DailyForecastFetchedEvent(
            source = "TestSource",
            location = Location("TestLocation", 50.0, 14.0),
            forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
            targetDate = LocalDate.of(2025, 1, 2),
            temperatureMinC = 10.0,
            temperatureMaxC = 20.0,
            temperatureMeanC = 15.0,
            precipitationMmSum = 5.0,
            windSpeedKph10mMax = 25.0
        )

        val serializedEvent = WeatherJson.encodeToString(DailyForecastFetchedEvent.serializer(), event)
        val record = ConsumerRecord("forecast.fetched", 0, 0, "key", serializedEvent)
        val records = ConsumerRecords(mapOf(TopicPartition("forecast.fetched", 0) to listOf(record)))

        every { mockConsumer.assignment() } returns setOf(TopicPartition("forecast.fetched", 0))
        every { mockConsumer.poll(Duration.ofMillis(100)) } returns records

        val consumer = ForecastEventConsumer(mockConsumer, config)
        val callback = mockk<(ForecastFetchedEvent) -> Unit>(relaxed = true)

        consumer.poll(callback)

        verify { callback.invoke(event) }
    }

    @Test
    fun `should parse and process hourly forecast events`() {
        val mockConsumer = mockk<Consumer<String, String>>(relaxed = true)
        val config = KafkaConfig(topics = listOf("forecast.fetched"))

        val event = HourlyForecastFetchedEvent(
            source = "TestSource",
            location = Location("TestLocation", 50.0, 14.0),
            forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
            targetDateTimeUtc = LocalDateTime.of(2025, 1, 2, 15, 0),
            temperatureC = 15.0,
            precipitationMm = 2.0,
            windSpeedKph10m = 20.0
        )

        val serializedEvent = WeatherJson.encodeToString(HourlyForecastFetchedEvent.serializer(), event)
        val record = ConsumerRecord("forecast.fetched", 0, 0, "key", serializedEvent)
        val records = ConsumerRecords(mapOf(TopicPartition("forecast.fetched", 0) to listOf(record)))

        every { mockConsumer.assignment() } returns setOf(TopicPartition("forecast.fetched", 0))
        every { mockConsumer.poll(Duration.ofMillis(100)) } returns records

        val consumer = ForecastEventConsumer(mockConsumer, config)
        val callback = mockk<(ForecastFetchedEvent) -> Unit>(relaxed = true)

        consumer.poll(callback)

        verify { callback.invoke(event) }
    }

    @Test
    fun `should handle malformed JSON gracefully`() {
        val mockConsumer = mockk<Consumer<String, String>>(relaxed = true)
        val config = KafkaConfig(topics = listOf("forecast.fetched"))

        val record = ConsumerRecord("forecast.fetched", 0, 0, "key", "invalid-json")
        val records = ConsumerRecords(mapOf(TopicPartition("forecast.fetched", 0) to listOf(record)))

        every { mockConsumer.assignment() } returns setOf(TopicPartition("forecast.fetched", 0))
        every { mockConsumer.poll(Duration.ofMillis(100)) } returns records

        val consumer = ForecastEventConsumer(mockConsumer, config)
        val callback = mockk<(ForecastFetchedEvent) -> Unit>(relaxed = true)

        consumer.poll(callback)

        verify(exactly = 0) { callback.invoke(any()) }
    }

    @Test
    fun `should handle missing granularity gracefully`() {
        val mockConsumer = mockk<Consumer<String, String>>(relaxed = true)
        val config = KafkaConfig(topics = listOf("forecast.fetched"))

        val invalidJson = """{"source": "test", "location": {"name": "test", "latitude": 50.0, "longitude": 14.0}}"""
        val record = ConsumerRecord("forecast.fetched", 0, 0, "key", invalidJson)
        val records = ConsumerRecords(mapOf(TopicPartition("forecast.fetched", 0) to listOf(record)))

        every { mockConsumer.assignment() } returns setOf(TopicPartition("forecast.fetched", 0))
        every { mockConsumer.poll(Duration.ofMillis(100)) } returns records

        val consumer = ForecastEventConsumer(mockConsumer, config)
        val callback = mockk<(ForecastFetchedEvent) -> Unit>(relaxed = true)

        consumer.poll(callback)

        verify(exactly = 0) { callback.invoke(any()) }
    }

    @Test
    fun `should handle empty poll results`() {
        val mockConsumer = mockk<Consumer<String, String>>(relaxed = true)
        val config = KafkaConfig(topics = listOf("forecast.fetched"))

        val emptyRecords = ConsumerRecords<String, String>(emptyMap())

        every { mockConsumer.assignment() } returns setOf(TopicPartition("forecast.fetched", 0))
        every { mockConsumer.poll(Duration.ofMillis(100)) } returns emptyRecords

        val consumer = ForecastEventConsumer(mockConsumer, config)
        val callback = mockk<(ForecastFetchedEvent) -> Unit>(relaxed = true)

        consumer.poll(callback)

        verify(exactly = 0) { callback.invoke(any()) }
    }
}