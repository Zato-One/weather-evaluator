package cz.savic.weatherevaluator.testdatagenerator.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import cz.savic.weatherevaluator.common.model.Location
import java.time.LocalDate

data class GeneratorConfig(
    val database: DatabaseConfig,
    val generation: DataGenerationConfig
) {
    companion object {
        fun load(): GeneratorConfig {
            return ConfigLoaderBuilder.default()
                .addResourceSource("/generator-application.conf")
                .build()
                .loadConfigOrThrow<GeneratorConfig>()
        }
    }
}

data class DatabaseConfig(
    val connectionString: String = "jdbc:oracle:thin:@localhost:1521/XEPDB1",
    val username: String = "weather_evaluator",
    val password: String = "weather_evaluator"
)

data class DataGenerationConfig(
    val daysBack: Int = 30,
    val baseAccuracy: Double = 0.7,
    val accuracyDecayPerDay: Double = 0.05,
    val randomVariance: Double = 0.15,
    val incompleteDaysRatio: Double = 0.1,
    val sources: List<String> = listOf("weather-api", "open-meteo", "test-source-1"),
    val locations: List<LocationConfig> = listOf(
        LocationConfig("Prague", 50.0755, 14.4378),
        LocationConfig("Brno", 49.1951, 16.6068),
        LocationConfig("Ostrava", 49.8209, 18.2625)
    )
) {
    val endDate: LocalDate = LocalDate.now().minusDays(1) // Yesterday
    val startDate: LocalDate = endDate.minusDays(daysBack.toLong())
}

data class LocationConfig(
    val name: String,
    val latitude: Double,
    val longitude: Double
) {
    fun toLocation(): Location = Location(name, latitude, longitude)
}