package dev.devrunner.infra.techblog.repository;

import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import java.util.List;
import java.util.Optional;

/**
 * TechBlog Repository 인터페이스
 *
 * 헥사고날 아키텍처에서 Port 역할을 수행하며,
 * 기술 블로그 도메인의 데이터 접근을 위한 인터페이스를 정의합니다.
 */
public interface TechBlogRepository {

    /**
     * ID로 TechBlog 조회
     *
     * @param identity TechBlog 식별자
     * @return TechBlog 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<TechBlog> findById(TechBlogIdentity identity);

    /**
     * 여러 ID로 TechBlog 목록 조회
     *
     * @param identities TechBlog 식별자 목록
     * @return TechBlog 목록
     */
    List<TechBlog> findByIdsIn(List<TechBlogIdentity> identities);

    /**
     * TechBlog 저장 (생성/수정)
     *
     * @param techBlog 저장할 TechBlog
     * @return 저장된 TechBlog
     */
    TechBlog save(TechBlog techBlog);

    /**
     * ID로 TechBlog 삭제
     *
     * @param identity TechBlog 식별자
     */
    void deleteById(TechBlogIdentity identity);

    /**
     * TechBlog 존재 여부 확인
     *
     * @param identity TechBlog 식별자
     * @return 존재하면 true, 없으면 false
     */
    boolean existsById(TechBlogIdentity identity);

    /**
     * 모든 TechBlog 조회
     *
     * @return TechBlog 목록
     */
    List<TechBlog> findAll();

    /**
     * URL로 TechBlog 조회
     *
     * @param url 블로그 URL
     * @return TechBlog 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<TechBlog> findByUrl(String url);

    /**
     * 회사명으로 TechBlog 목록 조회
     *
     * @param company 회사명
     * @return TechBlog 목록
     */
    List<TechBlog> findByCompany(String company);

    /**
     * 조회수 증가 (원자적 연산)
     *
     * DB 레벨에서 조회수를 증가시켜 동시성 문제를 방지합니다.
     * UPDATE tech_blog SET view_count = view_count + ? WHERE tech_blog_id = ?
     *
     * @param identity TechBlog 식별자
     * @param increment 증가시킬 조회수
     */
    void increaseViewCount(TechBlogIdentity identity, long increment);

    /**
     * 댓글수 증가 (원자적 연산)
     *
     * DB 레벨에서 댓글수를 증가시켜 동시성 문제를 방지합니다.
     * UPDATE tech_blogs SET comment_count = comment_count + 1 WHERE id = ?
     *
     * @param identity TechBlog 식별자
     */
    void increaseCommentCount(TechBlogIdentity identity);
}
