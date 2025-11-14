package dev.devrunner.crawler.step;

import dev.devrunner.model.common.CrawlStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * TheirStack Job 크롤링 데이터 엔티티
 */
@Table("crawl_theirstack_jobs")
@Getter
@AllArgsConstructor
public class CrawlTheirStackJobEntity {

    @Id
    private Long id;

    // TheirStack 원본 데이터
    private Long theirStackJobId;
    private String rawData;  // 전체 JobData JSON

    // 주요 컬럼 (빠른 조회/필터링용)
    private String company;
    private String url;
    private String title;
    private String location;
    private String seniority;
    private Instant datePosted;
    private String description;  // Job 설명 본문

    // 처리 상태
    private CrawlStatus status;
    private Long jobId;  // jobs 테이블 FK
    private String errorMessage;
    private Integer retryCount;

    // 타임스탬프
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 새로운 TheirStack Job 생성
     */
    public static CrawlTheirStackJobEntity createWaiting(
            Long theirStackJobId,
            String rawData,
            String company,
            String url,
            String title,
            String location,
            String seniority,
            Instant datePosted,
            String description
    ) {
        return new CrawlTheirStackJobEntity(
                null,
                theirStackJobId,
                rawData,
                company,
                url,
                title,
                location,
                seniority,
                datePosted,
                description,
                CrawlStatus.WAIT,
                null,
                null,
                0,
                Instant.now(),
                null
        );
    }
}
