package dev.devrunner.jdbc.job.repository;

import dev.devrunner.jdbc.job.repository.collection.*;
import dev.devrunner.jdbc.job.repository.embedded.*;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.job.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Set;

/**
 * Job Spring Data JDBC Entity
 *
 * Model 클래스 스펙을 기반으로 생성된 데이터베이스 매핑용 엔티티
 * - ExperienceRequirementEmbeddable: @Embedded로 jobs 테이블에 컬럼으로 저장
 * - InterviewProcessEmbeddable: @Embedded로 jobs 테이블에 컬럼으로 저장
 * - JobCompensationEmbeddable: @Embedded로 jobs 테이블에 컬럼으로 저장
 * - PopularityEmbeddable: @Embedded로 jobs 테이블에 컬럼으로 저장
 * - techCategories, locations: 별도 테이블로 정규화
 * - JobDescription: Mixed (positionIntroduction/fullDescription은 jobs 테이블, 나머지는 별도 테이블)
 */
@Table("jobs")
@Getter
@AllArgsConstructor
public class JobEntity {
    @Id
    private Long id;
    private String url;
    private Company company;
    private String title;
    private String organization;
    private String oneLineSummary;
    private String summary;

    // ExperienceRequirement - Embedded
    @Embedded.Nullable
    private ExperienceRequirementEmbeddable experience;

    private EmploymentType employmentType;
    private PositionCategory positionCategory;
    private RemotePolicy remotePolicy;

    // 1:N 관계 - job_tech_categories 테이블로 분리
    @MappedCollection(idColumn = "job_id", keyColumn = "job_id")
    private Set<JobTechCategory> techCategories;

    private Instant startedAt;
    private Instant endedAt;
    private Boolean isOpenEnded;
    private Boolean isClosed;

    // 1:N 관계 - job_locations 테이블로 분리
    @MappedCollection(idColumn = "job_id", keyColumn = "job_id")
    private Set<JobLocation> locations;

    // JobDescription - Mixed approach
    private String positionIntroduction;
    private String fullDescription;

    @MappedCollection(idColumn = "job_id", keyColumn = "job_id")
    private Set<JobResponsibility> responsibilities;

    @MappedCollection(idColumn = "job_id", keyColumn = "job_id")
    private Set<JobQualification> qualifications;

    @MappedCollection(idColumn = "job_id", keyColumn = "job_id")
    private Set<JobPreferredQualification> preferredQualifications;

    // InterviewProcess - Embedded
    @Embedded.Nullable
    private InterviewProcessEmbeddable interviewProcess;

    // Compensation - Embedded
    @Embedded.Nullable
    private JobCompensationEmbeddable compensation;

    // Popularity - Embedded
    @Embedded.Nullable
    private PopularityEmbeddable popularity;

    private Boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;
}
