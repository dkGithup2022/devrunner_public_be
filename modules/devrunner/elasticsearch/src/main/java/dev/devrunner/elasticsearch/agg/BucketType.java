package dev.devrunner.elasticsearch.agg;

/**
 * Elasticsearch Bucket Aggregation 타입
 */
public enum BucketType {
    TERMS,           // 카테고리별 그룹핑 (회사별, 기술스택별 등)
    DATE_HISTOGRAM,  // 시간대별 그룹핑
    RANGE            // 범위별 그룹핑
}
