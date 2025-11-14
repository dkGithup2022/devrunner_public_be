package dev.devrunner.model.notification;

import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.TargetType;
import lombok.Value;
import java.time.Instant;

@Value
public class Notification implements AuditProps {
    Long notificationId;
    Long userId;
    NotificationType type;
    String title;
    String content;
    TargetType targetType;
    Long targetId;
    Long actorUserId;
    Boolean isRead;
    Instant createdAt;
    Instant updatedAt;

    public static Notification create(
        Long userId,
        NotificationType type,
        String title,
        String content,
        TargetType targetType,
        Long targetId,
        Long actorUserId
    ) {
        Instant now = Instant.now();
        return new Notification(
            null,
            userId,
            type,
            title,
            content,
            targetType,
            targetId,
            actorUserId,
            false,
            now,
            now
        );
    }

    public Notification markAsRead() {
        return new Notification(
            notificationId, userId, type, title, content,
            targetType, targetId, actorUserId, true, createdAt, Instant.now()
        );
    }

    public Notification markAsUnread() {
        return new Notification(
            notificationId, userId, type, title, content,
            targetType, targetId, actorUserId, false, createdAt, Instant.now()
        );
    }
}
