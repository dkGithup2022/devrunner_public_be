package dev.devrunner.crawler.task.job.closedCheck;

public enum ClosedReason {
    NONE,               // 마감되지 않음
    EXPIRED,            // 기간 만료
    CLOSED,             // 채용 마감
    NOT_HIRING,         // 채용 중단
    CANNOT_READ_PAGE    // 페이지 읽기 실패
}