package dev.devrunner.elasticsearch.document;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.job.CareerLevel;
import dev.devrunner.model.job.EmploymentType;
import dev.devrunner.model.job.PositionCategory;
import dev.devrunner.model.job.RemotePolicy;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * JobDoc - Elasticsearch document for Job search
 * <p>
 * Flattened structure for optimal Elasticsearch indexing and querying.
 * All nested objects are flattened to simple fields with prefixes.
 */
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDoc implements DocBase {

    // ES Type: keyword (used as document ID)
    @JsonProperty("doc_id")
    String docId;

    // ES Type: long
    @JsonProperty("job_id")
    Long jobId;

    // ES Type: keyword
    @JsonProperty("url")
    String url;

    // ES Type: keyword
    @JsonProperty("company")
    Company company;

    // ES Type: text + keyword (multi-field for full-text search and exact match)
    @JsonProperty("title")
    String title;

    // ES Type: keyword
    @JsonProperty("organization")
    String organization;

    // ES Type: text
    @JsonProperty("one_line_summary")
    String oneLineSummary;

    // Experience fields (flattened from ExperienceRequirement)
    // ES Type: integer
    @JsonProperty("min_years")
    Integer minYears;

    // ES Type: integer
    @JsonProperty("max_years")
    Integer maxYears;

    // ES Type: boolean
    @JsonProperty("experience_required")
    Boolean experienceRequired;

    // ES Type: keyword
    @JsonProperty("career_level")
    CareerLevel careerLevel;

    // Job type fields
    // ES Type: keyword
    @JsonProperty("employment_type")
    EmploymentType employmentType;

    // ES Type: keyword
    @JsonProperty("position_category")
    PositionCategory positionCategory;

    // ES Type: keyword
    @JsonProperty("remote_policy")
    RemotePolicy remotePolicy;

    // ES Type: keyword (array)
    @JsonProperty("tech_categories")
    List<String> techCategories;


    // Date fields
    // ES Type: date (epoch_millis)
    @JsonProperty("started_at")
    Long startedAt;

    // ES Type: date (epoch_millis)
    @JsonProperty("ended_at")
    Long endedAt;

    // ES Type: boolean
    @JsonProperty("is_open_ended")
    Boolean isOpenEnded;

    // ES Type: boolean
    @JsonProperty("is_closed")
    Boolean isClosed;

    // ES Type: keyword (array)
    @JsonProperty("locations")
    List<String> locations;

    // Description (full text search)
    // ES Type: text
    @JsonProperty("full_description")
    String fullDescription;

    // Interview process fields (flattened from InterviewProcess)
    // ES Type: boolean
    @JsonProperty("has_assignment")
    Boolean hasAssignment;

    // ES Type: boolean
    @JsonProperty("has_coding_test")
    Boolean hasCodingTest;

    // ES Type: boolean
    @JsonProperty("has_live_coding")
    Boolean hasLiveCoding;

    // ES Type: integer
    @JsonProperty("interview_count")
    Integer interviewCount;

    // ES Type: integer
    @JsonProperty("interview_days")
    Integer interviewDays;

    // Compensation fields (flattened from JobCompensation)
    // ES Type: double
    @JsonProperty("compensation_min_base_pay")
    BigDecimal compensationMinBasePay;

    // ES Type: double
    @JsonProperty("compensation_max_base_pay")
    BigDecimal compensationMaxBasePay;

    // ES Type: keyword
    @JsonProperty("compensation_currency")
    String compensationCurrency;

    // ES Type: keyword
    @JsonProperty("compensation_unit")
    String compensationUnit;

    // ES Type: boolean
    @JsonProperty("compensation_has_stock_option")
    Boolean compensationHasStockOption;

    // Popularity fields (flattened from Popularity)
    // ES Type: long
    @JsonProperty("popularity_view_count")
    Long popularityViewCount;

    // ES Type: long
    @JsonProperty("popularity_comment_count")
    Long popularityCommentCount;

    // ES Type: long
    @JsonProperty("popularity_like_count")
    Long popularityLikeCount;

    // Meta fields
    // ES Type: boolean
    @JsonProperty("deleted")
    Boolean deleted;

    // ES Type: date (epoch_millis)
    @JsonProperty("created_at")
    Long createdAt;

    // ES Type: date (epoch_millis)
    @JsonProperty("updated_at")
    Long updatedAt;

    // ES Type: dense_vector
    @JsonProperty("vector")
    List<Float> vector;

    public static String docId(Long articleId) {
        return "job_" + articleId;
    }

    public static String docId(String articleId) {
        if (articleId == null|| articleId.isEmpty()) {
            throw new IllegalArgumentException("Article ID cannot be null or empty");
        }
        return "job_" + articleId;
    }

}
