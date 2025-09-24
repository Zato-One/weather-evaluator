package cz.savic.weatherevaluator.actualweatherfetcher.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalHoplite::class)
fun loadConfig(): AppConfig {
    logger.info { "Loading application configuration" }

    val config = ConfigLoaderBuilder.default()
        .withExplicitSealedTypes()
        .build()
        .loadConfigOrThrow<AppConfig>("/application.conf")

    logger.info { "Configuration loaded successfully: $config" }

    return config
}