package dev.devrunner.application.batch;


import dev.devrunner.logging.LogContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ScheduleUtils {
    public static void setLogContext(String jobName) {
        LogContext.setBatchContext(jobName);
    }

    // ✅ 공통 배치 실행 함수 (중복 제거 + 실행 시간 측정 + 로깅 개선)
    public static void executeBatchTask(AtomicBoolean taskFlag, String taskName, Runnable task) {
        if (!taskFlag.compareAndSet(false, true)) {
            log.warn("{} 이미 실행 중 - 중복 실행 방지", taskName);
            return;
        }

        setLogContext(taskName);

        long startTime = System.currentTimeMillis();
        log.info("{} 실행 시작...", taskName);

        try {
            task.run(); // ✅ 실제 작업 실행
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("{} 완료! (실행 시간: {} ms)", taskName, elapsedTime);
        } catch (Exception e) {
            log.error("{} 실행 중 오류 발생", taskName, e);

            // Root cause 체인 출력 (스택트레이스 포함)
            Throwable cause = e.getCause();
            int depth = 1;
            while (cause != null) {
                log.error("  Caused by [{}]: {}", depth++, cause.getClass().getName() + ": " + cause.getMessage(), cause);
                cause = cause.getCause();
            }

            // SQL 예외의 경우 추가 정보
            if (e instanceof org.springframework.jdbc.BadSqlGrammarException) {
                org.springframework.jdbc.BadSqlGrammarException sqlEx = (org.springframework.jdbc.BadSqlGrammarException) e;
                log.error("  SQL: {}", sqlEx.getSql());
            }
        } finally {
            taskFlag.set(false); // ✅ 작업 종료 후 플래그 해제
            LogContext.clear();
        }
    }
}
