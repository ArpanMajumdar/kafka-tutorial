package com.github.arpan.kafka.consumer

import com.github.arpan.kafka.client.ElasticSearchAuth
import com.github.arpan.kafka.client.TwitterElasticSearchClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.LoggerFactory
import java.time.Duration

fun main() = runBlocking {
    val logger = LoggerFactory.getLogger("com.github.arpan.kafka.consumer.TwitterConsumer")

    // ElasticSearch details
    val esHostName = "localhost"
    val index = "twitter"
    val esClient = TwitterElasticSearchClient(ElasticSearchAuth(hostName = esHostName))

    // Kafka details
    val bootstrapServer = "localhost:9092"
    val topic = "twitter-tweets"
    val kafkaConsumerHelper = KafkaConsumerHelper(bootstrapServer, topic).apply {
        subscribeToTopics(topic)
    }

    // Poll for tweets and dump them to ES
    val job = launch(Dispatchers.Default) {
        try {
            val records = kafkaConsumerHelper.poll(Duration.ofMillis(500))
            esClient.dumpKafkaRecordsToEs(index, records)
        } catch (exception: WakeupException) {
            logger.info("Received shutdown signal")
        } finally {
            logger.info("Exiting application ...")
            kafkaConsumerHelper.close()
            logger.info("Application has exited")
        }
    }

    // Close kafka consumer and ES client for graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            logger.info("Caught shutdown hook")
            kafkaConsumerHelper.wakeup()
            job.join()

            logger.info("Closing ES client ...")
            esClient.close()
            logger.info("ES client closed.")
        }
    })
}