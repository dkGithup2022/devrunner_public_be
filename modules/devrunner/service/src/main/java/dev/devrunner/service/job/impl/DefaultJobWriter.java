package dev.devrunner.service.job.impl;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import dev.devrunner.service.job.JobWriter;
import dev.devrunner.infra.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Job 도메인 변경 서비스 구현체
 *
 * CQRS 패턴의 Command 책임을 구현하며,
 * Infrastructure Repository를 활용한 변경 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultJobWriter implements JobWriter {

    private final JobRepository jobRepository;
    private final OutboxEventRecorder outboxEventRecorder;

    @Override
    public Job upsert(Job job) {
        log.info("Upserting Job: {}", job.getJobId());

        boolean isNewJob = job.getJobId() == null;

        // Job 저장
        Job saved = jobRepository.save(job);

        // Outbox 이벤트 기록
        RecordOutboxEventCommand command = isNewJob
            ? RecordOutboxEventCommand.created(TargetType.JOB, saved.getJobId())
            : RecordOutboxEventCommand.updated(TargetType.JOB, saved.getJobId());
        outboxEventRecorder.record(command);

        log.info("Job upserted successfully with outbox event: {}", saved.getJobId());
        return saved;
    }

    @Override
    public void delete(JobIdentity identity) {
        log.info("Deleting Job by id: {}", identity.getJobId());
        jobRepository.deleteById(identity);
        log.info("Job deleted successfully: {}", identity.getJobId());
    }
}
