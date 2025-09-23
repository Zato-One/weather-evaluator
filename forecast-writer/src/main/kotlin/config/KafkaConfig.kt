package cz.savic.weatherevaluator.forecastwriter.config

data class KafkaConfig(
    val bootstrapServers: String = "localhost:19092",
    val groupId: String = "forecast-writer",
    val topics: List<String> = listOf("forecast.fetched"),
    val pollTimeoutMs: Long = 100
)