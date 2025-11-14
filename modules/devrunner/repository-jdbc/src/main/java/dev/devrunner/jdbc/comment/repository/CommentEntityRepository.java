package dev.devrunner.jdbc.comment.repository;

import dev.devrunner.model.common.TargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment Entity CRUD API 인터페이스
 *
 * Spring Data JDBC를 활용한 CommentEntity 데이터 접근 계층
 * Infrastructure Repository 인터페이스 기반으로 필요한 메서드만 생성
 */
@Repository
public interface CommentEntityRepository extends CrudRepository<CommentEntity, Long> {
    List<CommentEntity> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);
    List<CommentEntity> findByParentId(Long parentId);

    /**
     * Find comment by ID with user information (LEFT JOIN)
     */
    @Query("SELECT c.id, c.user_id, c.content, c.target_type, c.target_id, c.parent_id, " +
           "c.comment_order, c.level, c.sort_number, c.child_count, c.is_hidden, c.created_at, c.updated_at, " +
           "u.nickname " +
           "FROM comments c " +
           "LEFT JOIN users u ON c.user_id = u.id " +
           "WHERE c.id = :commentId")
    CommentWithUserDto findByIdWithUser(@Param("commentId") Long commentId);

    /**
     * Find all comments with user information (LEFT JOIN)
     */
    @Query("SELECT c.id, c.user_id, c.content, c.target_type, c.target_id, c.parent_id, " +
           "c.comment_order, c.level, c.sort_number, c.child_count, c.is_hidden, c.created_at, c.updated_at, " +
           "u.nickname " +
           "FROM comments c " +
           "LEFT JOIN users u ON c.user_id = u.id")
    List<CommentWithUserDto> findAllWithUser();

    /**
     * Find comments by target type and target ID with user information (LEFT JOIN)
     */
    @Query("SELECT c.id, c.user_id, c.content, c.target_type, c.target_id, c.parent_id, " +
           "c.comment_order, c.level, c.sort_number, c.child_count, c.is_hidden, c.created_at, c.updated_at, " +
           "u.nickname " +
           "FROM comments c " +
           "LEFT JOIN users u ON c.user_id = u.id " +
           "WHERE c.target_type = :targetType AND c.target_id = :targetId " +
           "ORDER BY c.comment_order, c.sort_number")
    List<CommentWithUserDto> findByTargetTypeAndTargetIdWithUser(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId
    );

    /**
     * Find comments by parent ID with user information (LEFT JOIN)
     */
    @Query("SELECT c.id, c.user_id, c.content, c.target_type, c.target_id, c.parent_id, " +
           "c.comment_order, c.level, c.sort_number, c.child_count, c.is_hidden, c.created_at, c.updated_at, " +
           "u.nickname " +
           "FROM comments c " +
           "LEFT JOIN users u ON c.user_id = u.id " +
           "WHERE c.parent_id = :parentId " +
           "ORDER BY c.comment_order, c.sort_number")
    List<CommentWithUserDto> findByParentIdWithUser(@Param("parentId") Long parentId);

    /**
     * Find comments by target type and target ID with paging (offset-based)
     */
    @Query("SELECT * FROM comments " +
           "WHERE target_type = :targetType AND target_id = :targetId " +
           "ORDER BY comment_order, sort_number " +
           "LIMIT :limit OFFSET :offset")
    List<CommentEntity> findByTargetTypeAndTargetIdWithPaging(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * Find comments by user ID with paging
     */
    @Query("SELECT c.id, c.user_id, c.content, c.target_type, c.target_id, c.parent_id, " +
            "c.comment_order, c.level, c.sort_number, c.child_count, c.is_hidden, c.created_at, c.updated_at, " +
            "u.nickname " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.user_id = :userId " +
            "ORDER BY c.created_at DESC")
    List<CommentWithUserDto> findByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Find comments by target type and target ID with paging and user information (LEFT JOIN)
     */
    @Query("SELECT c.id, c.user_id, c.content, c.target_type, c.target_id, c.parent_id, " +
           "c.comment_order, c.level, c.sort_number, c.child_count, c.is_hidden, c.created_at, c.updated_at, " +
           "u.nickname " +
           "FROM comments c " +
           "LEFT JOIN users u ON c.user_id = u.id " +
           "WHERE c.target_type = :targetType AND c.target_id = :targetId " +
           "ORDER BY c.comment_order, c.sort_number " +
           "LIMIT :limit OFFSET :offset")
    List<CommentWithUserDto> findByTargetTypeAndTargetIdWithPagingAndUser(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 특정 대상의 최대 commentOrder 조회
     */
    @Query("SELECT COALESCE(MAX(comment_order), 0) FROM comments WHERE target_type = :targetType AND target_id = :targetId")
    Integer findMaxCommentOrder(@Param("targetType") TargetType targetType, @Param("targetId") Long targetId);

    /**
     * 특정 sortNumber보다 큰 모든 댓글의 sortNumber를 +1 증가
     */
    @Modifying
    @Query("UPDATE comments SET sort_number = sort_number + 1 " +
           "WHERE target_type = :targetType AND target_id = :targetId " +
           "AND comment_order = :commentOrder AND sort_number > :sortNumber")
    void incrementSortNumbersAbove(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId,
            @Param("commentOrder") Integer commentOrder,
            @Param("sortNumber") Integer sortNumber
    );

    /**
     * 특정 댓글의 childCount를 +1 증가
     */
    @Modifying
    @Query("UPDATE comments SET child_count = child_count + 1 WHERE id = :commentId")
    void incrementChildCount(@Param("commentId") Long commentId);
}
