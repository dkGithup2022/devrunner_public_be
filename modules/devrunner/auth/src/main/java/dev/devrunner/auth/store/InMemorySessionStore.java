package dev.devrunner.auth.store;

import dev.devrunner.auth.model.SessionUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 세션 저장소
 *
 * 단일 인스턴스 환경에서 사용하기 위한 구현체입니다.
 * 서버 재시작 시 모든 세션이 삭제됩니다.
 *
 * 프로덕션 환경에서는 RedisSessionStore로 교체 필요합니다.
 */
//@Component
@Slf4j
public class InMemorySessionStore implements SessionStore {

    // ConcurrentHashMap으로 스레드 세이프 보장
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    @Getter
    @AllArgsConstructor
    private static class SessionData {
        private final SessionUser user;
        private final Instant expiresAt;

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    @Override
    public String createSession(SessionUser user, Duration ttl) {
        String sessionId = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(ttl);

        sessions.put(sessionId, new SessionData(user, expiresAt));

        log.info("Session created: sessionId={}, userId={}, expiresAt={}",
                sessionId, user.getUserId(), expiresAt);

        return sessionId;
    }

    @Override
    public Optional<SessionUser> getSession(String sessionId) {
        SessionData data = sessions.get(sessionId);

        if (data == null) {
            return Optional.empty();
        }

        // 만료된 세션은 자동 삭제
        if (data.isExpired()) {
            sessions.remove(sessionId);
            log.info("Session expired and removed: sessionId={}", sessionId);
            return Optional.empty();
        }

        return Optional.of(data.getUser());
    }

    @Override
    public void deleteSession(String sessionId) {
        SessionData removed = sessions.remove(sessionId);
        if (removed != null) {
            log.info("Session deleted: sessionId={}, userId={}",
                    sessionId, removed.getUser().getUserId());
        }
    }

    @Override
    public void extendSession(String sessionId, Duration ttl) {
        SessionData data = sessions.get(sessionId);
        if (data != null && !data.isExpired()) {
            Instant newExpiresAt = Instant.now().plus(ttl);
            sessions.put(sessionId, new SessionData(data.getUser(), newExpiresAt));
            log.info("Session extended: sessionId={}, newExpiresAt={}",
                    sessionId, newExpiresAt);
        }
    }

    /**
     * 주기적으로 만료된 세션 정리 (선택적)
     *
     * @Scheduled 어노테이션은 필요시 추가
     */
    public void cleanupExpiredSessions() {
        int removed = 0;
        for (Map.Entry<String, SessionData> entry : sessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                sessions.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.info("Cleaned up {} expired sessions", removed);
        }
    }
}
