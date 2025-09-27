package cz.savic.weatherevaluator.forecastevaluator.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppConfigTest {

    @Test
    fun `should load test configuration`() {
        val config = ConfigLoaderBuilder.default()
            .addResourceSource("/test-application.conf")
            .build()
            .loadConfigOrThrow<AppConfig>()

        assertNotNull(config)
        assertNotNull(config.database)
        assertNotNull(config.validator)
    }

    @Test
    fun `should load database config correctly`() {
        val config = ConfigLoaderBuilder.default()
            .addResourceSource("/test-application.conf")
            .build()
            .loadConfigOrThrow<AppConfig>()

        assertEquals("jdbc:h2:mem:testdb;MODE=Oracle;DB_CLOSE_DELAY=-1", config.database.connectionString)
        assertEquals("test", config.database.username)
        assertEquals("test", config.database.password)
    }

    @Test
    fun `should load validator config correctly`() {
        val config = ConfigLoaderBuilder.default()
            .addResourceSource("/test-application.conf")
            .build()
            .loadConfigOrThrow<AppConfig>()

        assertEquals(5, config.validator.intervalMinutes)
        assertEquals(20, config.validator.dailyCompletenessThreshold)
        assertEquals(100, config.validator.batchSize)
    }

    @Test
    fun `should create valid config object`() {
        val databaseConfig = DatabaseConfig(
            connectionString = "jdbc:test:connection",
            username = "testuser",
            password = "testpass"
        )

        val validatorConfig = ValidatorConfig(
            intervalMinutes = 10,
            dailyCompletenessThreshold = 18,
            batchSize = 500
        )

        val appConfig = AppConfig(
            database = databaseConfig,
            validator = validatorConfig
        )

        assertEquals(databaseConfig, appConfig.database)
        assertEquals(validatorConfig, appConfig.validator)
    }
}