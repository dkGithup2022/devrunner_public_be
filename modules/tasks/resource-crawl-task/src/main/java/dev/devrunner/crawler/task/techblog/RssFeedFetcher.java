package dev.devrunner.crawler.task.techblog;


import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.util.List;

/**
 * RSS 피드 가져오기
 *
 * RestTemplate + ROME 라이브러리를 사용하여 RSS 피드를 파싱합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RssFeedFetcher {

    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * RSS URL에서 피드를 가져와 Entry 리스트 반환
     *
     * @param rssUrl RSS 피드 URL
     * @return RSS Entry 리스트
     */
    public List<SyndEntry> fetch(String rssUrl) {
        try {
            log.debug("Fetching RSS feed from: {}", rssUrl);

            // TODO: 최적화 필요 - 매번 RestTemplate을 새로 생성하는 것은 비효율적. 인스턴스 재사용 고려
            RestTemplate restTemplate = restTemplateBuilder.build();

            // RestTemplate로 XML 가져오기
            String xml = restTemplate.getForObject(rssUrl, String.class);

            if (xml == null || xml.isEmpty()) {
                log.warn("Empty RSS feed from: {}", rssUrl);
                return List.of();
            }

            // ROME으로 파싱
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new StringReader(xml));

            log.info("Successfully fetched {} entries from: {}", feed.getEntries().size(), rssUrl);
            return feed.getEntries();

        } catch (Exception e) {
            log.error("Failed to fetch RSS feed from: {}", rssUrl, e);
            throw new RuntimeException("RSS feed fetch failed: " + rssUrl, e);
        }
    }
}
