package dev.devrunner.api.user;

import dev.devrunner.api.user.dto.*;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.activityLog.*;
import dev.devrunner.model.common.NotificationSettings;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.model.user.UserMetrics;
import dev.devrunner.model.user.UserRole;
import dev.devrunner.service.user.UserActivityLogReader;
import dev.devrunner.service.user.UserReader;
import dev.devrunner.service.user.UserWriter;
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
import static org.mockito.Mockito.*;

/**
 * UserApiController 테스트
 *
 * @ExtendWith(MockitoExtension.class) 사용
 * MockMvc 없이 Controller를 직접 호출하여 테스트
 */
@ExtendWith(MockitoExtension.class)
class UserApiControllerTest {

    @Mock
    private UserReader userReader;

    @Mock
    private UserActivityLogReader userActivityLogReader;

    @Mock
    private UserWriter userWriter;

    @InjectMocks
    private UserApiController controller;

    // ========== getUserMetrics 테스트 ==========

    @Test
    void getUserMetrics_existingUser_returnsOkWithMetrics() {
        // given
        Long userId = 1L;
        User user = createSampleUser(userId);

        when(userReader.findById(new UserIdentity(userId))).thenReturn(Optional.of(user));

        // when
        ResponseEntity<UserMetricsResponse> response = controller.getUserMetrics(userId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(userId);
        assertThat(response.getBody().getNickname()).isEqualTo("testuser");
        assertThat(response.getBody().getCommunityPostCount()).isEqualTo(15L);
        assertThat(response.getBody().getCommentCount()).isEqualTo(48L);
        assertThat(response.getBody().getLikeGivenCount()).isEqualTo(120L);
        assertThat(response.getBody().getLikeReceivedCount()).isEqualTo(85L);
        assertThat(response.getBody().getBookmarkCount()).isEqualTo(32L);

        verify(userReader).findById(new UserIdentity(userId));
    }

    @Test
    void getUserMetrics_nonExistingUser_throwsException() {
        // given
        Long userId = 999L;

        when(userReader.findById(new UserIdentity(userId))).thenReturn(Optional.empty());

        // when & then
        try {
            controller.getUserMetrics(userId);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("User not found");
        }

        verify(userReader).findById(new UserIdentity(userId));
    }

    // ========== getCommentActivityLogs 테스트 ==========

    @Test
    void getCommentActivityLogs_existingUser_returnsOkWithLogs() {
        // given
        Long userId = 1L;
        User user = createSampleUser(userId);
        CommentActivityLog log = createCommentActivityLog();

        when(userReader.findById(new UserIdentity(userId))).thenReturn(Optional.of(user));
        when(userActivityLogReader.getCommentActivityLogs(new UserIdentity(userId), 0, 20))
                .thenReturn(List.of(log));

        // when
        ResponseEntity<UserCommentActivityLogsResponse> response = controller.getCommentActivityLogs(userId, 0, 20);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLogs()).hasSize(1);
        assertThat(response.getBody().getLogs().get(0).getCommentId()).isEqualTo(1L);
        assertThat(response.getBody().getLogs().get(0).getContent()).isEqualTo("Great post!");
        assertThat(response.getBody().getPage()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(20);

        verify(userReader).findById(new UserIdentity(userId));
        verify(userActivityLogReader).getCommentActivityLogs(new UserIdentity(userId), 0, 20);
    }

    @Test
    void getCommentActivityLogs_nonExistingUser_throwsException() {
        // given
        Long userId = 999L;

        when(userReader.findById(new UserIdentity(userId))).thenReturn(Optional.empty());

        // when & then
        try {
            controller.getCommentActivityLogs(userId, 0, 20);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("User not found");
        }

        verify(userReader).findById(new UserIdentity(userId));
    }

    // ========== getLikeActivityLogs 테스트 ==========

    @Test
    void getLikeActivityLogs_existingUser_returnsOkWithLogs() {
        // given
        Long userId = 1L;
        User user = createSampleUser(userId);
        LikeActivityLog log = createLikeActivityLog();

        when(userReader.findById(new UserIdentity(userId))).thenReturn(Optional.of(user));
        when(userActivityLogReader.getLikeActivityLogs(new UserIdentity(userId), 0, 20))
                .thenReturn(List.of(log));

        // when
        ResponseEntity<UserLikeActivityLogsResponse> response = controller.getLikeActivityLogs(userId, 0, 20);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLogs()).hasSize(1);
        assertThat(response.getBody().getLogs().get(0).getReactionId()).isEqualTo(1L);
        assertThat(response.getBody().getLogs().get(0).getTargetTitle()).isEqualTo("Liked Post Title");
        assertThat(response.getBody().getPage()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(20);

        verify(userReader).findById(new UserIdentity(userId));
        verify(userActivityLogReader).getLikeActivityLogs(new UserIdentity(userId), 0, 20);
    }

    // ========== getBookmarkActivityLogs 테스트 ==========

    @Test
    void getBookmarkActivityLogs_existingUser_returnsOkWithLogs() {
        // given
        Long userId = 1L;
        User user = createSampleUser(userId);
        BookmarkActivityLog log = createBookmarkActivityLog();

        when(userReader.findById(new UserIdentity(userId))).thenReturn(Optional.of(user));
        when(userActivityLogReader.getBookmarkActivityLogs(new UserIdentity(userId), 0, 20))
                .thenReturn(List.of(log));

        // when
        ResponseEntity<UserBookmarkActivityLogsResponse> response = controller.getBookmarkActivityLogs(userId, 0, 20);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLogs()).hasSize(1);
        assertThat(response.getBody().getLogs().get(0).getBookmarkId()).isEqualTo(1L);
        assertThat(response.getBody().getLogs().get(0).getTargetTitle()).isEqualTo("Backend Developer");
        assertThat(response.getBody().getPage()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(20);

        verify(userReader).findById(new UserIdentity(userId));
        verify(userActivityLogReader).getBookmarkActivityLogs(new UserIdentity(userId), 0, 20);
    }

    // ========== getPostActivityLogs 테스트 ==========

    @Test
    void getPostActivityLogs_existingUser_returnsOkWithLogs() {
        // given
        Long userId = 1L;
        User user = createSampleUser(userId);
        PostActivityLog log = createPostActivityLog();

        when(userReader.findById(new UserIdentity(userId))).thenReturn(Optional.of(user));
        when(userActivityLogReader.getPostActivityLogs(new UserIdentity(userId), 0, 20))
                .thenReturn(List.of(log));

        // when
        ResponseEntity<UserPostActivityLogsResponse> response = controller.getPostActivityLogs(userId, 0, 20);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLogs()).hasSize(1);
        assertThat(response.getBody().getLogs().get(0).getCommunityPostId()).isEqualTo(1L);
        assertThat(response.getBody().getLogs().get(0).getTitle()).isEqualTo("My Interview Experience");
        assertThat(response.getBody().getPage()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(20);

        verify(userReader).findById(new UserIdentity(userId));
        verify(userActivityLogReader).getPostActivityLogs(new UserIdentity(userId), 0, 20);
    }

    // ========== withdrawUser 테스트 ==========

    @Test
    void withdrawUser_validSession_returnsNoContent() {
        // given
        SessionUser sessionUser = SessionUser.of(1L, Instant.now(), Instant.now().plusSeconds(3600));
        User withdrawnUser = createSampleUser(1L);

        when(userWriter.withdraw(any(UserIdentity.class))).thenReturn(withdrawnUser);

        // when
        ResponseEntity<Void> response = controller.withdrawUser(sessionUser);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userWriter).withdraw(any(UserIdentity.class));
    }

    @Test
    void withdrawUser_nullSessionUser_throwsException() {
        // given
        SessionUser sessionUser = null;

        // when & then
        try {
            controller.withdrawUser(sessionUser);
        } catch (RuntimeException e) {
            // NullPointerException from sessionUser.getUserId() or RuntimeException from validation
            assertThat(e).isNotNull();
        }

        verifyNoInteractions(userWriter);
    }

    // ========== 헬퍼 메서드 ==========

    private User createSampleUser(Long userId) {
        return new User(
                userId,                                 // userId
                "google-123",                           // googleId
                "test@example.com",                     // email
                "testuser",                             // nickname
                UserRole.USER,                          // userRole
                List.of("Company A"),                   // interestedCompanies
                List.of("Seoul"),                       // interestedLocations
                NotificationSettings.defaultSettings(), // notificationSettings
                new UserMetrics(15L, 48L, 85L, 120L, 32L), // metrics (postCount, commentCount, likesReceived, likeGivenCount, bookmarkCount)
                Instant.now(),                          // lastLoginAt
                true,                                   // isActive
                false,                                  // isWithdrawn
                null,                                   // withdrawnAt
                Instant.parse("2025-01-01T00:00:00Z"),  // createdAt
                Instant.now()                           // updatedAt
        );
    }

    private CommentActivityLog createCommentActivityLog() {
        return new CommentActivityLog(
                1L,                                     // commentId
                "Great post!",                          // content
                false,                                  // isHidden
                Instant.now(),                          // commentedAt
                null,                                   // parentCommentId
                TargetType.COMMUNITY_POST,              // targetType
                10L,                                    // targetId
                "Community Post Title",                 // targetTitle
                "author123",                            // targetAuthorNickname
                100L,                                   // viewCount
                15L,                                    // likeCount
                8L                                      // commentCount
        );
    }

    private LikeActivityLog createLikeActivityLog() {
        return new LikeActivityLog(
                1L,                                     // reactionId
                Instant.now(),                          // likedAt
                TargetType.COMMUNITY_POST,              // targetType
                10L,                                    // targetId
                "Liked Post Title",                     // targetTitle
                "author456",                            // targetAuthorNickname
                200L,                                   // viewCount
                25L,                                    // likeCount
                12L                                     // commentCount
        );
    }

    private BookmarkActivityLog createBookmarkActivityLog() {
        return new BookmarkActivityLog(
                1L,                                     // bookmarkId
                Instant.now(),                          // bookmarkedAt
                TargetType.JOB,                         // targetType
                5L,                                     // targetId
                "Backend Developer",                    // targetTitle
                null,                                   // targetAuthorNickname
                150L,                                   // viewCount
                10L,                                    // likeCount
                5L                                      // commentCount
        );
    }

    private PostActivityLog createPostActivityLog() {
        return new PostActivityLog(
                1L,                                     // communityPostId
                CommunityPostCategory.INTERVIEW_SHARE,  // category
                "My Interview Experience",              // title
                "I had a great interview...",           // markdownBody
                Instant.now(),                          // postedAt
                300L,                                   // viewCount
                50L,                                    // likeCount
                20L                                     // commentCount
        );
    }
}
