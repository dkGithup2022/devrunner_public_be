package dev.devrunner.jdbc.job.repository;

import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.jdbc.job.repository.collection.*;
import dev.devrunner.jdbc.job.repository.embedded.*;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobDescription;
import dev.devrunner.model.job.JobIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Job Repository 구현체
 * <p>
 * 헥사고날 아키텍처에서 Adapter 역할을 수행하며,
 * Infrastructure의 JobRepository 인터페이스를
 * Spring Data JDBC를 활용하여 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class JobJdbcRepository implements JobRepository {

    private final JobEntityRepository entityRepository;

    @Override
    public Optional<Job> findById(JobIdentity identity) {
        return entityRepository.findById(identity.getJobId())
                .map(this::toDomain);
    }

    @Override
    public List<Job> findByIdsIn(List<JobIdentity> identities) {
        return entityRepository.findByIdIn(identities.stream().map(JobIdentity::getJobId).toList())
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Job save(Job job) {
        JobEntity entity = toEntity(job);
        JobEntity saved = entityRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(JobIdentity identity) {
        entityRepository.deleteById(identity.getJobId());
    }

    @Override
    public boolean existsById(JobIdentity identity) {
        return entityRepository.existsById(identity.getJobId());
    }

    @Override
    public List<Job> findAll() {
        return StreamSupport.stream(entityRepository.findAll().spliterator(), false)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Job> findByUrl(String url) {
        return entityRepository.findByUrl(url)
                .map(this::toDomain);
    }

    @Override
    public List<Job> findByCompany(String company) {
        return entityRepository.findByCompany(company).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void increaseViewCount(JobIdentity identity, long increment) {
        entityRepository.increaseViewCount(identity.getJobId(), increment);
    }

    @Override
    public void increaseCommentCount(JobIdentity identity) {
        entityRepository.increaseCommentCount(identity.getJobId());
    }

    @Override
    public void increaseLikeCount(JobIdentity identity, long increment) {
        entityRepository.increaseLikeCount(identity.getJobId(), increment);
    }

    @Override
    public void increaseDislikeCount(JobIdentity identity, long increment) {
        entityRepository.increaseDislikeCount(identity.getJobId(), increment);
    }

    /**
     * Entity ↔ Domain 변환 메서드
     * Spring Data JDBC가 자동으로 컬렉션과 embedded 객체를 처리
     */
    private Job toDomain(JobEntity entity) {
        List<String> responsibilities = entity.getResponsibilities() != null ?
                entity.getResponsibilities().stream()
                        .map(JobResponsibility::getResponsibility)
                        .collect(Collectors.toList()) : List.of();

        List<String> qualifications = entity.getQualifications() != null ?
                entity.getQualifications().stream()
                        .map(JobQualification::getQualification)
                        .collect(Collectors.toList()) : List.of();

        List<String> preferredQualifications = entity.getPreferredQualifications() != null ?
                entity.getPreferredQualifications().stream()
                        .map(JobPreferredQualification::getPreferredQualification)
                        .collect(Collectors.toList()) : List.of();

        // fullDescription 생성 (DB에 저장된 값이 있으면 사용, 없으면 조합)
        String fullDescription = entity.getFullDescription();
        if (fullDescription == null || fullDescription.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (entity.getPositionIntroduction() != null && !entity.getPositionIntroduction().isEmpty()) {
                sb.append(entity.getPositionIntroduction()).append("\n\n");
            }
            if (!responsibilities.isEmpty()) {
                sb.append("## Responsibilities\n");
                responsibilities.forEach(r -> sb.append("- ").append(r).append("\n"));
                sb.append("\n");
            }
            if (!qualifications.isEmpty()) {
                sb.append("## Qualifications\n");
                qualifications.forEach(q -> sb.append("- ").append(q).append("\n"));
                sb.append("\n");
            }
            if (!preferredQualifications.isEmpty()) {
                sb.append("## Preferred Qualifications\n");
                preferredQualifications.forEach(pq -> sb.append("- ").append(pq).append("\n"));
            }
            fullDescription = sb.toString().trim();
        }

        return new Job(
                entity.getId(),
                entity.getUrl(),
                entity.getCompany(),
                entity.getTitle(),
                entity.getOrganization(),
                entity.getOneLineSummary(),
                entity.getSummary(),
                entity.getExperience() != null ? entity.getExperience().toDomain() : null,
                entity.getEmploymentType(),
                entity.getPositionCategory(),
                entity.getRemotePolicy(),
                entity.getTechCategories() != null ?
                        entity.getTechCategories().stream()
                                .map(JobTechCategory::getCategoryName)
                                .collect(Collectors.toList()) : List.of(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                entity.getIsOpenEnded(),
                entity.getIsClosed(),
                entity.getLocations() != null ?
                        entity.getLocations().stream()
                                .map(JobLocation::getLocationName)
                                .collect(Collectors.toList()) : List.of(),
                JobDescription.of(
                        entity.getPositionIntroduction(),
                        responsibilities,
                        qualifications,
                        preferredQualifications,
                        fullDescription
                ),
                entity.getInterviewProcess() != null ? entity.getInterviewProcess().toDomain() : null,
                entity.getCompensation() != null ? entity.getCompensation().toDomain() : null,
                entity.getPopularity() != null ? entity.getPopularity().toDomain() : null,
                entity.getIsDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private JobEntity toEntity(Job domain) {
        return new JobEntity(
                domain.getJobId(),
                domain.getUrl(),
                domain.getCompany(),
                domain.getTitle(),
                domain.getOrganization(),
                domain.getOneLineSummary(),
                domain.getSummary(),
                // ExperienceRequirement - Embeddable로 변환
                ExperienceRequirementEmbeddable.from(domain.getExperience()),
                domain.getEmploymentType(),
                domain.getPositionCategory(),
                domain.getRemotePolicy(),
                // TechCategories - String으로 변환
                domain.getTechCategories() != null ?
                        domain.getTechCategories().stream()
                                .map(JobTechCategory::new)
                                .collect(Collectors.toSet()) : new HashSet<>(),
                domain.getStartedAt(),
                domain.getEndedAt(),
                domain.getIsOpenEnded(),
                domain.getIsClosed(),
                // Locations
                domain.getLocations() != null ?
                        domain.getLocations().stream()
                                .map(JobLocation::new)
                                .collect(Collectors.toSet()) : new HashSet<>(),
                // JobDescription fields
                domain.getDescription() != null ? domain.getDescription().getPositionIntroduction() : null,
                domain.getDescription() != null ? domain.getDescription().getFullDescription() : null,
                domain.getDescription() != null && domain.getDescription().getResponsibilities() != null ?
                        domain.getDescription().getResponsibilities().stream()
                                .map(JobResponsibility::new)
                                .collect(Collectors.toSet()) : new HashSet<>(),
                domain.getDescription() != null && domain.getDescription().getQualifications() != null ?
                        domain.getDescription().getQualifications().stream()
                                .map(JobQualification::new)
                                .collect(Collectors.toSet()) : new HashSet<>(),
                domain.getDescription() != null && domain.getDescription().getPreferredQualifications() != null ?
                        domain.getDescription().getPreferredQualifications().stream()
                                .map(JobPreferredQualification::new)
                                .collect(Collectors.toSet()) : new HashSet<>(),
                // InterviewProcess - Embeddable로 변환
                InterviewProcessEmbeddable.from(domain.getInterviewProcess()),
                // JobCompensation - Embeddable로 변환
                JobCompensationEmbeddable.from(domain.getCompensation()),
                // Popularity - Embeddable로 변환
                PopularityEmbeddable.from(domain.getPopularity()),
                domain.getIsDeleted(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
