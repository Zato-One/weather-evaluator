package cz.savic.weatherevaluator.forecastwriter.persistence.repository

import cz.savic.weatherevaluator.forecastwriter.model.DailyForecast
import cz.savic.weatherevaluator.forecastwriter.persistence.mappers.DailyForecastMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.ibatis.session.SqlSessionFactory

class DailyForecastRepository(private val sqlSessionFactory: SqlSessionFactory) {
    private val logger = KotlinLogging.logger {}
    
    fun save(forecast: DailyForecast): Boolean {
        return sqlSessionFactory.openSession(true).use { session ->
            try {
                val mapper = session.getMapper(DailyForecastMapper::class.java)
                val rowsAffected = mapper.insert(forecast)
                
                if (rowsAffected > 0) {
                    logger.debug { "Saved daily forecast for ${forecast.locationName} on ${forecast.targetDate}" }
                    true
                } else {
                    logger.warn { "Failed to save daily forecast for ${forecast.locationName} on ${forecast.targetDate}" }
                    false
                }
            } catch (e: Exception) {
                logger.error(e) { "Error saving daily forecast" }
                false
            }
        }
    }
    
    fun saveAll(forecasts: List<DailyForecast>): Int {
        var savedCount = 0
        
        sqlSessionFactory.openSession(false).use { session ->
            try {
                val mapper = session.getMapper(DailyForecastMapper::class.java)
                
                for (forecast in forecasts) {
                    val rowsAffected = mapper.insert(forecast)
                    if (rowsAffected > 0) {
                        savedCount++
                    }
                }
                
                session.commit()
                logger.info { "Successfully saved $savedCount/${forecasts.size} daily forecasts" }
            } catch (e: Exception) {
                session.rollback()
                logger.error(e) { "Error batch saving daily forecasts, rolling back transaction" }
            }
        }
        
        return savedCount
    }
}