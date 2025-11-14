package dev.devrunner.service.job.impl;

import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.*;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.model.UpdateType;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultJobWriterTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private OutboxEventRecorder outboxEventRecorder;

    @InjectMocks
    private DefaultJobWriter jobWriter;

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

    @Test
    void upsert_newJob_callsRepositorySave() {
        // given
        Job newJob = Job.newJob(
            "https://example.com/job/new",
            Company.META,
            "new title",
            "new organization",
            "new full description",
            List.of("Seoul")
        );
        when(jobRepository.save(any(Job.class)))
            .thenReturn(sampleJob);

        // when
        Job result = jobWriter.upsert(newJob);

        // then
        assertNotNull(result);
        assertEquals(sampleJob.getJobId(), result.getJobId());
        verify(jobRepository).save(newJob);
    }

    @Test
    void upsert_existingJob_callsRepositorySave() {
        // given
        Job updatedJob = new Job(
            1L, "https://example.com/job/1", Company.META, "updated title",
            "test organization", "updated one line summary", "updated summary",
            ExperienceRequirement.of(0, 3, false, CareerLevel.ENTRY),
            EmploymentType.FULL_TIME, PositionCategory.BACKEND, RemotePolicy.ONSITE,
            List.of(TechCategory.JAVA), Instant.now(), null, false, false,
            List.of("Seoul"),
            JobDescription.of(null, List.of(), List.of(), List.of(), null),
            InterviewProcess.of(false, false, false, 3, 7),
            JobCompensation.empty(), Popularity.empty(), false, Instant.now(), Instant.now()
        );
        when(jobRepository.save(any(Job.class)))
            .thenReturn(updatedJob);

        // when
        Job result = jobWriter.upsert(updatedJob);

        // then
        assertNotNull(result);
        assertEquals(Company.META, result.getCompany());
        assertEquals("updated title", result.getTitle());
        verify(jobRepository).save(updatedJob);
    }

    @Test
    void upsert_jobWithTechCategories_callsRepositorySave() {
        // given
        Job jobWithTech = new Job(
            null, "https://example.com/job/tech", Company.META, "tech title",
            "tech org", null, null,
            ExperienceRequirement.of(0, 5, false, CareerLevel.EXPERIENCED),
            EmploymentType.FULL_TIME, PositionCategory.BACKEND, RemotePolicy.ONSITE,
            List.of(TechCategory.JAVA, TechCategory.MYSQL),
            Instant.now(), null, false, false, List.of("Seoul"),
            JobDescription.of(null, List.of(), List.of(), List.of(), null),
            InterviewProcess.of(true, true, false, 2, 5),
            JobCompensation.empty(), Popularity.empty(), false, Instant.now(), Instant.now()
        );
        when(jobRepository.save(any(Job.class)))
            .thenReturn(jobWithTech);

        // when
        Job result = jobWriter.upsert(jobWithTech);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTechCategories().size());
        assertTrue(result.getTechCategories().contains(TechCategory.JAVA));
        assertTrue(result.getTechCategories().contains(TechCategory.MYSQL));
        verify(jobRepository).save(jobWithTech);
    }

    @Test
    void delete_existingJob_callsRepositoryDelete() {
        // given
        JobIdentity identity = new JobIdentity(1L);
        doNothing().when(jobRepository).deleteById(identity);

        // when
        jobWriter.delete(identity);

        // then
        verify(jobRepository).deleteById(identity);
    }

    @Test
    void delete_callsRepositoryDeleteWithCorrectIdentity() {
        // given
        JobIdentity identity = new JobIdentity(999L);
        doNothing().when(jobRepository).deleteById(identity);

        // when
        jobWriter.delete(identity);

        // then
        verify(jobRepository).deleteById(identity);
    }

    @Test
    void upsert_newJob_recordsCreatedEvent() {
        // given
        Job newJob = Job.newJob(
            "https://example.com/job/new",
            Company.META,
            "new title",
            "new organization",
            "new full description",
            List.of("Seoul")
        );
        Job savedJob = new Job(
            100L, "https://example.com/job/new", Company.META, "new title",
            "new organization", null, null,
            ExperienceRequirement.of(0, 3, false, CareerLevel.ENTRY),
            EmploymentType.FULL_TIME, PositionCategory.BACKEND, RemotePolicy.ONSITE,
            List.of(TechCategory.JAVA), Instant.now(), null, false, false,
            List.of("Seoul"),
            JobDescription.of(null, List.of(), List.of(), List.of(), null),
            InterviewProcess.of(false, false, false, 3, 7),
            JobCompensation.empty(), Popularity.empty(), false, Instant.now(), Instant.now()
        );
        when(jobRepository.save(any(Job.class)))
            .thenReturn(savedJob);

        ArgumentCaptor<RecordOutboxEventCommand> eventCaptor = ArgumentCaptor.forClass(RecordOutboxEventCommand.class);

        // when
        jobWriter.upsert(newJob);

        // then
        verify(outboxEventRecorder).record(eventCaptor.capture());
        RecordOutboxEventCommand capturedEvent = eventCaptor.getValue();
        assertEquals(TargetType.JOB, capturedEvent.getTargetType());
        assertEquals(100L, capturedEvent.getTargetId());
        assertEquals(UpdateType.CREATED, capturedEvent.getUpdateType());
    }

    @Test
    void upsert_existingJob_recordsUpdatedEvent() {
        // given
        Job existingJob = new Job(
            1L, "https://example.com/job/1", Company.META, "updated title",
            "test organization", "updated one line summary", "updated summary",
            ExperienceRequirement.of(0, 3, false, CareerLevel.ENTRY),
            EmploymentType.FULL_TIME, PositionCategory.BACKEND, RemotePolicy.ONSITE,
            List.of(TechCategory.JAVA), Instant.now(), null, false, false,
            List.of("Seoul"),
            JobDescription.of(null, List.of(), List.of(), List.of(), null),
            InterviewProcess.of(false, false, false, 3, 7),
            JobCompensation.empty(), Popularity.empty(), false, Instant.now(), Instant.now()
        );
        when(jobRepository.save(any(Job.class)))
            .thenReturn(existingJob);

        ArgumentCaptor<RecordOutboxEventCommand> eventCaptor = ArgumentCaptor.forClass(RecordOutboxEventCommand.class);

        // when
        jobWriter.upsert(existingJob);

        // then
        verify(outboxEventRecorder).record(eventCaptor.capture());
        RecordOutboxEventCommand capturedEvent = eventCaptor.getValue();
        assertEquals(TargetType.JOB, capturedEvent.getTargetType());
        assertEquals(1L, capturedEvent.getTargetId());
        assertEquals(UpdateType.UPDATED, capturedEvent.getUpdateType());
    }
}
