package cz.savic.weatherevaluator.forecastwriter.persistence.mappers

import cz.savic.weatherevaluator.forecastwriter.model.HourlyForecast
import org.apache.ibatis.annotations.Mapper

@Mapper
interface HourlyForecastMapper {
    fun insert(forecast: HourlyForecast): Int
}