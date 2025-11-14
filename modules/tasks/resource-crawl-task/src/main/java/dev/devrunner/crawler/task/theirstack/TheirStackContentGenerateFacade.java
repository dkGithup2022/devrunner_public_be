package dev.devrunner.crawler.task.theirstack;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.crawler.step.CrawlTheirStackJobEntity;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.*;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.explanation.*;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.hiringProcess.*;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.pay.JobPayDetailExtractor;
import dev.devrunner.crawler.task.job.contentGenerator.contentGenerator.detail.pay.JobPayExtractor;
import dev.devrunner.crawler.theirstack.dto.JobData;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.job.*;
import dev.devrunner.openai.base.GptParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TheirStack 데이터를 Job으로 변환하는 Facade
 * <p>
 * TheirStack API 응답 (구조화된 JSON)과 AI 추출을 조합하여 Job 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TheirStackContentGenerateFacade {

    // AI Extractors (기존 재사용)
    private final JobTechCategoryExtractor techCategoryExtractor;
    private final JobPositionCategoryExtractor positionCategoryExtractor;
    private final JobOrganizationExtractor organizationExtractor;
    private final JobOneLineSummaryExtractor oneLineSummaryExtractor;
    private final JobSummaryExtractor summaryExtractor;
    private final JobPositionIntroductionExtractor positionIntroductionExtractor;
    private final JobPositionRequirementsExtractor positionRequirementsExtractor;
    private final JobResponsibilitiesExtractor responsibilitiesExtractor;
    private final JobRequiredExperienceExtractor requiredExperienceExtractor;
    private final JobHiringProcessExtractor hiringProcessExtractor;
    private final JobInterviewStepsExtractor interviewStepsExtractor;
    private final JobPayExtractor jobPayExtractor;
    private final JobPayDetailExtractor jobPayDetailExtractor;

    private final ObjectMapper objectMapper;

    /**
     * TheirStack 크롤링 데이터로부터 Job 생성
     */
    public Job generate(CrawlTheirStackJobEntity entity) {
        log.info("Generating Job from TheirStack data: theirStackJobId={}, company={}, title={}",
                entity.getTheirStackJobId(), entity.getCompany(), entity.getTitle());

        // 1. raw_data 파싱
        JobData jobData = parseRawData(entity.getRawData());

        // 2. description을 기반으로 AI 추출
        String description = entity.getDescription();
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required for AI processing");
        }

        // AI 추출 실행
        var oneLineSummaryResult = oneLineSummaryExtractor.run(GptParams.ofMini(description));
        var summaryResult = summaryExtractor.run(GptParams.ofMini(description));
        var organizationResult = organizationExtractor.run(GptParams.ofMini(description));
        var experienceResult = requiredExperienceExtractor.run(GptParams.ofMini(description));
        var techCategoryResult = techCategoryExtractor.run(GptParams.ofMini(description));
        var positionCategoryResult = positionCategoryExtractor.run(GptParams.ofMini(description));
        var positionIntroResult = positionIntroductionExtractor.run(GptParams.ofMini(description));
        var requirementsResult = positionRequirementsExtractor.run(GptParams.ofMini(description));
        var responsibilitiesResult = responsibilitiesExtractor.run(GptParams.ofMini(description));

        // 면접 프로세스 추출 (2단계)
        var interviewStepsResult = extractInterviewSteps(description);

        // 급여 정보 추출 (2단계)
        var compensationResult = extractCompensation(description, jobData);

        // 3. TheirStack 데이터 직접 매핑
        Company company = mapCompany(entity.getCompany());
        String url = entity.getUrl();
        String title = entity.getTitle();
        List<String> locations = mapLocations(entity.getLocation(), jobData);
        Instant startedAt = entity.getDatePosted();
        CareerLevel careerLevel = mapSeniorityToCareerLevel(entity.getSeniority());
        RemotePolicy remotePolicy = mapRemotePolicy(jobData);
        EmploymentType employmentType = mapEmploymentType(jobData);

        // 4. 기술 카테고리 변환
        List<TechCategory> techCategories = techCategoryResult.categories().stream()
                .map(TechCategory::safeFrom)
                .filter(cat -> cat != null)
                .collect(Collectors.toList());

        // 5. 포지션 카테고리 변환
        PositionCategory positionCategory = PositionCategory.fromString(positionCategoryResult.positionCategory());

        // 6. fullDescription 구성
        String fullDescription = buildFullDescription(positionIntroResult, responsibilitiesResult, requirementsResult);

        // 7. Job 생성
        return new Job(
                null,
                url,
                company,
                title,
                organizationResult.organization(),
                oneLineSummaryResult.oneLineSummary(),
                summaryResult,
                new ExperienceRequirement(
                        experienceResult.minYears(),
                        experienceResult.maxYears(),
                        experienceResult.experienceRequired(),
                        careerLevel
                ),
                employmentType,
                positionCategory,
                remotePolicy,
                techCategories,
                startedAt,
                null, // endedAt
                false, // isOpenEnded
                false, // isClosed
                locations,
                new JobDescription(
                        positionIntroResult.introduction(),
                        responsibilitiesResult.responsibilities(),
                        requirementsResult.qualifications(),
                        requirementsResult.preferredQualifications(),
                        fullDescription
                ),
                new InterviewProcess(
                        interviewStepsResult.hasAssignment(),
                        interviewStepsResult.hasCodingTest(),
                        interviewStepsResult.hasLiveCoding(),
                        interviewStepsResult.interviewCount(),
                        interviewStepsResult.interviewDays()
                ),
                compensationResult,
                Popularity.empty(),
                false, // isDeleted
                Instant.now(),
                Instant.now()
        );
    }

    /**
     * raw_data JSON 파싱
     */
    private JobData parseRawData(String rawData) {
        try {
            return objectMapper.readValue(rawData, JobData.class);
        } catch (Exception e) {
            log.error("Failed to parse raw_data JSON", e);
            throw new RuntimeException("Invalid raw_data format", e);
        }
    }

    /**
     * 면접 프로세스 추출 (2단계)
     */
    private JobInterviewStepsExtractor.JobInterviewStepsResult extractInterviewSteps(String description) {
        var hiringProcessText = hiringProcessExtractor.run(GptParams.ofMini(description));
        if (hiringProcessText == null || hiringProcessText.hiringProcess() == null || hiringProcessText.hiringProcess().isEmpty()) {
            return new JobInterviewStepsExtractor.JobInterviewStepsResult(
                    null, null, null, null, null
            );
        }
        return interviewStepsExtractor.run(GptParams.ofMini(hiringProcessText.hiringProcess()));
    }

    /**
     * 급여 정보 추출 (AI + TheirStack 데이터 조합)
     */
    private JobCompensation extractCompensation(String description, JobData jobData) {
        // AI로 급여 섹션 추출 시도
        var paymentSection = jobPayExtractor.run(GptParams.ofMini(description));
        JobPayDetailExtractor.JobPayDetailResult aiResult = null;

        if (paymentSection != null && paymentSection.section() != null && !paymentSection.section().isEmpty()) {
            aiResult = jobPayDetailExtractor.run(GptParams.ofMini(paymentSection.section()));
        }

        // TheirStack 데이터와 AI 결과 병합
        BigDecimal minPay = null;
        BigDecimal maxPay = null;
        String currency = "USD";
        Boolean hasStockOption = null;
        String salaryNote = jobData.getSalaryString();

        // TheirStack 우선 사용
        if (jobData.getMinAnnualSalaryUsd() != null) {
            minPay = BigDecimal.valueOf(jobData.getMinAnnualSalaryUsd());
        }
        if (jobData.getMaxAnnualSalaryUsd() != null) {
            maxPay = BigDecimal.valueOf(jobData.getMaxAnnualSalaryUsd());
        }
        if (jobData.getSalaryCurrency() != null && !jobData.getSalaryCurrency().isBlank()) {
            currency = jobData.getSalaryCurrency();
        }

        // AI 결과로 보충
        if (aiResult != null) {
            if (minPay == null && aiResult.minBasepay() != null) {
                minPay = aiResult.minBasepay();
            }
            if (maxPay == null && aiResult.maxBasepay() != null) {
                maxPay = aiResult.maxBasepay();
            }
            if (aiResult.hasStockOption() != null) {
                hasStockOption = aiResult.hasStockOption();
            }
        }

        return new JobCompensation(
                minPay,
                maxPay,
                currency,
                CompensationUnit.YEARLY,
                hasStockOption,
                salaryNote
        );
    }

    /**
     * fullDescription 구성
     */
    private String buildFullDescription(
            JobPositionIntroductionExtractor.JobPositionIntroductionResult positionIntro,
            JobResponsibilitiesExtractor.JobResponsibilitiesResult responsibilities,
            JobPositionRequirementsExtractor.JobPositionRequirementsResult requirements
    ) {
        StringBuilder builder = new StringBuilder();

        if (positionIntro.introduction() != null && !positionIntro.introduction().isEmpty()) {
            builder.append(positionIntro.introduction()).append("\n\n");
        }
        if (responsibilities.responsibilities() != null && !responsibilities.responsibilities().isEmpty()) {
            builder.append("## Responsibilities\n");
            responsibilities.responsibilities().forEach(r -> builder.append("- ").append(r).append("\n"));
            builder.append("\n");
        }
        if (requirements.qualifications() != null && !requirements.qualifications().isEmpty()) {
            builder.append("## Qualifications\n");
            requirements.qualifications().forEach(q -> builder.append("- ").append(q).append("\n"));
            builder.append("\n");
        }
        if (requirements.preferredQualifications() != null && !requirements.preferredQualifications().isEmpty()) {
            builder.append("## Preferred Qualifications\n");
            requirements.preferredQualifications().forEach(pq -> builder.append("- ").append(pq).append("\n"));
        }

        return builder.toString().trim();
    }

    /**
     * Company 매핑
     */
    private Company mapCompany(String companyName) {
        try {
            return Company.valueOf(companyName.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown company: {}, using default", companyName);
            throw new IllegalArgumentException("Unsupported company: " + companyName);
        }
    }

    /**
     * Locations 매핑
     */
    private List<String> mapLocations(String location, JobData jobData) {
        if (jobData.getLocations() != null && !jobData.getLocations().isEmpty()) {
            return jobData.getLocations().stream()
                    .map(loc -> loc.getCity() != null ? loc.getCity() : loc.getCountryName())
                    .filter(loc -> loc != null && !loc.isBlank())
                    .collect(Collectors.toList());
        }

        if (location != null && !location.isBlank()) {
            return List.of(location);
        }

        return List.of();
    }

    /**
     * Seniority → CareerLevel 매핑
     */
    private CareerLevel mapSeniorityToCareerLevel(String seniority) {
        if (seniority == null || seniority.isBlank()) {
            return CareerLevel.ENTRY; // 기본값
        }

        String lower = seniority.toLowerCase();
        if (lower.contains("entry") || lower.contains("junior") || lower.contains("associate")) {
            return CareerLevel.ENTRY;
        } else {
            return CareerLevel.EXPERIENCED;
        }
    }

    /**
     * remote/hybrid → RemotePolicy 매핑
     */
    private RemotePolicy mapRemotePolicy(JobData jobData) {
        if (Boolean.TRUE.equals(jobData.getRemote())) {
            return RemotePolicy.REMOTE;
        } else if (Boolean.TRUE.equals(jobData.getHybrid())) {
            return RemotePolicy.HYBRID;
        } else {
            return RemotePolicy.ONSITE;
        }
    }

    /**
     * employmentStatuses → EmploymentType 매핱
     */
    private EmploymentType mapEmploymentType(JobData jobData) {
        if (jobData.getEmploymentStatuses() != null && !jobData.getEmploymentStatuses().isEmpty()) {
            String first = jobData.getEmploymentStatuses().get(0).toLowerCase();
            if (first.contains("contract")) {
                return EmploymentType.CONTRACT;
            } else if (first.contains("intern")) {
                return EmploymentType.INTERN;
            }
        }
        return EmploymentType.FULL_TIME; // 기본값
    }
}
