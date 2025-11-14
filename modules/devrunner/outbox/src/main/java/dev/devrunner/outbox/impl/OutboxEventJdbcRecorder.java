package dev.devrunner.outbox.impl;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.model.EventStatus;
import dev.devrunner.outbox.model.OutboxEvent;
import dev.devrunner.outbox.model.UpdateType;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of OutboxEventRecorder
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class OutboxEventJdbcRecorder implements OutboxEventRecorder {

    private final OutboxEventEntityRepository entityRepository;

    @Override
    public OutboxEvent record(RecordOutboxEventCommand command) {
        log.info("Recording outbox event: targetType={}, targetId={}, updateType={}",
            command.getTargetType(), command.getTargetId(), command.getUpdateType());

        OutboxEvent event = OutboxEvent.pending(
            command.getTargetType(),
            command.getTargetId(),
            command.getUpdateType()
        );

        OutboxEventEntity entity = toEntity(event);
        OutboxEventEntity saved = entityRepository.save(entity);

        OutboxEvent result = toDomain(saved);
        log.info("Outbox event recorded: id={}", result.getId());

        return result;
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
