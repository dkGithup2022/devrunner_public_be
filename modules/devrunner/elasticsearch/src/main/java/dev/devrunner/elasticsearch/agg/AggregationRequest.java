package dev.devrunner.elasticsearch.agg;

import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;

import java.util.List;

/**
 * 집계 요청 DTO
 * <p>
 * SearchCommand와 집계 설정을 포함하여 일반적인 집계 요청을 표현합니다.
 *
 * @param searchCommand 검색 조건 (필터로 사용됨)
 * @param queryName     집계 쿼리 이름 (결과 구분용)
 * @param bucketField   버킷 집계할 필드 (예: COMPANY, CREATED_AT)
 * @param bucketType    버킷 타입 (TERMS, DATE_HISTOGRAM 등)
 * @param size          버킷 크기 (optional, TERMS에서 사용)
 * @param interval      날짜 간격 (optional, DATE_HISTOGRAM에서 사용, 예: "1d", "1w")
 * @param metrics       버킷 내에서 계산할 메트릭 리스트
 */
public record AggregationRequest<F extends FieldName>(
        SearchCommand<F> searchCommand,
        String queryName,
        F bucketField,
        BucketType bucketType,
        Integer size,
        String interval,
        List<MetricAggregation<F>> metrics
) {
    public AggregationRequest {
        if (queryName == null || queryName.isBlank()) {
            throw new IllegalArgumentException("queryName is required");
        }
        if (bucketField == null) {
            throw new IllegalArgumentException("bucketField is required");
        }
        if (bucketType == null) {
            throw new IllegalArgumentException("bucketType is required");
        }
        metrics = (metrics == null) ? List.of() : List.copyOf(metrics);
    }

    /**
     * TERMS 버킷 집계 요청 생성
     *
     * @param searchCommand 검색 조건
     * @param queryName     쿼리 이름
     * @param bucketField   집계할 필드
     * @param size          버킷 크기 (top N)
     * @param metrics       메트릭 리스트
     */
    public static <F extends FieldName> AggregationRequest<F> terms(
            SearchCommand<F> searchCommand,
            String queryName,
            F bucketField,
            int size,
            List<MetricAggregation<F>> metrics
    ) {
        return new AggregationRequest<>(
                searchCommand,
                queryName,
                bucketField,
                BucketType.TERMS,
                size,
                null,
                metrics
        );
    }

    /**
     * DATE_HISTOGRAM 버킷 집계 요청 생성
     *
     * @param searchCommand 검색 조건
     * @param queryName     쿼리 이름
     * @param bucketField   집계할 날짜 필드
     * @param interval      간격 (예: "1d", "1w", "1M")
     * @param metrics       메트릭 리스트
     */
    public static <F extends FieldName> AggregationRequest<F> dateHistogram(
            SearchCommand<F> searchCommand,
            String queryName,
            F bucketField,
            String interval,
            List<MetricAggregation<F>> metrics
    ) {
        return new AggregationRequest<>(
                searchCommand,
                queryName,
                bucketField,
                BucketType.DATE_HISTOGRAM,
                null,
                interval,
                metrics
        );
    }
}
