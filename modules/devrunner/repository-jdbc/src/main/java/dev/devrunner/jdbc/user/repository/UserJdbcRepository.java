package dev.devrunner.jdbc.user.repository;

import dev.devrunner.jdbc.user.repository.embedded.NotificationSettingsEmbeddable;
import dev.devrunner.jdbc.user.repository.embedded.UserMetricsEmbeddable;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.model.user.UserMetrics;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.encryption.EmailEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * User Repository 구현체
 * <p>
 * 헥사고날 아키텍처에서 Adapter 역할을 수행하며,
 * Infrastructure의 UserRepository 인터페이스를
 * Spring Data JDBC를 활용하여 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class UserJdbcRepository implements UserRepository {

    private final UserEntityRepository entityRepository;
    private final EmailEncryptor emailEncryptor;

    @Override
    public Optional<User> findById(UserIdentity identity) {
        return entityRepository.findById(identity.getUserId())
                .map(this::toDomain);
    }

    @Override
    public List<User> findAllByIdIn(List<UserIdentity> identities) {
        var ids = identities.stream().map(UserIdentity::getUserId).collect(Collectors.toList());
        var users = entityRepository.findAllByIdIn(ids);
        return users.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = entityRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UserIdentity identity) {
        entityRepository.deleteById(identity.getUserId());
    }

    @Override
    public boolean existsById(UserIdentity identity) {
        return entityRepository.existsById(identity.getUserId());
    }

    @Override
    public List<User> findAll() {
        return StreamSupport.stream(entityRepository.findAll().spliterator(), false)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        return entityRepository.findByGoogleId(googleId)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // 평문 이메일을 암호화해서 DB 검색
        String encryptedEmail = emailEncryptor.encrypt(email);
        return entityRepository.findByEmail(encryptedEmail)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return entityRepository.findByNickname(nickname)
                .map(this::toDomain);
    }

    /**
     * Entity ↔ Domain 변환 메서드
     * Spring Data JDBC가 자동으로 컬렉션과 embedded 객체를 처리
     */
    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getGoogleId(),
                emailEncryptor.decrypt(entity.getEmail()),  // DB 암호문 → 평문으로 복호화
                entity.getNickname(),
                entity.getUserRole(),
                entity.getInterestedCompanies() != null ?
                        entity.getInterestedCompanies().stream()
                                .map(InterestedCompany::getCompanyName)
                                .collect(Collectors.toList()) : List.of(),
                entity.getInterestedLocations() != null ?
                        entity.getInterestedLocations().stream()
                                .map(InterestedLocation::getLocationName)
                                .collect(Collectors.toList()) : List.of(),
                entity.getNotificationSettings() != null ?
                        entity.getNotificationSettings().toDomain() : null,
                entity.getMetrics() != null ?
                        entity.getMetrics().toDomain() : UserMetrics.initial(),
                entity.getLastLoginAt(),
                entity.getIsActive(),
                entity.getIsWithdrawn(),
                entity.getWithdrawnAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserEntity toEntity(User domain) {
        return new UserEntity(
                domain.getUserId(),
                domain.getGoogleId(),
                emailEncryptor.encrypt(domain.getEmail()),  // 평문 → 암호문으로 암호화
                domain.getNickname(),
                domain.getUserRole(),
                domain.getInterestedCompanies() != null ?
                        domain.getInterestedCompanies().stream()
                                .map(InterestedCompany::new)
                                .collect(Collectors.toSet()) : new HashSet<>(),
                domain.getInterestedLocations() != null ?
                        domain.getInterestedLocations().stream()
                                .map(InterestedLocation::new)
                                .collect(Collectors.toSet()) : new HashSet<>(),
                NotificationSettingsEmbeddable.from(domain.getNotificationSettings()),
                UserMetricsEmbeddable.from(domain.getMetrics()),
                domain.getLastLoginAt(),
                domain.getIsActive(),
                domain.getIsWithdrawn(),
                domain.getWithdrawnAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
