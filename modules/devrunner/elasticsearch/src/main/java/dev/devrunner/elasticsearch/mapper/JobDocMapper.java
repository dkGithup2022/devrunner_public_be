package dev.devrunner.elasticsearch.mapper;

import dev.devrunner.elasticsearch.document.JobDoc;
import dev.devrunner.elasticsearch.vector.E5Vectorizer;
import dev.devrunner.elasticsearch.vector.VectorizeException;
import dev.devrunner.model.job.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static dev.devrunner.elasticsearch.util.InstantConverter.toEpochMillis;

/**
 * Mapper for converting Job domain model to JobDoc (Elasticsearch document)
 * Maps to flattened JobDoc structure for optimal Elasticsearch indexing
 */
@Component
@RequiredArgsConstructor
public class JobDocMapper {

    private final E5Vectorizer vectorizer;

    /**
     * Convert Job domain model to JobDoc
     *
     * @param job the job domain model
     * @return JobDoc for Elasticsearch indexing
     */
    public JobDoc newDoc(Job job) {
        if (job == null) {
            return null;
        }

        var experience = job.getExperience();
        var description = job.getDescription();
        var interviewProcess = job.getInterviewProcess();
        var compensation = job.getCompensation();
        var popularity = job.getPopularity();

        var vector = job.getOneLineSummary() != null
                ? vectorizer.vectorize(job.getOneLineSummary())
                : null;

        if (vector == null) {
            throw new VectorizeException(
                "Cannot index Job without oneLineSummary (required for vectorization) - jobId: " + job.getJobId()
            );
        }

        return new JobDoc(
                // Document metadata
                generateDocId(job),
                job.getJobId(),
                job.getUrl(),
                job.getCompany(),
                job.getTitle(),
                job.getOrganization(),
                job.getOneLineSummary(),

                // Experience fields (flattened from ExperienceRequirement)
                experience != null ? experience.getMinYears() : null,
                experience != null ? experience.getMaxYears() : null,
                experience != null ? experience.getRequired() : null,
                experience != null ? experience.getCareerLevel() : null,

                // Job type fields
                job.getEmploymentType(),
                job.getPositionCategory(),
                job.getRemotePolicy(),
                job.getTechCategories() != null
                        ? job.getTechCategories().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList())
                        : null,

                // Date fields
                toEpochMillis(job.getStartedAt()),
                toEpochMillis(job.getEndedAt()),
                job.getIsOpenEnded(),
                job.getIsClosed(),

                // Location
                job.getLocations(),

                // Description (only fullDescription for search)
                description != null ? description.getFullDescription() : null,

                // Interview process fields (flattened from InterviewProcess)
                interviewProcess != null ? interviewProcess.getHasAssignment() : null,
                interviewProcess != null ? interviewProcess.getHasCodingTest() : null,
                interviewProcess != null ? interviewProcess.getHasLiveCoding() : null,
                interviewProcess != null ? interviewProcess.getInterviewCount() : null,
                interviewProcess != null ? interviewProcess.getInterviewDays() : null,

                // Compensation fields (flattened from JobCompensation)
                compensation != null ? compensation.getMinBasePay() : null,
                compensation != null ? compensation.getMaxBasePay() : null,
                compensation != null ? compensation.getCurrency() : null,
                compensation != null && compensation.getUnit() != null
                        ? compensation.getUnit().name() : null,
                compensation != null ? compensation.getHasStockOption() : null,

                // Popularity fields (flattened from Popularity)
                popularity != null ? popularity.getViewCount() : null,
                popularity != null ? popularity.getCommentCount() : null,
                popularity != null ? popularity.getLikeCount() : null,

                // Meta fields
                job.getIsDeleted(),
                toEpochMillis(job.getCreatedAt()),
                toEpochMillis(job.getUpdatedAt()),

                // Vector field (generated from oneLineSummary)
                vector
        );
    }

    /**
     * Generate document ID for Elasticsearch
     * Format: "job_{jobId}"
     */
    public static String generateDocId(Job job) {
        return "job_" + job.getJobId();
    }
}
