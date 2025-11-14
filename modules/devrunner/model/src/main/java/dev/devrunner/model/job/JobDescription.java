package dev.devrunner.model.job;

import lombok.Value;

import java.util.List;

/**
 * Detailed job description including responsibilities and qualifications
 */
@Value
public class JobDescription {
    String positionIntroduction;
    List<String> responsibilities;
    List<String> qualifications;
    List<String> preferredQualifications;
    String fullDescription;

    public static JobDescription empty() {
        return new JobDescription(null, List.of(), List.of(), List.of(), null);
    }

    public static JobDescription of(
        String positionIntroduction,
        List<String> responsibilities,
        List<String> qualifications,
        List<String> preferredQualifications,
        String fullDescription
    ) {
        return new JobDescription(
            positionIntroduction,
            responsibilities,
            qualifications,
            preferredQualifications,
            fullDescription
        );
    }
}
