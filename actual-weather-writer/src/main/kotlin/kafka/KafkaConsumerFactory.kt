package cz.savic.weatherevaluator.actualweatherwriter.kafka

import cz.savic.weatherevaluator.actualweatherwriter.config.KafkaConfig
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.Properties

fun createKafkaConsumer(config: KafkaConfig): Consumer<String, String> {
    val props = Properties().apply {
        put("bootstrap.servers", config.bootstrapServers)
        put("group.id", config.groupId)
        put("key.deserializer", StringDeserializer::class.java.name)
        put("value.deserializer", StringDeserializer::class.java.name)
        put("auto.offset.reset", "earliest")
        put("enable.auto.commit", "true")
    }

    val consumer = KafkaConsumer<String, String>(props)
    consumer.subscribe(config.topics)
    return consumer
}