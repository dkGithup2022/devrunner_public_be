package dev.devrunner.api.comment;

import dev.devrunner.api.comment.dto.CommentResponse;
import dev.devrunner.api.comment.dto.CommentWriteRequest;
import dev.devrunner.api.comment.dto.CommentUpdateRequest;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.exception.auth.UnauthorizedException;
import dev.devrunner.model.comment.Comment;
import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.comment.CommentReader;
import dev.devrunner.service.comment.CommentWriter;
import dev.devrunner.service.comment.dto.CommentWriteCommand;

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
import java.util.stream.Collectors;

import static dev.devrunner.api.LoginSessionUtils.readUserIdOrThrow;

/**
 * Comment REST API 컨트롤러
 * <p>
 * Reader/Writer 패턴을 활용하여 CQRS 기반 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comment", description = "Comment API")
public class CommentApiController {

    private final CommentReader commentReader;
    private final CommentWriter commentWriter;

    /**
     * 대상별 댓글 목록 조회
     * GET /api/comments?targetType={targetType}&targetId={targetId}
     */
    @Operation(summary = "Get comments by target", description = "Retrieve comments for a specific target (Job, TechBlog, etc.)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByTarget(
            @Parameter(description = "Target type", example = "JOB") @RequestParam TargetType targetType,
            @Parameter(description = "Target ID", example = "100") @RequestParam Long targetId
    ) {
        log.info("GET /api/comments - targetType: {}, targetId: {}", targetType, targetId);

        List<CommentRead> comments = commentReader.getByTargetTypeAndTargetId(targetType, targetId);
        List<CommentResponse> responses = comments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        log.info("Retrieved {} comments for {} {}", responses.size(), targetType, targetId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 댓글 작성
     * POST /api/comments
     */
    @Operation(summary = "Write comment", description = "Create a new comment or reply")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<CommentResponse> writeComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Comment write request", required = true)
            @Valid @RequestBody CommentWriteRequest request, @AuthenticationPrincipal SessionUser sessionUser) {

        var userId = readUserIdOrThrow(sessionUser);

        log.info("POST /api/comments - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());

        CommentWriteCommand command = new CommentWriteCommand(
                userId,
                request.getContent(),
                request.getTargetType(),
                request.getTargetId(),
                request.getParentId()
        );

        Comment comment = commentWriter.write(command);

        // Re-fetch with user information for response
        CommentRead commentRead = commentReader.getById(new CommentIdentity(comment.getCommentId()));
        CommentResponse response = CommentResponse.from(commentRead);

        log.info("Comment created - commentId: {}", response.getCommentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 댓글 수정
     * PUT /api/comments/{commentId}
     */
    @Operation(summary = "Update comment", description = "Update comment content")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal SessionUser sessionUser,
            @Parameter(description = "Comment ID", example = "1") @PathVariable Long commentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Comment update request", required = true)
            @Valid @RequestBody CommentUpdateRequest request

    ) {
        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);
        log.info("PUT /api/comments/{} - userId :{} - newContent length: {}", commentId, userId, request.getContent().length());

        Comment comment = commentWriter.updateComment(userIdentity, commentId, request.getContent());

        // Re-fetch with user information for response
        CommentRead commentRead = commentReader.getById(new CommentIdentity(comment.getCommentId()));
        CommentResponse response = CommentResponse.from(commentRead);

        log.info("Comment updated - commentId: {}", response.getCommentId());
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 숨김 처리
     * POST /api/comments/{commentId}/hide
     */
    @Operation(summary = "Hide comment", description = "Mark comment as hidden")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully hidden"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PostMapping("/{commentId}/hide")
    public ResponseEntity<CommentResponse> hideComment(
            @AuthenticationPrincipal SessionUser sessionUser,
            @Parameter(description = "Comment ID", example = "1") @PathVariable Long commentId) {
        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);
        log.info("POST /api/comments/{}/hide - userId:{}", commentId, userId);

        Comment comment = commentWriter.hideComment(userIdentity, commentId);

        // Re-fetch with user information for response
        CommentRead commentRead = commentReader.getById(new CommentIdentity(comment.getCommentId()));
        CommentResponse response = CommentResponse.from(commentRead);

        log.info("Comment hidden - commentId: {}", response.getCommentId());
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 숨김 해제
     * POST /api/comments/{commentId}/show
     */
    @Operation(summary = "Show comment", description = "Make hidden comment visible again")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully shown"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PostMapping("/{commentId}/show")
    public ResponseEntity<CommentResponse> showComment(
            @AuthenticationPrincipal SessionUser sessionUser,
            @Parameter(description = "Comment ID", example = "1") @PathVariable Long commentId) {

        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);
        log.info("POST /api/comments/{}/show - userId: {}", commentId, userId);

        Comment comment = commentWriter.showComment(userIdentity, commentId);

        // Re-fetch with user information for response
        CommentRead commentRead = commentReader.getById(new CommentIdentity(comment.getCommentId()));
        CommentResponse response = CommentResponse.from(commentRead);

        log.info("Comment shown - commentId: {}", response.getCommentId());
        return ResponseEntity.ok(response);
    }
}
