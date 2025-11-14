package dev.devrunner.crawler.task.techblog;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import dev.devrunner.model.techblog.TechBlog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RSS Entry → TechBlog 도메인 객체 변환
 *
 * RSS 피드의 Entry를 TechBlog 도메인 모델로 변환합니다.
 * Meta, Airbnb, Spotify의 RSS 구조 차이를 처리합니다.
 */
@Component
@Slf4j
public class RssToTechBlogConverter {

    /**
     * RSS Entry를 TechBlog로 변환
     *
     * @param company 회사명
     * @param entry   RSS Entry
     * @return TechBlog 도메인 객체
     */
    public TechBlog convert(String company, SyndEntry entry) {
        String url = cleanUrl(entry.getLink());
        String title = entry.getTitle();
        String markdownBody = extractContent(entry);

        // 카테고리 추출
        List<String> categories = extractCategories(entry);

        log.debug("Converting RSS entry: company={}, title={}, url={}", company, title, url);

        // TechBlog.newExternalBlog 팩토리 메서드 사용
        // (url, company, title, markdownBody, originalUrl)
        TechBlog techBlog = TechBlog.newExternalBlog(url, company, title, markdownBody, url);

        // TODO: 카테고리를 techCategories에 설정하려면 TechBlog에 withCategories 같은 메서드가 필요
        // 현재는 빈 리스트로 생성됨

        return techBlog;
    }

    /**
     * RSS Entry에서 본문 추출
     *
     * 1. content:encoded 시도 (Meta, Airbnb)
     * 2. description 폴백 (Spotify 또는 요약만 있는 경우)
     *
     * @param entry RSS Entry
     * @return 본문 내용 (HTML)
     */
    private String extractContent(SyndEntry entry) {
        // 1. content:encoded 시도 (Meta, Airbnb는 전체 HTML 제공)
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            SyndContent content = entry.getContents().get(0);
            String value = content.getValue();
            if (value != null && !value.isBlank()) {
                log.debug("Extracted content from content:encoded, length={}", value.length());
                return value;
            }
        }

        // 2. description 폴백 (Spotify는 요약만 제공)
        if (entry.getDescription() != null) {
            String value = entry.getDescription().getValue();
            if (value != null && !value.isBlank()) {
                log.debug("Extracted content from description, length={}", value.length());
                return value;
            }
        }

        log.warn("No content found for entry: {}", entry.getLink());
        return "";
    }

    /**
     * RSS Entry에서 카테고리 추출
     *
     * @param entry RSS Entry
     * @return 카테고리 리스트
     */
    private List<String> extractCategories(SyndEntry entry) {
        if (entry.getCategories() == null || entry.getCategories().isEmpty()) {
            return List.of();
        }

        return entry.getCategories().stream()
                .map(SyndCategory::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * URL 정리 (쿼리 파라미터 제거 등)
     *
     * @param url 원본 URL
     * @return 정리된 URL
     */
    private String cleanUrl(String url) {
        if (url == null) {
            return "";
        }

        // Medium RSS는 source= 파라미터가 붙는데, 제거
        // 예: ?source=rss----53c7c27702d5---4
        if (url.contains("?source=rss")) {
            int idx = url.indexOf("?source=rss");
            return url.substring(0, idx);
        }

        return url;
    }
}
