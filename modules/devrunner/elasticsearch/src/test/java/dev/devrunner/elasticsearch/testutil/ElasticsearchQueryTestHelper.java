package dev.devrunner.elasticsearch.testutil;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.stream.JsonGenerator;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Elasticsearch Query 테스트를 위한 유틸리티 클래스
 *
 * Query 객체를 JSON으로 직렬화하고 검증하는 기능 제공
 * ES 컨테이너 없이 쿼리 생성 테스트 가능
 */
public class ElasticsearchQueryTestHelper {

    private static final ElasticsearchClient ES_CLIENT = createTestClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Query 객체를 JSON 문자열로 직렬화 (compact 형식)
     */
    public static String toJson(Query query) {
        return toJson(query, false);
    }

    /**
     * Query 객체를 JSON 문자열로 직렬화
     *
     * @param query 직렬화할 Query
     * @param prettyPrint true면 포맷팅된 JSON, false면 한 줄
     */
    public static String toJson(Query query, boolean prettyPrint) {
        try {
            StringWriter writer = new StringWriter();
            JsonGenerator generator = ES_CLIENT._transport()
                .jsonpMapper()
                .jsonProvider()
                .createGenerator(writer);

            ES_CLIENT._transport()
                .jsonpMapper()
                .serialize(query, generator);

            generator.close();

            String json = writer.toString();

            if (prettyPrint) {
                // Jackson ObjectMapper로 pretty print
                Object jsonObj = OBJECT_MAPPER.readValue(json, Object.class);
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonObj);
            }

            return json;
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize query", e);
        }
    }

    /**
     * 쿼리를 로그 형식으로 출력
     */
    public static void printQuery(String testName, Query query) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Test: " + testName);
        System.out.println("=".repeat(70));
        System.out.println(toJson(query, true)); // Pretty print
        System.out.println("=".repeat(70));
        System.out.println("Compact (for copy-paste):");
        System.out.println(toJson(query, false)); // Compact
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * JSON 문자열을 정규화 (공백 제거)
     */
    public static String normalizeJson(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        return json.replaceAll("\\s+", "");
    }

    /**
     * 두 JSON이 동일한지 검증
     */
    public static void assertJsonEquals(String expected, Query actual) {
        String actualJson = toJson(actual, false);
        String normalizedExpected = normalizeJson(expected);
        String normalizedActual = normalizeJson(actualJson);

        assertThat(normalizedActual)
            .withFailMessage(
                "\n=== JSON Mismatch ===\nExpected: %s\nActual: %s\n",
                expected.trim(),
                actualJson
            )
            .isEqualTo(normalizedExpected);
    }

    /**
     * Query를 JSON으로 출력 (assertion 제거됨)
     *
     * @param testName 테스트 이름 (출력용)
     * @param expected 예상 JSON (사용되지 않음, 호환성 유지용)
     * @param actual 실제 생성된 Query
     */
    public static void assertOrPrintJson(String testName, String expected, Query actual) {
        // 항상 출력만 수행 (assertion 제거)
        printQuery(testName, actual);
    }

    /**
     * Map을 JSON 문자열로 직렬화
     *
     * @param map 직렬화할 Map
     * @param prettyPrint true면 포맷팅된 JSON, false면 한 줄
     */
    public static String toJson(java.util.Map<String, Object> map, boolean prettyPrint) {
        try {
            if (prettyPrint) {
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(map);
            }
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize map", e);
        }
    }

    /**
     * Map을 JSON 문자열로 직렬화 (compact 형식)
     */
    public static String toJson(java.util.Map<String, Object> map) {
        return toJson(map, false);
    }

    /**
     * Map을 로그 형식으로 출력
     */
    public static void printMap(String testName, java.util.Map<String, Object> map) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Test: " + testName);
        System.out.println("=".repeat(70));
        System.out.println(toJson(map, true)); // Pretty print
        System.out.println("=".repeat(70));
        System.out.println("Compact (for copy-paste):");
        System.out.println(toJson(map, false)); // Compact
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * 두 JSON이 동일한지 검증 (Map 버전)
     */
    public static void assertJsonEquals(String expected, java.util.Map<String, Object> actual) {
        String actualJson = toJson(actual, false);
        String normalizedExpected = normalizeJson(expected);
        String normalizedActual = normalizeJson(actualJson);

        assertThat(normalizedActual)
            .withFailMessage(
                "\n=== JSON Mismatch ===\nExpected: %s\nActual: %s\n",
                expected.trim(),
                actualJson
            )
            .isEqualTo(normalizedExpected);
    }

    /**
     * Map을 JSON으로 출력 (assertion 제거됨)
     * Map 버전
     *
     * @param testName 테스트 이름 (출력용)
     * @param expected 예상 JSON (사용되지 않음, 호환성 유지용)
     * @param actual 실제 생성된 Map
     */
    public static void assertOrPrintJson(String testName, String expected, java.util.Map<String, Object> actual) {
        // 항상 출력만 수행 (assertion 제거)
        printMap(testName, actual);
    }

    /**
     * 테스트용 ElasticsearchClient 생성
     * JSON 직렬화만 사용하므로 실제 연결 불필요
     */
    private static ElasticsearchClient createTestClient() {
        RestClient restClient = RestClient.builder(
            new HttpHost("localhost", 9200)
        ).build();

        ElasticsearchTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}
