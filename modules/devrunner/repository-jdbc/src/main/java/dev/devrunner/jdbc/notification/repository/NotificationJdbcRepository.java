package dev.devrunner.jdbc.notification.repository;

import dev.devrunner.model.notification.Notification;
import dev.devrunner.model.notification.NotificationIdentity;
import dev.devrunner.infra.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Notification Repository 구현체
 *
 * 헥사고날 아키텍처에서 Adapter 역할을 수행하며,
 * Infrastructure의 NotificationRepository 인터페이스를
 * Spring Data JDBC를 활용하여 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class NotificationJdbcRepository implements NotificationRepository {

    private final NotificationEntityRepository entityRepository;

    @Override
    public Optional<Notification> findById(NotificationIdentity identity) {
        return entityRepository.findById(identity.getNotificationId())
                .map(this::toDomain);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = toEntity(notification);
        NotificationEntity saved = entityRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(NotificationIdentity identity) {
        entityRepository.deleteById(identity.getNotificationId());
    }

    @Override
    public boolean existsById(NotificationIdentity identity) {
        return entityRepository.existsById(identity.getNotificationId());
    }

    @Override
    public List<Notification> findAll() {
        return StreamSupport.stream(entityRepository.findAll().spliterator(), false)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return entityRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Entity ↔ Domain 변환 메서드 (Model 스펙 기반 자동 생성)
     */
    private Notification toDomain(NotificationEntity entity) {
        return new Notification(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getTitle(),
                entity.getContent(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getActorUserId(),
                entity.getIsRead(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private NotificationEntity toEntity(Notification domain) {
        return new NotificationEntity(
                domain.getNotificationId(),
                domain.getUserId(),
                domain.getType(),
                domain.getTitle(),
                domain.getContent(),
                domain.getTargetType(),
                domain.getTargetId(),
                domain.getActorUserId(),
                domain.getIsRead(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
