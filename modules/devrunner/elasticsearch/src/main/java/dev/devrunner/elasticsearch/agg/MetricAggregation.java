package dev.devrunner.elasticsearch.agg;

import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;
import lombok.Getter;

/**
 * Metric Aggregation - 숫자 필드에 대한 통계 집계
 * <p>
 * 평균, 합계, 최대/최소값 등의 메트릭을 계산합니다.
 */
@Getter
public class MetricAggregation<F extends FieldName> {
    private final F field;
    private final MetricType type;
    private final String name;

    public MetricAggregation(F field, MetricType type, String name) {
        this.field = field;
        this.type = type;
        this.name = name;
    }

    /**
     * 평균값 집계
     */
    public static <F extends FieldName> MetricAggregation<F> avg(F field, String name) {
        return new MetricAggregation<>(field, MetricType.AVG, name);
    }

    /**
     * 합계 집계
     */
    public static <F extends FieldName> MetricAggregation<F> sum(F field, String name) {
        return new MetricAggregation<>(field, MetricType.SUM, name);
    }

    /**
     * 최솟값 집계
     */
    public static <F extends FieldName> MetricAggregation<F> min(F field, String name) {
        return new MetricAggregation<>(field, MetricType.MIN, name);
    }

    /**
     * 최댓값 집계
     */
    public static <F extends FieldName> MetricAggregation<F> max(F field, String name) {
        return new MetricAggregation<>(field, MetricType.MAX, name);
    }

    /**
     * 문서 개수 (field 불필요)
     */
    public static <F extends FieldName> MetricAggregation<F> count(String name) {
        return new MetricAggregation<>(null, MetricType.VALUE_COUNT, name);
    }

    /**
     * 고유값 개수
     */
    public static <F extends FieldName> MetricAggregation<F> cardinality(F field, String name) {
        return new MetricAggregation<>(field, MetricType.CARDINALITY, name);
    }
}
