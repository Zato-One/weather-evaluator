package cz.savic.weatherevaluator.forecastevaluator.persistence.mapper

import cz.savic.weatherevaluator.forecastevaluator.accuracy.DailyForecastData
import cz.savic.weatherevaluator.forecastevaluator.accuracy.HourlyActualData
import cz.savic.weatherevaluator.forecastevaluator.accuracy.HourlyForecastData
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.LocalDate

@Mapper
interface DataRetrieverMapper {

    fun getReadyHourlyForecasts(@Param("batchSize") batchSize: Int): List<HourlyForecastData>

    fun getReadyDailyForecasts(@Param("batchSize") batchSize: Int): List<DailyForecastData>

    fun getActualWeatherForHour(
        @Param("locationName") locationName: String,
        @Param("targetTime") targetTime: java.time.LocalDateTime
    ): HourlyActualData?

    fun getActualWeatherForDay(
        @Param("locationName") locationName: String,
        @Param("targetDate") targetDate: LocalDate
    ): List<HourlyActualData>

    fun markHourlyForecastProcessed(
        @Param("source") source: String,
        @Param("locationName") locationName: String,
        @Param("forecastTime") forecastTime: java.time.LocalDateTime,
        @Param("targetTime") targetTime: java.time.LocalDateTime
    )

    fun markDailyForecastProcessed(
        @Param("source") source: String,
        @Param("locationName") locationName: String,
        @Param("forecastTime") forecastTime: java.time.LocalDateTime,
        @Param("targetDate") targetDate: LocalDate
    )

    fun markHourlyForecastsBatchProcessed(@Param("forecasts") forecasts: List<HourlyForecastData>)

    fun markDailyForecastsBatchProcessed(@Param("forecasts") forecasts: List<DailyForecastData>)
}