package dev.devrunner.model.job;

import lombok.Value;

/**
 * Experience requirement for a job position
 */
@Value
public class ExperienceRequirement {
    Integer minYears;
    Integer maxYears;

    Boolean required;

    CareerLevel careerLevel;

    public static ExperienceRequirement empty() {
        return new ExperienceRequirement(null, null, false, CareerLevel.ENTRY);
    }

    public static ExperienceRequirement of(Integer minYears, Integer maxYears, Boolean required, CareerLevel careerLevel) {
        return new ExperienceRequirement(minYears, maxYears, required, careerLevel);
    }
}
