package cz.savic.weatherevaluator.actualweatherfetcher.service

import cz.savic.weatherevaluator.actualweatherfetcher.adapter.ActualWeatherProvider
import cz.savic.weatherevaluator.actualweatherfetcher.kafka.WeatherObservedEventProducer
import cz.savic.weatherevaluator.actualweatherfetcher.util.TestDataBuilders
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ActualWeatherFetcherServiceTest {

    private val mockProvider = mockk<ActualWeatherProvider>(relaxed = true)
    private val mockProducer = mockk<WeatherObservedEventProducer>(relaxed = true)
    private val service = ActualWeatherFetcherService(mockProvider, mockProducer)

    @Test
    fun `should fetch current weather from provider and send events`() = runTest {
        val locations = TestDataBuilders.createMultipleLocations()
        val currentResult = TestDataBuilders.createCurrentWeatherResult()
        val results = listOf(currentResult)

        locations.forEach { location ->
            coEvery { mockProvider.fetchCurrent(location) } returns results
        }

        service.fetchAll(locations)

        locations.forEach { location ->
            coVerify { mockProvider.fetchCurrent(location) }
        }
        val expectedEventCount = locations.size * results.size
        verify(exactly = expectedEventCount) { mockProducer.send(any()) }
    }

    @Test
    fun `should handle provider failure without stopping other locations`() = runTest {
        val locations = TestDataBuilders.createMultipleLocations()
        val successResults = listOf(
            TestDataBuilders.createCurrentWeatherResult()
        )

        coEvery { mockProvider.fetchCurrent(locations[0]) } throws RuntimeException("API failure")
        coEvery { mockProvider.fetchCurrent(locations[1]) } returns successResults
        coEvery { mockProvider.fetchCurrent(locations[2]) } returns successResults

        service.fetchAll(locations)

        locations.forEach { location ->
            coVerify { mockProvider.fetchCurrent(location) }
        }

        verify(exactly = 2) { mockProducer.send(any()) }
    }

    @Test
    fun `should log correct statistics after processing current weather`() = runTest {
        val location = TestDataBuilders.createTestLocation()
        val currentResults = listOf(
            TestDataBuilders.createCurrentWeatherResult(source = "test-source"),
            TestDataBuilders.createCurrentWeatherResult(source = "test-source")
        )

        coEvery { mockProvider.fetchCurrent(location) } returns currentResults

        service.fetchAll(listOf(location))

        coVerify { mockProvider.fetchCurrent(location) }
        verify(exactly = currentResults.size) { mockProducer.send(any()) }
    }
}