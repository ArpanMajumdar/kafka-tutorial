package com.github.arpan.kafka.client

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory

data class ElasticSearchAuth(
        val hostName: String,
        val userName: String = "",
        val password: String = ""
)

class TwitterElasticSearchClient(private val elasticSearchAuth: ElasticSearchAuth) {

    private val logger = LoggerFactory.getLogger(TwitterElasticSearchClient::class.java)

    private val esClient: RestHighLevelClient = create()

    private fun create(): RestHighLevelClient {
        val credentialsProvider: BasicCredentialsProvider? =
                if (elasticSearchAuth.userName.isNotBlank() &&
                        elasticSearchAuth.password.isNotBlank()) {
                    BasicCredentialsProvider().apply {
                        setCredentials(
                                AuthScope.ANY,
                                UsernamePasswordCredentials(elasticSearchAuth.userName, elasticSearchAuth.password)
                        )
                    }
                } else null

        val httpHost = if (credentialsProvider != null) HttpHost(elasticSearchAuth.hostName, 443, "https")
        else HttpHost(elasticSearchAuth.hostName, 9200, "http")

        val restClientBuilder = if (credentialsProvider != null) {
            RestClient.builder(httpHost)
                    .setHttpClientConfigCallback { httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                    }
        } else RestClient.builder(httpHost)

        return RestHighLevelClient(restClientBuilder)
    }

    private fun index(indexRequest: IndexRequest): IndexResponse = esClient.index(indexRequest, RequestOptions.DEFAULT)

    private fun createIndex(index: String, message: String): String {
        val indexRequest = IndexRequest(index).source(message, XContentType.JSON)
        val indexResponse = index(indexRequest)
        return indexResponse.id
    }

    fun dumpKafkaRecordsToEs(index: String, records: ConsumerRecords<String, String>) {
        records.forEach { record ->
            val id = createIndex(index, record.value())
            logger.info(getLogMessageFromConsumerRecord(record, id))
        }
    }

    fun close() = esClient.close()

    private fun getLogMessageFromConsumerRecord(record: ConsumerRecord<String, String>, index: String) =
            "Received record from Topic ${record.topic()} from Partition: ${record.partition()} and " +
                    "Offset: ${record.offset()} at Timestamp: ${record.timestamp()} and saved to ES at index $index"
}