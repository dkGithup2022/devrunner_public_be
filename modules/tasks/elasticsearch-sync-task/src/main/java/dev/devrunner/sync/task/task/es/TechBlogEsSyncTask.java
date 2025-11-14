package dev.devrunner.sync.task.task.es;

import dev.devrunner.elasticsearch.api.techblog.TechBlogIndexer;
import dev.devrunner.elasticsearch.api.techblog.TechBlogPopularityManager;
import dev.devrunner.elasticsearch.document.TechBlogDoc;
import dev.devrunner.elasticsearch.mapper.TechBlogDocMapper;
import dev.devrunner.infra.techblog.repository.TechBlogRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import dev.devrunner.outbox.command.FindPendingEventsCommand;
import dev.devrunner.outbox.model.OutboxEvent;
import dev.devrunner.outbox.reader.OutboxEventReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TechBlogEsSyncTask {

    private final TechBlogIndexer techBlogIndexer;
    private final TechBlogPopularityManager techBlogPopularityManager;
    private final TechBlogRepository techBlogRepository;
    private final OutboxEventReader outboxEventReader;
    private final TechBlogDocMapper techBlogDocMapper;

    public void run() {
        log.info("Starting TechBlog ES sync task");

        // 1. 미처리 이벤트 조회 (TECH_BLOG 타입만, 최대 100개)
        List<OutboxEvent> events = outboxEventReader.findPending(
                FindPendingEventsCommand.ofType(100, TargetType.TECH_BLOG)
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
                log.error("Failed to process event: eventId={}, error={}",
                        event.getId(), e.getMessage(), e);
                outboxEventReader.update(event.markAsFailed(e.getMessage()));
                failCount++;
            }
        }

        log.info("TechBlog ES sync completed: success={}, failed={}", successCount, failCount);
    }

    private void processEvent(OutboxEvent event) {
        switch (event.getUpdateType()) {
            case CREATED, UPDATED,  DELETED  -> processFullIndex(event);
            case POPULARITY_ONLY -> processPopularityUpdate(event);

        }
    }

    /**
     * 전체 문서 인덱싱 (CREATED, UPDATED)
     */
    private void processFullIndex(OutboxEvent event) {
        TechBlog techBlog = techBlogRepository.findById(new TechBlogIdentity(event.getTargetId()))
                .orElseThrow(() -> new IllegalStateException("TechBlog not found: " + event.getTargetId()));

        TechBlogDoc techBlogDoc = techBlogDocMapper.newDoc(techBlog);
        techBlogIndexer.indexOne(techBlogDoc);

        log.debug("Successfully indexed techBlog: techBlogId={}, updateType={}", techBlog.getTechBlogId(), event.getUpdateType());
    }

    /**
     * 인기도만 업데이트 (POPULARITY_ONLY)
     */
    private void processPopularityUpdate(OutboxEvent event) {
        TechBlog techBlog = techBlogRepository.findById(new TechBlogIdentity(event.getTargetId()))
                .orElseThrow(() -> new IllegalStateException("TechBlog not found: " + event.getTargetId()));

        String docId = "techblog_" + techBlog.getTechBlogId();
        techBlogPopularityManager.updatePopularity(docId, techBlog.getPopularity());

        log.debug("Successfully updated popularity: techBlogId={}, popularity={}", techBlog.getTechBlogId(), techBlog.getPopularity());
    }
}
