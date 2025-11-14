package dev.devrunner.crawler.task.job.contentGenerator;

import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.ContentGenerateFacade;
import dev.devrunner.crawler.step.CrawlJobContentEntity;
import dev.devrunner.crawler.step.CrawlJobContentRepository;
import dev.devrunner.crawler.step.CrawlJobUrlEntity;
import dev.devrunner.crawler.step.CrawlJobUrlRepository;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.CrawlStatus;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.ExperienceRequirement;
import dev.devrunner.model.job.Job;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Job Content Generator
 * <p>
 * Step 2(crawl_job_contents)에서 WAIT 상태인 콘텐츠를 하나씩 가져와서:
 * 1. ContentGenerateFacade로 Job 생성
 * 2. jobs 테이블에 저장
 * 3. crawl_job_contents.job_id 업데이트
 * 4. crawl_job_contents.status → SUCCESS
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobContentGenerator {

    private final ContentGenerateFacade facade;
    private final CrawlJobContentRepository contentRepository;
    private final CrawlJobUrlRepository urlRepository;
    private final JobRepository jobRepository;

    private final OutboxEventRecorder outboxEventRecorder;
    /**
     * WAIT 상태의 콘텐츠를 순차 처리
     */
    public void run() {
        var contentEntityOpt = contentRepository.findFirstWaitingContent();
        if (contentEntityOpt.isEmpty()) {
            log.debug("No waiting contents found");
            return;
        }

        CrawlJobContentEntity contentEntity = contentEntityOpt.get();
        log.info("Processing content: id={}, urlId={}", contentEntity.getId(), contentEntity.getUrlId());

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

            RecordOutboxEventCommand command =  RecordOutboxEventCommand.created(TargetType.JOB, savedJob.getJobId());

            outboxEventRecorder.record(command);

            log.info("Job upserted successfully with outbox event: {}", savedJob.getJobId());


            // 4. crawl_job_contents 업데이트 (job_id 설정, status → SUCCESS)
            CrawlJobContentEntity updatedContent = new CrawlJobContentEntity(
                    contentEntity.getId(),
                    contentEntity.getUrlId(),
                    contentEntity.getMarkdownContent(),
                    contentEntity.getShortenedContent(),
                    CrawlStatus.SUCCESS,
                    savedJob.getJobId(),
                    null,
                    contentEntity.getRetryCount(),
                    contentEntity.getCreatedAt(),
                    Instant.now()
            );
            contentRepository.save(updatedContent);
            log.info("Updated content status to SUCCESS: id={}, jobId={}",
                    contentEntity.getId(), savedJob.getJobId());

        } catch (Exception e) {
            log.error("Failed to generate job: contentId={}, error={}",
                    contentEntity.getId(), e.getMessage(), e);

            // 5. 실패 시 status → FAILED, error_message 기록
            CrawlJobContentEntity failedContent = new CrawlJobContentEntity(
                    contentEntity.getId(),
                    contentEntity.getUrlId(),
                    contentEntity.getMarkdownContent(),
                    contentEntity.getShortenedContent(),
                    CrawlStatus.FAILED,
                    null,
                    e.getMessage(),
                    contentEntity.getRetryCount(),
                    contentEntity.getCreatedAt(),
                    Instant.now()
            );
            contentRepository.save(failedContent);
            log.warn("Updated content status to FAILED: id={}", contentEntity.getId());
        }
    }
}
