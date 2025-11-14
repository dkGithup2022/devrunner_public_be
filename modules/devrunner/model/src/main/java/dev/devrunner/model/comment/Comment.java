package dev.devrunner.model.comment;

import dev.devrunner.exception.comment.InvalidCommentException;
import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.CommentOrder;
import dev.devrunner.model.common.TargetType;
import lombok.Value;
import java.time.Instant;

@Value
public class Comment implements AuditProps {
    Long commentId;
    Long userId;
    String content;
    TargetType targetType;
    Long targetId;
    Long parentId;
    CommentOrder commentOrder;
    Boolean isHidden;
    Instant createdAt;
    Instant updatedAt;

    private static final int MAX_CONTENT_LENGTH = 2000;

    public static Comment newComment(
        Long userId,
        String content,
        TargetType targetType,
        Long targetId
    ) {
        validateUserId(userId);
        validateContent(content);
        validateTargetType(targetType);
        validateTargetId(targetId);

        Instant now = Instant.now();
        return new Comment(
            null,
            userId,
            content,
            targetType,
            targetId,
            null,
            CommentOrder.empty(),
            false,
            now,
            now
        );
    }

    public static Comment newReply(
        Long userId,
        String content,
        TargetType targetType,
        Long targetId,
        Long parentId
    ) {
        validateUserId(userId);
        validateContent(content);
        validateTargetType(targetType);
        validateTargetId(targetId);
        validateParentId(parentId);

        Instant now = Instant.now();
        return new Comment(
            null,
            userId,
            content,
            targetType,
            targetId,
            parentId,
            CommentOrder.empty(),
            false,
            now,
            now
        );
    }

    public Comment hide() {
        return new Comment(
            commentId, userId, content, targetType, targetId,
            parentId, commentOrder, true, createdAt, Instant.now()
        );
    }

    public Comment show() {
        return new Comment(
            commentId, userId, content, targetType, targetId,
            parentId, commentOrder, false, createdAt, Instant.now()
        );
    }

    public Comment updateCommentOrder(CommentOrder newCommentOrder) {
        return new Comment(
            commentId, userId, content, targetType, targetId,
            parentId, newCommentOrder, isHidden, createdAt, Instant.now()
        );
    }

    public Comment updateContent(String newContent) {
        validateContent(newContent);
        return new Comment(
            commentId, userId, newContent, targetType, targetId,
            parentId, commentOrder, isHidden, createdAt, Instant.now()
        );
    }

    /**
     * Validate userId
     */
    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new InvalidCommentException("User ID cannot be null");
        }
        if (userId <= 0) {
            throw new InvalidCommentException("User ID must be positive");
        }
    }

    /**
     * Validate content
     */
    private static void validateContent(String content) {
        if (content == null) {
            throw new InvalidCommentException("Content cannot be null");
        }
        if (content.isBlank()) {
            throw new InvalidCommentException("Content cannot be empty");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidCommentException("Content too long (max " + MAX_CONTENT_LENGTH + " characters)");
        }
    }

    /**
     * Validate targetType
     */
    private static void validateTargetType(TargetType targetType) {
        if (targetType == null) {
            throw new InvalidCommentException("Target type cannot be null");
        }
    }

    /**
     * Validate targetId
     */
    private static void validateTargetId(Long targetId) {
        if (targetId == null) {
            throw new InvalidCommentException("Target ID cannot be null");
        }
        if (targetId <= 0) {
            throw new InvalidCommentException("Target ID must be positive");
        }
    }

    /**
     * Validate parentId (for replies)
     */
    private static void validateParentId(Long parentId) {
        if (parentId == null) {
            throw new InvalidCommentException("Parent ID cannot be null for replies");
        }
        if (parentId <= 0) {
            throw new InvalidCommentException("Parent ID must be positive");
        }
    }
}
