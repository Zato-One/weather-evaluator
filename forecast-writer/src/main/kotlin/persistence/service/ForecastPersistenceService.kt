package cz.savic.weatherevaluator.forecastwriter.persistence.service

import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.forecastwriter.persistence.entity.DailyForecastEntity
import cz.savic.weatherevaluator.forecastwriter.persistence.entity.HourlyForecastEntity
import cz.savic.weatherevaluator.forecastwriter.persistence.mapper.ForecastMapper
import org.apache.ibatis.session.SqlSessionFactory

class ForecastPersistenceService(
    private val sessionFactory: SqlSessionFactory
) {
    fun persistBatch(events: List<ForecastFetchedEvent>) {
        val dailyEvents = events.filterIsInstance<DailyForecastFetchedEvent>()
        val hourlyEvents = events.filterIsInstance<HourlyForecastFetchedEvent>()

        val dailyForecasts = dailyEvents.map { event ->
            DailyForecastEntity(
                source = event.source,
                locationName = event.location.name,
                latitude = event.location.latitude,
                longitude = event.location.longitude,
                forecastTimeUtc = event.forecastTimeUtc,
                targetDate = event.targetDate,
                temperatureMinC = event.temperatureMinC,
                temperatureMaxC = event.temperatureMaxC,
                temperatureMeanC = event.temperatureMeanC,
                precipitationMmSum = event.precipitationMmSum,
                windSpeedKph10mMax = event.windSpeedKph10mMax
            )
        }

        val hourlyForecasts = hourlyEvents.map { event ->
            HourlyForecastEntity(
                source = event.source,
                locationName = event.location.name,
                latitude = event.location.latitude,
                longitude = event.location.longitude,
                forecastTimeUtc = event.forecastTimeUtc,
                targetDateTimeUtc = event.targetDateTimeUtc,
                temperatureC = event.temperatureC,
                precipitationMm = event.precipitationMm,
                windSpeedKph10m = event.windSpeedKph10m
            )
        }

        val session = sessionFactory.openSession(true)
        val mapper = session.getMapper(ForecastMapper::class.java)

        dailyForecasts.forEach { mapper.insertDaily(it) }
        hourlyForecasts.forEach { mapper.insertHourly(it) }
    }
}