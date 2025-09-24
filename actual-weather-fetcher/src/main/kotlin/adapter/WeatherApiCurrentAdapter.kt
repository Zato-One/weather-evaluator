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

class WeatherApiCurrentAdapter(
    private val client: HttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.weatherapi.com"
) : ActualWeatherProvider {

    private val logger = KotlinLogging.logger {}
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val ADAPTER_SOURCE_NAME = "weather-api"
    }

    override suspend fun fetchCurrent(location: Location): List<ActualWeatherResult> {
        try {
            val httpResponse = client.get("$baseUrl/v1/current.json") {
                parameter("key", apiKey)
                parameter("q", "${location.latitude},${location.longitude}")
                parameter("aqi", "no")
            }

            if (!httpResponse.status.isSuccess()) {
                handleErrorResponse(httpResponse)
                return emptyList()
            }

            val response = httpResponse.body<WeatherApiCurrentResponse>()
            val observedTime = LocalDateTime.now()

            val currentWeatherResult = CurrentWeatherResult(
                source = ADAPTER_SOURCE_NAME,
                location = location,
                observedTimeUtc = observedTime,
                temperatureC = response.current.tempC,
                precipitationMm = response.current.precipMm,
                windSpeedKph10m = response.current.windKph
            )

            return listOf(currentWeatherResult)
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch current weather data from WeatherAPI for location ${location.name}" }
            return emptyList()
        }
    }

    private suspend fun handleErrorResponse(response: HttpResponse) {
        try {
            val errorBody = response.bodyAsText()
            try {
                val errorResponse = json.decodeFromString<WeatherApiCurrentErrorResponse>(errorBody)
                logger.error { "WeatherAPI error: Code ${errorResponse.error.code} - ${errorResponse.error.message} (HTTP ${response.status.value})" }
            } catch (_: SerializationException) {
                logger.error { "Failed to parse WeatherAPI error response: $errorBody" }
            }
        } catch (e: Exception) {
            logger.error { "Error handling WeatherAPI failure: ${e.message}" }
        }
    }
}