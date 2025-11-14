package dev.devrunner.crawler.step;

import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.CrawlStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Crawler Step 1: URL 수집 단계
 *
 * 크롤링 대상 채용 공고 URL을 저장
 */
@Table("crawl_job_urls")
@Getter
@AllArgsConstructor
public class CrawlJobUrlEntity {
    @Id
    private Long id;
    private Company company;
    private String url;
    private String title;
    private CrawlStatus status;
    private Integer retryCount;
    private Instant createdAt;
    private Instant processedAt;

    /**
     * WAIT 상태의 새로운 URL 엔티티 생성
     */
    public static CrawlJobUrlEntity createWaiting(Company company, String url, String title) {
        return new CrawlJobUrlEntity(
                null,
                company,
                url,
                title,
                CrawlStatus.WAIT,
                0,
                Instant.now(),
                null
        );
    }
}
