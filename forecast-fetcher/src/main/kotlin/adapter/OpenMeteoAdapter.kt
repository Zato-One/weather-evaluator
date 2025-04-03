package cz.savic.weatherevaluator.forecastfetcher.adapter

import cz.savic.weatherevaluator.forecastfetcher.model.Location
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


class OpenMeteoAdapter(private val client: HttpClient) : ForecastProvider {
    override suspend fun fetch(location: Location): ForecastResult {
        val response: OpenMeteoResponse = client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", location.latitude)
            parameter("longitude", location.longitude)
            parameter("current", "temperature_2m")
            parameter("timezone", "auto")
        }.body()

        return ForecastResult(
            source = "open-meteo",
            location = location.name,
            temperature = response.current.temperature2m,
            timeUtc = response.current.time
        )
    }
}

@Serializable
data class OpenMeteoResponse(
    val current: Current
)

@Serializable
data class Current(
    @SerialName("temperature_2m")
    val temperature2m: Double,
    val time: String
)