package cz.savic.weatherevaluator.actualweatherfetcher.adapter

import cz.savic.weatherevaluator.actualweatherfetcher.util.ApiResponseSamples
import cz.savic.weatherevaluator.actualweatherfetcher.util.MockHttpClientFactory
import cz.savic.weatherevaluator.actualweatherfetcher.util.TestDataBuilders
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpenMeteoCurrentAdapterTest {

    @Test
    fun `should parse successful current weather response and return result`() = runTest {
        val client = MockHttpClientFactory.createMockClient(ApiResponseSamples.openMeteoCurrentSuccessResponse)
        val adapter = OpenMeteoCurrentAdapter(client)
        val testLocation = TestDataBuilders.createTestLocation()

        val results = adapter.fetchCurrent(testLocation)

        assertEquals(1, results.size)
        val result = results[0] as CurrentWeatherResult
        assertEquals("open-meteo", result.source)
        assertEquals(testLocation, result.location)
        assertEquals(5.2, result.temperatureC)
        assertEquals(0.1, result.precipitationMm)
        assertEquals(10.2, result.windSpeedKph10m)
        assertNotNull(result.observedTimeUtc)
    }

    @Test
    fun `should handle API error response and return empty list`() = runTest {
        val client = MockHttpClientFactory.createMockClient(
            ApiResponseSamples.openMeteoCurrentErrorResponse,
            HttpStatusCode.BadRequest
        )
        val adapter = OpenMeteoCurrentAdapter(client)
        val testLocation = TestDataBuilders.createTestLocation()

        val results = adapter.fetchCurrent(testLocation)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `should handle network exception and return empty list`() = runTest {
        val client = MockHttpClientFactory.createExceptionThrowingClient(RuntimeException("Network timeout"))
        val adapter = OpenMeteoCurrentAdapter(client)
        val testLocation = TestDataBuilders.createTestLocation()

        val results = adapter.fetchCurrent(testLocation)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `should handle malformed JSON response and return empty list`() = runTest {
        val client = MockHttpClientFactory.createMockClient("{}")
        val adapter = OpenMeteoCurrentAdapter(client)
        val testLocation = TestDataBuilders.createTestLocation()

        val results = adapter.fetchCurrent(testLocation)

        assertTrue(results.isEmpty())
    }
}