package dev.devrunner.outbox.reader;

import dev.devrunner.outbox.command.FindPendingEventsCommand;
import dev.devrunner.outbox.model.OutboxEvent;

import java.util.List;

/**
 * Interface for reading outbox events
 */
public interface OutboxEventReader {

    /**
     * Find pending events based on query criteria
     *
     * @param command the query command with filters
     * @return list of pending events matching the criteria
     */
    List<OutboxEvent> findPending(FindPendingEventsCommand command);

    /**
     * Update event status
     *
     * @param event the event to update
     * @return the updated event
     */
    OutboxEvent update(OutboxEvent event);

    /**
     * Update multiple events in batch
     *
     * @param events list of events to update
     */
    void updateBatch(List<OutboxEvent> events);
}
