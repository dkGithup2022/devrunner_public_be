package dev.devrunner.api.bookmark;

import dev.devrunner.api.bookmark.dto.BookmarkCheckResponse;
import dev.devrunner.api.bookmark.dto.BookmarkListResponse;
import dev.devrunner.api.bookmark.dto.BookmarkRequest;
import dev.devrunner.api.bookmark.dto.BookmarkResponse;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.bookmark.BookmarkReader;
import dev.devrunner.service.bookmark.BookmarkWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.devrunner.api.LoginSessionUtils.readUserIdOrThrow;

/**
 * Bookmark REST API 컨트롤러
 * <p>
 * 북마크 추가/삭제/조회 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bookmark", description = "Bookmark API")
public class BookmarkApiController {

    private final BookmarkReader bookmarkReader;
    private final BookmarkWriter bookmarkWriter;

    /**
     * 북마크 추가
     * POST /api/bookmarks
     */
    @Operation(summary = "Add bookmark", description = "Add a bookmark to a target")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully created"),
            @ApiResponse(responseCode = "401", description = "login required"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Already bookmarked")
    })
    @PostMapping
    public ResponseEntity<Void> addBookmark(
            @AuthenticationPrincipal SessionUser sessionUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Bookmark request", required = true)
            @Valid @RequestBody BookmarkRequest request) {

        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);

        log.info("POST /api/bookmarks - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());

        bookmarkWriter.addBookmark(userIdentity, request.getTargetType(), request.getTargetId());

        log.info("Bookmark added - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 북마크 삭제
     * DELETE /api/bookmarks
     */
    @Operation(summary = "Remove bookmark", description = "Remove a bookmark from a target")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully removed"),
            @ApiResponse(responseCode = "401", description = "login required"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Bookmark not found")
    })
    @DeleteMapping
    public ResponseEntity<Void> removeBookmark(
            @AuthenticationPrincipal SessionUser sessionUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Bookmark request", required = true)
            @Valid @RequestBody BookmarkRequest request) {

        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);

        log.info("DELETE /api/bookmarks - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());

        bookmarkWriter.removeBookmark(userIdentity, request.getTargetType(), request.getTargetId());

        log.info("Bookmark removed - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 북마크 목록 조회
     * GET /api/bookmarks?page=0&size=20&targetType=JOB
     */
    @Operation(summary = "Get my bookmarks", description = "Retrieve bookmarks for the current user with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "login required"),
    })
    @GetMapping
    public ResponseEntity<BookmarkListResponse> getBookmarks(
            @AuthenticationPrincipal SessionUser sessionUser,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by target type (optional)")
            @RequestParam(required = false) TargetType targetType) {

        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);

        log.info("GET /api/bookmarks - userId: {}, page: {}, size: {}, targetType: {}",
                userId, page, size, targetType);

        // 북마크 목록 조회 (필터링 여부에 따라 분기)
        List<Bookmark> bookmarks = (targetType == null)
                ? bookmarkReader.getByUserId(userIdentity, page, size)
                : bookmarkReader.getByUserIdAndTargetType(userIdentity, targetType, page, size);

        // 총 개수 조회
        long totalCount = bookmarkReader.countByUserId(userIdentity);

        // Response DTO 생성
        List<BookmarkResponse> responses = bookmarks.stream()
                .map(BookmarkResponse::from)
                .collect(Collectors.toList());

        BookmarkListResponse response = new BookmarkListResponse(responses, totalCount, page, size);

        log.info("Retrieved {} bookmarks for userId: {}", response.getBookmarks().size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 아이템 북마크 여부 확인
     * GET /api/bookmarks/check?targetType=JOB&targetId=100
     */
    @Operation(summary = "Check bookmark status", description = "Check if a target is bookmarked by the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully checked"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "login required"),
    })
    @GetMapping("/check")
    public ResponseEntity<BookmarkCheckResponse> checkBookmark(
            @AuthenticationPrincipal SessionUser sessionUser,
            @Parameter(description = "Target type", example = "JOB", required = true)
            @RequestParam TargetType targetType,
            @Parameter(description = "Target ID", example = "100", required = true)
            @RequestParam Long targetId) {

        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);

        log.info("GET /api/bookmarks/check - userId: {}, targetType: {}, targetId: {}",
                userId, targetType, targetId);

        Optional<Bookmark> bookmark = bookmarkReader.findByUserIdAndTargetTypeAndTargetId(
                userIdentity, targetType, targetId);

        BookmarkCheckResponse response = bookmark
                .map(b -> BookmarkCheckResponse.bookmarked(b.getBookmarkId()))
                .orElseGet(BookmarkCheckResponse::notBookmarked);

        log.info("Bookmark check result - userId: {}, isBookmarked: {}", userId, response.isBookmarked());
        return ResponseEntity.ok(response);
    }
}
