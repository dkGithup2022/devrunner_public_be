package dev.devrunner.model.user;

import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.NotificationSettings;
import lombok.Value;
import java.time.Instant;
import java.util.List;

@Value
public class User implements AuditProps {
    Long userId;
    String googleId;
    String email;
    String nickname;
    UserRole userRole;

    List<String> interestedCompanies;
    List<String> interestedLocations;
    NotificationSettings notificationSettings;

    UserMetrics metrics;

    Instant lastLoginAt;

    Boolean isActive;
    Boolean isWithdrawn;
    Instant withdrawnAt;

    Instant createdAt;
    Instant updatedAt;

    public static User newUser(String googleId, String email, String nickname) {
        Instant now = Instant.now();
        return new User(
            null,
            googleId,
            email,
            nickname,
            UserRole.USER,
            List.of(),
            List.of(),
            NotificationSettings.defaultSettings(),
            UserMetrics.initial(),
            now,
            true,
            false,
            null,
            now,
            now
        );
    }

    public User updateLastLogin() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics,
            Instant.now(), isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User incrementPostCount() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics.incrementPostCount(),
            lastLoginAt, isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User incrementCommentCount() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics.incrementCommentCount(),
            lastLoginAt, isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User incrementLikesReceived() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics.incrementLikesReceived(),
            lastLoginAt, isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User decrementLikesReceived() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics.decrementLikesReceived(),
            lastLoginAt, isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User incrementLikeGivenCount() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics.incrementLikeGivenCount(),
            lastLoginAt, isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User decrementLikeGivenCount() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics.decrementLikeGivenCount(),
            lastLoginAt, isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User incrementBookmarkCount() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics.incrementBookmarkCount(),
            lastLoginAt, isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User decrementBookmarkCount() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics.decrementBookmarkCount(),
            lastLoginAt, isActive, isWithdrawn, withdrawnAt, createdAt, Instant.now()
        );
    }

    public User withdraw() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics,
            lastLoginAt, isActive, true, Instant.now(), createdAt, Instant.now()
        );
    }

    public User reactivate() {
        return new User(
            userId, googleId, email, nickname, userRole,
            interestedCompanies, interestedLocations, notificationSettings,
            metrics,
            lastLoginAt, true, false, null, createdAt, Instant.now()
        );
    }

    // UserMetrics delegation methods for convenient access
    public Long getPostCount() {
        return metrics.getPostCount();
    }

    public Long getCommentCount() {
        return metrics.getCommentCount();
    }

    public Long getLikesReceived() {
        return metrics.getLikesReceived();
    }

    public Long getLikeGivenCount() {
        return metrics.getLikeGivenCount();
    }

    public Long getBookmarkCount() {
        return metrics.getBookmarkCount();
    }
}
