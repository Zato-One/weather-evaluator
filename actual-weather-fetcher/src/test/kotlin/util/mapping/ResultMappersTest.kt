package cz.savic.weatherevaluator.actualweatherfetcher.util.mapping

import cz.savic.weatherevaluator.actualweatherfetcher.util.TestDataBuilders
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ResultMappersTest {

    @Test
    fun `should correctly map CurrentWeatherResult to WeatherObservedEvent`() {
        val currentResult = TestDataBuilders.createCurrentWeatherResult(
            source = "test-source",
            temperatureC = 5.2,
            precipitationMm = 0.1,
            windSpeedKph10m = 10.2
        )

        val event = toEvent(currentResult)

        assertEquals(currentResult.source, event.source)
        assertEquals(currentResult.location, event.location)
        assertEquals(currentResult.observedTimeUtc, event.observedTimeUtc)
        assertEquals(currentResult.temperatureC, event.temperatureC)
        assertEquals(currentResult.precipitationMm, event.precipitationMm)
        assertEquals(currentResult.windSpeedKph10m, event.windSpeedKph10m)
    }

    @Test
    fun `should map CurrentWeatherResult to WeatherObservedEvent via extension`() {
        val currentResult = TestDataBuilders.createCurrentWeatherResult()

        val event = currentResult.toEvent()

        assertEquals(currentResult.source, event.source)
        assertEquals(currentResult.location, event.location)
        assertEquals(currentResult.observedTimeUtc, event.observedTimeUtc)
        assertEquals(currentResult.temperatureC, event.temperatureC)
        assertEquals(currentResult.precipitationMm, event.precipitationMm)
        assertEquals(currentResult.windSpeedKph10m, event.windSpeedKph10m)
    }

    @Test
    fun `should preserve all data fields during mapping`() {
        val location = TestDataBuilders.createTestLocation("TestCity", 12.34, 56.78)
        val currentResult = TestDataBuilders.createCurrentWeatherResult(
            source = "open-meteo",
            location = location,
            temperatureC = -2.5,
            precipitationMm = 15.7,
            windSpeedKph10m = 45.3
        )

        val event = toEvent(currentResult)

        assertEquals("open-meteo", event.source)
        assertEquals("TestCity", event.location.name)
        assertEquals(12.34, event.location.latitude)
        assertEquals(56.78, event.location.longitude)
        assertEquals(-2.5, event.temperatureC)
        assertEquals(15.7, event.precipitationMm)
        assertEquals(45.3, event.windSpeedKph10m)
        assertEquals(currentResult.observedTimeUtc, event.observedTimeUtc)
    }
}