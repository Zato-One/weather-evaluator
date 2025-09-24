package cz.savic.weatherevaluator.actualweatherfetcher

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class ActualWeatherFetcherRunner : AutoCloseable {

    suspend fun fetchAllOnce() {
        logger.info { "Starting actual weather fetcher..." }

        // TODO: Implement in Step 2-4
        // 1. Load configuration
        // 2. Initialize HTTP client
        // 3. Create weather adapters
        // 4. Setup Kafka producer
        // 5. Fetch current weather for all locations
        // 6. Publish to Kafka

        logger.info { "Actual weather fetcher completed" }
    }

    override fun close() {
        logger.info { "Closing actual weather fetcher resources..." }
        // TODO: Close HTTP client and Kafka producer
    }
}