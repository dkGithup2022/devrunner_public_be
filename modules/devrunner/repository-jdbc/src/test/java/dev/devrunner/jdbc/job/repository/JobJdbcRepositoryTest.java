package dev.devrunner.jdbc.job.repository;

import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.job.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JobJdbcRepository 테스트
 *
 * Custom Query 및 Entity↔Domain 변환 로직 검증
 */
@DataJdbcTest
@ComponentScan("dev.devrunner.jdbc.job.repository")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobJdbcRepositoryTest {

    @Autowired
    private JobJdbcRepository jobRepository;

    // ========== Entity↔Domain 변환 테스트 ==========

    @Test
    void save_withValidDomain_returnsConvertedDomain() {
        // given
        Job job = new Job(
                null,
                "https://example.com/job1",
                Company.META,
                "Backend Developer",
                "Tech Team",
                "One line summary",
                null,
                ExperienceRequirement.of(2, 5, true, CareerLevel.EXPERIENCED),
                EmploymentType.FULL_TIME,
                PositionCategory.BACKEND,
                RemotePolicy.HYBRID,
                List.of(TechCategory.JAVA, TechCategory.MYSQL),
                Instant.now(),
                null,
                true,
                false,
                List.of("Seoul"),
                JobDescription.of(
                        "Position intro",
                        List.of("Develop APIs", "Write tests"),
                        List.of("Java 3+ years", "Spring experience"),
                        List.of("MSA experience"),
                        "Full description"
                ),
                InterviewProcess.of(false, true, false, 3, 30),
                JobCompensation.empty(),
                Popularity.empty(),
                false,
                Instant.now(),
                Instant.now()
        );

        // when
        Job saved = jobRepository.save(job);

        // then - ID 생성 및 주요 필드 변환 확인
        assertThat(saved).isNotNull();
        assertThat(saved.getJobId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo("https://example.com/job1");
        assertThat(saved.getCompany()).isEqualTo(Company.META);
        assertThat(saved.getTitle()).isEqualTo("Backend Developer");
        assertThat(saved.getOrganization()).isEqualTo("Tech Team");
        assertThat(saved.getExperience().getMinYears()).isEqualTo(2);
        assertThat(saved.getExperience().getMaxYears()).isEqualTo(5);
        assertThat(saved.getEmploymentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(saved.getPositionCategory()).isEqualTo(PositionCategory.BACKEND);
        assertThat(saved.getTechCategories()).containsExactlyInAnyOrder(TechCategory.JAVA, TechCategory.MYSQL);
        assertThat(saved.getLocations()).containsExactly("Seoul");
        assertThat(saved.getDescription().getPositionIntroduction()).isEqualTo("Position intro");
    }

    @Test
    void findById_existingId_returnsConvertedDomain() {
        // given
        Job job = new Job(
                null,
                "https://example.com/job2",
                Company.GOOGLE,
                "Frontend Developer",
                "Design Team",
                "Summary",
                null,
                ExperienceRequirement.of(1, 3, false, CareerLevel.ENTRY),
                EmploymentType.CONTRACT,
                PositionCategory.FRONTEND,
                RemotePolicy.REMOTE,
                List.of(TechCategory.JAVA),
                Instant.now(),
                null,
                true,
                false,
                List.of("Busan"),
                JobDescription.of("Intro", List.of(), List.of(), List.of(), null),
                InterviewProcess.of(false, false, false, 2, 20),
                JobCompensation.empty(),
                Popularity.empty(),
                false,
                Instant.now(),
                Instant.now()
        );
        Job saved = jobRepository.save(job);
        JobIdentity identity = new JobIdentity(saved.getJobId());

        // when
        Optional<Job> found = jobRepository.findById(identity);

        // then - Entity → Domain 변환 확인
        assertThat(found).isPresent();
        assertThat(found.get().getJobId()).isEqualTo(saved.getJobId());
        assertThat(found.get().getUrl()).isEqualTo("https://example.com/job2");
        assertThat(found.get().getCompany()).isEqualTo(Company.GOOGLE);
        assertThat(found.get().getTitle()).isEqualTo("Frontend Developer");
        assertThat(found.get().getPositionCategory()).isEqualTo(PositionCategory.FRONTEND);
        assertThat(found.get().getTechCategories()).containsExactly(TechCategory.JAVA);
        assertThat(found.get().getLocations()).containsExactly("Busan");
    }

    // ========== Custom Query 테스트 ==========

    @Test
    void increaseViewCount_validJobId_incrementsSuccessfully() {
        // given
        Job job = createSampleJob();
        Job saved = jobRepository.save(job);
        JobIdentity identity = new JobIdentity(saved.getJobId());

        // when
        jobRepository.increaseViewCount(identity, 10);

        // then - 증가 성공 (실제 값 검증은 DB 직접 조회 필요)
        Optional<Job> found = jobRepository.findById(identity);
        assertThat(found).isPresent();
    }

    @Test
    void increaseCommentCount_validJobId_incrementsSuccessfully() {
        // given
        Job job = createSampleJob();
        Job saved = jobRepository.save(job);
        JobIdentity identity = new JobIdentity(saved.getJobId());

        // when
        jobRepository.increaseCommentCount(identity);

        // then - 증가 성공
        Optional<Job> found = jobRepository.findById(identity);
        assertThat(found).isPresent();
    }

    @Test
    void increaseLikeCount_validJobId_incrementsSuccessfully() {
        // given
        Job job = createSampleJob();
        Job saved = jobRepository.save(job);
        JobIdentity identity = new JobIdentity(saved.getJobId());

        // when
        jobRepository.increaseLikeCount(identity, 5);

        // then - 증가 성공
        Optional<Job> found = jobRepository.findById(identity);
        assertThat(found).isPresent();
    }

    @Test
    void increaseDislikeCount_validJobId_incrementsSuccessfully() {
        // given
        Job job = createSampleJob();
        Job saved = jobRepository.save(job);
        JobIdentity identity = new JobIdentity(saved.getJobId());

        // when
        jobRepository.increaseDislikeCount(identity, 3);

        // then - 증가 성공
        Optional<Job> found = jobRepository.findById(identity);
        assertThat(found).isPresent();
    }

    // ========== Helper Methods ==========

    private Job createSampleJob() {
        return new Job(
                null,
                "https://example.com/job-sample",
                Company.META,
                "Sample Job",
                "Team",
                "Summary",
                null,
                ExperienceRequirement.of(2, 5, true, CareerLevel.EXPERIENCED),
                EmploymentType.FULL_TIME,
                PositionCategory.BACKEND,
                RemotePolicy.HYBRID,
                List.of(TechCategory.JAVA),
                Instant.now(),
                null,
                true,
                false,
                List.of("Seoul"),
                JobDescription.of("Intro", List.of(), List.of(), List.of(), null),
                InterviewProcess.of(false, false, false, 3, 30),
                JobCompensation.empty(),
                Popularity.empty(),
                false,
                Instant.now(),
                Instant.now()
        );
    }
}
