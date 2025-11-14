package dev.devrunner.elasticsearch.agg;

import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;
import lombok.Getter;

import java.util.List;

/**
 * Bucket Aggregation - 문서들을 그룹으로 나누는 집계
 * <p>
 * 각 버킷(그룹)마다 문서 개수와 서브 메트릭을 계산할 수 있습니다.
 */
@Getter
public class BucketAggregation<F extends FieldName> {
    private final F field;
    private final BucketType type;
    private final String name;
    private final Integer size;                        // terms용
    private final String interval;                     // date_histogram용
    private final List<MetricAggregation<F>> metrics;  // 각 버킷의 서브 메트릭

    private BucketAggregation(
            F field,
            BucketType type,
            String name,
            Integer size,
            String interval,
            List<MetricAggregation<F>> metrics
    ) {
        this.field = field;
        this.type = type;
        this.name = name;
        this.size = size;
        this.interval = interval;
        this.metrics = metrics == null ? List.of() : List.copyOf(metrics);
    }

    /**
     * Terms 버킷 - 카테고리별 그룹핑
     * <p>
     * 예: 회사별, 기술스택별, 경력레벨별 등
     *
     * @param field   그룹핑 기준 필드
     * @param name    집계 이름
     * @param size    상위 N개 버킷만 반환
     * @param metrics 각 버킷별 계산할 메트릭들
     */
    public static <F extends FieldName> BucketAggregation<F> terms(
            F field,
            String name,
            int size,
            List<MetricAggregation<F>> metrics
    ) {
        return new BucketAggregation<>(field, BucketType.TERMS, name, size, null, metrics);
    }

    /**
     * Date Histogram 버킷 - 시간대별 그룹핑
     *
     * @param field    날짜 필드
     * @param name     집계 이름
     * @param interval 시간 간격 (예: "1d", "1w", "1M")
     * @param metrics  각 버킷별 계산할 메트릭들
     */
    public static <F extends FieldName> BucketAggregation<F> dateHistogram(
            F field,
            String name,
            String interval,
            List<MetricAggregation<F>> metrics
    ) {
        return new BucketAggregation<>(field, BucketType.DATE_HISTOGRAM, name, null, interval, metrics);
    }
}
