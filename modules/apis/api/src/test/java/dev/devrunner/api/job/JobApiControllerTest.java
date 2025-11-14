package dev.devrunner.api.job;

import dev.devrunner.api.job.dto.JobRead;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.job.*;
import dev.devrunner.service.job.JobReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * JobApiController 테스트
 *
 * @ExtendWith(MockitoExtension.class) 사용
 * MockMvc 없이 Controller를 직접 호출하여 테스트
 */
@ExtendWith(MockitoExtension.class)
class JobApiControllerTest {

    @Mock
    private JobReader jobReader;

    @InjectMocks
    private JobApiController controller;

    // ========== getJob 테스트 ==========

    @Test
    void getJob_existingId_returnsOkWithJob() {
        // given
        Long jobId = 1L;
        Job job = createSampleJob(jobId);

        when(jobReader.read(new JobIdentity(jobId))).thenReturn(job);

        // when
        ResponseEntity<JobRead> response = controller.getJob(jobId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getJobId()).isEqualTo(jobId);
        assertThat(response.getBody().getCompany()).isEqualTo("GOOGLE");
        assertThat(response.getBody().getTitle()).isEqualTo("Senior Backend Developer");
        assertThat(response.getBody().getCareerLevel()).isEqualTo(CareerLevel.EXPERIENCED);
        assertThat(response.getBody().getEmploymentType()).isEqualTo(EmploymentType.FULL_TIME);
        verify(jobReader).read(new JobIdentity(jobId));
    }

    @Test
    void getJob_nonExistingId_throwsException() {
        // given
        Long jobId = 999L;

        when(jobReader.read(new JobIdentity(jobId)))
                .thenThrow(new RuntimeException("Job not found"));

        // when & then
        try {
            controller.getJob(jobId);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Job not found");
        }

        verify(jobReader).read(new JobIdentity(jobId));
    }

    // ========== 헬퍼 메서드 ==========

    private Job createSampleJob(Long jobId) {
        return new Job(
                jobId,                                  // jobId
                "https://test.com/job/" + jobId,        // url
                Company.GOOGLE,                         // company
                "Senior Backend Developer",             // title
                "Engineering Team",                     // organization
                "Great opportunity",                    // oneLineSummary
                null,                                   // summary (검색용, Read에서 제외)
                ExperienceRequirement.of(3, 7, true, CareerLevel.EXPERIENCED), // experience
                EmploymentType.FULL_TIME,               // employmentType
                PositionCategory.BACKEND,               // positionCategory
                RemotePolicy.HYBRID,                    // remotePolicy
                List.of(TechCategory.JAVA, TechCategory.MYSQL), // techCategories
                Instant.now(),                          // startedAt
                null,                                   // endedAt
                false,                                  // isOpenEnded
                false,                                  // isClosed
                List.of("Seoul"),                       // locations
                JobDescription.of(null, List.of(), List.of(), List.of(), "Test markdown body"), // description
                InterviewProcess.of(true, true, false, 3, 14), // interviewProcess
                JobCompensation.empty(),                // compensation
                Popularity.empty(),                     // popularity
                false,                                  // isDeleted
                Instant.now(),                          // createdAt
                Instant.now()                           // updatedAt
        );
    }
}
