package dev.devrunner.search.techblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * TechBlog daily statistics response DTO
 */
@Schema(description = "일일 기술 블로그 통계 응답")
@Getter
@AllArgsConstructor
public class TechBlogDailyStatsResponse {

    @Schema(description = "회사별 블로그 수 (Top 10)")
    private final List<CompanyStat> topCompanies;

    @Schema(description = "전체 블로그 수", example = "1234")
    private final long totalCount;

    @Schema(description = "최근 7일 일별 블로그 추이")
    private final List<DailyStat> recentTrend;

    /**
     * 회사별 통계
     */
    @Schema(description = "회사별 통계")
    @Getter
    @AllArgsConstructor
    public static class CompanyStat {
        @Schema(description = "회사명", example = "Netflix")
        private final String company;

        @Schema(description = "블로그 수", example = "42")
        private final long count;
    }

    /**
     * 일별 통계
     */
    @Schema(description = "일별 통계")
    @Getter
    @AllArgsConstructor
    public static class DailyStat {
        @Schema(description = "날짜 (ISO 8601)", example = "2024-10-20T00:00:00.000Z")
        private final String date;

        @Schema(description = "블로그 수", example = "23")
        private final long count;
    }
}
