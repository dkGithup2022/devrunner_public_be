package dev.devrunner.model.job;

import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import lombok.Value;
import java.time.Instant;
import java.util.List;

@Value
public class Job implements AuditProps {
    Long jobId;
    String url;
    Company company;
    String title;
    String organization;
    String oneLineSummary;
    String summary;

    ExperienceRequirement experience;
    EmploymentType employmentType;
    PositionCategory positionCategory;
    RemotePolicy remotePolicy;
    List<TechCategory> techCategories;

    Instant startedAt;
    Instant endedAt;
    Boolean isOpenEnded;
    Boolean isClosed;
    List<String> locations;

    JobDescription description;
    InterviewProcess interviewProcess;
    JobCompensation compensation;
    Popularity popularity;

    Boolean isDeleted;
    Instant createdAt;
    Instant updatedAt;

    public static Job newJob(
        String url,
        Company company,
        String title,
        String organization,
        String fullDescription,
        List<String> locations
    ) {
        Instant now = Instant.now();
        return new Job(
            null,
            url,
            company,
            title,
            organization,
            null,
            null,
            ExperienceRequirement.empty(),
            EmploymentType.FULL_TIME,
            null,
            RemotePolicy.ONSITE,
            List.of(),
            now,
            null,
            false,
            false,
            locations,
            JobDescription.of(null, List.of(), List.of(), List.of(), fullDescription),
            InterviewProcess.empty(),
            JobCompensation.empty(),
            Popularity.empty(),
            false,
            now,
            now
        );
    }

    public Job close() {
        return new Job(
            jobId, url, company, title, organization, oneLineSummary, summary,
            experience, employmentType, positionCategory, remotePolicy, techCategories,
            startedAt, endedAt, isOpenEnded, true, locations,
            description, interviewProcess, compensation, popularity,
            isDeleted, createdAt, Instant.now()
        );
    }

    public Job incrementViewCount() {
        return new Job(
            jobId, url, company, title, organization, oneLineSummary, summary,
            experience, employmentType, positionCategory, remotePolicy, techCategories,
            startedAt, endedAt, isOpenEnded, isClosed, locations,
            description, interviewProcess, compensation,
            popularity.incrementViewCount(),
            isDeleted, createdAt, Instant.now()
        );
    }

    public Job incrementViewCount(long adder) {
        return new Job(
            jobId, url, company, title, organization, oneLineSummary, summary,
            experience, employmentType, positionCategory, remotePolicy, techCategories,
            startedAt, endedAt, isOpenEnded, isClosed, locations,
            description, interviewProcess, compensation,
            popularity.incrementViewCount(adder),
            isDeleted, createdAt, Instant.now()
        );
    }

    public Job incrementCommentCount() {
        return new Job(
            jobId, url, company, title, organization, oneLineSummary, summary,
            experience, employmentType, positionCategory, remotePolicy, techCategories,
            startedAt, endedAt, isOpenEnded, isClosed, locations,
            description, interviewProcess, compensation,
            popularity.incrementCommentCount(),
            isDeleted, createdAt, Instant.now()
        );
    }

    public Job incrementLikeCount() {
        return new Job(
            jobId, url, company, title, organization, oneLineSummary, summary,
            experience, employmentType, positionCategory, remotePolicy, techCategories,
            startedAt, endedAt, isOpenEnded, isClosed, locations,
            description, interviewProcess, compensation,
            popularity.incrementLikeCount(),
            isDeleted, createdAt, Instant.now()
        );
    }

    public Job decrementLikeCount() {
        return new Job(
            jobId, url, company, title, organization, oneLineSummary, summary,
            experience, employmentType, positionCategory, remotePolicy, techCategories,
            startedAt, endedAt, isOpenEnded, isClosed, locations,
            description, interviewProcess, compensation,
            popularity.decrementLikeCount(),
            isDeleted, createdAt, Instant.now()
        );
    }

    public Job markAsDeleted() {
        return new Job(
            jobId, url, company, title, organization, oneLineSummary, summary,
            experience, employmentType, positionCategory, remotePolicy, techCategories,
            startedAt, endedAt, isOpenEnded, isClosed, locations,
            description, interviewProcess, compensation, popularity,
            true, createdAt, Instant.now()
        );
    }
}
