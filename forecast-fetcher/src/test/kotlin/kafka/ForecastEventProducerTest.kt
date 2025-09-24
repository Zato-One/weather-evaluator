package cz.savic.weatherevaluator.forecastfetcher.kafka

import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.common.model.ForecastGranularity
import cz.savic.weatherevaluator.common.util.serialization.WeatherJson
import cz.savic.weatherevaluator.forecastfetcher.config.KafkaConfig
import cz.savic.weatherevaluator.forecastfetcher.util.TestDataBuilders
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ForecastEventProducerTest {

    private val mockKafkaProducer = mockk<Producer<String, String>>(relaxed = true)
    private val kafkaConfig = KafkaConfig(
        bootstrapServers = "localhost:9092",
        topic = "test-topic"
    )
    private val producer = ForecastEventProducer(mockKafkaProducer, kafkaConfig)

    @Test
    fun `should send daily and hourly events with correct serialization`() {
        val dailyEvent = TestDataBuilders.createDailyForecastEvent()
        val hourlyEvent = TestDataBuilders.createHourlyForecastEvent()
        val recordSlot = slot<ProducerRecord<String, String>>()

        every { mockKafkaProducer.send(capture(recordSlot)) } returns mockk()

        producer.send(dailyEvent)

        verify { mockKafkaProducer.send(any()) }

        val dailyRecord = recordSlot.captured
        assertEquals(kafkaConfig.topic, dailyRecord.topic())
        assertEquals(dailyEvent.location.name, dailyRecord.key())

        val deserializedDaily = WeatherJson.decodeFromString<DailyForecastFetchedEvent>(dailyRecord.value())
        assertEquals(dailyEvent.source, deserializedDaily.source)
        assertEquals(dailyEvent.location.name, deserializedDaily.location.name)
        assertEquals(ForecastGranularity.DAILY, deserializedDaily.granularity)

        producer.send(hourlyEvent)

        val hourlyRecord = recordSlot.captured
        assertEquals(kafkaConfig.topic, hourlyRecord.topic())
        assertEquals(hourlyEvent.location.name, hourlyRecord.key())

        val deserializedHourly = WeatherJson.decodeFromString<HourlyForecastFetchedEvent>(hourlyRecord.value())
        assertEquals(hourlyEvent.source, deserializedHourly.source)
        assertEquals(hourlyEvent.location.name, deserializedHourly.location.name)
        assertEquals(ForecastGranularity.HOURLY, deserializedHourly.granularity)
    }

    @Test
    fun `should handle kafka send failures gracefully`() {
        val dailyEvent = TestDataBuilders.createDailyForecastEvent()
        val recordSlot = slot<ProducerRecord<String, String>>()

        every { mockKafkaProducer.send(capture(recordSlot)) } throws RuntimeException("Kafka connection failed")

        // Kafka failures shouldn't propagate to prevent service crashes
        producer.send(dailyEvent)

        verify { mockKafkaProducer.send(any()) }

        val record = recordSlot.captured
        assertEquals(kafkaConfig.topic, record.topic())
        assertEquals(dailyEvent.location.name, record.key())

        val deserializedEvent = WeatherJson.decodeFromString<DailyForecastFetchedEvent>(record.value())
        assertEquals(dailyEvent.source, deserializedEvent.source)
    }

    @Test
    fun `should log correct statistics`() {
        val dailyEvent = TestDataBuilders.createDailyForecastEvent()

        every { mockKafkaProducer.send(any()) } returns mockk()

        producer.send(dailyEvent)
        producer.send(dailyEvent)

        producer.logStats()

        producer.send(dailyEvent)

        verify(exactly = 3) { mockKafkaProducer.send(any()) }
    }
}