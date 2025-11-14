package dev.devrunner.model.job;

/**
 * 고용 형태 Enum
 */
public enum EmploymentType {
    FULL_TIME,  // 정규직
    CONTRACT,   // 계약직
    INTERN,     // 인턴
    UNKNOWN;    // 정보 없음

    public static EmploymentType from(String value) {
        if (value == null) return UNKNOWN;

        try {
            return EmploymentType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("value : " + value);
        }
    }
}
