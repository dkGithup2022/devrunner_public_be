package dev.devrunner.service.job.impl;

import dev.devrunner.exception.job.JobNotFoundException;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.job.*;
import dev.devrunner.service.job.view.JobViewMemory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultJobReaderTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobViewMemory jobViewMemory;

    @InjectMocks
    private DefaultJobReader jobReader;

    private final Job sampleJob = new Job(
        1L,                              // jobId
        "https://example.com/job/1",     // url
        Company.META,                    // company
        "test title",                    // title
        "test organization",             // organization
        "test one line summary",         // oneLineSummary
        null,                            // summary
        ExperienceRequirement.of(0, 3, false, CareerLevel.ENTRY), // experience
        EmploymentType.FULL_TIME,        // employmentType
        PositionCategory.BACKEND,        // positionCategory
        RemotePolicy.ONSITE,             // remotePolicy
        List.of(TechCategory.JAVA),      // techCategories
        Instant.now(),                   // startedAt
        null,                            // endedAt
        false,                           // isOpenEnded
        false,                           // isClosed
        List.of("Seoul"),                // locations
        JobDescription.of(null, List.of(), List.of(), List.of(), null), // description
        InterviewProcess.of(false, false, false, 3, 7), // interviewProcess
        JobCompensation.empty(),         // compensation
        Popularity.empty(),              // popularity
        false,                           // isDeleted
        Instant.now(),                   // createdAt
        Instant.now()                    // updatedAt
    );

    private final JobIdentity testIdentity = new JobIdentity(1L);

    @Test
    void read_existingId_returnsJobAndIncrementsViewCount() {
        // given
        when(jobRepository.findById(testIdentity))
            .thenReturn(Optional.of(sampleJob));

        // when
        Job result = jobReader.read(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(sampleJob.getJobId(), result.getJobId());
        assertEquals(sampleJob.getTitle(), result.getTitle());
        verify(jobRepository).findById(testIdentity);
        verify(jobViewMemory).countUp(1L);
    }

    @Test
    void read_nonExistingId_throwsJobNotFoundException() {
        // given
        when(jobRepository.findById(testIdentity))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(JobNotFoundException.class, () ->
            jobReader.read(testIdentity)
        );
        verify(jobRepository).findById(testIdentity);
        verify(jobViewMemory, never()).countUp(any());
    }

    @Test
    void getById_existingId_returnsJobWithoutViewCount() {
        // given
        when(jobRepository.findById(testIdentity))
            .thenReturn(Optional.of(sampleJob));

        // when
        Job result = jobReader.getById(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(sampleJob.getJobId(), result.getJobId());
        assertEquals(sampleJob.getTitle(), result.getTitle());
        verify(jobRepository).findById(testIdentity);
        verify(jobViewMemory, never()).countUp(any());
    }

    @Test
    void getById_nonExistingId_throwsJobNotFoundException() {
        // given
        when(jobRepository.findById(testIdentity))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(JobNotFoundException.class, () ->
            jobReader.getById(testIdentity)
        );
        verify(jobRepository).findById(testIdentity);
    }

    @Test
    void getByIds_withExistingIds_returnsList() {
        // given
        JobIdentity identity1 = new JobIdentity(1L);
        JobIdentity identity2 = new JobIdentity(2L);
        List<JobIdentity> identities = List.of(identity1, identity2);

        Job job2 = new Job(
            2L, "https://example.com/job/2", Company.META, "test title 2",
            "test organization 2", "test one line summary 2", null,
            ExperienceRequirement.of(0, 3, false, CareerLevel.ENTRY),
            EmploymentType.FULL_TIME, PositionCategory.BACKEND, RemotePolicy.ONSITE,
            List.of(TechCategory.JAVA), Instant.now(), null, false, false,
            List.of("Seoul"), JobDescription.of(null, List.of(), List.of(), List.of(), null),
            InterviewProcess.of(false, false, false, 3, 7),
            JobCompensation.empty(), Popularity.empty(), false, Instant.now(), Instant.now()
        );

        List<Job> jobs = List.of(sampleJob, job2);
        when(jobRepository.findByIdsIn(identities))
            .thenReturn(jobs);

        // when
        List<Job> result = jobReader.getByIds(identities);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jobRepository).findByIdsIn(identities);
    }

    @Test
    void getByIds_withEmptyIds_returnsEmptyList() {
        // given
        List<JobIdentity> emptyIdentities = List.of();
        when(jobRepository.findByIdsIn(emptyIdentities))
            .thenReturn(List.of());

        // when
        List<Job> result = jobReader.getByIds(emptyIdentities);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jobRepository).findByIdsIn(emptyIdentities);
    }

    @Test
    void getByIds_withPartiallyExistingIds_returnsOnlyExistingJobs() {
        // given
        JobIdentity identity1 = new JobIdentity(1L);
        JobIdentity identity2 = new JobIdentity(999L); // non-existing
        List<JobIdentity> identities = List.of(identity1, identity2);

        List<Job> jobs = List.of(sampleJob); // only one exists
        when(jobRepository.findByIdsIn(identities))
            .thenReturn(jobs);

        // when
        List<Job> result = jobReader.getByIds(identities);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleJob.getJobId(), result.get(0).getJobId());
        verify(jobRepository).findByIdsIn(identities);
    }

    @Test
    void getAll_withData_returnsList() {
        // given
        List<Job> jobs = List.of(sampleJob);
        when(jobRepository.findAll())
            .thenReturn(jobs);

        // when
        List<Job> result = jobReader.getAll();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleJob.getJobId(), result.get(0).getJobId());
        verify(jobRepository).findAll();
    }

    @Test
    void getAll_emptyData_returnsEmptyList() {
        // given
        when(jobRepository.findAll())
            .thenReturn(List.of());

        // when
        List<Job> result = jobReader.getAll();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jobRepository).findAll();
    }

    @Test
    void getByUrl_existingUrl_returnsJob() {
        // given
        String url = "https://example.com/job/1";
        when(jobRepository.findByUrl(url))
            .thenReturn(Optional.of(sampleJob));

        // when
        Job result = jobReader.getByUrl(url);

        // then
        assertNotNull(result);
        assertEquals(url, result.getUrl());
        verify(jobRepository).findByUrl(url);
    }

    @Test
    void getByUrl_nonExistingUrl_throwsJobNotFoundException() {
        // given
        String url = "https://example.com/nonexistent";
        when(jobRepository.findByUrl(url))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(JobNotFoundException.class, () ->
            jobReader.getByUrl(url)
        );
        verify(jobRepository).findByUrl(url);
    }

    @Test
    void getByCompany_existingCompany_returnsList() {
        // given
        String company = Company.META.name();
        List<Job> jobs = List.of(sampleJob);
        when(jobRepository.findByCompany(company))
            .thenReturn(jobs);

        // when
        List<Job> result = jobReader.getByCompany(company);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Company.META, result.get(0).getCompany());
        verify(jobRepository).findByCompany(company);
    }

    @Test
    void getByCompany_nonExistingCompany_returnsEmptyList() {
        // given
        String company = "nonexistent company";
        when(jobRepository.findByCompany(company))
            .thenReturn(List.of());

        // when
        List<Job> result = jobReader.getByCompany(company);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jobRepository).findByCompany(company);
    }
}
