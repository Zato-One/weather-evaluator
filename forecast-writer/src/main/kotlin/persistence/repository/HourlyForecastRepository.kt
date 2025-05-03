package cz.savic.weatherevaluator.forecastwriter.persistence.repository

import cz.savic.weatherevaluator.forecastwriter.model.HourlyForecast
import cz.savic.weatherevaluator.forecastwriter.persistence.mappers.HourlyForecastMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.ibatis.session.SqlSessionFactory

class HourlyForecastRepository(private val sqlSessionFactory: SqlSessionFactory) {
    private val logger = KotlinLogging.logger {}
    
    fun save(forecast: HourlyForecast): Boolean {
        return sqlSessionFactory.openSession(true).use { session ->
            try {
                val mapper = session.getMapper(HourlyForecastMapper::class.java)
                val rowsAffected = mapper.insert(forecast)
                
                if (rowsAffected > 0) {
                    logger.debug { "Saved hourly forecast for ${forecast.locationName} at ${forecast.targetDateTimeUtc}" }
                    true
                } else {
                    logger.warn { "Failed to save hourly forecast for ${forecast.locationName} at ${forecast.targetDateTimeUtc}" }
                    false
                }
            } catch (e: Exception) {
                logger.error(e) { "Error saving hourly forecast" }
                false
            }
        }
    }
    
    fun saveAll(forecasts: List<HourlyForecast>): Int {
        var savedCount = 0
        
        sqlSessionFactory.openSession(false).use { session ->
            try {
                val mapper = session.getMapper(HourlyForecastMapper::class.java)
                
                for (forecast in forecasts) {
                    val rowsAffected = mapper.insert(forecast)
                    if (rowsAffected > 0) {
                        savedCount++
                    }
                }
                
                session.commit()
                logger.info { "Successfully saved $savedCount/${forecasts.size} hourly forecasts" }
            } catch (e: Exception) {
                session.rollback()
                logger.error(e) { "Error batch saving hourly forecasts, rolling back transaction" }
            }
        }
        
        return savedCount
    }
}