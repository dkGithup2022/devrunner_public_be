package dev.devrunner.sync.task.task.es;

import dev.devrunner.elasticsearch.api.communitypost.CommunityPostIndexer;
import dev.devrunner.elasticsearch.api.communitypost.CommunityPostPopularityManager;
import dev.devrunner.elasticsearch.document.CommunityPostDoc;
import dev.devrunner.elasticsearch.mapper.CommunityPostDocMapper;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
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
public class CommunityPostEsSyncTask {

    private final CommunityPostIndexer communityPostIndexer;
    private final CommunityPostPopularityManager communityPostPopularityManager;
    private final CommunityPostRepository communityPostRepository;
    private final OutboxEventReader outboxEventReader;
    private final CommunityPostDocMapper communityPostDocMapper;

    public void run() {
        log.info("Starting CommunityPost ES sync task");

        // 1. 미처리 이벤트 조회 (COMMUNITY_POST 타입만, 최대 100개)
        List<OutboxEvent> events = outboxEventReader.findPending(
                FindPendingEventsCommand.ofType(100, TargetType.COMMUNITY_POST)
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

        log.info("CommunityPost ES sync completed: success={}, failed={}", successCount, failCount);
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
        CommunityPostRead communityPost = communityPostRepository.findById(new CommunityPostIdentity(event.getTargetId()))
                .orElseThrow(() -> new IllegalStateException("CommunityPost not found: " + event.getTargetId()));

        CommunityPostDoc communityPostDoc = communityPostDocMapper.toDoc(communityPost);
        communityPostIndexer.indexOne(communityPostDoc);

        log.debug("Successfully indexed communityPost: communityPostId={}, updateType={}", communityPost.getCommunityPostId(), event.getUpdateType());
    }

    /**
     * 인기도만 업데이트 (POPULARITY_ONLY)
     */
    private void processPopularityUpdate(OutboxEvent event) {
        var communityPost = communityPostRepository.findById(new CommunityPostIdentity(event.getTargetId()))
                .orElseThrow(() -> new IllegalStateException("CommunityPost not found: " + event.getTargetId()));

        String docId = "communitypost_" + communityPost.getCommunityPostId();
        communityPostPopularityManager.updatePopularity(docId, communityPost.getPopularity());

        log.debug("Successfully updated popularity: communityPostId={}, popularity={}", communityPost.getCommunityPostId(), communityPost.getPopularity());
    }
}
