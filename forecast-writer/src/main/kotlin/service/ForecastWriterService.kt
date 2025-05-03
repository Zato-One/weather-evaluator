package cz.savic.weatherevaluator.forecastwriter.service

import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.forecastwriter.model.DailyForecast
import cz.savic.weatherevaluator.forecastwriter.model.HourlyForecast
import cz.savic.weatherevaluator.forecastwriter.persistence.repository.DailyForecastRepository
import cz.savic.weatherevaluator.forecastwriter.persistence.repository.HourlyForecastRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

class ForecastWriterService(
    private val dailyForecastRepository: DailyForecastRepository,
    private val hourlyForecastRepository: HourlyForecastRepository
) {
    private val logger = KotlinLogging.logger {}
    private val dailyProcessed = AtomicInteger(0)
    private val hourlyProcessed = AtomicInteger(0)
    private val dailySaved = AtomicInteger(0)
    private val hourlySaved = AtomicInteger(0)
    
    private val dailyBatchProcessor = ForecastBatchProcessor { events ->
        processDailyEventBatch(events.filterIsInstance<DailyForecastFetchedEvent>())
    }
    
    private val hourlyBatchProcessor = ForecastBatchProcessor { events -> 
        processHourlyEventBatch(events.filterIsInstance<HourlyForecastFetchedEvent>())
    }
    
    fun processForecastEvent(event: ForecastFetchedEvent) {
        when (event) {
            is DailyForecastFetchedEvent -> dailyBatchProcessor.submit(event)
            is HourlyForecastFetchedEvent -> hourlyBatchProcessor.submit(event)
        }
    }
    
    fun flushPendingForecasts() {
        dailyBatchProcessor.forceBatchProcessing()
        hourlyBatchProcessor.forceBatchProcessing()
    }
    
    private fun processDailyEventBatch(events: List<DailyForecastFetchedEvent>) {
        if (events.isEmpty()) return
        
        try {
            logger.info { "Processing batch of ${events.size} daily forecast events" }
            dailyProcessed.addAndGet(events.size)
            
            val forecasts = events.map { event ->
                DailyForecast(
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
            
            val savedCount = dailyForecastRepository.saveAll(forecasts)
            dailySaved.addAndGet(savedCount)
            
        } catch (e: Exception) {
            logger.error(e) { "Error processing daily forecast event batch" }
        }
    }
    
    private fun processHourlyEventBatch(events: List<HourlyForecastFetchedEvent>) {
        if (events.isEmpty()) return
        
        try {
            logger.info { "Processing batch of ${events.size} hourly forecast events" }
            hourlyProcessed.addAndGet(events.size)
            
            val forecasts = events.map { event ->
                HourlyForecast(
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
            
            val savedCount = hourlyForecastRepository.saveAll(forecasts)
            hourlySaved.addAndGet(savedCount)
            
        } catch (e: Exception) {
            logger.error(e) { "Error processing hourly forecast event batch" }
        }
    }
    
    private fun processDailyEvent(event: DailyForecastFetchedEvent) {
        try {
            dailyProcessed.incrementAndGet()
            
            val forecast = DailyForecast(
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
            
            if (dailyForecastRepository.save(forecast)) {
                dailySaved.incrementAndGet()
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Error processing daily forecast event: $event" }
        }
    }
    
    private fun processHourlyEvent(event: HourlyForecastFetchedEvent) {
        try {
            hourlyProcessed.incrementAndGet()
            
            val forecast = HourlyForecast(
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
            
            if (hourlyForecastRepository.save(forecast)) {
                hourlySaved.incrementAndGet()
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Error processing hourly forecast event: $event" }
        }
    }
    
    fun logProcessingStats() {
        val dailyProcessedCount = dailyProcessed.get()
        val hourlyProcessedCount = hourlyProcessed.get()
        val dailySavedCount = dailySaved.get()
        val hourlySavedCount = hourlySaved.get()
        
        logger.info { 
            "Forecast processing statistics - " +
            "Daily: processed=$dailyProcessedCount, saved=$dailySavedCount; " +
            "Hourly: processed=$hourlyProcessedCount, saved=$hourlySavedCount" 
        }
    }
}