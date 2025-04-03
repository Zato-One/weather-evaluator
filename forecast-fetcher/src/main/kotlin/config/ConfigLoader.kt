package cz.savic.weatherevaluator.forecastfetcher.config

import com.sksamuel.hoplite.ConfigLoader

fun loadConfig(): AppConfig {
    return ConfigLoader().loadConfigOrThrow<AppConfig>("/application.conf")
}