package dev.devrunner.jdbc.job.repository.collection;

import dev.devrunner.model.common.TechCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Job의 기술 카테고리 정보를 저장하는 자식 엔티티
 * Spring Data JDBC가 자동으로 job_tech_categories 테이블과 매핑
 */
@Table("job_tech_categories")
@Getter
@AllArgsConstructor
public class JobTechCategory {
    @Column("category_name")
    private TechCategory categoryName;
}
