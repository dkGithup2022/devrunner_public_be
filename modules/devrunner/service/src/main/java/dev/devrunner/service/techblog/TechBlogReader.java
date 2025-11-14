package dev.devrunner.service.techblog;

import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;

import java.util.List;
import java.util.Optional;

/**
 * TechBlog 도메인 조회 서비스 인터페이스
 *
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface TechBlogReader {

    /**
     * TechBlog 읽기 (조회수 증가 포함)
     *
     * @param identity TechBlog 식별자
     * @return TechBlog 엔티티
     */
    TechBlog read(TechBlogIdentity identity);

    /**
     * ID로 TechBlog 조회 (조회수 증가 없음)
     *
     * @param identity TechBlog 식별자
     * @return TechBlog 엔티티
     */
    TechBlog getById(TechBlogIdentity identity);

    /**
     * 여러 ID로 TechBlog 목록 조회 (조회수 증가 없음)
     *
     * @param identities TechBlog 식별자 목록
     * @return TechBlog 목록
     */
    List<TechBlog> getByIds(List<TechBlogIdentity> identities);

    /**
     * 모든 TechBlog 조회
     *
     * @return TechBlog 목록
     */
    List<TechBlog> getAll();

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
    List<TechBlog> getByCompany(String company);
}
