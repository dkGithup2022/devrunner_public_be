package dev.devrunner.crawler.step;

import dev.devrunner.model.common.CrawlStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Crawler Step 2: 콘텐츠 크롤링 및 요약 단계
 *
 * Firecrawl로 추출한 Markdown과 GPT로 요약한 내용을 저장
 */
@Table("crawl_job_contents")
@Getter
@AllArgsConstructor
public class CrawlJobContentEntity {
    @Id
    private Long id;
    private Long urlId;                // crawl_job_urls 참조
    private String markdownContent;    // Firecrawl 결과
    private String shortenedContent;   // GPT 요약
    private CrawlStatus status;
    private Long jobId;                // jobs 테이블 참조
    private String errorMessage;
    private Integer retryCount;
    private Instant createdAt;
    private Instant processedAt;

    /**
     * WAIT 상태의 새로운 Content 엔티티 생성
     */
    public static CrawlJobContentEntity createWaiting(Long urlId) {
        return new CrawlJobContentEntity(
                null,
                urlId,
                null,
                null,
                CrawlStatus.WAIT,
                null,
                null,
                0,
                Instant.now(),
                null
        );
    }
}
