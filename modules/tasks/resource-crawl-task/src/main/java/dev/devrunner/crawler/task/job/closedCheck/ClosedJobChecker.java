package dev.devrunner.crawler.task.job.closedCheck;

import dev.devrunner.jdbc.job.repository.JobEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Closed Job Checker
 * <p>
 * open 상태의 모든 Job을 가져와서 마감 여부를 확인합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClosedJobChecker {

    private final JobEntityRepository jobEntityRepository;
    private final ClosedJobProcessor processor;

    /**
     * open 상태(is_closed = false)의 모든 Job을 처리
     */
    public void run() {
        var openJobs = jobEntityRepository.findAllOpenJobs();
        log.info("Found {} open jobs to check", openJobs.size());

        if (openJobs.isEmpty()) {
            log.debug("No open jobs found");
            return;
        }

        for (var job : openJobs) {
            try {
                processor.process(job);
            } catch (Exception e) {
                log.error("Failed to process job: id={}, url={}", job.getId(), job.getUrl(), e);
                // 개별 Job 처리 실패해도 계속 진행
            }
        }

        log.info("Finished checking {} open jobs", openJobs.size());
    }
}
