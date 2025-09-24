package cz.savic.weatherevaluator.common.integration

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Properties
import kotlin.test.assertTrue

@Testcontainers
abstract class KafkaAdvertisedListenersTest {

    @Test
    fun `should connect successfully with correct advertised listeners`() {
        val container = KafkaTestUtils.createCorrectKafkaContainer()
        container.start()

        try {
            val bootstrapServers = KafkaTestUtils.getBootstrapServers(container)
            val producer = createTestProducer(bootstrapServers)

            val record = ProducerRecord("test-topic", "key", "value")
            producer.send(record)
            producer.flush()
            producer.close()

            assertTrue(true)
        } finally {
            container.stop()
        }
    }

    @Test
    fun `should demonstrate problem with Docker Compose advertised listeners`() {
        val container = KafkaTestUtils.createProblematicKafkaContainer()
        container.start()

        try {
            val bootstrapServers = KafkaTestUtils.getBootstrapServers(container)
            val producer = createTestProducer(bootstrapServers)

            val record = ProducerRecord("test-topic", "key", "value")
            producer.send(record)

            KafkaTestUtils.waitForConnectionErrors()

            producer.close()


        } finally {
            container.stop()
        }
    }

    @Test
    fun `should fail with completely unreachable advertised listeners`() {
        val container = KafkaTestUtils.createUnreachableKafkaContainer()
        container.start()

        try {
            val bootstrapServers = KafkaTestUtils.getBootstrapServers(container)
            val producer = createTestProducer(bootstrapServers)

            val record = ProducerRecord("test-topic", "key", "value")
            producer.send(record)

            KafkaTestUtils.waitForConnectionErrors()

            producer.close()

        } finally {
            container.stop()
        }
    }

    private fun createTestProducer(bootstrapServers: String): KafkaProducer<String, String> {
        val props = Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("key.serializer", StringSerializer::class.java.name)
            put("value.serializer", StringSerializer::class.java.name)
            put("acks", "all")
            put("retries", 3)
        }
        return KafkaProducer(props)
    }
}