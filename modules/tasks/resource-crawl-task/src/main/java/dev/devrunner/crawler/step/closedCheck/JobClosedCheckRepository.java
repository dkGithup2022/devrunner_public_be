package dev.devrunner.crawler.step.closedCheck;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobClosedCheckRepository extends CrudRepository<JobClosedCheckEntity, Long> {

    /**
     * 특정 Job의 모든 체크 이력 조회 (최신순)
     *
     * @param jobId Job ID
     * @return 체크 이력 목록
     */
    @Query("SELECT * FROM job_closed_checks WHERE job_id = :jobId ORDER BY checked_at DESC")
    List<JobClosedCheckEntity> findByJobIdOrderByCheckedAtDesc(@Param("jobId") Long jobId);

    /**
     * 특정 Job의 CANNOT_READ_PAGE 이력 개수 조회
     *
     * @param jobId Job ID
     * @return CANNOT_READ_PAGE 이력 개수
     */
    @Query("SELECT COUNT(*) FROM job_closed_checks WHERE job_id = :jobId AND closed_reason = 'CANNOT_READ_PAGE'")
    int countCannotReadPageByJobId(@Param("jobId") Long jobId);

    /**
     * 특정 Job의 최근 체크 이력 조회
     *
     * @param jobId Job ID
     * @return 최근 체크 이력 (Optional)
     */
    @Query("SELECT * FROM job_closed_checks WHERE job_id = :jobId ORDER BY checked_at DESC LIMIT 1")
    JobClosedCheckEntity findLatestByJobId(@Param("jobId") Long jobId);
}
