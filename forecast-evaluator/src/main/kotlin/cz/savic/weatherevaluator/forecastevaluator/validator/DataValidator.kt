package cz.savic.weatherevaluator.forecastevaluator.validator

import cz.savic.weatherevaluator.common.model.ProcessingState
import cz.savic.weatherevaluator.forecastevaluator.config.DatabaseConfig
import cz.savic.weatherevaluator.forecastevaluator.config.ValidatorConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.DriverManager

private val logger = KotlinLogging.logger {}

class DataValidator(
    private val databaseConfig: DatabaseConfig,
    private val validatorConfig: ValidatorConfig
) {

    suspend fun validateAndUpdateStates() {
        logger.info { "Starting validation cycle..." }

        DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        ).use { connection ->

            // Validate hourly data (simple 1:1 validation)
            val hourlyUpdated = validateHourlyData(connection)
            logger.info { "Updated $hourlyUpdated hourly records to READY_FOR_PROCESSING" }

            // Validate daily data (completeness check)
            val dailyUpdated = validateDailyData(connection)
            logger.info { "Updated $dailyUpdated daily records (READY_FOR_PROCESSING or INCOMPLETE)" }

            // Validate actual weather observations (simple 1:1 validation)
            val actualUpdated = validateActualWeatherData(connection)
            logger.info { "Updated $actualUpdated actual weather records to READY_FOR_PROCESSING" }
        }

        logger.info { "Validation cycle completed" }
    }

    private fun validateHourlyData(connection: java.sql.Connection): Int {
        val sql = """
            UPDATE forecast_hourly
            SET processing_state = ?
            WHERE processing_state = ?
        """.trimIndent()

        return connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, ProcessingState.READY_FOR_PROCESSING.name)
            stmt.setString(2, ProcessingState.STORED.name)
            stmt.executeUpdate()
        }
    }

    private fun validateActualWeatherData(connection: java.sql.Connection): Int {
        val sql = """
            UPDATE actual_weather_observations
            SET processing_state = ?
            WHERE processing_state = ?
        """.trimIndent()

        return connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, ProcessingState.READY_FOR_PROCESSING.name)
            stmt.setString(2, ProcessingState.STORED.name)
            stmt.executeUpdate()
        }
    }

    private fun validateDailyData(connection: java.sql.Connection): Int {
        logger.debug { "Validating daily data with completeness threshold: ${validatorConfig.dailyCompletenessThreshold}/24 hours" }

        // First, find complete days (only closed days - yesterday and older)
        val completeDaysQuery = """
            SELECT DISTINCT
                location_name,
                TRUNC(observed_time_utc) as observation_date
            FROM actual_weather_observations
            WHERE processing_state = ?
            AND TRUNC(observed_time_utc) < TRUNC(CURRENT_DATE)
            GROUP BY location_name, TRUNC(observed_time_utc)
            HAVING COUNT(*) >= ?
        """.trimIndent()

        val completeDays = mutableSetOf<Pair<String, String>>()

        connection.prepareStatement(completeDaysQuery).use { stmt ->
            stmt.setString(1, ProcessingState.READY_FOR_PROCESSING.name)
            stmt.setInt(2, validatorConfig.dailyCompletenessThreshold)

            val resultSet = stmt.executeQuery()
            while (resultSet.next()) {
                val locationName = resultSet.getString("location_name")
                val observationDate = resultSet.getString("observation_date")
                completeDays.add(Pair(locationName, observationDate))
            }
        }

        logger.debug { "Found ${completeDays.size} complete days for validation" }

        var totalUpdated = 0

        // Update daily forecasts to READY_FOR_PROCESSING for complete days
        if (completeDays.isNotEmpty()) {
            val readyUpdateSql = """
                UPDATE forecast_daily
                SET processing_state = ?
                WHERE processing_state = ?
                AND location_name = ?
                AND target_date = TO_DATE(?, 'YYYY-MM-DD')
                AND target_date < TRUNC(CURRENT_DATE)
            """.trimIndent()

            connection.prepareStatement(readyUpdateSql).use { stmt ->
                for (dayPair in completeDays) {
                    val location = dayPair.first
                    val date = dayPair.second
                    stmt.setString(1, ProcessingState.READY_FOR_PROCESSING.name)
                    stmt.setString(2, ProcessingState.STORED.name)
                    stmt.setString(3, location)
                    stmt.setString(4, date.substring(0, 10)) // Extract YYYY-MM-DD
                    stmt.addBatch()
                }
                val readyUpdates = stmt.executeBatch().sum()
                totalUpdated += readyUpdates
                logger.debug { "Marked $readyUpdates daily forecasts as READY_FOR_PROCESSING (only closed days)" }
            }
        }

        // Mark incomplete days as INCOMPLETE (only for closed days - yesterday and older)
        val incompleteUpdateSql = """
            UPDATE forecast_daily
            SET processing_state = ?
            WHERE processing_state = ?
            AND target_date < TRUNC(CURRENT_DATE)
            AND (location_name, target_date) NOT IN (
                SELECT location_name, TO_DATE(SUBSTR(observation_date, 1, 10), 'YYYY-MM-DD')
                FROM (
                    SELECT DISTINCT
                        location_name,
                        TO_CHAR(TRUNC(observed_time_utc), 'YYYY-MM-DD') as observation_date
                    FROM actual_weather_observations
                    WHERE processing_state = ?
                    AND TRUNC(observed_time_utc) < TRUNC(CURRENT_DATE)
                    GROUP BY location_name, TRUNC(observed_time_utc)
                    HAVING COUNT(*) >= ?
                )
            )
        """.trimIndent()

        connection.prepareStatement(incompleteUpdateSql).use { stmt ->
            stmt.setString(1, ProcessingState.INCOMPLETE.name)
            stmt.setString(2, ProcessingState.STORED.name)
            stmt.setString(3, ProcessingState.READY_FOR_PROCESSING.name)
            stmt.setInt(4, validatorConfig.dailyCompletenessThreshold)
            val incompleteUpdates = stmt.executeUpdate()
            totalUpdated += incompleteUpdates
            logger.debug { "Marked $incompleteUpdates daily forecasts as INCOMPLETE (only closed days)" }
        }

        return totalUpdated
    }
}