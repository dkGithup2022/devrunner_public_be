package dev.devrunner.search.job;

import dev.devrunner.elasticsearch.agg.*;
import dev.devrunner.elasticsearch.api.job.JobAggregator;
import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.search.job.dto.JobDailyStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Job Statistics API Controller
 */
@Tag(name = "Job Statistics", description = "채용 공고 통계 API")
@RestController
@RequestMapping("/api/jobs/stats")
@RequiredArgsConstructor
@Slf4j
public class JobStatsController {

    private final JobAggregator jobAggregator;

    /**
     * Get daily job statistics
     * - Top 10 companies by job count
     * - Junior position count (0-3 years)
     * - Senior position count (5+ years)
     * - Experience required junior position count (experience required AND 0-2 years)
     * - No experience required position count (entry level)
     * - Recent 7 days trend
     *
     * @return daily statistics
     */
    @Operation(summary = "일일 채용 통계 조회", description = "회사별 Top 10, 주니어/시니어 수, 경력 필요 여부별 수, 최근 7일 트렌드")
    @GetMapping("/daily")
    public ResponseEntity<JobDailyStatsResponse> getDailyStats() {
        log.info("📊 Fetching daily job statistics");

        var now = Instant.now();
        var oneWeekAgo = now.minus(7, ChronoUnit.DAYS);

        // Build aggregation requests
        var requests = List.of(
            // 1. Top 10 companies
            AggregationRequest.terms(
                new SearchCommand<>(List.of(
                    new SearchElement<>(JobIndexField.DELETED, false),
                    new SearchElement<>(JobIndexField.IS_CLOSED, false)
                ), 0, 0),
                "by_company",
                JobIndexField.COMPANY,
                10,
                List.of(MetricAggregation.count("count"))
            ),

            // 2. All position categories
            AggregationRequest.terms(
                new SearchCommand<>(List.of(
                    new SearchElement<>(JobIndexField.DELETED, false),
                    new SearchElement<>(JobIndexField.IS_CLOSED, false)
                ), 0, 0),
                "by_position_category",
                JobIndexField.POSITION_CATEGORY,
                100,
                List.of(MetricAggregation.count("count"))
            ),

            // 3. Junior positions (0-3 years)
            AggregationRequest.terms(
                new SearchCommand<>(List.of(
                    new SearchElement<>(JobIndexField.MIN_YEARS, null, 3),
                    new SearchElement<>(JobIndexField.DELETED, false),
                    new SearchElement<>(JobIndexField.IS_CLOSED, false)
                ), 0, 0),
                "junior_count",
                JobIndexField.COMPANY,  // dummy field for bucket
                1,
                List.of(MetricAggregation.count("count"))
            ),

            // 4. Senior positions (5+ years)
            AggregationRequest.terms(
                new SearchCommand<>(List.of(
                    new SearchElement<>(JobIndexField.MIN_YEARS, 5, null),
                    new SearchElement<>(JobIndexField.DELETED, false),
                    new SearchElement<>(JobIndexField.IS_CLOSED, false)
                ), 0, 0),
                "senior_count",
                JobIndexField.COMPANY,  // dummy field for bucket
                1,
                List.of(MetricAggregation.count("count"))
            ),

            // 5. Experience required junior positions (experience_required = true AND min_years <= 2)
            AggregationRequest.terms(
                new SearchCommand<>(List.of(
                    new SearchElement<>(JobIndexField.EXPERIENCE_REQUIRED, "true"),
                    new SearchElement<>(JobIndexField.MIN_YEARS, null, 2),
                    new SearchElement<>(JobIndexField.DELETED, false),
                    new SearchElement<>(JobIndexField.IS_CLOSED, false)
                ), 0, 0),
                "experience_required_junior_count",
                JobIndexField.COMPANY,  // dummy field for bucket
                1,
                List.of(MetricAggregation.count("count"))
            ),

            // 6. No experience required positions (experience_required = false)
            AggregationRequest.terms(
                new SearchCommand<>(List.of(
                    new SearchElement<>(JobIndexField.EXPERIENCE_REQUIRED, "false"),
                    new SearchElement<>(JobIndexField.DELETED, false),
                    new SearchElement<>(JobIndexField.IS_CLOSED, false)
                ), 0, 0),
                "no_experience_required_count",
                JobIndexField.COMPANY,  // dummy field for bucket
                1,
                List.of(MetricAggregation.count("count"))
            ),

            // 7. Recent 7 days trend
            AggregationRequest.dateHistogram(
                new SearchCommand<>(List.of(
                    new SearchElement<>(JobIndexField.DELETED, false),
                    new SearchElement<>(JobIndexField.IS_CLOSED, false),
                    new SearchElement<>(JobIndexField.CREATED_AT, oneWeekAgo, now)
                ), 0, 0),
                "recent_trend",
                JobIndexField.CREATED_AT,
                "1d",
                List.of(MetricAggregation.count("count"))
            )
        );

        // Execute aggregation
        MultiAggregationResult result = jobAggregator.aggregate(requests);

        // Convert to response DTO
        var response = new JobDailyStatsResponse(
            // Top companies
            result.results().get("by_company").bucket().entries().stream()
                .map(e -> new JobDailyStatsResponse.CompanyStat(e.key(), e.docCount()))
                .toList(),

            // Top position categories
            result.results().get("by_position_category").bucket().entries().stream()
                .map(e -> new JobDailyStatsResponse.PositionCategoryStat(e.key(), e.docCount()))
                .toList(),

            // Junior count
            getTotalCount(result, "junior_count"),

            // Senior count
            getTotalCount(result, "senior_count"),

            // Experience required junior count
            getTotalCount(result, "experience_required_junior_count"),

            // No experience required count
            getTotalCount(result, "no_experience_required_count"),

            // Recent trend
            result.results().get("recent_trend").bucket().entries().stream()
                .map(e -> new JobDailyStatsResponse.DailyStat(e.key(), e.docCount()))
                .toList()
        );

        log.info("✅ Daily stats fetched: {} companies, {} categories, {} junior, {} senior, {} exp-req-junior, {} no-exp-req, {} days trend",
            response.getTopCompanies().size(),
            response.getTopPositionCategories().size(),
            response.getJuniorCount(),
            response.getSeniorCount(),
            response.getExperienceRequiredJuniorCount(),
            response.getNoExperienceRequiredCount(),
            response.getRecentTrend().size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get total count from filter aggregation result
     * Uses filterDocCount from Filter aggregation's doc_count
     */
    private long getTotalCount(MultiAggregationResult result, String queryName) {
        var queryResult = result.results().get(queryName);
        if (queryResult == null) {
            return 0;
        }

        // ✅ Filter의 doc_count 사용 (전체 카운트)
        if (queryResult.filterDocCount() != null) {
            return queryResult.filterDocCount();
        }

        // Fallback: 버킷 합산 (사용하지 않지만 호환성 유지)
        if (queryResult.bucket() != null) {
            return queryResult.bucket().entries().stream()
                .mapToLong(MultiAggregationResult.BucketEntry::docCount)
                .sum();
        }

        return 0;
    }
}
