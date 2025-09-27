package cz.savic.weatherevaluator.testdatagenerator.generator

import cz.savic.weatherevaluator.testdatagenerator.config.DataGenerationConfig
import cz.savic.weatherevaluator.testdatagenerator.config.DatabaseConfig
import cz.savic.weatherevaluator.testdatagenerator.model.WeatherPattern
import cz.savic.weatherevaluator.testdatagenerator.persistence.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class TestDataGenerator(
    private val databaseConfig: DatabaseConfig,
    private val generationConfig: DataGenerationConfig
) {

    private val weatherPattern = WeatherPattern()
    private val writer = TestDataWriter(databaseConfig)
    private val random = Random.Default

    fun generateAllData() {
        logger.info { "Starting test data generation for ${generationConfig.daysBack} days" }
        logger.info { "Date range: ${generationConfig.startDate} to ${generationConfig.endDate}" }
        logger.info { "Locations: ${generationConfig.locations.map { it.name }}" }
        logger.info { "Sources: ${generationConfig.sources}" }

        // Generate actual weather observations for all days
        generateActualWeatherObservations()

        // Generate forecasts for all combinations
        generateForecasts()

        logger.info { "Test data generation completed successfully" }
    }

    private fun generateActualWeatherObservations() {
        logger.info { "Generating actual weather observations..." }

        val observations = mutableListOf<ActualWeatherRecord>()

        generationConfig.locations.forEach { locationConfig ->
            val location = locationConfig.toLocation()

            // Generate for each day and hour
            var currentDate = generationConfig.startDate
            while (!currentDate.isAfter(generationConfig.endDate)) {

                // Decide if this day should be incomplete (missing some hours)
                val isIncompleteDay = random.nextDouble() < generationConfig.incompleteDaysRatio
                val hoursToSkip = if (isIncompleteDay) {
                    // Skip 5-10 random hours to make day incomplete
                    (5..10).random(random)
                } else 0

                val skippedHours = if (hoursToSkip > 0) {
                    (0..23).shuffled(random).take(hoursToSkip).toSet()
                } else emptySet()

                for (hour in 0..23) {
                    if (hour in skippedHours) {
                        continue // Skip this hour for incomplete day
                    }

                    val dateTime = currentDate.atTime(hour, 0)
                    val weather = weatherPattern.generateActualWeather(dateTime)

                    // Use first source for actual observations (weather-api)
                    observations.add(
                        ActualWeatherRecord(
                            location = location,
                            dateTime = dateTime,
                            weather = weather,
                            source = generationConfig.sources.first()
                        )
                    )
                }

                currentDate = currentDate.plusDays(1)
            }
        }

        logger.info { "Generated ${observations.size} actual weather observations" }
        writer.writeBatchActualWeather(observations)
    }

    private fun generateForecasts() {
        logger.info { "Generating forecasts..." }

        val hourlyForecasts = mutableListOf<HourlyForecastRecord>()
        val dailyForecasts = mutableListOf<DailyForecastRecord>()

        generationConfig.locations.forEach { locationConfig ->
            val location = locationConfig.toLocation()

            generationConfig.sources.forEach { source ->

                // Generate forecasts for each day
                var targetDate = generationConfig.startDate
                while (!targetDate.isAfter(generationConfig.endDate)) {

                    // Generate forecasts with different lead times (1 to 14 days back)
                    for (leadDays in 1..14) {
                        val forecastDate = targetDate.minusDays(leadDays.toLong())

                        // Only generate forecasts that would have been made during our date range or before
                        if (forecastDate.isBefore(generationConfig.startDate.minusDays(14))) {
                            continue
                        }

                        val forecastTime = forecastDate.atTime(12, 0) // Assume forecasts made at noon

                        // Generate hourly forecasts for the target day
                        generateHourlyForecastsForDay(
                            location, source, forecastTime, targetDate, hourlyForecasts
                        )

                        // Generate daily forecast for the target day
                        generateDailyForecastForDay(
                            location, source, forecastTime, targetDate, dailyForecasts
                        )
                    }

                    targetDate = targetDate.plusDays(1)
                }
            }
        }

        logger.info { "Generated ${hourlyForecasts.size} hourly forecasts" }
        writer.writeBatchHourlyForecasts(hourlyForecasts)

        logger.info { "Generated ${dailyForecasts.size} daily forecasts" }
        writer.writeBatchDailyForecasts(dailyForecasts)
    }

    private fun generateHourlyForecastsForDay(
        location: cz.savic.weatherevaluator.common.model.Location,
        source: String,
        forecastTime: LocalDateTime,
        targetDate: LocalDate,
        hourlyForecasts: MutableList<HourlyForecastRecord>
    ) {
        for (hour in 0..23) {
            val targetDateTime = targetDate.atTime(hour, 0)

            val weather = weatherPattern.generateForecast(
                targetDateTime = targetDateTime,
                forecastDateTime = forecastTime,
                baseAccuracy = generationConfig.baseAccuracy,
                accuracyDecayPerDay = generationConfig.accuracyDecayPerDay
            )

            hourlyForecasts.add(
                HourlyForecastRecord(
                    location = location,
                    forecastTime = forecastTime,
                    targetTime = targetDateTime,
                    weather = weather,
                    source = source
                )
            )
        }
    }

    private fun generateDailyForecastForDay(
        location: cz.savic.weatherevaluator.common.model.Location,
        source: String,
        forecastTime: LocalDateTime,
        targetDate: LocalDate,
        dailyForecasts: MutableList<DailyForecastRecord>
    ) {
        // Generate hourly data for the day to aggregate into daily values
        val hourlyWeatherForDay = (0..23).map { hour ->
            val targetDateTime = targetDate.atTime(hour, 0)
            weatherPattern.generateForecast(
                targetDateTime = targetDateTime,
                forecastDateTime = forecastTime,
                baseAccuracy = generationConfig.baseAccuracy,
                accuracyDecayPerDay = generationConfig.accuracyDecayPerDay
            )
        }

        val dailyWeather = weatherPattern.generateDailyAggregates(hourlyWeatherForDay)

        dailyForecasts.add(
            DailyForecastRecord(
                location = location,
                forecastTime = forecastTime,
                targetDate = targetDate,
                dailyWeather = dailyWeather,
                source = source
            )
        )
    }
}