package cz.savic.weatherevaluator.actualweatherwriter.config

data class DatabaseConfig(
    val connectionString: String = "jdbc:oracle:thin:@localhost:1521:XEPDB1",
    val username: String = "weather_evaluator",
    val password: String = "weather_evaluator"
)