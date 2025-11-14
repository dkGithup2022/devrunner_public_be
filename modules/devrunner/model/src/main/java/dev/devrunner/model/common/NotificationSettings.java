package dev.devrunner.model.common;

import lombok.Value;

/**
 * 알림 설정 정보를 나타내는 Value Object
 * User 도메인에서 사용
 */
@Value
public class NotificationSettings {
    Boolean emailEnabled;
    Boolean newJobAlerts;
    Boolean replyAlerts;
    Boolean likeAlerts;

    public static NotificationSettings defaultSettings() {
        return new NotificationSettings(true, true, true, true);
    }

    public static NotificationSettings allDisabled() {
        return new NotificationSettings(false, false, false, false);
    }
}
