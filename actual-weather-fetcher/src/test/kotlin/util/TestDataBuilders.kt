package cz.savic.weatherevaluator.actualweatherfetcher.util

import cz.savic.weatherevaluator.actualweatherfetcher.adapter.CurrentWeatherResult
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.model.Location
import java.time.LocalDateTime

object TestDataBuilders {

    fun createTestLocation(
        name: String = "Prague",
        latitude: Double = 50.0755,
        longitude: Double = 14.4378
    ): Location = Location(name, latitude, longitude)

    fun createCurrentWeatherResult(
        source: String = "test-source",
        location: Location = createTestLocation(),
        observedTimeUtc: LocalDateTime = LocalDateTime.now(),
        temperatureC: Double = 5.2,
        precipitationMm: Double = 0.1,
        windSpeedKph10m: Double = 10.2
    ): CurrentWeatherResult = CurrentWeatherResult(
        source = source,
        location = location,
        observedTimeUtc = observedTimeUtc,
        temperatureC = temperatureC,
        precipitationMm = precipitationMm,
        windSpeedKph10m = windSpeedKph10m
    )

    fun createWeatherObservedEvent(
        source: String = "test-source",
        location: Location = createTestLocation(),
        observedTimeUtc: LocalDateTime = LocalDateTime.now(),
        temperatureC: Double = 5.2,
        precipitationMm: Double = 0.1,
        windSpeedKph10m: Double = 10.2
    ): WeatherObservedEvent = WeatherObservedEvent(
        source = source,
        location = location,
        observedTimeUtc = observedTimeUtc,
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