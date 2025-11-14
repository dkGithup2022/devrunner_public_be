package dev.devrunner.sync.task.task.es;

import dev.devrunner.elasticsearch.api.job.JobIndexer;
import dev.devrunner.elasticsearch.api.job.JobPopularityManager;
import dev.devrunner.elasticsearch.document.JobDoc;
import dev.devrunner.elasticsearch.mapper.JobDocMapper;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.outbox.command.FindPendingEventsCommand;
import dev.devrunner.outbox.model.OutboxEvent;
import dev.devrunner.outbox.reader.OutboxEventReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobEsSyncTask {

    private final JobIndexer jobIndexer;
    private final JobPopularityManager jobPopularityManager;
    private final JobRepository jobRepository;
    private final OutboxEventReader outboxEventReader;
    private final JobDocMapper jobDocMapper;

    public void run() {
        log.info("Starting Job ES sync task");

        // 1. 미처리 이벤트 조회 (JOB 타입만, 최대 100개)
        List<OutboxEvent> events = outboxEventReader.findPending(
                FindPendingEventsCommand.ofType(100, TargetType.JOB)
        );

        if (events.isEmpty()) {
            log.debug("No pending events to process");
            return;
        }

        log.info("Found {} pending events", events.size());

        // 2. 각 이벤트를 하나씩 처리 & 상태 업데이트
        int successCount = 0;
        int failCount = 0;

        for (OutboxEvent event : events) {
            try {
                processEvent(event);
                outboxEventReader.update(event.markAsCompleted());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to process event: eventId={}, targetId={}, targetType={}, updateType={}",
                        event.getId(), event.getTargetId(), event.getTargetType(), event.getUpdateType(), e);

                // 에러 메시지를 더 상세하게 저장
                String errorDetail = e.getClass().getSimpleName() + ": " + e.getMessage();
                if (e.getCause() != null) {
                    errorDetail += " (caused by: " + e.getCause().getMessage() + ")";
                }

                outboxEventReader.update(event.markAsFailed(errorDetail));
                failCount++;
            }
        }

        log.info("Job ES sync completed: success={}, failed={}", successCount, failCount);
    }

    private void processEvent(OutboxEvent event) {
        switch (event.getUpdateType()) {
            case CREATED, UPDATED, DELETED -> processFullIndex(event);
            case POPULARITY_ONLY -> processPopularityUpdate(event);
        }
    }

    /**
     * 전체 문서 인덱싱 (CREATED, UPDATED, DELETED)
     * DELETED의 경우 isDeleted=true로 인덱싱되어 soft delete 처리
     */
    private void processFullIndex(OutboxEvent event) {
        Job job = jobRepository.findById(new JobIdentity(event.getTargetId()))
                .orElseThrow(() -> new IllegalStateException("Job not found: " + event.getTargetId()));

        JobDoc jobDoc = jobDocMapper.newDoc(job);
        jobIndexer.indexOne(jobDoc);

        log.debug("Successfully indexed job: jobId={}, updateType={}", job.getJobId(), event.getUpdateType());
    }

    /**
     * 인기도만 업데이트 (POPULARITY_ONLY)
     */
    private void processPopularityUpdate(OutboxEvent event) {
        Job job = jobRepository.findById(new JobIdentity(event.getTargetId()))
                .orElseThrow(() -> new IllegalStateException("Job not found: " + event.getTargetId()));

        String docId = "job_" + job.getJobId();
        jobPopularityManager.updatePopularity(docId, job.getPopularity());

        log.debug("Successfully updated popularity: jobId={}, popularity={}", job.getJobId(), job.getPopularity());
    }
}
