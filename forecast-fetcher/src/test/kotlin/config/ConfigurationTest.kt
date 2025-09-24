package cz.savic.weatherevaluator.forecastfetcher.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigurationTest {

    @OptIn(ExperimentalHoplite::class)
    @Test
    fun `should load configuration from application conf`() {
        val config = ConfigLoaderBuilder.default()
            .withExplicitSealedTypes()
            .build()
            .loadConfigOrThrow<AppConfig>("/test-application.conf")

        assertEquals("localhost:9092", config.kafka.bootstrapServers)
        assertEquals("test.topic", config.kafka.topic)

        assertEquals(2, config.locations.size)

        val firstLocation = config.locations[0]
        assertEquals("TestCity", firstLocation.name)
        assertEquals(50.0, firstLocation.latitude)
        assertEquals(14.0, firstLocation.longitude)

        val secondLocation = config.locations[1]
        assertEquals("AnotherTestCity", secondLocation.name)
        assertEquals(49.0, secondLocation.latitude)
        assertEquals(16.0, secondLocation.longitude)
    }

    @Test
    fun `should handle missing secret properties gracefully`() {
        val apiKey = SecretLoader.getWeatherApiKey()

        assertTrue(apiKey.isEmpty())
    }
}