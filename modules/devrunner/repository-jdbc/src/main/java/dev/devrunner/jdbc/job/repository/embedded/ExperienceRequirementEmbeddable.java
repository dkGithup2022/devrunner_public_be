package dev.devrunner.jdbc.job.repository.embedded;

import dev.devrunner.model.job.CareerLevel;
import dev.devrunner.model.job.ExperienceRequirement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;

/**
 * ExperienceRequirement의 Entity 레이어 표현
 * Spring Data JDBC의 @Embedded로 사용되며, jobs 테이블의 컬럼으로 flatten됩니다.
 */
@Getter
@AllArgsConstructor
public class ExperienceRequirementEmbeddable {
    Integer minYears;
    Integer maxYears;

    @Column("experience_required")
    Boolean required;

    CareerLevel careerLevel;

    public static ExperienceRequirementEmbeddable from(ExperienceRequirement domain) {
        if (domain == null) {
            return null;
        }
        return new ExperienceRequirementEmbeddable(
            domain.getMinYears(),
            domain.getMaxYears(),
            domain.getRequired(),
            domain.getCareerLevel()
        );
    }

    public ExperienceRequirement toDomain() {
        return ExperienceRequirement.of(minYears, maxYears, required, careerLevel);
    }
}
