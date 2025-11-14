package dev.devrunner.service.auth.client;

import dev.devrunner.service.auth.client.dto.GoogleUserInfo;
import dev.devrunner.exception.auth.InvalidAccessTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Google OAuth2 API 클라이언트
 *
 * Access Token을 사용하여 Google 사용자 정보를 조회합니다.
 *
 * TODO: RestTemplate 의존성 추가 후 구현 활성화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuthClient {

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    // TODO: RestTemplate 의존성 추가 필요
    private final RestTemplate restTemplate;

    /**
     * Google Access Token으로 사용자 정보 조회
     *
     * @param accessToken Google OAuth Access Token
     * @return Google 사용자 정보
     * @throws InvalidAccessTokenException 토큰이 유효하지 않은 경우
     */
    public GoogleUserInfo getUserInfo(String accessToken) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    GOOGLE_USERINFO_URL,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            log.info("Google API raw response: {}", rawResponse.getBody());

            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                GOOGLE_USERINFO_URL,
                HttpMethod.GET,
                request,
                GoogleUserInfo.class
            );

            GoogleUserInfo userInfo = response.getBody();

            if (userInfo == null) {
                throw InvalidAccessTokenException.create();
            }

            log.debug("Google user info retrieved: email={}", userInfo.getEmail());

            return userInfo;

        } catch (HttpClientErrorException e) {
            log.warn("Failed to get Google user info: status={}, message={}",
                     e.getStatusCode(), e.getMessage());
            throw InvalidAccessTokenException.withCause(e);
        } catch (Exception e) {
            log.error("Unexpected error while calling Google API", e);
            throw InvalidAccessTokenException.withCause(e);
        }

    }
}
