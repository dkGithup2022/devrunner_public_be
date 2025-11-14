package dev.devrunner.api.communitypost;

import dev.devrunner.api.communitypost.dto.CommunityPostRequest;
import dev.devrunner.api.communitypost.dto.CommunityPostResponse;
import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.communitypost.CommunityPostReader;
import dev.devrunner.service.communitypost.CommunityPostWriter;
import dev.devrunner.service.communitypost.dto.CommunityPostUpsertCommand;
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

import static dev.devrunner.api.LoginSessionUtils.readUserIdOrThrow;

/**
 * CommunityPost REST API 컨트롤러
 * <p>
 * Reader/Writer 패턴을 활용하여 CQRS 기반 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/community-posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Community Post", description = "Community post API")
public class CommunityPostApiController {

    private final CommunityPostReader communityPostReader;
    private final CommunityPostWriter communityPostWriter;

    /**
     * 커뮤니티 게시글 조회
     * GET /api/community-posts/{communityPostId}
     */
    @Operation(summary = "Get community post", description = "Retrieve a community post by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{communityPostId}")
    public ResponseEntity<CommunityPostResponse> getCommunityPost(
            @AuthenticationPrincipal SessionUser sessionUser,
            @Parameter(description = "Community post ID", example = "1") @PathVariable Long communityPostId) {
        log.info("GET /api/community-posts/{}", communityPostId);

        CommunityPostRead communityPostRead = communityPostReader.read(new CommunityPostIdentity(communityPostId));
        Long viewerUserId = sessionUser != null ? sessionUser.getUserId() : null;
        CommunityPostResponse response = CommunityPostResponse.from(communityPostRead, viewerUserId);

        log.info("Retrieved community post - communityPostId: {}", response.getCommunityPostId());
        return ResponseEntity.ok(response);
    }

    /**
     * 커뮤니티 게시글 생성/수정
     * POST /api/community-posts
     */
    @Operation(summary = "Create or update community post", description = "Create a new community post or update existing one")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully created or updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<CommunityPostResponse> upsertCommunityPost(
            @AuthenticationPrincipal SessionUser sessionUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Community post request", required = true)
            @Valid @RequestBody CommunityPostRequest request) {
        var userId = readUserIdOrThrow(sessionUser);
        log.info("POST /api/community-posts - userId: {}, category: {}",
                userId, request.getCategory());

        // Request → Command 변환
        CommunityPostUpsertCommand command = new CommunityPostUpsertCommand(
                userId,
                request.getCommunityPostId(),
                request.getCategory(),
                request.getTitle(),
                request.getMarkdownBody(),
                request.getCompany(),
                request.getLocation(),
                request.getJobId(),
                request.getCommentId()
        );

        CommunityPost saved = communityPostWriter.upsert(command);

        // Re-fetch with user information for response
        CommunityPostRead communityPostRead = communityPostReader.getById(new CommunityPostIdentity(saved.getCommunityPostId()));
        CommunityPostResponse response = CommunityPostResponse.from(communityPostRead, userId);

        log.info("Community post upserted - communityPostId: {}", response.getCommunityPostId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 커뮤니티 게시글 삭제
     * DELETE /api/community-posts/{communityPostId}
     */
    @Operation(summary = "Delete community post", description = "Delete a community post by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @DeleteMapping("/{communityPostId}")
    public ResponseEntity<Void> deleteCommunityPost(
            @AuthenticationPrincipal SessionUser sessionUser,
            @Parameter(description = "Community post ID", example = "1") @PathVariable Long communityPostId) {

        var userId = readUserIdOrThrow(sessionUser);
        var userIdentity = new UserIdentity(userId);
        log.info("DELETE /api/community-posts/{} - userId : {} ", communityPostId, userId);

        communityPostWriter.delete(userIdentity, new CommunityPostIdentity(communityPostId));

        log.info("Community post deleted - communityPostId: {}", communityPostId);
        return ResponseEntity.noContent().build();
    }
}
