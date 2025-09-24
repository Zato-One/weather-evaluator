package cz.savic.weatherevaluator.actualweatherfetcher.adapter

import cz.savic.weatherevaluator.common.model.Location
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OpenMeteoCurrentAdapter(
    private val client: HttpClient,
    private val baseUrl: String = "https://api.open-meteo.com"
) : ActualWeatherProvider {

    private val logger = KotlinLogging.logger {}
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val ADAPTER_SOURCE_NAME = "open-meteo"
        private const val CURRENT_PARAMS = "temperature_2m,precipitation,wind_speed_10m"
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }

    override suspend fun fetchCurrent(location: Location): List<ActualWeatherResult> {
        try {
            val httpResponse = client.get("$baseUrl/v1/forecast") {
                parameter("latitude", location.latitude)
                parameter("longitude", location.longitude)
                parameter("current", CURRENT_PARAMS)
                parameter("timezone", "auto")
            }

            if (!httpResponse.status.isSuccess()) {
                handleErrorResponse(httpResponse)
                return emptyList()
            }

            val response = httpResponse.body<OpenMeteoCurrentResponse>()
            val observedTime = LocalDateTime.parse(response.current.time, TIME_FORMATTER)

            val currentWeatherResult = CurrentWeatherResult(
                source = ADAPTER_SOURCE_NAME,
                location = location,
                observedTimeUtc = observedTime,
                temperatureC = response.current.temperature,
                precipitationMm = response.current.precipitation,
                windSpeedKph10m = response.current.windSpeed
            )

            return listOf(currentWeatherResult)
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch current weather data from OpenMeteo for location ${location.name}" }
            return emptyList()
        }
    }

    private suspend fun handleErrorResponse(response: HttpResponse) {
        try {
            val errorBody = response.bodyAsText()
            try {
                val errorResponse = json.decodeFromString<OpenMeteoCurrentErrorResponse>(errorBody)
                logger.error { "OpenMeteo API error: ${errorResponse.reason} (HTTP ${response.status.value})" }
            } catch (_: SerializationException) {
                logger.error { "Failed to parse OpenMeteo error response: $errorBody" }
            }
        } catch (e: Exception) {
            logger.error { "Error handling OpenMeteo API failure: ${e.message}" }
        }
    }
}