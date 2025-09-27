package cz.savic.weatherevaluator.forecastwriter.persistence.mapper

import cz.savic.weatherevaluator.forecastwriter.persistence.entity.DailyForecastEntity
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

    @Test
    fun `should have correct property names in DailyForecastEntity`() {
        val clazz = DailyForecastEntity::class.java
        val properties = clazz.declaredFields.map { it.name }.toSet()

        val expectedProperties = setOf(
            "source", "locationName", "latitude", "longitude",
            "forecastTimeUtc", "targetDate", "temperatureMinC",
            "temperatureMaxC", "temperatureMeanC", "precipitationMmSum",
            "windSpeedKph10mMax"
        )

        expectedProperties.forEach { prop ->
            assertTrue(
                properties.contains(prop),
                "Property '$prop' not found. Available: $properties"
            )
        }

        assertTrue(
            properties.contains("targetDate"),
            "Property 'targetDate' must exist for daily forecasts"
        )

        println("✓ All required properties exist in DailyForecastEntity")
        println("✓ Property 'targetDate' is correctly named for daily forecasts")
    }

    @Test
    fun `should have id field in both entities`() {
        val hourlyProperties = HourlyForecastEntity::class.java.declaredFields.map { it.name }.toSet()
        val dailyProperties = DailyForecastEntity::class.java.declaredFields.map { it.name }.toSet()

        assertTrue(
            hourlyProperties.contains("id"),
            "HourlyForecastEntity must have 'id' field"
        )

        assertTrue(
            dailyProperties.contains("id"),
            "DailyForecastEntity must have 'id' field"
        )

        println("✓ Both entities have id field for database primary key")
    }
}