package cz.savic.weatherevaluator.forecastwriter.kafka

import cz.savic.weatherevaluator.forecastwriter.config.KafkaConfig
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.*

fun createKafkaConsumer(config: KafkaConfig): Consumer<String, String> {
    val props = Properties().apply {
        put("bootstrap.servers", config.bootstrapServers)
        put("group.id", config.groupId)
        put("key.deserializer", StringDeserializer::class.java.name)
        put("value.deserializer", StringDeserializer::class.java.name)

        // When there is no previously committed offset for this consumer group,
        // start reading from the beginning of the topic.
        put("auto.offset.reset", "earliest")

        // Automatically commit offsets at regular intervals (default every 5 seconds).
        // This simplifies offset management but might result in message loss if processing fails after the commit.
        // For production disabling this would be better.
        put("enable.auto.commit", "true")
    }

    val consumer = KafkaConsumer<String, String>(props)
    consumer.subscribe(config.topics)
    
    return consumer
}