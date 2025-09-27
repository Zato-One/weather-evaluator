package cz.savic.weatherevaluator.forecastevaluator

import cz.savic.weatherevaluator.common.model.ProcessingState
import cz.savic.weatherevaluator.forecastevaluator.config.DatabaseConfig
import cz.savic.weatherevaluator.forecastevaluator.config.ValidatorConfig
import cz.savic.weatherevaluator.forecastevaluator.validator.DataValidator
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegrationTest {

    private lateinit var connection: Connection
    private lateinit var dataValidator: DataValidator

    @BeforeEach
    fun setup() {
        val databaseConfig = DatabaseConfig(
            connectionString = "jdbc:h2:mem:integrationtest_${System.currentTimeMillis()};MODE=Oracle;DB_CLOSE_DELAY=-1",
            username = "test",
            password = "test"
        )

        val validatorConfig = ValidatorConfig(
            intervalMinutes = 1,
            dailyCompletenessThreshold = 20,
            batchSize = 100
        )

        connection = DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        )

        dataValidator = DataValidator(databaseConfig, validatorConfig)

        setupTestSchema()
    }

    @AfterEach
    fun cleanup() {
        // Clean up tables for next test
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS forecast_hourly")
            connection.createStatement().execute("DROP TABLE IF EXISTS actual_weather_observations")
            connection.createStatement().execute("DROP TABLE IF EXISTS forecast_daily")
        } catch (e: Exception) {
            // Ignore cleanup errors
        }

        try {
            connection.close()
        } catch (e: Exception) {
            // Ignore close errors
        }
    }

    private fun setupTestSchema() {
        // Drop tables if they exist (for cleanup)
        try {
            connection.createStatement().execute("DROP TABLE IF EXISTS forecast_hourly")
            connection.createStatement().execute("DROP TABLE IF EXISTS actual_weather_observations")
            connection.createStatement().execute("DROP TABLE IF EXISTS forecast_daily")
        } catch (e: Exception) {
            // Ignore errors if tables don't exist
        }

        // Create test tables similar to real schema
        connection.createStatement().execute("""
            CREATE TABLE forecast_hourly (
                id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                source VARCHAR2(100) NOT NULL,
                location_name VARCHAR2(100) NOT NULL,
                processing_state VARCHAR2(25) DEFAULT 'READY_FOR_PROCESSING' NOT NULL
            )
        """)

        connection.createStatement().execute("""
            CREATE TABLE actual_weather_observations (
                id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                source VARCHAR2(100) NOT NULL,
                location_name VARCHAR2(100) NOT NULL,
                observed_time_utc TIMESTAMP NOT NULL,
                processing_state VARCHAR2(25) DEFAULT 'READY_FOR_PROCESSING' NOT NULL
            )
        """)

        connection.createStatement().execute("""
            CREATE TABLE forecast_daily (
                id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                source VARCHAR2(100) NOT NULL,
                location_name VARCHAR2(100) NOT NULL,
                target_date DATE NOT NULL,
                processing_state VARCHAR2(25) DEFAULT 'STORED' NOT NULL
            )
        """)
    }

    @Test
    fun `should validate empty database without errors`() = runBlocking {
        // Should not throw any exceptions with empty database
        dataValidator.validateAndUpdateStates()

        // Verify no data exists
        val count = connection.createStatement().executeQuery("SELECT COUNT(*) FROM forecast_daily").use { rs ->
            rs.next()
            rs.getInt(1)
        }
        assertEquals(0, count)
    }

    @Test
    fun `should handle basic data insertion and validation`() = runBlocking {
        // Insert test data
        connection.prepareStatement("""
            INSERT INTO forecast_hourly (source, location_name, processing_state)
            VALUES (?, ?, ?)
        """).use { stmt ->
            stmt.setString(1, "test-source")
            stmt.setString(2, "test-location")
            stmt.setString(3, ProcessingState.STORED.name)
            stmt.executeUpdate()
        }

        // Run validation
        dataValidator.validateAndUpdateStates()

        // Verify hourly data was updated to READY_FOR_PROCESSING
        val state = connection.prepareStatement("""
            SELECT processing_state FROM forecast_hourly WHERE source = ?
        """).use { stmt ->
            stmt.setString(1, "test-source")
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getString("processing_state")
            }
        }

        assertEquals(ProcessingState.READY_FOR_PROCESSING.name, state)
    }

    @Test
    fun `should validate processing state constants`() {
        // Verify all states are available
        assertTrue(ProcessingState.entries.isNotEmpty())
        assertTrue(ProcessingState.entries.contains(ProcessingState.STORED))
        assertTrue(ProcessingState.entries.contains(ProcessingState.READY_FOR_PROCESSING))
        assertTrue(ProcessingState.entries.contains(ProcessingState.ACCURACY_PROCESSED))
        assertTrue(ProcessingState.entries.contains(ProcessingState.INCOMPLETE))
    }

    @Test
    fun `should handle database connection lifecycle`() {
        assertTrue(connection.isValid(1))

        // Test basic SQL operations
        val result = connection.createStatement().executeQuery("SELECT 1 FROM dual").use { rs ->
            rs.next()
            rs.getInt(1)
        }

        assertEquals(1, result)
    }
}