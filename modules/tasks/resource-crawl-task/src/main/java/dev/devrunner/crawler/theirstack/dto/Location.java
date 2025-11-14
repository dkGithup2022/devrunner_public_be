package dev.devrunner.crawler.theirstack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * TheirStack Location 정보
 */
@Getter
@Setter
public class Location {

    private Long id;

    private String name;

    private String type;

    @JsonProperty("feature_code")
    private String featureCode;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("admin1_name")
    private String admin1Name;

    @JsonProperty("admin1_code")
    private String admin1Code;

    @JsonProperty("admin2_name")
    private String admin2Name;

    @JsonProperty("admin2_code")
    private String admin2Code;

    private String continent;

    private Double latitude;

    private Double longitude;

    private String city;

    private String address;

    @JsonProperty("postal_code")
    private String postalCode;

    private String state;

    @JsonProperty("state_code")
    private String stateCode;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("country_name")
    private String countryName;
}
