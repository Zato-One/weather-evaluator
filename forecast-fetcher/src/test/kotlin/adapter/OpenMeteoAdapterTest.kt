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

class OpenMeteoAdapterTest {

    private val testLocation = Location("Prague", 50.0755, 14.4378)

    @Test
    fun `should parse successful response and return forecast results`() = runTest {
        val client = MockHttpClientFactory.createMockClient(ApiResponseSamples.openMeteoSuccessResponse)
        val adapter = OpenMeteoAdapter(client)

        val results = adapter.fetch(testLocation)

        assertEquals(4, results.size)

        val dailyResults = results.filterIsInstance<DailyForecastResult>()
        val hourlyResults = results.filterIsInstance<HourlyForecastResult>()

        assertEquals(2, dailyResults.size)
        assertEquals(2, hourlyResults.size)

        dailyResults[0].let { daily ->
            assertEquals("open-meteo", daily.source)
            assertEquals(testLocation, daily.location)
            assertEquals(2.5, daily.temperatureMinC)
            assertEquals(8.2, daily.temperatureMaxC)
            assertEquals(5.3, daily.temperatureMeanC)
            assertEquals(0.1, daily.precipitationMmSum)
            assertEquals(12.5, daily.windSpeedKph10mMax)
            assertNotNull(daily.forecastTimeUtc)
        }

        hourlyResults[0].let { hourly ->
            assertEquals("open-meteo", hourly.source)
            assertEquals(testLocation, hourly.location)
            assertEquals(5.2, hourly.temperatureC)
            assertEquals(0.0, hourly.precipitationMm)
            assertEquals(10.2, hourly.windSpeedKph10m)
            assertNotNull(hourly.forecastTimeUtc)
        }
    }

    @Test
    fun `should handle API error response gracefully`() = runTest {
        val client = MockHttpClientFactory.createMockClient(ApiResponseSamples.openMeteoErrorResponse, HttpStatusCode.BadRequest)
        val adapter = OpenMeteoAdapter(client)

        val results = adapter.fetch(testLocation)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `should handle network timeout exception`() = runTest {
        val client = MockHttpClientFactory.createExceptionThrowingClient(RuntimeException("Network timeout"))

        val adapter = OpenMeteoAdapter(client)

        val results = adapter.fetch(testLocation)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `should return correct supported granularities`() {
        val client = MockHttpClientFactory.createMockClient("{}")
        val adapter = OpenMeteoAdapter(client)

        val granularities = adapter.supportedGranularities()

        assertEquals(2, granularities.size)
        assertTrue(granularities.contains(ForecastGranularity.DAILY))
        assertTrue(granularities.contains(ForecastGranularity.HOURLY))
    }
}