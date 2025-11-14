package dev.devrunner.jdbc.job.repository.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("job_qualifications")
@Getter
@AllArgsConstructor
public class JobQualification {
    @Column("qualification")
    private String qualification;
}
