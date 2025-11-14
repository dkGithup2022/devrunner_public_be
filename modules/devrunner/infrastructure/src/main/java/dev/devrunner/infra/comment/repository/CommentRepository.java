package dev.devrunner.infra.comment.repository;

import dev.devrunner.model.comment.Comment;
import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;

import java.util.List;
import java.util.Optional;

/**
 * Comment Repository 인터페이스
 *
 * 헥사고날 아키텍처에서 Port 역할을 수행하며,
 * 댓글 도메인의 데이터 접근을 위한 인터페이스를 정의합니다.
 */
public interface CommentRepository {

    /**
     * ID로 Comment 조회
     *
     * @param identity Comment 식별자
     * @return CommentRead 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<CommentRead> findById(CommentIdentity identity);

    /**
     * Comment 저장 (생성/수정)
     *
     * @param comment 저장할 Comment
     * @return 저장된 Comment
     */
    Comment save(Comment comment);

    /**
     * ID로 Comment 삭제
     *
     * @param identity Comment 식별자
     */
    void deleteById(CommentIdentity identity);

    /**
     * Comment 존재 여부 확인
     *
     * @param identity Comment 식별자
     * @return 존재하면 true, 없으면 false
     */
    boolean existsById(CommentIdentity identity);

    /**
     * 모든 Comment 조회
     *
     * @return CommentRead 목록
     */
    List<CommentRead> findAll();

    /**
     * Find all comments by target type and target ID
     *
     * @param targetType Target type (JOB, COMMUNITY_POST, TECH_BLOG, etc.)
     * @param targetId Target ID
     * @return List of comments with user information
     */
    List<CommentRead> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /**
     * Find comments by target type and target ID with paging (offset-based)
     *
     * @param targetType Target type (JOB, COMMUNITY_POST, TECH_BLOG, etc.)
     * @param targetId Target ID
     * @param offset Number of comments to skip
     * @param limit Maximum number of comments to return
     * @return List of comments with user information ordered by (commentOrder, sortNumber)
     */
    List<CommentRead> findByTargetTypeAndTargetIdWithPaging(TargetType targetType, Long targetId, int offset, int limit);


    /**
     * 사용자 ID로 Comment 목록 조회 (페이징)
     *
     * @param userIdentity 사용자 식별자
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return CommentRead 목록
     */
    List<CommentRead> findByUserId(UserIdentity userIdentity, int page, int size);

    /**
     * 부모 댓글 ID로 대댓글 목록 조회
     *
     * @param parentId 부모 댓글 ID
     * @return CommentRead 목록 (대댓글)
     */
    List<CommentRead> findByParentId(Long parentId);

    /**
     * 특정 대상의 최대 commentOrder 조회
     *
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @return 최대 commentOrder (없으면 0)
     */
    Integer findMaxCommentOrder(TargetType targetType, Long targetId);

    /**
     * 특정 댓글 아래의 모든 sortNumber를 +1 증가
     * (새 댓글 삽입 시 기존 댓글들의 순서를 밀어내기 위함)
     * (parent 를 공유하고 , 입력된 댓글보다 하위의 댓글들. )
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param commentOrder 댓글 그룹 번호
     * @param sortNumber 기준 sortNumber (이보다 큰 값들을 +1)
     */
    void incrementSortNumbersAbove(TargetType targetType, Long targetId, Integer commentOrder, Integer sortNumber);

    /**
     * 특정 댓글의 childCount를 +1 증가
     *
     * @param commentId 댓글 ID
     */
    void incrementChildCount(Long commentId);
}
