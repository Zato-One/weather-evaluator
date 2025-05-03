package cz.savic.weatherevaluator.forecastwriter.persistence.mappers

import cz.savic.weatherevaluator.forecastwriter.model.DailyForecast
import org.apache.ibatis.annotations.Mapper

@Mapper
interface DailyForecastMapper {
    fun insert(forecast: DailyForecast): Int
}