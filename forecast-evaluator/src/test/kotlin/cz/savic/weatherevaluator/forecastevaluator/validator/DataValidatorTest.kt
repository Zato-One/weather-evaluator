package cz.savic.weatherevaluator.forecastevaluator.validator

import cz.savic.weatherevaluator.common.model.ProcessingState
import cz.savic.weatherevaluator.forecastevaluator.config.DatabaseConfig
import cz.savic.weatherevaluator.forecastevaluator.config.ValidatorConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DataValidatorTest {

    private lateinit var databaseConfig: DatabaseConfig
    private lateinit var validatorConfig: ValidatorConfig
    private lateinit var dataValidator: DataValidator
    private lateinit var connection: Connection

    @BeforeEach
    fun setup() {
        databaseConfig = DatabaseConfig(
            connectionString = "jdbc:h2:mem:testdb;MODE=Oracle;DB_CLOSE_DELAY=-1",
            username = "test",
            password = "test"
        )

        validatorConfig = ValidatorConfig(
            intervalMinutes = 5,
            dailyCompletenessThreshold = 20,
            batchSize = 100
        )

        dataValidator = DataValidator(databaseConfig, validatorConfig)

        // Setup in-memory H2 database
        connection = DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        )

        setupTestTables()
    }

    private fun setupTestTables() {
        connection.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS forecast_hourly (
                id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                source VARCHAR2(100) NOT NULL,
                location_name VARCHAR2(100) NOT NULL,
                processing_state VARCHAR2(25) DEFAULT 'STORED' NOT NULL
            )
        """)

        connection.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS actual_weather_observations (
                id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                source VARCHAR2(100) NOT NULL,
                location_name VARCHAR2(100) NOT NULL,
                observed_time_utc TIMESTAMP NOT NULL,
                processing_state VARCHAR2(25) DEFAULT 'STORED' NOT NULL
            )
        """)

        connection.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS forecast_daily (
                id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                source VARCHAR2(100) NOT NULL,
                location_name VARCHAR2(100) NOT NULL,
                target_date DATE NOT NULL,
                processing_state VARCHAR2(25) DEFAULT 'STORED' NOT NULL
            )
        """)
    }

    @Test
    fun `should create DataValidator with valid config`() {
        assertNotNull(dataValidator)
    }

    @Test
    fun `should handle empty database gracefully`() = runBlocking {
        // This should not throw any exceptions
        dataValidator.validateAndUpdateStates()
    }

    @Test
    fun `should validate configuration values`() {
        assertEquals(5, validatorConfig.intervalMinutes)
        assertEquals(20, validatorConfig.dailyCompletenessThreshold)
        assertEquals(100, validatorConfig.batchSize)
    }

    @Test
    fun `should handle database connection properly`() {
        val testConnection = DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        )

        assertNotNull(testConnection)
        testConnection.close()
    }

    @Test
    fun `should mock SQL operations correctly`() {
        val mockConnection = mockk<Connection>()
        val mockStatement = mockk<PreparedStatement>()
        val mockResultSet = mockk<ResultSet>()

        every { mockConnection.prepareStatement(any()) } returns mockStatement
        every { mockStatement.setString(any(), any()) } returns Unit
        every { mockStatement.setInt(any(), any()) } returns Unit
        every { mockStatement.executeUpdate() } returns 5
        every { mockStatement.executeQuery() } returns mockResultSet
        every { mockResultSet.next() } returns false
        every { mockStatement.close() } returns Unit
        every { mockConnection.close() } returns Unit

        // Verify mocking works
        mockConnection.prepareStatement("SELECT 1").use { stmt ->
            assertEquals(5, stmt.executeUpdate())
        }
    }

    @Test
    fun `should validate ProcessingState enum usage`() {
        assertEquals("STORED", ProcessingState.STORED.name)
        assertEquals("READY_FOR_PROCESSING", ProcessingState.READY_FOR_PROCESSING.name)
        assertEquals("ACCURACY_PROCESSED", ProcessingState.ACCURACY_PROCESSED.name)
        assertEquals("INCOMPLETE", ProcessingState.INCOMPLETE.name)
    }
}