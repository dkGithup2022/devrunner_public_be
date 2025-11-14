package dev.devrunner.jdbc.techblog.repository;

import dev.devrunner.model.common.Popularity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Set;

/**
 * TechBlog Spring Data JDBC Entity
 *
 * Model 클래스 스펙을 기반으로 생성된 데이터베이스 매핑용 엔티티
 * - Popularity: @Embedded로 tech_blogs 테이블에 컬럼으로 저장
 * - tags/techCategories: 별도 테이블로 정규화
 */
@Table("tech_blogs")
@Getter
@AllArgsConstructor
public class TechBlogEntity {
    @Id
    private Long id;
    private String url;
    private String company;
    private String title;
    private String oneLiner;       // 한 줄 소개
    private String summary;        // 요약본 (영어)
    private String summaryKo;      // 요약본 (한국어)
    private String markdownBody;
    private String thumbnailUrl;

    // 1:N 관계 - tech_blog_tech_categories 테이블로 분리
    @MappedCollection(idColumn = "tech_blog_id", keyColumn = "tech_blog_id")
    private Set<TechBlogCategory> techCategories;

    private String originalUrl;

    // Embedded - tech_blogs 테이블에 컬럼으로 flatten
    @Embedded.Nullable
    private Popularity popularity;

    private Boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;
}
