package dev.devrunner.service.job.view;

import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.job.JobIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultJobViewMemoryTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private DefaultJobViewMemory viewMemory;

    private Map<Long, AtomicLong> viewCounts;

    @BeforeEach
    void setUp() throws Exception {
        // Reflection을 사용하여 private viewCounts 필드에 접근
        Field field = DefaultJobViewMemory.class.getDeclaredField("viewCounts");
        field.setAccessible(true);
        viewCounts = (Map<Long, AtomicLong>) field.get(viewMemory);
        viewCounts.clear();
    }

    @Test
    void countUp_validId_incrementsViewCount() {
        // given
        Long jobId = 1L;

        // when
        viewMemory.countUp(jobId);

        // then
        assertTrue(viewCounts.containsKey(jobId));
        assertEquals(1, viewCounts.get(jobId).get());
    }

    @Test
    void countUp_multipleIncrements_accumulatesViewCount() {
        // given
        Long jobId = 1L;

        // when
        viewMemory.countUp(jobId);
        viewMemory.countUp(jobId);
        viewMemory.countUp(jobId);

        // then
        assertEquals(3, viewCounts.get(jobId).get());
    }

    @Test
    void countUp_nullId_doesNotThrowException() {
        // when & then
        assertDoesNotThrow(() -> viewMemory.countUp(null));
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void countUp_multipleJobIds_incrementsSeparately() {
        // given
        Long jobId1 = 1L;
        Long jobId2 = 2L;

        // when
        viewMemory.countUp(jobId1);
        viewMemory.countUp(jobId1);
        viewMemory.countUp(jobId2);

        // then
        assertEquals(2, viewCounts.get(jobId1).get());
        assertEquals(1, viewCounts.get(jobId2).get());
    }

    @Test
    void flush_withViewCounts_callsRepositoryIncreaseViewCount() {
        // given
        Long jobId = 1L;
        viewMemory.countUp(jobId);
        viewMemory.countUp(jobId);
        viewMemory.countUp(jobId);
        doNothing().when(jobRepository)
            .increaseViewCount(any(JobIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(jobRepository).increaseViewCount(
            eq(new JobIdentity(jobId)),
            eq(3L)
        );
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void flush_withMultipleJobs_callsRepositoryForEach() {
        // given
        Long jobId1 = 1L;
        Long jobId2 = 2L;
        viewMemory.countUp(jobId1);
        viewMemory.countUp(jobId1);
        viewMemory.countUp(jobId2);
        doNothing().when(jobRepository)
            .increaseViewCount(any(JobIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(jobRepository).increaseViewCount(
            eq(new JobIdentity(jobId1)),
            eq(2L)
        );
        verify(jobRepository).increaseViewCount(
            eq(new JobIdentity(jobId2)),
            eq(1L)
        );
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void flush_emptyViewCounts_doesNotCallRepository() {
        // given
        // viewCounts is empty

        // when
        viewMemory.flush();

        // then
        verify(jobRepository, never()).increaseViewCount(any(), anyLong());
    }

    @Test
    void flush_repositoryThrowsException_continuesProcessing() {
        // given
        Long jobId1 = 1L;
        Long jobId2 = 2L;
        viewMemory.countUp(jobId1);
        viewMemory.countUp(jobId2);

        doThrow(new RuntimeException("DB error"))
            .when(jobRepository)
            .increaseViewCount(eq(new JobIdentity(jobId1)), anyLong());

        doNothing().when(jobRepository)
            .increaseViewCount(eq(new JobIdentity(jobId2)), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(jobRepository).increaseViewCount(
            eq(new JobIdentity(jobId1)),
            eq(1L)
        );
        verify(jobRepository).increaseViewCount(
            eq(new JobIdentity(jobId2)),
            eq(1L)
        );
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void flush_clearsViewCountsAfterFlush() {
        // given
        viewMemory.countUp(1L);
        viewMemory.countUp(2L);
        viewMemory.countUp(3L);
        doNothing().when(jobRepository)
            .increaseViewCount(any(JobIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void countUp_afterFlush_startsFromZero() {
        // given
        Long jobId = 1L;
        viewMemory.countUp(jobId);
        viewMemory.countUp(jobId);
        doNothing().when(jobRepository)
            .increaseViewCount(any(JobIdentity.class), anyLong());

        // when
        viewMemory.flush();
        viewMemory.countUp(jobId);

        // then
        assertEquals(1, viewCounts.get(jobId).get());
    }
}
