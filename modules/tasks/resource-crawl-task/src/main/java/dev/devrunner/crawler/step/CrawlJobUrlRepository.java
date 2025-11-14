package dev.devrunner.crawler.step;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrawlJobUrlRepository extends CrudRepository<CrawlJobUrlEntity, Long> {

    /**
     * 처리 대기 중인 첫 번째 URL 조회
     */
    @Query("SELECT * FROM crawl_job_urls WHERE status = 'WAIT' ORDER BY created_at ASC LIMIT 1")
    Optional<CrawlJobUrlEntity> findFirstWaitingUrl();

    /**
     * 재처리 대상 FAILED URL 조회 (retry_count < 3)
     *
     * @param limit 조회할 최대 개수
     * @return FAILED 상태이면서 재시도 가능한 URL 목록
     */
    @Query("SELECT * FROM crawl_job_urls WHERE status = 'FAILED' AND retry_count < :retried ORDER BY created_at ASC LIMIT :limit")
    List<CrawlJobUrlEntity> findFailedUrlsForRetry(int retried ,int limit);

    /**
     * URL 중복 체크
     */
    boolean existsByUrl(String url);
}
