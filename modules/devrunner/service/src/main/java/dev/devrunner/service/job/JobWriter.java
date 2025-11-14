package dev.devrunner.service.job;

import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;

/**
 * Job 도메인 변경 서비스 인터페이스
 *
 * CQRS 패턴의 Command 책임을 담당하며,
 * Infrastructure Repository 기반으로 변경 로직을 제공합니다.
 */
public interface JobWriter {

    /**
     * Job 저장 (생성/수정)
     *
     * @param job 저장할 Job
     * @return 저장된 Job
     */
    Job upsert(Job job);

    /**
     * ID로 Job 삭제
     *
     * @param identity Job 식별자
     */
    void delete(JobIdentity identity);
}
