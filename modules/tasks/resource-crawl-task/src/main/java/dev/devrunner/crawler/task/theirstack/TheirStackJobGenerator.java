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
 * TheirStack Job Generator
 * <p>
 * crawl_theirstack_jobs 테이블에서 WAIT 상태인 데이터를 하나씩 가져와서:
 * 1. TheirStackContentGenerateFacade로 Job 생성
 * 2. jobs 테이블에 저장
 * 3. crawl_theirstack_jobs.job_id 업데이트
 * 4. crawl_theirstack_jobs.status → SUCCESS
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TheirStackJobGenerator {

    private final TheirStackContentGenerateFacade facade;
    private final CrawlTheirStackJobRepository theirStackRepository;
    private final JobRepository jobRepository;
    private final OutboxEventRecorder outboxEventRecorder;

    /**
     * WAIT 상태의 TheirStack Job을 순차 처리
     */
    public void run() {
        var entityOpt = theirStackRepository.findFirstWaitingJob();
        if (entityOpt.isEmpty()) {
            log.debug("No waiting TheirStack jobs found");
            return;
        }

        CrawlTheirStackJobEntity entity = entityOpt.get();
        log.info("Processing TheirStack job: id={}, theirStackJobId={}, company={}, title={}",
                entity.getId(), entity.getTheirStackJobId(), entity.getCompany(), entity.getTitle());

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
                    null,  // errorMessage
                    entity.getRetryCount(),
                    entity.getCreatedAt(),
                    Instant.now()
            );
            theirStackRepository.save(updatedEntity);
            log.info("Updated TheirStack job status to SUCCESS: id={}, jobId={}",
                    entity.getId(), savedJob.getJobId());

        } catch (Exception e) {
            log.error("Failed to generate job from TheirStack data: id={}, theirStackJobId={}, error={}",
                    entity.getId(), entity.getTheirStackJobId(), e.getMessage(), e);

            // 5. 실패 시 status → FAILED, error_message 기록
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
                    entity.getRetryCount(),
                    entity.getCreatedAt(),
                    Instant.now()
            );
            theirStackRepository.save(failedEntity);
            log.warn("Updated TheirStack job status to FAILED: id={}", entity.getId());
        }
    }
}
