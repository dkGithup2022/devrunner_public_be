package dev.devrunner.crawler.theirstack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TheirStack Company 정보
 */
@Getter
@Setter
public class CompanyObject {

    private String id;

    private String name;

    private String domain;

    private String industry;

    private String country;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("employee_count")
    private Integer employeeCount;

    private String logo;

    @JsonProperty("num_jobs")
    private Integer numJobs;

    @JsonProperty("num_technologies")
    private Integer numTechnologies;

    @JsonProperty("possible_domains")
    private List<String> possibleDomains;

    private String url;

    @JsonProperty("industry_id")
    private Integer industryId;

    @JsonProperty("linkedin_url")
    private String linkedinUrl;

    @JsonProperty("num_jobs_last_30_days")
    private Integer numJobsLast30Days;

    @JsonProperty("num_jobs_found")
    private Integer numJobsFound;

    @JsonProperty("yc_batch")
    private String ycBatch;

    @JsonProperty("apollo_id")
    private String apolloId;

    @JsonProperty("linkedin_id")
    private String linkedinId;

    @JsonProperty("url_source")
    private String urlSource;

    @JsonProperty("is_recruiting_agency")
    private Boolean isRecruitingAgency;

    @JsonProperty("founded_year")
    private Integer foundedYear;

    @JsonProperty("annual_revenue_usd")
    private Long annualRevenueUsd;

    @JsonProperty("annual_revenue_usd_readable")
    private String annualRevenueUsdReadable;

    @JsonProperty("total_funding_usd")
    private Long totalFundingUsd;

    @JsonProperty("last_funding_round_date")
    private String lastFundingRoundDate;

    @JsonProperty("last_funding_round_amount_readable")
    private String lastFundingRoundAmountReadable;

    @JsonProperty("employee_count_range")
    private String employeeCountRange;

    @JsonProperty("long_description")
    private String longDescription;

    @JsonProperty("seo_description")
    private String seoDescription;

    private String city;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("company_keywords")
    private List<String> companyKeywords;

    @JsonProperty("alexa_ranking")
    private Integer alexaRanking;

    @JsonProperty("publicly_traded_symbol")
    private String publiclyTradedSymbol;

    @JsonProperty("publicly_traded_exchange")
    private String publiclyTradedExchange;

    private List<String> investors;

    @JsonProperty("funding_stage")
    private String fundingStage;

    @JsonProperty("has_blurred_data")
    private Boolean hasBlurredData;

    @JsonProperty("technology_slugs")
    private List<String> technologySlugs;

    @JsonProperty("technology_names")
    private List<String> technologyNames;
}
