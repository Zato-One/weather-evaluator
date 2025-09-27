package cz.savic.weatherevaluator.forecastevaluator.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseConfigTest {

    @Test
    fun `should have reasonable default connection string`() {
        val config = DatabaseConfig(username = "test", password = "test")

        assertEquals("jdbc:oracle:thin:@localhost:1521:XEPDB1", config.connectionString)
        assertTrue(config.connectionString.startsWith("jdbc:oracle:thin:"))
    }

    @Test
    fun `should allow custom connection string`() {
        val customConnectionString = "jdbc:oracle:thin:@remote-host:1521/XEPDB1"
        val config = DatabaseConfig(
            connectionString = customConnectionString,
            username = "user",
            password = "pass"
        )

        assertEquals(customConnectionString, config.connectionString)
        assertEquals("user", config.username)
        assertEquals("pass", config.password)
    }

    @Test
    fun `should create copy with different values`() {
        val original = DatabaseConfig(username = "original", password = "original")
        val modified = original.copy(username = "modified")

        assertEquals("original", original.username)
        assertEquals("modified", modified.username)
        assertEquals(original.password, modified.password)
        assertEquals(original.connectionString, modified.connectionString)
    }

    @Test
    fun `should handle Oracle JDBC URL format`() {
        val config = DatabaseConfig(username = "test", password = "test")

        assertTrue(config.connectionString.contains("oracle"))
        assertTrue(config.connectionString.contains("1521"))
        assertTrue(config.connectionString.contains("XEPDB1"))
    }
}