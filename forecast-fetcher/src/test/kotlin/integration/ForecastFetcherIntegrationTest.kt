package cz.savic.weatherevaluator.forecastfetcher.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import cz.savic.weatherevaluator.common.model.Location
import cz.savic.weatherevaluator.forecastfetcher.adapter.OpenMeteoAdapter
import cz.savic.weatherevaluator.forecastfetcher.config.KafkaConfig
import cz.savic.weatherevaluator.forecastfetcher.kafka.ForecastEventProducer
import cz.savic.weatherevaluator.forecastfetcher.kafka.createKafkaProducer
import cz.savic.weatherevaluator.forecastfetcher.service.ForecastFetcherService
import cz.savic.weatherevaluator.forecastfetcher.util.ApiResponseSamples
import cz.savic.weatherevaluator.forecastfetcher.util.TestDataBuilders
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers
class ForecastFetcherIntegrationTest {

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
            topic = "test-forecast-topic"
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
                        .withBody(ApiResponseSamples.openMeteoSuccessResponse)
                )
        )

        val httpClient = createTestHttpClient()
        val adapter = OpenMeteoAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = ForecastEventProducer(kafkaProducer, kafkaConfig)
        val service = ForecastFetcherService(adapter, eventProducer)

        val location = TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378)
        service.fetchAll(listOf(location))

        kafkaProducer.flush()
        kafkaProducer.close()

        val messages = consumeMessages()
        assertTrue(messages.isNotEmpty())
        assertTrue(messages.any { it.contains("Prague") })
        assertTrue(messages.any { it.contains("open-meteo") })
    }

    @Test
    fun `should handle multiple locations concurrently`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.openMeteoSuccessResponse)
                )
        )

        val httpClient = createTestHttpClient()
        val adapter = OpenMeteoAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = ForecastEventProducer(kafkaProducer, kafkaConfig)
        val service = ForecastFetcherService(adapter, eventProducer)

        val locations = listOf(
            TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378),
            TestDataBuilders.createTestLocation("Brno", 49.1951, 16.6068),
            TestDataBuilders.createTestLocation("Ostrava", 49.8209, 18.2625)
        )

        service.fetchAll(locations)

        kafkaProducer.flush()
        kafkaProducer.close()

        val messages = consumeMessages()
        assertEquals(12, messages.size) // 3 locations × 4 results (2 daily + 2 hourly from response)

        assertTrue(messages.any { it.contains("Prague") })
        assertTrue(messages.any { it.contains("Brno") })
        assertTrue(messages.any { it.contains("Ostrava") })
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
                        .withBody(ApiResponseSamples.openMeteoSuccessResponse)
                )
        )

        val httpClient = createTestHttpClient()
        val adapter = OpenMeteoAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val kafkaProducer = createKafkaProducer(kafkaConfig)
        val eventProducer = ForecastEventProducer(kafkaProducer, kafkaConfig)
        val service = ForecastFetcherService(adapter, eventProducer)

        val locations = listOf(
            TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378),
            TestDataBuilders.createTestLocation("Brno", 49.1951, 16.6068)
        )

        service.fetchAll(locations)

        kafkaProducer.flush()
        kafkaProducer.close()

        val messages = consumeMessages()
        assertEquals(4, messages.size) // Only Brno results (1 location × 4 results)
        assertTrue(messages.all { it.contains("Brno") })
        assertTrue(messages.none { it.contains("Prague") })
    }

    private fun createTestHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    private fun consumeMessages(): List<String> {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        }

        KafkaConsumer<String, String>(props).use { consumer ->
            consumer.subscribe(listOf(kafkaConfig.topic))
            val records = consumer.poll(Duration.ofSeconds(10))
            return records.map { it.value() }
        }
    }
}