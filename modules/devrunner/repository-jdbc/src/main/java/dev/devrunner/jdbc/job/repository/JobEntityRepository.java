package dev.devrunner.jdbc.job.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Job Entity CRUD API 인터페이스
 * <p>
 * Spring Data JDBC를 활용한 JobEntity 데이터 접근 계층
 * Infrastructure Repository 인터페이스 기반으로 필요한 메서드만 생성
 */
@Repository
public interface JobEntityRepository extends CrudRepository<JobEntity, Long> {
    Optional<JobEntity> findByUrl(String url);

    List<JobEntity> findByCompany(String company);

    List<JobEntity> findByIdIn(List<Long> ids);

    @Modifying
    @Query("UPDATE jobs SET view_count = view_count + :increment WHERE id = :jobId")
    void increaseViewCount(@Param("jobId") Long jobId, @Param("increment") long increment);

    @Modifying
    @Query("UPDATE jobs SET comment_count = comment_count + 1 WHERE id = :jobId")
    void increaseCommentCount(@Param("jobId") Long jobId);

    @Modifying
    @Query("UPDATE jobs SET like_count = like_count + :increment WHERE id = :jobId")
    void increaseLikeCount(@Param("jobId") Long jobId, @Param("increment") long increment);

    @Modifying
    @Query("UPDATE jobs SET dislike_count = dislike_count + :increment WHERE id = :jobId")
    void increaseDislikeCount(@Param("jobId") Long jobId, @Param("increment") long increment);

    @Query("SELECT * FROM jobs WHERE is_closed = false AND is_deleted = false")
    List<JobEntity> findAllOpenJobs();
}
