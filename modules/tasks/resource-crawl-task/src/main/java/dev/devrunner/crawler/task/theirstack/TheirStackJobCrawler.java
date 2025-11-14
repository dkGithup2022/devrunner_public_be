package dev.devrunner.crawler.task.theirstack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.crawler.step.CrawlTheirStackJobEntity;
import dev.devrunner.crawler.theirstack.TheirStackApiClient;
import dev.devrunner.crawler.theirstack.dto.JobData;
import dev.devrunner.crawler.theirstack.dto.TheirStackJobSearchRequest;
import dev.devrunner.crawler.theirstack.dto.TheirStackJobSearchResponse;
import dev.devrunner.crawler.step.CrawlTheirStackJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TheirStack Job 크롤러
 * <p>
 * 한국 회사의 엔지니어/개발자 채용 공고를 TheirStack API로 수집
 * 회사별로 별도 메서드를 제공하여 개별 실행 가능
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TheirStackJobCrawler {

    private final TheirStackApiClient apiClient;
    private final CrawlTheirStackJobRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Google 채용 공고 크롤링
     */
    public void runGoogle() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Google"));
    }

    /**
     * Meta 채용 공고 크롤링
     */
    public void runMeta() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Meta"));
    }

    /**
     * Netflix 채용 공고 크롤링
     */
    public void runNetflix() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Netflix"));
    }

    /**
     * Spotify 채용 공고 크롤링
     */
    public void runSpotify() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Spotify"));
    }

    /**
     * TikTok 채용 공고 크롤링
     */
    public void runTikTok() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("TikTok"));
    }

    /**
     * Apple 채용 공고 크롤링
     */
    public void runApple() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Apple"));
    }

    /**
     * Amazon 채용 공고 크롤링
     */
    public void runAmazon() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Amazon"));
    }

    /**
     * Microsoft 채용 공고 크롤링
     */
    public void runMicrosoft() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Microsoft"));
    }

    /**
     * Airbnb 채용 공고 크롤링
     */
    public void runAirbnb() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Airbnb"));
    }

    /**
     * Uber 채용 공고 크롤링
     */
    public void runUber() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Uber"));
    }

    /**
     * Stripe 채용 공고 크롤링
     */
    public void runStripe() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Stripe"));
    }

    /**
     * Shopify 채용 공고 크롤링
     */
    public void runShopify() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Shopify"));
    }

    /**
     * Gitlab 채용 공고 크롤링
     */
    public void runGitlab() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Gitlab"));
    }

    /**
     * Toss 채용 공고 크롤링
     */
    public void runToss() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Toss"));
    }

    /**
     * Coupang 채용 공고 크롤링
     */
    public void runCoupang() {
        crawlCompany(TheirStackCrawlConfig.defaultConfig("Coupang"));
    }

    /**
     * 특정 회사의 채용 공고를 크롤링 (공통 로직)
     *
     * @param config 크롤링 설정
     */
    private void crawlCompany(TheirStackCrawlConfig config) {
        log.info("Crawling jobs for company: {}", config.getCompany());

        try {
            TheirStackJobSearchRequest request = TheirStackJobSearchRequest.builder()
                    .jobCountryCodeOr(config.getCountryCodes())
                    .companyNameCaseInsensitiveOr(List.of(config.getCompany()))
                    .jobTitlePatternOr(config.getJobTitlePatterns())
                    .postedAtMaxAgeDays(config.getPostedAtMaxAgeDays())
                    .page(0)
                    .limit(config.getLimit())
                    .build();

            TheirStackJobSearchResponse response = apiClient.searchJobs(request);

            if (response == null || response.getData() == null) {
                log.warn("No response data for company: {}", config.getCompany());
                return;
            }

            log.info("Found {} jobs for company: {}", response.getData().size(), config.getCompany());

            int collected = 0;
            int skipped = 0;

            for (JobData jobData : response.getData()) {
                if (processJobData(jobData)) {
                    collected++;
                } else {
                    skipped++;
                }
            }

            log.info("Company {} - Collected: {}, Skipped (duplicates): {}",
                    config.getCompany(), collected, skipped);

        } catch (Exception e) {
            log.error("Failed to crawl jobs for company: {}", config.getCompany(), e);
        }
    }

    /**
     * JobData 처리 및 저장
     *
     * @return true if saved, false if skipped (duplicate)
     */
    private boolean processJobData(JobData jobData) {
        try {
            // 중복 체크
            if (repository.existsByTheirStackJobId(jobData.getId())) {
                log.debug("Job already exists: theirStackJobId={}", jobData.getId());
                return false;
            }

            // rawData로 저장 (전체 JSON)
            String rawData = objectMapper.writeValueAsString(jobData);

            // datePosted 파싱
            Instant datePosted = parseDateTime(jobData.getDatePosted());

            // Entity 생성 및 저장
            CrawlTheirStackJobEntity entity = CrawlTheirStackJobEntity.createWaiting(
                    jobData.getId(),
                    rawData,
                    jobData.getCompany(),
                    jobData.getFinalUrl() != null ? jobData.getFinalUrl() : jobData.getUrl(),
                    jobData.getJobTitle(),
                    jobData.getLocation(),
                    jobData.getSeniority(),
                    datePosted,
                    jobData.getDescription()
            );

            repository.save(entity);
            log.debug("Saved TheirStack job: id={}, company={}, title={}",
                    jobData.getId(), jobData.getCompany(), jobData.getJobTitle());

            return true;

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JobData to JSON: theirStackJobId={}", jobData.getId(), e);
            return false;
        } catch (Exception e) {
            log.error("Failed to process JobData: theirStackJobId={}", jobData.getId(), e);
            return false;
        }
    }

    /**
     * ISO 8601 날짜 문자열을 Instant로 파싱
     * - ISO_DATE_TIME 형식 (2025-10-27T12:34:56Z)
     * - ISO_DATE 형식 (2025-10-27) → 00:00:00 UTC로 변환
     */
    private Instant parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }

        try {
            // ISO_DATE_TIME 시도 (2025-10-27T12:34:56Z)
            return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateTimeString));
        } catch (Exception e1) {
            try {
                // ISO_DATE 시도 (2025-10-27) → LocalDate로 파싱 후 00:00:00 UTC로 변환
                return java.time.LocalDate.parse(dateTimeString, DateTimeFormatter.ISO_DATE)
                        .atStartOfDay(java.time.ZoneOffset.UTC)
                        .toInstant();
            } catch (Exception e2) {
                log.warn("Failed to parse datetime: {}", dateTimeString);
                return null;
            }
        }
    }
}
