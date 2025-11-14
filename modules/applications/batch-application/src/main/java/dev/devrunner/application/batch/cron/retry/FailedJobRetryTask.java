package dev.devrunner.application.batch.cron.retry;

import dev.devrunner.crawler.task.job.contentCrawler.FailedJobContentCrawler;
import dev.devrunner.crawler.task.job.contentGenerator.FailedJobContentGenerator;
import dev.devrunner.crawler.task.theirstack.FailedTheirStackJobGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.devrunner.application.batch.ScheduleUtils.executeBatchTask;

/**
 * Failed Job Retry Task
 * <p>
 * FAILED 상태의 크롤링/생성 작업을 주기적으로 재시도
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FailedJobRetryTask {

    private final FailedJobContentCrawler failedJobContentCrawler;
    private final FailedJobContentGenerator failedJobContentGenerator;
    private final FailedTheirStackJobGenerator failedTheirStackJobGenerator;

    private static final AtomicBoolean FAILED_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean FAILED_GENERATION_RUNNING = new AtomicBoolean(false);

    /**
     * FAILED URL 크롤링 재시도
     * - initialDelay: 1분 (60000ms)
     * - fixedDelay: 6시간 (21600000ms)
     */
    @Scheduled(initialDelay = 60000, fixedDelay = 21600000)
    public void retryFailedCrawling() {
        executeBatchTask(FAILED_CRAWL_RUNNING, "retry_failed_url_crawling", failedJobContentCrawler::run);
    }

    /**
     * FAILED Job 생성 재시도
     * - initialDelay: 2분 (120000ms)
     * - fixedDelay: 6시간 (21600000ms)
     */
    @Scheduled(initialDelay = 120000, fixedDelay = 21600000)
    public void retryFailedGeneration() {
        executeBatchTask(FAILED_GENERATION_RUNNING, "retry_failed_job_generation", () -> {
            failedJobContentGenerator.run();
            failedTheirStackJobGenerator.run();
        });
    }
}
