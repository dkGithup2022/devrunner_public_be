package dev.devrunner.api.user.dto;

import dev.devrunner.model.activityLog.LikeActivityLog;
import dev.devrunner.model.common.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.Instant;

/**
 * 좋아요 활동 로그 응답 DTO
 */
@Value
@Schema(description = "Like activity log response")
public class LikeActivityLogResponse {

    @Schema(description = "Reaction ID", example = "1")
    Long reactionId;

    @Schema(description = "Liked at")
    Instant likedAt;

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

    public static LikeActivityLogResponse from(LikeActivityLog log) {
        return new LikeActivityLogResponse(
                log.getReactionId(),
                log.getLikedAt(),
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
