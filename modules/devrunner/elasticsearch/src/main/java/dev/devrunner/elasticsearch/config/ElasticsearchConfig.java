package dev.devrunner.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
@Slf4j
public class ElasticsearchConfig {

    @Value("${elasticsearch.url}")
    private String esUrl;

    @Value("${elasticsearch.api-key}")
    private String esApiKey;

    /**
     * Elasticsearch 전용 ObjectMapper
     *
     * Elasticsearch 응답을 Doc으로 역직렬화할 때 사용합니다.
     * - JavaTimeModule: Instant, LocalDateTime 등 Java 8 시간 타입 지원
     * - FAIL_ON_UNKNOWN_PROPERTIES: false - ES 스키마 변경 시에도 호환성 유지
     * - FAIL_ON_TRAILING_TOKENS: false - 파싱 유연성 확보
     */
    @Bean
    @Qualifier("esObjectMapper")
    public ObjectMapper esObjectMapper() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new ParameterNamesModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false)
                .build();

        // Instant를 epoch_millis 숫자로 직렬화 (long 타입)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        log.info("[ElasticsearchConfig] Initialized esObjectMapper: {}", System.identityHashCode(mapper));
        return mapper;
    }

    /**
     * Elasticsearch RestClient Bean
     *
     * - ApiKey 기반 인증
     * - KeepAlive: 5분
     * - SSL hostname verification 비활성화 (개발 환경용)
     */
    @Bean
    public RestClient restClient() {
        log.info("[ElasticsearchConfig] Initializing RestClient for: {}", esUrl);

        return RestClient
                .builder(HttpHost.create("https://" + esUrl))
                .setHttpClientConfigCallback(httpAsyncClientBuilder ->
                        httpAsyncClientBuilder
                                .setSSLHostnameVerifier((host, sslSession) -> true)
                                .setKeepAliveStrategy((response, context) -> Duration.ofMinutes(5).toMillis())
                )
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + esApiKey)
                })
                .build();
    }

    /**
     * Elasticsearch Client Bean
     *
     * RestClient와 esObjectMapper를 주입받아 ElasticsearchClient 생성
     */
    @Bean
    @Primary
    public ElasticsearchClient esClient(
            RestClient restClient,
            @Qualifier("esObjectMapper") ObjectMapper esObjectMapper
    ) {
        log.info("[ElasticsearchConfig] Using esObjectMapper (hash: {}) to build ElasticsearchClient",
                System.identityHashCode(esObjectMapper));

        var transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(esObjectMapper)
        );

        var client = new ElasticsearchClient(transport);
        log.info("[ElasticsearchConfig] ElasticsearchClient initialized: {}", System.identityHashCode(client));
        return client;
    }
}
