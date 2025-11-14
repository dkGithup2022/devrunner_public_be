package dev.devrunner.crawler.task.job.contentCrawler;

import dev.devrunner.crawler.step.CrawlJobContentEntity;
import dev.devrunner.crawler.step.CrawlJobContentRepository;
import dev.devrunner.crawler.step.CrawlJobUrlEntity;
import dev.devrunner.crawler.step.CrawlJobUrlRepository;
import dev.devrunner.crawler.task.job.contentCrawler.parser.*;
import dev.devrunner.crawler.task.job.contentCrawler.validator.HiringMarkDownValidator;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.CrawlStatus;
import dev.devrunner.openai.base.GptParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Job Content Processor
 * <p>
 * JobContentCrawler와 FailedJobContentCrawler의 공통 처리 로직을 담당
 * <p>
 * 주요 기능:
 * 1. JobPageReader로 Markdown 추출
 * 2. GPT로 본문 요약
 * 3. 유효성 검증 (길이 체크 + GPT 검증)
 * 4. crawl_job_contents 저장
 * 5. crawl_job_urls 상태 업데이트
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobContentProcessor {

    private final CrawlJobUrlRepository urlRepository;
    private final CrawlJobContentRepository contentRepository;
    private final JobPageReader jobPageReader;

    private final GeneralJobContentShortener generalShortener;
    private final GoogleJobContentShortener googleShortener;
    private final MetaJobContentShortener metaShortener;
    private final NetflixJobContentShortener netflixShortener;
    private final TikTokJobContentShortener tikTokShortener;
    private final SpotifyJobContentShortener spotifyShortener;

    private final HiringMarkDownValidator hiringMarkDownValidator;

    /**
     * URL 처리 (공통 로직)
     *
     * @param urlEntity 처리할 URL Entity
     */
    public void process(CrawlJobUrlEntity urlEntity) {
        log.info("Processing URL: id={}, company={}, url={}",
                urlEntity.getId(), urlEntity.getCompany(), urlEntity.getUrl());

        try {
            // 1. JobPageReader로 Markdown 추출
            String markdown = jobPageReader.read(urlEntity.getUrl(), urlEntity.getCompany());
            log.debug("Extracted markdown: length={}", markdown.length());

            // 2. GPT로 본문 요약
            String shortened = makeShort(urlEntity.getCompany(), markdown);

            // 3. 유효성 검증 (길이 체크 + GPT 검증)
            validateContent(shortened);

            // 4. crawl_job_contents 저장 (항상 새로 생성)
            saveContent(urlEntity, markdown, shortened);

            // 5. URL 상태를 SUCCESS로 업데이트
            updateUrlStatus(urlEntity, CrawlStatus.SUCCESS, null);

            log.info("Successfully processed URL: id={}", urlEntity.getId());

        } catch (Exception e) {
            log.error("Failed to process URL: id={}, url={}, error={}",
                    urlEntity.getId(), urlEntity.getUrl(), e.getMessage(), e);

            // URL 상태를 FAILED로 업데이트 (retry_count 증가)
            updateUrlStatus(urlEntity, CrawlStatus.FAILED, e.getMessage());
        }
    }

    /**
     * 회사별 본문 요약 전략 선택
     */
    private String makeShort(Company company, String markdown) {
        if (company == Company.GOOGLE)
            return googleShortener.run(GptParams.ofMini(markdown));

        if (company == Company.META)
            return metaShortener.run(GptParams.ofMini(markdown));

        if (company == Company.NETFLIX)
            return netflixShortener.run(GptParams.ofMini(markdown));

        if (company == Company.TIK_TOK)
            return tikTokShortener.run(GptParams.ofMini(markdown));

        if (company == Company.SPOTIFY)
            return spotifyShortener.run(GptParams.ofMini(markdown));

        // 한국 회사 및 기타 모든 회사는 General Shortener 사용
        return generalShortener.run(GptParams.ofMini(markdown));
    }

    /**
     * 콘텐츠 유효성 검증
     * 1. 길이 체크 (100 ~ 100000)
     * 2. GPT 검증
     */
    private void validateContent(String shortened) {
        // 길이 체크
        if (shortened == null) {
            throw new IllegalStateException("Shortened content is null");
        }
        if (shortened.length() < 100) {
            throw new IllegalStateException("Shortened content too short: " + shortened.length() + " characters");
        }
        if (shortened.length() > 100000) {
            throw new IllegalStateException("Shortened content too long: " + shortened.length() + " characters (possible GPT error)");
        }

        log.info("Shortened content length validated: {} characters", shortened.length());

        // GPT 유효성 검증
        HiringMarkDownValidator.IsValidHiringMd validation = hiringMarkDownValidator.run(GptParams.ofMini(shortened));
        if (!validation.valid()) {
            throw new IllegalStateException("Invalid hiring content: " + validation.reason());
        }

        log.info("Shortened content GPT validated: valid=true, reason={}", validation.reason());
    }

    /**
     * crawl_job_contents 저장 (항상 새로 생성)
     *
     * @param urlEntity URL Entity
     * @param markdown  전체 Markdown
     * @param shortened 요약된 Markdown
     */
    private void saveContent(CrawlJobUrlEntity urlEntity, String markdown, String shortened) {
        // 항상 새로 생성 (WAIT/FAILED 모두 이전에 content가 없음)
        CrawlJobContentEntity contentEntity = new CrawlJobContentEntity(
                null,
                urlEntity.getId(),
                markdown,
                shortened,
                CrawlStatus.WAIT,  // Job 생성 대기 상태
                null,
                null,
                0,  // 첫 생성이므로 0
                Instant.now(),
                null
        );
        contentRepository.save(contentEntity);
        log.info("Saved content: urlId={}", urlEntity.getId());
    }

    /**
     * URL 상태 업데이트
     *
     * @param urlEntity    URL Entity
     * @param status       새로운 상태 (SUCCESS or FAILED)
     * @param errorMessage 에러 메시지 (FAILED인 경우)
     */
    private void updateUrlStatus(CrawlJobUrlEntity urlEntity, CrawlStatus status, String errorMessage) {
        int newRetryCount = urlEntity.getRetryCount();

        // FAILED인 경우 retry_count 증가
        if (status == CrawlStatus.FAILED) {
            newRetryCount++;
        }

        CrawlJobUrlEntity updated = new CrawlJobUrlEntity(
                urlEntity.getId(),
                urlEntity.getCompany(),
                urlEntity.getUrl(),
                urlEntity.getTitle(),
                status,
                newRetryCount,
                urlEntity.getCreatedAt(),
                Instant.now()
        );
        urlRepository.save(updated);
        log.info("Updated URL status to {}: id={}, retryCount={}", status, urlEntity.getId(), newRetryCount);
    }
}
