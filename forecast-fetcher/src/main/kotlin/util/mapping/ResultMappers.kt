package cz.savic.weatherevaluator.forecastfetcher.util.mapping

import cz.savic.weatherevaluator.forecastfetcher.adapter.DailyForecastResult
import cz.savic.weatherevaluator.forecastfetcher.adapter.HourlyForecastResult
import cz.savic.weatherevaluator.forecastfetcher.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.forecastfetcher.event.HourlyForecastFetchedEvent

fun DailyForecastResult.toEvent() = DailyForecastFetchedEvent(
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

fun HourlyForecastResult.toEvent() = HourlyForecastFetchedEvent(
    source = source,
    location = location,
    forecastTimeUtc = forecastTimeUtc,
    targetDateTimeUtc = targetDateTimeUtc,
    temperatureC = temperatureC,
    precipitationMm = precipitationMm,
    windSpeedKph10m = windSpeedKph10m
)
