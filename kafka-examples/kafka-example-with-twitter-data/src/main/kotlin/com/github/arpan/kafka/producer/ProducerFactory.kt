package com.github.arpan.kafka.producer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

object ProducerFactory {
    fun create(bootstrapServer: String): KafkaProducer<String, String> {
        val kafkaConfig = Properties().apply {
            setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
            setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        }
        return KafkaProducer<String, String>(kafkaConfig)
    }
}