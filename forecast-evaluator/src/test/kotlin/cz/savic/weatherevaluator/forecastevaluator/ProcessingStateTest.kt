package cz.savic.weatherevaluator.forecastevaluator

import cz.savic.weatherevaluator.common.model.ProcessingState
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessingStateTest {

    @Test
    fun `should have all expected states`() {
        val expectedStates = setOf("STORED", "READY_FOR_PROCESSING", "ACCURACY_PROCESSED", "INCOMPLETE")
        val actualStates = ProcessingState.entries.map { it.name }.toSet()

        assertEquals(expectedStates, actualStates)
    }

    @Test
    fun `should serialize and deserialize correctly`() {
        ProcessingState.entries.forEach { state ->
            val serialized = Json.encodeToString(ProcessingState.serializer(), state)
            val deserialized = Json.decodeFromString(ProcessingState.serializer(), serialized)

            assertEquals(state, deserialized)
        }
    }

    @Test
    fun `should serialize to string name`() {
        assertEquals("\"STORED\"", Json.encodeToString(ProcessingState.serializer(), ProcessingState.STORED))
        assertEquals("\"READY_FOR_PROCESSING\"", Json.encodeToString(ProcessingState.serializer(), ProcessingState.READY_FOR_PROCESSING))
        assertEquals("\"ACCURACY_PROCESSED\"", Json.encodeToString(ProcessingState.serializer(), ProcessingState.ACCURACY_PROCESSED))
        assertEquals("\"INCOMPLETE\"", Json.encodeToString(ProcessingState.serializer(), ProcessingState.INCOMPLETE))
    }

    @Test
    fun `should have proper lifecycle order`() {
        val states = ProcessingState.entries

        assertTrue(states.contains(ProcessingState.STORED))
        assertTrue(states.contains(ProcessingState.READY_FOR_PROCESSING))
        assertTrue(states.contains(ProcessingState.ACCURACY_PROCESSED))
        assertTrue(states.contains(ProcessingState.INCOMPLETE))
    }

    @Test
    fun `should convert to string name correctly`() {
        assertEquals("STORED", ProcessingState.STORED.name)
        assertEquals("READY_FOR_PROCESSING", ProcessingState.READY_FOR_PROCESSING.name)
        assertEquals("ACCURACY_PROCESSED", ProcessingState.ACCURACY_PROCESSED.name)
        assertEquals("INCOMPLETE", ProcessingState.INCOMPLETE.name)
    }
}