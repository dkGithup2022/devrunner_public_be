package dev.devrunner.crawler.step;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TheirStack Job 크롤링 데이터 Repository
 */
@Repository
public interface CrawlTheirStackJobRepository extends CrudRepository<CrawlTheirStackJobEntity, Long> {

    /**
     * TheirStack Job ID로 존재 여부 확인
     */
    boolean existsByTheirStackJobId(Long theirStackJobId);

    /**
     * TheirStack Job ID로 조회
     */
    Optional<CrawlTheirStackJobEntity> findByTheirStackJobId(Long theirStackJobId);

    /**
     * 처리 대기 중인 첫 번째 Job 조회
     */
    @Query("SELECT * FROM crawl_theirstack_jobs WHERE status = 'WAIT' ORDER BY created_at ASC LIMIT 1")
    Optional<CrawlTheirStackJobEntity> findFirstWaitingJob();

    /**
     * 재처리 대상 FAILED Job 조회 (retry_count < 3)
     *
     * @param limit 조회할 최대 개수
     * @return FAILED 상태이면서 재시도 가능한 Job 목록
     */
    @Query("SELECT * FROM crawl_theirstack_jobs WHERE status = 'FAILED' AND retry_count < :failed ORDER BY created_at ASC LIMIT :limit")
    List<CrawlTheirStackJobEntity> findFailedJobsForRetry(int failed ,int limit);
}
