package cz.savic.weatherevaluator.forecastwriter.config

data class DatabaseConfig(
    val connectionString: String = "jdbc:oracle:thin:@localhost:1521/XEPDB1",
    val username: String = "system",
    val password: String = "oracle"
)