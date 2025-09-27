package cz.savic.weatherevaluator.forecastevaluator.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidatorConfigTest {

    @Test
    fun `should have reasonable default values`() {
        val config = ValidatorConfig()

        assertEquals(15, config.intervalMinutes)
        assertEquals(22, config.dailyCompletenessThreshold)
        assertEquals(1000, config.batchSize)
    }

    @Test
    fun `should allow custom values`() {
        val config = ValidatorConfig(
            intervalMinutes = 30,
            dailyCompletenessThreshold = 20,
            batchSize = 500
        )

        assertEquals(30, config.intervalMinutes)
        assertEquals(20, config.dailyCompletenessThreshold)
        assertEquals(500, config.batchSize)
    }

    @Test
    fun `should validate reasonable threshold values`() {
        val config = ValidatorConfig(dailyCompletenessThreshold = 22)

        assertTrue(config.dailyCompletenessThreshold <= 24, "Threshold should not exceed 24 hours")
        assertTrue(config.dailyCompletenessThreshold > 0, "Threshold should be positive")
    }

    @Test
    fun `should validate reasonable interval values`() {
        val config = ValidatorConfig(intervalMinutes = 15)

        assertTrue(config.intervalMinutes > 0, "Interval should be positive")
        assertTrue(config.intervalMinutes <= 1440, "Interval should not exceed 24 hours (1440 minutes)")
    }

    @Test
    fun `should validate reasonable batch size`() {
        val config = ValidatorConfig(batchSize = 1000)

        assertTrue(config.batchSize > 0, "Batch size should be positive")
        assertTrue(config.batchSize <= 10000, "Batch size should be reasonable (≤10000)")
    }

    @Test
    fun `should create copy with different values`() {
        val original = ValidatorConfig()
        val modified = original.copy(intervalMinutes = 30)

        assertEquals(15, original.intervalMinutes)
        assertEquals(30, modified.intervalMinutes)
        assertEquals(original.dailyCompletenessThreshold, modified.dailyCompletenessThreshold)
        assertEquals(original.batchSize, modified.batchSize)
    }
}