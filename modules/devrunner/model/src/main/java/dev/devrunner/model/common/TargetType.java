package dev.devrunner.model.common;

/**
 * 대상 타입 Enum
 *
 * Comment, Like, Notification, UserActivity에서 공통으로 사용
 * 어떤 종류의 엔티티를 참조하는지 나타냄
 */
public enum TargetType {
    JOB,            // 채용공고
    COMMUNITY_POST, // 커뮤니티 글
    TECH_BLOG,      // 테크 블로그
    COMMENT         // 댓글 (Notification에서만 사용)
}
