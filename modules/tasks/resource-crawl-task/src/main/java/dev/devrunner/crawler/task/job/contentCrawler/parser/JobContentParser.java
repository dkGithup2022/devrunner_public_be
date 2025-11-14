package dev.devrunner.crawler.task.job.contentCrawler.parser;

/**
 * Job Content Parser Interface
 *
 * URL에서 채용 공고 본문을 추출하는 파서 인터페이스
 * - Firecrawl 기반 파서 (대부분의 회사)
 * - Playwright + Jsoup 기반 파서 (한국 회사 등 동적 로딩이 필요한 경우)
 */
public interface JobContentParser {
    /**
     * URL에서 채용 공고 Markdown 추출
     *
     * @param url 채용 공고 URL
     * @return Markdown 형식의 채용 공고 본문
     */
    String parse(String url);
}
