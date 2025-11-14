package dev.devrunner.crawler.task.job.contentCrawler;

import dev.devrunner.crawler.step.CrawlJobUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Failed Job Content Crawler
 * <p>
 * Step 1(crawl_job_urls)에서 FAILED 상태이고 retry_count < 3인 URL을 재처리
 * JobContentProcessor로 실제 처리 로직 위임
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FailedJobContentCrawler {
    private final CrawlJobUrlRepository urlRepository;
    private final JobContentProcessor processor;

    public void run() {
        var failedUrls = urlRepository.findFailedUrlsForRetry(3, 100);
        log.info("Found {} failed URLs for retry", failedUrls.size());
        if (failedUrls.isEmpty()) {
            return;
        }

        for (var failedUrl : failedUrls) {
            processor.process(failedUrl);

            // Rate limiting: 3분 대기
            try {
                Thread.sleep(1000 * 60 * 3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
