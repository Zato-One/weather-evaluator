package cz.savic.weatherevaluator.forecastfetcher.config

data class KafkaConfig(
    val bootstrapServers: String,
    val topic: String
)