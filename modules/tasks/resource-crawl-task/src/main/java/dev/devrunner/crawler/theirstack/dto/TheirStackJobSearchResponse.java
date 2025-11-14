package dev.devrunner.crawler.theirstack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TheirStack Job Search API 응답
 */
@Getter
@Setter
public class TheirStackJobSearchResponse {

    private Metadata metadata;
    private List<JobData> data;

    @Getter
    @Setter
    public static class Metadata {
        @JsonProperty("total_results")
        private Integer totalResults;

        @JsonProperty("truncated_results")
        private Integer truncatedResults;

        @JsonProperty("truncated_companies")
        private Integer truncatedCompanies;

        @JsonProperty("total_companies")
        private Integer totalCompanies;
    }
}
