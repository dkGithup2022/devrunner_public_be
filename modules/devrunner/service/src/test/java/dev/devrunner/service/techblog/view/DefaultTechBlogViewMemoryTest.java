package dev.devrunner.service.techblog.view;

import dev.devrunner.infra.techblog.repository.TechBlogRepository;
import dev.devrunner.model.techblog.TechBlogIdentity;
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
class DefaultTechBlogViewMemoryTest {

    @Mock
    private TechBlogRepository techBlogRepository;

    @InjectMocks
    private DefaultTechBlogViewMemory viewMemory;

    private Map<Long, AtomicLong> viewCounts;

    @BeforeEach
    void setUp() throws Exception {
        // Reflection을 사용하여 private viewCounts 필드에 접근
        Field field = DefaultTechBlogViewMemory.class.getDeclaredField("viewCounts");
        field.setAccessible(true);
        viewCounts = (Map<Long, AtomicLong>) field.get(viewMemory);
        viewCounts.clear();
    }

    @Test
    void countUp_validId_incrementsViewCount() {
        // given
        Long blogId = 1L;

        // when
        viewMemory.countUp(blogId);

        // then
        assertTrue(viewCounts.containsKey(blogId));
        assertEquals(1, viewCounts.get(blogId).get());
    }

    @Test
    void countUp_multipleIncrements_accumulatesViewCount() {
        // given
        Long blogId = 1L;

        // when
        viewMemory.countUp(blogId);
        viewMemory.countUp(blogId);
        viewMemory.countUp(blogId);

        // then
        assertEquals(3, viewCounts.get(blogId).get());
    }

    @Test
    void countUp_nullId_doesNotThrowException() {
        // when & then
        assertDoesNotThrow(() -> viewMemory.countUp(null));
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void countUp_multipleBlogIds_incrementsSeparately() {
        // given
        Long blogId1 = 1L;
        Long blogId2 = 2L;

        // when
        viewMemory.countUp(blogId1);
        viewMemory.countUp(blogId1);
        viewMemory.countUp(blogId2);

        // then
        assertEquals(2, viewCounts.get(blogId1).get());
        assertEquals(1, viewCounts.get(blogId2).get());
    }

    @Test
    void flush_withViewCounts_callsRepositoryIncreaseViewCount() {
        // given
        Long blogId = 1L;
        viewMemory.countUp(blogId);
        viewMemory.countUp(blogId);
        viewMemory.countUp(blogId);
        doNothing().when(techBlogRepository)
            .increaseViewCount(any(TechBlogIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(techBlogRepository).increaseViewCount(
            eq(new TechBlogIdentity(blogId)),
            eq(3L)
        );
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void flush_withMultipleBlogs_callsRepositoryForEach() {
        // given
        Long blogId1 = 1L;
        Long blogId2 = 2L;
        viewMemory.countUp(blogId1);
        viewMemory.countUp(blogId1);
        viewMemory.countUp(blogId2);
        doNothing().when(techBlogRepository)
            .increaseViewCount(any(TechBlogIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(techBlogRepository).increaseViewCount(
            eq(new TechBlogIdentity(blogId1)),
            eq(2L)
        );
        verify(techBlogRepository).increaseViewCount(
            eq(new TechBlogIdentity(blogId2)),
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
        verify(techBlogRepository, never()).increaseViewCount(any(), anyLong());
    }

    @Test
    void flush_repositoryThrowsException_continuesProcessing() {
        // given
        Long blogId1 = 1L;
        Long blogId2 = 2L;
        viewMemory.countUp(blogId1);
        viewMemory.countUp(blogId2);

        doThrow(new RuntimeException("DB error"))
            .when(techBlogRepository)
            .increaseViewCount(eq(new TechBlogIdentity(blogId1)), anyLong());

        doNothing().when(techBlogRepository)
            .increaseViewCount(eq(new TechBlogIdentity(blogId2)), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(techBlogRepository).increaseViewCount(
            eq(new TechBlogIdentity(blogId1)),
            eq(1L)
        );
        verify(techBlogRepository).increaseViewCount(
            eq(new TechBlogIdentity(blogId2)),
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
        doNothing().when(techBlogRepository)
            .increaseViewCount(any(TechBlogIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void countUp_afterFlush_startsFromZero() {
        // given
        Long blogId = 1L;
        viewMemory.countUp(blogId);
        viewMemory.countUp(blogId);
        doNothing().when(techBlogRepository)
            .increaseViewCount(any(TechBlogIdentity.class), anyLong());

        // when
        viewMemory.flush();
        viewMemory.countUp(blogId);

        // then
        assertEquals(1, viewCounts.get(blogId).get());
    }
}
