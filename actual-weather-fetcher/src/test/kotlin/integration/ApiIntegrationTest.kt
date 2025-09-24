package cz.savic.weatherevaluator.actualweatherfetcher.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import cz.savic.weatherevaluator.actualweatherfetcher.adapter.CurrentWeatherResult
import cz.savic.weatherevaluator.actualweatherfetcher.adapter.OpenMeteoCurrentAdapter
import cz.savic.weatherevaluator.actualweatherfetcher.adapter.WeatherApiCurrentAdapter
import cz.savic.weatherevaluator.actualweatherfetcher.util.ApiResponseSamples
import cz.savic.weatherevaluator.actualweatherfetcher.util.TestDataBuilders
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApiIntegrationTest {

    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
        wireMockServer.start()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `should handle OpenMeteo real current weather API response format`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast"))
                .withQueryParam("latitude", equalTo("50.0755"))
                .withQueryParam("longitude", equalTo("14.4378"))
                .withQueryParam("current", containing("temperature_2m"))
                .withQueryParam("current", containing("precipitation"))
                .withQueryParam("current", containing("wind_speed_10m"))
                .withQueryParam("timezone", equalTo("auto"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.openMeteoCurrentSuccessResponse)
                )
        )

        val httpClient = IntegrationTestUtils.createTestHttpClient()
        val adapter = OpenMeteoCurrentAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val location = TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378)

        val results = adapter.fetchCurrent(location)

        assertEquals(1, results.size)

        val currentResult = results.filterIsInstance<CurrentWeatherResult>()[0]
        assertEquals("open-meteo", currentResult.source)
        assertEquals(location, currentResult.location)
        assertEquals(5.2, currentResult.temperatureC)
        assertEquals(0.1, currentResult.precipitationMm)
        assertEquals(10.2, currentResult.windSpeedKph10m)
        assertNotNull(currentResult.observedTimeUtc)
    }

    @Test
    fun `should handle WeatherAPI real current weather response format`() = runTest {
        val apiKey = "test-api-key-123"

        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/current.json"))
                .withQueryParam("key", equalTo(apiKey))
                .withQueryParam("q", equalTo("50.0755,14.4378"))
                .withQueryParam("aqi", equalTo("no"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.weatherApiCurrentSuccessResponse)
                )
        )

        val httpClient = IntegrationTestUtils.createTestHttpClient()
        val adapter = WeatherApiCurrentAdapter(httpClient, apiKey, "http://localhost:${wireMockServer.port()}")
        val location = TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378)

        val results = adapter.fetchCurrent(location)

        assertEquals(1, results.size)

        val currentResult = results.filterIsInstance<CurrentWeatherResult>()[0]
        assertEquals("weather-api", currentResult.source)
        assertEquals(location, currentResult.location)
        assertEquals(5.2, currentResult.temperatureC)
        assertEquals(0.1, currentResult.precipitationMm)
        assertEquals(10.2, currentResult.windSpeedKph10m)
        assertNotNull(currentResult.observedTimeUtc)
    }

    @Test
    fun `should handle network errors gracefully`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.openMeteoCurrentErrorResponse)
                )
        )

        val httpClient = IntegrationTestUtils.createTestHttpClient()
        val adapter = OpenMeteoCurrentAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val location = TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378)

        val results = adapter.fetchCurrent(location)

        assertEquals(0, results.size)
    }

    @Test
    fun `should handle WeatherAPI authentication errors`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/current.json"))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.weatherApiInvalidKeyErrorResponse)
                )
        )

        val httpClient = IntegrationTestUtils.createTestHttpClient()
        val adapter = WeatherApiCurrentAdapter(httpClient, "invalid-key", "http://localhost:${wireMockServer.port()}")
        val location = TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378)

        val results = adapter.fetchCurrent(location)

        assertEquals(0, results.size)
    }
}