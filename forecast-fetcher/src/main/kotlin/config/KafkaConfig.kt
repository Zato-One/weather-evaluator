package cz.savic.weatherevaluator.forecastfetcher.config

data class KafkaConfig(
    val bootstrapServers: String = "localhost:9092",
    val topic: String
)