package dev.devrunner.outbox.impl;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.outbox.command.FindPendingEventsCommand;
import dev.devrunner.outbox.model.EventStatus;
import dev.devrunner.outbox.model.OutboxEvent;
import dev.devrunner.outbox.model.UpdateType;
import dev.devrunner.outbox.reader.OutboxEventReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JDBC implementation of OutboxEventReader
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class OutboxEventJdbcReader implements OutboxEventReader {

    private final OutboxEventEntityRepository entityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPending(FindPendingEventsCommand command) {
        log.debug("Finding pending events: limit={}, targetType={}, updateType={}",
                command.getLimit(), command.getTargetType(), command.getUpdateType());

        String targetType = command.getTargetType() != null ? command.getTargetType().name() : null;
        String updateType = command.getUpdateType() != null ? command.getUpdateType().name() : null;

        List<OutboxEventEntity> entities = entityRepository.findPendingEvents(
                command.getLimit(),
                targetType,
                updateType
        );

        List<OutboxEvent> events = entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());

        log.debug("Found {} pending events", events.size());
        return events;
    }

    @Override
    @Transactional
    public OutboxEvent update(OutboxEvent event) {
        log.debug("Updating outbox event: id={}, status={}", event.getId(), event.getStatus());

        OutboxEventEntity entity = toEntity(event);
        OutboxEventEntity saved = entityRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    @Transactional
    public void updateBatch(List<OutboxEvent> events) {
        log.debug("Batch updating {} events", events.size());

        for (OutboxEvent event : events) {
            entityRepository.updateEventStatus(
                    event.getId(),
                    event.getStatus().name(),
                    event.getRetryCount(),
                    event.getErrorMessage(),
                    event.getProcessedAt()
            );
        }

        log.debug("Batch update completed");
    }

    private OutboxEventEntity toEntity(OutboxEvent domain) {
        return new OutboxEventEntity(
                domain.getId(),
                domain.getTargetType().name(),
                domain.getTargetId(),
                domain.getUpdateType().name(),
                domain.getStatus().name(),
                domain.getRetryCount(),
                domain.getErrorMessage(),
                domain.getUpdatedAt(),
                domain.getProcessedAt()
        );
    }

    private OutboxEvent toDomain(OutboxEventEntity entity) {
        return new OutboxEvent(
                entity.getId(),
                TargetType.valueOf(entity.getTargetType()),
                entity.getTargetId(),
                UpdateType.valueOf(entity.getUpdateType()),
                EventStatus.valueOf(entity.getStatus()),
                entity.getRetryCount(),
                entity.getErrorMessage(),
                entity.getUpdatedAt(),
                entity.getProcessedAt()
        );
    }
}
