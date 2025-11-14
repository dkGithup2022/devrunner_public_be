package dev.devrunner.elasticsearch.agg;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.GenericSearchQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.RangeQueryBuilder;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Elasticsearch Aggregation Query Builder
 * <p>
 * MultiAggregationCommand를 Elasticsearch aggregation JSON으로 변환합니다.
 */
public class GenericAggregationBuilder {

    /**
     * MultiAggregationCommand를 ES aggregation JSON으로 빌드
     */
    public static <F extends FieldName> Map<String, Object> build(
            MultiAggregationCommand<F> command,
            Function<? super F, Optional<FieldQueryBuilder>> qbRegistry,
            Function<? super F, Optional<RangeQueryBuilder>> rangeRegistry
    ) {
        Map<String, Object> aggs = new HashMap<>();

        for (AggregationQuery<F> query : command.queries()) {
            Map<String, Object> queryAgg = new HashMap<>();

            // 1. Filter가 있으면 filter aggregation으로 감싸기
            if (!query.getConditions().isEmpty()) {
                var filterQuery = buildFilterQuery(
                        query.getConditions(),
                        qbRegistry,
                        rangeRegistry
                );

                queryAgg.put("filter", filterQuery);

                // 2. 하위 집계 추가
                Map<String, Object> subAggs = new HashMap<>();
                if (query.getBucket() != null) {
                    subAggs.put(query.getBucket().getName(),
                            buildBucketAggregation(query.getBucket()));
                }
                for (MetricAggregation<F> metric : query.getMetrics()) {
                    subAggs.put(metric.getName(),
                            buildMetricAggregation(metric));
                }
                queryAgg.put("aggs", subAggs);

            } else {
                // 조건 없으면 직접 집계
                if (query.getBucket() != null) {
                    queryAgg = buildBucketAggregation(query.getBucket());
                } else if (!query.getMetrics().isEmpty()) {
                    // 메트릭만 있는 경우 (global aggregation)
                    Map<String, Object> subAggs = new HashMap<>();
                    for (MetricAggregation<F> metric : query.getMetrics()) {
                        subAggs.put(metric.getName(),
                                buildMetricAggregation(metric));
                    }
                    // global로 감싸기
                    queryAgg.put("global", Map.of());
                    queryAgg.put("aggs", subAggs);
                }
            }

            aggs.put(query.getQueryName(), queryAgg);
        }

        return aggs;
    }

    /**
     * SearchElement 리스트를 ES filter query로 변환
     */
    private static <F extends FieldName> Map<String, Object> buildFilterQuery(
            List<SearchElement<F>> conditions,
            Function<? super F, Optional<FieldQueryBuilder>> qbRegistry,
            Function<? super F, Optional<RangeQueryBuilder>> rangeRegistry
    ) {
        // GenericSearchQueryBuilder 재사용
        var query = GenericSearchQueryBuilder.build(
                conditions,
                qbRegistry,
                rangeRegistry
        );

        // Query를 Map으로 변환
        return queryToMap(query);
    }

    /**
     * Elasticsearch Query 객체를 Map으로 변환
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> queryToMap(Query query) {
        try {
            var writer = new StringWriter();
            var mapper = new ObjectMapper();

            // Query를 JSON으로 직렬화
            var jsonpMapper = new JacksonJsonpMapper(mapper);
            var generator = jsonpMapper.jsonProvider().createGenerator(writer);
            query.serialize(generator, jsonpMapper);
            generator.close();

            // JSON을 Map으로 변환
            return mapper.readValue(writer.toString(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Query to Map", e);
        }
    }

    /**
     * BucketAggregation을 ES JSON으로 변환
     */
    private static <F extends FieldName> Map<String, Object> buildBucketAggregation(
            BucketAggregation<F> bucket
    ) {
        Map<String, Object> bucketDef = new HashMap<>();

        // 버킷 타입별 정의
        Map<String, Object> bucketConfig = switch (bucket.getType()) {
            case TERMS -> Map.of(
                    "field", bucket.getField().getFieldName(),
                    "size", bucket.getSize()
            );
            case DATE_HISTOGRAM -> Map.of(
                    "field", bucket.getField().getFieldName(),
                    "calendar_interval", bucket.getInterval()
            );
            default -> throw new IllegalArgumentException("Unsupported bucket type: " + bucket.getType());
        };

        bucketDef.put(bucket.getType().name().toLowerCase(), bucketConfig);

        // 서브 메트릭 추가
        if (!bucket.getMetrics().isEmpty()) {
            Map<String, Object> subAggs = new HashMap<>();
            for (MetricAggregation<F> metric : bucket.getMetrics()) {
                subAggs.put(metric.getName(), buildMetricAggregation(metric));
            }
            bucketDef.put("aggs", subAggs);
        }

        return bucketDef;
    }

    /**
     * MetricAggregation을 ES JSON으로 변환
     */
    private static <F extends FieldName> Map<String, Object> buildMetricAggregation(
            MetricAggregation<F> metric
    ) {
        String metricType = metric.getType().name().toLowerCase();

        // VALUE_COUNT는 field가 필요 없음 (_index 사용)
        if (metric.getType() == MetricType.VALUE_COUNT) {
            return Map.of(metricType, Map.of("field", "_index"));
        }

        return Map.of(
                metricType,
                Map.of("field", metric.getField().getFieldName())
        );
    }
}
