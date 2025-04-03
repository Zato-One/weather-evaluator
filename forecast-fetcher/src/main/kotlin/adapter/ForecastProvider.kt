package cz.savic.weatherevaluator.forecastfetcher.adapter

import cz.savic.weatherevaluator.forecastfetcher.model.Location

interface ForecastProvider {
    suspend fun fetch(location: Location): ForecastResult
}