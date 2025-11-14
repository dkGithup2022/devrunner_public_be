package dev.devrunner.elasticsearch.agg;

import java.util.List;
import java.util.Map;

/**
 * 다중 집계 쿼리의 결과
 * <p>
 * queryName을 키로, 각 쿼리의 결과를 값으로 가집니다.
 */
public record MultiAggregationResult(
        Map<String, QueryResult> results  // queryName -> result
) {
    /**
     * 단일 쿼리의 결과
     */
    public record QueryResult(
            BucketResult bucket,              // 버킷 결과 (있으면)
            Map<String, Double> metrics,      // 메트릭 결과 (있으면)
            Long filterDocCount               // Filter aggregation의 doc_count (있으면)
    ) {
        public static QueryResult withBucket(BucketResult bucket) {
            return new QueryResult(bucket, null, null);
        }

        public static QueryResult withBucket(BucketResult bucket, long filterDocCount) {
            return new QueryResult(bucket, null, filterDocCount);
        }

        public static QueryResult withMetrics(Map<String, Double> metrics) {
            return new QueryResult(null, metrics, null);
        }
    }

    /**
     * 버킷 집계 결과
     */
    public record BucketResult(
            List<BucketEntry> entries
    ) {}

    /**
     * 개별 버킷 엔트리
     */
    public record BucketEntry(
            String key,                       // 버킷 키 (회사명, 날짜 등)
            long docCount,                    // 문서 개수
            Map<String, Double> metrics       // 이 버킷의 메트릭들
    ) {}
}
