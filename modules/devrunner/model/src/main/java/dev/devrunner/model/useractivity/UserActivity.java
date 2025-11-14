package dev.devrunner.model.useractivity;

import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.TargetType;
import lombok.Value;
import java.time.Instant;

@Value
public class UserActivity implements AuditProps {
    Long userActivityId;
    Long userId;
    ActivityType activityType;
    TargetType targetType;
    Long targetId;
    String targetTitle;
    String targetCompany;
    Instant createdAt;
    Instant updatedAt;

    public static UserActivity create(
        Long userId,
        ActivityType activityType,
        TargetType targetType,
        Long targetId,
        String targetTitle,
        String targetCompany
    ) {
        Instant now = Instant.now();
        return new UserActivity(
            null,
            userId,
            activityType,
            targetType,
            targetId,
            targetTitle,
            targetCompany,
            now,
            now
        );
    }

    public static UserActivity createSimple(
        Long userId,
        ActivityType activityType,
        TargetType targetType,
        Long targetId
    ) {
        Instant now = Instant.now();
        return new UserActivity(
            null,
            userId,
            activityType,
            targetType,
            targetId,
            null,
            null,
            now,
            now
        );
    }
}
