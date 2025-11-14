package dev.devrunner.jdbc.techblog.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * TechBlog의 기술 카테고리 정보를 저장하는 자식 엔티티
 * Spring Data JDBC가 자동으로 tech_blog_tech_categories 테이블과 매핑
 */
@Table("tech_blog_tech_categories")
@Getter
@AllArgsConstructor
public class TechBlogCategory {
    @Column("category_name")
    private String categoryName;
}
