package dev.devrunner.service.techblog;

import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;

/**
 * TechBlog 도메인 변경 서비스 인터페이스
 *
 * CQRS 패턴의 Command 책임을 담당하며,
 * Infrastructure Repository 기반으로 변경 로직을 제공합니다.
 */
public interface TechBlogWriter {

    /**
     * TechBlog 저장 (생성/수정)
     *
     * @param techBlog 저장할 TechBlog
     * @return 저장된 TechBlog
     */
    TechBlog upsert(TechBlog techBlog);

    /**
     * ID로 TechBlog 삭제
     *
     * @param identity TechBlog 식별자
     */
    void delete(TechBlogIdentity identity);
}
