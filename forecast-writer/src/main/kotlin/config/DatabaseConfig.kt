package cz.savic.weatherevaluator.forecastwriter.config

data class DatabaseConfig(
    val connectionString: String = "jdbc:oracle:thin:@oracle:1521/XEPDB1",
    val username: String = "weather-evaluator",
    val password: String = "weather-evaluator"
)