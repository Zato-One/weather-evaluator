package cz.savic.weatherevaluator.forecastfetcher.event

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

fun createKafkaProducer(bootstrapServers: String): Producer<String, String> {
    val props = Properties().apply {
        put("bootstrap.servers", bootstrapServers)
        put("key.serializer", StringSerializer::class.java.name)
        put("value.serializer", StringSerializer::class.java.name)
        put("acks", "all")
        put("retries", 3)
    }

    return KafkaProducer(props)
}