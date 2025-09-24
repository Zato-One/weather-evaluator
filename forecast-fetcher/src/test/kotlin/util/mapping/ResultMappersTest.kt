package cz.savic.weatherevaluator.forecastfetcher.util.mapping

import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.common.model.ForecastGranularity
import cz.savic.weatherevaluator.forecastfetcher.util.TestDataBuilders
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResultMappersTest {

    @Test
    fun `should correctly map ForecastResult to ForecastFetchedEvent`() {
        val dailyResult = TestDataBuilders.createDailyForecastResult(
            source = "test-source",
            temperatureMinC = 2.5,
            temperatureMaxC = 8.2,
            temperatureMeanC = 5.3,
            precipitationMmSum = 0.1,
            windSpeedKph10mMax = 12.5
        )

        val hourlyResult = TestDataBuilders.createHourlyForecastResult(
            source = "test-source",
            temperatureC = 5.2,
            precipitationMm = 0.0,
            windSpeedKph10m = 10.2
        )

        val dailyEvent = toEvent(dailyResult)
        assertIs<DailyForecastFetchedEvent>(dailyEvent)

        assertEquals(ForecastGranularity.DAILY, dailyEvent.granularity)
        assertEquals(dailyResult.source, dailyEvent.source)
        assertEquals(dailyResult.location, dailyEvent.location)
        assertEquals(dailyResult.forecastTimeUtc, dailyEvent.forecastTimeUtc)
        assertEquals(dailyResult.targetDate, dailyEvent.targetDate)
        assertEquals(dailyResult.temperatureMinC, dailyEvent.temperatureMinC)
        assertEquals(dailyResult.temperatureMaxC, dailyEvent.temperatureMaxC)
        assertEquals(dailyResult.temperatureMeanC, dailyEvent.temperatureMeanC)
        assertEquals(dailyResult.precipitationMmSum, dailyEvent.precipitationMmSum)
        assertEquals(dailyResult.windSpeedKph10mMax, dailyEvent.windSpeedKph10mMax)

        val hourlyEvent = toEvent(hourlyResult)
        assertIs<HourlyForecastFetchedEvent>(hourlyEvent)

        assertEquals(ForecastGranularity.HOURLY, hourlyEvent.granularity)
        assertEquals(hourlyResult.source, hourlyEvent.source)
        assertEquals(hourlyResult.location, hourlyEvent.location)
        assertEquals(hourlyResult.forecastTimeUtc, hourlyEvent.forecastTimeUtc)
        assertEquals(hourlyResult.targetDateTimeUtc, hourlyEvent.targetDateTimeUtc)
        assertEquals(hourlyResult.temperatureC, hourlyEvent.temperatureC)
        assertEquals(hourlyResult.precipitationMm, hourlyEvent.precipitationMm)
        assertEquals(hourlyResult.windSpeedKph10m, hourlyEvent.windSpeedKph10m)
    }

    @Test
    fun `should map DailyForecastResult to DailyForecastFetchedEvent via extension`() {
        val dailyResult = TestDataBuilders.createDailyForecastResult()

        val event = dailyResult.toDailyEvent()

        assertEquals(ForecastGranularity.DAILY, event.granularity)
        assertEquals(dailyResult.source, event.source)
        assertEquals(dailyResult.location, event.location)
        assertEquals(dailyResult.forecastTimeUtc, event.forecastTimeUtc)
        assertEquals(dailyResult.targetDate, event.targetDate)
        assertEquals(dailyResult.temperatureMinC, event.temperatureMinC)
        assertEquals(dailyResult.temperatureMaxC, event.temperatureMaxC)
        assertEquals(dailyResult.temperatureMeanC, event.temperatureMeanC)
        assertEquals(dailyResult.precipitationMmSum, event.precipitationMmSum)
        assertEquals(dailyResult.windSpeedKph10mMax, event.windSpeedKph10mMax)
    }

    @Test
    fun `should map HourlyForecastResult to HourlyForecastFetchedEvent via extension`() {
        val hourlyResult = TestDataBuilders.createHourlyForecastResult()

        val event = hourlyResult.toHourlyEvent()

        assertEquals(ForecastGranularity.HOURLY, event.granularity)
        assertEquals(hourlyResult.source, event.source)
        assertEquals(hourlyResult.location, event.location)
        assertEquals(hourlyResult.forecastTimeUtc, event.forecastTimeUtc)
        assertEquals(hourlyResult.targetDateTimeUtc, event.targetDateTimeUtc)
        assertEquals(hourlyResult.temperatureC, event.temperatureC)
        assertEquals(hourlyResult.precipitationMm, event.precipitationMm)
        assertEquals(hourlyResult.windSpeedKph10m, event.windSpeedKph10m)
    }
}