package dev.devrunner.model.job;

import lombok.Value;

/**
 * Interview process details for a job position
 */
@Value
public class InterviewProcess {
    Boolean hasAssignment;
    Boolean hasCodingTest;
    Boolean hasLiveCoding;
    Integer interviewCount;
    Integer interviewDays;

    public static InterviewProcess empty() {
        return new InterviewProcess(false, false, false, null, null);
    }

    public static InterviewProcess of(
        Boolean hasAssignment,
        Boolean hasCodingTest,
        Boolean hasLiveCoding,
        Integer interviewCount,
        Integer interviewDays
    ) {
        return new InterviewProcess(
            hasAssignment,
            hasCodingTest,
            hasLiveCoding,
            interviewCount,
            interviewDays
        );
    }
}
