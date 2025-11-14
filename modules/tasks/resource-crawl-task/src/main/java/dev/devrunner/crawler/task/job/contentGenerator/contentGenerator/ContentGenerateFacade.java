package dev.devrunner.crawler.task.job.contentGenerator.contentGenerator;

import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.*;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.explanation.*;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.hiringProcess.*;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.pay.JobPayDetailExtractor;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.pay.JobPayExtractor;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.job.*;
import dev.devrunner.openai.base.GptParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentGenerateFacade {

    private final JobTechCategoryExtractor techCategoryExtractor;
    private final JobLocationExtractor locationExtractor;
    private final JobEmploymentTypeExtractor employmentTypeExtractor;
    private final JobPositionCategoryExtractor positionCategoryExtractor;
    private final JobRemotePolicyExtractor remotePolicyExtractor;
    private final JobOrganizationExtractor organizationExtractor;
    private final JobDateExtractor dateExtractor;
    private final JobHiringProcessExtractor hiringProcessExtractor;
    private final JobInterviewStepsExtractor interviewStepsExtractor;
    private final JobOneLineSummaryExtractor oneLineSummaryExtractor;
    private final JobPositionIntroductionExtractor positionIntroductionExtractor;
    private final JobPositionRequirementsExtractor positionRequirementsExtractor;
    private final JobResponsibilitiesExtractor responsibilitiesExtractor;
    private final JobRequiredExperienceExtractor requiredExperienceExtractor;

    private final JobSummaryExtractor summaryExtractor;

    private final JobPayExtractor jobPayExtractor;

    private final JobPayDetailExtractor jobPayDetailExtractor;

    public Job generate(String text, String url, String title, Company company) {
        // Basic info extraction
        var techCategoryResult = techCategoryExtractor.run(GptParams.ofMini(text));
        var locationResult = locationExtractor.run(GptParams.ofMini(text));
        var employmentTypeResult = employmentTypeExtractor.run(GptParams.ofMini(text));
        var positionCategoryResult = positionCategoryExtractor.run(GptParams.ofMini(text));
        var remotePolicyResult = remotePolicyExtractor.run(GptParams.ofMini(text));
        var organizationResult = organizationExtractor.run(GptParams.ofMini(text));
        var dateResult = dateExtractor.run(GptParams.ofMini(text));
        var oneLineSummaryResult = oneLineSummaryExtractor.run(GptParams.ofMini(text));

        var summary = summaryExtractor.run(GptParams.ofMini(text));
        // Hiring process extraction (two-step)
        var interviewStepsResult = getJobInterviewStepsResult(text);

        // Explanation extraction
        var positionIntroResult = positionIntroductionExtractor.run(GptParams.ofMini(text));
        var requirementsResult = positionRequirementsExtractor.run(GptParams.ofMini(text));
        var responsibilitiesResult = responsibilitiesExtractor.run(GptParams.ofMini(text));

        var requiredExperienceResult = requiredExperienceExtractor.run(GptParams.ofMini(text));

        var compensation = getJobPayDetailResult(text);

        // Convert tech categories
        List<TechCategory> techCategories = techCategoryResult.categories().stream()
                .map(TechCategory::safeFrom)
                .filter(cat -> cat != null)
                .collect(Collectors.toList());

        // Convert employment type
        EmploymentType employmentType = EmploymentType.from(employmentTypeResult.employmentType());

        // Convert position category
        PositionCategory positionCategory = PositionCategory.fromString(positionCategoryResult.positionCategory());

        // Convert remote policy
        RemotePolicy remotePolicy = RemotePolicy.fromString(remotePolicyResult.remotePolicy());

        // Parse dates
        Instant startedAt = parseDate(dateResult.startedAt());
        Instant endedAt = parseDate(dateResult.endedAt());


        // Build fullDescription
        StringBuilder fullDescBuilder = new StringBuilder();
        if (positionIntroResult.introduction() != null && !positionIntroResult.introduction().isEmpty()) {
            fullDescBuilder.append(positionIntroResult.introduction()).append("\n\n");
        }
        if (responsibilitiesResult.responsibilities() != null && !responsibilitiesResult.responsibilities().isEmpty()) {
            fullDescBuilder.append("## Responsibilities\n");
            responsibilitiesResult.responsibilities().forEach(r -> fullDescBuilder.append("- ").append(r).append("\n"));
            fullDescBuilder.append("\n");
        }
        if (requirementsResult.qualifications() != null && !requirementsResult.qualifications().isEmpty()) {
            fullDescBuilder.append("## Qualifications\n");
            requirementsResult.qualifications().forEach(q -> fullDescBuilder.append("- ").append(q).append("\n"));
            fullDescBuilder.append("\n");
        }
        if (requirementsResult.preferredQualifications() != null && !requirementsResult.preferredQualifications().isEmpty()) {
            fullDescBuilder.append("## Preferred Qualifications\n");
            requirementsResult.preferredQualifications().forEach(pq -> fullDescBuilder.append("- ").append(pq).append("\n"));
        }
        String fullDescription = fullDescBuilder.toString().trim();

        return new Job(
                null,
                url, // url - will be set by caller
                company, // company - will be set by caller
                title, // title - will be set by caller
                organizationResult.organization(),
                oneLineSummaryResult.oneLineSummary(),
                summary,
                new ExperienceRequirement(
                        requiredExperienceResult.minYears(),
                        requiredExperienceResult.maxYears(),
                        requiredExperienceResult.experienceRequired(),
                        CareerLevel.ENTRY
                ),

                employmentType,
                positionCategory,
                remotePolicy,
                techCategories,
                startedAt,
                endedAt,
                dateResult.isOpenEnded(),
                false, // isClosed
                locationResult.locations(),

                new JobDescription(
                        positionIntroResult.introduction(),
                        responsibilitiesResult.responsibilities(),
                        requirementsResult.qualifications(),
                        requirementsResult.preferredQualifications(),
                        fullDescription
                )
                ,
                new InterviewProcess(
                        interviewStepsResult.hasAssignment(),
                        interviewStepsResult.hasCodingTest(),
                        interviewStepsResult.hasLiveCoding(),
                        interviewStepsResult.interviewCount(),
                        interviewStepsResult.interviewDays()
                )
                ,
                new JobCompensation(
                        compensation.minBasepay(),
                        compensation.maxBasepay(),
                        compensation.currency(),
                        compensation.unit(),
                        compensation.hasStockOption(),
                        compensation.salaryNote()
                ),
                Popularity.empty(), // popularity - will be initialized
                false, // isDeleted
                Instant.now(),
                Instant.now()
        );
    }

    private JobInterviewStepsExtractor.JobInterviewStepsResult getJobInterviewStepsResult(String text) {
        var hiringProcessText = hiringProcessExtractor.run(GptParams.ofMini(text));
        if (hiringProcessText == null || hiringProcessText.hiringProcess() == null || hiringProcessText.hiringProcess().isEmpty()) {
            return new JobInterviewStepsExtractor.JobInterviewStepsResult(
                    null, null, null, null, null
            );
        }
        return interviewStepsExtractor.run(GptParams.ofMini(hiringProcessText.hiringProcess()));
    }

    private JobPayDetailExtractor.JobPayDetailResult getJobPayDetailResult(String text) {
        var paymentSection = jobPayExtractor.run(GptParams.ofMini(text));
        if (paymentSection == null || paymentSection.section() == null || paymentSection.section().isEmpty()) {
            return new JobPayDetailExtractor.JobPayDetailResult(
                    null, null, null, null, null, null
            );
        }

        return jobPayDetailExtractor.run(GptParams.ofMini(paymentSection.section()));
    }

    private Instant parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            LocalDate localDate = LocalDate.parse(dateStr, formatter);
            return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }
}
