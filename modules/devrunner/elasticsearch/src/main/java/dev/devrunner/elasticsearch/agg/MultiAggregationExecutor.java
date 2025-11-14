package dev.devrunner.elasticsearch.agg;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.elasticsearch.exception.ElasticsearchQueryException;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.RangeQueryBuilder;
import jakarta.json.stream.JsonGenerator;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;

/**
 * Elasticsearch 집계 쿼리 실행기
 * <p>
 * 다중 집계 쿼리를 실행하고 결과를 파싱합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MultiAggregationExecutor {
    private final ElasticsearchClient esClient;
    private final ObjectMapper objectMapper;

    /**
     * 다중 집계 쿼리 실행
     * <p>
     * 여러 개의 독립적인 집계 쿼리를 한 번에 실행합니다.
     *
     * @param indexName      인덱스 이름
     * @param command        집계 명령
     * @param qbRegistry     일반 쿼리 빌더 룩업
     * @param rangeRegistry  범위 쿼리 빌더 룩업
     * @return 집계 결과
     */
    public <F extends FieldName> MultiAggregationResult aggregateMulti(
            String indexName,
            MultiAggregationCommand<F> command,
            Function<? super F, Optional<FieldQueryBuilder>> qbRegistry,
            Function<? super F, Optional<RangeQueryBuilder>> rangeRegistry
    ) {
        try {
            // 1. 집계 쿼리 빌드
            Map<String, Object> aggs = GenericAggregationBuilder.build(
                    command,
                    qbRegistry,
                    rangeRegistry
            );

            // 2. 전체 쿼리 구성
            Map<String, Object> queryBody = new HashMap<>();
            queryBody.put("size", 0);  // 문서는 필요 없음, 집계 결과만
            queryBody.put("aggs", aggs);

            // 3. JSON으로 변환 (로깅용)
            String queryJson = objectMapper.writeValueAsString(queryBody);
            log.info("집계 쿼리 JSON: {}", queryJson);

            // 4. Raw JSON으로 검색 실행
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .withJson(new java.io.StringReader(queryJson))
            );

            // ✅ NEW: Object.class로 받아서 SearchResponse 객체 얻기
            SearchResponse<Object> response = esClient.search(searchRequest, Object.class);

            // ✅ NEW: response.aggregations()로 Map<String, Aggregate> 가져오기
            Map<String, Aggregate> aggregations = response.aggregations();
            log.info("집계 결과 키 목록: {}", aggregations.keySet());

            // ✅ NEW: Aggregate 객체로 직접 파싱
            return parseAggregationsFromEsApi(aggregations, command);

            /* ========== 기존 로직 (주석처리) ==========
            var response = esClient.search(searchRequest, Object.class);

            // 5. 결과 파싱
            String responseJson = objectMapper.writeValueAsString(response);
            JsonNode root = objectMapper.readTree(responseJson);
            log.info("집계 응답: {}", responseJson);

            return parseMultiAggregationResult(root, command);
            ========== 기존 로직 끝 ========== */

        } catch (Exception e) {
            log.error("집계 쿼리 실패 - Index: {}, Error: {}", indexName, e.getMessage(), e);
            throw new ElasticsearchQueryException(indexName, e);
        }
    }

    /**
     * ✅ NEW: Aggregate 맵에서 MultiAggregationResult 파싱
     */
    private <F extends FieldName> MultiAggregationResult parseAggregationsFromEsApi(
            Map<String, Aggregate> aggregations,
            MultiAggregationCommand<F> command
    ) {
        Map<String, MultiAggregationResult.QueryResult> results = new HashMap<>();

        for (AggregationQuery<F> query : command.queries()) {
            String queryName = query.getQueryName();
            Aggregate agg = aggregations.get(queryName);

            if (agg == null) {
                log.warn("집계 결과 없음: {}", queryName);
                continue;
            }

            log.debug("파싱 중: {} - kind: {}", queryName, agg._kind());

            // 디버거에서 확인: 모든 결과가 Filter로 감싸져 있음
            if (agg.isFilter()) {
                // filter().aggregations()로 서브 집계 맵 가져오기
                var filterAgg = agg.filter();
                Map<String, Aggregate> subAggs = filterAgg.aggregations();
                long filterDocCount = filterAgg.docCount(); // ✅ Filter의 doc_count 가져오기

                if (query.getBucket() != null) {
                    // 버킷 이름으로 실제 버킷 집계 가져오기
                    String bucketName = query.getBucket().getName();
                    Aggregate bucketAgg = subAggs.get(bucketName);

                    if (bucketAgg != null) {
                        var bucketResult = parseBucketAggregate(bucketAgg, query);
                        // ✅ filterDocCount 포함하여 생성
                        results.put(queryName, MultiAggregationResult.QueryResult.withBucket(bucketResult, filterDocCount));
                    }
                } else if (!query.getMetrics().isEmpty()) {
                    var metrics = parseMetricsFromAggregates(subAggs, query);
                    results.put(queryName, MultiAggregationResult.QueryResult.withMetrics(metrics));
                }
            }
            // Global aggregation (조건 없는 메트릭)
            else if (agg.isGlobal()) {
                Map<String, Aggregate> globalAggs = agg.global().aggregations();
                var metrics = parseMetricsFromAggregates(globalAggs, query);
                results.put(queryName, MultiAggregationResult.QueryResult.withMetrics(metrics));
            }
            // 직접 버킷 (조건 없는 버킷)
            else if (query.getBucket() != null) {
                var bucketResult = parseBucketAggregate(agg, query);
                results.put(queryName, MultiAggregationResult.QueryResult.withBucket(bucketResult));
            }
        }

        return new MultiAggregationResult(results);
    }

    /* ========== 기존 JSON 파싱 로직 (주석처리) ==========
    private <F extends FieldName> MultiAggregationResult parseMultiAggregationResult(
            JsonNode root,
            MultiAggregationCommand<F> command
    ) {
        JsonNode aggsNode = root.path("aggregations");
        Map<String, MultiAggregationResult.QueryResult> results = new HashMap<>();

        for (AggregationQuery<F> query : command.queries()) {
            JsonNode queryNode = aggsNode.path(query.getQueryName());

            // filter가 있었으면 한 단계 더 들어가야 함
            if (!query.getConditions().isEmpty()) {
                // filter aggregation 내부의 실제 집계 결과
                if (query.getBucket() != null) {
                    JsonNode bucketNode = queryNode.path(query.getBucket().getName());
                    var bucketResult = parseBucketResult(bucketNode, query);
                    results.put(query.getQueryName(),
                            MultiAggregationResult.QueryResult.withBucket(bucketResult));
                } else if (!query.getMetrics().isEmpty()) {
                    var metrics = parseMetrics(queryNode, query);
                    results.put(query.getQueryName(),
                            MultiAggregationResult.QueryResult.withMetrics(metrics));
                }
            } else {
                // 조건 없으면 바로 집계 결과
                if (query.getBucket() != null) {
                    var bucketResult = parseBucketResult(queryNode, query);
                    results.put(query.getQueryName(),
                            MultiAggregationResult.QueryResult.withBucket(bucketResult));
                } else if (!query.getMetrics().isEmpty()) {
                    // global aggregation인 경우
                    var metrics = parseMetrics(queryNode, query);
                    results.put(query.getQueryName(),
                            MultiAggregationResult.QueryResult.withMetrics(metrics));
                }
            }
        }

        return new MultiAggregationResult(results);
    }
    ========== 기존 JSON 파싱 로직 끝 ========== */

    /**
     * ✅ NEW: Aggregate에서 버킷 결과 파싱
     * 디버거 결과 기준: sterms 또는 date_histogram
     */
    private <F extends FieldName> MultiAggregationResult.BucketResult parseBucketAggregate(
            Aggregate agg,
            AggregationQuery<F> query
    ) {
        List<MultiAggregationResult.BucketEntry> entries = new ArrayList<>();

        // StringTerms (sterms) - 디버거에서 확인된 타입
        if (agg.isSterms()) {
            var sterms = agg.sterms();
            var buckets = sterms.buckets().array(); // Buckets._kind = "Array"

            for (StringTermsBucket bucket : buckets) {
                String key = bucket.key().stringValue();
                long docCount = bucket.docCount();

                // 버킷 내 메트릭 파싱 (예: value_count#count)
                Map<String, Double> metrics = new HashMap<>();
                if (query.getBucket() != null) {
                    for (var metric : query.getBucket().getMetrics()) {
                        String metricName = metric.getName();
                        Aggregate metricAgg = bucket.aggregations().get(metricName);
                        if (metricAgg != null) {
                            metrics.put(metricName, getMetricValue(metricAgg));
                        }
                    }
                }

                entries.add(new MultiAggregationResult.BucketEntry(key, docCount, metrics));
            }
        }
        // DateHistogram - 디버거에서 확인된 타입
        else if (agg.isDateHistogram()) {
            var dateHist = agg.dateHistogram();
            var buckets = dateHist.buckets().array(); // Buckets._kind = "Array"

            for (DateHistogramBucket bucket : buckets) {
                String key = bucket.keyAsString(); // "2025-10-20T00:00:00.000Z"
                long docCount = bucket.docCount();

                // 버킷 내 메트릭 파싱
                Map<String, Double> metrics = new HashMap<>();
                if (query.getBucket() != null) {
                    for (var metric : query.getBucket().getMetrics()) {
                        String metricName = metric.getName();
                        Aggregate metricAgg = bucket.aggregations().get(metricName);
                        if (metricAgg != null) {
                            metrics.put(metricName, getMetricValue(metricAgg));
                        }
                    }
                }

                entries.add(new MultiAggregationResult.BucketEntry(key, docCount, metrics));
            }
        }

        return new MultiAggregationResult.BucketResult(entries);
    }

    /**
     * ✅ NEW: Aggregate 맵에서 메트릭 파싱
     */
    private <F extends FieldName> Map<String, Double> parseMetricsFromAggregates(
            Map<String, Aggregate> aggregates,
            AggregationQuery<F> query
    ) {
        Map<String, Double> metrics = new HashMap<>();
        for (var metric : query.getMetrics()) {
            String metricName = metric.getName();
            Aggregate metricAgg = aggregates.get(metricName);
            if (metricAgg != null) {
                metrics.put(metricName, getMetricValue(metricAgg));
            }
        }
        return metrics;
    }

    /**
     * ✅ NEW: Aggregate에서 메트릭 값 추출
     * 디버거에서 확인: value_count#count → ValueCountAggregate
     */
    private double getMetricValue(Aggregate agg) {
        if (agg.isValueCount()) {
            return agg.valueCount().value(); // 디버거: {"value":71.0}
        } else if (agg.isAvg()) {
            return agg.avg().value();
        } else if (agg.isSum()) {
            return agg.sum().value();
        } else if (agg.isMin()) {
            return agg.min().value();
        } else if (agg.isMax()) {
            return agg.max().value();
        }
        log.warn("알 수 없는 메트릭 타입: {}", agg._kind());
        return 0.0;
    }

    /* ========== 기존 버킷/메트릭 파싱 로직 (주석처리) ==========
    private <F extends FieldName> MultiAggregationResult.BucketResult parseBucketResult(
            JsonNode bucketNode,
            AggregationQuery<F> query
    ) {
        List<MultiAggregationResult.BucketEntry> entries = new ArrayList<>();
        JsonNode bucketsArray = bucketNode.path("buckets");

        for (JsonNode bucket : bucketsArray) {
            String key = bucket.has("key_as_string")
                    ? bucket.path("key_as_string").asText()
                    : bucket.path("key").asText();
            long docCount = bucket.path("doc_count").asLong();

            // 버킷 내 메트릭 파싱
            Map<String, Double> metrics = new HashMap<>();
            if (query.getBucket() != null) {
                for (var metric : query.getBucket().getMetrics()) {
                    JsonNode metricNode = bucket.path(metric.getName());
                    if (metricNode.has("value")) {
                        metrics.put(metric.getName(), metricNode.path("value").asDouble());
                    }
                }
            }

            entries.add(new MultiAggregationResult.BucketEntry(key, docCount, metrics));
        }

        return new MultiAggregationResult.BucketResult(entries);
    }

    private <F extends FieldName> Map<String, Double> parseMetrics(
            JsonNode parentNode,
            AggregationQuery<F> query
    ) {
        Map<String, Double> metrics = new HashMap<>();
        for (var metric : query.getMetrics()) {
            JsonNode metricNode = parentNode.path(metric.getName());
            if (metricNode.has("value")) {
                metrics.put(metric.getName(), metricNode.path("value").asDouble());
            }
        }
        return metrics;
    }
    ========== 기존 버킷/메트릭 파싱 로직 끝 ========== */
}
