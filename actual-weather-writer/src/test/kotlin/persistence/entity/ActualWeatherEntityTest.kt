package cz.savic.weatherevaluator.actualweatherwriter.persistence.entity

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ActualWeatherEntityTest {

    @Test
    fun `should create entity with all properties`() {
        val observedTime = LocalDateTime.of(2025, 1, 1, 12, 0)
        val createdAt = LocalDateTime.of(2025, 1, 1, 12, 1)

        val entity = ActualWeatherEntity(
            id = 1L,
            source = "TestSource",
            locationName = "TestLocation",
            latitude = 50.0,
            longitude = 14.0,
            observedTimeUtc = observedTime,
            temperatureC = 20.0,
            precipitationMm = 5.0,
            windSpeedKph10m = 15.0,
            createdAt = createdAt
        )

        assertEquals(1L, entity.id)
        assertEquals("TestSource", entity.source)
        assertEquals("TestLocation", entity.locationName)
        assertEquals(50.0, entity.latitude)
        assertEquals(14.0, entity.longitude)
        assertEquals(observedTime, entity.observedTimeUtc)
        assertEquals(20.0, entity.temperatureC)
        assertEquals(5.0, entity.precipitationMm)
        assertEquals(15.0, entity.windSpeedKph10m)
        assertEquals(createdAt, entity.createdAt)
    }

    @Test
    fun `should create entity with default null values`() {
        val observedTime = LocalDateTime.of(2025, 1, 1, 12, 0)

        val entity = ActualWeatherEntity(
            source = "TestSource",
            locationName = "TestLocation",
            latitude = 50.0,
            longitude = 14.0,
            observedTimeUtc = observedTime,
            temperatureC = 20.0,
            precipitationMm = 5.0,
            windSpeedKph10m = 15.0
        )

        assertEquals(null, entity.id)
        assertEquals(null, entity.createdAt)
    }
}