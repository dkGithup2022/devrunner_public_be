package dev.devrunner.crawler.task.job.contentGenerator;

import dev.devrunner.crawler.step.CrawlJobContentEntity;
import dev.devrunner.crawler.step.CrawlJobContentRepository;
import dev.devrunner.crawler.step.CrawlJobUrlEntity;
import dev.devrunner.crawler.step.CrawlJobUrlRepository;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.ContentGenerateFacade;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.CrawlStatus;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.Job;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Failed Job Content Generator
 * <p>
 * Step 2(crawl_job_contents)에서 FAILED 상태이고 retry_count < 3인 콘텐츠를 재처리:
 * 1. ContentGenerateFacade로 Job 생성
 * 2. jobs 테이블에 저장
 * 3. crawl_job_contents.job_id 업데이트
 * 4. crawl_job_contents.status → SUCCESS (retry_count 증가)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FailedJobContentGenerator {

    private final ContentGenerateFacade facade;
    private final CrawlJobContentRepository contentRepository;
    private final CrawlJobUrlRepository urlRepository;
    private final JobRepository jobRepository;
    private final OutboxEventRecorder outboxEventRecorder;

    /**
     * FAILED 상태의 콘텐츠를 재처리 (retry_count < 3)
     */
    public void run() {
        var failedContents = contentRepository.findFailedContentsForRetry(3, 100);
        log.info("Found {} failed contents for retry", failedContents.size());
        if (failedContents.isEmpty()) {
            return;
        }

        for (var contentEntity : failedContents) {
            processContent(contentEntity);

            // Rate limiting: 3분 대기
            try {
                Thread.sleep(1000 * 60 * 3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 단일 콘텐츠 처리
     */
    private void processContent(CrawlJobContentEntity contentEntity) {
        log.info("Processing failed content: id={}, urlId={}, retryCount={}",
                contentEntity.getId(), contentEntity.getUrlId(), contentEntity.getRetryCount());

        try {
            // 1. URL 정보 조회 (company, url, title)
            var urlEntityOpt = urlRepository.findById(contentEntity.getUrlId());
            if (urlEntityOpt.isEmpty()) {
                throw new IllegalStateException("URL entity not found: urlId=" + contentEntity.getUrlId());
            }
            CrawlJobUrlEntity urlEntity = urlEntityOpt.get();

            // 2. ContentGenerateFacade로 Job 생성
            Job job = facade.generate(
                    contentEntity.getShortenedContent(),
                    urlEntity.getUrl(),
                    urlEntity.getTitle(),
                    Company.valueOf(urlEntity.getCompany().name())
            );

            // 3. jobs 테이블에 저장
            Job savedJob = jobRepository.save(job);
            log.info("Saved job: jobId={}, url={}", savedJob.getJobId(), savedJob.getUrl());

            // 4. Outbox 이벤트 기록
            RecordOutboxEventCommand command = RecordOutboxEventCommand.created(TargetType.JOB, savedJob.getJobId());
            outboxEventRecorder.record(command);
            log.info("Job upserted successfully with outbox event: {}", savedJob.getJobId());

            // 5. crawl_job_contents 업데이트 (job_id 설정, status → SUCCESS, retry_count 증가)
            CrawlJobContentEntity updatedContent = new CrawlJobContentEntity(
                    contentEntity.getId(),
                    contentEntity.getUrlId(),
                    contentEntity.getMarkdownContent(),
                    contentEntity.getShortenedContent(),
                    CrawlStatus.SUCCESS,
                    savedJob.getJobId(),
                    null,  // errorMessage 초기화
                    contentEntity.getRetryCount() ,  // retry_count 증가
                    contentEntity.getCreatedAt(),
                    Instant.now()
            );
            contentRepository.save(updatedContent);
            log.info("Updated content status to SUCCESS: id={}, jobId={}, retryCount={}",
                    contentEntity.getId(), savedJob.getJobId(), updatedContent.getRetryCount());

        } catch (Exception e) {
            log.error("Failed to generate job: contentId={}, error={}",
                    contentEntity.getId(), e.getMessage(), e);

            // 6. 실패 시 status → FAILED, error_message 기록, retry_count 증가
            CrawlJobContentEntity failedContent = new CrawlJobContentEntity(
                    contentEntity.getId(),
                    contentEntity.getUrlId(),
                    contentEntity.getMarkdownContent(),
                    contentEntity.getShortenedContent(),
                    CrawlStatus.FAILED,
                    null,
                    e.getMessage(),
                    contentEntity.getRetryCount() + 1,  // retry_count 증가
                    contentEntity.getCreatedAt(),
                    Instant.now()
            );
            contentRepository.save(failedContent);
            log.warn("Updated content status to FAILED: id={}, retryCount={}",
                    contentEntity.getId(), failedContent.getRetryCount());
        }
    }
}
