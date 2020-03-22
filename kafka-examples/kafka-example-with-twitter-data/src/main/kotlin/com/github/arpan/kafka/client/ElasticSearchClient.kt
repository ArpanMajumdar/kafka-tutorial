package com.github.arpan.kafka.client


import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

data class ElasticSearchAuth(
    val hostName: String,
    val userName: String,
    val password: String
)

class ElasticSearchClient(private val elasticSearchAuth: ElasticSearchAuth) {

    fun create(): RestHighLevelClient {
        val credentialsProvider = BasicCredentialsProvider().apply {
            setCredentials(
                AuthScope.ANY,
                UsernamePasswordCredentials(elasticSearchAuth.userName, elasticSearchAuth.password)
            )
        }
        val httpHost = HttpHost(elasticSearchAuth.hostName, 443, "https")
        val restClientBuilder = RestClient.builder(httpHost)
            .setHttpClientConfigCallback { httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            }
        return RestHighLevelClient(restClientBuilder)
    }
}