package dev.devrunner.crawler.theirstack;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.crawler.theirstack.dto.TheirStackJobSearchRequest;
import dev.devrunner.crawler.theirstack.dto.TheirStackJobSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * TheirStack API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TheirStackApiClient {

    private final RestTemplate theirStackRestTemplate;
    private final ObjectMapper objectMapper;

    @Value("${theirstack.api.key}")
    private String apiKey;

    @Value("${theirstack.api.url:https://api.theirstack.com/v1/jobs/search}")
    private String apiUrl;

    /**
     * Job 검색
     *
     * @param request 검색 요청
     * @return 검색 응답
     */
    public TheirStackJobSearchResponse searchJobs(TheirStackJobSearchRequest request) {
        log.info("Searching jobs via TheirStack API. companies={}, maxAgeDays={}, page={}, limit={}",
                request.getCompanyNameOr(), request.getPostedAtMaxAgeDays(), request.getPage(), request.getLimit());

        try {
            String requestJson = objectMapper.writeValueAsString(request);
            log.debug("Request JSON: {}", requestJson);
        } catch (Exception e) {
            log.warn("Failed to serialize request for logging", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<TheirStackJobSearchRequest> entity = new HttpEntity<>(request, headers);

        try {
            // 먼저 String으로 받아서 로깅
            ResponseEntity<String> rawResponse = theirStackRestTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Raw response status: {}", rawResponse.getStatusCode());
            String responseBody = rawResponse.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                log.error("Response body is null or empty");
                throw new RuntimeException("Empty response from TheirStack API");
            }

            // 응답 본문 길이 출력
            log.info("Raw response body length: {}", responseBody.length());

            // 응답 본문 처음 500자만 출력
            String preview = responseBody.length() > 500
                    ? responseBody.substring(0, 500) + "..."
                    : responseBody;
            log.info("Raw response body preview: {}", preview);

            log.info("Raw response body responseBody: {}", responseBody);

            // 그 다음 파싱
            TheirStackJobSearchResponse response = objectMapper.readValue(
                    responseBody,
                    TheirStackJobSearchResponse.class
            );

            if (response != null && response.getData() != null) {
                log.info("TheirStack API returned {} jobs", response.getData().size());
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to call TheirStack API", e);
            throw new RuntimeException("TheirStack API call failed", e);
        }
    }

    /**
     * 특정 회사의 최근 Job 검색
     *
     * @param companyNames 회사명 목록
     * @param maxAgeDays 최대 게시 기간 (일)
     * @param limit 결과 개수
     * @return 검색 응답
     */
    public TheirStackJobSearchResponse searchRecentJobs(List<String> companyNames, int maxAgeDays, int limit) {
        TheirStackJobSearchRequest request = TheirStackJobSearchRequest.builder()
                .companyNameOr(companyNames)
                .postedAtMaxAgeDays(maxAgeDays)
                .page(0)
                .limit(limit)
                .jobDescriptionPatternOr(List.of("(?i)software\\s+engineer"))
                .build();

        return searchJobs(request);
    }
}
