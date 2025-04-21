package cz.savic.weatherevaluator.forecastfetcher.adapter

import cz.savic.weatherevaluator.common.model.ForecastGranularity
import cz.savic.weatherevaluator.forecastfetcher.model.Location
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherApiAdapter (
    private val client: HttpClient,
    private val apiKey: String
) : ForecastProvider {

    companion object {
        private const val ADAPTER_SOURCE_NAME = "weather-api"
        private const val BASE_URL = "https://api.weatherapi.com/v1/forecast.json"
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    override fun supportedGranularities(): Set<ForecastGranularity> =
        setOf(ForecastGranularity.HOURLY, ForecastGranularity.DAILY)

    override suspend fun fetch(location: Location): List<ForecastResult> {
        val response: WeatherApiResponse = client.get(BASE_URL) {
            parameter("key", apiKey)
            parameter("q", "${location.latitude},${location.longitude}")
            parameter("days", 14)
            parameter("aqi", "no")
            parameter("alerts", "no")
        }.body()

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
    }
}