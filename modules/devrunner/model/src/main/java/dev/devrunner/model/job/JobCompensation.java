package dev.devrunner.model.job;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class JobCompensation {
    BigDecimal minBasePay;
    BigDecimal maxBasePay;
    String currency;              // TODO: Analyze actual values and convert to enum later
    CompensationUnit unit;
    Boolean hasStockOption;
    String salaryNote;

    public static JobCompensation empty() {
        return new JobCompensation(null, null, "USD", CompensationUnit.YEARLY, false, null);
    }
}
