package dev.devrunner.outbox.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Spring Data JDBC entity for outbox_events table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("outbox_events")
public class OutboxEventEntity {

    @Id
    private Long id;

    private String targetType;
    private Long targetId;
    private String updateType;
    private String status;
    private Integer retryCount;
    private String errorMessage;
    private Instant updatedAt;
    private Instant processedAt;
}
