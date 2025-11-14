package dev.devrunner.crawler.task.job.urlCrawler;

import java.util.List;

/**
 * HTML에서 채용 공고 URL 목록을 추출하는 파서 인터페이스
 */
public interface UrlListParser {
    /**
     * HTML에서 채용 공고 detail URL 및 제목 목록 추출
     * @param html 채용 목록 페이지의 HTML
     * @return 추출된 URL 및 제목 정보 목록
     */
    List<JobUrlInfo> extractUrls(String html);
}
