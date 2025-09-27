package cz.savic.weatherevaluator.forecastevaluator.persistence.service

import cz.savic.weatherevaluator.forecastevaluator.accuracy.DailyForecastData
import cz.savic.weatherevaluator.forecastevaluator.accuracy.HourlyActualData
import cz.savic.weatherevaluator.forecastevaluator.accuracy.HourlyForecastData
import cz.savic.weatherevaluator.forecastevaluator.persistence.mapper.DataRetrieverMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.ibatis.session.SqlSessionFactory

private val logger = KotlinLogging.logger {}

class DataRetrievalService(private val sqlSessionFactory: SqlSessionFactory) {

    fun getReadyHourlyForecasts(batchSize: Int): List<HourlyForecastData> {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(DataRetrieverMapper::class.java)
            return mapper.getReadyHourlyForecasts(batchSize)
        }
    }

    fun getReadyDailyForecasts(batchSize: Int): List<DailyForecastData> {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(DataRetrieverMapper::class.java)
            return mapper.getReadyDailyForecasts(batchSize)
        }
    }

    fun getActualWeatherForHour(locationName: String, targetTime: java.time.LocalDateTime): HourlyActualData? {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(DataRetrieverMapper::class.java)
            return mapper.getActualWeatherForHour(locationName, targetTime)
        }
    }

    fun getActualWeatherForDay(locationName: String, targetDate: java.time.LocalDate): List<HourlyActualData> {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(DataRetrieverMapper::class.java)
            return mapper.getActualWeatherForDay(locationName, targetDate)
        }
    }

    fun markHourlyForecastProcessed(forecast: HourlyForecastData) {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(DataRetrieverMapper::class.java)
            mapper.markHourlyForecastProcessed(
                forecast.source,
                forecast.locationName,
                forecast.forecastTime,
                forecast.targetTime
            )
            session.commit()
            logger.debug { "Marked hourly forecast as processed: ${forecast.locationName} at ${forecast.targetTime}" }
        }
    }

    fun markDailyForecastProcessed(forecast: DailyForecastData) {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(DataRetrieverMapper::class.java)
            mapper.markDailyForecastProcessed(
                forecast.source,
                forecast.locationName,
                forecast.forecastTime,
                forecast.targetDate
            )
            session.commit()
            logger.debug { "Marked daily forecast as processed: ${forecast.locationName} on ${forecast.targetDate}" }
        }
    }

    fun markHourlyForecastsBatchProcessed(forecasts: List<HourlyForecastData>) {
        if (forecasts.isEmpty()) return

        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(DataRetrieverMapper::class.java)
            mapper.markHourlyForecastsBatchProcessed(forecasts)
            session.commit()
            logger.info { "Marked ${forecasts.size} hourly forecasts as processed" }
        }
    }

    fun markDailyForecastsBatchProcessed(forecasts: List<DailyForecastData>) {
        if (forecasts.isEmpty()) return

        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(DataRetrieverMapper::class.java)
            mapper.markDailyForecastsBatchProcessed(forecasts)
            session.commit()
            logger.info { "Marked ${forecasts.size} daily forecasts as processed" }
        }
    }
}