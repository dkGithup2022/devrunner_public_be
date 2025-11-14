package dev.devrunner.application.batch.cron.check;

import dev.devrunner.crawler.task.job.closedCheck.ClosedJobChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.devrunner.application.batch.ScheduleUtils.executeBatchTask;

/**
 * Closed Job Check Task
 * <p>
 * open 상태의 Job들을 주기적으로 검사하여 마감 여부를 확인합니다.
 * - 3일마다 새벽 5시에 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClosedJobCheckTask {

    private final ClosedJobChecker closedJobChecker;
    private static final AtomicBoolean CLOSED_JOB_CHECK_RUNNING = new AtomicBoolean(false);

    /**
     * Closed Job Check
     * - 프로덕션: 3일마다 새벽 5시에 실행 (매월 1, 4, 7, 10, 13, 16, 19, 22, 25, 28일)
     * - 로컬: 주석 처리된 initialDelay로 테스트 가능
     */
    @Scheduled(cron = "0 0 5 1,4,7,10,13,16,19,22,25,28 * ?")  // 프로덕션용: 3일마다 새벽 5시
    //@Scheduled(initialDelay = 10000, fixedDelay = 259200000)  // 로컬용: 10초 후, 3일 후 재실행
    public void checkClosedJobs() {
        executeBatchTask(CLOSED_JOB_CHECK_RUNNING, "check_closed_jobs", closedJobChecker::run);
    }
}
