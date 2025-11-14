package dev.devrunner.jdbc.techblog.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TechBlog Entity CRUD API 인터페이스
 *
 * Spring Data JDBC를 활용한 TechBlogEntity 데이터 접근 계층
 * Infrastructure Repository 인터페이스 기반으로 필요한 메서드만 생성
 */
@Repository
public interface TechBlogEntityRepository extends CrudRepository<TechBlogEntity, Long> {
    Optional<TechBlogEntity> findByUrl(String url);
    List<TechBlogEntity> findByCompany(String company);
    List<TechBlogEntity> findByIdIn(List<Long> ids);

    @Modifying
    @Query("UPDATE tech_blogs SET view_count = view_count + :increment WHERE id = :techBlogId")
    void increaseViewCount(@Param("techBlogId") Long techBlogId, @Param("increment") long increment);

    @Modifying
    @Query("UPDATE tech_blogs SET comment_count = comment_count + :increment WHERE id = :techBlogId")
    void increaseCommentCount(@Param("techBlogId") Long techBlogId, @Param("increment") long increment);

}
