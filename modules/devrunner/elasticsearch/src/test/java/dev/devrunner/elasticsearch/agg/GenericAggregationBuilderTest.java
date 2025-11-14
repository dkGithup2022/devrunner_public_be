package dev.devrunner.elasticsearch.agg;

import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.query.job.JobIndexQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.query.job.JobIndexRangeQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static dev.devrunner.elasticsearch.testutil.ElasticsearchQueryTestHelper.printMap;

/**
 * GenericAggregationBuilder 테스트
 * <p>
 * Elasticsearch 집계 쿼리 JSON 생성을 검증합니다.
 */
@DisplayName("GenericAggregationBuilder 테스트")
class GenericAggregationBuilderTest {

    @Test
    @DisplayName("TERMS 버킷 집계 - 조건 없음")
    void build_termsAggregation_withoutConditions() {
        // Given
        var query = AggregationQuery.bucket(
            "company_stats",
            List.of(),  // 조건 없음
            BucketAggregation.terms(
                JobIndexField.COMPANY,
                "company_bucket",
                10,
                List.of(
                    MetricAggregation.count("job_count"),
                    MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views")
                )
            )
        );

        var command = MultiAggregationCommand.of(List.of(query));

        // When
        Map<String, Object> aggs = GenericAggregationBuilder.build(
            command,
            JobIndexQueryBuilderRegistry.LOOKUP,
            JobIndexRangeQueryBuilderRegistry.LOOKUP
        );

        // Then - 첫 실행 시 비어있음, 출력된 JSON을 복사해서 채우기
        String expectedJson = """
            {
                             "company_stats": {
                               "terms": {
                                 "field": "company",
                                 "size": 10
                               },
                               "aggs": {
                                 "job_count": {
                                   "value_count": {
                                     "field": "_index"
                                   }
                                 },
                                 "avg_views": {
                                   "avg": {
                                     "field": "popularity_view_count"
                                   }
                                 }
                               }
                             }
                           }
        """;

       printMap("build_termsAggregation_withoutConditions", aggs);
    }

    @Test
    @DisplayName("TERMS 버킷 집계 - 필터 조건 포함")
    void build_termsAggregation_withFilterConditions() {
        // Given - META 회사의 포지션별 통계
        var conditions = List.of(
            new SearchElement<>(JobIndexField.COMPANY, "META"),
            new SearchElement<>(JobIndexField.DELETED, false)
        );

        var query = AggregationQuery.bucket(
            "meta_by_position",
            conditions,
            BucketAggregation.terms(
                JobIndexField.POSITION_CATEGORY,
                "position_bucket",
                5,
                List.of(MetricAggregation.count("count"))
            )
        );

        var command = MultiAggregationCommand.of(List.of(query));

        // When
        Map<String, Object> aggs = GenericAggregationBuilder.build(
            command,
            JobIndexQueryBuilderRegistry.LOOKUP,
            JobIndexRangeQueryBuilderRegistry.LOOKUP
        );

        // Then
        printMap("build_termsAggregation_withFilterConditions", aggs);
    }

    @Test
    @DisplayName("DATE_HISTOGRAM 버킷 집계")
    void build_dateHistogramAggregation() {
        // Given
        var query = AggregationQuery.bucket(
            "daily_trend",
            List.of(),
            BucketAggregation.dateHistogram(
                JobIndexField.CREATED_AT,
                "daily_bucket",
                "1d",
                List.of(MetricAggregation.count("daily_count"))
            )
        );

        var command = MultiAggregationCommand.of(List.of(query));

        // When
        Map<String, Object> aggs = GenericAggregationBuilder.build(
            command,
            JobIndexQueryBuilderRegistry.LOOKUP,
            JobIndexRangeQueryBuilderRegistry.LOOKUP
        );

        // Then
        printMap("build_dateHistogramAggregation", aggs);
    }

    @Test
    @DisplayName("메트릭만 있는 집계 (global aggregation)")
    void build_metricsOnly() {
        // Given
        var query = AggregationQuery.metrics(
            "global_metrics",
            List.of(),
            List.of(
                MetricAggregation.count("total"),
                MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views"),
                MetricAggregation.sum(JobIndexField.POPULARITY_VIEW_COUNT, "total_views"),
                MetricAggregation.min(JobIndexField.MIN_YEARS, "min_experience"),
                MetricAggregation.max(JobIndexField.MAX_YEARS, "max_experience")
            )
        );

        var command = MultiAggregationCommand.of(List.of(query));

        // When
        Map<String, Object> aggs = GenericAggregationBuilder.build(
            command,
            JobIndexQueryBuilderRegistry.LOOKUP,
            JobIndexRangeQueryBuilderRegistry.LOOKUP
        );

        // Then
        printMap("build_metricsOnly", aggs);
    }

    @Test
    @DisplayName("여러 집계를 동시에 실행 (Multi-Aggregation)")
    void build_multipleAggregations() {
        // Given - 전체 통계 + 최근 7일 통계 + 회사별 통계
        var now = LocalDateTime.of(2024, 10, 22, 0, 0);
        var sevenDaysAgo = now.minusDays(7);

        var command = MultiAggregationCommand.of(List.of(
            // 1. 전체 통계
            AggregationQuery.metrics(
                "overall_stats",
                List.of(),
                List.of(
                    MetricAggregation.count("total_jobs"),
                    MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views")
                )
            ),

            // 2. 최근 7일 통계
            AggregationQuery.metrics(
                "recent_stats",
                List.of(
                    new SearchElement<>(JobIndexField.CREATED_AT, sevenDaysAgo, now)
                ),
                List.of(MetricAggregation.count("recent_jobs"))
            ),

            // 3. 회사별 통계
            AggregationQuery.bucket(
                "top_companies",
                List.of(),
                BucketAggregation.terms(
                    JobIndexField.COMPANY,
                    "companies",
                    5,
                    List.of(MetricAggregation.count("count"))
                )
            )
        ));

        // When
        Map<String, Object> aggs = GenericAggregationBuilder.build(
            command,
            JobIndexQueryBuilderRegistry.LOOKUP,
            JobIndexRangeQueryBuilderRegistry.LOOKUP
        );

        // Then
        printMap("build_multipleAggregations", aggs);
    }

    @Test
    @DisplayName("복잡한 필터 조건 + DATE_HISTOGRAM")
    void build_complexFilterWithDateHistogram() {
        // Given - 삭제되지 않은 META의 백엔드 포지션, 최근 30일 일별 추이
        var now = LocalDateTime.of(2024, 10, 22, 0, 0);
        var thirtyDaysAgo = now.minusDays(30);

        var conditions = List.of(
            new SearchElement<>(JobIndexField.COMPANY, "META"),
            new SearchElement<>(JobIndexField.POSITION_CATEGORY, "Backend"),
            new SearchElement<>(JobIndexField.DELETED, false),
            new SearchElement<>(JobIndexField.CREATED_AT, thirtyDaysAgo, now)
        );

        var query = AggregationQuery.bucket(
            "meta_backend_trend",
            conditions,
            BucketAggregation.dateHistogram(
                JobIndexField.CREATED_AT,
                "daily",
                "1d",
                List.of(
                    MetricAggregation.count("jobs_count"),
                    MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views")
                )
            )
        );

        var command = MultiAggregationCommand.of(List.of(query));

        // When
        Map<String, Object> aggs = GenericAggregationBuilder.build(
            command,
            JobIndexQueryBuilderRegistry.LOOKUP,
            JobIndexRangeQueryBuilderRegistry.LOOKUP
        );

        // Then
        printMap("build_complexFilterWithDateHistogram", aggs);
    }
}
