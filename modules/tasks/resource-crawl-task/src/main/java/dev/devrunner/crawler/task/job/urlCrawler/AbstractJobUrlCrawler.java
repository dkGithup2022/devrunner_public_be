package dev.devrunner.crawler.task.job.urlCrawler;

import dev.devrunner.crawler.playwright.PlaywrightApi;
import dev.devrunner.crawler.step.CrawlJobUrlEntity;
import dev.devrunner.crawler.step.CrawlJobUrlRepository;
import dev.devrunner.model.common.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 채용 공고 URL 수집 크롤러 추상 클래스
 *
 * 서브클래스는 다음을 구현해야 함:
 * - getBaseUrl(): 채용 목록 페이지 URL
 * - getCompany(): 회사 enum
 * - getParser(): HTML 파서
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractJobUrlCrawler {

    protected final PlaywrightApi playwrightApi;
    protected final CrawlJobUrlRepository urlRepository;

    /**
     * 채용 목록 페이지 URL 반환
     */
    protected abstract String getBaseUrl();

    /**
     * 회사 enum 반환
     */
    protected abstract Company getCompany();

    /**
     * HTML 파서 반환
     */
    protected abstract UrlListParser getParser();

    /**
     * 크롤링 실행
     */
    public void run() {
        log.info("JobUrlCrawler started. company={}, baseUrl={}", getCompany(), getBaseUrl());

        List<JobUrlInfo> allUrlInfos = extractAllUrls(getBaseUrl());
        log.info("Extracted {} URLs from {} jobs page", allUrlInfos.size(), getCompany());

        var validated = validateUrls(allUrlInfos);
        log.info("Validated URLs: {} (duplicates filtered: {})", validated.size(), allUrlInfos.size() - validated.size());

        saveUrlRecords(validated);
        log.info("JobUrlCrawler completed. company={}, saved {} new URLs", getCompany(), validated.size());
    }

    /**
     * Playwright로 HTML 렌더링 후 URL 추출
     */
    protected List<JobUrlInfo> extractAllUrls(String baseUrl) {
        String html;
        try {
            html = playwrightApi.waitAndGetHtml(baseUrl, 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return getParser().extractUrls(html);
    }

    /**
     * 중복 URL 필터링
     */
    private List<JobUrlInfo> validateUrls(List<JobUrlInfo> allUrlInfos) {
        List<JobUrlInfo> validated = new ArrayList<>();
        for (JobUrlInfo urlInfo : allUrlInfos) {
            if (urlRepository.existsByUrl(urlInfo.url())) {
                log.info("URL already exists, skipping: {}", urlInfo.url());
                continue;
            }
            validated.add(urlInfo);
        }
        return validated;
    }

    /**
     * URL 레코드 저장
     */
    private void saveUrlRecords(List<JobUrlInfo> validatedUrlInfos) {
        for (JobUrlInfo urlInfo : validatedUrlInfos) {
            CrawlJobUrlEntity entity = CrawlJobUrlEntity.createWaiting(getCompany(), urlInfo.url(), urlInfo.title());
            urlRepository.save(entity);
        }
        log.debug("Saved {} URL records to crawl_job_urls table", validatedUrlInfos.size());
    }
}
