package dev.devrunner.api.reaction;

import dev.devrunner.api.reaction.dto.ReactionRequest;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.reaction.ReactionWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static dev.devrunner.api.LoginSessionUtils.readUserIdOrThrow;

/**
 * Reaction REST API 컨트롤러
 *
 * 좋아요/싫어요 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reaction", description = "Reaction API")
public class ReactionApiController {

    private final ReactionWriter reactionWriter;

    /**
     * 좋아요 추가
     * POST /api/reactions/like
     */
    @Operation(summary = "Add like", description = "Add a like reaction to a target")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully added"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/like")
    public ResponseEntity<Void> likeUp(
            @AuthenticationPrincipal SessionUser sessionUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reaction request", required = true)
            @Valid @RequestBody ReactionRequest request) {
        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);
        log.info("POST /api/reactions/like - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());

        reactionWriter.likeUp(userIdentity, request.getTargetType(), request.getTargetId());

        log.info("Like added - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());
        return ResponseEntity.ok().build();
    }

    /**
     * 싫어요 추가
     * POST /api/reactions/dislike
     */
    @Operation(summary = "Add dislike", description = "Add a dislike reaction to a target")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully added"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/dislike")
    public ResponseEntity<Void> dislikeUp(
            @AuthenticationPrincipal SessionUser sessionUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reaction request", required = true)
            @Valid @RequestBody ReactionRequest request) {
        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);
        log.info("POST /api/reactions/dislike - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());

        reactionWriter.dislikeUp(userIdentity, request.getTargetType(), request.getTargetId());

        log.info("Dislike added - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());
        return ResponseEntity.ok().build();
    }

    /**
     * 좋아요 취소
     * DELETE /api/reactions/like
     */
    @Operation(summary = "Remove like", description = "Remove a like reaction from a target")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Successfully removed"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @DeleteMapping("/like")
    public ResponseEntity<Void> likeDown(
            @AuthenticationPrincipal SessionUser sessionUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reaction request", required = true)
            @Valid @RequestBody ReactionRequest request) {
        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);
        log.info("DELETE /api/reactions/like - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());

        reactionWriter.likeDown(userIdentity, request.getTargetType(), request.getTargetId());

        log.info("Like removed - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 싫어요 취소
     * DELETE /api/reactions/dislike
     */
    @Operation(summary = "Remove dislike", description = "Remove a dislike reaction from a target")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Successfully removed"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @DeleteMapping("/dislike")
    public ResponseEntity<Void> dislikeDown(
            @AuthenticationPrincipal SessionUser sessionUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reaction request", required = true)
            @Valid @RequestBody ReactionRequest request) {
        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);
        log.info("DELETE /api/reactions/dislike - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());

        reactionWriter.dislikeDown(userIdentity, request.getTargetType(), request.getTargetId());

        log.info("Dislike removed - userId: {}, targetType: {}, targetId: {}",
                userId, request.getTargetType(), request.getTargetId());
        return ResponseEntity.noContent().build();
    }
}
