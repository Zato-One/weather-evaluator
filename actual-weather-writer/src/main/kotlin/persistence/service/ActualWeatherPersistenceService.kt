package cz.savic.weatherevaluator.actualweatherwriter.persistence.service

import cz.savic.weatherevaluator.actualweatherwriter.persistence.entity.ActualWeatherEntity
import cz.savic.weatherevaluator.actualweatherwriter.persistence.mapper.ActualWeatherMapper
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.ibatis.session.SqlSessionFactory

class ActualWeatherPersistenceService(private val sqlSessionFactory: SqlSessionFactory) {
    private val logger = KotlinLogging.logger {}

    fun persistBatch(events: List<WeatherObservedEvent>) {
        if (events.isEmpty()) return

        sqlSessionFactory.openSession(true).use { session ->
            val mapper = session.getMapper(ActualWeatherMapper::class.java)

            events.forEach { event ->
                val entity = mapEventToEntity(event)
                mapper.insert(entity)
            }

            session.flushStatements()
        }

        logger.debug { "Persisted ${events.size} actual weather observations" }
    }

    private fun mapEventToEntity(event: WeatherObservedEvent): ActualWeatherEntity {
        return ActualWeatherEntity(
            source = event.source,
            locationName = event.location.name,
            latitude = event.location.latitude,
            longitude = event.location.longitude,
            observedTimeUtc = event.observedTimeUtc,
            temperatureC = event.temperatureC,
            precipitationMm = event.precipitationMm,
            windSpeedKph10m = event.windSpeedKph10m
        )
    }
}