package dev.devrunner.service.notification;

import dev.devrunner.model.notification.Notification;
import dev.devrunner.model.notification.NotificationIdentity;

import java.util.List;

/**
 * Notification 도메인 조회 서비스 인터페이스
 *
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface NotificationReader {

    /**
     * ID로 Notification 조회
     *
     * @param identity Notification 식별자
     * @return Notification 엔티티
     */
    Notification getById(NotificationIdentity identity);

    /**
     * 모든 Notification 조회
     *
     * @return Notification 목록
     */
    List<Notification> getAll();

    /**
     * 사용자 ID로 Notification 목록 조회
     *
     * @param userId 사용자 ID
     * @return Notification 목록
     */
    List<Notification> getByUserId(Long userId);
}
