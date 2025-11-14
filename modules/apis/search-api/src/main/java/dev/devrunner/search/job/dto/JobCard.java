package dev.devrunner.search.job.dto;

import dev.devrunner.elasticsearch.document.JobDoc;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.job.CareerLevel;
import dev.devrunner.model.job.EmploymentType;
import dev.devrunner.model.job.PositionCategory;
import dev.devrunner.model.job.RemotePolicy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static dev.devrunner.elasticsearch.util.InstantConverter.fromEpochMillis;

/**
 * Job search result card DTO
 *
 * Contains only essential information to display in search result list
 */
@Schema(description = "Job search result card")
public record JobCard(
    @Schema(description = "Job ID", example = "1")
    Long jobId,

    @Schema(description = "Job posting URL", example = "https://careers.google.com/jobs/1")
    String url,

    @Schema(description = "Company", example = "GOOGLE")
    Company company,

    @Schema(description = "Job title", example = "Senior Backend Engineer")
    String title,

    @Schema(description = "Organization/Team", example = "Search Infrastructure")
    String organization,

    @Schema(description = "One-line summary", example = "Join our team to build scalable backend systems")
    String oneLineSummary,

    @Schema(description = "Minimum years of experience", example = "3")
    Integer minYears,

    @Schema(description = "Maximum years of experience", example = "7")
    Integer maxYears,

    @Schema(description = "Career level", example = "EXPERIENCED")
    CareerLevel careerLevel,

    @Schema(description = "Employment type", example = "FULL_TIME")
    EmploymentType employmentType,

    @Schema(description = "Position category", example = "BACKEND")
    PositionCategory positionCategory,

    @Schema(description = "Remote work policy", example = "HYBRID")
    RemotePolicy remotePolicy,

    @Schema(description = "Tech stack", example = "[\"Java\", \"Kubernetes\", \"MySQL\"]")
    List<String> techCategories,

    @Schema(description = "Work locations", example = "[\"Seoul\", \"Pangyo\"]")
    List<String> locations,

    @Schema(description = "Minimum base salary", example = "80000000")
    BigDecimal compensationMinBasePay,

    @Schema(description = "Maximum base salary", example = "120000000")
    BigDecimal compensationMaxBasePay,

    @Schema(description = "Currency code", example = "KRW")
    String compensationCurrency,

    @Schema(description = "Salary payment unit", example = "YEARLY")
    String compensationUnit,

    @Schema(description = "Stock option availability", example = "true")
    Boolean compensationHasStockOption,

    @Schema(description = "Job posting start date")
    Instant startedAt,

    @Schema(description = "Job posting end date")
    Instant endedAt,

    @Schema(description = "Open-ended position (no deadline)", example = "false")
    Boolean isOpenEnded,

    @Schema(description = "Position closed", example = "false")
    Boolean isClosed,

    @Schema(description = "View count", example = "1234")
    Long viewCount,

    @Schema(description = "Comment count", example = "56")
    Long commentCount,

    @Schema(description = "Like count", example = "89")
    Long likeCount,

    @Schema(description = "Created timestamp")
    Instant createdAt,

    @Schema(description = "Last updated timestamp")
    Instant updatedAt
) {
    /**
     * Create JobCard from JobDoc
     */
    public static JobCard from(JobDoc doc) {
        return new JobCard(
            doc.getJobId(),
            doc.getUrl(),
            doc.getCompany(),
            doc.getTitle(),
            doc.getOrganization(),
            doc.getOneLineSummary(),
            doc.getMinYears(),
            doc.getMaxYears(),
            doc.getCareerLevel(),
            doc.getEmploymentType(),
            doc.getPositionCategory(),
            doc.getRemotePolicy(),
            doc.getTechCategories(),
            doc.getLocations(),
            doc.getCompensationMinBasePay(),
            doc.getCompensationMaxBasePay(),
            doc.getCompensationCurrency(),
            doc.getCompensationUnit(),
            doc.getCompensationHasStockOption(),
                fromEpochMillis(doc.getStartedAt()),
                fromEpochMillis(doc.getEndedAt()),
            doc.getIsOpenEnded(),
            doc.getIsClosed(),
            doc.getPopularityViewCount(),
            doc.getPopularityCommentCount(),
            doc.getPopularityLikeCount(),
                fromEpochMillis(doc.getCreatedAt()),
                fromEpochMillis(doc.getUpdatedAt())
        );
    }
}
