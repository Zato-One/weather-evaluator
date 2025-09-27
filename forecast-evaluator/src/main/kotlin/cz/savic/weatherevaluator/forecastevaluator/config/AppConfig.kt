package cz.savic.weatherevaluator.forecastevaluator.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class AppConfig(
    val database: DatabaseConfig,
    val validator: ValidatorConfig
) {
    companion object {
        fun load(): AppConfig {
            return ConfigLoaderBuilder.default()
                .addResourceSource("/application.conf")
                .build()
                .loadConfigOrThrow<AppConfig>()
        }
    }
}

data class DatabaseConfig(
    val connectionString: String = "jdbc:oracle:thin:@localhost:1521:XEPDB1",
    val username: String,
    val password: String
)

data class ValidatorConfig(
    val intervalMinutes: Int = 15,
    val dailyCompletenessThreshold: Int = 22,
    val batchSize: Int = 1000
)