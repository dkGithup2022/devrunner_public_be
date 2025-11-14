package dev.devrunner.outbox.recorder;

import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.model.OutboxEvent;

/**
 * Interface for recording outbox events
 */
public interface OutboxEventRecorder {

    /**
     * Record a new outbox event
     *
     * @param command the command containing event details
     * @return the recorded event with generated ID
     */
    OutboxEvent record(RecordOutboxEventCommand command);
}
