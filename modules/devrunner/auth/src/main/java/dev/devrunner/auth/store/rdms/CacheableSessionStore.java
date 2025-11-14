package dev.devrunner.auth.store.rdms;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.auth.store.SessionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class CacheableSessionStore implements SessionStore {

    private final SessionEntityRepository sessionEntityRepository;

    // Caffeine Cache with TTL (3일 자동 만료)
    private final Cache<String, SessionUser> sessions = Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.DAYS)  // 3일 후 자동 삭제
            .maximumSize(100_000)  // 최대 10만개 (메모리 보호)
            .removalListener((key, value, cause) -> {
                if (value != null) {
                    log.debug("Session removed from cache: sessionId={}, cause={}", key, cause);
                }
            })
            .build();

    @Override
    @Transactional
    public String createSession(SessionUser user, Duration ttl) {
        var now = Instant.now();
        var expiresAt = now.plus(ttl);

        // 1. 기존 세션 모두 삭제 (Single Session Policy)
        var existingSessions = sessionEntityRepository.findByUserId(user.getUserId());
        for (var session : existingSessions) {
            sessions.invalidate(session.getSessionKey());
            log.debug("Previous session removed from cache: sessionId={}", session.getSessionKey());
        }

        if (!existingSessions.isEmpty()) {
            long deletedCount = sessionEntityRepository.deleteByUserId(user.getUserId());
            log.info("Previous sessions deleted from DB: userId={}, count={}",
                    user.getUserId(), deletedCount);
        }

        // 2. 새 세션 생성
        var sessionEntity = SessionEntity.newOne(
                newSessionId(), user.getUserId(), now, expiresAt
        );
        var created = sessionEntityRepository.save(sessionEntity);

        // 3. 캐시에 저장 (다음 조회 시 DB hit 방지)
        var sessionUser = SessionUser.of(user.getUserId(), now, expiresAt);
        sessions.put(created.getSessionKey(), sessionUser);

        log.info("New session created: sessionId={}, userId={}",
                created.getSessionKey(), user.getUserId());

        return created.getSessionKey();
    }

    @Override
    public Optional<SessionUser> getSession(String sessionId) {
        var now = Instant.now();

        // 1. 캐시 확인 (Caffeine이 TTL 자동 관리)
        var cached = sessions.getIfPresent(sessionId);
        if (cached != null) {
            // 캐시가 만료되었다고 의심됨 → DB 확인
            if (cached.getExpireAt().isBefore(now)) {
                log.debug("Cache expired, checking DB: sessionId={}", sessionId);
                // 캐시 무효화하고 DB로 넘어감
                sessions.invalidate(sessionId);
                // 아래 DB 조회 로직으로 fall-through
            } else {
                // 캐시 유효
                return Optional.of(cached);
            }
        }

        // 2. DB 조회 (캐시 miss 또는 캐시 만료)
        var queried = sessionEntityRepository.findById(sessionId);
        if (queried.isEmpty()) {
            return Optional.empty();
        }

        var sessionEntity = queried.get();

        // 3. DB 데이터 만료 체크
        if (sessionEntity.getExpiresAt().isBefore(now)) {
            sessionEntityRepository.deleteById(sessionId);
            log.debug("Expired session removed: sessionId={}", sessionId);
            return Optional.empty();
        }

        // 4. 유효한 세션 캐싱 (갱신 포함)
        var sessionUser = new SessionUser(
                sessionEntity.getUserId(),
                sessionEntity.getCreatedAt(),
                sessionEntity.getExpiresAt()
        );
        sessions.put(sessionId, sessionUser);

        return Optional.of(sessionUser);
    }

    @Override
    public void deleteSession(String sessionId) {
        sessions.invalidate(sessionId);
        sessionEntityRepository.deleteById(sessionId);
    }

    @Override
    public void extendSession(String sessionId, Duration ttl) {
        var queried = sessionEntityRepository.findById(sessionId);

        if (queried.isEmpty()) {
            log.debug("Cannot extend session - not found: sessionId={}", sessionId);
            return;
        }

        var sessionEntity = queried.get();
        var newExpiresAt = sessionEntity.getExpiresAt().plus(ttl);

        // 1. DB 먼저 업데이트
        sessionEntityRepository.save(
                SessionEntity.newOne(
                        sessionEntity.getSessionKey(),
                        sessionEntity.getUserId(),
                        sessionEntity.getCreatedAt(),
                        newExpiresAt
                )
        );

        // 2. 캐시 강제 갱신 (무조건 덮어씀)
        var updatedSessionUser = new SessionUser(
                sessionEntity.getUserId(),
                sessionEntity.getCreatedAt(),
                newExpiresAt
        );
        sessions.put(sessionId, updatedSessionUser);

        log.debug("Session extended in DB and cache: sessionId={}, newExpiresAt={}",
                sessionId, newExpiresAt);
    }

    private String newSessionId() {
        var sessionId = UUID.randomUUID().toString();
        var iter = 0;

        while (sessionEntityRepository.findById(sessionId).isPresent()) {
            sessionId = UUID.randomUUID().toString();
            iter++;
            if (iter > 5)
                throw new RuntimeException("");
        }

        return sessionId;
    }

}
