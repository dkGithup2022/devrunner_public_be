package dev.devrunner.outbox.impl;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JDBC repository for OutboxEventEntity
 */
public interface OutboxEventEntityRepository extends CrudRepository<OutboxEventEntity, Long> {

    /**
     * Find pending events with optional filters
     */
    @Query("""
        SELECT * FROM outbox_events
        WHERE status = 'WAIT'
        AND (:targetType IS NULL OR target_type = :targetType)
        AND (:updateType IS NULL OR update_type = :updateType)
        ORDER BY updated_at ASC
        LIMIT :limit
        """)
    List<OutboxEventEntity> findPendingEvents(
        @Param("limit") int limit,
        @Param("targetType") String targetType,
        @Param("updateType") String updateType
    );

    /**
     * Update event status for batch processing
     */
    @Modifying
    @Query("""
        UPDATE outbox_events
        SET status = :status,
            retry_count = :retryCount,
            error_message = :errorMessage,
            processed_at = :processedAt
        WHERE id = :id
        """)
    void updateEventStatus(
        @Param("id") Long id,
        @Param("status") String status,
        @Param("retryCount") Integer retryCount,
        @Param("errorMessage") String errorMessage,
        @Param("processedAt") java.time.Instant processedAt
    );
}
