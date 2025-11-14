package dev.devrunner.crawler.step;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrawlJobContentRepository extends CrudRepository<CrawlJobContentEntity, Long> {

    /**
     * 처리 대기 중인 첫 번째 콘텐츠 조회
     */
    @Query("SELECT * FROM crawl_job_contents WHERE status = 'WAIT' ORDER BY created_at ASC LIMIT 1")
    Optional<CrawlJobContentEntity> findFirstWaitingContent();

    /**
     * 재처리 대상 FAILED 콘텐츠 조회 (retry_count < 3)
     *
     * @param limit 조회할 최대 개수
     * @return FAILED 상태이면서 재시도 가능한 콘텐츠 목록
     */
    @Query("SELECT * FROM crawl_job_contents WHERE status = 'FAILED' AND retry_count < :failed ORDER BY created_at ASC LIMIT :limit")
    List<CrawlJobContentEntity> findFailedContentsForRetry(int failed, int limit);

    /**
     * URL ID로 콘텐츠 조회
     */
    Optional<CrawlJobContentEntity> findByUrlId(Long urlId);
}
