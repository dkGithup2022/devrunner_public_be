package dev.devrunner.model.notification;

/**
 * 알림 타입 Enum
 */
public enum NotificationType {
    COMMENT_REPLY,  // 댓글 답글
    POST_LIKE,      // 게시글 좋아요
    COMMENT_LIKE,   // 댓글 좋아요
    NEW_JOB,        // 새 채용공고
    MENTIONED,      // 멘션
    JOB_DEADLINE    // 채용공고 마감 임박
}
