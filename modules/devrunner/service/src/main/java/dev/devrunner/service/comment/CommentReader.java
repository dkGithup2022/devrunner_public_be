package dev.devrunner.service.comment;

import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;

import java.util.List;

/**
 * Comment 도메인 조회 서비스 인터페이스
 * <p>
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface CommentReader {

    /**
     * ID로 Comment 조회 (with user information)
     *
     * @param identity Comment 식별자
     * @return CommentRead 엔티티 (nickname 포함)
     */
    CommentRead getById(CommentIdentity identity);


    List<CommentRead> getByUserId(UserIdentity userIdentity, int page, int size);


    /**
     * 모든 Comment 조회 (with user information)
     *
     * @return CommentRead 목록 (nickname 포함)
     */
    List<CommentRead> getAll();

    /**
     * 대상 타입과 대상 ID로 Comment 목록 조회 (with user information)
     *
     * @param targetType 대상 타입
     * @param targetId   대상 ID
     * @return CommentRead 목록 (nickname 포함)
     */
    List<CommentRead> getByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /**
     * 부모 댓글 ID로 대댓글 목록 조회 (with user information)
     *
     * @param parentId 부모 댓글 ID
     * @return CommentRead 목록 (nickname 포함)
     */
    List<CommentRead> getByParentId(Long parentId);
}
