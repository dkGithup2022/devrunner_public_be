package dev.devrunner.service.bookmark.impl;

import dev.devrunner.exception.bookmark.BookmarkNotFoundException;
import dev.devrunner.infra.bookmark.repository.BookmarkRepository;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.bookmark.BookmarkIdentity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
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
class DefaultBookmarkReaderTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private DefaultBookmarkReader bookmarkReader;

    private final Bookmark sampleBookmark = new Bookmark(
        1L,                  // bookmarkId
        1L,                  // userId
        TargetType.JOB,      // targetType
        100L,                // targetId
        Instant.now(),       // createdAt
        Instant.now()        // updatedAt
    );

    private final BookmarkIdentity testIdentity = new BookmarkIdentity(1L);
    private final UserIdentity testUserIdentity = new UserIdentity(1L);

    // ========== getById 테스트 ==========

    @Test
    void getById_existingId_returnsBookmark() {
        // given
        when(bookmarkRepository.findById(testIdentity))
            .thenReturn(Optional.of(sampleBookmark));

        // when
        Bookmark result = bookmarkReader.getById(testIdentity);

        // then
        assertNotNull(result);
        assertEquals(sampleBookmark.getBookmarkId(), result.getBookmarkId());
        assertEquals(sampleBookmark.getTargetType(), result.getTargetType());
        verify(bookmarkRepository).findById(testIdentity);
    }

    @Test
    void getById_nonExistingId_throwsBookmarkNotFoundException() {
        // given
        when(bookmarkRepository.findById(testIdentity))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(BookmarkNotFoundException.class, () ->
            bookmarkReader.getById(testIdentity)
        );
        verify(bookmarkRepository).findById(testIdentity);
    }

    // ========== getByUserId 테스트 ==========

    @Test
    void getByUserId_existingUser_returnsList() {
        // given
        int page = 0;
        int size = 20;
        List<Bookmark> bookmarks = List.of(sampleBookmark);
        when(bookmarkRepository.findByUserId(testUserIdentity, page, size))
            .thenReturn(bookmarks);

        // when
        List<Bookmark> result = bookmarkReader.getByUserId(testUserIdentity, page, size);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserIdentity.getUserId(), result.get(0).getUserId());
        verify(bookmarkRepository).findByUserId(testUserIdentity, page, size);
    }

    @Test
    void getByUserId_nonExistingUser_returnsEmptyList() {
        // given
        UserIdentity nonExistingUser = new UserIdentity(999L);
        int page = 0;
        int size = 20;
        when(bookmarkRepository.findByUserId(nonExistingUser, page, size))
            .thenReturn(List.of());

        // when
        List<Bookmark> result = bookmarkReader.getByUserId(nonExistingUser, page, size);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookmarkRepository).findByUserId(nonExistingUser, page, size);
    }

    // ========== getByUserIdAndTargetType 테스트 ==========

    @Test
    void getByUserIdAndTargetType_existingData_returnsList() {
        // given
        TargetType targetType = TargetType.JOB;
        int page = 0;
        int size = 20;
        List<Bookmark> bookmarks = List.of(sampleBookmark);
        when(bookmarkRepository.findByUserIdAndTargetType(testUserIdentity, targetType, page, size))
            .thenReturn(bookmarks);

        // when
        List<Bookmark> result = bookmarkReader.getByUserIdAndTargetType(testUserIdentity, targetType, page, size);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(targetType, result.get(0).getTargetType());
        verify(bookmarkRepository).findByUserIdAndTargetType(testUserIdentity, targetType, page, size);
    }

    @Test
    void getByUserIdAndTargetType_nonExistingData_returnsEmptyList() {
        // given
        TargetType targetType = TargetType.TECH_BLOG;
        int page = 0;
        int size = 20;
        when(bookmarkRepository.findByUserIdAndTargetType(testUserIdentity, targetType, page, size))
            .thenReturn(List.of());

        // when
        List<Bookmark> result = bookmarkReader.getByUserIdAndTargetType(testUserIdentity, targetType, page, size);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookmarkRepository).findByUserIdAndTargetType(testUserIdentity, targetType, page, size);
    }

    // ========== countByUserId 테스트 ==========

    @Test
    void countByUserId_existingUser_returnsCount() {
        // given
        long expectedCount = 5L;
        when(bookmarkRepository.countByUserId(testUserIdentity))
            .thenReturn(expectedCount);

        // when
        long result = bookmarkReader.countByUserId(testUserIdentity);

        // then
        assertEquals(expectedCount, result);
        verify(bookmarkRepository).countByUserId(testUserIdentity);
    }

    @Test
    void countByUserId_nonExistingUser_returnsZero() {
        // given
        UserIdentity nonExistingUser = new UserIdentity(999L);
        when(bookmarkRepository.countByUserId(nonExistingUser))
            .thenReturn(0L);

        // when
        long result = bookmarkReader.countByUserId(nonExistingUser);

        // then
        assertEquals(0L, result);
        verify(bookmarkRepository).countByUserId(nonExistingUser);
    }

    // ========== findByUserIdAndTargetTypeAndTargetId 테스트 ==========

    @Test
    void findByUserIdAndTargetTypeAndTargetId_existingBookmark_returnsOptionalWithBookmark() {
        // given
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;
        when(bookmarkRepository.findByUserIdAndTargetTypeAndTargetId(testUserIdentity, targetType, targetId))
            .thenReturn(Optional.of(sampleBookmark));

        // when
        Optional<Bookmark> result = bookmarkReader.findByUserIdAndTargetTypeAndTargetId(testUserIdentity, targetType, targetId);

        // then
        assertTrue(result.isPresent());
        assertEquals(testUserIdentity.getUserId(), result.get().getUserId());
        assertEquals(targetType, result.get().getTargetType());
        assertEquals(targetId, result.get().getTargetId());
        verify(bookmarkRepository).findByUserIdAndTargetTypeAndTargetId(testUserIdentity, targetType, targetId);
    }

    @Test
    void findByUserIdAndTargetTypeAndTargetId_nonExistingBookmark_returnsEmptyOptional() {
        // given
        UserIdentity nonExistingUser = new UserIdentity(999L);
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;
        when(bookmarkRepository.findByUserIdAndTargetTypeAndTargetId(nonExistingUser, targetType, targetId))
            .thenReturn(Optional.empty());

        // when
        Optional<Bookmark> result = bookmarkReader.findByUserIdAndTargetTypeAndTargetId(nonExistingUser, targetType, targetId);

        // then
        assertFalse(result.isPresent());
        verify(bookmarkRepository).findByUserIdAndTargetTypeAndTargetId(nonExistingUser, targetType, targetId);
    }
}
