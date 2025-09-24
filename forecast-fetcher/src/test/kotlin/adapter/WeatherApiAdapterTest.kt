package cz.savic.weatherevaluator.forecastfetcher.adapter

import cz.savic.weatherevaluator.common.model.ForecastGranularity
import cz.savic.weatherevaluator.common.model.Location
import cz.savic.weatherevaluator.forecastfetcher.util.ApiResponseSamples
import cz.savic.weatherevaluator.forecastfetcher.util.MockHttpClientFactory
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WeatherApiAdapterTest {

    private val testLocation = Location("Prague", 50.0755, 14.4378)
    private val testApiKey = "test-api-key"

    @Test
    fun `should parse successful response with API key`() = runTest {
        val client = MockHttpClientFactory.createMockClient(ApiResponseSamples.weatherApiSuccessResponse)
        val adapter = WeatherApiAdapter(client, testApiKey)

        val results = adapter.fetch(testLocation)

        assertEquals(3, results.size)

        val dailyResults = results.filterIsInstance<DailyForecastResult>()
        val hourlyResults = results.filterIsInstance<HourlyForecastResult>()

        assertEquals(1, dailyResults.size)
        assertEquals(2, hourlyResults.size)

        dailyResults[0].let { daily ->
            assertEquals("weather-api", daily.source)
            assertEquals(testLocation, daily.location)
            assertEquals(2.5, daily.temperatureMinC)
            assertEquals(8.2, daily.temperatureMaxC)
            assertEquals(5.3, daily.temperatureMeanC)
            assertEquals(0.1, daily.precipitationMmSum)
            assertEquals(12.5, daily.windSpeedKph10mMax)
            assertNotNull(daily.forecastTimeUtc)
        }

        hourlyResults[0].let { hourly ->
            assertEquals("weather-api", hourly.source)
            assertEquals(testLocation, hourly.location)
            assertEquals(5.2, hourly.temperatureC)
            assertEquals(0.0, hourly.precipitationMm)
            assertEquals(10.2, hourly.windSpeedKph10m)
            assertNotNull(hourly.forecastTimeUtc)
        }
    }

    @Test
    fun `should handle API error response with proper logging`() = runTest {
        val client = MockHttpClientFactory.createMockClient(ApiResponseSamples.weatherApiLocationErrorResponse, HttpStatusCode.BadRequest)
        val adapter = WeatherApiAdapter(client, testApiKey)

        val results = adapter.fetch(testLocation)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `should handle invalid API key error`() = runTest {
        val client = MockHttpClientFactory.createMockClient(ApiResponseSamples.weatherApiInvalidKeyErrorResponse, HttpStatusCode.Forbidden)
        val adapter = WeatherApiAdapter(client, "invalid-key")

        val results = adapter.fetch(testLocation)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `should return correct supported granularities`() {
        val client = MockHttpClientFactory.createMockClient("{}")
        val adapter = WeatherApiAdapter(client, testApiKey)

        val granularities = adapter.supportedGranularities()

        assertEquals(2, granularities.size)
        assertTrue(granularities.contains(ForecastGranularity.DAILY))
        assertTrue(granularities.contains(ForecastGranularity.HOURLY))
    }
}