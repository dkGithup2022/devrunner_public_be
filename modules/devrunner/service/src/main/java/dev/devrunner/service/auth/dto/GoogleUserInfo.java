package dev.devrunner.service.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Google UserInfo API 응답 DTO
 *
 * GET https://www.googleapis.com/oauth2/v3/userinfo
 */
@Getter
@NoArgsConstructor
public class GoogleUserInfo {

    @JsonProperty("sub")
    private String googleId;  // Google 사용자 고유 ID

    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    private String name;

    private String picture;  // 프로필 이미지 URL

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;
}
