package cz.savic.weatherevaluator.forecastfetcher.config

import java.util.Properties
import java.io.FileInputStream
import java.io.FileNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

object SecretLoader {
    private val secrets = Properties()
    private const val SECRETS_FILE = "forecast-fetcher/src/main/resources/secrets.properties"
    
    init {
        try {
            FileInputStream(SECRETS_FILE).use { stream ->
                secrets.load(stream)
                logger.info { "Loaded secrets from $SECRETS_FILE" }
            }
        } catch (e: FileNotFoundException) {
            logger.warn { "Secrets file not found at $SECRETS_FILE. Make sure to create this file with your API keys." }
        } catch (e: Exception) {
            logger.error(e) { "Error loading secrets from $SECRETS_FILE" }
        }
    }
    
    fun getWeatherApiKey(): String {
        return secrets.getProperty("weather-api.key", "")
            .also { if (it.isEmpty()) logger.warn { "Weather API key not found in secrets file" } }
    }
}
