plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "weather-evaluator"
include("forecast-fetcher")
include("common")
