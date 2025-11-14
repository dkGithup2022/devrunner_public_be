package dev.devrunner.crawler.task.job.contentCrawler;

import dev.devrunner.crawler.step.CrawlJobUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Job Content Crawler
 * <p>
 * Step 1(crawl_job_urls)에서 WAIT 상태인 URL을 하나씩 가져와서 JobContentProcessor로 처리
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobContentCrawler {

    private final CrawlJobUrlRepository urlRepository;
    private final JobContentProcessor processor;

    /**
     * WAIT 상태의 URL을 하나씩 처리
     */
    public void run() {
        var urlEntityOpt = urlRepository.findFirstWaitingUrl();
        if (urlEntityOpt.isEmpty()) {
            log.debug("No waiting URLs found");
            return;
        }

        processor.process(urlEntityOpt.get());
    }
}
