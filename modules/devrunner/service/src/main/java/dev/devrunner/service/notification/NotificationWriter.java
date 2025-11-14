package dev.devrunner.service.notification;

import dev.devrunner.model.notification.Notification;
import dev.devrunner.model.notification.NotificationIdentity;

/**
 * Notification 도메인 변경 서비스 인터페이스
 *
 * CQRS 패턴의 Command 책임을 담당하며,
 * Infrastructure Repository 기반으로 변경 로직을 제공합니다.
 */
public interface NotificationWriter {

    /**
     * Notification 저장 (생성/수정)
     *
     * @param notification 저장할 Notification
     * @return 저장된 Notification
     */
    Notification upsert(Notification notification);

    /**
     * ID로 Notification 삭제
     *
     * @param identity Notification 식별자
     */
    void delete(NotificationIdentity identity);
}
