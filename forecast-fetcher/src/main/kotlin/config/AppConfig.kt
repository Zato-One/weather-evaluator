package cz.savic.weatherevaluator.forecastfetcher.config

import cz.savic.weatherevaluator.forecastfetcher.model.Location

data class AppConfig(
    val kafka: KafkaConfig,
    val locations: List<Location>
)