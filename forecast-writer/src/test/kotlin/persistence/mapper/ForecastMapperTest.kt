package cz.savic.weatherevaluator.forecastwriter.persistence.mapper

import cz.savic.weatherevaluator.forecastwriter.persistence.entity.HourlyForecastEntity
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ForecastMapperTest {

    @Test
    fun `should have correct property names in HourlyForecastEntity`() {
        val clazz = HourlyForecastEntity::class.java
        val properties = clazz.declaredFields.map { it.name }.toSet()

        val expectedProperties = setOf(
            "source", "locationName", "latitude", "longitude",
            "forecastTimeUtc", "targetDateTimeUtc",
            "temperatureC", "precipitationMm", "windSpeedKph10m"
        )

        expectedProperties.forEach { prop ->
            assertTrue(
                properties.contains(prop),
                "Property '$prop' not found. Available: $properties"
            )
        }

        assertTrue(
            properties.contains("targetDateTimeUtc"),
            "Property 'targetDateTimeUtc' must exist (not 'targetDateTime')"
        )

        println("✓ All required properties exist in HourlyForecastEntity")
        println("✓ Property 'targetDateTimeUtc' is correctly named")
    }
}