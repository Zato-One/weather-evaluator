package cz.savic.weatherevaluator.forecastfetcher.util.mapping

import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.forecastfetcher.adapter.DailyForecastResult
import cz.savic.weatherevaluator.forecastfetcher.adapter.ForecastResult
import cz.savic.weatherevaluator.forecastfetcher.adapter.HourlyForecastResult

fun toEvent(result: ForecastResult): ForecastFetchedEvent = when (result) {
    is DailyForecastResult -> result.toDailyEvent()
    is HourlyForecastResult -> result.toHourlyEvent()
}

fun DailyForecastResult.toDailyEvent() = DailyForecastFetchedEvent(
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

fun HourlyForecastResult.toHourlyEvent() = HourlyForecastFetchedEvent(
    source = source,
    location = location,
    forecastTimeUtc = forecastTimeUtc,
    targetDateTimeUtc = targetDateTimeUtc,
    temperatureC = temperatureC,
    precipitationMm = precipitationMm,
    windSpeedKph10m = windSpeedKph10m
)