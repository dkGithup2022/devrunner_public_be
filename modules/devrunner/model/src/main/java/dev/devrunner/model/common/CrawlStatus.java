package dev.devrunner.model.common;

/**
 * 크롤링 작업의 상태
 */
public enum CrawlStatus {
    WAIT,       // 처리 대기 중
    SUCCESS,    // 처리 완료
    FAILED      // 처리 실패
}
