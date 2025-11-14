package dev.devrunner.service.techblog.view;

import dev.devrunner.infra.techblog.repository.TechBlogRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
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
 * TechBlog 조회수 메모리 관리 구현체
 * <p>
 * ConcurrentHashMap과 AtomicLong을 활용하여 동시성을 보장하며,
 * 메모리에 조회수를 누적한 후 주기적으로 DB에 일괄 반영합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultTechBlogViewMemory implements TechBlogViewMemory {

    private final TechBlogRepository techBlogRepository;
    private final OutboxEventRecorder outboxEventRecorder;
    /**
     * 메모리에 누적된 조회수 저장소
     * Key: TechBlog ID, Value: 조회수 증가량
     */
    private final Map<Long, AtomicLong> viewCounts = new ConcurrentHashMap<>();

    @Override
    public void countUp(Long techBlogId) {
        if (techBlogId == null) {
            log.warn("TechBlogId is null, skipping countUp");
            return;
        }

        viewCounts.computeIfAbsent(techBlogId, k -> new AtomicLong(0))
                .incrementAndGet();

        log.debug("View count incremented for TechBlog: {}", techBlogId);
    }

    @Override
    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    public void flush() {
        if (viewCounts.isEmpty()) {
            log.debug("No view counts to flush");
            return;
        }

        log.info("Starting to flush {} TechBlog view counts", viewCounts.size());
        int successCount = 0;
        int failCount = 0;

        // 현재 메모리의 스냅샷을 추출하고 초기화
        Map<Long, AtomicLong> snapshot = new ConcurrentHashMap<>(viewCounts);
        viewCounts.clear();

        for (Map.Entry<Long, AtomicLong> entry : snapshot.entrySet()) {
            Long techBlogId = entry.getKey();
            long incrementCount = entry.getValue().get();

            if (incrementCount <= 0) {
                continue;
            }

            try {
                TechBlogIdentity identity = new TechBlogIdentity(techBlogId);

                // DB 레벨에서 원자적으로 조회수 증가
                techBlogRepository.increaseViewCount(identity, incrementCount);
                successCount++;
                log.debug("Flushed {} view counts for TechBlog: {}", incrementCount, techBlogId);

                var command = RecordOutboxEventCommand.popularityOnly(
                        TargetType.TECH_BLOG, identity.getTechBlogId()
                );
                outboxEventRecorder.record(command);

            } catch (Exception e) {
                log.error("Failed to flush view count for TechBlog: {}", techBlogId, e);
                failCount++;
            }
        }

        log.info("Flush completed - Success: {}, Failed: {}", successCount, failCount);
    }
}
