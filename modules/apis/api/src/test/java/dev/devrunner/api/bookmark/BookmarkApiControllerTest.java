package dev.devrunner.api.bookmark;

import dev.devrunner.api.bookmark.dto.BookmarkCheckResponse;
import dev.devrunner.api.bookmark.dto.BookmarkListResponse;
import dev.devrunner.api.bookmark.dto.BookmarkRequest;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.bookmark.BookmarkReader;
import dev.devrunner.service.bookmark.BookmarkWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * BookmarkApiController 테스트
 *
 * @ExtendWith(MockitoExtension.class) 사용
 * MockMvc 없이 Controller를 직접 호출하여 테스트
 */
@ExtendWith(MockitoExtension.class)
class BookmarkApiControllerTest {

    @Mock
    private BookmarkReader bookmarkReader;

    @Mock
    private BookmarkWriter bookmarkWriter;

    @InjectMocks
    private BookmarkApiController controller;

    // ========== addBookmark 테스트 ==========

    @Test
    void addBookmark_success() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        BookmarkRequest request = createBookmarkRequest(TargetType.JOB, 100L);

        doNothing().when(bookmarkWriter).addBookmark(any(UserIdentity.class), eq(TargetType.JOB), eq(100L));

        // when
        ResponseEntity<Void> response = controller.addBookmark(sessionUser, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(bookmarkWriter).addBookmark(any(UserIdentity.class), eq(TargetType.JOB), eq(100L));
    }

    // ========== removeBookmark 테스트 ==========

    @Test
    void removeBookmark_success() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        BookmarkRequest request = createBookmarkRequest(TargetType.JOB, 100L);

        doNothing().when(bookmarkWriter).removeBookmark(any(UserIdentity.class), eq(TargetType.JOB), eq(100L));

        // when
        ResponseEntity<Void> response = controller.removeBookmark(sessionUser, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(bookmarkWriter).removeBookmark(any(UserIdentity.class), eq(TargetType.JOB), eq(100L));
    }

    // ========== getBookmarks 테스트 ==========

    @Test
    void getBookmarks_withoutTargetType_success() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        int page = 0;
        int size = 20;

        List<Bookmark> bookmarks = List.of(
                createBookmark(1L, 1L, TargetType.JOB, 100L),
                createBookmark(2L, 1L, TargetType.TECH_BLOG, 200L)
        );

        when(bookmarkReader.getByUserId(any(UserIdentity.class), eq(page), eq(size)))
                .thenReturn(bookmarks);
        when(bookmarkReader.countByUserId(any(UserIdentity.class)))
                .thenReturn(2L);

        // when
        ResponseEntity<BookmarkListResponse> response = controller.getBookmarks(sessionUser, page, size, null);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookmarks()).hasSize(2);
        assertThat(response.getBody().getTotalCount()).isEqualTo(2L);
        verify(bookmarkReader).getByUserId(any(UserIdentity.class), eq(page), eq(size));
        verify(bookmarkReader).countByUserId(any(UserIdentity.class));
    }

    @Test
    void getBookmarks_withTargetType_success() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        int page = 0;
        int size = 20;
        TargetType targetType = TargetType.JOB;

        List<Bookmark> bookmarks = List.of(
                createBookmark(1L, 1L, TargetType.JOB, 100L),
                createBookmark(3L, 1L, TargetType.JOB, 101L)
        );

        when(bookmarkReader.getByUserIdAndTargetType(any(UserIdentity.class), eq(targetType), eq(page), eq(size)))
                .thenReturn(bookmarks);
        when(bookmarkReader.countByUserId(any(UserIdentity.class)))
                .thenReturn(2L);

        // when
        ResponseEntity<BookmarkListResponse> response = controller.getBookmarks(sessionUser, page, size, targetType);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookmarks()).hasSize(2);
        assertThat(response.getBody().getTotalCount()).isEqualTo(2L);
        verify(bookmarkReader).getByUserIdAndTargetType(any(UserIdentity.class), eq(targetType), eq(page), eq(size));
        verify(bookmarkReader).countByUserId(any(UserIdentity.class));
    }

    // ========== checkBookmark 테스트 ==========

    @Test
    void checkBookmark_bookmarked_returnsTrue() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;

        Bookmark bookmark = createBookmark(1L, 1L, targetType, targetId);

        when(bookmarkReader.findByUserIdAndTargetTypeAndTargetId(any(UserIdentity.class), eq(targetType), eq(targetId)))
                .thenReturn(Optional.of(bookmark));

        // when
        ResponseEntity<BookmarkCheckResponse> response = controller.checkBookmark(sessionUser, targetType, targetId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isBookmarked()).isTrue();
        assertThat(response.getBody().getBookmarkId()).isEqualTo(1L);
        verify(bookmarkReader).findByUserIdAndTargetTypeAndTargetId(any(UserIdentity.class), eq(targetType), eq(targetId));
    }

    @Test
    void checkBookmark_notBookmarked_returnsFalse() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        TargetType targetType = TargetType.JOB;
        Long targetId = 100L;

        when(bookmarkReader.findByUserIdAndTargetTypeAndTargetId(any(UserIdentity.class), eq(targetType), eq(targetId)))
                .thenReturn(Optional.empty());

        // when
        ResponseEntity<BookmarkCheckResponse> response = controller.checkBookmark(sessionUser, targetType, targetId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isBookmarked()).isFalse();
        assertThat(response.getBody().getBookmarkId()).isNull();
        verify(bookmarkReader).findByUserIdAndTargetTypeAndTargetId(any(UserIdentity.class), eq(targetType), eq(targetId));
    }

    // ========== 헬퍼 메서드 ==========

    private BookmarkRequest createBookmarkRequest(TargetType targetType, Long targetId) {
        BookmarkRequest request = new BookmarkRequest();
        // Reflection을 사용하여 private 필드 설정
        try {
            var targetTypeField = BookmarkRequest.class.getDeclaredField("targetType");
            targetTypeField.setAccessible(true);
            targetTypeField.set(request, targetType);

            var targetIdField = BookmarkRequest.class.getDeclaredField("targetId");
            targetIdField.setAccessible(true);
            targetIdField.set(request, targetId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    private Bookmark createBookmark(Long bookmarkId, Long userId, TargetType targetType, Long targetId) {
        return new Bookmark(
                bookmarkId,
                userId,
                targetType,
                targetId,
                Instant.now(),
                Instant.now()
        );
    }
}
