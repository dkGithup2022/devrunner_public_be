package dev.devrunner.crawler.theirstack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TheirStack Job 데이터
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobData {

    private Long id;

    @JsonProperty("job_title")
    private String jobTitle;

    private String url;

    @JsonProperty("date_posted")
    private String datePosted;

    @JsonProperty("has_blurred_data")
    private Boolean hasBlurredData;

    private String company;

    @JsonProperty("final_url")
    private String finalUrl;

    @JsonProperty("source_url")
    private String sourceUrl;

    private String location;

    @JsonProperty("short_location")
    private String shortLocation;

    @JsonProperty("long_location")
    private String longLocation;

    @JsonProperty("state_code")
    private String stateCode;

    private Double latitude;

    private Double longitude;

    @JsonProperty("postal_code")
    private String postalCode;

    private Boolean remote;

    private Boolean hybrid;

    @JsonProperty("salary_string")
    private String salaryString;

    @JsonProperty("min_annual_salary")
    private Integer minAnnualSalary;

    @JsonProperty("min_annual_salary_usd")
    private Integer minAnnualSalaryUsd;

    @JsonProperty("max_annual_salary")
    private Integer maxAnnualSalary;

    @JsonProperty("max_annual_salary_usd")
    private Integer maxAnnualSalaryUsd;

    @JsonProperty("avg_annual_salary_usd")
    private Integer avgAnnualSalaryUsd;

    @JsonProperty("salary_currency")
    private String salaryCurrency;

    private List<String> countries;

    private String country;

    @JsonProperty("country_codes")
    private List<String> countryCodes;

    @JsonProperty("country_code")
    private String countryCode;

    private List<String> cities;

    private List<String> continents;

    private String seniority;

    @JsonProperty("discovered_at")
    private String discoveredAt;

    @JsonProperty("company_domain")
    private String companyDomain;

    private Boolean reposted;

    @JsonProperty("date_reposted")
    private String dateReposted;

    @JsonProperty("employment_statuses")
    private List<String> employmentStatuses;

    @JsonProperty("easy_apply")
    private Boolean easyApply;

    @JsonProperty("technology_slugs")
    private List<String> technologySlugs;

    private String description;

    @JsonProperty("company_object")
    private CompanyObject companyObject;

    private List<Location> locations;

    @JsonProperty("normalized_title")
    private String normalizedTitle;

    @JsonProperty("manager_roles")
    private List<String> managerRoles;

    @JsonProperty("matching_phrases")
    private List<String> matchingPhrases;

    @JsonProperty("matching_words")
    private List<String> matchingWords;
}
