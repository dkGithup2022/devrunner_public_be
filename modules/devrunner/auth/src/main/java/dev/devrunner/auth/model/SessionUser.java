package dev.devrunner.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * 세션에 저장되는 간이 유저 정보
 * <p>
 * SecurityContext에 저장되어 인증된 사용자 정보를 표현합니다.
 */
@Getter
@AllArgsConstructor
public class SessionUser {
    private final Long userId;
    private final Instant loginAt;

    private final Instant expireAt;

    /**
     * 세션 유저 생성
     */
    public static SessionUser of(Long userId, Instant loginAt, Instant expireAt) {
        return new SessionUser(userId, loginAt, expireAt);
    }
}
