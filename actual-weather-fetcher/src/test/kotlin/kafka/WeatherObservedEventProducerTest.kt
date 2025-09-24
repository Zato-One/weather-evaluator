package cz.savic.weatherevaluator.actualweatherfetcher.kafka

import cz.savic.weatherevaluator.actualweatherfetcher.config.KafkaConfig
import cz.savic.weatherevaluator.actualweatherfetcher.util.TestDataBuilders
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.util.serialization.WeatherJson
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WeatherObservedEventProducerTest {

    private val mockKafkaProducer = mockk<Producer<String, String>>(relaxed = true)
    private val kafkaConfig = KafkaConfig(
        bootstrapServers = "localhost:9092",
        topic = "test-weather-observed-topic"
    )
    private val producer = WeatherObservedEventProducer(mockKafkaProducer, kafkaConfig)

    @Test
    fun `should send weather observed events with correct serialization`() {
        val weatherEvent = TestDataBuilders.createWeatherObservedEvent()
        val recordSlot = slot<ProducerRecord<String, String>>()

        every { mockKafkaProducer.send(capture(recordSlot)) } returns mockk()

        producer.send(weatherEvent)

        verify { mockKafkaProducer.send(any()) }

        val record = recordSlot.captured
        assertEquals(kafkaConfig.topic, record.topic())
        assertEquals(weatherEvent.location.name, record.key())

        val deserializedEvent = WeatherJson.decodeFromString<WeatherObservedEvent>(record.value())
        assertEquals(weatherEvent.source, deserializedEvent.source)
        assertEquals(weatherEvent.location.name, deserializedEvent.location.name)
        assertEquals(weatherEvent.temperatureC, deserializedEvent.temperatureC)
        assertEquals(weatherEvent.precipitationMm, deserializedEvent.precipitationMm)
        assertEquals(weatherEvent.windSpeedKph10m, deserializedEvent.windSpeedKph10m)
    }

    @Test
    fun `should handle kafka send failures gracefully`() {
        val weatherEvent = TestDataBuilders.createWeatherObservedEvent()
        val recordSlot = slot<ProducerRecord<String, String>>()

        every { mockKafkaProducer.send(capture(recordSlot)) } throws RuntimeException("Kafka connection failed")

        producer.send(weatherEvent)

        verify { mockKafkaProducer.send(any()) }

        val record = recordSlot.captured
        assertEquals(kafkaConfig.topic, record.topic())
        assertEquals(weatherEvent.location.name, record.key())

        val deserializedEvent = WeatherJson.decodeFromString<WeatherObservedEvent>(record.value())
        assertEquals(weatherEvent.source, deserializedEvent.source)
    }

    @Test
    fun `should log correct statistics`() {
        val weatherEvent = TestDataBuilders.createWeatherObservedEvent()

        every { mockKafkaProducer.send(any()) } returns mockk()

        producer.send(weatherEvent)
        producer.send(weatherEvent)

        producer.logStats()

        producer.send(weatherEvent)

        verify(exactly = 3) { mockKafkaProducer.send(any()) }
    }
}