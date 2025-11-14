package dev.devrunner.auth.scheduler;

import dev.devrunner.auth.store.rdms.SessionEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 세션 정리 스케줄러
 *
 * 만료된 세션을 주기적으로 정리합니다.
 * - 실시간 정리: getSession() 호출 시 만료된 세션은 즉시 삭제 (주 정리 방식)
 * - 배치 정리: 스케줄러가 미처리된 만료 세션을 일괄 정리 (보조 수단)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final SessionEntityRepository sessionEntityRepository;

    /**
     * 만료된 세션 배치 정리
     *
     * 실행 시간: 매일 새벽 3시
     * - 트래픽이 적은 시간대 선택
     * - 로그인 기한 3일 기준, 하루 1번 정리면 충분
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredSessions() {
        log.info("Starting expired session cleanup...");

        try {
            long deleted = sessionEntityRepository.deleteByExpiresAtBefore(Instant.now());

            if (deleted > 0) {
                log.info("Cleaned up {} expired sessions", deleted);
            } else {
                log.debug("No expired sessions to clean up");
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired sessions", e);
        }
    }
}
