package cz.savic.weatherevaluator.forecastwriter.persistence.mapper

import cz.savic.weatherevaluator.forecastwriter.persistence.entity.DailyForecastEntity
import cz.savic.weatherevaluator.forecastwriter.persistence.entity.HourlyForecastEntity

interface ForecastMapper {
    fun insertDaily(forecast: DailyForecastEntity)
    fun insertHourly(forecast: HourlyForecastEntity)
}