plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "weather-evaluator"
include("common")
include("forecast-fetcher")
include("forecast-writer")
include("actual-weather-fetcher")
include("actual-weather-writer")
