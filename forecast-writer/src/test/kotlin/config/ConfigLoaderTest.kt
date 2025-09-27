package cz.savic.weatherevaluator.forecastwriter.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConfigLoaderTest {

    @Test
    fun `should load default configuration`() {
        val config = loadConfig()

        assertNotNull(config)
        assertEquals("forecast-writer", config.kafka.groupId)
        assertEquals(listOf("forecast.fetched"), config.kafka.topics)
        assertEquals("weather_evaluator", config.database.username)
        assertEquals("weather_evaluator", config.database.password)
        assertEquals(100, config.kafka.pollTimeoutMs)
    }
}