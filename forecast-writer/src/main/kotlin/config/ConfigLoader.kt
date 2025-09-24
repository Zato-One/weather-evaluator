package cz.savic.weatherevaluator.forecastwriter.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addEnvironmentSource
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun loadConfig(): AppConfig {
    logger.info { "Loading application configuration" }

    val config = ConfigLoaderBuilder.default()
        .addEnvironmentSource()
        .build()
        .loadConfigOrThrow<AppConfig>()

    logger.info { "Configuration loaded successfully: $config" }
    return config
}