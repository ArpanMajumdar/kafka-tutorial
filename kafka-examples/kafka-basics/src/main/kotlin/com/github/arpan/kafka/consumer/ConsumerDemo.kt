package com.github.arpan.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

fun main() {
    // Consumer config
    val bootstrapServer = "localhost:9092"
    val consumerGroup = "kafka-consumer-demo-app-cg-1"
    val topic = "test2"

    val kafkaConsumerConfig = ConsumerDemo.getKafkaConfig(bootstrapServer, consumerGroup)
    val consumer = ConsumerDemo.createConsumer(kafkaConsumerConfig)
    ConsumerDemo.subscribeToTopics(consumer, topic)

    while (true) {
        ConsumerDemo.poll(consumer, Duration.ofMillis(500))
    }
}


object ConsumerDemo {
    private val logger = LoggerFactory.getLogger(ConsumerDemo::class.java)

    fun getKafkaConfig(bootstrapServer: String, consumerGroup: String): Properties = Properties().apply {
        setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
        setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        setProperty(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup)
        setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }

    fun createConsumer(kafkaConfig: Properties) = KafkaConsumer<String, String>(kafkaConfig)

    fun subscribeToTopics(consumer: KafkaConsumer<String, String>, vararg topics: String) =
        consumer.subscribe(topics.toList())

    fun poll(consumer: KafkaConsumer<String, String>, duration: Duration) {
        val records = consumer.poll(duration)

        records.forEach { record ->
            logger.info("Received record ${record.key()} -> ${record.value()} from Topic ${record.topic()} from Partition: ${record.partition()} and Offset: ${record.offset()} at Timestamp: ${record.timestamp()}")
        }
    }
}