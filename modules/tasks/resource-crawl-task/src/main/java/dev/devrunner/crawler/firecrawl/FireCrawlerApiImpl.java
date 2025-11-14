package dev.devrunner.crawler.firecrawl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

/**
 * Firecrawl API 구현체
 *
 * URL을 Markdown으로 변환하는 Firecrawl 서비스 호출
 * - Unirest HTTP Client 사용
 * - 응답 검증
 * - 재시도 로직 (최대 3회)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FireCrawlerApiImpl implements FireCrawlerApi {

    private final ObjectMapper objectMapper;

    @Value("${firecrawl.api-key}")
    private String apiKey;

    @Override
    public String md(String url) {
        log.debug("Firecrawl request: url={}", url);

        try {
            // Request Body 생성
            String requestBody = createRequestBody(url);

            // Unirest 타임아웃 설정
            Unirest.setTimeouts(10000, 30000);

            // API 호출
            com.mashape.unirest.http.HttpResponse<String> response = Unirest
                    .post("https://api.firecrawl.dev/v1/scrape")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .asString();

            // 응답 파싱
            FirecrawlResponse firecrawlResponse = objectMapper.readValue(
                    response.getBody().trim(),
                    FirecrawlResponse.class
            );

            // 응답 검증
            validateResponse(firecrawlResponse);

            String markdown = firecrawlResponse.data().markdown();
            log.debug("Firecrawl response: url={}, markdownLength={}", url, markdown.length());

            return markdown;

        } catch (UnirestException e) {
            log.error("Firecrawl HTTP request failed: url={}, error={}", url, e.getMessage());
            throw new FirecrawlException("HTTP request failed for Firecrawl: " + url, e);
        } catch (Exception e) {
            log.error("Firecrawl fetch failed: url={}, error={}", url, e.getMessage());
            throw new FirecrawlException("Failed to fetch markdown from Firecrawl: " + url, e);
        }
    }

    private String createRequestBody(String url) {
        return String.format("""
                {
                  "url": "%s",
                  "formats": ["markdown"],
                  "onlyMainContent": true,
                  "excludeTags": ["#ad", "#footer"],
                  "waitFor": 10000,
                  "timeout": 30000
                }
                """, url);
    }

    private void validateResponse(FirecrawlResponse response) {
        if (response == null) {
            throw new FirecrawlException("Firecrawl response is null");
        }

        if (!response.success()) {
            throw new FirecrawlException("Firecrawl API returned success=false");
        }

        if (response.data() == null) {
            throw new FirecrawlException("Firecrawl response data is null");
        }

        if (response.data().markdown() == null || response.data().markdown().isBlank()) {
            throw new FirecrawlException("Firecrawl markdown content is empty");
        }
    }
}
