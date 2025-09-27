package cz.savic.weatherevaluator.common.model

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime

/**
 * Represents the time horizon between when a forecast was made and its target time.
 * Used for categorizing forecast accuracy by lead time.
 */
@Serializable
enum class ForecastHorizon(val displayName: String, val maxHours: Long) {
    H1("1 Hour", 1),
    H2("2 Hours", 2),
    H3("3 Hours", 3),
    H6("6 Hours", 6),
    H12("12 Hours", 12),
    D1("1 Day", 24),
    D2("2 Days", 48),
    D3("3 Days", 72),
    D7("1 Week", 168),
    D14("2 Weeks", 336);

    companion object {
        /**
         * Determines the appropriate forecast horizon based on the time difference
         * between forecast creation and target time.
         */
        fun fromDuration(forecastTime: LocalDateTime, targetTime: LocalDateTime): ForecastHorizon {
            val hours = Duration.between(forecastTime, targetTime).toHours()

            return when {
                hours <= 1 -> H1
                hours <= 2 -> H2
                hours <= 3 -> H3
                hours <= 6 -> H6
                hours <= 12 -> H12
                hours <= 24 -> D1
                hours <= 48 -> D2
                hours <= 72 -> D3
                hours <= 168 -> D7
                else -> D14
            }
        }

        /**
         * Get all horizons up to a maximum duration (useful for filtering).
         */
        fun upToMaxHours(maxHours: Long): List<ForecastHorizon> {
            return entries.filter { it.maxHours <= maxHours }
        }
    }
}