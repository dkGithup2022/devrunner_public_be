package dev.devrunner.api.user.dto;

import dev.devrunner.model.activityLog.PostActivityLog;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.Instant;

/**
 * 글 작성 활동 로그 응답 DTO
 */
@Value
@Schema(description = "Post activity log response")
public class PostActivityLogResponse {

    @Schema(description = "Community post ID", example = "1")
    Long communityPostId;

    @Schema(description = "Category", example = "INTERVIEW_SHARE")
    CommunityPostCategory category;

    @Schema(description = "Title")
    String title;

    @Schema(description = "Markdown body")
    String markdownBody;

    @Schema(description = "Posted at")
    Instant postedAt;

    @Schema(description = "View count", example = "120")
    Long viewCount;

    @Schema(description = "Like count", example = "15")
    Long likeCount;

    @Schema(description = "Comment count", example = "8")
    Long commentCount;

    public static PostActivityLogResponse from(PostActivityLog log) {
        return new PostActivityLogResponse(
                log.getCommunityPostId(),
                log.getCategory(),
                log.getTitle(),
                log.getMarkdownBody(),
                log.getPostedAt(),
                log.getViewCount(),
                log.getLikeCount(),
                log.getCommentCount()
        );
    }
}
