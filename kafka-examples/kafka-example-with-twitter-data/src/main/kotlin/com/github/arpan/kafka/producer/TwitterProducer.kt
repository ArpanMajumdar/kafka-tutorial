package com.github.arpan.kafka.producer

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

fun main() {
    val consumerKey: String = System.getenv("CONSUMER_KEY")
            ?: throw IllegalArgumentException("CONSUMER_KEY is required.")
    val consumerSecret: String = System.getenv("CONSUMER_SECRET")
            ?: throw IllegalArgumentException("CONSUMER_SECRET is required.")
    val accessToken: String = System.getenv("ACCESS_TOKEN")
            ?: throw IllegalArgumentException("ACCESS_TOKEN is required.")
    val accessTokenSecret: String = System.getenv("ACCESS_TOKEN_SECRET")
            ?: throw IllegalArgumentException("ACCESS_TOKEN_SECRET is required.")

    val msgQueue = LinkedBlockingQueue<String>(1000)
    val twitterAuth = TwitterAuth(consumerKey, consumerSecret, accessToken, accessTokenSecret)
    val terms = listOf("corona")

    val bootstrapServer = "localhost:9092"
    val producer = ProducerFactory.create(bootstrapServer)
    val timeout = 5L
    val timeUnit = TimeUnit.SECONDS
    TwitterClient(msgQueue, twitterAuth, terms).apply {
        connect()
        pollAndSendToKafka(producer, timeout, timeUnit)
    }
}