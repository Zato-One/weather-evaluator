package cz.savic.weatherevaluator.forecastfetcher.config

data class KafkaConfig(
    val bootstrapServers: String = "localhost:19092",
    val topic: String
)