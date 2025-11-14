package dev.devrunner.model.job;

/**
 * 포지션 카테고리 Enum
 */
public enum PositionCategory {
    BACKEND,
    FRONTEND,
    FULLSTACK,
    MOBILE,
    DATA,
    ML_AI,
    DEVOPS,
    HARDWARE,
    QA,
    NOT_CATEGORIZED;

    public static PositionCategory fromString(String input) {
        try {
            return PositionCategory.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid PositionCategory: " + input);
        }
    }
}
