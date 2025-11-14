package dev.devrunner.auth.store;

import dev.devrunner.auth.model.SessionUser;

import java.time.Duration;
import java.util.Optional;

/**
 * 세션 저장소 인터페이스
 *
 * 구현체:
 * - InMemorySessionStore: 인메모리 저장소 (개발/단일 인스턴스용)
 * - RedisSessionStore: Redis 저장소 (프로덕션/멀티 인스턴스용) - 추후 구현
 */
public interface SessionStore {

    /**
     * 세션 생성 및 저장
     *
     * @param user 세션에 저장할 유저 정보
     * @param ttl  세션 만료 시간
     * @return 생성된 세션 ID
     */
    String createSession(SessionUser user, Duration ttl);

    /**
     * 세션 조회
     *
     * @param sessionId 세션 ID
     * @return 세션 유저 정보 (만료되었거나 존재하지 않으면 empty)
     */
    Optional<SessionUser> getSession(String sessionId);

    /**
     * 세션 삭제 (로그아웃)
     *
     * @param sessionId 세션 ID
     */
    void deleteSession(String sessionId);

    /**
     * 세션 만료 시간 연장
     *
     * @param sessionId 세션 ID
     * @param ttl       연장할 시간
     */
    void extendSession(String sessionId, Duration ttl);
}
