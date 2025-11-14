package dev.devrunner.outbox.model;

import dev.devrunner.model.common.TargetType;
import lombok.Value;

import java.time.Instant;

/**
 * Outbox event domain model
 *
 * Represents a change event that needs to be synchronized to external systems (e.g., Elasticsearch)
 */
@Value
public class OutboxEvent {
    Long id;
    TargetType targetType;
    Long targetId;
    UpdateType updateType;
    EventStatus status;
    Integer retryCount;
    String errorMessage;
    Instant updatedAt;
    Instant processedAt;

    /**
     * Create a new pending event
     */
    public static OutboxEvent pending(TargetType targetType, Long targetId, UpdateType updateType) {
        return new OutboxEvent(
            null,
            targetType,
            targetId,
            updateType,
            EventStatus.WAIT,
            0,
            null,
            Instant.now(),
            null
        );
    }

    /**
     * Mark event as processing
     */
    public OutboxEvent markAsProcessing() {
        return new OutboxEvent(
            id,
            targetType,
            targetId,
            updateType,
            EventStatus.PROCESSING,
            retryCount,
            errorMessage,
            updatedAt,
            Instant.now()
        );
    }

    /**
     * Mark event as completed
     */
    public OutboxEvent markAsCompleted() {
        return new OutboxEvent(
            id,
            targetType,
            targetId,
            updateType,
            EventStatus.COMPLETED,
            retryCount,
            errorMessage,
            updatedAt,
            Instant.now()
        );
    }

    /**
     * Mark event as failed with error message
     */
    public OutboxEvent markAsFailed(String errorMessage) {
        return new OutboxEvent(
            id,
            targetType,
            targetId,
            updateType,
            EventStatus.FAILED,
            retryCount + 1,
            errorMessage,
            updatedAt,
            Instant.now()
        );
    }
}
