package com.pig4cloud.pigx.admin.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 客户端配置（基于 elasticsearch-java 8.x）
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "es")
public class ElasticsearchClientConfig {

    /**
     * ES 地址，支持多个，用逗号分隔，例如：
     * 14.103.193.249:9200 或 host1:9200,host2:9200
     */
    private String address;

    /**
     * 用户名（如 elastic）
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 底层 RestClient（由 Spring 托管，负责关闭）
     */
    @Bean(destroyMethod = "close")
    public RestClient restClient() {
        List<HttpHost> hosts = parseHosts(address);
        if (hosts.isEmpty()) {
            throw new IllegalArgumentException("es.address 配置不能为空");
        }

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[0]))
                .setHttpClientConfigCallback((HttpAsyncClientBuilder httpClientBuilder) ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                );

        log.info("Init Elasticsearch RestClient, hosts={}", hosts);
        return builder.build();
    }

    /**
     * Transport（JSON 序列化层）
     */
    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    /**
     * 上层 ElasticsearchClient，业务层注入用这个
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    /**
     * 解析 address 配置为 HttpHost 列表
     */
    private List<HttpHost> parseHosts(String address) {
        List<HttpHost> hosts = new ArrayList<>();
        if (address == null || address.trim().isEmpty()) {
            return hosts;
        }

        String[] parts = address.split(",");
        for (String part : parts) {
            String addr = part.trim();
            if (addr.isEmpty()) {
                continue;
            }

            String host;
            int port;

            if (addr.contains(":")) {
                String[] hp = addr.split(":", 2);
                host = hp[0].trim();
                port = Integer.parseInt(hp[1].trim());
            } else {
                host = addr;
                port = 9200;
            }

            hosts.add(new HttpHost(host, port, "http")); // 如果你是 https，这里改成 "https"
        }
        return hosts;
    }
}
