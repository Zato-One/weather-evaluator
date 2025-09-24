package cz.savic.weatherevaluator.actualweatherwriter.persistence.mapper

import cz.savic.weatherevaluator.actualweatherwriter.persistence.entity.ActualWeatherEntity

interface ActualWeatherMapper {
    fun insert(observation: ActualWeatherEntity)
}