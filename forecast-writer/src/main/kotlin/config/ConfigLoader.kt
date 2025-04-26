package cz.savic.weatherevaluator.forecastwriter.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun loadConfig(): AppConfig {
    logger.info { "Loading application configuration" }
    
    val config = ConfigLoaderBuilder.default()
        .build()
        .loadConfigOrThrow<AppConfig>("/application.conf")

    logger.info { "Configuration loaded successfully: $config" }
    
    return config
}