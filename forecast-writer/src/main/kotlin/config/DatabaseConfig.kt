package cz.savic.weatherevaluator.forecastwriter.config

data class DatabaseConfig(
    val connectionString: String,
    val username: String,
    val password: String
)