package dev.devrunner.service.communitypost.view;

import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCommunityPostViewMemoryTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @InjectMocks
    private DefaultCommunityPostViewMemory viewMemory;

    private Map<Long, AtomicLong> viewCounts;

    @BeforeEach
    void setUp() throws Exception {
        // Reflection을 사용하여 private viewCounts 필드에 접근
        Field field = DefaultCommunityPostViewMemory.class.getDeclaredField("viewCounts");
        field.setAccessible(true);
        viewCounts = (Map<Long, AtomicLong>) field.get(viewMemory);
        viewCounts.clear();
    }

    @Test
    void countUp_validId_incrementsViewCount() {
        // given
        Long postId = 1L;

        // when
        viewMemory.countUp(postId);

        // then
        assertTrue(viewCounts.containsKey(postId));
        assertEquals(1, viewCounts.get(postId).get());
    }

    @Test
    void countUp_multipleIncrements_accumulatesViewCount() {
        // given
        Long postId = 1L;

        // when
        viewMemory.countUp(postId);
        viewMemory.countUp(postId);
        viewMemory.countUp(postId);

        // then
        assertEquals(3, viewCounts.get(postId).get());
    }

    @Test
    void countUp_nullId_doesNotThrowException() {
        // when & then
        assertDoesNotThrow(() -> viewMemory.countUp(null));
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void countUp_multiplePostIds_incrementsSeparately() {
        // given
        Long postId1 = 1L;
        Long postId2 = 2L;

        // when
        viewMemory.countUp(postId1);
        viewMemory.countUp(postId1);
        viewMemory.countUp(postId2);

        // then
        assertEquals(2, viewCounts.get(postId1).get());
        assertEquals(1, viewCounts.get(postId2).get());
    }

    @Test
    void flush_withViewCounts_callsRepositoryIncreaseViewCount() {
        // given
        Long postId = 1L;
        viewMemory.countUp(postId);
        viewMemory.countUp(postId);
        viewMemory.countUp(postId);
        doNothing().when(communityPostRepository)
            .increaseViewCount(any(CommunityPostIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(communityPostRepository).increaseViewCount(
            eq(new CommunityPostIdentity(postId)),
            eq(3L)
        );
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void flush_withMultiplePosts_callsRepositoryForEach() {
        // given
        Long postId1 = 1L;
        Long postId2 = 2L;
        viewMemory.countUp(postId1);
        viewMemory.countUp(postId1);
        viewMemory.countUp(postId2);
        doNothing().when(communityPostRepository)
            .increaseViewCount(any(CommunityPostIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(communityPostRepository).increaseViewCount(
            eq(new CommunityPostIdentity(postId1)),
            eq(2L)
        );
        verify(communityPostRepository).increaseViewCount(
            eq(new CommunityPostIdentity(postId2)),
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
        verify(communityPostRepository, never()).increaseViewCount(any(), anyLong());
    }

    @Test
    void flush_repositoryThrowsException_continuesProcessing() {
        // given
        Long postId1 = 1L;
        Long postId2 = 2L;
        viewMemory.countUp(postId1);
        viewMemory.countUp(postId2);

        doThrow(new RuntimeException("DB error"))
            .when(communityPostRepository)
            .increaseViewCount(eq(new CommunityPostIdentity(postId1)), anyLong());

        doNothing().when(communityPostRepository)
            .increaseViewCount(eq(new CommunityPostIdentity(postId2)), anyLong());

        // when
        viewMemory.flush();

        // then
        verify(communityPostRepository).increaseViewCount(
            eq(new CommunityPostIdentity(postId1)),
            eq(1L)
        );
        verify(communityPostRepository).increaseViewCount(
            eq(new CommunityPostIdentity(postId2)),
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
        doNothing().when(communityPostRepository)
            .increaseViewCount(any(CommunityPostIdentity.class), anyLong());

        // when
        viewMemory.flush();

        // then
        assertTrue(viewCounts.isEmpty());
    }

    @Test
    void countUp_afterFlush_startsFromZero() {
        // given
        Long postId = 1L;
        viewMemory.countUp(postId);
        viewMemory.countUp(postId);
        doNothing().when(communityPostRepository)
            .increaseViewCount(any(CommunityPostIdentity.class), anyLong());

        // when
        viewMemory.flush();
        viewMemory.countUp(postId);

        // then
        assertEquals(1, viewCounts.get(postId).get());
    }
}
