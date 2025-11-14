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
 * 실제 사용 사례를 기반으로 한 집계 테스트
 */
@DisplayName("MultiAggregation 실제 사용 사례 테스트")
class MultiAggregationUseCaseTest {

    @Test
    @DisplayName("4가지 집계를 한 번에 실행 - 회사별/주니어/시니어/최근")
    void aggregateMultiple_realWorldScenario() {
        // Given
        var now = LocalDateTime.now();
        var oneWeekAgo = now.minusDays(7);

        var command = MultiAggregationCommand.of(List.of(
                // 1. 회사명별 집계 (조건 없음 - 전체)
                AggregationQuery.bucket(
                        "by_company",
                        List.of(),
                        BucketAggregation.terms(
                                JobIndexField.COMPANY,
                                "company_bucket",
                                10,
                                List.of(
                                        MetricAggregation.count("job_count")
                                )
                        )
                ),

                // 2. 주니어 포지션 집계 (minYears < 2 또는 experience = entry)
                // 참고: OR 조건은 여러 집계로 분리하여 클라이언트에서 합산
                AggregationQuery.metrics(
                        "junior_positions_by_years",
                        List.of(
                                new SearchElement<>(JobIndexField.EXPERIENCE_REQUIRED, "true"),
                                new SearchElement<>(JobIndexField.MIN_YEARS, null, 2),  // minYears < 2
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        List.of(
                                MetricAggregation.count("count"),
                                MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views")
                        )
                ),

                // 2-1. Entry 레벨 집계 (experience_required = "entry")
                AggregationQuery.metrics(
                        "junior_positions_by_experience",
                        List.of(
                                new SearchElement<>(JobIndexField.EXPERIENCE_REQUIRED, "false"),
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        List.of(
                                MetricAggregation.count("count")
                        )
                ),

                // 3. 시니어 포지션 집계 (minYears >= 5)
                AggregationQuery.bucket(
                        "senior_positions",
                        List.of(
                                new SearchElement<>(JobIndexField.MIN_YEARS, 5, null),  // minYears >= 5
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        BucketAggregation.terms(
                                JobIndexField.POSITION_CATEGORY,
                                "position_bucket",
                                5,
                                List.of(
                                        MetricAggregation.count("count")
                                )
                        )
                ),

                // 4. 최근 일주일 집계 (deleted=false AND created_at >= 7일전)
                AggregationQuery.bucket(
                        "recent_week",
                        List.of(
                                new SearchElement<>(JobIndexField.DELETED, false),
                                new SearchElement<>(JobIndexField.CREATED_AT, oneWeekAgo, now)
                        ),
                        BucketAggregation.dateHistogram(
                                JobIndexField.CREATED_AT,
                                "daily",
                                "1d",
                                List.of(
                                        MetricAggregation.count("daily_count")
                                )
                        )
                )
        ));

        // When
        Map<String, Object> aggs = GenericAggregationBuilder.build(
                command,
                JobIndexQueryBuilderRegistry.LOOKUP,
                JobIndexRangeQueryBuilderRegistry.LOOKUP
        );

        // Then - 첫 실행 시 비어있음
        String expectedJson = "";

        printMap("aggregateMultiple_realWorldScenario", aggs);
    }

    @Test
    @DisplayName("포지션별 통계 - 회사 필터 적용")
    void aggregateByPosition_withCompanyFilter() {
        // Given - META와 Google의 포지션별 통계
        var command = MultiAggregationCommand.of(List.of(
                // META 포지션별
                AggregationQuery.bucket(
                        "meta_positions",
                        List.of(
                                new SearchElement<>(JobIndexField.COMPANY, "META"),
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        BucketAggregation.terms(
                                JobIndexField.POSITION_CATEGORY,
                                "positions",
                                10,
                                List.of(
                                        MetricAggregation.count("count"),
                                        MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views")
                                )
                        )
                ),

                // Google 포지션별
                AggregationQuery.bucket(
                        "google_positions",
                        List.of(
                                new SearchElement<>(JobIndexField.COMPANY, "Google"),
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        BucketAggregation.terms(
                                JobIndexField.POSITION_CATEGORY,
                                "positions",
                                10,
                                List.of(
                                        MetricAggregation.count("count"),
                                        MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views")
                                )
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
        printMap("aggregateByPosition_withCompanyFilter", aggs);
    }

    @Test
    @DisplayName("경력별 통계 - 범위별 집계")
    void aggregateByExperienceRanges() {
        // Given - 경력별로 여러 범위 집계
        var command = MultiAggregationCommand.of(List.of(
                // 신입 (0-2년)
                AggregationQuery.metrics(
                        "entry_level",
                        List.of(
                                new SearchElement<>(JobIndexField.MIN_YEARS, null, 2),
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        List.of(
                                MetricAggregation.count("count"),
                                MetricAggregation.avg(JobIndexField.COMPENSATION_MAX_BASE_PAY, "avg_salary")
                        )
                ),

                // 주니어 (2-5년)
                AggregationQuery.metrics(
                        "junior_level",
                        List.of(
                                new SearchElement<>(JobIndexField.MIN_YEARS, 2, 5),
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        List.of(
                                MetricAggregation.count("count"),
                                MetricAggregation.avg(JobIndexField.COMPENSATION_MAX_BASE_PAY, "avg_salary")
                        )
                ),

                // 시니어 (5년 이상)
                AggregationQuery.metrics(
                        "senior_level",
                        List.of(
                                new SearchElement<>(JobIndexField.MIN_YEARS, 5, null),
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        List.of(
                                MetricAggregation.count("count"),
                                MetricAggregation.avg(JobIndexField.COMPENSATION_MAX_BASE_PAY, "avg_salary"),
                                MetricAggregation.max(JobIndexField.COMPENSATION_MAX_BASE_PAY, "max_salary")
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
        printMap("aggregateByExperienceRanges", aggs);
    }

    @Test
    @DisplayName("최근 30일 트렌드 + 전체 통계")
    void aggregateTrendAndOverallStats() {
        // Given
        var now = LocalDateTime.of(2024, 10, 22, 0, 0);
        var thirtyDaysAgo = now.minusDays(30);

        var command = MultiAggregationCommand.of(List.of(
                // 전체 통계
                AggregationQuery.metrics(
                        "overall_stats",
                        List.of(
                                new SearchElement<>(JobIndexField.DELETED, false)
                        ),
                        List.of(
                                MetricAggregation.count("total_jobs"),
                                MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views"),
                                MetricAggregation.avg(JobIndexField.COMPENSATION_MAX_BASE_PAY, "avg_max_salary")
                        )
                ),

                // 최근 30일 일별 추이
                AggregationQuery.bucket(
                        "recent_trend",
                        List.of(
                                new SearchElement<>(JobIndexField.DELETED, false),
                                new SearchElement<>(JobIndexField.CREATED_AT, thirtyDaysAgo, now)
                        ),
                        BucketAggregation.dateHistogram(
                                JobIndexField.CREATED_AT,
                                "daily",
                                "1d",
                                List.of(
                                        MetricAggregation.count("daily_count"),
                                        MetricAggregation.avg(JobIndexField.POPULARITY_VIEW_COUNT, "avg_views")
                                )
                        )
                ),

                // Top 회사 (최근 30일)
                AggregationQuery.bucket(
                        "top_companies_recent",
                        List.of(
                                new SearchElement<>(JobIndexField.DELETED, false),
                                new SearchElement<>(JobIndexField.CREATED_AT, thirtyDaysAgo, now)
                        ),
                        BucketAggregation.terms(
                                JobIndexField.COMPANY,
                                "companies",
                                10,
                                List.of(
                                        MetricAggregation.count("job_count")
                                )
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
        printMap("aggregateTrendAndOverallStats", aggs);
    }
}
