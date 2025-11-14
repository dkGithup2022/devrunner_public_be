package dev.devrunner.search.job.dto;

import dev.devrunner.elasticsearch.api.job.JobSearchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Job search response DTO
 */
@Schema(description = "Job search response")
@Getter
@AllArgsConstructor
public class JobSearchResponse {
    @Schema(description = "List of job cards")
    private final List<JobCard> jobs;

    @Schema(description = "Whether next page exists", example = "true")
    private final boolean hasNext;

    @Schema(description = "Number of results in current page", example = "30")
    private final int count;

    @Schema(description = "Total number of results matching the query", example = "150")
    private final long totalHits;

    public static JobSearchResponse from(JobSearchResult result) {
        List<JobCard> cards = result.docs().stream()
            .map(JobCard::from)
            .toList();

        return new JobSearchResponse(
            cards,
            result.hasNext(),
            cards.size(),
            result.totalHits()
        );
    }
}
