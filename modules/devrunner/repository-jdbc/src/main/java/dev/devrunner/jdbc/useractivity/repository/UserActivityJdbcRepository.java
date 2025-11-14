package dev.devrunner.jdbc.useractivity.repository;

import dev.devrunner.model.useractivity.UserActivity;
import dev.devrunner.model.useractivity.UserActivityIdentity;
import dev.devrunner.infra.useractivity.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * UserActivity Repository 구현체
 *
 * 헥사고날 아키텍처에서 Adapter 역할을 수행하며,
 * Infrastructure의 UserActivityRepository 인터페이스를
 * Spring Data JDBC를 활용하여 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class UserActivityJdbcRepository implements UserActivityRepository {

    private final UserActivityEntityRepository entityRepository;

    @Override
    public Optional<UserActivity> findById(UserActivityIdentity identity) {
        return entityRepository.findById(identity.getUserActivityId())
                .map(this::toDomain);
    }

    @Override
    public UserActivity save(UserActivity userActivity) {
        UserActivityEntity entity = toEntity(userActivity);
        UserActivityEntity saved = entityRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UserActivityIdentity identity) {
        entityRepository.deleteById(identity.getUserActivityId());
    }

    @Override
    public boolean existsById(UserActivityIdentity identity) {
        return entityRepository.existsById(identity.getUserActivityId());
    }

    @Override
    public List<UserActivity> findAll() {
        return StreamSupport.stream(entityRepository.findAll().spliterator(), false)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserActivity> findByUserId(Long userId) {
        return entityRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Entity ↔ Domain 변환 메서드 (Model 스펙 기반 자동 생성)
     */
    private UserActivity toDomain(UserActivityEntity entity) {
        return new UserActivity(
                entity.getId(),
                entity.getUserId(),
                entity.getActivityType(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getTargetTitle(),
                entity.getTargetCompany(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserActivityEntity toEntity(UserActivity domain) {
        return new UserActivityEntity(
                domain.getUserActivityId(),
                domain.getUserId(),
                domain.getActivityType(),
                domain.getTargetType(),
                domain.getTargetId(),
                domain.getTargetTitle(),
                domain.getTargetCompany(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
