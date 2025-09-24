package cz.savic.weatherevaluator.actualweatherfetcher.integration

import cz.savic.weatherevaluator.actualweatherfetcher.config.KafkaConfig
import cz.savic.weatherevaluator.actualweatherfetcher.kafka.WeatherObservedEventProducer
import cz.savic.weatherevaluator.actualweatherfetcher.kafka.createKafkaProducer
import cz.savic.weatherevaluator.actualweatherfetcher.util.TestDataBuilders
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.util.serialization.WeatherJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Testcontainers
class KafkaIntegrationTest {

    @Container
    private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

    @Test
    fun `should publish and consume weather observed events correctly`() = runTest {
        val kafkaConfig = KafkaConfig(
            bootstrapServers = kafkaContainer.bootstrapServers,
            topic = "test-weather-observed-kafka"
        )

        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = WeatherObservedEventProducer(kafkaProducer, kafkaConfig)

        val testEvent = TestDataBuilders.createWeatherObservedEvent(
            source = "test-source",
            temperatureC = 15.5,
            precipitationMm = 2.3,
            windSpeedKph10m = 25.8
        )

        eventProducer.send(testEvent)
        kafkaProducer.flush()
        kafkaProducer.close()

        val consumer = IntegrationTestUtils.createTestKafkaConsumer(kafkaContainer.bootstrapServers, kafkaConfig.topic)
        consumer.use {
            val messages = IntegrationTestUtils.consumeMessages(it)

            assertEquals(1, messages.size)

            val messageContent = messages[0]
            val deserializedEvent = WeatherJson.decodeFromString<WeatherObservedEvent>(messageContent)

            assertEquals(testEvent.source, deserializedEvent.source)
            assertEquals(testEvent.location.name, deserializedEvent.location.name)
            assertEquals(testEvent.location.latitude, deserializedEvent.location.latitude)
            assertEquals(testEvent.location.longitude, deserializedEvent.location.longitude)
            assertEquals(testEvent.temperatureC, deserializedEvent.temperatureC)
            assertEquals(testEvent.precipitationMm, deserializedEvent.precipitationMm)
            assertEquals(testEvent.windSpeedKph10m, deserializedEvent.windSpeedKph10m)
            assertNotNull(deserializedEvent.observedTimeUtc)
        }
    }

    @Test
    fun `should handle multiple events in correct order`() = runTest {
        val kafkaConfig = KafkaConfig(
            bootstrapServers = kafkaContainer.bootstrapServers,
            topic = "test-multiple-events-kafka"
        )

        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = WeatherObservedEventProducer(kafkaProducer, kafkaConfig)

        val events = listOf(
            TestDataBuilders.createWeatherObservedEvent(
                location = TestDataBuilders.createTestLocation("Prague"),
                temperatureC = 10.0
            ),
            TestDataBuilders.createWeatherObservedEvent(
                location = TestDataBuilders.createTestLocation("Brno"),
                temperatureC = 12.0
            ),
            TestDataBuilders.createWeatherObservedEvent(
                location = TestDataBuilders.createTestLocation("Ostrava"),
                temperatureC = 8.0
            )
        )

        events.forEach { event ->
            eventProducer.send(event)
        }

        kafkaProducer.flush()
        kafkaProducer.close()

        val consumer = IntegrationTestUtils.createTestKafkaConsumer(kafkaContainer.bootstrapServers, kafkaConfig.topic)
        consumer.use {
            val messages = IntegrationTestUtils.consumeMessages(it)

            assertEquals(3, messages.size)

            val deserializedEvents = messages.map { messageContent ->
                WeatherJson.decodeFromString<WeatherObservedEvent>(messageContent)
            }

            assertTrue(deserializedEvents.any { it.location.name == "Prague" && it.temperatureC == 10.0 })
            assertTrue(deserializedEvents.any { it.location.name == "Brno" && it.temperatureC == 12.0 })
            assertTrue(deserializedEvents.any { it.location.name == "Ostrava" && it.temperatureC == 8.0 })
        }
    }

    @Test
    fun `should track event statistics correctly`() = runTest {
        val kafkaConfig = KafkaConfig(
            bootstrapServers = kafkaContainer.bootstrapServers,
            topic = "test-stats-kafka"
        )

        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = WeatherObservedEventProducer(kafkaProducer, kafkaConfig)

        val testEvent = TestDataBuilders.createWeatherObservedEvent()

        repeat(5) {
            eventProducer.send(testEvent)
        }

        eventProducer.logStats()

        kafkaProducer.flush()
        kafkaProducer.close()

        val consumer = IntegrationTestUtils.createTestKafkaConsumer(kafkaContainer.bootstrapServers, kafkaConfig.topic)
        consumer.use {
            val messages = IntegrationTestUtils.consumeMessages(it)
            assertEquals(5, messages.size)
        }
    }
}