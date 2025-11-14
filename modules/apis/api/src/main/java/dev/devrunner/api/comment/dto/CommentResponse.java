package dev.devrunner.api.comment.dto;

import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.CommentOrder;
import dev.devrunner.model.common.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Schema(description = "Comment response")
public class CommentResponse {

    @Schema(description = "Comment ID", example = "1")
    private final Long commentId;

    @Schema(description = "User ID", example = "1")
    private final Long userId;

    @Schema(description = "User nickname", example = "testuser")
    private final String nickname;

    @Schema(description = "Comment content", example = "Great information, thanks!")
    private final String content;

    @Schema(description = "Target type", example = "JOB")
    private final TargetType targetType;

    @Schema(description = "Target ID", example = "100")
    private final Long targetId;

    @Schema(description = "Parent comment ID", example = "1")
    private final Long parentId;

    @Schema(description = "Comment order information")
    private final CommentOrder commentOrder;

    @Schema(description = "Is hidden", example = "false")
    private final Boolean isHidden;

    @Schema(description = "Created at", example = "2025-10-09T14:30:00Z")
    private final Instant createdAt;

    @Schema(description = "Updated at", example = "2025-10-09T14:30:00Z")
    private final Instant updatedAt;

    public static CommentResponse from(CommentRead comment) {
        return new CommentResponse(
                comment.getCommentId(),
                comment.getUserId(),
                comment.getNickname(),
                comment.getContent(),
                comment.getTargetType(),
                comment.getTargetId(),
                comment.getParentId(),
                comment.getCommentOrder(),
                comment.getIsHidden(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
