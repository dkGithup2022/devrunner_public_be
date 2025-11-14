package dev.devrunner.jdbc.comment.repository;

import dev.devrunner.model.common.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * CommentWithUserDto - JOIN query result mapping
 *
 * Used for fetching comments with user information (nickname) in a single query.
 * Maps the result of: SELECT c.*, u.nickname FROM comments c JOIN users u ON c.user_id = u.id
 */
@Getter
@AllArgsConstructor
public class CommentWithUserDto {
    // Comment fields
    private Long id;
    private Long userId;
    private String content;
    private TargetType targetType;
    private Long targetId;
    private Long parentId;

    // CommentOrder embedded fields
    private Integer commentOrder;
    private Integer level;
    private Integer sortNumber;
    private Integer childCount;

    private Boolean isHidden;
    private Instant createdAt;
    private Instant updatedAt;

    // User fields (from JOIN)
    private String nickname;
}
