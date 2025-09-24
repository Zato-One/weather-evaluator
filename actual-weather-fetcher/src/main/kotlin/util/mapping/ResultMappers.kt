package cz.savic.weatherevaluator.actualweatherfetcher.util.mapping

import cz.savic.weatherevaluator.actualweatherfetcher.adapter.ActualWeatherResult
import cz.savic.weatherevaluator.actualweatherfetcher.adapter.CurrentWeatherResult
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent

fun toEvent(result: ActualWeatherResult): WeatherObservedEvent {
    return when (result) {
        is CurrentWeatherResult -> WeatherObservedEvent(
            source = result.source,
            location = result.location,
            observedTimeUtc = result.observedTimeUtc,
            temperatureC = result.temperatureC,
            precipitationMm = result.precipitationMm,
            windSpeedKph10m = result.windSpeedKph10m
        )
    }
}