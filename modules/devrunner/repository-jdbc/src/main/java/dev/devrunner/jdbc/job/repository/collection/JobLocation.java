package dev.devrunner.jdbc.job.repository.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Job의 위치 정보를 저장하는 자식 엔티티
 * Spring Data JDBC가 자동으로 job_locations 테이블과 매핑
 */
@Table("job_locations")
@Getter
@AllArgsConstructor
public class JobLocation {
    @Column("location_name")
    private String locationName;
}
