package dev.devrunner.service.notification.impl;

// import dev.devrunner.exception.notification.NotificationNotFoundException;
import dev.devrunner.model.notification.Notification;
import dev.devrunner.model.notification.NotificationIdentity;
import dev.devrunner.service.notification.NotificationReader;
import dev.devrunner.infra.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Notification 도메인 조회 서비스 구현체
 *
 * CQRS 패턴의 Query 책임을 구현하며,
 * Infrastructure Repository를 활용한 조회 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationReader implements NotificationReader {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification getById(NotificationIdentity identity) {
        log.debug("Fetching Notification by id: {}", identity.getNotificationId());
        return notificationRepository.findById(identity)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + identity));
                // .orElseThrow(() -> new NotificationNotFoundException(identity));
    }

    @Override
    public List<Notification> getAll() {
        log.debug("Fetching all Notifications");
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> getByUserId(Long userId) {
        log.debug("Fetching Notifications by userId: {}", userId);
        return notificationRepository.findByUserId(userId);
    }
}
