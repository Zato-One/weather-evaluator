package cz.savic.weatherevaluator.actualweatherwriter.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConfigLoaderTest {

    @Test
    fun `should load default configuration`() {
        val config = loadConfig()

        assertNotNull(config)
        assertEquals("actual-weather-writer", config.kafka.groupId)
        assertEquals(listOf("weather.observed"), config.kafka.topics)
        assertEquals("weather_evaluator", config.database.username)
    }
}