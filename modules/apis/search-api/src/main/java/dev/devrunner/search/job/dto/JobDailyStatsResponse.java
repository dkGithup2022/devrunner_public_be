package dev.devrunner.search.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Job daily statistics response DTO
 */
@Schema(description = "일일 채용 통계 응답")
@Getter
@AllArgsConstructor
public class JobDailyStatsResponse {

    @Schema(description = "회사별 채용 공고 수 (Top 10)")
    private final List<CompanyStat> topCompanies;

    @Schema(description = "분야별 채용 공고 수 (전체)")
    private final List<PositionCategoryStat> topPositionCategories;

    @Schema(description = "주니어 포지션 수 (경력 0-3년)", example = "48")
    private final long juniorCount;

    @Schema(description = "시니어 포지션 수 (경력 5년 이상)", example = "20")
    private final long seniorCount;

    @Schema(description = "경력 필요 주니어 포지션 수 (경력 필요 AND 경력 0-2년)", example = "35")
    private final long experienceRequiredJuniorCount;

    @Schema(description = "경력 불필요 포지션 수 (신입 가능)", example = "15")
    private final long noExperienceRequiredCount;

    @Schema(description = "최근 7일 일별 채용 추이")
    private final List<DailyStat> recentTrend;

    /**
     * 회사별 통계
     */
    @Schema(description = "회사별 통계")
    @Getter
    @AllArgsConstructor
    public static class CompanyStat {
        @Schema(description = "회사명", example = "Google")
        private final String company;

        @Schema(description = "채용 공고 수", example = "27")
        private final long count;
    }

    /**
     * 분야별 통계
     */
    @Schema(description = "분야별 통계")
    @Getter
    @AllArgsConstructor
    public static class PositionCategoryStat {
        @Schema(description = "직무 분야", example = "Backend")
        private final String positionCategory;

        @Schema(description = "채용 공고 수", example = "42")
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

        @Schema(description = "채용 공고 수", example = "71")
        private final long count;
    }
}
