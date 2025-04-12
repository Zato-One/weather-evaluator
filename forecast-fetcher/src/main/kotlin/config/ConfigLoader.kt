package cz.savic.weatherevaluator.forecastfetcher.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite

@OptIn(ExperimentalHoplite::class)
fun loadConfig(): AppConfig {
    return ConfigLoaderBuilder.default()
        .withExplicitSealedTypes()
        .build()
        .loadConfigOrThrow("/application.conf")
}