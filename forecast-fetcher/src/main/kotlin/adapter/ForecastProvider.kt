package cz.savic.weatherevaluator.forecastfetcher.adapter

import cz.savic.weatherevaluator.common.model.ForecastGranularity
import cz.savic.weatherevaluator.common.model.Location

interface ForecastProvider {
    suspend fun fetch(location: Location): List<ForecastResult>

    fun supportedGranularities(): Set<ForecastGranularity>
}