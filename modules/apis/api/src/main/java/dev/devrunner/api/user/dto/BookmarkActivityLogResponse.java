package dev.devrunner.api.user.dto;

import dev.devrunner.model.activityLog.BookmarkActivityLog;
import dev.devrunner.model.common.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.Instant;

/**
 * 북마크 활동 로그 응답 DTO
 */
@Value
@Schema(description = "Bookmark activity log response")
public class BookmarkActivityLogResponse {

    @Schema(description = "Bookmark ID", example = "1")
    Long bookmarkId;

    @Schema(description = "Bookmarked at")
    Instant bookmarkedAt;

    @Schema(description = "Target type", example = "COMMUNITY_POST")
    TargetType targetType;

    @Schema(description = "Target ID", example = "10")
    Long targetId;

    @Schema(description = "Target title")
    String targetTitle;

    @Schema(description = "Target author nickname")
    String targetAuthorNickname;

    @Schema(description = "View count", example = "120")
    Long viewCount;

    @Schema(description = "Like count", example = "15")
    Long likeCount;

    @Schema(description = "Comment count", example = "8")
    Long commentCount;

    public static BookmarkActivityLogResponse from(BookmarkActivityLog log) {
        return new BookmarkActivityLogResponse(
                log.getBookmarkId(),
                log.getBookmarkedAt(),
                log.getTargetType(),
                log.getTargetId(),
                log.getTargetTitle(),
                log.getTargetAuthorNickname(),
                log.getViewCount(),
                log.getLikeCount(),
                log.getCommentCount()
        );
    }
}
