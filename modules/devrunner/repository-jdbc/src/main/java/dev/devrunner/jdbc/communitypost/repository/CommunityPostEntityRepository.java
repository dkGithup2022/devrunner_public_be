package dev.devrunner.jdbc.communitypost.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CommunityPost Entity CRUD API 인터페이스
 *
 * Spring Data JDBC를 활용한 CommunityPostEntity 데이터 접근 계층
 * Infrastructure Repository 인터페이스 기반으로 필요한 메서드만 생성
 */
@Repository
public interface CommunityPostEntityRepository extends CrudRepository<CommunityPostEntity, Long> {
    List<CommunityPostEntity> findByUserId(Long userId);
    List<CommunityPostEntity> findByCompany(String company);
    List<CommunityPostEntity> findByLocation(String location);


    @Modifying
    @Query("UPDATE community_posts SET view_count = view_count + :increment WHERE id = :communityPostId")
    void increaseViewCount(@Param("communityPostId") Long communityPostId, @Param("increment") long increment);

    @Modifying
    @Query("UPDATE community_posts SET comment_count = comment_count + 1 WHERE id = :communityPostId")
    void increaseCommentCount(@Param("communityPostId") Long communityPostId);

    @Modifying
    @Query("UPDATE community_posts SET comment_count = comment_count + :increment  WHERE id = :communityPostId")
    void increaseCommentCount(@Param("communityPostId") Long communityPostId, @Param("increment") long increment);


    @Modifying
    @Query("UPDATE community_posts SET like_count = like_count + :increment WHERE id = :communityPostId")
    void increaseLikeCount(@Param("communityPostId") Long communityPostId, @Param("increment") long increment);

    @Modifying
    @Query("UPDATE community_posts SET dislike_count = dislike_count + :increment WHERE id = :communityPostId")
    void increaseDislikeCount(@Param("communityPostId") Long communityPostId, @Param("increment") long increment);

    // CommunityPostRead 조회용 - LEFT JOIN으로 nickname 포함
    @Query("SELECT cp.id, cp.user_id, cp.category, cp.title, cp.markdown_body, cp.company, cp.location, " +
           "cp.job_id, cp.comment_id, cp.view_count, cp.like_count, cp.dislike_count, " +
           "cp.comment_count, cp.is_deleted, cp.created_at, cp.updated_at, u.nickname " +
           "FROM community_posts cp " +
           "LEFT JOIN users u ON cp.user_id = u.id " +
           "WHERE cp.id = :communityPostId")
    CommunityPostWithUserDto findByIdWithUser(@Param("communityPostId") Long communityPostId);

    @Query("SELECT cp.id, cp.user_id, cp.category, cp.title, cp.markdown_body, cp.company, cp.location, " +
           "cp.job_id, cp.comment_id, cp.view_count, cp.like_count, cp.dislike_count, " +
           "cp.comment_count, cp.is_deleted, cp.created_at, cp.updated_at, u.nickname " +
           "FROM community_posts cp " +
           "LEFT JOIN users u ON cp.user_id = u.id")
    List<CommunityPostWithUserDto> findAllWithUser();

    @Query("SELECT cp.id, cp.user_id, cp.category, cp.title, cp.markdown_body, cp.company, cp.location, " +
           "cp.job_id, cp.comment_id, cp.view_count, cp.like_count, cp.dislike_count, " +
           "cp.comment_count, cp.is_deleted, cp.created_at, cp.updated_at, u.nickname " +
           "FROM community_posts cp " +
           "LEFT JOIN users u ON cp.user_id = u.id " +
           "WHERE cp.user_id = :userId")
    List<CommunityPostWithUserDto> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT cp.id, cp.user_id, cp.category, cp.title, cp.markdown_body, cp.company, cp.location, " +
           "cp.job_id, cp.comment_id, cp.view_count, cp.like_count, cp.dislike_count, " +
           "cp.comment_count, cp.is_deleted, cp.created_at, cp.updated_at, u.nickname " +
           "FROM community_posts cp " +
           "LEFT JOIN users u ON cp.user_id = u.id " +
           "WHERE cp.company = :company")
    List<CommunityPostWithUserDto> findByCompanyWithUser(@Param("company") String company);

    @Query("SELECT cp.id, cp.user_id, cp.category, cp.title, cp.markdown_body, cp.company, cp.location, " +
           "cp.job_id, cp.comment_id, cp.view_count, cp.like_count, cp.dislike_count, " +
           "cp.comment_count, cp.is_deleted, cp.created_at, cp.updated_at, u.nickname " +
           "FROM community_posts cp " +
           "LEFT JOIN users u ON cp.user_id = u.id " +
           "WHERE cp.location = :location")
    List<CommunityPostWithUserDto> findByLocationWithUser(@Param("location") String location);

    @Query("SELECT cp.id, cp.user_id, cp.category, cp.title, cp.markdown_body, cp.company, cp.location, " +
           "cp.job_id, cp.comment_id, cp.view_count, cp.like_count, cp.dislike_count, " +
           "cp.comment_count, cp.is_deleted, cp.created_at, cp.updated_at, u.nickname " +
           "FROM community_posts cp " +
           "LEFT JOIN users u ON cp.user_id = u.id " +
           "WHERE cp.id IN (:ids)")
    List<CommunityPostWithUserDto> findByIdsWithUser(@Param("ids") List<Long> ids);
}
