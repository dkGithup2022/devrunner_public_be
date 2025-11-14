package dev.devrunner.crawler.task.job.contentCrawler;

import dev.devrunner.model.common.Company;

/**
 * Job Page Reader Interface
 * <p>
 * 채용 공고 페이지를 읽어서 마크다운으로 변환하는 인터페이스입니다.
 * <p>
 * 회사별로 다른 파싱 전략을 사용할 수 있습니다:
 * - Firecrawl API 사용 (대부분)
 * - Playwright + Custom Parser (동적 페이지)
 * <p>
 * 사용처:
 * - JobContentProcessor: 신규 채용 공고 콘텐츠 수집
 * - ClosedJobProcessor: 채용 공고 마감 여부 확인
 */
public interface JobPageReader {

    /**
     * URL에서 마크다운 콘텐츠를 읽어옵니다.
     *
     * @param url     읽을 URL
     * @param company 회사 (파싱 전략 선택에 사용)
     * @return 마크다운 형식의 페이지 콘텐츠
     */
    String read(String url, Company company);
}
