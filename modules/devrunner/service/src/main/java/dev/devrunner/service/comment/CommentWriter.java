package dev.devrunner.service.comment;

import dev.devrunner.model.comment.Comment;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.comment.dto.CommentWriteCommand;

/**
 * Comment 비즈니스 로직 서비스
 * <p>
 * 계층형 댓글 구조를 관리하며, CommentOrder 기반의 정렬을 지원합니다.
 */
public interface CommentWriter {

    /**
     * 댓글 작성 (최상위 댓글 또는 대댓글)
     *
     * @param command 댓글 작성 커맨드 (parentId가 null이면 최상위 댓글)
     * @return 생성된 Comment
     */
    Comment write(CommentWriteCommand command);

    /**
     * 댓글 수정
     *
     * @param requestUser 요청 사용자 Identity
     * @param commentId  댓글 ID
     * @param newContent 새로운 내용
     * @return 수정된 Comment
     */
    Comment updateComment(UserIdentity requestUser, Long commentId, String newContent);

    /**
     * 댓글 숨김 처리
     *
     * @param requestUser 요청 사용자 Identity
     * @param commentId 댓글 ID
     * @return 숨김 처리된 Comment
     */
    Comment hideComment(UserIdentity requestUser, Long commentId);

    /**
     * 댓글 숨김 해제
     *
     * @param requestUser 요청 사용자 Identity
     * @param commentId 댓글 ID
     * @return 숨김 해제된 Comment
     */
    Comment showComment(UserIdentity requestUser, Long commentId);
}
