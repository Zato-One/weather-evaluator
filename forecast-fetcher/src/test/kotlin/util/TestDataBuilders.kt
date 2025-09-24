package cz.savic.weatherevaluator.forecastfetcher.util

import cz.savic.weatherevaluator.common.model.Location
import cz.savic.weatherevaluator.forecastfetcher.adapter.DailyForecastResult
import cz.savic.weatherevaluator.forecastfetcher.adapter.HourlyForecastResult
import java.time.LocalDate
import java.time.LocalDateTime

object TestDataBuilders {

    fun createTestLocation(
        name: String = "Prague",
        latitude: Double = 50.0755,
        longitude: Double = 14.4378
    ): Location = Location(name, latitude, longitude)

    fun createDailyForecastResult(
        source: String = "test-source",
        location: Location = createTestLocation(),
        forecastTimeUtc: LocalDateTime = LocalDateTime.now(),
        targetDate: LocalDate = LocalDate.now().plusDays(1),
        temperatureMinC: Double = 2.5,
        temperatureMaxC: Double = 8.2,
        temperatureMeanC: Double = 5.3,
        precipitationMmSum: Double = 0.1,
        windSpeedKph10mMax: Double = 12.5
    ): DailyForecastResult = DailyForecastResult(
        source = source,
        location = location,
        forecastTimeUtc = forecastTimeUtc,
        targetDate = targetDate,
        temperatureMinC = temperatureMinC,
        temperatureMaxC = temperatureMaxC,
        temperatureMeanC = temperatureMeanC,
        precipitationMmSum = precipitationMmSum,
        windSpeedKph10mMax = windSpeedKph10mMax
    )

    fun createHourlyForecastResult(
        source: String = "test-source",
        location: Location = createTestLocation(),
        forecastTimeUtc: LocalDateTime = LocalDateTime.now(),
        targetDateTimeUtc: LocalDateTime = LocalDateTime.now().plusHours(1),
        temperatureC: Double = 5.2,
        precipitationMm: Double = 0.0,
        windSpeedKph10m: Double = 10.2
    ): HourlyForecastResult = HourlyForecastResult(
        source = source,
        location = location,
        forecastTimeUtc = forecastTimeUtc,
        targetDateTimeUtc = targetDateTimeUtc,
        temperatureC = temperatureC,
        precipitationMm = precipitationMm,
        windSpeedKph10m = windSpeedKph10m
    )

    fun createMultipleLocations(): List<Location> = listOf(
        createTestLocation("Prague", 50.0755, 14.4378),
        createTestLocation("Brno", 49.1951, 16.6068),
        createTestLocation("Ostrava", 49.8209, 18.2625)
    )
}