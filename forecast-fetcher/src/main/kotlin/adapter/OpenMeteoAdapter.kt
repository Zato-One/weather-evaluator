package cz.savic.weatherevaluator.forecastfetcher.adapter

import cz.savic.weatherevaluator.forecastfetcher.model.ForecastGranularity
import cz.savic.weatherevaluator.forecastfetcher.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.time.LocalDate
import java.time.LocalDateTime

class OpenMeteoAdapter(
    private val client: HttpClient
) : ForecastProvider {

    companion object {
        private const val ADAPTER_SOURCE_NAME = "open-meteo"
        private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
        private const val DAILY_PARAMS =
            "temperature_2m_min,temperature_2m_max,temperature_2m_mean,precipitation_sum,wind_speed_10m_max"
        private const val HOURLY_PARAMS =
            "temperature_2m,precipitation,wind_speed_10m"
    }

    override fun supportedGranularities(): Set<ForecastGranularity> =
        setOf(ForecastGranularity.HOURLY, ForecastGranularity.DAILY)

    override suspend fun fetch(location: Location): List<ForecastResult> {
        val response: OpenMeteoResponse = client.get(BASE_URL) {
            parameter("latitude", location.latitude)
            parameter("longitude", location.longitude)
            parameter("daily", DAILY_PARAMS)
            parameter("hourly", HOURLY_PARAMS)
            parameter("timezone", "auto")
            parameter("forecast_days", 16)
        }.body()

        val forecastTime = LocalDateTime.now()

        val dailyResults = response.daily.time.mapIndexed { index, dateStr ->
            DailyForecastResult(
                source = ADAPTER_SOURCE_NAME,
                location = location,
                forecastTimeUtc = forecastTime,
                targetDate = LocalDate.parse(dateStr),
                temperatureMinC = response.daily.temperatureMin[index],
                temperatureMaxC = response.daily.temperatureMax[index],
                temperatureMeanC = response.daily.temperatureMean[index],
                precipitationMmSum = response.daily.precipitationSum[index],
                windSpeedKph10mMax = response.daily.windSpeedMax[index]
            )
        }

        val hourlyResults = response.hourly.time.mapIndexed { index, datetimeStr ->
            HourlyForecastResult(
                source = ADAPTER_SOURCE_NAME,
                location = location,
                forecastTimeUtc = forecastTime,
                targetDateTimeUtc = LocalDateTime.parse(datetimeStr),
                temperatureC = response.hourly.temperature[index],
                precipitationMm = response.hourly.precipitation[index],
                windSpeedKph10m = response.hourly.windSpeed[index]
            )
        }

        return dailyResults + hourlyResults
    }
}
