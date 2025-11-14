package dev.devrunner.service.bookmark.impl;

import dev.devrunner.exception.bookmark.BookmarkNotFoundException;
import dev.devrunner.exception.bookmark.DuplicateBookmarkException;
import dev.devrunner.infra.bookmark.repository.BookmarkRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.bookmark.BookmarkIdentity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.bookmark.BookmarkReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultBookmarkWriterTest {

    @Mock
    private BookmarkReader bookmarkReader;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DefaultBookmarkWriter bookmarkWriter;

    private final Long userId = 1L;
    private final TargetType targetType = TargetType.JOB;
    private final Long targetId = 100L;

    private final Bookmark sampleBookmark = new Bookmark(
        1L,                  // bookmarkId
        userId,              // userId
        targetType,          // targetType
        targetId,            // targetId
        Instant.now(),       // createdAt
        Instant.now()        // updatedAt
    );

    // ========== addBookmark 테스트 ==========

    @Test
    void addBookmark_noExistingBookmark_createsNewBookmarkAndIncrementsBookmarkCount() {
        // given
        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.incrementBookmarkCount();

        when(bookmarkReader.findByUserIdAndTargetTypeAndTargetId(
            any(UserIdentity.class), eq(targetType), eq(targetId)))
            .thenReturn(Optional.empty());
        when(bookmarkRepository.save(any(Bookmark.class)))
            .thenReturn(sampleBookmark);
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);

        // when
        bookmarkWriter.addBookmark(new UserIdentity(userId), targetType, targetId);

        // then
        verify(bookmarkReader).findByUserIdAndTargetTypeAndTargetId(
            any(UserIdentity.class), eq(targetType), eq(targetId));
        verify(bookmarkRepository).save(any(Bookmark.class));
        verify(userRepository).findById(new UserIdentity(userId));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addBookmark_existingBookmark_throwsDuplicateBookmarkException() {
        // given
        when(bookmarkReader.findByUserIdAndTargetTypeAndTargetId(
            any(UserIdentity.class), eq(targetType), eq(targetId)))
            .thenReturn(Optional.of(sampleBookmark));

        // when & then
        assertThrows(DuplicateBookmarkException.class, () ->
            bookmarkWriter.addBookmark(new UserIdentity(userId), targetType, targetId)
        );

        verify(bookmarkReader).findByUserIdAndTargetTypeAndTargetId(
            any(UserIdentity.class), eq(targetType), eq(targetId));
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== removeBookmark 테스트 ==========

    @Test
    void removeBookmark_existingBookmark_deletesBookmarkAndDecrementsBookmarkCount() {
        // given
        User mockUser = User.newUser("google123", "test@example.com", "testuser");
        User updatedUser = mockUser.decrementBookmarkCount();

        when(bookmarkReader.findByUserIdAndTargetTypeAndTargetId(
            any(UserIdentity.class), eq(targetType), eq(targetId)))
            .thenReturn(Optional.of(sampleBookmark));
        doNothing().when(bookmarkRepository).deleteById(any(BookmarkIdentity.class));
        when(userRepository.findById(any(UserIdentity.class)))
            .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);

        // when
        bookmarkWriter.removeBookmark(new UserIdentity(userId), targetType, targetId);

        // then
        verify(bookmarkReader).findByUserIdAndTargetTypeAndTargetId(
            any(UserIdentity.class), eq(targetType), eq(targetId));
        verify(bookmarkRepository).deleteById(new BookmarkIdentity(sampleBookmark.getBookmarkId()));
        verify(userRepository).findById(new UserIdentity(userId));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void removeBookmark_noExistingBookmark_throwsBookmarkNotFoundException() {
        // given
        when(bookmarkReader.findByUserIdAndTargetTypeAndTargetId(
            any(UserIdentity.class), eq(targetType), eq(targetId)))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(BookmarkNotFoundException.class, () ->
            bookmarkWriter.removeBookmark(new UserIdentity(userId), targetType, targetId)
        );

        verify(bookmarkReader).findByUserIdAndTargetTypeAndTargetId(
            any(UserIdentity.class), eq(targetType), eq(targetId));
        verify(bookmarkRepository, never()).deleteById(any(BookmarkIdentity.class));
        verify(userRepository, never()).findById(any(UserIdentity.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
