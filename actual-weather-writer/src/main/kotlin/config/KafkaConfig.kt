package cz.savic.weatherevaluator.actualweatherwriter.config

data class KafkaConfig(
    val bootstrapServers: String = "localhost:19092",
    val groupId: String = "actual-weather-writer",
    val topics: List<String> = listOf("weather.observed"),
    val pollTimeoutMs: Long = 100
)