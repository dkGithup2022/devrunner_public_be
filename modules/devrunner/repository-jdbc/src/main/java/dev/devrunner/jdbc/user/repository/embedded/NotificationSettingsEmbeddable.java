package dev.devrunner.jdbc.user.repository.embedded;

import dev.devrunner.model.common.NotificationSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * NotificationSettings의 Entity 레이어 표현
 * Spring Data JDBC의 @Embedded로 사용되며, users 테이블의 컬럼으로 flatten됩니다.
 */
@Getter
@AllArgsConstructor
public class NotificationSettingsEmbeddable {
    Boolean emailEnabled;
    Boolean newJobAlerts;
    Boolean replyAlerts;
    Boolean likeAlerts;

    public static NotificationSettingsEmbeddable from(NotificationSettings domain) {
        if (domain == null) {
            return null;
        }
        return new NotificationSettingsEmbeddable(
            domain.getEmailEnabled(),
            domain.getNewJobAlerts(),
            domain.getReplyAlerts(),
            domain.getLikeAlerts()
        );
    }

    public NotificationSettings toDomain() {
        return new NotificationSettings(
            emailEnabled,
            newJobAlerts,
            replyAlerts,
            likeAlerts
        );
    }
}
