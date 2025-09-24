package cz.savic.weatherevaluator.actualweatherfetcher.kafka

import cz.savic.weatherevaluator.actualweatherfetcher.config.KafkaConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

fun createKafkaProducer(config: KafkaConfig): Producer<String, String> {
    val props = Properties().apply {
        put("bootstrap.servers", config.bootstrapServers)
        put("key.serializer", StringSerializer::class.java.name)
        put("value.serializer", StringSerializer::class.java.name)

        // Wait for all replicas to acknowledge the message.
        put("acks", "all")

        // Retry sending the message up to 3 times in case of failures.
        put("retries", 3)
    }

    return KafkaProducer(props)
}