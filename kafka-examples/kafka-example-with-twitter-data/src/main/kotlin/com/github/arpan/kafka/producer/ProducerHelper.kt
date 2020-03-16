package com.github.arpan.kafka.producer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Future

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

object ProducerHelper {
    private val logger = LoggerFactory.getLogger(ProducerHelper::class.java)

    fun sendRecord(producer: Producer<String, String>, topic: String, key: String? = null, message: String):
        Future<RecordMetadata> {
        val record = if (key == null) ProducerRecord(topic, message)
        else ProducerRecord(topic, key, message)
        return producer.send(record) { metadata, exception: Exception? ->
            if (exception == null)
                logger.info("Produced record ${record.key()} -> ${record.value()} to Topic: ${metadata.topic()} on Partition: ${metadata.partition()} and Offset: ${metadata.offset()} on Timestamp: ${metadata.timestamp()}")
            else
                logger.error("Error producing record to topic ${metadata.topic()}: ", exception)
        }
    }
}