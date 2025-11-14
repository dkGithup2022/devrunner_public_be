package dev.devrunner.elasticsearch.agg;

/**
 * Elasticsearch Metric Aggregation 타입
 */
public enum MetricType {
    AVG,            // 평균
    SUM,            // 합계
    MIN,            // 최솟값
    MAX,            // 최댓값
    VALUE_COUNT,    // 개수
    CARDINALITY     // 고유값 개수
}
