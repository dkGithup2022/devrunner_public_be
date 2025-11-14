package dev.devrunner.search.techblog;

import dev.devrunner.elasticsearch.agg.*;
import dev.devrunner.elasticsearch.api.techblog.TechBlogAggregator;
import dev.devrunner.elasticsearch.document.fieldSpec.techblog.TechBlogIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.search.techblog.dto.TechBlogDailyStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TechBlog Statistics API Controller
 */
@Tag(name = "TechBlog Statistics", description = "기술 블로그 통계 API")
@RestController
@RequestMapping("/api/techblogs/stats")
@RequiredArgsConstructor
@Slf4j
public class TechBlogStatsController {

    private final TechBlogAggregator techBlogAggregator;

    /**
     * Get daily techblog statistics
     * - Top 10 companies by blog count
     * - Total blog count
     * - Recent 7 days trend
     *
     * @return daily statistics
     */
    @Operation(summary = "일일 기술 블로그 통계 조회", description = "회사별 Top 10, 전체 수, 최근 7일 트렌드")
    @GetMapping("/daily")
    public ResponseEntity<TechBlogDailyStatsResponse> getDailyStats() {
        log.info("📊 Fetching daily techblog statistics");

        var now = LocalDateTime.now();
        var tomorrow = now.plusDays(1);
        var oneWeekAgo = now.minusDays(7);

        // Build aggregation requests
        var requests = List.of(
                // 1. Top 10 companies
                AggregationRequest.terms(
                        new SearchCommand<>(List.of(
                                new SearchElement<>(TechBlogIndexField.DELETED, false)
                        ), 0, 0),
                        "by_company",
                        TechBlogIndexField.COMPANY,
                        10,
                        List.of(MetricAggregation.count("count"))
                ),

                // 2. Total count
                AggregationRequest.terms(
                        new SearchCommand<>(List.of(
                                new SearchElement<>(TechBlogIndexField.DELETED, false)
                        ), 0, 0),
                        "total_count",
                        TechBlogIndexField.COMPANY,  // dummy field for bucket
                        1,
                        List.of(MetricAggregation.count("count"))
                ),

                // 3. Recent 7 days trend
                AggregationRequest.dateHistogram(
                        new SearchCommand<>(List.of(
                                new SearchElement<>(TechBlogIndexField.DELETED, false),
                                new SearchElement<>(TechBlogIndexField.CREATED_AT, oneWeekAgo, tomorrow)
                        ), 0, 0),
                        "recent_trend",
                        TechBlogIndexField.CREATED_AT,
                        "1d",
                        List.of(MetricAggregation.count("count"))
                )
        );

        // Execute aggregation
        MultiAggregationResult result = techBlogAggregator.aggregate(requests);

        // Convert to response DTO
        var response = new TechBlogDailyStatsResponse(
                // Top companies
                result.results().get("by_company").bucket().entries().stream()
                        .map(e -> new TechBlogDailyStatsResponse.CompanyStat(e.key(), e.docCount()))
                        .toList(),

                // Total count
                getTotalCount(result, "total_count"),

                // Recent trend
                result.results().get("recent_trend").bucket().entries().stream()
                        .map(e -> new TechBlogDailyStatsResponse.DailyStat(e.key(), e.docCount()))
                        .toList()
        );

        log.info("✅ Daily stats fetched: {} companies, {} total, {} days trend",
                response.getTopCompanies().size(),
                response.getTotalCount(),
                response.getRecentTrend().size());


        log.info("stat response: {}", response);

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
