package dev.devrunner.model.activityLog;

import dev.devrunner.model.common.TargetType;
import lombok.Value;

import java.time.Instant;

/**
 * 댓글 활동 로그
 *
 * 사용자가 댓글을 단 활동 기록 및 댓글 단 대상 아티클 정보
 */
@Value
public class CommentActivityLog {
    // 댓글 정보
    Long commentId;
    String content;
    Boolean isHidden;
    Instant commentedAt;
    Long parentCommentId;

    // 댓글 단 대상 아티클 정보
    TargetType targetType;        // JOB, TECH_BLOG, COMMUNITY_POST
    Long targetId;
    String targetTitle;
    String targetAuthorNickname;

    // 아티클 인기지표
    Long viewCount;
    Long likeCount;
    Long commentCount;
}
