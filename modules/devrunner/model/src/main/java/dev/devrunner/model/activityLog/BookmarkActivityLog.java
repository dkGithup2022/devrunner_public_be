package dev.devrunner.model.activityLog;

import dev.devrunner.model.common.TargetType;
import lombok.Value;

import java.time.Instant;

/**
 * 북마크 활동 로그
 *
 * 사용자가 북마크한 활동 기록 및 대상 아티클 정보
 */
@Value
public class BookmarkActivityLog {
    // 북마크 정보
    Long bookmarkId;
    Instant bookmarkedAt;

    // 북마크한 아티클 정보
    TargetType targetType;
    Long targetId;
    String targetTitle;
    String targetAuthorNickname;

    // 아티클 인기지표
    Long viewCount;
    Long likeCount;
    Long commentCount;
}
