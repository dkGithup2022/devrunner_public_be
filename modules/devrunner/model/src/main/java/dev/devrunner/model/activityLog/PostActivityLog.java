package dev.devrunner.model.activityLog;

import dev.devrunner.model.communitypost.CommunityPostCategory;
import lombok.Value;

import java.time.Instant;

/**
 * 글 작성 활동 로그
 *
 * 사용자가 작성한 커뮤니티 글 및 인기지표
 */
@Value
public class PostActivityLog {
    // 글 정보
    Long communityPostId;
    CommunityPostCategory category;
    String title;
    String markdownBody;
    Instant postedAt;

    // 글 인기지표
    Long viewCount;
    Long likeCount;
    Long commentCount;
}
