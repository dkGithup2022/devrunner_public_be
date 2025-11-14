package dev.devrunner.model.activityLog;

import dev.devrunner.model.common.TargetType;
import lombok.Value;

import java.time.Instant;

/**
 * 좋아요 활동 로그
 *
 * 사용자가 좋아요를 누른 활동 기록 및 대상 아티클 정보
 */
@Value
public class LikeActivityLog {
    // 좋아요 정보
    Long reactionId;
    Instant likedAt;

    // 좋아요 누른 아티클 정보
    TargetType targetType;
    Long targetId;
    String targetTitle;
    String targetAuthorNickname;

    // 아티클 인기지표
    Long viewCount;
    Long likeCount;
    Long commentCount;
}
