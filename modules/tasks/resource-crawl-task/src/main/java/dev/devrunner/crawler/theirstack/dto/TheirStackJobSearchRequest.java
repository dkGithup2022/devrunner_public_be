package dev.devrunner.crawler.theirstack.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * TheirStack Job Search API 요청
 *
 * @see <a href="https://api.theirstack.com/#tag/decision-makers">TheirStack API Documentation</a>
 * <p>
 * 추가 파라미터가 필요한 경우 API 문서를 참고하여 필드를 추가할 수 있습니다.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TheirStackJobSearchRequest {

    public static final String SOFTWARE_ENGINEER_DESCRIPTION_SEARCH_WORD = "(?i)software\\s+engineer";


    /**
     * 정렬 기준 (선택사항)
     * 예: [{"field": "date_posted", "desc": true}]
     */
    @JsonProperty("order_by")
    private List<Map<String, Object>> orderBy;

    /**
     * 페이지 번호 (0부터 시작)
     */
    @JsonProperty("page")
    private Integer page;

    /**
     * 페이지당 결과 개수
     */
    @JsonProperty("limit")
    private Integer limit;

    /**
     * 회사명 필터 (OR 조건)
     */
    @JsonProperty("company_name_or")
    private List<String> companyNameOr;

    /**
     * 최대 게시 기간 (일 단위)
     */
    @JsonProperty("posted_at_max_age_days")
    private Integer postedAtMaxAgeDays;

    /**
     * Job description 정규식 패턴 (OR 조건)
     */
    @JsonProperty("job_description_pattern_or")
    private List<String> jobDescriptionPatternOr;

    /**
     * 원격 근무 필터
     * - true: 원격 근무 공고만 표시
     * - false: 비원격 근무 공고만 표시
     * - null: 모든 공고 표시
     */
    @JsonProperty("remote")
    private Boolean remote;

    /**
     * 회사명 필터 (OR 조건, 대소문자 구분 없음)
     */
    @JsonProperty("company_name_case_insensitive_or")
    private List<String> companyNameCaseInsensitiveOr;

    /**
     * Job 국가 코드 필터 (OR 조건)
     * 예: ["KR", "US"]
     */
    @JsonProperty("job_country_code_or")
    private List<String> jobCountryCodeOr;

    /**
     * Job 타이틀 정규식 패턴 (OR 조건)
     * 예: ["(?i)engineer", "(?i)developer"]
     */
    @JsonProperty("job_title_pattern_or")
    private List<String> jobTitlePatternOr;
}
