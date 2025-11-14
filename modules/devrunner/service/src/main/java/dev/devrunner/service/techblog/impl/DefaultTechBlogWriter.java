package dev.devrunner.service.techblog.impl;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import dev.devrunner.service.techblog.TechBlogWriter;
import dev.devrunner.infra.techblog.repository.TechBlogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * TechBlog 도메인 변경 서비스 구현체
 * <p>
 * CQRS 패턴의 Command 책임을 구현하며,
 * Infrastructure Repository를 활용한 변경 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultTechBlogWriter implements TechBlogWriter {

    private final TechBlogRepository techBlogRepository;
    private final OutboxEventRecorder outboxEventRecorder;

    @Override
    public TechBlog upsert(TechBlog techBlog) {
        log.info("Upserting TechBlog: {}", techBlog.getTechBlogId());

        boolean isNew = techBlog.getTechBlogId() == null;
        TechBlog saved = techBlogRepository.save(techBlog);
        log.info("TechBlog upserted successfully: {}", saved.getTechBlogId());

        RecordOutboxEventCommand recordCommand = isNew
                ? RecordOutboxEventCommand.created(TargetType.TECH_BLOG, saved.getTechBlogId())
                : RecordOutboxEventCommand.updated(TargetType.TECH_BLOG, saved.getTechBlogId());
        outboxEventRecorder.record(recordCommand);
        return saved;
    }

    @Override
    public void delete(TechBlogIdentity identity) {
        log.info("Deleting TechBlog by id: {}", identity.getTechBlogId());
        techBlogRepository.deleteById(identity);
        log.info("TechBlog deleted successfully: {}", identity.getTechBlogId());
    }
}
