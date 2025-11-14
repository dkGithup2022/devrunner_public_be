package dev.devrunner.infra.job.repository;

import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;
import java.util.List;
import java.util.Optional;

/**
 * Job Repository 인터페이스
 *
 * 헥사고날 아키텍처에서 Port 역할을 수행하며,
 * 채용공고 도메인의 데이터 접근을 위한 인터페이스를 정의합니다.
 */
public interface JobRepository {

    /**
     * ID로 Job 조회
     *
     * @param identity Job 식별자
     * @return Job 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Job> findById(JobIdentity identity);


    List<Job> findByIdsIn(List<JobIdentity> identities);

    /**
     * Job 저장 (생성/수정)
     *
     * @param job 저장할 Job
     * @return 저장된 Job
     */
    Job save(Job job);

    /**
     * ID로 Job 삭제
     *
     * @param identity Job 식별자
     */
    void deleteById(JobIdentity identity);

    /**
     * Job 존재 여부 확인
     *
     * @param identity Job 식별자
     * @return 존재하면 true, 없으면 false
     */
    boolean existsById(JobIdentity identity);

    /**
     * 모든 Job 조회
     *
     * @return Job 목록
     */
    List<Job> findAll();

    /**
     * URL로 Job 조회
     *
     * @param url 채용공고 URL
     * @return Job 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<Job> findByUrl(String url);

    /**
     * 회사명으로 Job 목록 조회
     *
     * @param company 회사명
     * @return Job 목록
     */
    List<Job> findByCompany(String company);

    /**
     * 조회수 증가 (원자적 연산)
     *
     * DB 레벨에서 조회수를 증가시켜 동시성 문제를 방지합니다.
     * UPDATE job SET view_count = view_count + ? WHERE job_id = ?
     *
     * @param identity Job 식별자
     * @param increment 증가시킬 조회수
     */
    void increaseViewCount(JobIdentity identity, long increment);

    /**
     * 댓글수 증가 (원자적 연산)
     *
     * DB 레벨에서 댓글수를 증가시켜 동시성 문제를 방지합니다.
     * UPDATE jobs SET comment_count = comment_count + 1 WHERE id = ?
     *
     * @param identity Job 식별자
     */
    void increaseCommentCount(JobIdentity identity);

    /**
     * 좋아요 수 증가/감소 (원자적 연산)
     *
     * DB 레벨에서 좋아요 수를 변경하여 동시성 문제를 방지합니다.
     * UPDATE jobs SET like_count = like_count + ? WHERE id = ?
     *
     * @param identity Job 식별자
     * @param increment 증가/감소시킬 값 (양수: 증가, 음수: 감소)
     */
    void increaseLikeCount(JobIdentity identity, long increment);

    /**
     * 싫어요 수 증가/감소 (원자적 연산)
     *
     * DB 레벨에서 싫어요 수를 변경하여 동시성 문제를 방지합니다.
     * UPDATE jobs SET dislike_count = dislike_count + ? WHERE id = ?
     *
     * @param identity Job 식별자
     * @param increment 증가/감소시킬 값 (양수: 증가, 음수: 감소)
     */
    void increaseDislikeCount(JobIdentity identity, long increment);
}
