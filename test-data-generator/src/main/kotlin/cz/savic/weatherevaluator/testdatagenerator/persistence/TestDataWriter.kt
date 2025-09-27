package cz.savic.weatherevaluator.testdatagenerator.persistence

import cz.savic.weatherevaluator.common.model.Location
import cz.savic.weatherevaluator.common.model.ProcessingState
import cz.savic.weatherevaluator.testdatagenerator.config.DatabaseConfig
import cz.savic.weatherevaluator.testdatagenerator.model.DailyWeatherData
import cz.savic.weatherevaluator.testdatagenerator.model.WeatherData
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

class TestDataWriter(private val databaseConfig: DatabaseConfig) {

    fun writeActualWeatherObservations(
        location: Location,
        dateTime: LocalDateTime,
        weather: WeatherData,
        source: String
    ) {
        DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        ).use { connection ->

            val sql = """
                INSERT INTO actual_weather_observations
                (source, location_name, latitude, longitude, observed_time_utc,
                 temperature_c, precipitation_mm, wind_speed_kph_10m, processing_state)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, source)
                stmt.setString(2, location.name)
                stmt.setDouble(3, location.latitude)
                stmt.setDouble(4, location.longitude)
                stmt.setTimestamp(5, Timestamp.valueOf(dateTime))
                stmt.setDouble(6, weather.temperature)
                stmt.setDouble(7, weather.precipitation)
                stmt.setDouble(8, weather.windSpeed)
                stmt.setString(9, ProcessingState.READY_FOR_PROCESSING.name)

                stmt.executeUpdate()
            }
        }
    }

    fun writeHourlyForecast(
        location: Location,
        forecastTime: LocalDateTime,
        targetTime: LocalDateTime,
        weather: WeatherData,
        source: String
    ) {
        DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        ).use { connection ->

            val sql = """
                INSERT INTO forecast_hourly
                (source, location_name, latitude, longitude, forecast_time_utc, target_datetime_utc,
                 temperature_c, precipitation_mm, wind_speed_kph_10m, processing_state)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, source)
                stmt.setString(2, location.name)
                stmt.setDouble(3, location.latitude)
                stmt.setDouble(4, location.longitude)
                stmt.setTimestamp(5, Timestamp.valueOf(forecastTime))
                stmt.setTimestamp(6, Timestamp.valueOf(targetTime))
                stmt.setDouble(7, weather.temperature)
                stmt.setDouble(8, weather.precipitation)
                stmt.setDouble(9, weather.windSpeed)
                stmt.setString(10, ProcessingState.READY_FOR_PROCESSING.name)

                stmt.executeUpdate()
            }
        }
    }

    fun writeDailyForecast(
        location: Location,
        forecastTime: LocalDateTime,
        targetDate: LocalDate,
        dailyWeather: DailyWeatherData,
        source: String
    ) {
        DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        ).use { connection ->

            val sql = """
                INSERT INTO forecast_daily
                (source, location_name, latitude, longitude, forecast_time_utc, target_date,
                 temperature_min_c, temperature_max_c, temperature_mean_c,
                 precipitation_mm_sum, wind_speed_kph_10m_max, processing_state)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, source)
                stmt.setString(2, location.name)
                stmt.setDouble(3, location.latitude)
                stmt.setDouble(4, location.longitude)
                stmt.setTimestamp(5, Timestamp.valueOf(forecastTime))
                stmt.setDate(6, java.sql.Date.valueOf(targetDate))
                stmt.setDouble(7, dailyWeather.temperatureMin)
                stmt.setDouble(8, dailyWeather.temperatureMax)
                stmt.setDouble(9, dailyWeather.temperatureMean)
                stmt.setDouble(10, dailyWeather.precipitationSum)
                stmt.setDouble(11, dailyWeather.windSpeedMax)
                stmt.setString(12, ProcessingState.STORED.name)

                stmt.executeUpdate()
            }
        }
    }

    fun writeBatchActualWeather(observations: List<ActualWeatherRecord>) {
        if (observations.isEmpty()) return

        DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        ).use { connection ->
            writeBatchActualWeatherInternal(connection, observations)
        }
    }

    fun writeBatchHourlyForecasts(forecasts: List<HourlyForecastRecord>) {
        if (forecasts.isEmpty()) return

        DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        ).use { connection ->
            writeBatchHourlyForecastsInternal(connection, forecasts)
        }
    }

    fun writeBatchDailyForecasts(forecasts: List<DailyForecastRecord>) {
        if (forecasts.isEmpty()) return

        DriverManager.getConnection(
            databaseConfig.connectionString,
            databaseConfig.username,
            databaseConfig.password
        ).use { connection ->
            writeBatchDailyForecastsInternal(connection, forecasts)
        }
    }

    private fun writeBatchActualWeatherInternal(connection: Connection, observations: List<ActualWeatherRecord>) {
        val sql = """
            INSERT INTO actual_weather_observations
            (source, location_name, latitude, longitude, observed_time_utc,
             temperature_c, precipitation_mm, wind_speed_kph_10m, processing_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            observations.forEach { record ->
                stmt.setString(1, record.source)
                stmt.setString(2, record.location.name)
                stmt.setDouble(3, record.location.latitude)
                stmt.setDouble(4, record.location.longitude)
                stmt.setTimestamp(5, Timestamp.valueOf(record.dateTime))
                stmt.setDouble(6, record.weather.temperature)
                stmt.setDouble(7, record.weather.precipitation)
                stmt.setDouble(8, record.weather.windSpeed)
                stmt.setString(9, ProcessingState.READY_FOR_PROCESSING.name)
                stmt.addBatch()
            }
            val results = stmt.executeBatch()
            logger.info { "Inserted ${results.sum()} actual weather observations" }
        }
    }

    private fun writeBatchHourlyForecastsInternal(connection: Connection, forecasts: List<HourlyForecastRecord>) {
        val sql = """
            INSERT INTO forecast_hourly
            (source, location_name, latitude, longitude, forecast_time_utc, target_datetime_utc,
             temperature_c, precipitation_mm, wind_speed_kph_10m, processing_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            forecasts.forEach { record ->
                stmt.setString(1, record.source)
                stmt.setString(2, record.location.name)
                stmt.setDouble(3, record.location.latitude)
                stmt.setDouble(4, record.location.longitude)
                stmt.setTimestamp(5, Timestamp.valueOf(record.forecastTime))
                stmt.setTimestamp(6, Timestamp.valueOf(record.targetTime))
                stmt.setDouble(7, record.weather.temperature)
                stmt.setDouble(8, record.weather.precipitation)
                stmt.setDouble(9, record.weather.windSpeed)
                stmt.setString(10, ProcessingState.READY_FOR_PROCESSING.name)
                stmt.addBatch()
            }
            val results = stmt.executeBatch()
            logger.info { "Inserted ${results.sum()} hourly forecasts" }
        }
    }

    private fun writeBatchDailyForecastsInternal(connection: Connection, forecasts: List<DailyForecastRecord>) {
        val sql = """
            INSERT INTO forecast_daily
            (source, location_name, latitude, longitude, forecast_time_utc, target_date,
             temperature_min_c, temperature_max_c, temperature_mean_c,
             precipitation_mm_sum, wind_speed_kph_10m_max, processing_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            forecasts.forEach { record ->
                stmt.setString(1, record.source)
                stmt.setString(2, record.location.name)
                stmt.setDouble(3, record.location.latitude)
                stmt.setDouble(4, record.location.longitude)
                stmt.setTimestamp(5, Timestamp.valueOf(record.forecastTime))
                stmt.setDate(6, java.sql.Date.valueOf(record.targetDate))
                stmt.setDouble(7, record.dailyWeather.temperatureMin)
                stmt.setDouble(8, record.dailyWeather.temperatureMax)
                stmt.setDouble(9, record.dailyWeather.temperatureMean)
                stmt.setDouble(10, record.dailyWeather.precipitationSum)
                stmt.setDouble(11, record.dailyWeather.windSpeedMax)
                stmt.setString(12, ProcessingState.STORED.name)
                stmt.addBatch()
            }
            val results = stmt.executeBatch()
            logger.info { "Inserted ${results.sum()} daily forecasts" }
        }
    }
}

data class ActualWeatherRecord(
    val location: Location,
    val dateTime: LocalDateTime,
    val weather: WeatherData,
    val source: String
)

data class HourlyForecastRecord(
    val location: Location,
    val forecastTime: LocalDateTime,
    val targetTime: LocalDateTime,
    val weather: WeatherData,
    val source: String
)

data class DailyForecastRecord(
    val location: Location,
    val forecastTime: LocalDateTime,
    val targetDate: LocalDate,
    val dailyWeather: DailyWeatherData,
    val source: String
)