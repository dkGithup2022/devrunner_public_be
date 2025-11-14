package dev.devrunner.jdbc.job.repository.embedded;

import dev.devrunner.model.job.CompensationUnit;
import dev.devrunner.model.job.JobCompensation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * JobCompensation의 Entity 레이어 표현
 * Spring Data JDBC의 @Embedded로 사용되며, jobs 테이블의 컬럼으로 flatten됩니다.
 */
@Getter
@AllArgsConstructor
public class JobCompensationEmbeddable {
    BigDecimal minBasePay;
    BigDecimal maxBasePay;
    String currency;
    CompensationUnit unit;
    Boolean hasStockOption;
    String salaryNote;

    public static JobCompensationEmbeddable from(JobCompensation domain) {
        if (domain == null) {
            return null;
        }
        return new JobCompensationEmbeddable(
            domain.getMinBasePay(),
            domain.getMaxBasePay(),
            domain.getCurrency(),
            domain.getUnit(),
            domain.getHasStockOption(),
            domain.getSalaryNote()
        );
    }

    public JobCompensation toDomain() {
        return new JobCompensation(
            minBasePay,
            maxBasePay,
            currency,
            unit,
            hasStockOption,
            salaryNote
        );
    }
}
