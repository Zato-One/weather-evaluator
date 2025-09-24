package cz.savic.weatherevaluator.actualweatherfetcher.config

import cz.savic.weatherevaluator.common.model.Location

data class AppConfig(
    val kafka: KafkaConfig,
    val locations: List<Location>
)