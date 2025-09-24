package cz.savic.weatherevaluator.actualweatherfetcher.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import cz.savic.weatherevaluator.actualweatherfetcher.adapter.OpenMeteoCurrentAdapter
import cz.savic.weatherevaluator.actualweatherfetcher.config.KafkaConfig
import cz.savic.weatherevaluator.actualweatherfetcher.kafka.WeatherObservedEventProducer
import cz.savic.weatherevaluator.actualweatherfetcher.kafka.createKafkaProducer
import cz.savic.weatherevaluator.actualweatherfetcher.service.ActualWeatherFetcherService
import cz.savic.weatherevaluator.actualweatherfetcher.util.ApiResponseSamples
import cz.savic.weatherevaluator.actualweatherfetcher.util.TestDataBuilders
import cz.savic.weatherevaluator.common.model.Location
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers
class ActualWeatherFetcherIntegrationTest {

    @Container
    private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

    private lateinit var wireMockServer: WireMockServer
    private lateinit var kafkaConfig: KafkaConfig

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
        wireMockServer.start()

        kafkaConfig = KafkaConfig(
            bootstrapServers = kafkaContainer.bootstrapServers,
            topic = "test-weather-observed-topic"
        )
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `should fetch from OpenMeteo and publish to embedded Kafka`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.openMeteoCurrentSuccessResponse)
                )
        )

        val httpClient = IntegrationTestUtils.createTestHttpClient()
        val adapter = OpenMeteoCurrentAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = WeatherObservedEventProducer(kafkaProducer, kafkaConfig)
        val service = ActualWeatherFetcherService(adapter, eventProducer)

        val location = TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378)
        service.fetchAll(listOf(location))

        kafkaProducer.flush()
        kafkaProducer.close()

        val consumer = IntegrationTestUtils.createTestKafkaConsumer(kafkaContainer.bootstrapServers, kafkaConfig.topic)
        consumer.use {
            val messages = IntegrationTestUtils.consumeMessages(it)
            assertTrue(messages.isNotEmpty())
            assertTrue(messages.any { msg -> msg.contains("Prague") })
            assertTrue(messages.any { msg -> msg.contains("open-meteo") })
        }
    }

    @Test
    fun `should handle multiple locations concurrently`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.openMeteoCurrentSuccessResponse)
                )
        )

        val httpClient = IntegrationTestUtils.createTestHttpClient()
        val adapter = OpenMeteoCurrentAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = WeatherObservedEventProducer(kafkaProducer, kafkaConfig)
        val service = ActualWeatherFetcherService(adapter, eventProducer)

        val locations = listOf(
            TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378),
            TestDataBuilders.createTestLocation("Brno", 49.1951, 16.6068),
            TestDataBuilders.createTestLocation("Ostrava", 49.8209, 18.2625)
        )

        service.fetchAll(locations)

        kafkaProducer.flush()
        kafkaProducer.close()

        val consumer = IntegrationTestUtils.createTestKafkaConsumer(kafkaContainer.bootstrapServers, kafkaConfig.topic)
        consumer.use {
            val messages = IntegrationTestUtils.consumeMessages(it)
            assertEquals(3, messages.size) // 3 locations × 1 current weather result each

            assertTrue(messages.any { msg -> msg.contains("Prague") })
            assertTrue(messages.any { msg -> msg.contains("Brno") })
            assertTrue(messages.any { msg -> msg.contains("Ostrava") })
        }
    }

    @Test
    fun `should continue processing when one adapter fails`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast"))
                .withQueryParam("latitude", equalTo("50.0755"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": true, \"reason\": \"Server error\"}")
                )
        )

        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast"))
                .withQueryParam("latitude", equalTo("49.1951"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.openMeteoCurrentSuccessResponse)
                )
        )

        val httpClient = IntegrationTestUtils.createTestHttpClient()
        val adapter = OpenMeteoCurrentAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = WeatherObservedEventProducer(kafkaProducer, kafkaConfig)
        val service = ActualWeatherFetcherService(adapter, eventProducer)

        val locations = listOf(
            TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378),
            TestDataBuilders.createTestLocation("Brno", 49.1951, 16.6068)
        )

        service.fetchAll(locations)

        kafkaProducer.flush()
        kafkaProducer.close()

        val consumer = IntegrationTestUtils.createTestKafkaConsumer(kafkaContainer.bootstrapServers, kafkaConfig.topic)
        consumer.use {
            val messages = IntegrationTestUtils.consumeMessages(it)
            assertEquals(1, messages.size) // Only Brno results (Prague failed)
            assertTrue(messages.all { msg -> msg.contains("Brno") })
            assertTrue(messages.none { msg -> msg.contains("Prague") })
        }
    }
}