package dev.devrunner.crawler.task.job.closedCheck;


import dev.devrunner.crawler.step.closedCheck.JobClosedCheckEntity;
import dev.devrunner.crawler.step.closedCheck.JobClosedCheckRepository;
import dev.devrunner.crawler.task.job.closedCheck.validator.JobClosureDetector;
import dev.devrunner.crawler.task.job.closedCheck.validator.JobContentValidator;
import dev.devrunner.crawler.task.job.contentCrawler.JobPageReader;
import dev.devrunner.crawler.task.job.contentCrawler.parser.GeneralJobContentShortener;
import dev.devrunner.jdbc.job.repository.JobEntity;
import dev.devrunner.jdbc.job.repository.JobEntityRepository;
import dev.devrunner.openai.base.GptParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Closed Job Processor
 * <p>
 * 개별 Job의 마감 여부를 확인하고 처리합니다.
 * <p>
 * 주요 기능:
 * 1. JobPageReader로 페이지 내용 읽기
 * 2. check_is_closed로 마감 여부 확인
 * 3. job_closed_checks 테이블에 체크 이력 저장
 * 4. 마감 상태에 따라 Job 상태 업데이트
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClosedJobProcessor {

    private final JobPageReader jobPageReader;
    private final GeneralJobContentShortener shortener;
    private final JobContentValidator jobContentValidator;
    private final JobClosureDetector jobClosureDetector;
    private final JobClosedCheckRepository jobClosedCheckRepository;

    /**
     * Job 마감 여부 확인 및 처리
     *
     * @param jobEntity 확인할 Job Entity
     */
    public void process(JobEntity jobEntity) {
        log.info("Checking job closure: id={}, company={}, url={}",
                jobEntity.getId(), jobEntity.getCompany(), jobEntity.getUrl());

        try {
            // 1. JobPageReader로 페이지 내용 읽기
            String longMarkdown = jobPageReader.read(jobEntity.getUrl(), jobEntity.getCompany());
            log.debug("Read page content: length={}", longMarkdown.length());

            var markdown = shortener.run(GptParams.ofMini(longMarkdown));

            // 2. check_is_closed로 마감 여부 확인
            ClosedCheckResult result = checkIsClosed(markdown, jobEntity.getTitle(), jobEntity.getUrl());
            log.info("Check result: closed={}, reason={}", result.closed(), result.closedReason());

            // 3. job_closed_checks 테이블에 체크 이력 저장 (TODO: 구현 필요)
            saveCheckHistory(jobEntity, result);

            // 4. 마감 상태에 따라 Job 상태 업데이트 (TODO: 구현 필요)
            updateJobStatus(jobEntity, result);

            log.info("Successfully checked job closure: id={}", jobEntity.getId());

        } catch (Exception e) {
            log.error("Failed to check job closure: id={}, url={}, error={}",
                    jobEntity.getId(), jobEntity.getUrl(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 마크다운 콘텐츠로 마감 여부 확인
     * <p>
     * 1단계: JobContentValidator로 채용 공고 형식 검증
     * 2단계: JobClosureDetector로 마감 상태 감지
     */
    private ClosedCheckResult checkIsClosed(String markdown, String title, String url) {
        // 1단계: 채용 공고 형식 검증
        JobContentValidator.ValidationResult validationResult =
                jobContentValidator.run(GptParams.ofMini(markdown));

        if (!validationResult.validHiringContent()) {
            return new ClosedCheckResult(true, ClosedReason.CANNOT_READ_PAGE, "can not read page on job content validator");
        }

        // 2단계: 마감 상태 감지
        String userPrompt = jobClosureDetector.buildUserPrompt(markdown, title, url);
        JobClosureDetector.DetectionResult detectionResult =
                jobClosureDetector.run(GptParams.ofMini(userPrompt));

        // DetectionResult를 ClosedCheckResult로 변환
        return convertToClosedCheckResult(detectionResult);
    }

    /**
     * DetectionResult를 ClosedCheckResult로 변환
     */
    private ClosedCheckResult convertToClosedCheckResult(JobClosureDetector.DetectionResult detectionResult) {
        String state = detectionResult.state();
        String reason = detectionResult.reason();

        // CLOSED 상태 처리
        if ("CLOSED".equals(state)) {
            if ("deadline_passed".equals(reason)) {
                return new ClosedCheckResult(true, ClosedReason.EXPIRED,
                        "Deadline has passed");
            } else if ("explicit_closed".equals(reason)) {
                return new ClosedCheckResult(true, ClosedReason.CLOSED,
                        "Explicitly marked as closed");
            } else {
                return new ClosedCheckResult(true, ClosedReason.NOT_HIRING,
                        "No longer hiring: " + reason);
            }
        }

        // OPEN 상태 처리
        if ("OPEN".equals(state)) {
            return new ClosedCheckResult(false, ClosedReason.NONE,
                    "Still open: " + reason);
        }

        // UNKNOWN 상태 처리
        log.warn("Unknown closure state detected: state={}, reason={}", state, reason);
        return new ClosedCheckResult(false, ClosedReason.NONE,
                "Unknown state: " + reason);
    }

    /**
     * 체크 이력 저장
     */
    private void saveCheckHistory(JobEntity jobEntity, ClosedCheckResult result) {
        JobClosedCheckEntity checkEntity = JobClosedCheckEntity.create(
                jobEntity.getId(),
                jobEntity.getUrl(),
                result.closed(),
                result.closedReason() != null ? result.closedReason().name() : null,
                result.explanation()
        );

        jobClosedCheckRepository.save(checkEntity);
        log.debug("Saved check history: jobId={}, closed={}, reason={}",
                jobEntity.getId(), result.closed(), result.closedReason());
    }

    /**
     * Job 상태 업데이트
     * <p>
     * - NOT_HIRING/EXPIRED/CLOSED: 즉시 마감 처리
     * - CANNOT_READ_PAGE: 실패 횟수 확인 후 2회 이상이면 마감 처리
     */
    private void updateJobStatus(JobEntity jobEntity, ClosedCheckResult result) {
        if (!result.closed()) {
            log.debug("Job is still open: id={}", jobEntity.getId());
            return;
        }

        ClosedReason reason = result.closedReason();

        // NOT_HIRING, EXPIRED, CLOSED인 경우 즉시 마감 처리
        if (reason == ClosedReason.NOT_HIRING ||
            reason == ClosedReason.EXPIRED ||
            reason == ClosedReason.CLOSED) {

            closeJob(jobEntity, reason);
            log.info("Job closed immediately: id={}, reason={}", jobEntity.getId(), reason);
            return;
        }

        // CANNOT_READ_PAGE인 경우 실패 횟수 확인
        if (reason == ClosedReason.CANNOT_READ_PAGE) {
            int failCount = jobClosedCheckRepository.countCannotReadPageByJobId(jobEntity.getId());
            log.debug("CANNOT_READ_PAGE fail count: jobId={}, count={}", jobEntity.getId(), failCount);

            if (failCount >= 2) {
                closeJob(jobEntity, reason);
                log.info("Job closed due to multiple read failures: id={}, failCount={}",
                        jobEntity.getId(), failCount);
            } else {
                log.info("Job not closed yet, fail count below threshold: id={}, failCount={}",
                        jobEntity.getId(), failCount);
            }
            return;
        }

        log.debug("No status update needed: id={}, reason={}", jobEntity.getId(), reason);
    }

    /**
     * Job을 마감 상태로 변경
     */
    private void closeJob(JobEntity jobEntity, ClosedReason reason) {
        /* 실제론 저장 안함. 11월 첫재주, 둘쨰주는 테스트만 */
        /*
        jobEntity.setIsClosed(true);
        jobEntityRepository.save(jobEntity);

         */
        log.info("Job marked as closed: id={}, reason={}", jobEntity.getId(), reason);


    }

    /**
     * 채용 공고 마감 체크 결과
     */
    public record ClosedCheckResult(
            boolean closed,
            ClosedReason closedReason,
            String explanation
    ) {
    }

}
