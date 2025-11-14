package dev.devrunner.jdbc.job.repository.embedded;

import dev.devrunner.model.job.InterviewProcess;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * InterviewProcess의 Entity 레이어 표현
 * Spring Data JDBC의 @Embedded로 사용되며, jobs 테이블의 컬럼으로 flatten됩니다.
 */
@Getter
@AllArgsConstructor
public class InterviewProcessEmbeddable {
    Boolean hasAssignment;
    Boolean hasCodingTest;
    Boolean hasLiveCoding;
    Integer interviewCount;
    Integer interviewDays;

    public static InterviewProcessEmbeddable from(InterviewProcess domain) {
        if (domain == null) {
            return null;
        }
        return new InterviewProcessEmbeddable(
            domain.getHasAssignment(),
            domain.getHasCodingTest(),
            domain.getHasLiveCoding(),
            domain.getInterviewCount(),
            domain.getInterviewDays()
        );
    }

    public InterviewProcess toDomain() {
        return InterviewProcess.of(
            hasAssignment,
            hasCodingTest,
            hasLiveCoding,
            interviewCount,
            interviewDays
        );
    }
}
