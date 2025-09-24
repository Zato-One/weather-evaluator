package cz.savic.weatherevaluator.actualweatherfetcher.adapter

import cz.savic.weatherevaluator.common.model.Location

interface ActualWeatherProvider {
    suspend fun fetchCurrent(location: Location): List<ActualWeatherResult>
}