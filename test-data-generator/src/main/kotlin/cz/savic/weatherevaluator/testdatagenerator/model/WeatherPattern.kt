package cz.savic.weatherevaluator.testdatagenerator.model

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.*
import kotlin.random.Random

data class WeatherData(
    val temperature: Double,
    val precipitation: Double,
    val windSpeed: Double
)

class WeatherPattern(
    private val baseTemperature: Double = 15.0,
    private val random: Random = Random.Default
) {

    fun generateActualWeather(dateTime: LocalDateTime): WeatherData {
        val dayOfYear = dateTime.dayOfYear
        val hourOfDay = dateTime.hour

        // Seasonal temperature variation (sinusoidal)
        val seasonalTemp = baseTemperature + 10 * sin(2 * PI * dayOfYear / 365.0)

        // Daily temperature variation (warmer during day)
        val dailyVariation = 5 * sin(2 * PI * (hourOfDay - 6) / 24.0)

        // Random noise
        val noise = random.nextDouble(-2.0, 2.0)

        val temperature = seasonalTemp + dailyVariation + noise

        // Precipitation (realistic clustering - either dry or rainy period)
        val precipitationProbability = if (random.nextDouble() < 0.3) 0.8 else 0.1
        val precipitation = if (random.nextDouble() < precipitationProbability) {
            random.nextDouble() * 10.0 // 0-10mm
        } else {
            0.0
        }

        // Wind speed (correlated with weather activity)
        val baseWind = 5.0 + precipitation * 0.5
        val windSpeed = maxOf(0.0, baseWind + random.nextDouble(-2.0, 2.0))

        return WeatherData(
            temperature = round(temperature * 100) / 100, // 2 decimal places
            precipitation = round(precipitation * 100) / 100,
            windSpeed = round(windSpeed * 100) / 100
        )
    }

    fun generateForecast(
        targetDateTime: LocalDateTime,
        forecastDateTime: LocalDateTime,
        baseAccuracy: Double,
        accuracyDecayPerDay: Double
    ): WeatherData {
        // Generate "true" weather for the target time
        val actualWeather = generateActualWeather(targetDateTime)

        // Calculate accuracy based on forecast distance
        val hoursDifference = java.time.Duration.between(forecastDateTime, targetDateTime).toHours()
        val daysDifference = hoursDifference / 24.0
        val accuracy = maxOf(0.1, baseAccuracy - (daysDifference * accuracyDecayPerDay))

        // Apply forecast error
        val errorScale = (1 - accuracy)
        val tempError = random.nextDouble(-1.0, 1.0) * errorScale * 5.0
        val precipError = random.nextDouble(-1.0, 1.0) * errorScale * 2.0
        val windError = random.nextDouble(-1.0, 1.0) * errorScale * 3.0

        return WeatherData(
            temperature = round((actualWeather.temperature + tempError) * 100) / 100,
            precipitation = maxOf(0.0, round((actualWeather.precipitation + precipError) * 100) / 100),
            windSpeed = maxOf(0.0, round((actualWeather.windSpeed + windError) * 100) / 100)
        )
    }

    fun generateDailyAggregates(hourlyData: List<WeatherData>): DailyWeatherData {
        if (hourlyData.isEmpty()) {
            throw IllegalArgumentException("Cannot generate daily aggregates from empty hourly data")
        }

        val temperatures = hourlyData.map { it.temperature }
        val precipitations = hourlyData.map { it.precipitation }
        val windSpeeds = hourlyData.map { it.windSpeed }

        return DailyWeatherData(
            temperatureMin = temperatures.minOrNull() ?: 0.0,
            temperatureMax = temperatures.maxOrNull() ?: 0.0,
            temperatureMean = temperatures.average(),
            precipitationSum = precipitations.sum(),
            windSpeedMax = windSpeeds.maxOrNull() ?: 0.0
        )
    }
}

data class DailyWeatherData(
    val temperatureMin: Double,
    val temperatureMax: Double,
    val temperatureMean: Double,
    val precipitationSum: Double,
    val windSpeedMax: Double
)