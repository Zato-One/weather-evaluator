package cz.savic.weatherevaluator.testdatagenerator

import cz.savic.weatherevaluator.testdatagenerator.config.GeneratorConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.DriverManager

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting database cleanup..." }

    try {
        val config = GeneratorConfig.load()
        clearDatabase(config.database)
        logger.info { "Database cleanup completed successfully" }
    } catch (e: Exception) {
        logger.error(e) { "Database cleanup failed" }
        throw e
    }
}

fun clearDatabase(databaseConfig: cz.savic.weatherevaluator.testdatagenerator.config.DatabaseConfig) {
    DriverManager.getConnection(
        databaseConfig.connectionString,
        databaseConfig.username,
        databaseConfig.password
    ).use { connection ->

        val tables = listOf(
            "forecast_daily",
            "forecast_hourly",
            "actual_weather_observations",
            "accuracy_daily",
            "accuracy_hourly"
        )

        tables.forEach { table ->
            val sql = "DELETE FROM $table"
            val deletedRows = connection.createStatement().executeUpdate(sql)
            logger.info { "Cleared $deletedRows rows from $table" }
        }

        logger.info { "Database cleanup completed" }
    }
}