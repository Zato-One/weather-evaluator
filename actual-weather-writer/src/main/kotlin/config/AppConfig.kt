package cz.savic.weatherevaluator.actualweatherwriter.config

data class AppConfig(
    val kafka: KafkaConfig,
    val database: DatabaseConfig
)