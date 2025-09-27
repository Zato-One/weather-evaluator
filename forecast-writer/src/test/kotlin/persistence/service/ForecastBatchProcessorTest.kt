package cz.savic.weatherevaluator.forecastwriter.persistence.service

import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.common.model.Location
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ForecastBatchProcessorTest {

    @Test
    fun `should process batch when batch size is reached`() {
        val processor = mockk<(List<ForecastFetchedEvent>) -> Unit>(relaxed = true)
        val batchProcessor = ForecastBatchProcessor(batchSize = 2, processor = processor)

        val event1 = createDailyEvent("event1")
        val event2 = createDailyEvent("event2")

        batchProcessor.submit(event1)
        batchProcessor.submit(event2)

        verify { processor.invoke(match { it.size == 2 && it.contains(event1) && it.contains(event2) }) }
    }

    @Test
    fun `should not process batch when batch size is not reached`() {
        val processor = mockk<(List<ForecastFetchedEvent>) -> Unit>(relaxed = true)
        val batchProcessor = ForecastBatchProcessor(batchSize = 3, processor = processor)

        val event1 = createDailyEvent("event1")
        val event2 = createDailyEvent("event2")

        batchProcessor.submit(event1)
        batchProcessor.submit(event2)

        verify(exactly = 0) { processor.invoke(any()) }
    }

    @Test
    fun `should force batch processing when explicitly called`() {
        val processor = mockk<(List<ForecastFetchedEvent>) -> Unit>(relaxed = true)
        val batchProcessor = ForecastBatchProcessor(batchSize = 10, processor = processor)

        val event1 = createDailyEvent("event1")
        val event2 = createHourlyEvent("event2")

        batchProcessor.submit(event1)
        batchProcessor.submit(event2)
        batchProcessor.forceBatchProcessing()

        verify { processor.invoke(match { it.size == 2 && it.contains(event1) && it.contains(event2) }) }
    }

    @Test
    fun `should handle empty batch on force processing`() {
        val processor = mockk<(List<ForecastFetchedEvent>) -> Unit>(relaxed = true)
        val batchProcessor = ForecastBatchProcessor(processor = processor)

        batchProcessor.forceBatchProcessing()

        verify(exactly = 0) { processor.invoke(any()) }
    }

    @Test
    fun `should handle mixed event types in batch`() {
        val processor = mockk<(List<ForecastFetchedEvent>) -> Unit>(relaxed = true)
        val batchProcessor = ForecastBatchProcessor(batchSize = 3, processor = processor)

        val dailyEvent = createDailyEvent("daily")
        val hourlyEvent1 = createHourlyEvent("hourly1")
        val hourlyEvent2 = createHourlyEvent("hourly2")

        batchProcessor.submit(dailyEvent)
        batchProcessor.submit(hourlyEvent1)
        batchProcessor.submit(hourlyEvent2)

        verify {
            processor.invoke(match { batch ->
                batch.size == 3 &&
                batch.filterIsInstance<DailyForecastFetchedEvent>().size == 1 &&
                batch.filterIsInstance<HourlyForecastFetchedEvent>().size == 2
            })
        }
    }

    @Test
    fun `should handle processor exception gracefully`() {
        val processor = mockk<(List<ForecastFetchedEvent>) -> Unit>()
        val batchProcessor = ForecastBatchProcessor(batchSize = 1, processor = processor)

        val event = createDailyEvent("test")

        // Configure processor to throw exception
        io.mockk.every { processor.invoke(any()) } throws RuntimeException("Processing failed")

        // Should not throw exception
        batchProcessor.submit(event)

        verify { processor.invoke(any()) }
    }

    @Test
    fun `should process multiple separate batches`() {
        var processedBatches = 0
        val processor: (List<ForecastFetchedEvent>) -> Unit = {
            processedBatches++
        }
        val batchProcessor = ForecastBatchProcessor(batchSize = 2, processor = processor)

        // First batch
        batchProcessor.submit(createDailyEvent("event1"))
        batchProcessor.submit(createDailyEvent("event2"))

        // Second batch
        batchProcessor.submit(createDailyEvent("event3"))
        batchProcessor.submit(createDailyEvent("event4"))

        assertEquals(2, processedBatches)
    }

    @Test
    fun `should handle single event in force processing`() {
        val processor = mockk<(List<ForecastFetchedEvent>) -> Unit>(relaxed = true)
        val batchProcessor = ForecastBatchProcessor(batchSize = 10, processor = processor)

        val event = createDailyEvent("single")
        batchProcessor.submit(event)
        batchProcessor.forceBatchProcessing()

        verify { processor.invoke(match { it.size == 1 && it[0] == event }) }
    }

    private fun createDailyEvent(source: String = "TestSource") = DailyForecastFetchedEvent(
        source = source,
        location = Location("TestLocation", 50.0, 14.0),
        forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
        targetDate = LocalDate.of(2025, 1, 2),
        temperatureMinC = 10.0,
        temperatureMaxC = 20.0,
        temperatureMeanC = 15.0,
        precipitationMmSum = 5.0,
        windSpeedKph10mMax = 25.0
    )

    private fun createHourlyEvent(source: String = "TestSource") = HourlyForecastFetchedEvent(
        source = source,
        location = Location("TestLocation", 50.0, 14.0),
        forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
        targetDateTimeUtc = LocalDateTime.of(2025, 1, 2, 15, 0),
        temperatureC = 15.0,
        precipitationMm = 2.0,
        windSpeedKph10m = 20.0
    )
}