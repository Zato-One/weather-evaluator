package cz.savic.weatherevaluator.common.model

import kotlinx.serialization.Serializable

/**
 * Processing state for weather data records to track their lifecycle in forecast accuracy evaluation.
 *
 * This enum represents the processing pipeline stages:
 * 1. Data is fetched from APIs and stored → STORED
 * 2. Data is validated for completeness → READY_FOR_PROCESSING or INCOMPLETE
 * 3. Accuracy is calculated and stored → ACCURACY_PROCESSED
 */
@Serializable
enum class ProcessingState {

    /**
     * Data has been fetched from weather APIs and stored in database.
     * Next step: validation by forecast-evaluator service.
     *
     * Used for:
     * - forecast_daily records (default state)
     * - Any record that needs validation before processing
     */
    STORED,

    /**
     * Data has been validated and is ready for accuracy evaluation.
     *
     * Used for:
     * - forecast_hourly records (default state - no validation needed)
     * - actual_weather_observations records (default state - no validation needed)
     * - forecast_daily records that have sufficient actual weather data (≥22/24 hours)
     */
    READY_FOR_PROCESSING,

    /**
     * Accuracy evaluation has been completed and results stored in accuracy tables.
     * This is the final state for successfully processed records.
     *
     * Future use: Records that have been processed by accuracy calculation engine.
     */
    ACCURACY_PROCESSED,

    /**
     * Data is incomplete and cannot be processed for accuracy evaluation.
     *
     * Used for:
     * - forecast_daily records that don't have enough actual weather observations
     *   (less than configured threshold, default 22/24 hours)
     * - Records with missing or corrupted data
     */
    INCOMPLETE
}