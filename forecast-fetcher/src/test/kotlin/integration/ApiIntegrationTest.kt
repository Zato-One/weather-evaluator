package cz.savic.weatherevaluator.forecastfetcher.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import cz.savic.weatherevaluator.forecastfetcher.adapter.DailyForecastResult
import cz.savic.weatherevaluator.forecastfetcher.adapter.HourlyForecastResult
import cz.savic.weatherevaluator.forecastfetcher.adapter.OpenMeteoAdapter
import cz.savic.weatherevaluator.forecastfetcher.adapter.WeatherApiAdapter
import cz.savic.weatherevaluator.forecastfetcher.util.ApiResponseSamples
import cz.savic.weatherevaluator.forecastfetcher.util.TestDataBuilders
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
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
    fun `should handle OpenMeteo real API response format`() = runTest {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast"))
                .withQueryParam("latitude", equalTo("50.0755"))
                .withQueryParam("longitude", equalTo("14.4378"))
                .withQueryParam("daily", containing("temperature_2m_min"))
                .withQueryParam("hourly", containing("temperature_2m"))
                .withQueryParam("timezone", equalTo("auto"))
                .withQueryParam("forecast_days", equalTo("16"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.openMeteoSuccessResponse)
                )
        )

        val httpClient = createTestHttpClient()
        val adapter = OpenMeteoAdapter(httpClient, "http://localhost:${wireMockServer.port()}")
        val location = TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378)

        val results = adapter.fetch(location)

        assertEquals(4, results.size)

        val dailyResults = results.filterIsInstance<DailyForecastResult>()
        val hourlyResults = results.filterIsInstance<HourlyForecastResult>()

        assertEquals(2, dailyResults.size)
        assertEquals(2, hourlyResults.size)

        val dailyResult = dailyResults[0]
        assertEquals("open-meteo", dailyResult.source)
        assertEquals(location, dailyResult.location)
        assertEquals(2.5, dailyResult.temperatureMinC)
        assertEquals(8.2, dailyResult.temperatureMaxC)
        assertEquals(5.3, dailyResult.temperatureMeanC)
        assertEquals(0.1, dailyResult.precipitationMmSum)
        assertEquals(12.5, dailyResult.windSpeedKph10mMax)
        assertNotNull(dailyResult.forecastTimeUtc)

        val hourlyResult = hourlyResults[0]
        assertEquals("open-meteo", hourlyResult.source)
        assertEquals(location, hourlyResult.location)
        assertEquals(5.2, hourlyResult.temperatureC)
        assertEquals(0.0, hourlyResult.precipitationMm)
        assertEquals(10.2, hourlyResult.windSpeedKph10m)
        assertNotNull(hourlyResult.forecastTimeUtc)
    }

    @Test
    fun `should handle WeatherAPI real response format`() = runTest {
        val apiKey = "test-api-key-123"

        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/forecast.json"))
                .withQueryParam("key", equalTo(apiKey))
                .withQueryParam("q", equalTo("50.0755,14.4378"))
                .withQueryParam("days", equalTo("14"))
                .withQueryParam("aqi", equalTo("no"))
                .withQueryParam("alerts", equalTo("no"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ApiResponseSamples.weatherApiSuccessResponse)
                )
        )

        val httpClient = createTestHttpClient()
        val adapter = WeatherApiAdapter(httpClient, apiKey, "http://localhost:${wireMockServer.port()}")
        val location = TestDataBuilders.createTestLocation("Prague", 50.0755, 14.4378)

        val results = adapter.fetch(location)

        assertEquals(3, results.size)

        val dailyResults = results.filterIsInstance<DailyForecastResult>()
        val hourlyResults = results.filterIsInstance<HourlyForecastResult>()

        assertEquals(1, dailyResults.size)
        assertEquals(2, hourlyResults.size)

        val dailyResult = dailyResults[0]
        assertEquals("weather-api", dailyResult.source)
        assertEquals(location, dailyResult.location)
        assertEquals(2.5, dailyResult.temperatureMinC)
        assertEquals(8.2, dailyResult.temperatureMaxC)
        assertEquals(5.3, dailyResult.temperatureMeanC)
        assertEquals(0.1, dailyResult.precipitationMmSum)
        assertEquals(12.5, dailyResult.windSpeedKph10mMax)
        assertNotNull(dailyResult.forecastTimeUtc)

        val hourlyResult = hourlyResults[0]
        assertEquals("weather-api", hourlyResult.source)
        assertEquals(location, hourlyResult.location)
        assertEquals(5.2, hourlyResult.temperatureC)
        assertEquals(0.0, hourlyResult.precipitationMm)
        assertEquals(10.2, hourlyResult.windSpeedKph10m)
        assertNotNull(hourlyResult.forecastTimeUtc)
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

}