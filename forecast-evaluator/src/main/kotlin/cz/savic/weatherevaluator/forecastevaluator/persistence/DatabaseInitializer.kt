package cz.savic.weatherevaluator.forecastevaluator.persistence

import cz.savic.weatherevaluator.forecastevaluator.config.DatabaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

object DatabaseInitializer {
    fun initialize(config: DatabaseConfig) {
        logger.info { "Database initialization for forecast-evaluator - skipping (migrations handled by writer services)" }
        logger.info { "Connection string: ${config.connectionString}" }
    }
}