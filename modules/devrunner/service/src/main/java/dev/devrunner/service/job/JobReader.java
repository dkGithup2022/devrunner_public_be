package dev.devrunner.service.job;

import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;

import java.util.List;

/**
 * Job 도메인 조회 서비스 인터페이스
 * <p>
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface JobReader {

    /**
     * Job 읽기 (조회수 증가 포함)
     *
     * @param identity Job 식별자
     * @return Job 엔티티
     */
    Job read(JobIdentity identity);

    /**
     * ID로 Job 조회 (조회수 증가 없음)
     *
     * @param identity Job 식별자
     * @return Job 엔티티
     */
    Job getById(JobIdentity identity);

    List<Job> getByIds(List<JobIdentity> identities);

    /**
     * 모든 Job 조회
     *
     * @return Job 목록
     */
    List<Job> getAll();

    /**
     * URL로 Job 조회
     *
     * @param url 채용공고 URL
     * @return Job 엔티티
     */
    Job getByUrl(String url);

    /**
     * 회사명으로 Job 목록 조회
     *
     * @param company 회사명
     * @return Job 목록
     */
    List<Job> getByCompany(String company);
}
