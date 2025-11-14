package dev.devrunner.elasticsearch.agg;

import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import lombok.Getter;

import java.util.List;

/**
 * 단일 집계 쿼리
 * <p>
 * queryName으로 구분되며, 독립적인 필터 조건을 가질 수 있습니다.
 */
@Getter
public class AggregationQuery<F extends FieldName> {
    private final String queryName;
    private final List<SearchElement<F>> conditions;      // 이 쿼리의 필터 조건
    private final BucketAggregation<F> bucket;            // 버킷 집계 (optional)
    private final List<MetricAggregation<F>> metrics;     // 메트릭 집계 (optional)

    private AggregationQuery(
            String queryName,
            List<SearchElement<F>> conditions,
            BucketAggregation<F> bucket,
            List<MetricAggregation<F>> metrics
    ) {
        if (queryName == null || queryName.isBlank()) {
            throw new IllegalArgumentException("queryName is required");
        }
        if (bucket == null && (metrics == null || metrics.isEmpty())) {
            throw new IllegalArgumentException("At least one of bucket or metrics is required");
        }

        this.queryName = queryName;
        this.conditions = conditions == null ? List.of() : List.copyOf(conditions);
        this.bucket = bucket;
        this.metrics = metrics == null ? List.of() : List.copyOf(metrics);
    }

    /**
     * 버킷 집계 쿼리 생성
     *
     * @param queryName  쿼리 이름 (결과 구분용)
     * @param conditions 필터 조건들
     * @param bucket     버킷 집계 정의
     */
    public static <F extends FieldName> AggregationQuery<F> bucket(
            String queryName,
            List<SearchElement<F>> conditions,
            BucketAggregation<F> bucket
    ) {
        return new AggregationQuery<>(queryName, conditions, bucket, null);
    }

    /**
     * 메트릭 집계 쿼리 생성 (버킷 없이 전체 통계)
     *
     * @param queryName  쿼리 이름 (결과 구분용)
     * @param conditions 필터 조건들
     * @param metrics    메트릭 집계 정의들
     */
    public static <F extends FieldName> AggregationQuery<F> metrics(
            String queryName,
            List<SearchElement<F>> conditions,
            List<MetricAggregation<F>> metrics
    ) {
        return new AggregationQuery<>(queryName, conditions, null, metrics);
    }
}
