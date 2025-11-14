package dev.devrunner.jdbc.notification.repository;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Notification Spring Data JDBC Entity
 *
 * Model 클래스 스펙을 기반으로 생성된 데이터베이스 매핑용 엔티티
 */
@Table("notifications")
@Getter
@AllArgsConstructor
public class NotificationEntity {
    @Id
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String content;
    private TargetType targetType;
    private Long targetId;
    private Long actorUserId;
    private Boolean isRead;
    private Instant createdAt;
    private Instant updatedAt;
}
