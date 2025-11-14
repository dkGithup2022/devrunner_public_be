package dev.devrunner.service.comment.dto;

import dev.devrunner.model.common.TargetType;

/**
 * 댓글 작성 커맨드
 *
 * parentId가 null이면 최상위 댓글, 있으면 대댓글
 */
public record CommentWriteCommand(
        Long userId,
        String content,
        TargetType targetType,
        Long targetId,
        Long parentId  // null이면 최상위 댓글
) {
    /**
     * 최상위 댓글 작성용 팩토리 메서드
     */
    public static CommentWriteCommand root(Long userId, String content, TargetType targetType, Long targetId) {
        return new CommentWriteCommand(userId, content, targetType, targetId, null);
    }

    /**
     * 대댓글 작성용 팩토리 메서드
     */
    public static CommentWriteCommand reply(Long userId, String content, TargetType targetType, Long targetId, Long parentId) {
        return new CommentWriteCommand(userId, content, targetType, targetId, parentId);
    }

    /**
     * 최상위 댓글인지 확인
     */
    public boolean isRootComment() {
        return parentId == null;
    }
}
