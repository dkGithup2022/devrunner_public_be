package dev.devrunner.service.job.view;

import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Job 조회수 메모리 관리 구현체
 * <p>
 * ConcurrentHashMap과 AtomicLong을 활용하여 동시성을 보장하며,
 * 메모리에 조회수를 누적한 후 주기적으로 DB에 일괄 반영합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultJobViewMemory implements JobViewMemory {

    private final JobRepository jobRepository;

    /**
     * 메모리에 누적된 조회수 저장소
     * Key: Job ID, Value: 조회수 증가량
     */
    private final Map<Long, AtomicLong> viewCounts = new ConcurrentHashMap<>();

    private final OutboxEventRecorder outboxEventRecorder;

    @Override
    public void countUp(Long jobId) {
        if (jobId == null) {
            log.warn("JobId is null, skipping countUp");
            return;
        }

        viewCounts.computeIfAbsent(jobId, k -> new AtomicLong(0))
                .incrementAndGet();

        log.debug("View count incremented for Job: {}", jobId);
    }

    @Override
    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    public void flush() {
        if (viewCounts.isEmpty()) {
            log.debug("No view counts to flush");
            return;
        }

        log.info("Starting to flush {} Job view counts", viewCounts.size());
        int successCount = 0;
        int failCount = 0;

        // 현재 메모리의 스냅샷을 추출하고 초기화
        Map<Long, AtomicLong> snapshot = new ConcurrentHashMap<>(viewCounts);
        viewCounts.clear();

        for (Map.Entry<Long, AtomicLong> entry : snapshot.entrySet()) {
            Long jobId = entry.getKey();
            long incrementCount = entry.getValue().get();

            if (incrementCount <= 0) {
                continue;
            }

            try {
                JobIdentity identity = new JobIdentity(jobId);

                // DB 레벨에서 원자적으로 조회수 증가
                jobRepository.increaseViewCount(identity, incrementCount);
                successCount++;
                log.debug("Flushed {} view counts for Job: {}", incrementCount, jobId);

                var command = RecordOutboxEventCommand.popularityOnly(
                        TargetType.JOB, jobId
                );
                outboxEventRecorder.record(command);

            } catch (Exception e) {
                log.error("Failed to flush view count for Job: {}", jobId, e);
                failCount++;
            }
        }

        log.info("Flush completed - Success: {}, Failed: {}", successCount, failCount);
    }
}
