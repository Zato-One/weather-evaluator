package cz.savic.weatherevaluator.forecastfetcher.adapter

import cz.savic.weatherevaluator.common.model.ForecastGranularity
import cz.savic.weatherevaluator.common.model.Location
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherApiAdapter (
    private val client: HttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.weatherapi.com"
) : ForecastProvider {

    private val logger = KotlinLogging.logger {}
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val ADAPTER_SOURCE_NAME = "weather-api"
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    override fun supportedGranularities(): Set<ForecastGranularity> =
        setOf(ForecastGranularity.HOURLY, ForecastGranularity.DAILY)

    override suspend fun fetch(location: Location): List<ForecastResult> {
        try {
            val httpResponse = client.get("$baseUrl/v1/forecast.json") {
                parameter("key", apiKey)
                parameter("q", "${location.latitude},${location.longitude}")
                parameter("days", 14)
                parameter("aqi", "no")
                parameter("alerts", "no")
            }

            if (!httpResponse.status.isSuccess()) {
                handleErrorResponse(httpResponse)
                return emptyList()
            }

            val response = httpResponse.body<WeatherApiResponse>()
            val forecastTime = LocalDateTime.now()

            val dailyResults = response.forecast.forecastday.map { day ->
                DailyForecastResult(
                    source = ADAPTER_SOURCE_NAME,
                    location = location,
                    forecastTimeUtc = forecastTime,
                    targetDate = LocalDate.parse(day.date),
                    temperatureMinC = day.day.mintempC,
                    temperatureMaxC = day.day.maxtempC,
                    temperatureMeanC = day.day.avgtempC,
                    precipitationMmSum = day.day.totalPrecipMm,
                    windSpeedKph10mMax = day.day.maxWindKph
                )
            }

            val hourlyResults = response.forecast.forecastday.flatMap { day ->
                day.hour.map { hour ->
                    HourlyForecastResult(
                        source = ADAPTER_SOURCE_NAME,
                        location = location,
                        forecastTimeUtc = forecastTime,
                        targetDateTimeUtc = LocalDateTime.parse(hour.time, TIME_FORMATTER),
                        temperatureC = hour.tempC,
                        precipitationMm = hour.precipMm,
                        windSpeedKph10m = hour.windKph
                    )
                }
            }

            return dailyResults + hourlyResults
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch weather data from WeatherAPI for location ${location.name}" }
            return emptyList()
        }
    }
    
    private suspend fun handleErrorResponse(response: HttpResponse) {
        try {
            val errorBody = response.bodyAsText()
            try {
                val errorResponse = json.decodeFromString<WeatherApiErrorResponse>(errorBody)
                logger.error { "WeatherAPI error: Code ${errorResponse.error.code} - ${errorResponse.error.message} (HTTP ${response.status.value})" }
            } catch (_: SerializationException) {
                logger.error { "Failed to parse WeatherAPI error response: $errorBody" }
            }
        } catch (e: Exception) {
            logger.error { "Error handling WeatherAPI failure: ${e.message}" }
        }
    }
}