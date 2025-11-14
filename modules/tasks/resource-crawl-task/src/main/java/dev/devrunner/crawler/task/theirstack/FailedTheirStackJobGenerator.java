package dev.devrunner.crawler.task.theirstack;

import dev.devrunner.crawler.step.CrawlTheirStackJobEntity;
import dev.devrunner.crawler.step.CrawlTheirStackJobRepository;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.common.CrawlStatus;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.Job;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Failed TheirStack Job Generator
 * <p>
 * crawl_theirstack_jobs에서 FAILED 상태이고 retry_count < 3인 데이터를 재처리:
 * 1. TheirStackContentGenerateFacade로 Job 생성
 * 2. jobs 테이블에 저장
 * 3. crawl_theirstack_jobs.job_id 업데이트
 * 4. crawl_theirstack_jobs.status → SUCCESS
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FailedTheirStackJobGenerator {

    private final TheirStackContentGenerateFacade facade;
    private final CrawlTheirStackJobRepository theirStackRepository;
    private final JobRepository jobRepository;
    private final OutboxEventRecorder outboxEventRecorder;

    /**
     * FAILED 상태의 TheirStack Job을 재처리 (retry_count < 3)
     */
    public void run() {
        var failedJobs = theirStackRepository.findFailedJobsForRetry(3, 100);
        log.info("Found {} failed TheirStack jobs for retry", failedJobs.size());
        if (failedJobs.isEmpty()) {
            return;
        }

        for (var entity : failedJobs) {
            processJob(entity);

            // Rate limiting: 1분 대기
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 단일 TheirStack Job 처리
     */
    private void processJob(CrawlTheirStackJobEntity entity) {
        log.info("Processing failed TheirStack job: id={}, theirStackJobId={}, company={}, retryCount={}",
                entity.getId(), entity.getTheirStackJobId(), entity.getCompany(), entity.getRetryCount());

        try {
            // 1. TheirStackContentGenerateFacade로 Job 생성
            Job job = facade.generate(entity);

            // 2. jobs 테이블에 저장
            Job savedJob = jobRepository.save(job);
            log.info("Saved job: jobId={}, url={}", savedJob.getJobId(), savedJob.getUrl());

            // 3. Outbox 이벤트 기록
            RecordOutboxEventCommand command = RecordOutboxEventCommand.created(TargetType.JOB, savedJob.getJobId());
            outboxEventRecorder.record(command);
            log.info("Job upserted successfully with outbox event: {}", savedJob.getJobId());

            // 4. crawl_theirstack_jobs 업데이트 (job_id 설정, status → SUCCESS)
            CrawlTheirStackJobEntity updatedEntity = new CrawlTheirStackJobEntity(
                    entity.getId(),
                    entity.getTheirStackJobId(),
                    entity.getRawData(),
                    entity.getCompany(),
                    entity.getUrl(),
                    entity.getTitle(),
                    entity.getLocation(),
                    entity.getSeniority(),
                    entity.getDatePosted(),
                    entity.getDescription(),
                    CrawlStatus.SUCCESS,
                    savedJob.getJobId(),
                    null,  // errorMessage 초기화
                    entity.getRetryCount(),  // 성공 시 증가 안 함
                    entity.getCreatedAt(),
                    Instant.now()
            );
            theirStackRepository.save(updatedEntity);
            log.info("Updated TheirStack job status to SUCCESS: id={}, jobId={}, retryCount={}",
                    entity.getId(), savedJob.getJobId(), updatedEntity.getRetryCount());

        } catch (Exception e) {
            log.error("Failed to generate job from TheirStack data: id={}, theirStackJobId={}, error={}",
                    entity.getId(), entity.getTheirStackJobId(), e.getMessage(), e);

            // 5. 실패 시 status → FAILED, error_message 기록, retry_count 증가
            CrawlTheirStackJobEntity failedEntity = new CrawlTheirStackJobEntity(
                    entity.getId(),
                    entity.getTheirStackJobId(),
                    entity.getRawData(),
                    entity.getCompany(),
                    entity.getUrl(),
                    entity.getTitle(),
                    entity.getLocation(),
                    entity.getSeniority(),
                    entity.getDatePosted(),
                    entity.getDescription(),
                    CrawlStatus.FAILED,
                    null,  // jobId
                    e.getMessage(),
                    entity.getRetryCount() + 1,  // retry_count 증가
                    entity.getCreatedAt(),
                    Instant.now()
            );
            theirStackRepository.save(failedEntity);
            log.warn("Updated TheirStack job status to FAILED: id={}, retryCount={}",
                    entity.getId(), failedEntity.getRetryCount());
        }
    }
}
