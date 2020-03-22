package com.github.arpan.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

class KafkaConsumerHelper(private val bootstrapServer: String, private val consumerGroup: String) {
    private val logger = LoggerFactory.getLogger(KafkaConsumerHelper::class.java)

    private val kafkaConfig: Properties = Properties().apply {
        setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
        setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        setProperty(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup)
        setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
    private val consumer: KafkaConsumer<String, String> = createConsumer(kafkaConfig)

    private fun createConsumer(kafkaConfig: Properties) = KafkaConsumer<String, String>(kafkaConfig)

    fun subscribeToTopics(vararg topics: String) = consumer.subscribe(topics.toList())

    fun poll(duration: Duration): ConsumerRecords<String, String> = consumer.poll(duration)

    fun close() = consumer.close()

    fun wakeup() = consumer.wakeup()
}