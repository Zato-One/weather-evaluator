package cz.savic.weatherevaluator.forecastfetcher.service

import cz.savic.weatherevaluator.forecastfetcher.adapter.ForecastProvider
import cz.savic.weatherevaluator.forecastfetcher.kafka.ForecastEventProducer
import cz.savic.weatherevaluator.forecastfetcher.util.TestDataBuilders
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ForecastFetcherServiceTest {

    private val mockProvider = mockk<ForecastProvider>(relaxed = true)
    private val mockProducer = mockk<ForecastEventProducer>(relaxed = true)
    private val service = ForecastFetcherService(mockProvider, mockProducer)

    @Test
    fun `should fetch forecasts from provider and send events`() = runTest {
        val locations = TestDataBuilders.createMultipleLocations()
        val dailyResult = TestDataBuilders.createDailyForecastResult()
        val hourlyResult = TestDataBuilders.createHourlyForecastResult()
        val results = listOf(dailyResult, hourlyResult)

        locations.forEach { location ->
            coEvery { mockProvider.fetch(location) } returns results
        }

        service.fetchAll(locations)

        locations.forEach { location ->
            coVerify { mockProvider.fetch(location) }
        }
        val expectedEventCount = locations.size * results.size
        verify(exactly = expectedEventCount) { mockProducer.send(any()) }
    }

    @Test
    fun `should handle provider failure without stopping other locations`() = runTest {
        val locations = TestDataBuilders.createMultipleLocations()
        val successResults = listOf(
            TestDataBuilders.createDailyForecastResult(),
            TestDataBuilders.createHourlyForecastResult()
        )

        coEvery { mockProvider.fetch(locations[0]) } throws RuntimeException("API failure")
        coEvery { mockProvider.fetch(locations[1]) } returns successResults
        coEvery { mockProvider.fetch(locations[2]) } returns successResults

        service.fetchAll(locations)
        locations.forEach { location ->
            coVerify { mockProvider.fetch(location) }
        }

        verify(exactly = 4) { mockProducer.send(any()) }
    }

    @Test
    fun `should log correct statistics after processing`() = runTest {
        val location = TestDataBuilders.createTestLocation()
        val dailyResults = listOf(
            TestDataBuilders.createDailyForecastResult(source = "test-source"),
            TestDataBuilders.createDailyForecastResult(source = "test-source")
        )
        val hourlyResults = listOf(
            TestDataBuilders.createHourlyForecastResult(source = "test-source")
        )
        val allResults = dailyResults + hourlyResults

        coEvery { mockProvider.fetch(location) } returns allResults

        service.fetchAll(listOf(location))

        coVerify { mockProvider.fetch(location) }
        verify(exactly = allResults.size) { mockProducer.send(any()) }
    }
}