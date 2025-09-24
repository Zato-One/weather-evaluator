package cz.savic.weatherevaluator.actualweatherwriter.persistence.service

import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.model.Location
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ActualWeatherBatchProcessorTest {

    @Test
    fun `should process batch when batch size is reached`() {
        val mockPersistFunc: (List<WeatherObservedEvent>) -> Unit = mockk(relaxed = true)
        val processor = ActualWeatherBatchProcessor(mockPersistFunc, batchSize = 2)

        val event1 = WeatherObservedEvent(
            source = "TestSource",
            location = Location("TestLocation", 50.0, 14.0),
            observedTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
            temperatureC = 20.0,
            precipitationMm = 5.0,
            windSpeedKph10m = 15.0
        )
        val event2 = event1.copy(observedTimeUtc = LocalDateTime.of(2025, 1, 1, 13, 0))

        processor.submit(event1)
        verify(exactly = 0) { mockPersistFunc.invoke(any()) }

        processor.submit(event2)
        verify(exactly = 1) { mockPersistFunc.invoke(any()) }
    }

    @Test
    fun `should handle single event without processing`() {
        val mockPersistFunc: (List<WeatherObservedEvent>) -> Unit = mockk(relaxed = true)
        val processor = ActualWeatherBatchProcessor(mockPersistFunc, batchSize = 2)

        val event = WeatherObservedEvent(
            source = "TestSource",
            location = Location("TestLocation", 50.0, 14.0),
            observedTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
            temperatureC = 20.0,
            precipitationMm = 5.0,
            windSpeedKph10m = 15.0
        )

        processor.submit(event)

        verify(exactly = 0) { mockPersistFunc.invoke(any()) }
    }
}