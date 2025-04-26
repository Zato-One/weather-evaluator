package cz.savic.weatherevaluator.forecastwriter.config

data class AppConfig(
    val kafka: KafkaConfig,
    val database: DatabaseConfig
)