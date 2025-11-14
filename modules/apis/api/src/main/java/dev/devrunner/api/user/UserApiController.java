package dev.devrunner.api.user;

import dev.devrunner.api.user.dto.*;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.activityLog.BookmarkActivityLog;
import dev.devrunner.model.activityLog.CommentActivityLog;
import dev.devrunner.model.activityLog.LikeActivityLog;
import dev.devrunner.model.activityLog.PostActivityLog;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.user.UserActivityLogReader;
import dev.devrunner.service.user.UserReader;
import dev.devrunner.service.user.UserWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User REST API 컨트롤러
 * <p>
 * 사용자 활동 통계 및 이력 조회 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User API")
public class UserApiController {

    private final UserReader userReader;
    private final UserActivityLogReader userActivityLogReader;
    private final UserWriter userWriter;

    /**
     * 내 활동 통계 조회
     * GET /api/users/{userId}/metrics
     */
    @Operation(summary = "Get user activity metrics", description = "Retrieve activity statistics for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/metrics")
    public ResponseEntity<UserMetricsResponse> getUserMetrics(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long userId) {

        log.info("GET /api/users/{}/metrics", userId);

        UserIdentity userIdentity = new UserIdentity(userId);

        // User 기본 정보 조회
        User user = userReader.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // User 모델에서 모든 메트릭 직접 조회 (실시간 집계 제거)
        UserMetricsResponse response = new UserMetricsResponse(
                user.getUserId(),
                user.getNickname(),
                user.getPostCount(),
                user.getCommentCount(),
                user.getLikeGivenCount(),
                user.getLikesReceived(),
                user.getBookmarkCount(),
                user.getCreatedAt()
        );

        log.info("User metrics retrieved - userId: {}, postCount: {}, commentCount: {}, likeGivenCount: {}, bookmarkCount: {}",
                userId, user.getPostCount(), user.getCommentCount(), user.getLikeGivenCount(), user.getBookmarkCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 활동 로그 조회
     * GET /api/users/{userId}/activity-logs/comments?page=0&size=20
     */
    @Operation(summary = "Get user's comment activity logs", description = "Retrieve comment activity logs with target article info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/activity-logs/comments")
    public ResponseEntity<UserCommentActivityLogsResponse> getCommentActivityLogs(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/users/{}/activity-logs/comments - page: {}, size: {}", userId, page, size);

        UserIdentity userIdentity = new UserIdentity(userId);

        userReader.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CommentActivityLog> logs = userActivityLogReader.getCommentActivityLogs(userIdentity, page, size);

        List<CommentActivityLogResponse> responses = logs.stream()
                .map(CommentActivityLogResponse::from)
                .collect(Collectors.toList());

        log.info("Retrieved {} comment activity logs for userId: {}", responses.size(), userId);
        return ResponseEntity.ok(new UserCommentActivityLogsResponse(responses, page, size));
    }

    /**
     * 좋아요 활동 로그 조회
     * GET /api/users/{userId}/activity-logs/likes?page=0&size=20
     */
    @Operation(summary = "Get user's like activity logs", description = "Retrieve like activity logs with target article info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/activity-logs/likes")
    public ResponseEntity<UserLikeActivityLogsResponse> getLikeActivityLogs(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/users/{}/activity-logs/likes - page: {}, size: {}", userId, page, size);

        UserIdentity userIdentity = new UserIdentity(userId);

        userReader.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LikeActivityLog> logs = userActivityLogReader.getLikeActivityLogs(userIdentity, page, size);

        List<LikeActivityLogResponse> responses = logs.stream()
                .map(LikeActivityLogResponse::from)
                .collect(Collectors.toList());

        log.info("Retrieved {} like activity logs for userId: {}", responses.size(), userId);
        return ResponseEntity.ok(new UserLikeActivityLogsResponse(responses, page, size));
    }

    /**
     * 북마크 활동 로그 조회
     * GET /api/users/{userId}/activity-logs/bookmarks?page=0&size=20
     */
    @Operation(summary = "Get user's bookmark activity logs", description = "Retrieve bookmark activity logs with target article info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/activity-logs/bookmarks")
    public ResponseEntity<UserBookmarkActivityLogsResponse> getBookmarkActivityLogs(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/users/{}/activity-logs/bookmarks - page: {}, size: {}", userId, page, size);

        UserIdentity userIdentity = new UserIdentity(userId);

        userReader.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BookmarkActivityLog> logs = userActivityLogReader.getBookmarkActivityLogs(userIdentity, page, size);

        List<BookmarkActivityLogResponse> responses = logs.stream()
                .map(BookmarkActivityLogResponse::from)
                .collect(Collectors.toList());

        log.info("Retrieved {} bookmark activity logs for userId: {}", responses.size(), userId);
        return ResponseEntity.ok(new UserBookmarkActivityLogsResponse(responses, page, size));
    }

    /**
     * 글 작성 활동 로그 조회
     * GET /api/users/{userId}/activity-logs/posts?page=0&size=20
     */
    @Operation(summary = "Get user's post activity logs", description = "Retrieve post activity logs with popularity metrics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/activity-logs/posts")
    public ResponseEntity<UserPostActivityLogsResponse> getPostActivityLogs(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/users/{}/activity-logs/posts - page: {}, size: {}", userId, page, size);

        UserIdentity userIdentity = new UserIdentity(userId);

        userReader.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PostActivityLog> logs = userActivityLogReader.getPostActivityLogs(userIdentity, page, size);

        List<PostActivityLogResponse> responses = logs.stream()
                .map(PostActivityLogResponse::from)
                .collect(Collectors.toList());

        log.info("Retrieved {} post activity logs for userId: {}", responses.size(), userId);
        return ResponseEntity.ok(new UserPostActivityLogsResponse(responses, page, size));
    }

    /**
     * 회원 탈퇴
     * DELETE /api/users/me
     */
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자 계정을 탈퇴합니다 (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully withdrawn"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Not logged in"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawUser(
            @AuthenticationPrincipal SessionUser sessionUser) {

        log.info("DELETE /api/users/me - Withdraw request by userId={}", sessionUser.getUserId());

        if (sessionUser == null || sessionUser.getUserId() == null) {
            throw new RuntimeException("User not authenticated");
        }

        UserIdentity userIdentity = new UserIdentity(sessionUser.getUserId());

        // 회원 탈퇴 처리
        userWriter.withdraw(userIdentity);

        log.info("User withdrawn successfully: userId={}", sessionUser.getUserId());

        return ResponseEntity.noContent().build();
    }
}
