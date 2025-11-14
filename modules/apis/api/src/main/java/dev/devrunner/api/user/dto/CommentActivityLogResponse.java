package dev.devrunner.api.user.dto;

import dev.devrunner.model.activityLog.CommentActivityLog;
import dev.devrunner.model.common.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.Instant;

/**
 * 댓글 활동 로그 응답 DTO
 */
@Value
@Schema(description = "Comment activity log response")
public class CommentActivityLogResponse {

    @Schema(description = "Comment ID", example = "1")
    Long commentId;

    @Schema(description = "Comment content")
    String content;

    @Schema(description = "Is hidden", example = "false")
    Boolean isHidden;

    @Schema(description = "Commented at")
    Instant commentedAt;

    @Schema(description = "Parent comment ID (null for root comments)")
    Long parentCommentId;

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

    public static CommentActivityLogResponse from(CommentActivityLog log) {
        return new CommentActivityLogResponse(
                log.getCommentId(),
                log.getContent(),
                log.getIsHidden(),
                log.getCommentedAt(),
                log.getParentCommentId(),
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
